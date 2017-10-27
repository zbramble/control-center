package com.wenba.scheduler.search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wenba.scheduler.AbstractResult.StatusCode;
import com.wenba.scheduler.ISchedulerStrategy;
import com.wenba.scheduler.SchedulerConstants;
import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.SchedulerConfiguration;

/**
 * @author zhangbo
 *
 */
public class SearchClassicPoemStrategy implements
        ISchedulerStrategy<SearchParam, SearchResult> {

    // constants

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public SearchResult excute(SearchParam searchParam) {
        SearchResult searchClassicPoemResult = new SearchResult();
        SearchServer searchClassicPoemServer = searchParam.getSearchServer();
        searchClassicPoemResult.setSchedulerResult(null);

        // search server is null
        if (searchClassicPoemServer == null) {
            searchClassicPoemResult.setExcuteTime(0);
            searchClassicPoemResult.setStatusCode(StatusCode.NOSERVER);
            return searchClassicPoemResult;
        }

        searchClassicPoemServer.getSearchServerStatistics()
                .incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = searchParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = searchParam
                .getSchedulerControllerStatistics();
        long searchClassicPoemStartTime = System.currentTimeMillis();
        long searchClassicPoemStopTime;
        HttpPost post = new HttpPost(
                SchedulerConstants.HTTP_URL
                        + searchClassicPoemServer.getIp()
                        + ((searchClassicPoemServer.getPort() != null && !""
                                .equals(searchClassicPoemServer.getPort())) ? (SchedulerConstants.COLON + searchClassicPoemServer
                                .getPort()) : "")
                        + searchClassicPoemServer.getUrl()
                        + searchParam.getClassicPoemUrl());
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("keywords", searchParam
                .getKeywords()));
        if (searchParam.getLimit() > 0) {
            postData.add(new BasicNameValuePair("limit", String
                    .valueOf(searchParam.getLimit())));
        }
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getConnectTimeout())
                .setSocketTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getSearchTimeout()).build();
        post.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData,
                    "UTF-8");
            post.setEntity(entity);
            schedulerControllerStatistics
                    .incrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} ISCHCR: {}", searchParam.getUid(),
                        searchParam.getFid(), schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
            response = schedulerConfiguration.getHttpClient().execute(post,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                schedulerConfiguration.calcMarks(searchClassicPoemServer, true,
                        true);
                HttpEntity searchResponse = response.getEntity();
                if (searchResponse != null) {
                    String searchResponseResult = EntityUtils
                            .toString(searchResponse);
                    if (searchResponseResult != null
                            && !"".equals(searchResponseResult)) {
                        searchClassicPoemResult
                                .setSchedulerResult(searchResponseResult);
                        searchClassicPoemResult.setStatusCode(StatusCode.OK);
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE", searchParam.getUid(),
                                    searchParam.getFid(),
                                    searchClassicPoemServer.getId());
                        }
                        searchClassicPoemServer.getSearchServerStatistics()
                                .incrementAndGetHeseNum();
                        searchClassicPoemResult
                                .setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE", searchParam.getUid(),
                                searchParam.getFid(),
                                searchClassicPoemServer.getId());
                    }
                    searchClassicPoemServer.getSearchServerStatistics()
                            .incrementAndGetHeneNum();
                    searchClassicPoemResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HSNE {}", searchParam.getUid(),
                            searchParam.getFid(), searchClassicPoemServer
                                    .getId(), response.getStatusLine()
                                    .getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                            searchClassicPoemServer.getId());
                }
                searchClassicPoemServer.getSearchServerStatistics()
                        .incrementAndGetHsneNum();
                // TODO schedulerConfiguration.calcMarks(searchServer, false,
                // true);
                searchClassicPoemResult
                        .setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} HHCE", searchParam.getUid(),
                        searchParam.getFid(), searchClassicPoemServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} HttpHostConnectException",
                        searchClassicPoemServer.getId());
            }
            searchClassicPoemServer.getSearchServerStatistics()
                    .incrementAndGetHhceNum();
            schedulerConfiguration.calcMarks(searchClassicPoemServer, false,
                    true);
            searchClassicPoemResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CTE", searchParam.getUid(),
                        searchParam.getFid(), searchClassicPoemServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} ConnectTimeoutException",
                        searchClassicPoemServer.getId());
            }
            searchClassicPoemServer.getSearchServerStatistics()
                    .incrementAndGetCteNum();
            schedulerConfiguration.calcMarks(searchClassicPoemServer, false,
                    true);
            searchClassicPoemResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} NHRE", searchParam.getUid(),
                        searchParam.getFid(), searchClassicPoemServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} NoHttpResponseException",
                        searchClassicPoemServer.getId());
            }
            searchClassicPoemServer.getSearchServerStatistics()
                    .incrementAndGetNhreNum();
            // TODO schedulerConfiguration.calcMarks(searchServer, false, true);
            searchClassicPoemResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} STE", searchParam.getUid(),
                        searchParam.getFid(), searchClassicPoemServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} SocketTimeoutException",
                        searchClassicPoemServer.getId());
            }
            searchClassicPoemServer.getSearchServerStatistics()
                    .incrementAndGetSteNum();
            // TODO schedulerConfiguration.calcMarks(searchServer, false, true);
            searchClassicPoemResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", searchParam.getUid(),
                        searchParam.getFid(), searchClassicPoemServer.getId());
            }
            searchClassicPoemServer.getSearchServerStatistics()
                    .incrementAndGetCpeNum();
            searchClassicPoemResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} UEE", searchParam.getUid(),
                        searchParam.getFid(), searchClassicPoemServer.getId());
            }
            searchClassicPoemServer.getSearchServerStatistics()
                    .incrementAndGetUeeNum();
            searchClassicPoemResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", searchParam.getUid(),
                        searchParam.getFid(), searchClassicPoemServer.getId());
            }
            searchClassicPoemServer.getSearchServerStatistics()
                    .incrementAndGetIoeNum();
            searchClassicPoemResult.setStatusCode(StatusCode.OTHER);
        } catch (IndexOutOfBoundsException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOOBE", searchParam.getUid(),
                        searchParam.getFid(), searchClassicPoemServer.getId());
            }
            searchClassicPoemServer.getSearchServerStatistics()
                    .incrementAndGetIoobeNum();
            searchClassicPoemResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", searchParam.getUid(),
                        searchParam.getFid(), searchClassicPoemServer.getId());
            }
            searchClassicPoemServer.getSearchServerStatistics()
                    .incrementAndGetOeNum();
            searchClassicPoemResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} IOE", searchParam.getUid(),
                                searchParam.getFid(),
                                searchClassicPoemServer.getId());
                    }
                    searchClassicPoemServer.getSearchServerStatistics()
                            .incrementAndGetIoeNum();
                    searchClassicPoemResult.setStatusCode(StatusCode.OTHER);
                }
            }
            searchClassicPoemStopTime = System.currentTimeMillis();
            schedulerControllerStatistics
                    .decrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} DSCHCR: {}", searchParam.getUid(),
                        searchParam.getFid(), schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
        }
        long excuteTime = searchClassicPoemStopTime
                - searchClassicPoemStartTime;
        searchClassicPoemResult.setExcuteTime(excuteTime);
        searchClassicPoemServer.getSearchServerStatistics()
                .decrementAndGetCurrentRequestNum();
        return searchClassicPoemResult;
    }
}

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
public class SearchHomeworkStrategy implements
        ISchedulerStrategy<SearchParam, SearchResult> {

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public SearchResult excute(SearchParam searchHomeworkParam) {
        SearchResult searchHomeworkResult = new SearchResult();
        SearchServer searchHomeworkServer = searchHomeworkParam
                .getSearchServer();

        // search server is null
        if (searchHomeworkServer == null) {
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setExcuteTime(0);
            searchHomeworkResult.setStatusCode(StatusCode.NOSERVER);
            return searchHomeworkResult;
        }

        searchHomeworkServer.getSearchServerStatistics()
                .incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = searchHomeworkParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = searchHomeworkParam
                .getSchedulerControllerStatistics();
        long searchHomeworkStartTime = System.currentTimeMillis();
        long searchHomeworkStopTime;
        HttpPost post = new HttpPost(
                SchedulerConstants.HTTP_URL
                        + searchHomeworkServer.getIp()
                        + ((searchHomeworkServer.getPort() != null && !""
                                .equals(searchHomeworkServer.getPort())) ? (SchedulerConstants.COLON + searchHomeworkServer
                                .getPort()) : "")
                        + searchHomeworkServer.getUrl());
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("uids", searchHomeworkParam
                .getUids()));
        postData.add(new BasicNameValuePair("app", searchHomeworkParam.getApp()));
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getConnectTimeout())
                .setSocketTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getSearchHomeworkTimeout()).build();
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
                debugLogger.info("{} {} IMHCHCR: {}", searchHomeworkParam
                        .getUid(), searchHomeworkParam.getFid(),
                        schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
            response = schedulerConfiguration.getHttpClient().execute(post,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                schedulerConfiguration.calcMarks(searchHomeworkServer, true,
                        true);
                HttpEntity searchHomeworkResponse = response.getEntity();
                if (searchHomeworkResponse != null) {
                    String searchHomeworkResponseResult = EntityUtils
                            .toString(searchHomeworkResponse);
                    if (searchHomeworkResponseResult != null
                            && !"".equals(searchHomeworkResponseResult)) {
                        searchHomeworkResult
                                .setSchedulerResult(searchHomeworkResponseResult);
                        searchHomeworkResult.setStatusCode(StatusCode.OK);
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE",
                                    searchHomeworkParam.getUid(),
                                    searchHomeworkParam.getFid(),
                                    searchHomeworkServer.getId());
                        }
                        searchHomeworkServer.getSearchServerStatistics()
                                .incrementAndGetHeseNum();
                        searchHomeworkResult.setSchedulerResult(null);
                        searchHomeworkResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE",
                                searchHomeworkParam.getUid(),
                                searchHomeworkParam.getFid(),
                                searchHomeworkServer.getId());
                    }
                    searchHomeworkServer.getSearchServerStatistics()
                            .incrementAndGetHeneNum();
                    searchHomeworkResult.setSchedulerResult(null);
                    searchHomeworkResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HSNE {}", searchHomeworkParam
                            .getUid(), searchHomeworkParam.getFid(),
                            searchHomeworkServer.getId(), response
                                    .getStatusLine().getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                            searchHomeworkServer.getId());
                }
                searchHomeworkServer.getSearchServerStatistics()
                        .incrementAndGetHsneNum();
                // TODO schedulerConfiguration.calcMarks(searchHomeworkServer,
                // false,
                // true);
                searchHomeworkResult.setSchedulerResult(null);
                searchHomeworkResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} HHCE", searchHomeworkParam.getUid(),
                        searchHomeworkParam.getFid(),
                        searchHomeworkServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} HttpHostConnectException",
                        searchHomeworkServer.getId());
            }
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetHhceNum();
            schedulerConfiguration.calcMarks(searchHomeworkServer, false, true);
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CTE", searchHomeworkParam.getUid(),
                        searchHomeworkParam.getFid(),
                        searchHomeworkServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} ConnectTimeoutException",
                        searchHomeworkServer.getId());
            }
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetCteNum();
            schedulerConfiguration.calcMarks(searchHomeworkServer, false, true);
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} NHRE", searchHomeworkParam.getUid(),
                        searchHomeworkParam.getFid(),
                        searchHomeworkServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} NoHttpResponseException",
                        searchHomeworkServer.getId());
            }
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetNhreNum();
            // TODO schedulerConfiguration.calcMarks(searchHomeworkServer,
            // false, true);
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} STE", searchHomeworkParam.getUid(),
                        searchHomeworkParam.getFid(),
                        searchHomeworkServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} SocketTimeoutException",
                        searchHomeworkServer.getId());
            }
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetSteNum();
            // TODO schedulerConfiguration.calcMarks(searchHomeworkServer,
            // false, true);
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", searchHomeworkParam.getUid(),
                        searchHomeworkParam.getFid(),
                        searchHomeworkServer.getId());
            }
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetCpeNum();
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} UEE", searchHomeworkParam.getUid(),
                        searchHomeworkParam.getFid(),
                        searchHomeworkServer.getId());
            }
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetUeeNum();
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", searchHomeworkParam.getUid(),
                        searchHomeworkParam.getFid(),
                        searchHomeworkServer.getId());
            }
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetIoeNum();
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setStatusCode(StatusCode.OTHER);
        } catch (IndexOutOfBoundsException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOOBE", searchHomeworkParam.getUid(),
                        searchHomeworkParam.getFid(),
                        searchHomeworkServer.getId());
            }
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetIoobeNum();
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", searchHomeworkParam.getUid(),
                        searchHomeworkParam.getFid(),
                        searchHomeworkServer.getId());
            }
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetOeNum();
            searchHomeworkResult.setSchedulerResult(null);
            searchHomeworkResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} IOE",
                                searchHomeworkParam.getUid(),
                                searchHomeworkParam.getFid(),
                                searchHomeworkServer.getId());
                    }
                    searchHomeworkServer.getSearchServerStatistics()
                            .incrementAndGetIoeNum();
                    searchHomeworkResult.setSchedulerResult(null);
                    searchHomeworkResult.setStatusCode(StatusCode.OTHER);
                }
            }
            searchHomeworkStopTime = System.currentTimeMillis();
            schedulerControllerStatistics
                    .decrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} DMHCHCR: {}", searchHomeworkParam
                        .getUid(), searchHomeworkParam.getFid(),
                        schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
        }
        long excuteTime = searchHomeworkStopTime - searchHomeworkStartTime;
        searchHomeworkResult.setExcuteTime(excuteTime);
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchHomeworkServerExecTimeDebugSwitch()) {
            searchHomeworkServer.getSearchServerStatistics()
                    .addAndGetExecTimeByHomework(excuteTime);
            searchHomeworkServer.getSearchServerStatistics()
                    .incrementAndGetExecTimeByHomeworkNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isSearchHomeworkServerExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} SMHET: {}",
                    searchHomeworkParam.getUid(), searchHomeworkParam.getFid(),
                    searchHomeworkServer.getId(), excuteTime);
        }
        searchHomeworkServer.getSearchServerStatistics()
                .decrementAndGetCurrentRequestNum();
        return searchHomeworkResult;
    }
}

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
import com.wenba.scheduler.config.SchedulerConfiguration;

/**
 * @author zhangbo
 *
 */
public class SearchMatrixStrategy implements
        ISchedulerStrategy<SearchParam, SearchResult> {

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public SearchResult excute(SearchParam searchParam) {
        SearchResult searchResult = new SearchResult();
        SearchServer searchServer = searchParam.getSearchServer();

        // search server is null
        if (searchServer == null) {
            searchResult.setSchedulerResult(null);
            searchResult.setExcuteTime(0);
            searchResult.setStatusCode(StatusCode.NOSERVER);
            return searchResult;
        }

        SchedulerConfiguration schedulerConfiguration = searchParam
                .getSchedulerConfiguration();
        long searchStartTime = System.currentTimeMillis();
        long searchStopTime;
        HttpPost post = new HttpPost(
                SchedulerConstants.HTTP_URL
                        + searchServer.getIp()
                        + ((searchServer.getPort() != null && !""
                                .equals(searchServer.getPort())) ? (SchedulerConstants.COLON + searchServer
                                .getPort()) : "")
                        + searchServer.getUrl()
                        + schedulerConfiguration.getSystemDataConfiguration()
                                .getSearchQuery());
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("keywords", searchParam
                .getOcrResult().toString().replace("<BODY>", "")
                .replace("</BODY>", "")));
        postData.add(new BasicNameValuePair("task", "matrix"));
        postData.add(new BasicNameValuePair("bookids", searchParam.getBookIds()));
        debugLogger.info("keywords:{}, task:{}, bookids:{}",
                searchParam.getOcrResult().toString().replace("<BODY>", "")
                        .replace("</BODY>", ""), "matrix",
                searchParam.getBookIds()); // TODO
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
            response = schedulerConfiguration.getHttpClient().execute(post,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity searchResponse = response.getEntity();
                if (searchResponse != null) {
                    String searchResponseResult = EntityUtils
                            .toString(searchResponse);
                    debugLogger.info("searchResponseResult:{}",
                            searchResponseResult); // TODO
                    if (searchResponseResult != null
                            && !"".equals(searchResponseResult)) {
                        searchResult.setSchedulerResult(searchResponseResult);
                        searchResult.setStatusCode(StatusCode.OK);
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE", searchParam.getUid(),
                                    searchParam.getFid(), searchServer.getId());
                        }
                        searchServer.getSearchServerStatistics()
                                .incrementAndGetHeseNum();
                        searchResult.setSchedulerResult(null);
                        searchResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE", searchParam.getUid(),
                                searchParam.getFid(), searchServer.getId());
                    }
                    searchServer.getSearchServerStatistics()
                            .incrementAndGetHeneNum();
                    searchResult.setSchedulerResult(null);
                    searchResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HSNE {}", searchParam.getUid(),
                            searchParam.getFid(), searchServer.getId(),
                            response.getStatusLine().getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                            searchServer.getId());
                }
                searchServer.getSearchServerStatistics()
                        .incrementAndGetHsneNum();
                searchResult.setSchedulerResult(null);
                searchResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} HHCE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} HttpHostConnectException",
                        searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetHhceNum();
            searchResult.setSchedulerResult(null);
            searchResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CTE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} ConnectTimeoutException",
                        searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetCteNum();
            searchResult.setSchedulerResult(null);
            searchResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} NHRE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} NoHttpResponseException",
                        searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetNhreNum();
            searchResult.setSchedulerResult(null);
            searchResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} STE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} SocketTimeoutException",
                        searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetSteNum();
            searchResult.setSchedulerResult(null);
            searchResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetCpeNum();
            searchResult.setSchedulerResult(null);
            searchResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} UEE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetUeeNum();
            searchResult.setSchedulerResult(null);
            searchResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetIoeNum();
            searchResult.setSchedulerResult(null);
            searchResult.setStatusCode(StatusCode.OTHER);
        } catch (IndexOutOfBoundsException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOOBE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetIoobeNum();
            searchResult.setSchedulerResult(null);
            searchResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetOeNum();
            searchResult.setSchedulerResult(null);
            searchResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} IOE", searchParam.getUid(),
                                searchParam.getFid(), searchServer.getId());
                    }
                    searchServer.getSearchServerStatistics()
                            .incrementAndGetIoeNum();
                    searchResult.setSchedulerResult(null);
                    searchResult.setStatusCode(StatusCode.OTHER);
                }
            }
            searchStopTime = System.currentTimeMillis();
        }
        long excuteTime = searchStopTime - searchStartTime;
        searchResult.setExcuteTime(excuteTime);
        return searchResult;
    }
}

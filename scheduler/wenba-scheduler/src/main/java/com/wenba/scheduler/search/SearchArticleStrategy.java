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
import com.wenba.scheduler.search.SearchParam.ArticleType;

/**
 * @author zhangbo
 *
 */
public class SearchArticleStrategy implements
        ISchedulerStrategy<SearchParam, SearchResult> {

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public SearchResult excute(SearchParam searchArticleParam) {
        SearchResult searchArticleResult = new SearchResult();
        SearchServer searchArticleServer = searchArticleParam.getSearchServer();

        // search article server is null
        if (searchArticleServer == null) {
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setExcuteTime(0);
            searchArticleResult.setStatusCode(StatusCode.NOSERVER);
            return searchArticleResult;
        }

        searchArticleServer.getSearchServerStatistics()
                .incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = searchArticleParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = searchArticleParam
                .getSchedulerControllerStatistics();
        long searchArticleStartTime = System.currentTimeMillis();
        long searchArticleStopTime;
        HttpPost post = new HttpPost(
                SchedulerConstants.HTTP_URL
                        + searchArticleServer.getIp()
                        + ((searchArticleServer.getPort() != null && !""
                                .equals(searchArticleServer.getPort())) ? (SchedulerConstants.COLON + searchArticleServer
                                .getPort()) : "")
                        + searchArticleServer.getUrl()
                        + searchArticleParam.getArticleUrl());
        List<NameValuePair> postData = new ArrayList<NameValuePair>();

        if (ArticleType.QUERY.equals(searchArticleParam.getArticleType())) {
            postData.add(new BasicNameValuePair("uid", searchArticleParam
                    .getUid()));
            postData.add(new BasicNameValuePair("keywords", searchArticleParam
                    .getKeywords()));
            postData.add(new BasicNameValuePair("filter", searchArticleParam
                    .getFilter()));
            postData.add(new BasicNameValuePair("grade", searchArticleParam
                    .getGrade()));
            if (searchArticleParam.getTagsLimit() != null) {
                postData.add(new BasicNameValuePair("tagsLimit", String
                        .valueOf(searchArticleParam.getTagsLimit())));
            }
            if (searchArticleParam.getPageNo() != null) {
                postData.add(new BasicNameValuePair("pageNo", String
                        .valueOf(searchArticleParam.getPageNo())));
            }
            if (searchArticleParam.getPageSize() != null) {
                postData.add(new BasicNameValuePair("pageSize", String
                        .valueOf(searchArticleParam.getPageSize())));
            }
            postData.add(new BasicNameValuePair("subject", searchArticleParam
                    .getSubject()));
        } else if (ArticleType.QUERY_BY_ID.equals(searchArticleParam
                .getArticleType())) {
            postData.add(new BasicNameValuePair("ids", searchArticleParam
                    .getIds()));
        } else if (ArticleType.AUTO_COMPLETE.equals(searchArticleParam
                .getArticleType())) {
            postData.add(new BasicNameValuePair("keywords", searchArticleParam
                    .getKeywords()));
        }

        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getConnectTimeout())
                .setSocketTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getSearchArticleTimeout()).build();
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
                debugLogger.info("{} {} ISACHCR: {}", searchArticleParam
                        .getUid(), searchArticleParam.getFid(),
                        schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
            response = schedulerConfiguration.getHttpClient().execute(post,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                schedulerConfiguration.calcMarks(searchArticleServer, true,
                        true);
                HttpEntity searchResponse = response.getEntity();
                if (searchResponse != null) {
                    String searchResponseResult = EntityUtils
                            .toString(searchResponse);
                    if (searchResponseResult != null
                            && !"".equals(searchResponseResult)) {
                        searchArticleResult
                                .setSchedulerResult(searchResponseResult);
                        searchArticleResult.setStatusCode(StatusCode.OK);
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE",
                                    searchArticleParam.getUid(),
                                    searchArticleParam.getFid(),
                                    searchArticleServer.getId());
                        }
                        searchArticleServer.getSearchServerStatistics()
                                .incrementAndGetHeseNum();
                        searchArticleResult.setSchedulerResult(null);
                        searchArticleResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE",
                                searchArticleParam.getUid(),
                                searchArticleParam.getFid(),
                                searchArticleServer.getId());
                    }
                    searchArticleServer.getSearchServerStatistics()
                            .incrementAndGetHeneNum();
                    searchArticleResult.setSchedulerResult(null);
                    searchArticleResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HSNE {}", searchArticleParam
                            .getUid(), searchArticleParam.getFid(),
                            searchArticleServer.getId(), response
                                    .getStatusLine().getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                            searchArticleServer.getId());
                }
                searchArticleServer.getSearchServerStatistics()
                        .incrementAndGetHsneNum();
                // TODO schedulerConfiguration.calcMarks(searchArticleServer,
                // false,
                // true);
                searchArticleResult.setSchedulerResult(null);
                searchArticleResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} HHCE", searchArticleParam.getUid(),
                        searchArticleParam.getFid(),
                        searchArticleServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} HttpHostConnectException",
                        searchArticleServer.getId());
            }
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetHhceNum();
            schedulerConfiguration.calcMarks(searchArticleServer, false, true);
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CTE", searchArticleParam.getUid(),
                        searchArticleParam.getFid(),
                        searchArticleServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} ConnectTimeoutException",
                        searchArticleServer.getId());
            }
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetCteNum();
            schedulerConfiguration.calcMarks(searchArticleServer, false, true);
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} NHRE", searchArticleParam.getUid(),
                        searchArticleParam.getFid(),
                        searchArticleServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} NoHttpResponseException",
                        searchArticleServer.getId());
            }
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetNhreNum();
            // TODO schedulerConfiguration.calcMarks(searchArticleServer, false,
            // true);
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} STE", searchArticleParam.getUid(),
                        searchArticleParam.getFid(),
                        searchArticleServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} SocketTimeoutException",
                        searchArticleServer.getId());
            }
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetSteNum();
            // TODO schedulerConfiguration.calcMarks(searchArticleServer, false,
            // true);
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", searchArticleParam.getUid(),
                        searchArticleParam.getFid(),
                        searchArticleServer.getId());
            }
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetCpeNum();
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} UEE", searchArticleParam.getUid(),
                        searchArticleParam.getFid(),
                        searchArticleServer.getId());
            }
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetUeeNum();
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", searchArticleParam.getUid(),
                        searchArticleParam.getFid(),
                        searchArticleServer.getId());
            }
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetIoeNum();
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setStatusCode(StatusCode.OTHER);
        } catch (IndexOutOfBoundsException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOOBE", searchArticleParam.getUid(),
                        searchArticleParam.getFid(),
                        searchArticleServer.getId());
            }
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetIoobeNum();
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", searchArticleParam.getUid(),
                        searchArticleParam.getFid(),
                        searchArticleServer.getId());
            }
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetOeNum();
            searchArticleResult.setSchedulerResult(null);
            searchArticleResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} IOE",
                                searchArticleParam.getUid(),
                                searchArticleParam.getFid(),
                                searchArticleServer.getId());
                    }
                    searchArticleServer.getSearchServerStatistics()
                            .incrementAndGetIoeNum();
                    searchArticleResult.setSchedulerResult(null);
                    searchArticleResult.setStatusCode(StatusCode.OTHER);
                }
            }
            searchArticleStopTime = System.currentTimeMillis();
            schedulerControllerStatistics
                    .decrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} DSACHCR: {}", searchArticleParam
                        .getUid(), searchArticleParam.getFid(),
                        schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
        }
        long excuteTime = searchArticleStopTime - searchArticleStartTime;
        searchArticleResult.setExcuteTime(excuteTime);
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchArticleServerExecTimeDebugSwitch()) {
            searchArticleServer.getSearchServerStatistics()
                    .addAndGetExecTimeByArticle(excuteTime);
            searchArticleServer.getSearchServerStatistics()
                    .incrementAndGetExecTimeByArticleNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isSearchArticleServerExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} SAET: {}", searchArticleParam.getUid(),
                    searchArticleParam.getFid(), searchArticleServer.getId(),
                    excuteTime);
        }
        searchArticleServer.getSearchServerStatistics()
                .decrementAndGetCurrentRequestNum();
        return searchArticleResult;
    }
}

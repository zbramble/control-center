package com.wenba.scheduler.search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

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
public class SearchStrategy implements
        ISchedulerStrategy<SearchParam, SearchResult> {

    // constants
    private static final String SEARCH_RESULT_TYPE_OK = "ok";
    private static final String SEARCH_RESULT_SIMILARITY = "similarity";
    private static final String SEARCH_RESULT_ID = "id";

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public SearchResult excute(SearchParam searchParam) {
        SearchResult searchResult = new SearchResult();
        SearchServer searchServer = searchParam.getSearchServer();
        searchResult.setSchedulerResult(null);
        searchResult.setVersion(String.valueOf(-1));

        // search server is null
        if (searchServer == null) {
            searchResult.setExcuteTime(0);
            searchResult.setStatusCode(StatusCode.NOSERVER);
            return searchResult;
        }

        searchServer.getSearchServerStatistics()
                .incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = searchParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = searchParam
                .getSchedulerControllerStatistics();
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
        if (searchParam.getIndex() != null
                && !searchParam.getIndex().matches("^\\s*$")) {
            postData.add(new BasicNameValuePair("index", searchParam.getIndex()));
        }
        if (searchParam.getUser() != null
                && !searchParam.getUser().matches("^\\s*$")) {
            postData.add(new BasicNameValuePair("user", searchParam.getUser()));
        }
        if (searchParam.getToken() != null
                && !searchParam.getToken().matches("^\\s*$")) {
            postData.add(new BasicNameValuePair("token", searchParam.getToken()));
        }
        postData.add(new BasicNameValuePair("limit", String.valueOf(searchParam
                .getLimit())));
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
        String searchResponseResult = ""; // TODO
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
                schedulerConfiguration.calcMarks(searchServer, true, true);
                HttpEntity searchResponse = response.getEntity();
                if (searchResponse != null) {
                    searchResponseResult = EntityUtils.toString(searchResponse);
                    if (searchResponseResult != null
                            && !"".equals(searchResponseResult)) {
                        // 把json字符串转换成searchJsonResult对象
                        SearchJsonResult searchJsonResult = schedulerConfiguration
                                .getMapper().readValue(searchResponseResult,
                                        SearchJsonResult.class);
                        searchResult.setVersion(searchJsonResult.getVersion());
                        if (SEARCH_RESULT_TYPE_OK.equals(searchJsonResult
                                .getType())) {
                            JSONArray questionsJsonArray = searchJsonResult
                                    .getQuestions();
                            // set max similarity
                            float maxSimilarity = (float) questionsJsonArray
                                    .getJSONObject(0).getDouble(
                                            SEARCH_RESULT_SIMILARITY);
                            searchResult.setMaxSimilarity(maxSimilarity);

                            // set search result
                            List<com.xueba100.mining.common.SearchResult> searchResultList = new ArrayList<com.xueba100.mining.common.SearchResult>();
                            for (int i = 0; i < questionsJsonArray.size(); ++i) {
                                searchResultList
                                        .add(new com.xueba100.mining.common.SearchResult(
                                                Integer.parseInt(questionsJsonArray
                                                        .getJSONObject(i)
                                                        .getString(
                                                                SEARCH_RESULT_ID)),
                                                questionsJsonArray
                                                        .getJSONObject(i)
                                                        .getDouble(
                                                                SEARCH_RESULT_SIMILARITY)));
                            }
                            searchResult.setSearchResultList(searchResultList);
                            searchResult.setStatusCode(StatusCode.OK);
                        } else {
                            // set search result:null
                            searchResult.setSearchResultList(null);
                            searchResult.setStatusCode(StatusCode.NORESULT);
                        }
                        searchResult.setSchedulerResult(searchResponseResult);
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE", searchParam.getUid(),
                                    searchParam.getFid(), searchServer.getId());
                        }
                        searchServer.getSearchServerStatistics()
                                .incrementAndGetHeseNum();
                        searchResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE", searchParam.getUid(),
                                searchParam.getFid(), searchServer.getId());
                    }
                    searchServer.getSearchServerStatistics()
                            .incrementAndGetHeneNum();
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
                // TODO schedulerConfiguration.calcMarks(searchServer, false,
                // true);
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
            schedulerConfiguration.calcMarks(searchServer, false, true);
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
            schedulerConfiguration.calcMarks(searchServer, false, true);
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
            // TODO schedulerConfiguration.calcMarks(searchServer, false, true);
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
            // TODO schedulerConfiguration.calcMarks(searchServer, false, true);
            searchResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetCpeNum();
            searchResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} UEE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetUeeNum();
            searchResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetIoeNum();
            searchResult.setStatusCode(StatusCode.OTHER);
        } catch (IndexOutOfBoundsException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOOBE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
                debugLogger.error("IOOBE!", e); // TODO
                debugLogger.error("{} {} {}", searchParam.getUid(),
                        searchParam.getFid(), searchResponseResult); // TODO
            }
            searchServer.getSearchServerStatistics().incrementAndGetIoobeNum();
            searchResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", searchParam.getUid(),
                        searchParam.getFid(), searchServer.getId());
            }
            searchServer.getSearchServerStatistics().incrementAndGetOeNum();
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
                    searchResult.setStatusCode(StatusCode.OTHER);
                }
            }
            searchStopTime = System.currentTimeMillis();
            schedulerControllerStatistics
                    .decrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} DSCHCR: {}", searchParam.getUid(),
                        searchParam.getFid(), schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
        }
        long excuteTime = searchStopTime - searchStartTime;
        searchResult.setExcuteTime(excuteTime);
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchServerExecTimeDebugSwitch()) {
            searchServer.getSearchServerStatistics().addAndGetExecTime(
                    excuteTime);
            searchServer.getSearchServerStatistics()
                    .incrementAndGetExecTimeNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isSearchServerExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} SET: {}", searchParam.getUid(),
                    searchParam.getFid(), searchServer.getId(), excuteTime);
        }
        searchServer.getSearchServerStatistics()
                .decrementAndGetCurrentRequestNum();
        return searchResult;
    }
}

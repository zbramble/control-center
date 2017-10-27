package com.wenba.scheduler.em;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
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
public class EmStrategy implements ISchedulerStrategy<EmParam, EmResult> {

    // constants
    private static final String APPLICATION_JSON = "application/json";

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public EmResult excute(EmParam emParam) {
        EmResult emResult = new EmResult();
        EmServer emServer = emParam.getEmServer();
        emResult.setSchedulerResult(null);

        // em server is null
        if (emServer == null) {
            emResult.setExcuteTime(0);
            emResult.setStatusCode(StatusCode.NOSERVER);
            return emResult;
        }

        emServer.getEmServerStatistics().incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = emParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = emParam
                .getSchedulerControllerStatistics();
        long emStartTime = System.currentTimeMillis();
        long emStopTime;
        HttpPost post = new HttpPost(
                SchedulerConstants.HTTP_URL
                        + emServer.getIp()
                        + ((emServer.getPort() != null && !"".equals(emServer
                                .getPort())) ? (SchedulerConstants.COLON + emServer
                                .getPort()) : "")
                        + emServer.getUrl()
                        + schedulerConfiguration.getSystemDataConfiguration()
                                .getEmQuery());
        post.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getConnectTimeout())
                .setSocketTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getEmTimeout()).build();
        post.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            StringEntity entity = new StringEntity(emParam.getDatum()
                    .toString());
            post.setEntity(entity);
            schedulerControllerStatistics
                    .incrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} IECHCR: {}", emParam.getUid(), emParam
                        .getFid(), schedulerControllerStatistics
                        .getCurrentHttpClientRequestNum());
            }
            response = schedulerConfiguration.getHttpClient().execute(post,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                schedulerConfiguration.calcMarks(emServer, true, true);
                HttpEntity searchResponse = response.getEntity();
                if (searchResponse != null) {
                    String searchResponseResult = EntityUtils
                            .toString(searchResponse);
                    if (searchResponseResult != null
                            && !"".equals(searchResponseResult)) {
                        emResult.setSchedulerResult(searchResponseResult);
                        emResult.setStatusCode(StatusCode.OK);
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE", emParam.getUid(),
                                    emParam.getFid(), emServer.getId());
                        }
                        emServer.getEmServerStatistics()
                                .incrementAndGetHeseNum();
                        emResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE", emParam.getUid(),
                                emParam.getFid(), emServer.getId());
                    }
                    emServer.getEmServerStatistics().incrementAndGetHeneNum();
                    emResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HSNE {}", emParam.getUid(), emParam
                            .getFid(), emServer.getId(), response
                            .getStatusLine().getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                            emServer.getId());
                }
                emServer.getEmServerStatistics().incrementAndGetHsneNum();
                // TODO schedulerConfiguration.calcMarks(searchServer, false,
                // true);
                emResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} HHCE", emParam.getUid(),
                        emParam.getFid(), emServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} HttpHostConnectException",
                        emServer.getId());
            }
            emServer.getEmServerStatistics().incrementAndGetHhceNum();
            schedulerConfiguration.calcMarks(emServer, false, true);
            emResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CTE", emParam.getUid(),
                        emParam.getFid(), emServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} ConnectTimeoutException",
                        emServer.getId());
            }
            emServer.getEmServerStatistics().incrementAndGetCteNum();
            schedulerConfiguration.calcMarks(emServer, false, true);
            emResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} NHRE", emParam.getUid(),
                        emParam.getFid(), emServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} NoHttpResponseException",
                        emServer.getId());
            }
            emServer.getEmServerStatistics().incrementAndGetNhreNum();
            // TODO schedulerConfiguration.calcMarks(searchServer, false, true);
            emResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} STE", emParam.getUid(),
                        emParam.getFid(), emServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} SocketTimeoutException",
                        emServer.getId());
            }
            emServer.getEmServerStatistics().incrementAndGetSteNum();
            // TODO schedulerConfiguration.calcMarks(searchServer, false, true);
            emResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", emParam.getUid(),
                        emParam.getFid(), emServer.getId());
            }
            emServer.getEmServerStatistics().incrementAndGetCpeNum();
            emResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} UEE", emParam.getUid(),
                        emParam.getFid(), emServer.getId());
            }
            emServer.getEmServerStatistics().incrementAndGetUeeNum();
            emResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", emParam.getUid(),
                        emParam.getFid(), emServer.getId());
            }
            emServer.getEmServerStatistics().incrementAndGetIoeNum();
            emResult.setStatusCode(StatusCode.OTHER);
        } catch (IndexOutOfBoundsException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOOBE", emParam.getUid(),
                        emParam.getFid(), emServer.getId());
            }
            emServer.getEmServerStatistics().incrementAndGetIoobeNum();
            emResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", emParam.getUid(), emParam.getFid(),
                        emServer.getId());
            }
            emServer.getEmServerStatistics().incrementAndGetOeNum();
            emResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} IOE", emParam.getUid(),
                                emParam.getFid(), emServer.getId());
                    }
                    emServer.getEmServerStatistics().incrementAndGetIoeNum();
                    emResult.setStatusCode(StatusCode.OTHER);
                }
            }
            emStopTime = System.currentTimeMillis();
            schedulerControllerStatistics
                    .decrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} DECHCR: {}", emParam.getUid(), emParam
                        .getFid(), schedulerControllerStatistics
                        .getCurrentHttpClientRequestNum());
            }
        }
        long excuteTime = emStopTime - emStartTime;
        emResult.setExcuteTime(excuteTime);
        emServer.getEmServerStatistics().decrementAndGetCurrentRequestNum();
        return emResult;
    }
}

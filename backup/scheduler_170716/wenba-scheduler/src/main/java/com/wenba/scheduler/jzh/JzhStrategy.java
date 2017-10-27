package com.wenba.scheduler.jzh;

import java.io.ByteArrayOutputStream;
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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
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
public class JzhStrategy implements ISchedulerStrategy<JzhParam, JzhResult> {

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public JzhResult excute(JzhParam ieParam) {
        JzhResult ieResult = new JzhResult();
        IeServer ieServer = ieParam.getIeServer();
        ieResult.setSchedulerResult(null);

        // ie server is null
        if (ieServer == null) {
            ieResult.setExcuteTime(0);
            ieResult.setStatusCode(StatusCode.NOSERVER);
            return ieResult;
        }

        ieServer.getIeServerStatistics().incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = ieParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = ieParam
                .getSchedulerControllerStatistics();
        long ieStartTime = System.currentTimeMillis();
        long ieStopTime;
        HttpPost post = new HttpPost(
                SchedulerConstants.HTTP_URL
                        + ieServer.getIp()
                        + ((ieServer.getPort() != null && !"".equals(ieServer
                                .getPort())) ? (SchedulerConstants.COLON + ieServer
                                .getPort()) : "") + ieServer.getUrl());
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getConnectTimeout())
                .setSocketTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getIeTimeout()).build();
        post.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder
                    .create();
            multipartEntityBuilder.addPart("file", new ByteArrayBody(ieParam
                    .getImg().getBytes(), ContentType.MULTIPART_FORM_DATA,
                    ieParam.getImg().getOriginalFilename()));
            HttpEntity entity = multipartEntityBuilder.build();
            post.setEntity(entity);
            schedulerControllerStatistics
                    .incrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} IECHCR: {}", ieParam.getUid(), ieParam
                        .getFid(), schedulerControllerStatistics
                        .getCurrentHttpClientRequestNum());
            }
            response = schedulerConfiguration.getHttpClient().execute(post,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                schedulerConfiguration.calcMarks(ieServer, true, true);
                HttpEntity ieResponse = response.getEntity();
                if (ieResponse != null) {
                    long length = ieResponse.getContentLength();
                    if (length > 0) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream(
                                (int) ieResponse.getContentLength());
                        ieResponse.writeTo(bos);
                        byte[] entityContentAsBytes = bos.toByteArray();
                        ieResult.setEntityContentAsBytes(entityContentAsBytes);
                        ieResult.setStatusCode(StatusCode.OK);
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE", ieParam.getUid(),
                                    ieParam.getFid(), ieServer.getId());
                        }
                        ieServer.getIeServerStatistics()
                                .incrementAndGetHeseNum();
                        ieResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE", ieParam.getUid(),
                                ieParam.getFid(), ieServer.getId());
                    }
                    ieServer.getIeServerStatistics().incrementAndGetHeneNum();
                    ieResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HSNE {}", ieParam.getUid(), ieParam
                            .getFid(), ieServer.getId(), response
                            .getStatusLine().getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                            ieServer.getId());
                }
                ieServer.getIeServerStatistics().incrementAndGetHsneNum();
                // TODO schedulerConfiguration.calcMarks(searchServer, false,
                // true);
                ieResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} HHCE", ieParam.getUid(),
                        ieParam.getFid(), ieServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} HttpHostConnectException",
                        ieServer.getId());
            }
            ieServer.getIeServerStatistics().incrementAndGetHhceNum();
            schedulerConfiguration.calcMarks(ieServer, false, true);
            ieResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CTE", ieParam.getUid(),
                        ieParam.getFid(), ieServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} ConnectTimeoutException",
                        ieServer.getId());
            }
            ieServer.getIeServerStatistics().incrementAndGetCteNum();
            schedulerConfiguration.calcMarks(ieServer, false, true);
            ieResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} NHRE", ieParam.getUid(),
                        ieParam.getFid(), ieServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} NoHttpResponseException",
                        ieServer.getId());
            }
            ieServer.getIeServerStatistics().incrementAndGetNhreNum();
            // TODO schedulerConfiguration.calcMarks(searchServer, false, true);
            ieResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} STE", ieParam.getUid(),
                        ieParam.getFid(), ieServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} SocketTimeoutException",
                        ieServer.getId());
            }
            ieServer.getIeServerStatistics().incrementAndGetSteNum();
            // TODO schedulerConfiguration.calcMarks(searchServer, false, true);
            ieResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", ieParam.getUid(),
                        ieParam.getFid(), ieServer.getId());
            }
            ieServer.getIeServerStatistics().incrementAndGetCpeNum();
            ieResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} UEE", ieParam.getUid(),
                        ieParam.getFid(), ieServer.getId());
            }
            ieServer.getIeServerStatistics().incrementAndGetUeeNum();
            ieResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", ieParam.getUid(),
                        ieParam.getFid(), ieServer.getId());
            }
            ieServer.getIeServerStatistics().incrementAndGetIoeNum();
            ieResult.setStatusCode(StatusCode.OTHER);
        } catch (IndexOutOfBoundsException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOOBE", ieParam.getUid(),
                        ieParam.getFid(), ieServer.getId());
            }
            ieServer.getIeServerStatistics().incrementAndGetIoobeNum();
            ieResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", ieParam.getUid(), ieParam.getFid(),
                        ieServer.getId());
            }
            ieServer.getIeServerStatistics().incrementAndGetOeNum();
            ieResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} IOE", ieParam.getUid(),
                                ieParam.getFid(), ieServer.getId());
                    }
                    ieServer.getIeServerStatistics().incrementAndGetIoeNum();
                    ieResult.setStatusCode(StatusCode.OTHER);
                }
            }
            ieStopTime = System.currentTimeMillis();
            schedulerControllerStatistics
                    .decrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} DECHCR: {}", ieParam.getUid(), ieParam
                        .getFid(), schedulerControllerStatistics
                        .getCurrentHttpClientRequestNum());
            }
        }
        long excuteTime = ieStopTime - ieStartTime;
        ieResult.setExcuteTime(excuteTime);
        ieServer.getIeServerStatistics().decrementAndGetCurrentRequestNum();
        return ieResult;
    }
}

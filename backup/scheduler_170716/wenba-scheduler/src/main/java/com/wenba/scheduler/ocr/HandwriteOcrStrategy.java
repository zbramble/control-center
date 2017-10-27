package com.wenba.scheduler.ocr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

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
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wenba.scheduler.AbstractResult.StatusCode;
import com.wenba.scheduler.AbstractServer.ServerType;
import com.wenba.scheduler.ISchedulerStrategy;
import com.wenba.scheduler.SchedulerConstants;
import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.ocr.OcrResult.OcrFailedType;

/**
 * @author zhangbo
 *
 */
public class HandwriteOcrStrategy implements
        ISchedulerStrategy<OcrParam, OcrResult> {

    // constants
    private static final String STATUS = "status";
    private static final String DATA = "data";
    private static final String VERSION = "version";

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public OcrResult excute(OcrParam handwriteOcrParam) {
        OcrResult handwriteOcrResult = new OcrResult();
        OcrServer handwriteOcrServer = handwriteOcrParam.getOcrServer();
        handwriteOcrResult.setSchedulerResult(null);
        handwriteOcrResult.setNlpFailedType(null);
        handwriteOcrResult.setOcrFailedType(null);
        handwriteOcrResult.setVersion(String.valueOf(-1));
        handwriteOcrResult.setNlpVersion(String.valueOf(-1));

        // handwrite ocr server is null
        if (handwriteOcrServer == null) {
            handwriteOcrResult.setExcuteTime(0);
            handwriteOcrResult.setOcrType(null);
            handwriteOcrResult.setStatusCode(StatusCode.NOSERVER);
            return handwriteOcrResult;
        }
        handwriteOcrResult.setOcrType(handwriteOcrServer.getServerType());

        handwriteOcrServer.getOcrServerStatistics()
                .incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = handwriteOcrParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = handwriteOcrParam
                .getSchedulerControllerStatistics();
        long handwriteOcrStartTime = System.currentTimeMillis();
        long handwriteOcrStopTime;
        HttpPost post = null;
        CloseableHttpResponse response = null;
        try {
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder
                    .create();
            multipartEntityBuilder.addPart("img", new ByteArrayBody(
                    handwriteOcrParam.getImg().getBytes(),
                    ContentType.MULTIPART_FORM_DATA, handwriteOcrParam.getImg()
                            .getOriginalFilename()));
            if (handwriteOcrParam.isUseLayoutinfoOrNot()) {
                multipartEntityBuilder.addTextBody("json",
                        handwriteOcrParam.getLayoutinfo());
                // debugLogger.info("{} {} layoutinfo2: {}",
                // handwriteOcrParam.getUid(), handwriteOcrParam.getFid(),
                // handwriteOcrParam.getLayoutinfo()); // TODO
            }
            HttpEntity entity = multipartEntityBuilder.build();
            post = new HttpPost(
                    SchedulerConstants.HTTP_URL
                            + handwriteOcrServer.getIp()
                            + ((handwriteOcrServer.getPort() != null && !""
                                    .equals(handwriteOcrServer.getPort())) ? (SchedulerConstants.COLON + handwriteOcrServer
                                    .getPort()) : "")
                            + handwriteOcrServer.getUrl());
            RequestConfig requestConfig = RequestConfig
                    .custom()
                    .setConnectTimeout(
                            schedulerConfiguration.getTimeoutConfiguration()
                                    .getConnectTimeout())
                    .setSocketTimeout(
                            (int) (schedulerConfiguration
                                    .getTimeoutConfiguration()
                                    .getConnectTimeout()
                                    * 2
                                    + schedulerConfiguration
                                            .getTimeoutConfiguration()
                                            .getOcrTimeout() - handwriteOcrParam
                                    .getOcrAndNlpExcuteTime())).build();
            post.setConfig(requestConfig);
            post.setEntity(entity);
            schedulerControllerStatistics
                    .incrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} IHOCHCR: {}", handwriteOcrParam
                        .getUid(), handwriteOcrParam.getFid(),
                        schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
            response = schedulerConfiguration.getHttpClient().execute(post,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                schedulerConfiguration.calcMarks(handwriteOcrServer, true,
                        false);
                HttpEntity handwriteOcrResponse = response.getEntity();
                if (handwriteOcrResponse != null) {
                    String handwriteOcrResponseResult = EntityUtils
                            .toString(handwriteOcrResponse);
                    // debugLogger.info("{} {} handwriteOcrResponseResult: {}",
                    // handwriteOcrParam.getUid(),
                    // handwriteOcrParam.getFid(),
                    // handwriteOcrResponseResult); // TODO
                    if (handwriteOcrResponseResult != null
                            && !"".equals(handwriteOcrResponseResult)) {
                        try {
                            // 把json字符串转换成json对象
                            JSONObject handwriteOcrResultJson = JSONObject
                                    .fromObject(handwriteOcrResponseResult);
                            try {
                                String version = handwriteOcrResultJson
                                        .getString(VERSION);
                                if (version != null && !"".equals(version)) {
                                    handwriteOcrResult.setVersion(version);
                                }
                                handwriteOcrResult.setRotate(handwriteOcrParam
                                        .getRotate());
                            } catch (JSONException e) {
                                if (schedulerConfiguration
                                        .isSaveExceptionLogSwitch()) {
                                    logger.error("{} {} {} JE",
                                            handwriteOcrParam.getUid(),
                                            handwriteOcrParam.getFid(),
                                            handwriteOcrServer.getId());
                                }
                                debugLogger.error("JE1", e); // TODO
                                handwriteOcrServer.getOcrServerStatistics()
                                        .incrementAndGetJeNum();
                                handwriteOcrResult
                                        .setStatusCode(StatusCode.JSON_EXCEPTION);
                            }

                            int status = handwriteOcrResultJson.getInt(STATUS);
                            if (status == OcrFailedType.NORMAL.getValue()) {
                                handwriteOcrResult
                                        .setSchedulerResult(URLDecoder.decode(
                                                handwriteOcrResultJson
                                                        .getString(DATA),
                                                "UTF-8"));
                                handwriteOcrResult
                                        .setOcrType(ServerType.HANDWRITE_OCR);
                                if (handwriteOcrResult.getSchedulerResult() != null
                                        && !"".equals(handwriteOcrResult
                                                .getSchedulerResult())) {
                                    handwriteOcrResult
                                            .setStatusCode(StatusCode.OK);
                                } else {
                                    handwriteOcrServer.getOcrServerStatistics()
                                            .incrementAndGetOfteNum();
                                    handwriteOcrResult
                                            .setOcrFailedType(OcrFailedType.OCR);
                                    handwriteOcrResult
                                            .setStatusCode(StatusCode.OCR_FAILED_OCR);
                                }
                            } else {
                                handwriteOcrServer.getOcrServerStatistics()
                                        .incrementAndGetOfteNum();
                                switch (status) {
                                case 1:
                                    handwriteOcrResult
                                            .setOcrFailedType(OcrFailedType.IMG);
                                    handwriteOcrResult
                                            .setStatusCode(StatusCode.OCR_FAILED_IMG);
                                    break;
                                case 2:
                                    handwriteOcrResult
                                            .setOcrFailedType(OcrFailedType.OCR);
                                    handwriteOcrResult
                                            .setStatusCode(StatusCode.OCR_FAILED_OCR);
                                    break;
                                default:
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            if (schedulerConfiguration
                                    .isSaveExceptionLogSwitch()) {
                                logger.error("{} {} {} JE",
                                        handwriteOcrParam.getUid(),
                                        handwriteOcrParam.getFid(),
                                        handwriteOcrServer.getId());
                            }
                            debugLogger.error("JE2", e); // TODO
                            handwriteOcrServer.getOcrServerStatistics()
                                    .incrementAndGetJeNum();
                            handwriteOcrResult
                                    .setStatusCode(StatusCode.JSON_EXCEPTION);
                        }
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE",
                                    handwriteOcrParam.getUid(),
                                    handwriteOcrParam.getFid(),
                                    handwriteOcrServer.getId());
                        }
                        handwriteOcrServer.getOcrServerStatistics()
                                .incrementAndGetHeseNum();
                        handwriteOcrResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE",
                                handwriteOcrParam.getUid(),
                                handwriteOcrParam.getFid(),
                                handwriteOcrServer.getId());
                    }
                    handwriteOcrServer.getOcrServerStatistics()
                            .incrementAndGetHeneNum();
                    handwriteOcrResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HSNE {}",
                            handwriteOcrParam.getUid(), handwriteOcrParam
                                    .getFid(), handwriteOcrServer.getId(),
                            response.getStatusLine().getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                            handwriteOcrServer.getId());
                }
                handwriteOcrServer.getOcrServerStatistics()
                        .incrementAndGetHsneNum();
                // TODO schedulerConfiguration.calcMarks(ocrServer, false,
                // false);
                handwriteOcrResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} HHCE", handwriteOcrParam.getUid(),
                        handwriteOcrParam.getFid(), handwriteOcrServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} HttpHostConnectException",
                        handwriteOcrServer.getId());
            }
            handwriteOcrServer.getOcrServerStatistics()
                    .incrementAndGetHhceNum();
            schedulerConfiguration.calcMarks(handwriteOcrServer, false, false);
            handwriteOcrResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CTE", handwriteOcrParam.getUid(),
                        handwriteOcrParam.getFid(), handwriteOcrServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} ConnectTimeoutException",
                        handwriteOcrServer.getId());
            }
            handwriteOcrServer.getOcrServerStatistics().incrementAndGetCteNum();
            schedulerConfiguration.calcMarks(handwriteOcrServer, false, false);
            handwriteOcrResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} NHRE", handwriteOcrParam.getUid(),
                        handwriteOcrParam.getFid(), handwriteOcrServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} NoHttpResponseException",
                        handwriteOcrServer.getId());
            }
            handwriteOcrServer.getOcrServerStatistics()
                    .incrementAndGetNhreNum();
            // TODO schedulerConfiguration.calcMarks(ocrServer, false, false);
            handwriteOcrResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} STE", handwriteOcrParam.getUid(),
                        handwriteOcrParam.getFid(), handwriteOcrServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} SocketTimeoutException",
                        handwriteOcrServer.getId());
            }
            handwriteOcrServer.getOcrServerStatistics().incrementAndGetSteNum();
            // TODO schedulerConfiguration.calcMarks(ocrServer, false, false);
            handwriteOcrResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", handwriteOcrParam.getUid(),
                        handwriteOcrParam.getFid(), handwriteOcrServer.getId());
            }
            handwriteOcrServer.getOcrServerStatistics().incrementAndGetCpeNum();
            handwriteOcrResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} UEE", handwriteOcrParam.getUid(),
                        handwriteOcrParam.getFid(), handwriteOcrServer.getId());
            }
            handwriteOcrServer.getOcrServerStatistics().incrementAndGetUeeNum();
            handwriteOcrResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", handwriteOcrParam.getUid(),
                        handwriteOcrParam.getFid(), handwriteOcrServer.getId());
            }
            handwriteOcrServer.getOcrServerStatistics().incrementAndGetIoeNum();
            handwriteOcrResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", handwriteOcrParam.getUid(),
                        handwriteOcrParam.getFid(), handwriteOcrServer.getId());
            }
            debugLogger.error("OE", e); // TODO
            handwriteOcrServer.getOcrServerStatistics().incrementAndGetOeNum();
            handwriteOcrResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} IOE",
                                handwriteOcrParam.getUid(),
                                handwriteOcrParam.getFid(),
                                handwriteOcrServer.getId());
                    }
                    handwriteOcrServer.getOcrServerStatistics()
                            .incrementAndGetIoeNum();
                    handwriteOcrResult.setStatusCode(StatusCode.OTHER);
                }
            }
            handwriteOcrStopTime = System.currentTimeMillis();
            schedulerControllerStatistics
                    .decrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} DHOCHCR: {}", handwriteOcrParam
                        .getUid(), handwriteOcrParam.getFid(),
                        schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
        }
        long excuteTime = handwriteOcrStopTime - handwriteOcrStartTime;
        handwriteOcrResult.setExcuteTime(excuteTime);
        // if (schedulerConfiguration.getDebugSwitchConfiguration()
        // .isAvgCnnServerExecTimeDebugSwitch()
        // && ServerType.CNN.equals(handwriteOcrServer.getServerType())) {
        // handwriteOcrServer.getOcrServerStatistics().addAndGetExecTime(
        // excuteTime);
        // handwriteOcrServer.getOcrServerStatistics()
        // .incrementAndGetExecTimeNum();
        // }
        // if (schedulerConfiguration.getDebugSwitchConfiguration()
        // .isCnnServerExecTimeDebugSwitch()
        // && ServerType.CNN.equals(handwriteOcrServer.getServerType())) {
        // debugLogger.info("{} {} {} OET: {}", handwriteOcrParam.getUid(),
        // handwriteOcrParam.getFid(), handwriteOcrServer.getId(), excuteTime);
        // }
        handwriteOcrServer.getOcrServerStatistics()
                .decrementAndGetCurrentRequestNum();
        return handwriteOcrResult;
    }
}

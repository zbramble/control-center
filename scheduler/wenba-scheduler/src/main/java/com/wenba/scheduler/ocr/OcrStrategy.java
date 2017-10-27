package com.wenba.scheduler.ocr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.Map.Entry;
import java.util.Random;

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
import com.wenba.scheduler.config.SystemDataConfiguration.SchedulerStrategy;
import com.wenba.scheduler.nlp.NlpParam;
import com.wenba.scheduler.nlp.NlpResult;
import com.wenba.scheduler.nlp.NlpServer;
import com.wenba.scheduler.ocr.OcrResult.OcrFailedType;

/**
 * @author zhangbo
 *
 */
public class OcrStrategy implements ISchedulerStrategy<OcrParam, OcrResult> {

    // constants

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");
    private static Random r = new Random();

    public OcrResult excute(OcrParam ocrParam) {
        OcrResult ocrResult = new OcrResult();
        OcrServer ocrServer = ocrParam.getOcrServer();
        ocrResult.setSchedulerResult(null);
        ocrResult.setNlpFailedType(null);
        ocrResult.setOcrFailedType(null);
        ocrResult.setVersion(String.valueOf(-1));
        ocrResult.setNlpVersion(String.valueOf(-1));
        ocrResult.setLayoutinfo(null);
        ocrResult.setExcuteNlp(false);

        // ocr server is null
        if (ocrServer == null) {
            ocrResult.setExcuteTime(0);
            ocrResult.setOcrType(null);
            ocrResult.setStatusCode(StatusCode.NOSERVER);
            return ocrResult;
        }
        ocrResult.setOcrType(ocrServer.getServerType());

        ocrServer.getOcrServerStatistics().incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = ocrParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = ocrParam
                .getSchedulerControllerStatistics();
        long ocrStartTime = System.currentTimeMillis();
        long ocrStopTime;
        HttpPost post = null;
        CloseableHttpResponse response = null;
        try {
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder
                    .create();
            multipartEntityBuilder.addPart("file", new ByteArrayBody(ocrParam
                    .getImg().getBytes(), ContentType.MULTIPART_FORM_DATA,
                    ocrParam.getImg().getOriginalFilename()));
            HttpEntity entity = multipartEntityBuilder.build();
            post = new HttpPost(
                    SchedulerConstants.HTTP_URL
                            + ocrServer.getIp()
                            + ((ocrServer.getPort() != null && !""
                                    .equals(ocrServer.getPort())) ? (SchedulerConstants.COLON + ocrServer
                                    .getPort()) : "") + ocrServer.getUrl());
            RequestConfig requestConfig = RequestConfig
                    .custom()
                    .setConnectTimeout(
                            schedulerConfiguration.getTimeoutConfiguration()
                                    .getConnectTimeout())
                    .setSocketTimeout(
                            schedulerConfiguration.getTimeoutConfiguration()
                                    .getOcrTimeout()).build();
            post.setConfig(requestConfig);
            post.setEntity(entity);
            schedulerControllerStatistics
                    .incrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} IOCHCR: {}", ocrParam.getUid(),
                        ocrParam.getFid(), schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
            response = schedulerConfiguration.getHttpClient().execute(post,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                ocrStopTime = System.currentTimeMillis();
                schedulerConfiguration.calcMarks(ocrServer, true, false);
                HttpEntity ocrResponse = response.getEntity();
                if (ocrResponse != null) {
                    String ocrResponseResult = EntityUtils
                            .toString(ocrResponse);
                    if (ocrResponseResult != null
                            && !"".equals(ocrResponseResult)) {
                        // 把json字符串转换成ocrJsonResult对象
                        OcrJsonResult ocrJsonResult = schedulerConfiguration
                                .getMapper().readValue(ocrResponseResult,
                                        OcrJsonResult.class);
                        ocrStopTime = System.currentTimeMillis();
                        int status = ocrJsonResult.getStatus();
                        if (status == OcrFailedType.NORMAL.getValue()) {
                            ocrResult.setVersion(ocrJsonResult.getVersion());
                            ocrResult.setRotate(ocrJsonResult.getRotate());
                            ocrResult.setLayoutinfo(ocrJsonResult
                                    .getLayoutinfo() != null ? ocrJsonResult
                                    .getLayoutinfo().toString() : "");
                            int ocrExcuteTime = (int) (ocrStopTime - ocrStartTime);
                            if (schedulerConfiguration.isNlpOnSwitch()
                                    && ocrExcuteTime < (schedulerConfiguration
                                            .getTimeoutConfiguration()
                                            .getConnectTimeout() + schedulerConfiguration
                                            .getTimeoutConfiguration()
                                            .getOcrTimeout())) {
                                NlpParam nlpParam = new NlpParam();
                                NlpServer nlpServer = chooseNlpServer(ocrParam
                                        .getSchedulerConfiguration());
                                OcrResult ocrNlpResult = new OcrResult();
                                ocrNlpResult.setSchedulerResult(URLDecoder
                                        .decode(ocrJsonResult.getNlpdata(),
                                                "UTF-8"));
                                nlpParam.setOcrNlpResult(ocrNlpResult);
                                nlpParam.setFid(ocrParam.getFid());
                                nlpParam.setUid(ocrParam.getUid());
                                nlpParam.setNlpServer(nlpServer);
                                nlpParam.setSchedulerConfiguration(schedulerConfiguration);
                                nlpParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
                                nlpParam.setOcrExcuteTime(ocrExcuteTime);
                                NlpResult nlpResult = ocrParam.getNlpStrategy()
                                        .excute(nlpParam);
                                if (nlpResult.getSchedulerResult() != null) {
                                    ocrResult.setSchedulerResult(nlpResult
                                            .getSchedulerResult());
                                } else {
                                    ocrResult.setSchedulerResult(URLDecoder
                                            .decode(ocrJsonResult.getData(),
                                                    "UTF-8"));
                                }
                                ocrResult.setExcuteNlp(true);
                                ocrResult.setNlpExcuteTime(nlpResult
                                        .getExcuteTime());
                                ocrResult.setNlpFailedType(nlpResult
                                        .getNlpFailedType());
                                ocrResult.setNlpVersion(nlpResult.getVersion());
                            } else {
                                ocrResult.setSchedulerResult(URLDecoder.decode(
                                        ocrJsonResult.getData(), "UTF-8"));
                            }
                            if (ocrResult.getSchedulerResult() != null
                                    && !"".equals(ocrResult
                                            .getSchedulerResult())) {
                                ocrResult.setStatusCode(StatusCode.OK);
                            } else {
                                ocrServer.getOcrServerStatistics()
                                        .incrementAndGetOfteNum();
                                ocrResult.setOcrFailedType(OcrFailedType.OCR);
                                ocrResult
                                        .setStatusCode(StatusCode.OCR_FAILED_OCR);
                            }
                        } else {
                            ocrServer.getOcrServerStatistics()
                                    .incrementAndGetOfteNum();
                            switch (status) {
                            case 1:
                                ocrResult.setOcrFailedType(OcrFailedType.IMG);
                                ocrResult
                                        .setStatusCode(StatusCode.OCR_FAILED_IMG);
                                break;
                            case 2:
                                ocrResult.setOcrFailedType(OcrFailedType.OCR);
                                ocrResult
                                        .setStatusCode(StatusCode.OCR_FAILED_OCR);
                                break;
                            default:
                                break;
                            }
                        }
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE", ocrParam.getUid(),
                                    ocrParam.getFid(), ocrServer.getId());
                        }
                        ocrServer.getOcrServerStatistics()
                                .incrementAndGetHeseNum();
                        ocrResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE", ocrParam.getUid(),
                                ocrParam.getFid(), ocrServer.getId());
                    }
                    ocrServer.getOcrServerStatistics().incrementAndGetHeneNum();
                    ocrResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HSNE {}", ocrParam.getUid(),
                            ocrParam.getFid(), ocrServer.getId(), response
                                    .getStatusLine().getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                            ocrServer.getId());
                }
                ocrServer.getOcrServerStatistics().incrementAndGetHsneNum();
                // TODO schedulerConfiguration.calcMarks(ocrServer, false,
                // false);
                ocrResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} HHCE", ocrParam.getUid(),
                        ocrParam.getFid(), ocrServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} HttpHostConnectException",
                        ocrServer.getId());
            }
            ocrServer.getOcrServerStatistics().incrementAndGetHhceNum();
            schedulerConfiguration.calcMarks(ocrServer, false, false);
            ocrResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CTE", ocrParam.getUid(),
                        ocrParam.getFid(), ocrServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} ConnectTimeoutException",
                        ocrServer.getId());
            }
            ocrServer.getOcrServerStatistics().incrementAndGetCteNum();
            schedulerConfiguration.calcMarks(ocrServer, false, false);
            ocrResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} NHRE", ocrParam.getUid(),
                        ocrParam.getFid(), ocrServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} NoHttpResponseException",
                        ocrServer.getId());
            }
            ocrServer.getOcrServerStatistics().incrementAndGetNhreNum();
            // TODO schedulerConfiguration.calcMarks(ocrServer, false, false);
            ocrResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} STE", ocrParam.getUid(),
                        ocrParam.getFid(), ocrServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} SocketTimeoutException",
                        ocrServer.getId());
            }
            ocrServer.getOcrServerStatistics().incrementAndGetSteNum();
            // TODO schedulerConfiguration.calcMarks(ocrServer, false, false);
            ocrResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", ocrParam.getUid(),
                        ocrParam.getFid(), ocrServer.getId());
            }
            ocrServer.getOcrServerStatistics().incrementAndGetCpeNum();
            ocrResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} UEE", ocrParam.getUid(),
                        ocrParam.getFid(), ocrServer.getId());
            }
            ocrServer.getOcrServerStatistics().incrementAndGetUeeNum();
            ocrResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", ocrParam.getUid(),
                        ocrParam.getFid(), ocrServer.getId());
            }
            ocrServer.getOcrServerStatistics().incrementAndGetIoeNum();
            ocrResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", ocrParam.getUid(),
                        ocrParam.getFid(), ocrServer.getId());
            }
            debugLogger.error("OE", e); // TODO
            ocrServer.getOcrServerStatistics().incrementAndGetOeNum();
            ocrResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} IOE", ocrParam.getUid(),
                                ocrParam.getFid(), ocrServer.getId());
                    }
                    ocrServer.getOcrServerStatistics().incrementAndGetIoeNum();
                    ocrResult.setStatusCode(StatusCode.OTHER);
                }
            }
            ocrStopTime = System.currentTimeMillis();
            schedulerControllerStatistics
                    .decrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} DOCHCR: {}", ocrParam.getUid(),
                        ocrParam.getFid(), schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
        }
        long excuteTime = ocrStopTime - ocrStartTime;
        ocrResult.setExcuteTime(excuteTime - ocrResult.getNlpExcuteTime());
        ocrResult.setOcrAndNlpExcuteTime(excuteTime);
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgCnnServerExecTimeDebugSwitch()
                && ServerType.CNN.equals(ocrServer.getServerType())) {
            ocrServer.getOcrServerStatistics().addAndGetExecTime(excuteTime);
            ocrServer.getOcrServerStatistics().incrementAndGetExecTimeNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgJavaServerExecTimeDebugSwitch()
                && ServerType.JAVA.equals(ocrServer.getServerType())) {
            ocrServer.getOcrServerStatistics().addAndGetExecTime(excuteTime);
            ocrServer.getOcrServerStatistics().incrementAndGetExecTimeNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isCnnServerExecTimeDebugSwitch()
                && ServerType.CNN.equals(ocrServer.getServerType())) {
            debugLogger.info("{} {} {} OET: {}", ocrParam.getUid(),
                    ocrParam.getFid(), ocrServer.getId(), excuteTime);
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isJavaServerExecTimeDebugSwitch()
                && ServerType.JAVA.equals(ocrServer.getServerType())) {
            debugLogger.info("{} {} {} OET: {}", ocrParam.getUid(),
                    ocrParam.getFid(), ocrServer.getId(), excuteTime);
        }
        ocrServer.getOcrServerStatistics().decrementAndGetCurrentRequestNum();
        return ocrResult;
    }

    /**
     * @return nlp server
     */
    private NlpServer chooseNlpServer(
            SchedulerConfiguration schedulerConfiguration) {
        NlpServer nlpServer = null;
        if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getNlpTotalWeight() == 0) {
                return nlpServer;
            }
            int weightForChooingNlp = r.nextInt(schedulerConfiguration
                    .getNlpTotalWeight());
            for (Entry<Integer, NlpServer> entry : schedulerConfiguration
                    .getNlpWeightTable().entrySet()) {
                if (weightForChooingNlp < entry.getKey()) {
                    nlpServer = entry.getValue();
                    break;
                }
            }
        } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getCurrentPollingNlpServerIndex() == -1) {
                return nlpServer;
            }
            int index = (int) (schedulerConfiguration
                    .getAndIncrementCurrentPollingNlpServerIndex() % schedulerConfiguration
                    .getNlpServersPollingListSize());
            nlpServer = schedulerConfiguration.getNlpServersPollingList().get(
                    index);
        }
        return nlpServer;
    }
}

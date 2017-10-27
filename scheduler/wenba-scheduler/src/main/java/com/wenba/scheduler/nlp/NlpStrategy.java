package com.wenba.scheduler.nlp;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wenba.scheduler.AbstractResult.StatusCode;
import com.wenba.scheduler.ISchedulerStrategy;
import com.wenba.scheduler.SchedulerConstants;
import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.nlp.NlpResult.NlpFailedType;

/**
 * @author zhangbo
 *
 */
public class NlpStrategy implements ISchedulerStrategy<NlpParam, NlpResult> {

    // constants

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public NlpResult excute(NlpParam nlpParam) {
        NlpResult nlpResult = new NlpResult();
        NlpServer nlpServer = nlpParam.getNlpServer();
        nlpResult.setSchedulerResult(null);
        nlpResult.setNlpFailedType(null);
        nlpResult.setVersion(String.valueOf(-1));

        // nlp server is null
        if (nlpServer == null) {
            nlpResult.setExcuteTime(0);
            nlpResult.setStatusCode(StatusCode.NOSERVER);
            return nlpResult;
        }

        nlpServer.getNlpServerStatistics().incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = nlpParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = nlpParam
                .getSchedulerControllerStatistics();
        long nlpStartTime = System.currentTimeMillis();
        long nlpStopTime;
        HttpGet get = new HttpGet();
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getConnectTimeout())
                .setSocketTimeout(
                        (int) (schedulerConfiguration.getTimeoutConfiguration()
                                .getConnectTimeout()
                                + schedulerConfiguration
                                        .getTimeoutConfiguration()
                                        .getOcrTimeout() - nlpParam
                                .getOcrExcuteTime())).build();
        get.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            get.setURI(new URI(
                    SchedulerConstants.HTTP_URL
                            + nlpServer.getIp()
                            + ((nlpServer.getPort() != null && !""
                                    .equals(nlpServer.getPort())) ? (SchedulerConstants.COLON + nlpServer
                                    .getPort()) : "")
                            + nlpServer.getUrl()
                            + "?ocr="
                            + Base64.encodeBase64String(nlpParam
                                    .getOcrNlpResult().toString().getBytes())));
            schedulerControllerStatistics
                    .incrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} INCHCR: {}", nlpParam.getUid(),
                        nlpParam.getFid(), schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
            response = schedulerConfiguration.getHttpClient().execute(get,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                schedulerConfiguration.calcMarks(nlpServer, true, false);
                HttpEntity nlpResponse = response.getEntity();
                if (nlpResponse != null) {
                    String nlpResponseResult = EntityUtils
                            .toString(nlpResponse);
                    if (nlpResponseResult != null
                            && !"".equals(nlpResponseResult)) {
                        // 把json字符串转换成nlpJsonResult对象
                        NlpJsonResult nlpJsonResult = schedulerConfiguration
                                .getMapper().readValue(nlpResponseResult,
                                        NlpJsonResult.class);
                        nlpResult.setVersion(nlpJsonResult.getVersion());
                        int status = nlpJsonResult.getStatus();
                        if (status != NlpFailedType.NORMAL.ordinal()
                                || status == NlpFailedType.NORMAL.ordinal()
                                && "".equals(URLDecoder.decode(
                                        nlpJsonResult.getNlpdata(), "UTF-8"))) {
                            nlpResult.setStatusCode(StatusCode.NORESULT);
                        } else {
                            nlpResult.setSchedulerResult(URLDecoder.decode(
                                    nlpJsonResult.getNlpdata(), "UTF-8"));
                            nlpResult.setStatusCode(StatusCode.OK);
                        }
                        nlpResult
                                .setNlpFailedType(NlpFailedType.values()[status]);
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} HESE", nlpParam.getUid(),
                                    nlpParam.getFid(), nlpServer.getId());
                        }
                        nlpServer.getNlpServerStatistics()
                                .incrementAndGetHeseNum();
                        nlpResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HENE", nlpParam.getUid(),
                                nlpParam.getFid(), nlpServer.getId());
                    }
                    nlpServer.getNlpServerStatistics().incrementAndGetHeneNum();
                    nlpResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HSNE {}", nlpParam.getUid(),
                            nlpParam.getFid(), nlpServer.getId(), response
                                    .getStatusLine().getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                            nlpServer.getId());
                }
                nlpServer.getNlpServerStatistics().incrementAndGetHsneNum();
                // TODO schedulerConfiguration.calcMarks(nlpServer, false,
                // false);
                nlpResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} HHCE", nlpParam.getUid(),
                        nlpParam.getFid(), nlpServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} HttpHostConnectException",
                        nlpServer.getId());
            }
            nlpServer.getNlpServerStatistics().incrementAndGetHhceNum();
            schedulerConfiguration.calcMarks(nlpServer, false, false);
            nlpResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CTE", nlpParam.getUid(),
                        nlpParam.getFid(), nlpServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} ConnectTimeoutException",
                        nlpServer.getId());
            }
            nlpServer.getNlpServerStatistics().incrementAndGetCteNum();
            schedulerConfiguration.calcMarks(nlpServer, false, false);
            nlpResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} NHRE", nlpParam.getUid(),
                        nlpParam.getFid(), nlpServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} NoHttpResponseException",
                        nlpServer.getId());
            }
            nlpServer.getNlpServerStatistics().incrementAndGetNhreNum();
            // TODO schedulerConfiguration.calcMarks(nlpServer, false, false);
            nlpResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} STE", nlpParam.getUid(),
                        nlpParam.getFid(), nlpServer.getId());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger.warn("{} SocketTimeoutException",
                        nlpServer.getId());
            }
            nlpServer.getNlpServerStatistics().incrementAndGetSteNum();
            // TODO schedulerConfiguration.calcMarks(nlpServer, false, false);
            nlpResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} CPE", nlpParam.getUid(),
                        nlpParam.getFid(), nlpServer.getId());
            }
            nlpServer.getNlpServerStatistics().incrementAndGetCpeNum();
            nlpResult.setStatusCode(StatusCode.OTHER);
        } catch (URISyntaxException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} USE", nlpParam.getUid(),
                        nlpParam.getFid(), nlpServer.getId());
            }
            nlpServer.getNlpServerStatistics().incrementAndGetUseNum();
            nlpResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} IOE", nlpParam.getUid(),
                        nlpParam.getFid(), nlpServer.getId());
            }
            nlpServer.getNlpServerStatistics().incrementAndGetIoeNum();
            nlpResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} {} OE", nlpParam.getUid(),
                        nlpParam.getFid(), nlpServer.getId());
            }
            nlpServer.getNlpServerStatistics().incrementAndGetOeNum();
            nlpResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} IOE", nlpParam.getUid(),
                                nlpParam.getFid(), nlpServer.getId());
                    }
                    nlpServer.getNlpServerStatistics().incrementAndGetIoeNum();
                    nlpResult.setStatusCode(StatusCode.OTHER);
                }
            }
            nlpStopTime = System.currentTimeMillis();
            schedulerControllerStatistics
                    .decrementAndGetCurrentHttpClientRequestNum();
            if (schedulerConfiguration.getDebugSwitchConfiguration()
                    .isHttpclientDebugSwitch()) {
                debugLogger.info("{} {} DNCHCR: {}", nlpParam.getUid(),
                        nlpParam.getFid(), schedulerControllerStatistics
                                .getCurrentHttpClientRequestNum());
            }
        }
        long excuteTime = nlpStopTime - nlpStartTime;
        nlpResult.setExcuteTime(excuteTime);
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgNlpServerExecTimeDebugSwitch()) {
            nlpServer.getNlpServerStatistics().addAndGetExecTime(excuteTime);
            nlpServer.getNlpServerStatistics().incrementAndGetExecTimeNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isNlpServerExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} NET: {}", nlpParam.getUid(),
                    nlpParam.getFid(), nlpServer.getId(), excuteTime);
        }
        nlpServer.getNlpServerStatistics().decrementAndGetCurrentRequestNum();
        return nlpResult;
    }
}

package com.wenba.scheduler.statistics;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.json.JSONObject;

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
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;

import com.wenba.scheduler.SchedulerConstants;
import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.SchedulerConfiguration;

/**
 * @author zhangbo
 *
 */
public class BIStrategy {

    // constants

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");
    private static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    @Async
    public void excute(ConcurrentLinkedQueue<BIParam> queryBiQueue) {
        BIParam biParam = queryBiQueue.poll();
        while (biParam != null) {
            SchedulerConfiguration schedulerConfiguration = biParam
                    .getSchedulerConfiguration();
            SchedulerControllerStatistics schedulerControllerStatistics = biParam
                    .getSchedulerControllerStatistics();

            if (!schedulerConfiguration.isBiOnSwitch()) {
                return;
            }
            BIServer biServer = schedulerConfiguration.getBiServers().get(0);

            // bi server is null
            if (biServer == null) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("uid:{}, fid:{}, biServer is null!",
                            biParam.getUid(), biParam.getFid());
                }
                return;
            }

            // prepare json data start
            // event_args json
            JSONObject eventArgsJson = new JSONObject();
            eventArgsJson.put("status", biParam.getStatus());
            eventArgsJson.put("version", biParam.getVersion());
            eventArgsJson.put("fid", biParam.getFid());
            eventArgsJson.put("ocrTime", biParam.getOcrTime());
            eventArgsJson.put("handwriteTime", biParam.getHandwriteTime());
            if (biParam.isExcuteNlp()) {
                eventArgsJson.put("nlpTime", biParam.getNlpTime());
            }
            if (biParam.isExcuteFirstSearch()) {
                eventArgsJson.put("firstSearchTime",
                        biParam.getFirstSearchTime());
            }
            if (biParam.isExcuteSecondSearch()) {
                eventArgsJson.put("secondSearchTime",
                        biParam.getSecondSearchTime());
            }
            eventArgsJson.put("imgName", biParam.getImgName());
            if (biParam.getStatus() == 0 && biParam.isSearchSuccess()) {
                eventArgsJson.put("doc_ids", biParam.getDocIds());
                eventArgsJson.put("similarity", biParam.getSimilarity());
            }

            // common_args json
            JSONObject commonArgsJson = new JSONObject();
            commonArgsJson.put("uid", biParam.getUid());

            JSONObject biRootJson = new JSONObject();
            biRootJson.put("event_name", "backend.wulong.ocr");
            biRootJson
                    .put("event_time", sdf.format(System.currentTimeMillis()));
            biRootJson.put("common_args", commonArgsJson);
            biRootJson.put("event_args", eventArgsJson);
            // prepare json data end

            // post data to BI server start
            HttpPost post = new HttpPost(
                    SchedulerConstants.HTTP_URL
                            + biServer.getIp()
                            + ((biServer.getPort() != null && !""
                                    .equals(biServer.getPort())) ? (SchedulerConstants.COLON + biServer
                                    .getPort()) : "") + biServer.getUrl());
            RequestConfig requestConfig = RequestConfig
                    .custom()
                    .setConnectTimeout(
                            schedulerConfiguration.getTimeoutConfiguration()
                                    .getConnectTimeout())
                    .setSocketTimeout(
                            schedulerConfiguration.getTimeoutConfiguration()
                                    .getBiTimeout()).build();
            post.setConfig(requestConfig);
            CloseableHttpResponse response = null;
            try {
                StringEntity entity = new StringEntity(biRootJson.toString(),
                        ContentType.APPLICATION_JSON);
                post.setEntity(entity);
                schedulerControllerStatistics
                        .incrementAndGetCurrentHttpClientRequestNum();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isHttpclientDebugSwitch()) {
                    debugLogger.info("{} {} IBCHCR: {}", biParam.getUid(),
                            biParam.getFid(), schedulerControllerStatistics
                                    .getCurrentHttpClientRequestNum());
                }
                response = schedulerConfiguration.getHttpClient().execute(post,
                        HttpClientContext.create());
                if (HttpStatus.SC_OK == response.getStatusLine()
                        .getStatusCode()) {
                    schedulerConfiguration.calcMarks(biServer, true, true);
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} HSNE {}", biParam.getUid(),
                                biParam.getFid(), biServer.getId(), response
                                        .getStatusLine().getStatusCode());
                    }
                    if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                        serverMonitorLogger.warn("{} HTTP_STATUS_ISN'T_200",
                                biServer.getId());
                    }
                    biServer.getBiServerStatistics().incrementAndGetHsneNum();
                    // TODO schedulerConfiguration.calcMarks(biServer, false,
                    // true);
                }
            } catch (HttpHostConnectException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} HHCE", biParam.getUid(),
                            biParam.getFid(), biServer.getId());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} HttpHostConnectException",
                            biServer.getId());
                }
                biServer.getBiServerStatistics().incrementAndGetHhceNum();
                schedulerConfiguration.calcMarks(biServer, false, true);
            } catch (ConnectTimeoutException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} CTE", biParam.getUid(),
                            biParam.getFid(), biServer.getId());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} ConnectTimeoutException",
                            biServer.getId());
                }
                biServer.getBiServerStatistics().incrementAndGetCteNum();
                schedulerConfiguration.calcMarks(biServer, false, true);
            } catch (NoHttpResponseException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} NHRE", biParam.getUid(),
                            biParam.getFid(), biServer.getId());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} NoHttpResponseException",
                            biServer.getId());
                }
                biServer.getBiServerStatistics().incrementAndGetNhreNum();
                // TODO schedulerConfiguration.calcMarks(biServer, false, true);
            } catch (SocketTimeoutException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} STE", biParam.getUid(),
                            biParam.getFid(), biServer.getId());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger.warn("{} SocketTimeoutException",
                            biServer.getId());
                }
                biServer.getBiServerStatistics().incrementAndGetSteNum();
                // TODO schedulerConfiguration.calcMarks(biServer, false, true);
            } catch (UnsupportedEncodingException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} UEE", biParam.getUid(),
                            biParam.getFid(), biServer.getId());
                }
                biServer.getBiServerStatistics().incrementAndGetUeeNum();
            } catch (ClientProtocolException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} CPE", biParam.getUid(),
                            biParam.getFid(), biServer.getId());
                }
                biServer.getBiServerStatistics().incrementAndGetCpeNum();
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} IOE", biParam.getUid(),
                            biParam.getFid(), biServer.getId());
                }
                biServer.getBiServerStatistics().incrementAndGetIoeNum();
            } catch (Exception e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} OE", biParam.getUid(),
                            biParam.getFid(), biServer.getId());
                }
                biServer.getBiServerStatistics().incrementAndGetOeNum();
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} IOE", biParam.getUid(),
                                    biParam.getFid(), biServer.getId());
                        }
                        biServer.getBiServerStatistics()
                                .incrementAndGetIoeNum();
                    }
                }
                schedulerControllerStatistics
                        .decrementAndGetCurrentHttpClientRequestNum();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isHttpclientDebugSwitch()) {
                    debugLogger.info("{} {} DBCHCR: {}", biParam.getUid(),
                            biParam.getFid(), schedulerControllerStatistics
                                    .getCurrentHttpClientRequestNum());
                }
            }
            // post data to BI server end

            biParam = queryBiQueue.poll();
        }
    }
}

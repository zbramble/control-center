package com.wenba.scheduler.ugc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
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
import com.wenba.scheduler.config.UgcConfigConfiguration;
import com.wenba.scheduler.ocr.OcrResult.OcrFailedType;

/**
 * @author zhangbo
 *
 */
public class UgcHandwriteOcrStrategy implements
        ISchedulerStrategy<UgcParam, UgcResult> {

    // constants
    private static final String STATUS = "status";
    private static final String DATA = "data";
    private static final String VERSION = "version";
    private static final int BUFFER_LEN = 4096;;

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public UgcResult excute(UgcParam handwriteOcrParam) {
        UgcResult handwriteOcrResult = new UgcResult();
        handwriteOcrResult.setSchedulerResult(null);
        handwriteOcrResult.setNlpFailedType(null);
        handwriteOcrResult.setOcrFailedType(null);
        handwriteOcrResult.setVersion(String.valueOf(-1));
        handwriteOcrResult.setNlpVersion(String.valueOf(-1));
        handwriteOcrResult.setOcrType(ServerType.UGC_HANDWRITE_OCR);
        // logger.error("{} {} version-1:{}", handwriteOcrParam.getUid(),
        // handwriteOcrParam.getFid(), handwriteOcrResult.getVersion()); // TODO
        // handwriteOcrServer.getOcrServerStatistics()
        // .incrementAndGetCurrentRequestNum();

        SchedulerConfiguration schedulerConfiguration = handwriteOcrParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = handwriteOcrParam
                .getSchedulerControllerStatistics();
        UgcConfigConfiguration ugcConfigConfiguration = schedulerConfiguration
                .getUgcConfigConfiguration();
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
            // multipartEntityBuilder.addPart("img", new ByteArrayBody(
            // handwriteOcrParam.getImg().getBytes(),
            // ContentType.APPLICATION_OCTET_STREAM, handwriteOcrParam
            // .getImg().getOriginalFilename())); // TODO
            if (handwriteOcrParam.isUseLayoutinfoOrNot()) {
                multipartEntityBuilder.addTextBody("json",
                        handwriteOcrParam.getLayoutinfo());
                // debugLogger.info("{} {} layoutinfo2: {}",
                // handwriteOcrParam.getUid(), handwriteOcrParam.getFid(),
                // handwriteOcrParam.getLayoutinfo()); // TODO
            }
            HttpEntity entity = multipartEntityBuilder.build();
            post = new HttpPost(SchedulerConstants.HTTP_URL
                    + ugcConfigConfiguration.getTaskApiUrl() + "?Region="
                    + ugcConfigConfiguration.getRegion() + "&ImageName="
                    + ugcConfigConfiguration.getImageName()
                    + "&Action=SubmitTask&AccessToken="
                    + ugcConfigConfiguration.getAccessToken());
            // debugLogger.info("ugcHandwriteOcr URI: {}", post.getURI()
            // .toString());
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
            long ugcStartTime = System.currentTimeMillis(); // TODO
            response = schedulerConfiguration.getHttpClient().execute(post,
                    HttpClientContext.create());
            long ugcStopTime = System.currentTimeMillis(); // TODO
            long tarStartTime = System.currentTimeMillis();
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity handwriteOcrResponse = response.getEntity();
                // handwriteOcrResponse.getContent(); // TODO
                if (handwriteOcrResponse != null) {
                    String handwriteOcrTarResponseResult = EntityUtils
                            .toString(handwriteOcrResponse);
                    // writeUgcResult(handwriteOcrResponseResult); // TODO
                    String handwriteOcrResponseResult = tarDecompress(handwriteOcrTarResponseResult
                            .getBytes());
                    long tarOcrStopTime = System.currentTimeMillis(); // TODO
                    if (handwriteOcrResponseResult != null
                            && !"".equals(handwriteOcrResponseResult)) {
                        try {
                            // 把json字符串转换成json对象
                            JSONObject handwriteOcrResultJson = JSONObject
                                    .fromObject(handwriteOcrResponseResult);
                            try {
                                String version = handwriteOcrResultJson
                                        .getString(VERSION);
                                // logger.error("{} {} version-2:{}, result:{}",
                                // handwriteOcrParam.getUid(),
                                // handwriteOcrParam.getFid(), version,
                                // handwriteOcrResponseResult); // TODO
                                if (version != null && !"".equals(version)) {
                                    handwriteOcrResult.setVersion(version);
                                }
                                handwriteOcrResult.setRotate(handwriteOcrParam
                                        .getRotate());

                                long ugcET = ugcStopTime - ugcStartTime; // TODO
                                long ocrTime = handwriteOcrResultJson
                                        .getInt("ocr_time")
                                        + handwriteOcrResultJson
                                                .getInt("recv_time"); // TODO
                                // debugLogger
                                // .info("ugcET:{}, ocrTime:{}, (ugcET-ocrTime) time:{}, tar ET:{}, tarResultSize:{}",
                                // ugcET,
                                // ocrTime,
                                // (ugcET - ocrTime),
                                // (tarOcrStopTime - tarStartTime),
                                // handwriteOcrTarResponseResult
                                // .length()); // TODO
                            } catch (JSONException e) {
                                if (schedulerConfiguration
                                        .isSaveExceptionLogSwitch()) {
                                    logger.error("{} {} UgcHandwriteOcr JE",
                                            handwriteOcrParam.getUid(),
                                            handwriteOcrParam.getFid());
                                }
                                debugLogger.error("JE1", e); // TODO
                                // handwriteOcrServer.getOcrServerStatistics()
                                // .incrementAndGetJeNum();
                                handwriteOcrResult
                                        .setStatusCode(StatusCode.JSON_EXCEPTION);
                            }

                            int status = handwriteOcrResultJson.getInt(STATUS);
                            // logger.error("{} {} version-3:{}, result:{}",
                            // handwriteOcrParam.getUid(),
                            // handwriteOcrParam.getFid(),
                            // handwriteOcrResult.getVersion(),
                            // handwriteOcrResponseResult); // TODO
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
                                    // handwriteOcrServer.getOcrServerStatistics()
                                    // .incrementAndGetOfteNum();
                                    handwriteOcrResult
                                            .setOcrFailedType(OcrFailedType.OCR);
                                    handwriteOcrResult
                                            .setStatusCode(StatusCode.OCR_FAILED_OCR);
                                }
                            } else {
                                // handwriteOcrServer.getOcrServerStatistics()
                                // .incrementAndGetOfteNum();
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
                                logger.error("{} {} UgcHandwriteOcr JE",
                                        handwriteOcrParam.getUid(),
                                        handwriteOcrParam.getFid());
                            }
                            debugLogger.error("JE2", e); // TODO
                            // handwriteOcrServer.getOcrServerStatistics()
                            // .incrementAndGetJeNum();
                            handwriteOcrResult
                                    .setStatusCode(StatusCode.JSON_EXCEPTION);
                        }
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} UgcHandwriteOcr HESE",
                                    handwriteOcrParam.getUid(),
                                    handwriteOcrParam.getFid());
                        }
                        // handwriteOcrServer.getOcrServerStatistics()
                        // .incrementAndGetHeseNum();
                        handwriteOcrResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} UgcHandwriteOcr HENE",
                                handwriteOcrParam.getUid(),
                                handwriteOcrParam.getFid());
                    }
                    // handwriteOcrServer.getOcrServerStatistics()
                    // .incrementAndGetHeneNum();
                    handwriteOcrResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} UgcHandwriteOcr HSNE {}",
                            handwriteOcrParam.getUid(), handwriteOcrParam
                                    .getFid(), response.getStatusLine()
                                    .getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger
                            .warn("UgcHandwriteOcr HTTP_STATUS_ISN'T_200");
                }
                // handwriteOcrServer.getOcrServerStatistics()
                // .incrementAndGetHsneNum();
                handwriteOcrResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} UgcHandwriteOcr HHCE",
                        handwriteOcrParam.getUid(), handwriteOcrParam.getFid());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger
                        .warn("UgcHandwriteOcr HttpHostConnectException");
            }
            // handwriteOcrServer.getOcrServerStatistics()
            // .incrementAndGetHhceNum();
            handwriteOcrResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} UgcHandwriteOcr CTE",
                        handwriteOcrParam.getUid(), handwriteOcrParam.getFid());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger
                        .warn("UgcHandwriteOcr ConnectTimeoutException");
            }
            // handwriteOcrServer.getOcrServerStatistics().incrementAndGetCteNum();
            handwriteOcrResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} UgcHandwriteOcr NHRE",
                        handwriteOcrParam.getUid(), handwriteOcrParam.getFid());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger
                        .warn("UgcHandwriteOcr NoHttpResponseException");
            }
            // handwriteOcrServer.getOcrServerStatistics()
            // .incrementAndGetNhreNum();
            handwriteOcrResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} UgcHandwriteOcr STE",
                        handwriteOcrParam.getUid(), handwriteOcrParam.getFid());
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger
                        .warn("UgcHandwriteOcr SocketTimeoutException");
            }
            // handwriteOcrServer.getOcrServerStatistics().incrementAndGetSteNum();
            handwriteOcrResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} UgcHandwriteOcr CPE",
                        handwriteOcrParam.getUid(), handwriteOcrParam.getFid());
            }
            // handwriteOcrServer.getOcrServerStatistics().incrementAndGetCpeNum();
            handwriteOcrResult.setStatusCode(StatusCode.OTHER);
        } catch (UnsupportedEncodingException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} UgcHandwriteOcr UEE",
                        handwriteOcrParam.getUid(), handwriteOcrParam.getFid());
            }
            // handwriteOcrServer.getOcrServerStatistics().incrementAndGetUeeNum();
            handwriteOcrResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} UgcHandwriteOcr IOE",
                        handwriteOcrParam.getUid(), handwriteOcrParam.getFid());
            }
            // handwriteOcrServer.getOcrServerStatistics().incrementAndGetIoeNum();
            handwriteOcrResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} UgcHandwriteOcr OE",
                        handwriteOcrParam.getUid(), handwriteOcrParam.getFid());
            }
            debugLogger.error("OE", e); // TODO
            // handwriteOcrServer.getOcrServerStatistics().incrementAndGetOeNum();
            handwriteOcrResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} UgcHandwriteOcr IOE",
                                handwriteOcrParam.getUid(),
                                handwriteOcrParam.getFid());
                    }
                    // handwriteOcrServer.getOcrServerStatistics()
                    // .incrementAndGetIoeNum();
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
        // handwriteOcrParam.getFid(), handwriteOcrServer.getId(),
        // excuteTime);
        // }
        // handwriteOcrServer.getOcrServerStatistics()
        // .decrementAndGetCurrentRequestNum();
        return handwriteOcrResult;
    }

    // private void writeUgcResult(String ugcResult) {
    // try {
    // File config = new File("ugcResult_" + System.currentTimeMillis());
    // FileWriter fw = new FileWriter(config);
    // BufferedWriter bw = new BufferedWriter(fw);
    // bw.write(ugcResult);
    // bw.close();
    // } catch (IOException e) {
    // logger.error("write to cnn_servers IOE!");
    // }
    // }

    private String tarDecompress(byte[] src) {
        StringBuilder sb = null;
        TarArchiveInputStream is = null;
        int bufferLen = BUFFER_LEN;
        try {
            is = new TarArchiveInputStream(new ByteArrayInputStream(src));
            TarArchiveEntry entry = null;
            // sb.delete(0, sb.length());
            while ((entry = is.getNextTarEntry()) != null) {
                if (entry.isFile() && entry.getName() != null
                        && "Stdout".equalsIgnoreCase(entry.getName())) {
                    // debugLogger.info(entry.getName()); // TODO
                    sb = new StringBuilder("");
                    byte[] buffer = new byte[bufferLen];
                    int len;
                    long size = entry.getSize();
                    while (size > 0) {
                        if (size < bufferLen) {
                            len = is.read(buffer, 0, (int) size);
                            size -= len;
                        } else {
                            len = is.read(buffer);
                            size -= len;
                        }
                        sb.append(new String(buffer, 0, len));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        // debugLogger.info(sb != null ? sb.toString() : "null"); // TODO
        return sb.toString();
    }
}

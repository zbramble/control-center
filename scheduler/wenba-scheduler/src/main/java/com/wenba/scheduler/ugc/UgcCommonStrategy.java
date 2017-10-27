package com.wenba.scheduler.ugc;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;

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

import com.wenba.scheduler.SchedulerConstants;
import com.wenba.scheduler.AbstractResult.StatusCode;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.config.UgcConfigConfiguration;

/**
 * @author zhangbo
 *
 */
public class UgcCommonStrategy {

    // constants

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    public UgcResult getAccessToken(UgcParam ugcCommonParam) {
        UgcResult ugcCommonResult = new UgcResult();
        SchedulerConfiguration schedulerConfiguration = ugcCommonParam
                .getSchedulerConfiguration();
        UgcConfigConfiguration ugcConfigConfiguration = schedulerConfiguration
                .getUgcConfigConfiguration();
        long getAccessTokenStartTime = System.currentTimeMillis();
        long getAccessTokenStopTime;
        HttpGet get = new HttpGet();
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getConnectTimeout())
                .setSocketTimeout(
                        schedulerConfiguration.getTimeoutConfiguration()
                                .getUgcCommonTimeout()).build();
        get.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            String signature = generateSignature(ugcConfigConfiguration);
            get.setURI(new URI(SchedulerConstants.HTTP_URL
                    + ugcConfigConfiguration.getCommonApiUrl()
                    + "?Region="
                    + ugcConfigConfiguration.getRegion()
                    + "&ExpireIn="
                    + ugcConfigConfiguration.getExpireIn()
                    + "&Action=GetAccessToken&PublicKey="
                    + URLEncoder.encode(ugcConfigConfiguration.getPublicKey(),
                            "UTF-8") + "&Signature=" + signature));
            // debugLogger.info("getAccessToken URI: {}",
            // get.getURI().toString()); // TODO
            response = schedulerConfiguration.getHttpClient().execute(get,
                    HttpClientContext.create());
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity getAccessTokenResponse = response.getEntity();
                if (getAccessTokenResponse != null) {
                    String getAccessTokenResponseResult = EntityUtils
                            .toString(getAccessTokenResponse);
                    if (getAccessTokenResponseResult != null
                            && !getAccessTokenResponseResult.matches("^\\s*$")) {
                        JSONTokener jsonTokener = new JSONTokener(
                                getAccessTokenResponseResult);
                        try {
                            JSONObject studentJSONObject = (JSONObject) jsonTokener
                                    .nextValue();
                            String accessToken = studentJSONObject
                                    .getString("AccessToken");
                            ugcConfigConfiguration.setAccessToken(accessToken);
                            debugLogger.info("accessToken: {}",
                                    ugcConfigConfiguration.getAccessToken()); // TODO
                        } catch (JSONException e) {
                            if (schedulerConfiguration
                                    .isSaveExceptionLogSwitch()) {
                                logger.error("getUgcAccessToken JE");
                            }
                            ugcCommonResult
                                    .setStatusCode(StatusCode.JSON_EXCEPTION);
                        }
                    } else {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("getUgcAccessToken HESE");
                        }
                        ugcCommonResult.setStatusCode(StatusCode.NORESULT);
                    }
                } else {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("getUgcAccessToken HENE");
                    }
                    ugcCommonResult.setStatusCode(StatusCode.NORESULT);
                }
            } else {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("getUgcAccessToken HSNE {}", response
                            .getStatusLine().getStatusCode());
                }
                if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                    serverMonitorLogger
                            .warn("getUgcAccessToken HTTP_STATUS_ISN'T_200");
                }
                ugcCommonResult.setStatusCode(StatusCode.STATUS_NOT_200);
            }
        } catch (HttpHostConnectException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getUgcAccessToken HHCE");
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger
                        .warn("getUgcAccessToken HttpHostConnectException");
            }
            ugcCommonResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (ConnectTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getUgcAccessToken CTE");
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger
                        .warn("getUgcAccessToken ConnectTimeoutException");
            }
            ugcCommonResult.setStatusCode(StatusCode.REQUEST_TIMEOUT);
        } catch (NoHttpResponseException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getUgcAccessToken NHRE");
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger
                        .warn("getUgcAccessToken NoHttpResponseException");
            }
            ugcCommonResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getUgcAccessToken STE");
            }
            if (schedulerConfiguration.isSaveServerMonitorLogSwitch()) {
                serverMonitorLogger
                        .warn("getUgcAccessToken SocketTimeoutException");
            }
            ugcCommonResult.setStatusCode(StatusCode.RESPONSE_TIMEOUT);
        } catch (ClientProtocolException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getUgcAccessToken CPE");
            }
            ugcCommonResult.setStatusCode(StatusCode.OTHER);
        } catch (URISyntaxException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getUgcAccessToken UEE");
            }
            ugcCommonResult.setStatusCode(StatusCode.OTHER);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getUgcAccessToken IOE");
            }
            ugcCommonResult.setStatusCode(StatusCode.OTHER);
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getUgcAccessToken OE");
            }
            debugLogger.error("OE", e); // TODO
            ugcCommonResult.setStatusCode(StatusCode.OTHER);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("getUgcAccessToken IOE");
                    }
                    ugcCommonResult.setStatusCode(StatusCode.OTHER);
                }
            }
            getAccessTokenStopTime = System.currentTimeMillis();
        }
        long excuteTime = getAccessTokenStopTime - getAccessTokenStartTime;
        ugcCommonResult.setExcuteTime(excuteTime);
        return ugcCommonResult;
    }

    private String generateSignature(
            UgcConfigConfiguration ugcConfigConfiguration) {
        String urlParam = "";
        urlParam += "Action";
        urlParam += "GetAccessToken";
        urlParam += "ExpireIn";
        urlParam += ugcConfigConfiguration.getExpireIn();
        urlParam += "PublicKey";
        urlParam += ugcConfigConfiguration.getPublicKey();
        urlParam += "Region";
        urlParam += ugcConfigConfiguration.getRegion();
        urlParam += ugcConfigConfiguration.getPrivateKey();
        debugLogger.info("urlParam: {}", urlParam); // TODO
        return sha1(urlParam);
    }

    private String sha1(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(input.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}

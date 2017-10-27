package com.wenba.scheduler.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.hadoop.conf.Configuration;
import org.apache.http.HttpHost;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wenba.scheduler.AbstractServer.ServerState;
import com.wenba.scheduler.AbstractServer.ServerType;
import com.wenba.scheduler.ISchedulerStrategy;
import com.wenba.scheduler.SchedulerConstants;
import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.SystemDataConfiguration.SchedulerStrategy;
import com.wenba.scheduler.em.EmServer;
import com.wenba.scheduler.em.EmServerStatistics;
import com.wenba.scheduler.email.EmailParam;
import com.wenba.scheduler.email.EmailUtil;
import com.wenba.scheduler.jzh.IeServer;
import com.wenba.scheduler.jzh.IeServerStatistics;
import com.wenba.scheduler.nlp.NlpServer;
import com.wenba.scheduler.nlp.NlpServerStatistics;
import com.wenba.scheduler.ocr.OcrServer;
import com.wenba.scheduler.ocr.OcrServerStatistics;
import com.wenba.scheduler.search.SearchServer;
import com.wenba.scheduler.search.SearchServerStatistics;
import com.wenba.scheduler.statistics.BIServer;
import com.wenba.scheduler.statistics.BIServerStatistics;
import com.xueba100.mining.common.HbClient;

/**
 * config from config file
 * 
 * @author zhangbo
 *
 */
public class ConfigStrategy implements
        ISchedulerStrategy<ConfigParam, ConfigResult> {

    // constants
    private static final int THRESHOLD_100 = 100;
    private static final int LIMIT_10 = 10;
    private static final int DIVISOR_10 = 10;
    private static final int SOCKET_TIMEOUT = 1000;

    // 成员变量
    private static Random r = new Random();;
    private static Logger logger = LogManager.getLogger(ConfigStrategy.class);
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");
    private static Logger configLogger = LogManager.getLogger("config");
    private static String oldMailConfiguration = "";
    private static String oldSystemData = "";
    private static String oldSystemSwitch = "";
    private static String oldDebugSwitch = "";
    private static String oldConfigServers = "";
    private static String oldCnnServers = "";
    private static String oldJavaServers = "";
    private static String oldSearchServers = "";
    private static String oldSearchHomeworkServers = "";
    private static String oldSearchArticleServers = "";
    private static String oldSearchByIdServers = "";
    private static String oldNlpServers = "";
    private static String oldBiServers = "";
    private static String oldTimeoutData = "";
    private static String oldEmServers = "";
    private static String oldIeServers = "";
    private static String oldHbaseConfig = "";
    private static String oldHbaseConfigBk = "";
    private static String oldSdkSearchServers = "";
    private static String oldHandwriteOcrServers = "";
    private static String oldUgcConfig = "";
    private static String oldSearchMatrixServers = "";

    public ConfigResult excute(ConfigParam configParam) {
        ConfigResult configResult = null;
        EmailParam emailParam = new EmailParam();
        emailParam.setSuccess(true);
        emailParam.setMsg("");

        SchedulerConfiguration schedulerConfiguration = configParam
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = configParam
                .getSchedulerControllerStatistics();

        switch (configParam.getConfigFileType()) {
        case ALL:
            // config mail configuration
            configMailConfiguration(schedulerConfiguration, configParam,
                    emailParam);
            // config system data
            configSystemData(schedulerConfiguration, configParam, emailParam);
            // config system switch
            configSystemSwitch(schedulerConfiguration, configParam, emailParam);
            // config debug switch
            configDebugSwitch(schedulerConfiguration,
                    schedulerControllerStatistics, configParam, emailParam);
            // config access servers
            configConfigServers(schedulerConfiguration, configParam, emailParam);
            // config cnn servers
            configCnnServers(schedulerConfiguration, configParam, emailParam);
            // config java servers
            configJavaServers(schedulerConfiguration, configParam, emailParam);
            // config search servers
            configSearchServers(schedulerConfiguration, configParam, emailParam);
            // config search homework servers
            configSearchHomeworkServers(schedulerConfiguration, configParam,
                    emailParam);
            // config search article servers
            configSearchArticleServers(schedulerConfiguration, configParam,
                    emailParam);
            // config search by id servers
            configSearchByIdServers(schedulerConfiguration, configParam,
                    emailParam);
            // config nlp servers
            configNlpServers(schedulerConfiguration, configParam, emailParam);
            // config bi servers
            configBiServers(schedulerConfiguration, configParam, emailParam);
            // config timeout data
            configTimeoutData(schedulerConfiguration, configParam, emailParam);
            // config em servers
            configEmServers(schedulerConfiguration, configParam, emailParam);
            // config hbase config
            configHbaseConfig(schedulerConfiguration, configParam, emailParam);
            // config hbase config backup
            configHbaseConfigBk(schedulerConfiguration, configParam, emailParam);
            // config ie servers
            configIeServers(schedulerConfiguration, configParam, emailParam);
            // config sdk search servers
            configSdkSearchServers(schedulerConfiguration, configParam,
                    emailParam);
            // config handwrite ocr servers
            configHandwriteOcrServers(schedulerConfiguration, configParam,
                    emailParam);
            // config ugc config
            configUgcConfig(schedulerConfiguration, configParam, emailParam);
            // config search matrix servers
            configSearchMatrixServers(schedulerConfiguration, configParam,
                    emailParam);
            break;
        case MAIL_CONFIGURATION:
            // config mail configuration
            configMailConfiguration(schedulerConfiguration, configParam,
                    emailParam);
            break;
        case SYSTEM_DATA:
            // config system data
            configSystemData(schedulerConfiguration, configParam, emailParam);
            break;
        case SYSTEM_SWITCH:
            // config system switch
            configSystemSwitch(schedulerConfiguration, configParam, emailParam);
            break;
        case DEBUG_SWITCH:
            // config debug switch
            configDebugSwitch(schedulerConfiguration,
                    schedulerControllerStatistics, configParam, emailParam);
            break;
        case ACCESS_SERVERS:
            // config access servers
            configConfigServers(schedulerConfiguration, configParam, emailParam);
            break;
        case CNN_SERVERS:
            // config cnn servers
            configCnnServers(schedulerConfiguration, configParam, emailParam);
            break;
        case JAVA_SERVERS:
            // config java servers
            configJavaServers(schedulerConfiguration, configParam, emailParam);
            break;
        case SEARCH_SERVERS:
            // config search servers
            configSearchServers(schedulerConfiguration, configParam, emailParam);
            break;
        case SEARCH_HOMEWORK_SERVERS:
            // config search homework servers
            configSearchHomeworkServers(schedulerConfiguration, configParam,
                    emailParam);
            break;
        case SEARCH_ARTICLE_SERVERS:
            // config search article servers
            configSearchArticleServers(schedulerConfiguration, configParam,
                    emailParam);
            break;
        case SEARCH_BY_ID_SERVERS:
            // config search by id servers
            configSearchByIdServers(schedulerConfiguration, configParam,
                    emailParam);
            break;
        case NLP_SERVERS:
            // config nlp servers
            configNlpServers(schedulerConfiguration, configParam, emailParam);
            break;
        case BI_SERVERS:
            // config bi servers
            configBiServers(schedulerConfiguration, configParam, emailParam);
            break;
        case TIMEOUT_DATA:
            // config timeout data
            configTimeoutData(schedulerConfiguration, configParam, emailParam);
            break;
        case EM_SERVERS:
            // config em servers
            configEmServers(schedulerConfiguration, configParam, emailParam);
            break;
        case HBASE_CONFIG:
            // config hbase config
            configHbaseConfig(schedulerConfiguration, configParam, emailParam);
            break;
        case HBASE_CONFIG_BK:
            // config hbase config backup
            configHbaseConfigBk(schedulerConfiguration, configParam, emailParam);
            break;
        case IE_SERVERS:
            // config ie servers
            configIeServers(schedulerConfiguration, configParam, emailParam);
            break;
        case SDK_SEARCH_SERVERS:
            // config sdk search servers
            configSdkSearchServers(schedulerConfiguration, configParam,
                    emailParam);
            break;
        case HANDWRITE_OCR_SERVERS:
            // config handwrite ocr servers
            configHandwriteOcrServers(schedulerConfiguration, configParam,
                    emailParam);
            break;
        case UGC_CONFIG:
            // config ugc config
            configUgcConfig(schedulerConfiguration, configParam, emailParam);
            break;
        case SEARCH_MATRIX_SERVERS:
            // config search matrix servers
            configSearchMatrixServers(schedulerConfiguration, configParam,
                    emailParam);
            break;
        default:
            break;
        }

        if (schedulerConfiguration.isMailOnSwitch()) {
            String ip = schedulerConfiguration.getIp() != null ? schedulerConfiguration
                    .getIp() : "";
            String name = schedulerConfiguration.getName() != null ? schedulerConfiguration
                    .getName() : "";

            List<String> ipList = schedulerConfiguration.getIpList();
            String ipListStr = "";
            if (ipList != null) {
                for (String ipStr : ipList) {
                    ipListStr += ("      " + ipStr);
                }
            }

            emailParam.setSchedulerId(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerId());
            emailParam.setReceiverAddress(schedulerConfiguration.getEmailUtil()
                    .getConfigFilesMailRecipients());
            if (emailParam.isSuccess()) {
                emailParam.setSub("config success, ip: " + ip + ", name: "
                        + name);
                emailParam.setMsg("config success!\n\n" + "local IP list: "
                        + ipListStr + "\n\n" + emailParam.getMsg());
            } else {
                emailParam.setSub("config exception, ip: " + ip + ", name: "
                        + name);
                emailParam.setMsg("config exception!\n\n" + "local IP list: "
                        + ipListStr + "\n\n" + emailParam.getMsg());
            }

            EmailUtil emailUtil = schedulerConfiguration.getEmailUtil();
            if (emailUtil != null) {
                emailUtil.sendEmail(emailParam);
            }
        }

        if (emailParam.isSuccess()) {
            configLogger.info("config success!\n\n {}", emailParam.getMsg());
        } else {
            configLogger.error("config exception!\n\n {}", emailParam.getMsg());
        }

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(schedulerConfiguration.getSystemDataConfiguration()
                .getMaxTotalConnections());
        cm.setDefaultMaxPerRoute(schedulerConfiguration
                .getSystemDataConfiguration()
                .getDefaultMaxConnectionsPerRoute());
        // TODO
        // SocketConfig socketConfig = SocketConfig.custom()
        // .setSoKeepAlive(true)
        // .build();
        // cm.setDefaultSocketConfig(socketConfig);
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(SOCKET_TIMEOUT).build();
        cm.setDefaultSocketConfig(socketConfig);
        if (schedulerConfiguration.getCnnServers() != null) {
            for (OcrServer cnnServer : schedulerConfiguration.getCnnServers()) {
                HttpHost cnnHost = new HttpHost(cnnServer.getIp(),
                        Integer.parseInt(cnnServer.getPort()));
                cm.setMaxPerRoute(new HttpRoute(cnnHost),
                        schedulerConfiguration.getSystemDataConfiguration()
                                .getCnnConnectionsPerRoute());
            }
        }
        if (schedulerConfiguration.getSearchServers() != null) {
            for (SearchServer searchServer : schedulerConfiguration
                    .getSearchServers()) {
                HttpHost searchHost = new HttpHost(searchServer.getIp(),
                        Integer.parseInt(searchServer.getPort()));
                cm.setMaxPerRoute(new HttpRoute(searchHost),
                        schedulerConfiguration.getSystemDataConfiguration()
                                .getSearchConnectionsPerRoute());
            }
        }

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm).disableAutomaticRetries().build();
        schedulerConfiguration.setHttpClient(httpClient);

        return configResult;
    }

    private void configMailConfiguration(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取mail_configuration文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/mail_configuration");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                emailParam.setSuccess(false);
                emailParam.setMsg("config mail_configuration IOException!\n");
                serverMonitorLogger.error("config mail_configuration IOE!");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    emailParam.setSuccess(false);
                    emailParam
                            .setMsg("config mail_configuration JSONException!\n");
                    serverMonitorLogger.error("config mail_configuration JE!");
                    return;
                }
            }
        }
        logger.info("old mail configuration: {}", oldMailConfiguration);
        logger.info("new mail configuration: {}", configData.toString());

        EmailUtil emailUtil = new EmailUtil();
        // config smtp mail host(sender)
        String mailHostSmtp = configData.getString("mailHostSmtp");
        emailUtil.setMailHostSmtp(mailHostSmtp);

        // config smtp mail port
        int mailPortSmtp = configData.getInt("mailPortSmtp");
        emailUtil.setMailPortSmtp(mailPortSmtp);

        // config imap mail host(receiver)
        String mailHostImap = configData.getString("mailHostImap");
        emailUtil.setMailHostImap(mailHostImap);

        // config imap mail port
        int mailPortImap = configData.getInt("mailPortImap");
        emailUtil.setMailPortImap(mailPortImap);

        // config encryption type
        int encryptionType = configData.getInt("encryptionType");
        emailUtil.setEncryptionType(encryptionType);

        // config auth
        boolean auth = configData.getBoolean("auth");
        emailUtil.setAuth(auth);

        // config mail host account
        String mailHostAccount = configData.getString("mailHostAccount");
        emailUtil.setMailHostAccount(mailHostAccount);

        // config mail Host password
        String mailHostPassword = configData.getString("mailHostPassword");
        emailUtil.setMailHostPassword(mailHostPassword);

        // config config files mail recipients
        String configFilesMailRecipients = configData
                .getString("configFilesMailRecipients");
        emailUtil.setConfigFilesMailRecipients(configFilesMailRecipients);

        // config cnn servers mail recipients
        String cnnServersMailRecipients = configData
                .getString("cnnServersMailRecipients");
        emailUtil.setCnnServersMailRecipients(cnnServersMailRecipients);

        // config search servers mail recipients
        String searchServersMailRecipients = configData
                .getString("searchServersMailRecipients");
        emailUtil.setSearchServersMailRecipients(searchServersMailRecipients);

        // config nlp servers mail recipients
        String nlpServersMailRecipients = configData
                .getString("nlpServersMailRecipients");
        emailUtil.setNlpServersMailRecipients(nlpServersMailRecipients);

        // config config files mail senders
        String configFilesMailSenders = configData
                .getString("configFilesMailSenders");
        emailUtil.setConfigFilesMailSenders(configFilesMailSenders);

        schedulerConfiguration.setEmailUtil(emailUtil);
        oldMailConfiguration = configData.toString();
    }

    private void configSystemData(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取system_data文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/system_data");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config system_data IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config system_data IOException!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config system_data JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config system_data JSONException!\n");
                    return;
                }
            }
        }
        logger.info("old system data: {}", oldSystemData);
        logger.info("new system data: {}", configData.toString());
        SystemDataConfiguration systemDataConfiguration = new SystemDataConfiguration();

        // config scheduler id
        String schedulerId = configData.getString("schedulerId");
        systemDataConfiguration.setSchedulerId((schedulerConfiguration
                .getName() != null && !"".equals(schedulerConfiguration
                .getName())) ? schedulerConfiguration.getName() : schedulerId);

        // config cnn server weight
        int ocrServerCnnWeight = configData.getInt("ocrServerCnnWeight");
        if (ocrServerCnnWeight < 0) {
            logger.error("设置的值为： {}, CNN服务器集群权值必须大于等于0!", ocrServerCnnWeight);
            emailParam.setSuccess(false);
            emailParam.setMsg("ocrServerCnnWeight设置的值为： " + ocrServerCnnWeight
                    + ", CNN服务器集群权值必须大于等于0!\n");
            return;
        } else {
            systemDataConfiguration.setOcrServerCnnWeight(ocrServerCnnWeight);
        }

        // config java server weight
        int ocrServerJavaWeight = configData.getInt("ocrServerJavaWeight");
        if (ocrServerJavaWeight < 0) {
            logger.error("设置的值为： {}, JAVA服务器集群权值必须大于等于0!", ocrServerJavaWeight);
            emailParam.setSuccess(false);
            emailParam.setMsg("ocrServerJavaWeight设置的值为： "
                    + ocrServerJavaWeight + ", JAVA服务器集群权值必须大于等于0!\n");
            return;
        } else {
            systemDataConfiguration.setOcrServerJavaWeight(ocrServerJavaWeight);
        }
        int ocrServerTotalWeight = ocrServerCnnWeight + ocrServerJavaWeight;
        systemDataConfiguration.setOcrServerTotalWeight(ocrServerTotalWeight);

        // config thresUnusedResult
        float thresUnusedResult = Float.valueOf(configData
                .getString("thresUnusedResult"));
        if (thresUnusedResult < 0 || thresUnusedResult > THRESHOLD_100) {
            logger.error("设置的值为： {}, 相似度阈值必须大于等于0并且小于等于100!", thresUnusedResult);
            emailParam.setSuccess(false);
            emailParam.setMsg("thresUnusedResult设置的值为： " + thresUnusedResult
                    + ", 相似度阈值必须大于等于0并且小于等于100!\n");
            return;
        } else {
            systemDataConfiguration.setThresUnusedResult(thresUnusedResult);
        }

        // config search result limit num
        int limit = configData.getInt("limit");
        if (limit < 0 || limit > LIMIT_10) {
            logger.error("设置的值为： {}, 搜索结果限制个数必须大于等于0并且小于等于10!", limit);
            emailParam.setSuccess(false);
            emailParam.setMsg("limit设置的值为： " + limit
                    + ", 搜索结果限制个数必须大于等于0并且小于等于10!\n");
            return;
        } else {
            systemDataConfiguration.setLimit(limit);
        }

        // config marksThreshold
        int marksThreshold = configData.getInt("marksThreshold");
        if (marksThreshold < 0 || marksThreshold > THRESHOLD_100) {
            logger.error("设置的值为： {}, 服务器分数阈值必须大于等于0并且小于等于100!", marksThreshold);
            emailParam.setSuccess(false);
            emailParam.setMsg("marksThreshold设置的值为： " + marksThreshold
                    + ", 服务器分数阈值必须大于等于0并且小于等于100!\n");
            return;
        } else {
            systemDataConfiguration.setMarksThreshold(marksThreshold);
        }

        // config scheduler strategy
        boolean isSchedulerStrategyChanged = false;
        int schedulerStrategy = configData.getInt("schedulerStrategy");
        if (schedulerStrategy < 0
                || schedulerStrategy > SchedulerStrategy.OTHER.ordinal()) {
            logger.error("设置的值为： {}, 服务器调度策略必须大于等于0并且小于等于{}!",
                    schedulerStrategy, SchedulerStrategy.OTHER.ordinal());
            emailParam.setSuccess(false);
            emailParam.setMsg("schedulerStrategy设置的值为： " + schedulerStrategy
                    + ", 服务器调度策略必须大于等于0并且小于等于"
                    + SchedulerStrategy.OTHER.ordinal() + "!\n");
            return;
        } else {
            if (systemDataConfiguration.getSchedulerStrategy() != null
                    && systemDataConfiguration.getSchedulerStrategy().ordinal() != schedulerStrategy) {
                isSchedulerStrategyChanged = true;
            }
            systemDataConfiguration.setSchedulerStrategy(SchedulerStrategy
                    .values()[schedulerStrategy]);
        }

        // config cnn process number threshold
        int cnnProcessNumThres = configData.getInt("cnnProcessNumThres");
        if (cnnProcessNumThres < 0) {
            logger.error("设置的值为： {}, cnn服务器处理数阈值必须大于等于0!", cnnProcessNumThres);
            emailParam.setSuccess(false);
            emailParam.setMsg("cnnProcessNumThres设置的值为： " + cnnProcessNumThres
                    + ", cnn服务器处理数阈值必须大于等于0!\n");
            return;
        } else {
            systemDataConfiguration.setCnnProcessNumThres(cnnProcessNumThres);
            systemDataConfiguration.setMaxCnnProcessNum(cnnProcessNumThres);
        }

        // config cnn process max number threshold
        int cnnProcessMaxNumThres = configData.getInt("cnnProcessMaxNumThres");
        if (cnnProcessMaxNumThres < 0) {
            logger.error("设置的值为： {}, cnn服务器最大处理数阈值必须大于等于0!",
                    cnnProcessMaxNumThres);
            emailParam.setSuccess(false);
            emailParam.setMsg("cnnProcessMaxNumThres设置的值为： "
                    + cnnProcessMaxNumThres + ", cnn服务器最大处理数阈值必须大于等于0!\n");
            return;
        } else {
            systemDataConfiguration
                    .setCnnProcessMaxNumThres(cnnProcessMaxNumThres);
        }

        // config search process number threshold
        int searchProcessNumThres = configData.getInt("searchProcessNumThres");
        if (searchProcessNumThres < 0) {
            logger.error("设置的值为： {}, search服务器处理数阈值必须大于等于0!",
                    searchProcessNumThres);
            emailParam.setSuccess(false);
            emailParam.setMsg("searchProcessNumThres设置的值为： "
                    + searchProcessNumThres + ", search服务器处理数阈值必须大于等于0!\n");
            return;
        } else {
            systemDataConfiguration
                    .setSearchProcessNumThres(searchProcessNumThres);
            systemDataConfiguration
                    .setMaxSearchProcessNum(searchProcessNumThres);
        }

        // config search query url
        String searchQuery = configData.getString("searchQuery");
        systemDataConfiguration.setSearchQuery(searchQuery);

        // config search query by id url
        String searchQueryById = configData.getString("searchQueryById");
        systemDataConfiguration.setSearchQueryById(searchQueryById);

        // config search query by id user
        String searchQueryByIdUser = configData
                .getString("searchQueryByIdUser");
        systemDataConfiguration.setSearchQueryByIdUser(searchQueryByIdUser);

        // config search query by id token
        String searchQueryByIdToken = configData
                .getString("searchQueryByIdToken");
        systemDataConfiguration.setSearchQueryByIdToken(searchQueryByIdToken);

        // config word search query url
        String wordSearchQuery = configData.getString("wordSearchQuery");
        systemDataConfiguration.setWordSearchQuery(wordSearchQuery);

        // config classic poem query url
        String classicPoemQuery = configData.getString("classicPoemQuery");
        systemDataConfiguration.setClassicPoemQuery(classicPoemQuery);

        // config classic poem auto complete url
        String classicPoemAutoComplete = configData
                .getString("classicPoemAutoComplete");
        systemDataConfiguration
                .setClassicPoemAutoComplete(classicPoemAutoComplete);

        // config article query url
        String articleQuery = configData.getString("articleQuery");
        systemDataConfiguration.setArticleQuery(articleQuery);

        // config article query by id url
        String articleQueryById = configData.getString("articleQueryById");
        systemDataConfiguration.setArticleQueryById(articleQueryById);

        // config article auto complete url
        String articleAutoComplete = configData
                .getString("articleAutoComplete");
        systemDataConfiguration.setArticleAutoComplete(articleAutoComplete);

        // config article query log address
        String articleQueryLogAddress = configData
                .getString("articleQueryLogAddress");
        systemDataConfiguration
                .setArticleQueryLogAddress(articleQueryLogAddress);

        // config article auto complete log address
        String articleAutoCompleteLogAddress = configData
                .getString("articleAutoCompleteLogAddress");
        systemDataConfiguration
                .setArticleAutoCompleteLogAddress(articleAutoCompleteLogAddress);

        // config em query url
        String emQuery = configData.getString("emQuery");
        systemDataConfiguration.setEmQuery(emQuery);

        // async queue size
        int asyncQueueSize = configData.getInt("asyncQueueSize");
        if (asyncQueueSize < 0) {
            logger.error("设置的值为： {}, 异步队列的大小必须大于等于0!", asyncQueueSize);
            emailParam.setSuccess(false);
            emailParam.setMsg("asyncQueueSize设置的值为： " + asyncQueueSize
                    + ", 异步队列的大小必须大于等于0!\n");
            return;
        } else {
            systemDataConfiguration.setAsyncQueueSize(asyncQueueSize);
        }

        // query request limit
        int queryRequestLimit = configData.getInt("queryRequestLimit");
        if (queryRequestLimit < 0) {
            logger.error("设置的值为： {}, query请求限制必须大于等于0!", queryRequestLimit);
            emailParam.setSuccess(false);
            emailParam.setMsg("queryRequestLimit设置的值为： " + queryRequestLimit
                    + ", query请求限制必须大于等于0!\n");
            return;
        } else {
            systemDataConfiguration.setQueryRequestLimit(queryRequestLimit);
        }

        // query BI queue limit
        int queryBiQueueLimit = configData.getInt("queryBiQueueLimit");
        if (queryBiQueueLimit < 0) {
            logger.error("设置的值为： {}, query BI队列限制必须大于等于0!", queryBiQueueLimit);
            emailParam.setSuccess(false);
            emailParam.setMsg("queryBiQueueLimit设置的值为： " + queryBiQueueLimit
                    + ", query BI队列限制必须大于等于0!\n");
            return;
        } else {
            systemDataConfiguration.setQueryBiQueueLimit(queryBiQueueLimit);
        }

        // config jedis ip
        String jedisIp = configData.getString("jedisIp");
        systemDataConfiguration.setJedisIp(jedisIp);

        // config jedis port
        int jedisPort = configData.getInt("jedisPort");
        systemDataConfiguration.setJedisPort(jedisPort);

        // config local jedis ip
        String localJedisIp = configData.getString("localJedisIp");
        systemDataConfiguration.setLocalJedisIp(localJedisIp);

        // config local Jedis port
        int localJedisPort = configData.getInt("localJedisPort");
        systemDataConfiguration.setLocalJedisPort(localJedisPort);

        // config redis expire seconds
        int redisExpireSeconds = configData.getInt("redisExpireSeconds");
        systemDataConfiguration.setRedisExpireSeconds(redisExpireSeconds);

        // config max total connections
        int maxTotalConnections = configData.getInt("maxTotalConnections");
        systemDataConfiguration.setMaxTotalConnections(maxTotalConnections);

        // config default max connections per route
        int defaultMaxConnectionsPerRoute = configData
                .getInt("defaultMaxConnectionsPerRoute");
        systemDataConfiguration
                .setDefaultMaxConnectionsPerRoute(defaultMaxConnectionsPerRoute);

        // config cnn connections per route
        int cnnConnectionsPerRoute = configData
                .getInt("cnnConnectionsPerRoute");
        systemDataConfiguration
                .setCnnConnectionsPerRoute(cnnConnectionsPerRoute);

        // config search connections per route
        int searchConnectionsPerRoute = configData
                .getInt("searchConnectionsPerRoute");
        systemDataConfiguration
                .setSearchConnectionsPerRoute(searchConnectionsPerRoute);

        // config ugc handwrite ocr connections
        // int ugcHandwriteOcrConnections = configData
        // .getInt("ugcHandwriteOcrConnections");
        // systemDataConfiguration
        // .setUgcHandwriteOcrConnections(ugcHandwriteOcrConnections);

        schedulerConfiguration
                .setSystemDataConfiguration(systemDataConfiguration);

        emailParam.setMsg(emailParam.getMsg() + "old system data:\n"
                + oldSystemData + "\n\n new system data:\n"
                + configData.toString() + "\n\n\n");
        oldSystemData = configData.toString();

        // 调度策略变化并且目前仅支持文件修改，不支持email修改
        if (isSchedulerStrategyChanged && configParam.getConfigData() == null) {
            // config cnn servers
            configCnnServers(schedulerConfiguration, configParam, emailParam);
            // config java servers
            configJavaServers(schedulerConfiguration, configParam, emailParam);
            // config search servers
            configSearchServers(schedulerConfiguration, configParam, emailParam);
            // config search homework servers
            configSearchHomeworkServers(schedulerConfiguration, configParam,
                    emailParam);
            // config search article servers
            configSearchArticleServers(schedulerConfiguration, configParam,
                    emailParam);
            // config nlp servers
            configNlpServers(schedulerConfiguration, configParam, emailParam);
            // config search by id servers
            configSearchByIdServers(schedulerConfiguration, configParam,
                    emailParam);
            // config em servers
            configEmServers(schedulerConfiguration, configParam, emailParam);
            // config ie servers
            configIeServers(schedulerConfiguration, configParam, emailParam);
        }
    }

    private void configSystemSwitch(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取system_switch文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/system_switch");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config system_switch IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config system_switch IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config system_switch JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config system_switch JE!\n");
                    return;
                }
            }
        }
        logger.info("old system switch: {}", oldSystemSwitch);
        logger.info("new system switch: {}", configData.toString());

        // config nlp on switch
        boolean nlpOnSwitch = configData.getBoolean("nlpOnSwitch");
        schedulerConfiguration.setNlpOnSwitch(nlpOnSwitch);

        // config bi on switch
        boolean biOnSwitch = configData.getBoolean("biOnSwitch");
        schedulerConfiguration.setBiOnSwitch(biOnSwitch);

        // config save server monitor log switch
        boolean saveServerMonitorLogSwitch = configData
                .getBoolean("saveServerMonitorLogSwitch");
        schedulerConfiguration
                .setSaveServerMonitorLogSwitch(saveServerMonitorLogSwitch);

        // config save exception log switch
        boolean saveExceptionLogSwitch = configData
                .getBoolean("saveExceptionLogSwitch");
        schedulerConfiguration
                .setSaveExceptionLogSwitch(saveExceptionLogSwitch);

        // config white list on switch
        boolean whiteListOnSwitch = configData.getBoolean("whiteListOnSwitch");
        schedulerConfiguration.setWhiteListOnSwitch(whiteListOnSwitch);

        // config server down on switch
        boolean serverDownOnSwitch = configData
                .getBoolean("serverDownOnSwitch");
        schedulerConfiguration.setServerDownOnSwitch(serverDownOnSwitch);

        // config save search article log switch
        boolean saveSearchArticleLogSwitch = configData
                .getBoolean("saveSearchArticleLogSwitch");
        schedulerConfiguration
                .setSaveSearchArticleLogSwitch(saveSearchArticleLogSwitch);

        // config save timeout log switch
        boolean saveTimeoutLogSwitch = configData
                .getBoolean("saveTimeoutLogSwitch");
        schedulerConfiguration.setSaveTimeoutLogSwitch(saveTimeoutLogSwitch);

        // config mail on switch
        boolean mailOnSwitch = configData.getBoolean("mailOnSwitch");
        schedulerConfiguration.setMailOnSwitch(mailOnSwitch);

        // config exec ocr only switch
        boolean execOcrOnlySwitch = configData.getBoolean("execOcrOnlySwitch");
        schedulerConfiguration.setExecOcrOnlySwitch(execOcrOnlySwitch);

        // config save query result to redis on switch
        boolean queryResultToRedisOnSwitch = configData
                .getBoolean("queryResultToRedisOnSwitch");
        schedulerConfiguration
                .setQueryResultToRedisOnSwitch(queryResultToRedisOnSwitch);

        // config force hbase on switch
        boolean forceHbaseOnSwitch = configData
                .getBoolean("forceHbaseOnSwitch");
        schedulerConfiguration.setForceHbaseOnSwitch(forceHbaseOnSwitch);

        // config backup hbase on switch
        boolean bkHbaseOnSwitch = configData.getBoolean("bkHbaseOnSwitch");
        schedulerConfiguration.setBkHbaseOnSwitch(bkHbaseOnSwitch);

        // config ugc handwrite ocr on switch
        boolean ugcHandwriteOcrOnSwitch = configData
                .getBoolean("ugcHandwriteOcrOnSwitch");
        schedulerConfiguration
                .setUgcHandwriteOcrOnSwitch(ugcHandwriteOcrOnSwitch);

        emailParam.setMsg(emailParam.getMsg() + "old system switch:\n"
                + oldSystemSwitch + "\n\n new system switch:\n"
                + configData.toString() + "\n\n\n");
        oldSystemSwitch = configData.toString();

    }

    private void configDebugSwitch(
            SchedulerConfiguration schedulerConfiguration,
            SchedulerControllerStatistics schedulerControllerStatistics,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取debug_switch文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/debug_switch");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config debug_switch IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config debug_switch IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config debug_switch JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config debug_switch JE!\n");
                    return;
                }
            }
        }
        logger.info("old debug switch: {}", oldDebugSwitch);
        logger.info("new debug switch: {}", configData.toString());

        DebugSwitchConfiguration debugSwitchConfiguration = new DebugSwitchConfiguration();

        // 1.config all request, current request, hbase queue num debug switch
        boolean allRequestAndHbaseQueueDebugSwitch = configData
                .getBoolean("allRequestAndHbaseQueueDebugSwitch");
        debugSwitchConfiguration
                .setAllRequestAndHbaseQueueDebugSwitch(allRequestAndHbaseQueueDebugSwitch);

        // 2.config current request debug switch
        boolean currentRequestDebugSwitch = configData
                .getBoolean("currentRequestDebugSwitch");
        debugSwitchConfiguration
                .setCurrentRequestDebugSwitch(currentRequestDebugSwitch);

        // 3.config httpclient debug switch
        boolean httpclientDebugSwitch = configData
                .getBoolean("httpclientDebugSwitch");
        debugSwitchConfiguration
                .setHttpclientDebugSwitch(httpclientDebugSwitch);

        // 4.config gc debug switch
        boolean gcDebugSwitch = configData.getBoolean("gcDebugSwitch");
        debugSwitchConfiguration.setGcDebugSwitch(gcDebugSwitch);

        // 5.config display marks debug switch
        boolean displayMarksDebugSwitch = configData
                .getBoolean("displayMarksDebugSwitch");
        debugSwitchConfiguration
                .setDisplayMarksDebugSwitch(displayMarksDebugSwitch);

        // 6.config current cnn ocr request debug switch
        boolean currentCnnOcrRequestDebugSwitch = configData
                .getBoolean("currentCnnOcrRequestDebugSwitch");
        debugSwitchConfiguration
                .setCurrentCnnOcrRequestDebugSwitch(currentCnnOcrRequestDebugSwitch);

        // 7.config current search request debug switch
        boolean currentSearchRequestDebugSwitch = configData
                .getBoolean("currentSearchRequestDebugSwitch");
        debugSwitchConfiguration
                .setCurrentSearchRequestDebugSwitch(currentSearchRequestDebugSwitch);

        // 8.config current nlp request debug switch
        boolean currentNlpRequestDebugSwitch = configData
                .getBoolean("currentNlpRequestDebugSwitch");
        debugSwitchConfiguration
                .setCurrentNlpRequestDebugSwitch(currentNlpRequestDebugSwitch);

        // 9.config avg query exec time debug switch
        boolean avgQueryExecTimeDebugSwitch = configData
                .getBoolean("avgQueryExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgQueryExecTimeDebugSwitch(avgQueryExecTimeDebugSwitch);
        if (!avgQueryExecTimeDebugSwitch) {
            schedulerControllerStatistics.setExecTime(0);
            schedulerControllerStatistics.setExecTimeNum(0);
        }

        // 10.config avg query by id exec time debug switch
        boolean avgQueryByIdExecTimeDebugSwitch = configData
                .getBoolean("avgQueryByIdExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgQueryByIdExecTimeDebugSwitch(avgQueryByIdExecTimeDebugSwitch);
        if (!avgQueryByIdExecTimeDebugSwitch) {
            schedulerControllerStatistics.setExecTimeById(0);
            schedulerControllerStatistics.setExecTimeByIdNum(0);
        }

        // 11.config avg mining homework exec time debug switch
        boolean avgHomeworkExecTimeDebugSwitch = configData
                .getBoolean("avgHomeworkExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgHomeworkExecTimeDebugSwitch(avgHomeworkExecTimeDebugSwitch);
        if (!avgHomeworkExecTimeDebugSwitch) {
            schedulerControllerStatistics.setExecTimeByHomework(0);
            schedulerControllerStatistics.setExecTimeByHomeworkNum(0);
        }

        // 12.config avg search exec time debug switch
        boolean avgSearchExecTimeDebugSwitch = configData
                .getBoolean("avgSearchExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgSearchExecTimeDebugSwitch(avgSearchExecTimeDebugSwitch);
        if (!avgSearchExecTimeDebugSwitch) {
            schedulerControllerStatistics.setExecTimeBySearch(0);
            schedulerControllerStatistics.setExecTimeBySearchNum(0);
        }

        // 13.config avg word search exec time debug switch
        boolean avgWordSearchExecTimeDebugSwitch = configData
                .getBoolean("avgWordSearchExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgWordSearchExecTimeDebugSwitch(avgWordSearchExecTimeDebugSwitch);
        if (!avgWordSearchExecTimeDebugSwitch) {
            schedulerControllerStatistics.setExecTimeByWordSearch(0);
            schedulerControllerStatistics.setExecTimeByWordSearchNum(0);
        }

        // 14.config avg search article exec time debug switch
        boolean avgArticleExecTimeDebugSwitch = configData
                .getBoolean("avgArticleExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgArticleExecTimeDebugSwitch(avgArticleExecTimeDebugSwitch);
        if (!avgArticleExecTimeDebugSwitch) {
            schedulerControllerStatistics.setExecTimeByArticle(0);
            schedulerControllerStatistics.setExecTimeByArticleNum(0);
        }

        // 15.config avg save ocr hbase exec time debug switch
        boolean avgOcrHbaseExecTimeDebugSwitch = configData
                .getBoolean("avgOcrHbaseExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgOcrHbaseExecTimeDebugSwitch(avgOcrHbaseExecTimeDebugSwitch);
        if (!avgOcrHbaseExecTimeDebugSwitch) {
            schedulerControllerStatistics.setOcrHbaseExecTime(0);
            schedulerControllerStatistics.setOcrHbaseExecTimeNum(0);
        }

        // 16.config avg save search hbase exec time debug switch
        boolean avgSearchHbaseExecTimeDebugSwitch = configData
                .getBoolean("avgSearchHbaseExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgSearchHbaseExecTimeDebugSwitch(avgSearchHbaseExecTimeDebugSwitch);
        if (!avgSearchHbaseExecTimeDebugSwitch) {
            schedulerControllerStatistics.setSearchHbaseExecTime(0);
            schedulerControllerStatistics.setSearchHbaseExecTimeNum(0);
        }

        // 17.config avg save search article hbase exec time debug switch
        boolean avgSearchArticleHbaseExecTimeDebugSwitch = configData
                .getBoolean("avgSearchArticleHbaseExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgSearchArticleHbaseExecTimeDebugSwitch(avgSearchArticleHbaseExecTimeDebugSwitch);
        if (!avgSearchArticleHbaseExecTimeDebugSwitch) {
            schedulerControllerStatistics.setSearchArticleHbaseExecTime(0);
            schedulerControllerStatistics.setSearchArticleHbaseExecTimeNum(0);
        }

        // 18.config avg cnn server exec time debug switch
        boolean avgCnnServerExecTimeDebugSwitch = configData
                .getBoolean("avgCnnServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgCnnServerExecTimeDebugSwitch(avgCnnServerExecTimeDebugSwitch);
        if (!avgCnnServerExecTimeDebugSwitch) {
            if (schedulerConfiguration.getCnnServers() != null) {
                for (OcrServer ocrServer : schedulerConfiguration
                        .getCnnServers()) {
                    if (ocrServer.getOcrServerStatistics().getExecTimeNum() > 0) {
                        ocrServer.getOcrServerStatistics().setExecTime(0);
                        ocrServer.getOcrServerStatistics().setExecTimeNum(0);
                    }
                }
            }
        }

        // 19.config avg java server exec time debug switch
        boolean avgJavaServerExecTimeDebugSwitch = configData
                .getBoolean("avgJavaServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgJavaServerExecTimeDebugSwitch(avgJavaServerExecTimeDebugSwitch);
        if (!avgJavaServerExecTimeDebugSwitch) {
            if (schedulerConfiguration.getJavaServers() != null) {
                for (OcrServer ocrServer : schedulerConfiguration
                        .getJavaServers()) {
                    if (ocrServer.getOcrServerStatistics().getExecTimeNum() > 0) {
                        if (ocrServer.getOcrServerStatistics().getExecTimeNum() > 0) {
                            ocrServer.getOcrServerStatistics().setExecTime(0);
                            ocrServer.getOcrServerStatistics()
                                    .setExecTimeNum(0);
                        }
                    }
                }
            }
        }

        // 20.config avg search server exec time debug switch
        boolean avgSearchServerExecTimeDebugSwitch = configData
                .getBoolean("avgSearchServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgSearchServerExecTimeDebugSwitch(avgSearchServerExecTimeDebugSwitch);
        if (!avgSearchServerExecTimeDebugSwitch) {
            if (schedulerConfiguration.getSearchServers() != null) {
                for (SearchServer searchServer : schedulerConfiguration
                        .getSearchServers()) {
                    if (searchServer.getSearchServerStatistics()
                            .getExecTimeNum() > 0) {
                        searchServer.getSearchServerStatistics().setExecTime(0);
                        searchServer.getSearchServerStatistics()
                                .setExecTimeNum(0);
                    }
                }
            }
        }

        // config avg word search server exec time debug switch
        boolean avgWordSearchServerExecTimeDebugSwitch = configData
                .getBoolean("avgWordSearchServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgWordSearchServerExecTimeDebugSwitch(avgWordSearchServerExecTimeDebugSwitch);
        if (!avgWordSearchServerExecTimeDebugSwitch) {
            if (schedulerConfiguration.getSearchServers() != null) {
                for (SearchServer searchServer : schedulerConfiguration
                        .getSearchServers()) {
                    if (searchServer.getSearchServerStatistics()
                            .getExecTimeByWordNum() > 0) {
                        searchServer.getSearchServerStatistics()
                                .setExecTimeByWord(0);
                        searchServer.getSearchServerStatistics()
                                .setExecTimeByWordNum(0);
                    }
                }
            }
        }

        // 21.config avg search by id server exec time debug switch
        boolean avgSearchByIdServerExecTimeDebugSwitch = configData
                .getBoolean("avgSearchByIdServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgSearchByIdServerExecTimeDebugSwitch(avgSearchByIdServerExecTimeDebugSwitch);
        if (!avgSearchByIdServerExecTimeDebugSwitch) {
            if (schedulerConfiguration.getSearchServers() != null) {
                for (SearchServer searchServer : schedulerConfiguration
                        .getSearchServers()) {
                    if (searchServer.getSearchServerStatistics()
                            .getExecTimeByIdNum() > 0) {
                        searchServer.getSearchServerStatistics()
                                .setExecTimeById(0);
                        searchServer.getSearchServerStatistics()
                                .setExecTimeByIdNum(0);
                    }
                }
            }
        }

        // 22.config avg search homework server exec time debug switch
        boolean avgSearchHomeworkServerExecTimeDebugSwitch = configData
                .getBoolean("avgSearchHomeworkServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgSearchHomeworkServerExecTimeDebugSwitch(avgSearchHomeworkServerExecTimeDebugSwitch);
        if (!avgSearchHomeworkServerExecTimeDebugSwitch) {
            if (schedulerConfiguration.getSearchHomeworkServers() != null) {
                for (SearchServer searchHomeworkServer : schedulerConfiguration
                        .getSearchHomeworkServers()) {
                    if (searchHomeworkServer.getSearchServerStatistics()
                            .getExecTimeByHomeworkNum() > 0) {
                        searchHomeworkServer.getSearchServerStatistics()
                                .setExecTimeByHomework(0);
                        searchHomeworkServer.getSearchServerStatistics()
                                .setExecTimeByHomeworkNum(0);
                    }
                }
            }
        }

        // 23.config avg search article server exec time debug switch
        boolean avgSearchArticleServerExecTimeDebugSwitch = configData
                .getBoolean("avgSearchArticleServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgSearchArticleServerExecTimeDebugSwitch(avgSearchArticleServerExecTimeDebugSwitch);
        if (!avgSearchArticleServerExecTimeDebugSwitch) {
            if (schedulerConfiguration.getSearchArticleServers() != null) {
                for (SearchServer searchArticleServer : schedulerConfiguration
                        .getSearchArticleServers()) {
                    if (searchArticleServer.getSearchServerStatistics()
                            .getExecTimeByArticleNum() > 0) {
                        searchArticleServer.getSearchServerStatistics()
                                .setExecTimeByArticle(0);
                        searchArticleServer.getSearchServerStatistics()
                                .setExecTimeByArticleNum(0);
                    }
                }
            }
        }

        // 24.config avg nlp server exec time debug switch
        boolean avgNlpServerExecTimeDebugSwitch = configData
                .getBoolean("avgNlpServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setAvgNlpServerExecTimeDebugSwitch(avgNlpServerExecTimeDebugSwitch);
        if (!avgNlpServerExecTimeDebugSwitch) {
            if (schedulerConfiguration.getNlpServers() != null) {
                for (NlpServer nlpServer : schedulerConfiguration
                        .getNlpServers()) {
                    if (nlpServer.getNlpServerStatistics().getExecTimeNum() > 0) {
                        nlpServer.getNlpServerStatistics().setExecTime(0);
                        nlpServer.getNlpServerStatistics().setExecTimeNum(0);
                    }
                }
            }
        }

        // 25.config query exec time debug switch
        boolean queryExecTimeDebugSwitch = configData
                .getBoolean("queryExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setQueryExecTimeDebugSwitch(queryExecTimeDebugSwitch);

        // 26.config query by id exec time debug switch
        boolean queryByIdExecTimeDebugSwitch = configData
                .getBoolean("queryByIdExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setQueryByIdExecTimeDebugSwitch(queryByIdExecTimeDebugSwitch);

        // 27.config mining homework exec time debug switch
        boolean homeworkExecTimeDebugSwitch = configData
                .getBoolean("homeworkExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setHomeworkExecTimeDebugSwitch(homeworkExecTimeDebugSwitch);

        // 28.config search exec time debug switch
        boolean searchExecTimeDebugSwitch = configData
                .getBoolean("searchExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setSearchExecTimeDebugSwitch(searchExecTimeDebugSwitch);

        // 29.config word search exec time debug switch
        boolean wordSearchExecTimeDebugSwitch = configData
                .getBoolean("wordSearchExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setWordSearchExecTimeDebugSwitch(wordSearchExecTimeDebugSwitch);

        // 30.config search article exec time debug switch
        boolean articleExecTimeDebugSwitch = configData
                .getBoolean("articleExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setArticleExecTimeDebugSwitch(articleExecTimeDebugSwitch);

        // config classic poem query exec time debug switch
        boolean classicPoemQueryExecTimeDebugSwitch = configData
                .getBoolean("classicPoemQueryExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setClassicPoemQueryExecTimeDebugSwitch(classicPoemQueryExecTimeDebugSwitch);

        // config classic poem auto complete exec time debug switch
        boolean classicPoemAutoCompleteExecTimeDebugSwitch = configData
                .getBoolean("classicPoemAutoCompleteExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setClassicPoemAutoCompleteExecTimeDebugSwitch(classicPoemAutoCompleteExecTimeDebugSwitch);

        // config em query exec time debug switch
        boolean emQueryExecTimeDebugSwitch = configData
                .getBoolean("emQueryExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setEmQueryExecTimeDebugSwitch(emQueryExecTimeDebugSwitch);

        // 31.config save ocr to hbase exec time
        boolean ocrHbaseExecTimeDebugSwitch = configData
                .getBoolean("ocrHbaseExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setOcrHbaseExecTimeDebugSwitch(ocrHbaseExecTimeDebugSwitch);

        // 32.config save search to hbase exec time
        boolean searchHbaseExecTimeDebugSwitch = configData
                .getBoolean("searchHbaseExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setSearchHbaseExecTimeDebugSwitch(searchHbaseExecTimeDebugSwitch);

        // 33.config save search article to hbase exec time
        boolean searchArticleHbaseExecTimeDebugSwitch = configData
                .getBoolean("searchArticleHbaseExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setSearchArticleHbaseExecTimeDebugSwitch(searchArticleHbaseExecTimeDebugSwitch);

        // 34.config cnn server exec time debug switch
        boolean cnnServerExecTimeDebugSwitch = configData
                .getBoolean("cnnServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setCnnServerExecTimeDebugSwitch(cnnServerExecTimeDebugSwitch);

        // 35.config java server exec time debug switch
        boolean javaServerExecTimeDebugSwitch = configData
                .getBoolean("javaServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setJavaServerExecTimeDebugSwitch(javaServerExecTimeDebugSwitch);

        // 36.config search server exec time debug switch
        boolean searchServerExecTimeDebugSwitch = configData
                .getBoolean("searchServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setSearchServerExecTimeDebugSwitch(searchServerExecTimeDebugSwitch);

        // config word search server exec time debug switch
        boolean wordSearchServerExecTimeDebugSwitch = configData
                .getBoolean("wordSearchServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setWordSearchServerExecTimeDebugSwitch(wordSearchServerExecTimeDebugSwitch);

        // 37.config search by id server exec time debug switch
        boolean searchByIdServerExecTimeDebugSwitch = configData
                .getBoolean("searchByIdServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setSearchByIdServerExecTimeDebugSwitch(searchByIdServerExecTimeDebugSwitch);

        // 38.config search homework server exec time debug switch
        boolean searchHomeworkServerExecTimeDebugSwitch = configData
                .getBoolean("searchHomeworkServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setSearchHomeworkServerExecTimeDebugSwitch(searchHomeworkServerExecTimeDebugSwitch);

        // 39.config search article server exec time debug switch
        boolean searchArticleServerExecTimeDebugSwitch = configData
                .getBoolean("searchArticleServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setSearchArticleServerExecTimeDebugSwitch(searchArticleServerExecTimeDebugSwitch);

        // 40.config nlp server exec time debug switch
        boolean nlpServerExecTimeDebugSwitch = configData
                .getBoolean("nlpServerExecTimeDebugSwitch");
        debugSwitchConfiguration
                .setNlpServerExecTimeDebugSwitch(nlpServerExecTimeDebugSwitch);

        // 41.redis debug switch
        boolean redisDebugSwitch = configData.getBoolean("redisDebugSwitch");
        debugSwitchConfiguration.setRedisDebugSwitch(redisDebugSwitch);

        // backup hbase debug switch
        boolean bkHbaseDebugSwitch = configData
                .getBoolean("bkHbaseDebugSwitch");
        debugSwitchConfiguration.setBkHbaseDebugSwitch(bkHbaseDebugSwitch);

        schedulerConfiguration
                .setDebugSwitchConfiguration(debugSwitchConfiguration);

        emailParam.setMsg(emailParam.getMsg() + "old debug switch:\n"
                + oldDebugSwitch + "\n\n new debug switch:\n"
                + configData.toString() + "\n\n\n");
        oldDebugSwitch = configData.toString();
    }

    private void configConfigServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取access_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/access_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config access_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config access_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config access_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config access_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old config servers: {}", oldConfigServers);
        logger.info("new config servers: {}", configData.toString());

        List<ConfigServer> configServers;
        JSONArray configServersArrary = configData
                .getJSONArray("accessServers");
        if (configServersArrary.size() > 0) {
            configServers = new ArrayList<ConfigServer>();
            for (int i = 0; i < configServersArrary.size(); i++) {
                JSONObject configServerJson = (JSONObject) configServersArrary
                        .get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getConfigServers() != null
                        && schedulerConfiguration.getConfigServers().size() > 0) {
                    String id = configServerJson.getString("id");
                    String ip = configServerJson.getString("ip");
                    for (ConfigServer configServer : schedulerConfiguration
                            .getConfigServers()) {
                        if (id.equals(configServer.getId())
                                && ip.equals(configServer.getIp())) {
                            configServers.add(configServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    ConfigServer configServer = new ConfigServer();
                    configServer.setId(configServerJson.getString("id"));
                    configServer.setIp(configServerJson.getString("ip"));
                    configServer.setServerType(ServerType.CONFIG);
                    ConfigServerStatistics configServerStatistics = new ConfigServerStatistics();
                    configServer
                            .setConfigServerStatistics(configServerStatistics);
                    configServers.add(configServer);
                }
            }
            schedulerConfiguration.setConfigServers(configServers);
        } else if (schedulerConfiguration.getConfigServers() != null) {
            schedulerConfiguration.getConfigServers().clear();
            schedulerConfiguration.setConfigServers(null);
        }

        emailParam.setMsg(emailParam.getMsg() + "old config servers:\n"
                + oldConfigServers + "\n\n new config servers:\n"
                + configData.toString() + "\n\n\n");
        oldConfigServers = configData.toString();
    }

    private void configCnnServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取cnn_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/cnn_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config cnn_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config cnn_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config cnn_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config cnn_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old cnn servers: {}", oldCnnServers);
        logger.info("new cnn servers: {}", configData.toString());

        List<OcrServer> cnnServers;
        JSONArray cnnServersArrary = configData.getJSONArray("cnnServers");
        if (cnnServersArrary.size() > 0) {
            cnnServers = new ArrayList<OcrServer>();
            for (int i = 0; i < cnnServersArrary.size(); i++) {
                JSONObject cnnServerJson = (JSONObject) cnnServersArrary.get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getCnnServers() != null
                        && schedulerConfiguration.getCnnServers().size() > 0) {
                    String id = cnnServerJson.getString("id");
                    String ip = cnnServerJson.getString("ip");
                    String port = cnnServerJson.getString("port");
                    String url = cnnServerJson.getString("url");
                    for (OcrServer ocrServer : schedulerConfiguration
                            .getCnnServers()) {
                        if (id.equals(ocrServer.getId())
                                && ip.equals(ocrServer.getIp())
                                && port.equals(ocrServer.getPort())
                                && url.equals(ocrServer.getUrl())) {
                            if (cnnServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的CNN服务器权值为： {}, 该权值必须大于等于0!",
                                        ocrServer.getId(),
                                        cnnServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + ocrServer.getId()
                                        + "的CNN服务器权值为： "
                                        + cnnServerJson.getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            ocrServer.setWeight(cnnServerJson.getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    cnnServerJson.getString("state"))) {
                                if (ServerState.DOWN.equals(ocrServer
                                        .getState())) {
                                    ocrServer
                                            .setMarks(SchedulerConstants.MARKS_100);
                                }
                                ocrServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    cnnServerJson.getString("state"))) {
                                ocrServer.setState(ServerState.DOWN);
                                ocrServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的CNN服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        ocrServer.getId(),
                                        cnnServerJson.getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + ocrServer.getId()
                                        + "的CNN服务器状态为： "
                                        + cnnServerJson.getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            cnnServers.add(ocrServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    OcrServer cnnServer = new OcrServer();
                    cnnServer.setId(cnnServerJson.getString("id"));
                    if (ServerState.UP.toString().equals(
                            cnnServerJson.getString("state"))) {
                        cnnServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            cnnServerJson.getString("state"))) {
                        cnnServer.setState(ServerState.DOWN);
                    } else {
                        logger.error("设置的ID为{}的CNN服务器状态为： {}, 该值必须为UP或者DOWN!",
                                cnnServerJson.getString("id"),
                                cnnServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + cnnServerJson.getString("id")
                                + "的CNN服务器状态为： "
                                + cnnServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    cnnServer.setIp(cnnServerJson.getString("ip"));
                    cnnServer.setPort(cnnServerJson.getString("port"));
                    cnnServer.setUrl(cnnServerJson.getString("url"));
                    cnnServer.setServerType(ServerType.CNN);
                    cnnServer.setFailConnNum(0);
                    if (cnnServerJson.getInt("weight") < 0) {
                        logger.error("设置的ID为{}的CNN服务器权值为： {}, 该权值必须大于等于0!",
                                cnnServerJson.getString("id"),
                                cnnServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + cnnServerJson.getString("id")
                                + "的CNN服务器权值为： "
                                + cnnServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    cnnServer.setWeight(cnnServerJson.getInt("weight"));
                    cnnServer.setAbsoluteWeight(0);
                    OcrServerStatistics ocrServerStatistics = new OcrServerStatistics();
                    cnnServer.setOcrServerStatistics(ocrServerStatistics);
                    cnnServers.add(cnnServer);
                }
            }
            schedulerConfiguration.setCnnServers(cnnServers);
            if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                Map<Integer, OcrServer> cnnWeightTable = new TreeMap<Integer, OcrServer>();
                int currentCnnWeight = 0;
                int validCnnServersSize = 0;
                for (OcrServer cnnServer : schedulerConfiguration
                        .getCnnServers()) {
                    if (ServerState.UP.equals(cnnServer.getState())
                            && cnnServer.getWeight() > 0) {
                        currentCnnWeight += cnnServer.getWeight();
                        cnnWeightTable.put(currentCnnWeight, cnnServer);
                        cnnServer.setAbsoluteWeight(currentCnnWeight);
                        ++validCnnServersSize;
                    }
                }
                schedulerConfiguration.setCnnTotalWeight(currentCnnWeight);
                schedulerConfiguration.setCnnWeightTable(cnnWeightTable);
                schedulerConfiguration
                        .setValidCnnServersSize(validCnnServersSize);
            } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<OcrServer> cnnServersPollingList = new ArrayList<OcrServer>();
                int validCnnServersSize = 0;
                for (OcrServer cnnServer : schedulerConfiguration
                        .getCnnServers()) {
                    if (ServerState.UP.equals(cnnServer.getState())
                            && cnnServer.getWeight() > 0) {
                        int weight = cnnServer.getWeight() / DIVISOR_10;
                        while (weight-- > 0) {
                            cnnServersPollingList.add(cnnServer);
                        }
                        ++validCnnServersSize;
                    }
                }
                if (cnnServersPollingList.size() > 0) {
                    schedulerConfiguration.setCurrentPollingCnnServerIndex(r
                            .nextInt(cnnServersPollingList.size()));
                    schedulerConfiguration
                            .setCnnServersPollingList(cnnServersPollingList);
                    schedulerConfiguration
                            .setCnnServersPollingListSize(cnnServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration.setCurrentPollingCnnServerIndex(-1);
                }
                schedulerConfiguration
                        .setValidCnnServersSize(validCnnServersSize);
            }
        } else if (schedulerConfiguration.getCnnServers() != null) {
            schedulerConfiguration.getCnnServers().clear();
            schedulerConfiguration.setCnnServers(null);
            schedulerConfiguration.getCnnServersPollingList().clear();
            schedulerConfiguration.setCnnServersPollingList(null);
            schedulerConfiguration.setCurrentPollingCnnServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old cnn servers:\n"
                + oldCnnServers + "\n\n new cnn servers:\n"
                + configData.toString() + "\n\n\n");
        oldCnnServers = configData.toString();
    }

    private void configJavaServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取java_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/java_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config java_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config java_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config java_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config java_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old java servers: {}", oldJavaServers);
        logger.info("new java servers: {}", configData.toString());

        List<OcrServer> javaServers;
        JSONArray javaServersArrary = configData.getJSONArray("javaServers");
        if (javaServersArrary.size() > 0) {
            javaServers = new ArrayList<OcrServer>();
            for (int i = 0; i < javaServersArrary.size(); i++) {
                JSONObject javaServerJson = (JSONObject) javaServersArrary
                        .get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getJavaServers() != null
                        && schedulerConfiguration.getJavaServers().size() > 0) {
                    String id = javaServerJson.getString("id");
                    String ip = javaServerJson.getString("ip");
                    String port = javaServerJson.getString("port");
                    String url = javaServerJson.getString("url");
                    for (OcrServer ocrServer : schedulerConfiguration
                            .getJavaServers()) {
                        if (id.equals(ocrServer.getId())
                                && ip.equals(ocrServer.getIp())
                                && port.equals(ocrServer.getPort())
                                && url.equals(ocrServer.getUrl())) {
                            if (javaServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的JAVA服务器权值为： {}, 该权值必须大于等于0!",
                                        ocrServer.getId(),
                                        javaServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + ocrServer.getId()
                                        + "的JAVA服务器权值为： "
                                        + javaServerJson.getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            ocrServer
                                    .setWeight(javaServerJson.getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    javaServerJson.getString("state"))) {
                                if (ServerState.DOWN.equals(ocrServer
                                        .getState())) {
                                    ocrServer
                                            .setMarks(SchedulerConstants.MARKS_100);
                                }
                                ocrServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    javaServerJson.getString("state"))) {
                                ocrServer.setState(ServerState.DOWN);
                                ocrServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的JAVA服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        ocrServer.getId(),
                                        javaServerJson.getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + ocrServer.getId()
                                        + "的JAVA服务器状态为： "
                                        + javaServerJson.getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            javaServers.add(ocrServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    OcrServer javaServer = new OcrServer();
                    javaServer.setId(javaServerJson.getString("id"));
                    if (ServerState.UP.toString().equals(
                            javaServerJson.getString("state"))) {
                        javaServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            javaServerJson.getString("state"))) {
                        javaServer.setState(ServerState.DOWN);
                    } else {
                        logger.error("设置的ID为{}的JAVA服务器状态为： {}, 该值必须为UP或者DOWN!",
                                javaServerJson.getString("id"),
                                javaServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + javaServerJson.getString("id")
                                + "的JAVA服务器状态为： "
                                + javaServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    javaServer.setIp(javaServerJson.getString("ip"));
                    javaServer.setPort(javaServerJson.getString("port"));
                    javaServer.setUrl(javaServerJson.getString("url"));
                    javaServer.setServerType(ServerType.JAVA);
                    javaServer.setFailConnNum(0);
                    if (javaServerJson.getInt("weight") < 0) {
                        logger.error("设置的ID为{}的JAVA服务器权值为： {}, 该权值必须大于等于0!",
                                javaServerJson.getString("id"),
                                javaServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + javaServerJson.getString("id")
                                + "的JAVA服务器权值为： "
                                + javaServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    javaServer.setWeight(javaServerJson.getInt("weight"));
                    javaServer.setAbsoluteWeight(0);
                    OcrServerStatistics ocrServerStatistics = new OcrServerStatistics();
                    javaServer.setOcrServerStatistics(ocrServerStatistics);
                    javaServers.add(javaServer);
                }
            }
            schedulerConfiguration.setJavaServers(javaServers);
            if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                Map<Integer, OcrServer> javaWeightTable = new TreeMap<Integer, OcrServer>();
                int currentJavaWeight = 0;
                for (OcrServer javaServer : schedulerConfiguration
                        .getJavaServers()) {
                    if (ServerState.UP.equals(javaServer.getState())
                            && javaServer.getWeight() > 0) {
                        currentJavaWeight += javaServer.getWeight();
                        javaWeightTable.put(currentJavaWeight, javaServer);
                        javaServer.setAbsoluteWeight(currentJavaWeight);
                    }
                }
                schedulerConfiguration.setJavaTotalWeight(currentJavaWeight);
                schedulerConfiguration.setJavaWeightTable(javaWeightTable);
            } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<OcrServer> javaServersPollingList = new ArrayList<OcrServer>();
                for (OcrServer javaServer : schedulerConfiguration
                        .getJavaServers()) {
                    if (ServerState.UP.equals(javaServer.getState())
                            && javaServer.getWeight() > 0) {
                        int weight = javaServer.getWeight() / DIVISOR_10;
                        while (weight-- > 0) {
                            javaServersPollingList.add(javaServer);
                        }
                    }
                }
                if (javaServersPollingList.size() > 0) {
                    schedulerConfiguration.setCurrentPollingJavaServerIndex(r
                            .nextInt(javaServersPollingList.size()));
                    schedulerConfiguration
                            .setJavaServersPollingList(javaServersPollingList);
                    schedulerConfiguration
                            .setJavaServersPollingListSize(javaServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration.setCurrentPollingJavaServerIndex(-1);
                }
            }
        } else if (schedulerConfiguration.getJavaServers() != null) {
            schedulerConfiguration.getJavaServers().clear();
            schedulerConfiguration.setJavaServers(null);
            schedulerConfiguration.getJavaServersPollingList().clear();
            schedulerConfiguration.setJavaServersPollingList(null);
            schedulerConfiguration.setCurrentPollingJavaServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old java servers:\n"
                + oldJavaServers + "\n\n new java servers:\n"
                + configData.toString() + "\n\n\n");
        oldJavaServers = configData.toString();
    }

    private void configSearchServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取search_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/search_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config search_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config search_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config search_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config search_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old search servers: {}", oldSearchServers);
        logger.info("new search servers: {}", configData.toString());

        List<SearchServer> searchServers;
        JSONArray searchServersArrary = configData
                .getJSONArray("searchServers");
        if (searchServersArrary.size() > 0) {
            searchServers = new ArrayList<SearchServer>();
            for (int i = 0; i < searchServersArrary.size(); i++) {
                JSONObject searchServerJson = (JSONObject) searchServersArrary
                        .get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getSearchServers() != null
                        && schedulerConfiguration.getSearchServers().size() > 0) {
                    String id = searchServerJson.getString("id");
                    String ip = searchServerJson.getString("ip");
                    String port = searchServerJson.getString("port");
                    String url = searchServerJson.getString("url");
                    for (SearchServer searchServer : schedulerConfiguration
                            .getSearchServers()) {
                        if (id.equals(searchServer.getId())
                                && ip.equals(searchServer.getIp())
                                && port.equals(searchServer.getPort())
                                && url.equals(searchServer.getUrl())) {
                            if (searchServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的Search服务器权值为： {}, 该权值必须大于等于0!",
                                        searchServer.getId(),
                                        searchServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchServer.getId()
                                        + "的Search服务器权值为： "
                                        + searchServerJson.getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            searchServer.setWeight(searchServerJson
                                    .getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    searchServerJson.getString("state"))) {
                                if (ServerState.DOWN.equals(searchServer
                                        .getState())) {
                                    searchServer
                                            .setMarks(SchedulerConstants.MARKS_100);
                                }
                                searchServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    searchServerJson.getString("state"))) {
                                searchServer.setState(ServerState.DOWN);
                                searchServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的Search服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        searchServer.getId(),
                                        searchServerJson.getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchServer.getId()
                                        + "的Search服务器状态为： "
                                        + searchServerJson.getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            searchServers.add(searchServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    SearchServer searchServer = new SearchServer();
                    searchServer.setId(searchServerJson.getString("id"));
                    if (ServerState.UP.toString().equals(
                            searchServerJson.getString("state"))) {
                        searchServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            searchServerJson.getString("state"))) {
                        searchServer.setState(ServerState.DOWN);
                    } else {
                        logger.error(
                                "设置的ID为{}的Search服务器状态为： {}, 该值必须为UP或者DOWN!",
                                searchServerJson.getString("id"),
                                searchServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchServerJson.getString("id")
                                + "的Search服务器状态为： "
                                + searchServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    searchServer.setIp(searchServerJson.getString("ip"));
                    searchServer.setPort(searchServerJson.getString("port"));
                    searchServer.setUrl(searchServerJson.getString("url"));
                    searchServer.setServerType(ServerType.SEARCH);
                    searchServer.setFailConnNum(0);
                    if (searchServerJson.getInt("weight") < 0) {
                        logger.error("设置的ID为{}的Search服务器权值为： {}, 该权值必须大于等于0!",
                                searchServerJson.getString("id"),
                                searchServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchServerJson.getString("id")
                                + "的Search服务器权值为： "
                                + searchServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    searchServer.setWeight(searchServerJson.getInt("weight"));
                    searchServer.setAbsoluteWeight(0);
                    SearchServerStatistics searchServerStatistics = new SearchServerStatistics();
                    searchServer
                            .setSearchServerStatistics(searchServerStatistics);
                    searchServers.add(searchServer);
                }
            }
            schedulerConfiguration.setSearchServers(searchServers);
            if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                Map<Integer, SearchServer> searchWeightTable = new TreeMap<Integer, SearchServer>();
                int currentSearchWeight = 0;
                for (SearchServer searchServer : schedulerConfiguration
                        .getSearchServers()) {
                    if (ServerState.UP.equals(searchServer.getState())
                            && searchServer.getWeight() > 0) {
                        currentSearchWeight += searchServer.getWeight();
                        searchWeightTable
                                .put(currentSearchWeight, searchServer);
                        searchServer.setAbsoluteWeight(currentSearchWeight);
                    }
                }
                schedulerConfiguration
                        .setSearchTotalWeight(currentSearchWeight);
                schedulerConfiguration.setSearchWeightTable(searchWeightTable);
            } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<SearchServer> searchServersPollingList = new ArrayList<SearchServer>();
                int validSearchServersSize = 0;
                for (SearchServer searchServer : schedulerConfiguration
                        .getSearchServers()) {
                    if (ServerState.UP.equals(searchServer.getState())
                            && searchServer.getWeight() > 0) {
                        int weight = searchServer.getWeight() / DIVISOR_10;
                        while (weight-- > 0) {
                            searchServersPollingList.add(searchServer);
                        }
                        ++validSearchServersSize;
                    }
                }
                if (searchServersPollingList.size() > 0) {
                    schedulerConfiguration.setCurrentPollingSearchServerIndex(r
                            .nextInt(searchServersPollingList.size()));
                    schedulerConfiguration
                            .setSearchServersPollingList(searchServersPollingList);
                    schedulerConfiguration
                            .setSearchServersPollingListSize(searchServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration
                            .setCurrentPollingSearchServerIndex(-1);
                }
                schedulerConfiguration
                        .setValidSearchServersSize(validSearchServersSize);
            }
        } else if (schedulerConfiguration.getSearchServers() != null) {
            schedulerConfiguration.getSearchServers().clear();
            schedulerConfiguration.setSearchServers(null);
            schedulerConfiguration.getSearchServersPollingList().clear();
            schedulerConfiguration.setSearchServersPollingList(null);
            schedulerConfiguration.setCurrentPollingSearchServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old search servers:\n"
                + oldSearchServers + "\n\n new search servers:\n"
                + configData.toString() + "\n\n\n");
        oldSearchServers = configData.toString();
    }

    private void configSearchHomeworkServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取search_homework_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/search_homework_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config search_homework_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config search_homework_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config search_homework_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config search_homework_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old search homework servers: {}", oldSearchHomeworkServers);
        logger.info("new search homework servers: {}", configData.toString());

        List<SearchServer> searchHomeworkServers;
        JSONArray searchHomeworkServersArrary = configData
                .getJSONArray("searchHomeworkServers");
        if (searchHomeworkServersArrary.size() > 0) {
            searchHomeworkServers = new ArrayList<SearchServer>();
            for (int i = 0; i < searchHomeworkServersArrary.size(); i++) {
                JSONObject searchHomeworkServerJson = (JSONObject) searchHomeworkServersArrary
                        .get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getSearchHomeworkServers() != null
                        && schedulerConfiguration.getSearchHomeworkServers()
                                .size() > 0) {
                    String id = searchHomeworkServerJson.getString("id");
                    String ip = searchHomeworkServerJson.getString("ip");
                    String port = searchHomeworkServerJson.getString("port");
                    String url = searchHomeworkServerJson.getString("url");
                    for (SearchServer searchHomeworkServer : schedulerConfiguration
                            .getSearchHomeworkServers()) {
                        if (id.equals(searchHomeworkServer.getId())
                                && ip.equals(searchHomeworkServer.getIp())
                                && port.equals(searchHomeworkServer.getPort())
                                && url.equals(searchHomeworkServer.getUrl())) {
                            if (searchHomeworkServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的Search Homework服务器权值为： {}, 该权值必须大于等于0!",
                                        searchHomeworkServer.getId(),
                                        searchHomeworkServerJson
                                                .getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchHomeworkServer.getId()
                                        + "的Search Homework服务器权值为： "
                                        + searchHomeworkServerJson
                                                .getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            searchHomeworkServer
                                    .setWeight(searchHomeworkServerJson
                                            .getInt("weight"));
                            if (ServerState.UP.toString()
                                    .equals(searchHomeworkServerJson
                                            .getString("state"))) {
                                if (ServerState.DOWN
                                        .equals(searchHomeworkServer.getState())) {
                                    searchHomeworkServer
                                            .setMarks(SchedulerConstants.MARKS_100);
                                }
                                searchHomeworkServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString()
                                    .equals(searchHomeworkServerJson
                                            .getString("state"))) {
                                searchHomeworkServer.setState(ServerState.DOWN);
                                searchHomeworkServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的Search Homework服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        searchHomeworkServer.getId(),
                                        searchHomeworkServerJson
                                                .getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchHomeworkServer.getId()
                                        + "的Search Homework服务器状态为： "
                                        + searchHomeworkServerJson
                                                .getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            searchHomeworkServers.add(searchHomeworkServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    SearchServer searchHomeworkServer = new SearchServer();
                    searchHomeworkServer.setId(searchHomeworkServerJson
                            .getString("id"));
                    if (ServerState.UP.toString().equals(
                            searchHomeworkServerJson.getString("state"))) {
                        searchHomeworkServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            searchHomeworkServerJson.getString("state"))) {
                        searchHomeworkServer.setState(ServerState.DOWN);
                    } else {
                        logger.error(
                                "设置的ID为{}的Search Homework服务器状态为： {}, 该值必须为UP或者DOWN!",
                                searchHomeworkServerJson.getString("id"),
                                searchHomeworkServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchHomeworkServerJson.getString("id")
                                + "的Search Homework服务器状态为： "
                                + searchHomeworkServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    searchHomeworkServer.setIp(searchHomeworkServerJson
                            .getString("ip"));
                    searchHomeworkServer.setPort(searchHomeworkServerJson
                            .getString("port"));
                    searchHomeworkServer.setUrl(searchHomeworkServerJson
                            .getString("url"));
                    searchHomeworkServer
                            .setServerType(ServerType.SEARCH_HOMEWORK);
                    searchHomeworkServer.setFailConnNum(0);
                    if (searchHomeworkServerJson.getInt("weight") < 0) {
                        logger.error(
                                "设置的ID为{}的Search Homework服务器权值为： {}, 该权值必须大于等于0!",
                                searchHomeworkServerJson.getString("id"),
                                searchHomeworkServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchHomeworkServerJson.getString("id")
                                + "的Search Homework服务器权值为： "
                                + searchHomeworkServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    searchHomeworkServer.setWeight(searchHomeworkServerJson
                            .getInt("weight"));
                    searchHomeworkServer.setAbsoluteWeight(0);
                    SearchServerStatistics searchHomeworkServerStatistics = new SearchServerStatistics();
                    searchHomeworkServer
                            .setSearchServerStatistics(searchHomeworkServerStatistics);
                    searchHomeworkServers.add(searchHomeworkServer);
                }
            }
            schedulerConfiguration
                    .setSearchHomeworkServers(searchHomeworkServers);
            if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                Map<Integer, SearchServer> searchHomeworkWeightTable = new TreeMap<Integer, SearchServer>();
                int currentSearchHomeworkWeight = 0;
                for (SearchServer searchHomeworkServer : schedulerConfiguration
                        .getSearchHomeworkServers()) {
                    if (ServerState.UP.equals(searchHomeworkServer.getState())
                            && searchHomeworkServer.getWeight() > 0) {
                        currentSearchHomeworkWeight += searchHomeworkServer
                                .getWeight();
                        searchHomeworkWeightTable.put(
                                currentSearchHomeworkWeight,
                                searchHomeworkServer);
                        searchHomeworkServer
                                .setAbsoluteWeight(currentSearchHomeworkWeight);
                    }
                }
                schedulerConfiguration
                        .setSearchHomeworkTotalWeight(currentSearchHomeworkWeight);
                schedulerConfiguration
                        .setSearchHomeworkWeightTable(searchHomeworkWeightTable);
            } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<SearchServer> searchHomeworkServersPollingList = new ArrayList<SearchServer>();
                for (SearchServer searchHomeworkServer : schedulerConfiguration
                        .getSearchHomeworkServers()) {
                    if (ServerState.UP.equals(searchHomeworkServer.getState())
                            && searchHomeworkServer.getWeight() > 0) {
                        int weight = searchHomeworkServer.getWeight()
                                / DIVISOR_10;
                        while (weight-- > 0) {
                            searchHomeworkServersPollingList
                                    .add(searchHomeworkServer);
                        }
                    }
                }
                if (searchHomeworkServersPollingList.size() > 0) {
                    schedulerConfiguration
                            .setCurrentPollingSearchHomeworkServerIndex(r
                                    .nextInt(searchHomeworkServersPollingList
                                            .size()));
                    schedulerConfiguration
                            .setSearchHomeworkServersPollingList(searchHomeworkServersPollingList);
                    schedulerConfiguration
                            .setSearchHomeworkServersPollingListSize(searchHomeworkServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration
                            .setCurrentPollingSearchHomeworkServerIndex(-1);
                }
            }
        } else if (schedulerConfiguration.getSearchHomeworkServers() != null) {
            schedulerConfiguration.getSearchHomeworkServers().clear();
            schedulerConfiguration.setSearchHomeworkServers(null);
            schedulerConfiguration.getSearchHomeworkServersPollingList()
                    .clear();
            schedulerConfiguration.setSearchHomeworkServersPollingList(null);
            schedulerConfiguration
                    .setCurrentPollingSearchHomeworkServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg()
                + "old search homework servers:\n" + oldSearchHomeworkServers
                + "\n\n new search homework servers:\n" + configData.toString()
                + "\n\n\n");
        oldSearchHomeworkServers = configData.toString();
    }

    private void configSearchArticleServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取search_article_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/search_article_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config search_article_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config search_article_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config search_article_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config search_article_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old search article servers: {}", oldSearchArticleServers);
        logger.info("new search article servers: {}", configData.toString());

        List<SearchServer> searchArticleServers;
        JSONArray searchArticleServersArrary = configData
                .getJSONArray("searchArticleServers");
        if (searchArticleServersArrary.size() > 0) {
            searchArticleServers = new ArrayList<SearchServer>();
            for (int i = 0; i < searchArticleServersArrary.size(); i++) {
                JSONObject searchArticleServerJson = (JSONObject) searchArticleServersArrary
                        .get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getSearchArticleServers() != null
                        && schedulerConfiguration.getSearchArticleServers()
                                .size() > 0) {
                    String id = searchArticleServerJson.getString("id");
                    String ip = searchArticleServerJson.getString("ip");
                    String port = searchArticleServerJson.getString("port");
                    String url = searchArticleServerJson.getString("url");
                    for (SearchServer searchArticleServer : schedulerConfiguration
                            .getSearchArticleServers()) {
                        if (id.equals(searchArticleServer.getId())
                                && ip.equals(searchArticleServer.getIp())
                                && port.equals(searchArticleServer.getPort())
                                && url.equals(searchArticleServer.getUrl())) {
                            if (searchArticleServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的Search Article服务器权值为： {}, 该权值必须大于等于0!",
                                        searchArticleServer.getId(),
                                        searchArticleServerJson
                                                .getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchArticleServer.getId()
                                        + "的Search Article服务器权值为： "
                                        + searchArticleServerJson
                                                .getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            searchArticleServer
                                    .setWeight(searchArticleServerJson
                                            .getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    searchArticleServerJson.getString("state"))) {
                                if (ServerState.DOWN.equals(searchArticleServer
                                        .getState())) {
                                    searchArticleServer
                                            .setMarks(SchedulerConstants.MARKS_100);
                                }
                                searchArticleServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    searchArticleServerJson.getString("state"))) {
                                searchArticleServer.setState(ServerState.DOWN);
                                searchArticleServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的Search Article服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        searchArticleServer.getId(),
                                        searchArticleServerJson
                                                .getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchArticleServer.getId()
                                        + "的Search Article服务器状态为： "
                                        + searchArticleServerJson
                                                .getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            searchArticleServers.add(searchArticleServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    SearchServer searchArticleServer = new SearchServer();
                    searchArticleServer.setId(searchArticleServerJson
                            .getString("id"));
                    if (ServerState.UP.toString().equals(
                            searchArticleServerJson.getString("state"))) {
                        searchArticleServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            searchArticleServerJson.getString("state"))) {
                        searchArticleServer.setState(ServerState.DOWN);
                    } else {
                        logger.error(
                                "设置的ID为{}的Search Article服务器状态为： {}, 该值必须为UP或者DOWN!",
                                searchArticleServerJson.getString("id"),
                                searchArticleServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchArticleServerJson.getString("id")
                                + "的Search Article服务器状态为： "
                                + searchArticleServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    searchArticleServer.setIp(searchArticleServerJson
                            .getString("ip"));
                    searchArticleServer.setPort(searchArticleServerJson
                            .getString("port"));
                    searchArticleServer.setUrl(searchArticleServerJson
                            .getString("url"));
                    searchArticleServer
                            .setServerType(ServerType.SEARCH_ARTICLE);
                    searchArticleServer.setFailConnNum(0);
                    if (searchArticleServerJson.getInt("weight") < 0) {
                        logger.error(
                                "设置的ID为{}的Search Article服务器权值为： {}, 该权值必须大于等于0!",
                                searchArticleServerJson.getString("id"),
                                searchArticleServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchArticleServerJson.getString("id")
                                + "的Search Article服务器权值为： "
                                + searchArticleServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    searchArticleServer.setWeight(searchArticleServerJson
                            .getInt("weight"));
                    searchArticleServer.setAbsoluteWeight(0);
                    SearchServerStatistics searchArticleServerStatistics = new SearchServerStatistics();
                    searchArticleServer
                            .setSearchServerStatistics(searchArticleServerStatistics);
                    searchArticleServers.add(searchArticleServer);
                }
            }
            schedulerConfiguration
                    .setSearchArticleServers(searchArticleServers);
            if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                Map<Integer, SearchServer> searchArticleWeightTable = new TreeMap<Integer, SearchServer>();
                int currentSearchArticleWeight = 0;
                for (SearchServer searchArticleServer : schedulerConfiguration
                        .getSearchArticleServers()) {
                    if (ServerState.UP.equals(searchArticleServer.getState())
                            && searchArticleServer.getWeight() > 0) {
                        currentSearchArticleWeight += searchArticleServer
                                .getWeight();
                        searchArticleWeightTable
                                .put(currentSearchArticleWeight,
                                        searchArticleServer);
                        searchArticleServer
                                .setAbsoluteWeight(currentSearchArticleWeight);
                    }
                }
                schedulerConfiguration
                        .setSearchArticleTotalWeight(currentSearchArticleWeight);
                schedulerConfiguration
                        .setSearchArticleWeightTable(searchArticleWeightTable);
            } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<SearchServer> searchArticleServersPollingList = new ArrayList<SearchServer>();
                for (SearchServer searchArticleServer : schedulerConfiguration
                        .getSearchArticleServers()) {
                    if (ServerState.UP.equals(searchArticleServer.getState())
                            && searchArticleServer.getWeight() > 0) {
                        int weight = searchArticleServer.getWeight()
                                / DIVISOR_10;
                        while (weight-- > 0) {
                            searchArticleServersPollingList
                                    .add(searchArticleServer);
                        }
                    }
                }
                if (searchArticleServersPollingList.size() > 0) {
                    schedulerConfiguration
                            .setCurrentPollingSearchArticleServerIndex(r
                                    .nextInt(searchArticleServersPollingList
                                            .size()));
                    schedulerConfiguration
                            .setSearchArticleServersPollingList(searchArticleServersPollingList);
                    schedulerConfiguration
                            .setSearchArticleServersPollingListSize(searchArticleServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration
                            .setCurrentPollingSearchArticleServerIndex(-1);
                }
            }
        } else if (schedulerConfiguration.getSearchArticleServers() != null) {
            schedulerConfiguration.getSearchArticleServers().clear();
            schedulerConfiguration.setSearchArticleServers(null);
            schedulerConfiguration.getSearchArticleServersPollingList().clear();
            schedulerConfiguration.setSearchArticleServersPollingList(null);
            schedulerConfiguration
                    .setCurrentPollingSearchArticleServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old search article servers:\n"
                + oldSearchArticleServers
                + "\n\n new search article servers:\n" + configData.toString()
                + "\n\n\n");
        oldSearchArticleServers = configData.toString();
    }

    private void configNlpServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取nlp_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/nlp_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config nlp_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config nlp_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config nlp_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config nlp_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old nlp servers: {}", oldNlpServers);
        logger.info("new nlp servers: {}", configData.toString());

        List<NlpServer> nlpServers;
        JSONArray nlpServersArrary = configData.getJSONArray("nlpServers");
        if (nlpServersArrary.size() > 0) {
            nlpServers = new ArrayList<NlpServer>();
            for (int i = 0; i < nlpServersArrary.size(); i++) {
                JSONObject nlpServerJson = (JSONObject) nlpServersArrary.get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getNlpServers() != null
                        && schedulerConfiguration.getNlpServers().size() > 0) {
                    String id = nlpServerJson.getString("id");
                    String ip = nlpServerJson.getString("ip");
                    String port = nlpServerJson.getString("port");
                    String url = nlpServerJson.getString("url");
                    for (NlpServer nlpServer : schedulerConfiguration
                            .getNlpServers()) {
                        if (id.equals(nlpServer.getId())
                                && ip.equals(nlpServer.getIp())
                                && port.equals(nlpServer.getPort())
                                && url.equals(nlpServer.getUrl())) {
                            if (nlpServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的Nlp服务器权值为： {}, 该权值必须大于等于0!",
                                        nlpServer.getId(),
                                        nlpServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + nlpServer.getId()
                                        + "的Nlp服务器权值为： "
                                        + nlpServerJson.getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            nlpServer.setWeight(nlpServerJson.getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    nlpServerJson.getString("state"))) {
                                if (ServerState.DOWN.equals(nlpServer
                                        .getState())) {
                                    nlpServer
                                            .setMarks(SchedulerConstants.MARKS_100);
                                }
                                nlpServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    nlpServerJson.getString("state"))) {
                                nlpServer.setState(ServerState.DOWN);
                                nlpServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的Nlp服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        nlpServer.getId(),
                                        nlpServerJson.getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + nlpServer.getId()
                                        + "的Nlp服务器状态为： "
                                        + nlpServerJson.getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            nlpServers.add(nlpServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    NlpServer nlpServer = new NlpServer();
                    nlpServer.setId(nlpServerJson.getString("id"));
                    if (ServerState.UP.toString().equals(
                            nlpServerJson.getString("state"))) {
                        nlpServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            nlpServerJson.getString("state"))) {
                        nlpServer.setState(ServerState.DOWN);
                    } else {
                        logger.error("设置的ID为{}的Nlp服务器状态为： {}, 该值必须为UP或者DOWN!",
                                nlpServerJson.getString("id"),
                                nlpServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + nlpServerJson.getString("id")
                                + "的Nlp服务器状态为： "
                                + nlpServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    nlpServer.setIp(nlpServerJson.getString("ip"));
                    nlpServer.setPort(nlpServerJson.getString("port"));
                    nlpServer.setUrl(nlpServerJson.getString("url"));
                    nlpServer.setServerType(ServerType.NLP);
                    nlpServer.setFailConnNum(0);
                    if (nlpServerJson.getInt("weight") < 0) {
                        logger.error("设置的ID为{}的Nlp服务器权值为： {}, 该权值必须大于等于0!",
                                nlpServerJson.getString("id"),
                                nlpServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + nlpServerJson.getString("id")
                                + "的Nlp服务器权值为： "
                                + nlpServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    nlpServer.setWeight(nlpServerJson.getInt("weight"));
                    nlpServer.setAbsoluteWeight(0);
                    NlpServerStatistics nlpServerStatistics = new NlpServerStatistics();
                    nlpServer.setNlpServerStatistics(nlpServerStatistics);
                    nlpServers.add(nlpServer);
                }
            }
            schedulerConfiguration.setNlpServers(nlpServers);
            if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                Map<Integer, NlpServer> nlpWeightTable = new TreeMap<Integer, NlpServer>();
                int currentNlpWeight = 0;
                for (NlpServer nlpServer : schedulerConfiguration
                        .getNlpServers()) {
                    if (ServerState.UP.equals(nlpServer.getState())
                            && nlpServer.getWeight() > 0) {
                        currentNlpWeight += nlpServer.getWeight();
                        nlpWeightTable.put(currentNlpWeight, nlpServer);
                        nlpServer.setAbsoluteWeight(currentNlpWeight);
                    }
                }
                schedulerConfiguration.setNlpTotalWeight(currentNlpWeight);
                schedulerConfiguration.setNlpWeightTable(nlpWeightTable);
            } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<NlpServer> nlpServersPollingList = new ArrayList<NlpServer>();
                for (NlpServer nlpServer : schedulerConfiguration
                        .getNlpServers()) {
                    if (ServerState.UP.equals(nlpServer.getState())
                            && nlpServer.getWeight() > 0) {
                        int weight = nlpServer.getWeight() / DIVISOR_10;
                        while (weight-- > 0) {
                            nlpServersPollingList.add(nlpServer);
                        }
                    }
                }
                if (nlpServersPollingList.size() > 0) {
                    schedulerConfiguration.setCurrentPollingNlpServerIndex(r
                            .nextInt(nlpServersPollingList.size()));
                    schedulerConfiguration
                            .setNlpServersPollingList(nlpServersPollingList);
                    schedulerConfiguration
                            .setNlpServersPollingListSize(nlpServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration.setCurrentPollingNlpServerIndex(-1);
                }
            }
        } else if (schedulerConfiguration.getNlpServers() != null) {
            schedulerConfiguration.getNlpServers().clear();
            schedulerConfiguration.setNlpServers(null);
            schedulerConfiguration.getNlpServersPollingList().clear();
            schedulerConfiguration.setNlpServersPollingList(null);
            schedulerConfiguration.setCurrentPollingNlpServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old nlp servers:\n"
                + oldNlpServers + "\n\n new nlp servers:\n"
                + configData.toString() + "\n\n\n");
        oldNlpServers = configData.toString();
    }

    private void configBiServers(SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取bi_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/bi_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config bi_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config bi_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config bi_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config bi_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old bi servers: {}", oldBiServers);
        logger.info("new bi servers: {]", configData.toString());

        List<BIServer> biServers;
        JSONArray biServersArrary = configData.getJSONArray("biServers");
        if (biServersArrary.size() > 0) {
            biServers = new ArrayList<BIServer>();
            for (int i = 0; i < biServersArrary.size(); i++) {
                JSONObject biServerJson = (JSONObject) biServersArrary.get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getBiServers() != null
                        && schedulerConfiguration.getBiServers().size() > 0) {
                    String id = biServerJson.getString("id");
                    String ip = biServerJson.getString("ip");
                    String port = biServerJson.getString("port");
                    String url = biServerJson.getString("url");
                    for (BIServer biServer : schedulerConfiguration
                            .getBiServers()) {
                        if (id.equals(biServer.getId())
                                && ip.equals(biServer.getIp())
                                && port.equals(biServer.getPort())
                                && url.equals(biServer.getUrl())) {
                            if (biServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的BI服务器权值为： {}, 该权值必须大于等于0!",
                                        biServer.getId(),
                                        biServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + biServer.getId()
                                        + "的BI服务器权值为： "
                                        + biServerJson.getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            biServer.setWeight(biServerJson.getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    biServerJson.getString("state"))) {
                                if (ServerState.DOWN
                                        .equals(biServer.getState())) {
                                    biServer.setMarks(SchedulerConstants.MARKS_100);
                                }
                                biServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    biServerJson.getString("state"))) {
                                biServer.setState(ServerState.DOWN);
                                biServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的BI服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        biServer.getId(),
                                        biServerJson.getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + biServer.getId()
                                        + "的BI服务器状态为： "
                                        + biServerJson.getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            biServers.add(biServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    BIServer biServer = new BIServer();
                    biServer.setId(biServerJson.getString("id"));
                    if (ServerState.UP.toString().equals(
                            biServerJson.getString("state"))) {
                        biServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            biServerJson.getString("state"))) {
                        biServer.setState(ServerState.DOWN);
                    } else {
                        logger.error("设置的ID为{}的BI服务器状态为： {}, 该值必须为UP或者DOWN!",
                                biServerJson.getString("id"),
                                biServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + biServerJson.getString("id") + "的BI服务器状态为： "
                                + biServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    biServer.setIp(biServerJson.getString("ip"));
                    biServer.setPort(biServerJson.getString("port"));
                    biServer.setUrl(biServerJson.getString("url"));
                    biServer.setServerType(ServerType.BI);
                    biServer.setFailConnNum(0);
                    if (biServerJson.getInt("weight") < 0) {
                        logger.error("设置的ID为{}的BI服务器权值为： {}, 该权值必须大于等于0!",
                                biServerJson.getString("id"),
                                biServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + biServerJson.getString("id") + "的BI服务器权值为： "
                                + biServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    biServer.setWeight(biServerJson.getInt("weight"));
                    biServer.setAbsoluteWeight(0);
                    BIServerStatistics biServerStatistics = new BIServerStatistics();
                    biServer.setBiServerStatistics(biServerStatistics);
                    biServers.add(biServer);
                }
            }
            schedulerConfiguration.setBiServers(biServers);
        } else if (schedulerConfiguration.getBiServers() != null) {
            schedulerConfiguration.getBiServers().clear();
            schedulerConfiguration.setBiServers(null);
        }

        emailParam.setMsg(emailParam.getMsg() + "old bi servers:\n"
                + oldBiServers + "\n\n new bi servers:\n"
                + configData.toString() + "\n\n\n");
        oldBiServers = configData.toString();
    }

    private void configTimeoutData(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取timeout_data文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/timeout_data");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config timeout_data IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config timeout_data IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config timeout_data JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config timeout_data JE!\n");
                    return;
                }
            }
        }
        logger.info("old timeout data: {}", oldTimeoutData);
        logger.info("new timeout data: {}", configData.toString());

        TimeoutConfiguration timeoutConfiguration = new TimeoutConfiguration();

        // config connect timeout
        int connectTimeout = configData.getInt("connectTimeout");
        if (connectTimeout < 0) {
            logger.error("设置的值为： {}, 设置的connect timeout必须大于等于0!",
                    connectTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("connectTimeout设置的值为： " + connectTimeout
                    + ", 设置的connect timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setConnectTimeout(connectTimeout);
        }

        // ocr timeout
        int ocrTimeout = configData.getInt("ocrTimeout");
        if (ocrTimeout < 0) {
            logger.error("设置的值为： {}, 设置的ocr timeout必须大于等于0!", ocrTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("ocrTimeout设置的值为： " + ocrTimeout
                    + ", 设置的ocr timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setOcrTimeout(ocrTimeout);
        }

        // search timeout
        int searchTimeout = configData.getInt("searchTimeout");
        if (searchTimeout < 0) {
            logger.error("设置的值为： {}, 设置的search timeout必须大于等于0!", searchTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("searchTimeout设置的值为： " + searchTimeout
                    + ", 设置的search timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setSearchTimeout(searchTimeout);
        }

        // search by id timeout
        int searchByIdTimeout = configData.getInt("searchByIdTimeout");
        if (searchByIdTimeout < 0) {
            logger.error("设置的值为： {}, 设置的search by id timeout必须大于等于0!",
                    searchByIdTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("searchByIdTimeout设置的值为： " + searchByIdTimeout
                    + ", 设置的search by id timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setSearchByIdTimeout(searchByIdTimeout);
        }

        // search homework timeout
        int searchHomeworkTimeout = configData.getInt("searchHomeworkTimeout");
        if (searchHomeworkTimeout < 0) {
            logger.error("设置的值为： {}, 设置的search homework timeout必须大于等于0!",
                    searchHomeworkTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("searchHomeworkTimeout设置的值为： "
                    + searchHomeworkTimeout
                    + ", 设置的search homework timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration
                    .setSearchHomeworkTimeout(searchHomeworkTimeout);
        }

        // search article timeout
        int searchArticleTimeout = configData.getInt("searchArticleTimeout");
        if (searchArticleTimeout < 0) {
            logger.error("设置的值为： {}, 设置的search article timeout必须大于等于0!",
                    searchArticleTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("searchArticleTimeout设置的值为： "
                    + searchArticleTimeout
                    + ", 设置的search article timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setSearchArticleTimeout(searchArticleTimeout);
        }

        // nlp timeout
        int nlpTimeout = configData.getInt("nlpTimeout");
        if (nlpTimeout < 0) {
            logger.error("设置的值为： {}, 设置的nlp timeout必须大于等于0!", nlpTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("nlpTimeout设置的值为： " + nlpTimeout
                    + ", 设置的nlp timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setNlpTimeout(nlpTimeout);
        }

        // bi timeout
        int biTimeout = configData.getInt("biTimeout");
        if (biTimeout < 0) {
            logger.error("设置的值为： {}, 设置的bi timeout必须大于等于0!", biTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("biTimeout设置的值为： " + biTimeout
                    + ", 设置的bi timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setBiTimeout(biTimeout);
        }

        // query timeout
        int queryTimeout = configData.getInt("queryTimeout");
        if (queryTimeout < 0) {
            logger.error("设置的值为： {}, 设置的query timeout必须大于等于0!", queryTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("queryTimeout设置的值为： " + queryTimeout
                    + ", 设置的query timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setQueryTimeout(queryTimeout);
        }

        // em timeout
        int emTimeout = configData.getInt("emTimeout");
        if (emTimeout < 0) {
            logger.error("设置的值为： {}, 设置的em timeout必须大于等于0!", emTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("emTimeout设置的值为： " + emTimeout
                    + ", 设置的em timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setEmTimeout(emTimeout);
        }

        // hbase timeout
        int hbaseTimeout = configData.getInt("hbaseTimeout");
        if (hbaseTimeout < 0) {
            logger.error("设置的值为： {}, 设置的hbase timeout必须大于等于0!", hbaseTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("hbaseTimeout设置的值为： " + hbaseTimeout
                    + ", 设置的hbase timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setHbaseTimeout(hbaseTimeout);
        }

        // hbase monitor time
        int hbaseMonitorTime = configData.getInt("hbaseMonitorTime");
        if (hbaseMonitorTime < 0) {
            logger.error("设置的值为： {}, 设置的hbase monitor time必须大于等于0!",
                    hbaseMonitorTime);
            emailParam.setSuccess(false);
            emailParam.setMsg("hbaseMonitorTime设置的值为： " + hbaseMonitorTime
                    + ", 设置的hbase monitor time必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setHbaseMonitorTime(hbaseMonitorTime);
        }

        // ie timeout
        int ieTimeout = configData.getInt("ieTimeout");
        if (ieTimeout < 0) {
            logger.error("设置的值为： {}, 设置的ie timeout必须大于等于0!", ieTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("ieTimeout设置的值为： " + ieTimeout
                    + ", 设置的ie timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setIeTimeout(ieTimeout);
        }

        // ugc common timeout
        int ugcCommonTimeout = configData.getInt("ugcCommonTimeout");
        if (ugcCommonTimeout < 0) {
            logger.error("设置的值为： {}, 设置的ugc common timeout必须大于等于0!",
                    ugcCommonTimeout);
            emailParam.setSuccess(false);
            emailParam.setMsg("ugcCommonTimeout设置的值为： " + ugcCommonTimeout
                    + ", 设置的ugc common timeout必须大于等于0!\n");
            return;
        } else {
            timeoutConfiguration.setUgcCommonTimeout(ugcCommonTimeout);
        }

        schedulerConfiguration.setTimeoutConfiguration(timeoutConfiguration);

        emailParam.setMsg(emailParam.getMsg() + "old timeout data:\n"
                + oldTimeoutData + "\n\n new timeout data:\n"
                + configData.toString() + "\n\n\n");
        oldTimeoutData = configData.toString();
    }

    private void configEmServers(SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取em_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/em_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config em_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config em_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config em_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config em_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old em servers: {}", oldEmServers);
        logger.info("new em servers: {}", configData.toString());

        List<EmServer> emServers;
        JSONArray emServersArrary = configData.getJSONArray("emServers");
        if (emServersArrary.size() > 0) {
            emServers = new ArrayList<EmServer>();
            for (int i = 0; i < emServersArrary.size(); i++) {
                JSONObject emServerJson = (JSONObject) emServersArrary.get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getEmServers() != null
                        && schedulerConfiguration.getEmServers().size() > 0) {
                    String id = emServerJson.getString("id");
                    String ip = emServerJson.getString("ip");
                    String port = emServerJson.getString("port");
                    String url = emServerJson.getString("url");
                    for (EmServer emServer : schedulerConfiguration
                            .getEmServers()) {
                        if (id.equals(emServer.getId())
                                && ip.equals(emServer.getIp())
                                && port.equals(emServer.getPort())
                                && url.equals(emServer.getUrl())) {
                            if (emServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的EM服务器权值为： {}, 该权值必须大于等于0!",
                                        emServer.getId(),
                                        emServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + emServer.getId()
                                        + "的EM服务器权值为： "
                                        + emServerJson.getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            emServer.setWeight(emServerJson.getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    emServerJson.getString("state"))) {
                                if (ServerState.DOWN
                                        .equals(emServer.getState())) {
                                    emServer.setMarks(SchedulerConstants.MARKS_100);
                                }
                                emServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    emServerJson.getString("state"))) {
                                emServer.setState(ServerState.DOWN);
                                emServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的EM服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        emServer.getId(),
                                        emServerJson.getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + emServer.getId()
                                        + "的EM服务器状态为： "
                                        + emServerJson.getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            emServers.add(emServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    EmServer emServer = new EmServer();
                    emServer.setId(emServerJson.getString("id"));
                    if (ServerState.UP.toString().equals(
                            emServerJson.getString("state"))) {
                        emServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            emServerJson.getString("state"))) {
                        emServer.setState(ServerState.DOWN);
                    } else {
                        logger.error("设置的ID为{}的EM服务器状态为： {}, 该值必须为UP或者DOWN!",
                                emServerJson.getString("id"),
                                emServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + emServerJson.getString("id") + "的EM服务器状态为： "
                                + emServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    emServer.setIp(emServerJson.getString("ip"));
                    emServer.setPort(emServerJson.getString("port"));
                    emServer.setUrl(emServerJson.getString("url"));
                    emServer.setServerType(ServerType.EM);
                    emServer.setFailConnNum(0);
                    if (emServerJson.getInt("weight") < 0) {
                        logger.error("设置的ID为{}的EM服务器权值为： {}, 该权值必须大于等于0!",
                                emServerJson.getString("id"),
                                emServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + emServerJson.getString("id") + "的EM服务器权值为： "
                                + emServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    emServer.setWeight(emServerJson.getInt("weight"));
                    emServer.setAbsoluteWeight(0);
                    EmServerStatistics emServerStatistics = new EmServerStatistics();
                    emServer.setEmServerStatistics(emServerStatistics);
                    emServers.add(emServer);
                }
            }
            schedulerConfiguration.setEmServers(emServers);
            if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                Map<Integer, EmServer> emWeightTable = new TreeMap<Integer, EmServer>();
                int currentEmWeight = 0;
                for (EmServer emServer : schedulerConfiguration.getEmServers()) {
                    if (ServerState.UP.equals(emServer.getState())
                            && emServer.getWeight() > 0) {
                        currentEmWeight += emServer.getWeight();
                        emWeightTable.put(currentEmWeight, emServer);
                        emServer.setAbsoluteWeight(currentEmWeight);
                    }
                }
                schedulerConfiguration.setEmTotalWeight(currentEmWeight);
            } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<EmServer> emServersPollingList = new ArrayList<EmServer>();
                for (EmServer emServer : schedulerConfiguration.getEmServers()) {
                    if (ServerState.UP.equals(emServer.getState())
                            && emServer.getWeight() > 0) {
                        int weight = emServer.getWeight() / DIVISOR_10;
                        while (weight-- > 0) {
                            emServersPollingList.add(emServer);
                        }
                    }
                }
                if (emServersPollingList.size() > 0) {
                    schedulerConfiguration.setCurrentPollingEmServerIndex(r
                            .nextInt(emServersPollingList.size()));
                    schedulerConfiguration
                            .setEmServersPollingList(emServersPollingList);
                    schedulerConfiguration
                            .setEmServersPollingListSize(emServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration.setCurrentPollingEmServerIndex(-1);
                }
            }
        } else if (schedulerConfiguration.getEmServers() != null) {
            schedulerConfiguration.getEmServers().clear();
            schedulerConfiguration.setEmServers(null);
            schedulerConfiguration.getEmServersPollingList().clear();
            schedulerConfiguration.setEmServersPollingList(null);
            schedulerConfiguration.setCurrentPollingEmServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old em servers:\n"
                + oldEmServers + "\n\n new em servers:\n"
                + configData.toString() + "\n\n\n");
        oldEmServers = configData.toString();
    }

    private void configHbaseConfig(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取hbase_config文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/hbase_config");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config hbase_config IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config hbase_config IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config hbase_config JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config hbase_config JE!\n");
                    return;
                }
            }
        }
        logger.info("old hbase config: {}", oldHbaseConfig);
        logger.info("new hbase config: {}", configData.toString());

        // config hbase config
        Configuration hbaseConfig = new Configuration();
        if (configData.size() > 0) {
            for (Object attr : configData.entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) attr;
                String attrname = (String) entry.getKey();
                String value = (String) entry.getValue();
                logger.info("hbase config: {}, {}", attrname, value);
                hbaseConfig.set(attrname, value);
            }
        }
        schedulerConfiguration.setHbaseConfig(hbaseConfig);

        HbClient hbClient = new HbClient(hbaseConfig);
        schedulerConfiguration.setHbClient(hbClient);

        emailParam.setMsg(emailParam.getMsg() + "old hbase config:\n"
                + oldHbaseConfig + "\n\n new hbase config:\n"
                + configData.toString() + "\n\n\n");
        oldHbaseConfig = configData.toString();
    }

    private void configHbaseConfigBk(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取hbase_config_bk文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/hbase_config_bk");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config hbase_config_bk IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config hbase_config_bk IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config hbase_config_bk JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config hbase_config_bk JE!\n");
                    return;
                }
            }
        }
        logger.info("old hbase config bk: {}", oldHbaseConfigBk);
        logger.info("new hbase config bk: {}", configData.toString());

        // config hbase config backup
        Configuration hbaseConfigBk = new Configuration();
        if (configData.size() > 0) {
            for (Object attr : configData.entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) attr;
                String attrname = (String) entry.getKey();
                String value = (String) entry.getValue();
                logger.info("hbase config bk: {}, {}", attrname, value);
                hbaseConfigBk.set(attrname, value);
            }
        }
        schedulerConfiguration.setHbaseConfigBk(hbaseConfigBk);

        HbClient hbClientBk = new HbClient(hbaseConfigBk);
        schedulerConfiguration.setHbClientBk(hbClientBk);

        emailParam.setMsg(emailParam.getMsg() + "old hbase config bk:\n"
                + oldHbaseConfigBk + "\n\n new hbase config bk:\n"
                + configData.toString() + "\n\n\n");
        oldHbaseConfigBk = configData.toString();
    }

    private void configSearchByIdServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取search_by_id_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/search_by_id_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config search_by_id_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config search_by_id_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config search_by_id_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config search_by_id_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old search by id servers: {}", oldSearchByIdServers);
        logger.info("new search by id servers: {}", configData.toString());

        List<SearchServer> searchByIdServers;
        JSONArray searchByIdServersArrary = configData
                .getJSONArray("searchByIdServers");
        if (searchByIdServersArrary.size() > 0) {
            searchByIdServers = new ArrayList<SearchServer>();
            for (int i = 0; i < searchByIdServersArrary.size(); i++) {
                JSONObject searchByIdServerJson = (JSONObject) searchByIdServersArrary
                        .get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getSearchByIdServers() != null
                        && schedulerConfiguration.getSearchByIdServers().size() > 0) {
                    String id = searchByIdServerJson.getString("id");
                    String ip = searchByIdServerJson.getString("ip");
                    String port = searchByIdServerJson.getString("port");
                    String url = searchByIdServerJson.getString("url");
                    for (SearchServer searchByIdServer : schedulerConfiguration
                            .getSearchByIdServers()) {
                        if (id.equals(searchByIdServer.getId())
                                && ip.equals(searchByIdServer.getIp())
                                && port.equals(searchByIdServer.getPort())
                                && url.equals(searchByIdServer.getUrl())) {
                            if (searchByIdServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的Search By Id服务器权值为： {}, 该权值必须大于等于0!",
                                        searchByIdServer.getId(),
                                        searchByIdServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchByIdServer.getId()
                                        + "的Search By Id服务器权值为： "
                                        + searchByIdServerJson.getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            searchByIdServer.setWeight(searchByIdServerJson
                                    .getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    searchByIdServerJson.getString("state"))) {
                                if (ServerState.DOWN.equals(searchByIdServer
                                        .getState())) {
                                    searchByIdServer
                                            .setMarks(SchedulerConstants.MARKS_100);
                                }
                                searchByIdServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    searchByIdServerJson.getString("state"))) {
                                searchByIdServer.setState(ServerState.DOWN);
                                searchByIdServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的Search By Id服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        searchByIdServer.getId(),
                                        searchByIdServerJson.getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchByIdServer.getId()
                                        + "的Search By Id服务器状态为： "
                                        + searchByIdServerJson
                                                .getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            searchByIdServers.add(searchByIdServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    SearchServer searchByIdServer = new SearchServer();
                    searchByIdServer
                            .setId(searchByIdServerJson.getString("id"));
                    if (ServerState.UP.toString().equals(
                            searchByIdServerJson.getString("state"))) {
                        searchByIdServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            searchByIdServerJson.getString("state"))) {
                        searchByIdServer.setState(ServerState.DOWN);
                    } else {
                        logger.error(
                                "设置的ID为{}的Search By Id服务器状态为： {}, 该值必须为UP或者DOWN!",
                                searchByIdServerJson.getString("id"),
                                searchByIdServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchByIdServerJson.getString("id")
                                + "的Search By Id服务器状态为： "
                                + searchByIdServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    searchByIdServer
                            .setIp(searchByIdServerJson.getString("ip"));
                    searchByIdServer.setPort(searchByIdServerJson
                            .getString("port"));
                    searchByIdServer.setUrl(searchByIdServerJson
                            .getString("url"));
                    searchByIdServer.setServerType(ServerType.SEARCH_BY_ID);
                    searchByIdServer.setFailConnNum(0);
                    if (searchByIdServerJson.getInt("weight") < 0) {
                        logger.error(
                                "设置的ID为{}的Search By Id服务器权值为： {}, 该权值必须大于等于0!",
                                searchByIdServerJson.getString("id"),
                                searchByIdServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchByIdServerJson.getString("id")
                                + "的Search By Id服务器权值为： "
                                + searchByIdServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    searchByIdServer.setWeight(searchByIdServerJson
                            .getInt("weight"));
                    searchByIdServer.setAbsoluteWeight(0);
                    SearchServerStatistics searchByIdServerStatistics = new SearchServerStatistics();
                    searchByIdServer
                            .setSearchServerStatistics(searchByIdServerStatistics);
                    searchByIdServers.add(searchByIdServer);
                }
            }
            schedulerConfiguration.setSearchByIdServers(searchByIdServers);
            if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                Map<Integer, SearchServer> searchByIdWeightTable = new TreeMap<Integer, SearchServer>();
                int currentSearchByIdWeight = 0;
                for (SearchServer searchByIdServer : schedulerConfiguration
                        .getSearchByIdServers()) {
                    if (ServerState.UP.equals(searchByIdServer.getState())
                            && searchByIdServer.getWeight() > 0) {
                        currentSearchByIdWeight += searchByIdServer.getWeight();
                        searchByIdWeightTable.put(currentSearchByIdWeight,
                                searchByIdServer);
                        searchByIdServer
                                .setAbsoluteWeight(currentSearchByIdWeight);
                    }
                }
                schedulerConfiguration
                        .setSearchByIdTotalWeight(currentSearchByIdWeight);
                schedulerConfiguration
                        .setSearchByIdWeightTable(searchByIdWeightTable);
            } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<SearchServer> searchByIdServersPollingList = new ArrayList<SearchServer>();
                for (SearchServer searchByIdServer : schedulerConfiguration
                        .getSearchByIdServers()) {
                    if (ServerState.UP.equals(searchByIdServer.getState())
                            && searchByIdServer.getWeight() > 0) {
                        int weight = searchByIdServer.getWeight() / DIVISOR_10;
                        while (weight-- > 0) {
                            searchByIdServersPollingList.add(searchByIdServer);
                        }
                    }
                }
                if (searchByIdServersPollingList.size() > 0) {
                    schedulerConfiguration
                            .setCurrentPollingSearchByIdServerIndex(r
                                    .nextInt(searchByIdServersPollingList
                                            .size()));
                    schedulerConfiguration
                            .setSearchByIdServersPollingList(searchByIdServersPollingList);
                    schedulerConfiguration
                            .setSearchByIdServersPollingListSize(searchByIdServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration
                            .setCurrentPollingSearchByIdServerIndex(-1);
                }
            }
        } else if (schedulerConfiguration.getSearchByIdServers() != null) {
            schedulerConfiguration.getSearchByIdServers().clear();
            schedulerConfiguration.setSearchByIdServers(null);
            schedulerConfiguration.getSearchByIdServersPollingList().clear();
            schedulerConfiguration.setSearchByIdServersPollingList(null);
            schedulerConfiguration.setCurrentPollingSearchByIdServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old search by id servers:\n"
                + oldSearchByIdServers + "\n\n new search by id servers:\n"
                + configData.toString() + "\n\n\n");
        oldSearchByIdServers = configData.toString();
    }

    private void configIeServers(SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取ie_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/ie_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config ie_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config ie_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config ie_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config ie_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old ie servers: {}", oldIeServers);
        logger.info("new ie servers: {}", configData.toString());

        List<IeServer> ieServers;
        JSONArray ieServersArrary = configData.getJSONArray("ieServers");
        if (ieServersArrary.size() > 0) {
            ieServers = new ArrayList<IeServer>();
            for (int i = 0; i < ieServersArrary.size(); i++) {
                JSONObject ieServerJson = (JSONObject) ieServersArrary.get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getIeServers() != null
                        && schedulerConfiguration.getIeServers().size() > 0) {
                    String id = ieServerJson.getString("id");
                    String ip = ieServerJson.getString("ip");
                    String port = ieServerJson.getString("port");
                    String url = ieServerJson.getString("url");
                    for (IeServer ieServer : schedulerConfiguration
                            .getIeServers()) {
                        if (id.equals(ieServer.getId())
                                && ip.equals(ieServer.getIp())
                                && port.equals(ieServer.getPort())
                                && url.equals(ieServer.getUrl())) {
                            if (ieServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的IE服务器权值为： {}, 该权值必须大于等于0!",
                                        ieServer.getId(),
                                        ieServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + ieServer.getId()
                                        + "的IE服务器权值为： "
                                        + ieServerJson.getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            ieServer.setWeight(ieServerJson.getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    ieServerJson.getString("state"))) {
                                if (ServerState.DOWN
                                        .equals(ieServer.getState())) {
                                    ieServer.setMarks(SchedulerConstants.MARKS_100);
                                }
                                ieServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    ieServerJson.getString("state"))) {
                                ieServer.setState(ServerState.DOWN);
                                ieServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的IE服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        ieServer.getId(),
                                        ieServerJson.getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为" + ieServer.getId()
                                        + "的IE服务器状态为： "
                                        + ieServerJson.getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            ieServers.add(ieServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    IeServer ieServer = new IeServer();
                    ieServer.setId(ieServerJson.getString("id"));
                    if (ServerState.UP.toString().equals(
                            ieServerJson.getString("state"))) {
                        ieServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            ieServerJson.getString("state"))) {
                        ieServer.setState(ServerState.DOWN);
                    } else {
                        logger.error("设置的ID为{}的IE服务器状态为： {}, 该值必须为UP或者DOWN!",
                                ieServerJson.getString("id"),
                                ieServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + ieServerJson.getString("id") + "的IE服务器状态为： "
                                + ieServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    ieServer.setIp(ieServerJson.getString("ip"));
                    ieServer.setPort(ieServerJson.getString("port"));
                    ieServer.setUrl(ieServerJson.getString("url"));
                    ieServer.setServerType(ServerType.IE);
                    ieServer.setFailConnNum(0);
                    if (ieServerJson.getInt("weight") < 0) {
                        logger.error("设置的ID为{}的IE服务器权值为： {}, 该权值必须大于等于0!",
                                ieServerJson.getString("id"),
                                ieServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + ieServerJson.getString("id") + "的IE服务器权值为： "
                                + ieServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    ieServer.setWeight(ieServerJson.getInt("weight"));
                    ieServer.setAbsoluteWeight(0);
                    IeServerStatistics ieServerStatistics = new IeServerStatistics();
                    ieServer.setIeServerStatistics(ieServerStatistics);
                    ieServers.add(ieServer);
                }
            }
            schedulerConfiguration.setIeServers(ieServers);
            if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                Map<Integer, IeServer> ieWeightTable = new TreeMap<Integer, IeServer>();
                int currentIeWeight = 0;
                for (IeServer ieServer : schedulerConfiguration.getIeServers()) {
                    if (ServerState.UP.equals(ieServer.getState())
                            && ieServer.getWeight() > 0) {
                        currentIeWeight += ieServer.getWeight();
                        ieWeightTable.put(currentIeWeight, ieServer);
                        ieServer.setAbsoluteWeight(currentIeWeight);
                    }
                }
                schedulerConfiguration.setIeTotalWeight(currentIeWeight);
            } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<IeServer> ieServersPollingList = new ArrayList<IeServer>();
                for (IeServer ieServer : schedulerConfiguration.getIeServers()) {
                    if (ServerState.UP.equals(ieServer.getState())
                            && ieServer.getWeight() > 0) {
                        int weight = ieServer.getWeight() / DIVISOR_10;
                        while (weight-- > 0) {
                            ieServersPollingList.add(ieServer);
                        }
                    }
                }
                if (ieServersPollingList.size() > 0) {
                    schedulerConfiguration.setCurrentPollingIeServerIndex(r
                            .nextInt(ieServersPollingList.size()));
                    schedulerConfiguration
                            .setIeServersPollingList(ieServersPollingList);
                    schedulerConfiguration
                            .setIeServersPollingListSize(ieServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration.setCurrentPollingIeServerIndex(-1);
                }
            }
        } else if (schedulerConfiguration.getIeServers() != null) {
            schedulerConfiguration.getIeServers().clear();
            schedulerConfiguration.setIeServers(null);
            schedulerConfiguration.getIeServersPollingList().clear();
            schedulerConfiguration.setIeServersPollingList(null);
            schedulerConfiguration.setCurrentPollingIeServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old ie servers:\n"
                + oldIeServers + "\n\n new ie servers:\n"
                + configData.toString() + "\n\n\n");
        oldIeServers = configData.toString();
    }

    private void configSdkSearchServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取sdk_search_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/sdk_search_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config sdk_search_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config sdk_search_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config sdk_search_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config sdk_search_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old sdk search servers: {}", oldSdkSearchServers);
        logger.info("new sdk search servers: {}", configData.toString());

        JSONArray sdkSearchServersRootArrary = configData
                .getJSONArray("sdkSearchServers");
        if (sdkSearchServersRootArrary.size() > 0) {
            // config sdk search servers
            JSONObject sdkSearchServersJson = (JSONObject) sdkSearchServersRootArrary
                    .get(0);
            JSONArray sdkSearchServersArrary = sdkSearchServersJson
                    .getJSONArray("sdk");
            if (sdkSearchServersArrary.size() > 0) {
                List<SearchServer> sdkSearchServers = new ArrayList<SearchServer>();
                for (int i = 0; i < sdkSearchServersArrary.size(); i++) {
                    JSONObject sdkSearchServerJson = (JSONObject) sdkSearchServersArrary
                            .get(i);
                    boolean addNewServer = true;
                    if (schedulerConfiguration.getSdkSearchServers() != null
                            && schedulerConfiguration.getSdkSearchServers()
                                    .size() > 0) {
                        String id = sdkSearchServerJson.getString("id");
                        String ip = sdkSearchServerJson.getString("ip");
                        String port = sdkSearchServerJson.getString("port");
                        String url = sdkSearchServerJson.getString("url");
                        String level = sdkSearchServerJson.getString("level");
                        for (SearchServer sdkSearchServer : schedulerConfiguration
                                .getSdkSearchServers()) {
                            if (id.equals(sdkSearchServer.getId())
                                    && ip.equals(sdkSearchServer.getIp())
                                    && port.equals(sdkSearchServer.getPort())
                                    && url.equals(sdkSearchServer.getUrl())
                                    && level.equals(sdkSearchServer.getLevel())) {
                                if (sdkSearchServerJson.getInt("weight") < 0) {
                                    logger.error(
                                            "设置的ID为{}的Sdk Search服务器权值为： {}, 该权值必须大于等于0!",
                                            sdkSearchServer.getId(),
                                            sdkSearchServerJson
                                                    .getInt("weight"));
                                    emailParam.setSuccess(false);
                                    emailParam.setMsg("设置的ID为"
                                            + sdkSearchServer.getId()
                                            + "的Sdk Search服务器权值为： "
                                            + sdkSearchServerJson
                                                    .getInt("weight")
                                            + ", 该权值必须大于等于0!\n");
                                    return;
                                }
                                sdkSearchServer.setWeight(sdkSearchServerJson
                                        .getInt("weight"));
                                if (ServerState.UP.toString().equals(
                                        sdkSearchServerJson.getString("state"))) {
                                    if (ServerState.DOWN.equals(sdkSearchServer
                                            .getState())) {
                                        sdkSearchServer
                                                .setMarks(SchedulerConstants.MARKS_100);
                                    }
                                    sdkSearchServer.setState(ServerState.UP);
                                } else if (ServerState.DOWN.toString().equals(
                                        sdkSearchServerJson.getString("state"))) {
                                    sdkSearchServer.setState(ServerState.DOWN);
                                    sdkSearchServer.setAbsoluteWeight(0);
                                } else {
                                    logger.error(
                                            "设置的ID为{}的Sdk Search服务器状态为： {}, 该值必须为UP或者DOWN!",
                                            sdkSearchServer.getId(),
                                            sdkSearchServerJson
                                                    .getString("state"));
                                    emailParam.setSuccess(false);
                                    emailParam.setMsg("设置的ID为"
                                            + sdkSearchServer.getId()
                                            + "的Sdk Search服务器状态为： "
                                            + sdkSearchServerJson
                                                    .getString("state")
                                            + ", 该值必须为UP或者DOWN!\n");
                                    return;
                                }
                                sdkSearchServers.add(sdkSearchServer);
                                addNewServer = false;
                                break;
                            }
                        }
                    }
                    if (addNewServer) {
                        SearchServer sdkSearchServer = new SearchServer();
                        sdkSearchServer.setId(sdkSearchServerJson
                                .getString("id"));
                        if (ServerState.UP.toString().equals(
                                sdkSearchServerJson.getString("state"))) {
                            sdkSearchServer.setState(ServerState.UP);
                        } else if (ServerState.DOWN.toString().equals(
                                sdkSearchServerJson.getString("state"))) {
                            sdkSearchServer.setState(ServerState.DOWN);
                        } else {
                            logger.error(
                                    "设置的ID为{}的Sdk Search服务器状态为： {}, 该值必须为UP或者DOWN!",
                                    sdkSearchServerJson.getString("id"),
                                    sdkSearchServerJson.getString("state"));
                            emailParam.setSuccess(false);
                            emailParam.setMsg("设置的ID为"
                                    + sdkSearchServerJson.getString("id")
                                    + "的Sdk Search服务器状态为： "
                                    + sdkSearchServerJson.getString("state")
                                    + ", 该值必须为UP或者DOWN!\n");
                            return;
                        }
                        sdkSearchServer.setIp(sdkSearchServerJson
                                .getString("ip"));
                        sdkSearchServer.setPort(sdkSearchServerJson
                                .getString("port"));
                        sdkSearchServer.setUrl(sdkSearchServerJson
                                .getString("url"));
                        sdkSearchServer.setLevel(sdkSearchServerJson
                                .getString("level"));
                        sdkSearchServer.setServerType(ServerType.SDK_SEARCH);
                        sdkSearchServer.setFailConnNum(0);
                        if (sdkSearchServerJson.getInt("weight") < 0) {
                            logger.error(
                                    "设置的ID为{}的Sdk Search服务器权值为： {}, 该权值必须大于等于0!",
                                    sdkSearchServerJson.getString("id"),
                                    sdkSearchServerJson.getInt("weight"));
                            emailParam.setSuccess(false);
                            emailParam.setMsg("设置的ID为"
                                    + sdkSearchServerJson.getString("id")
                                    + "的Sdk Search服务器权值为： "
                                    + sdkSearchServerJson.getInt("weight")
                                    + ", 该权值必须大于等于0!\n");
                            return;
                        }
                        sdkSearchServer.setWeight(sdkSearchServerJson
                                .getInt("weight"));
                        sdkSearchServer.setAbsoluteWeight(0);
                        SearchServerStatistics sdkSearchServerStatistics = new SearchServerStatistics();
                        sdkSearchServer
                                .setSearchServerStatistics(sdkSearchServerStatistics);
                        sdkSearchServers.add(sdkSearchServer);
                    }
                }
                schedulerConfiguration.setSdkSearchServers(sdkSearchServers);
                if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                        .getSystemDataConfiguration().getSchedulerStrategy())) {
                    List<SearchServer> sdkSearchServersPollingList = new ArrayList<SearchServer>();
                    for (SearchServer sdkSearchServer : schedulerConfiguration
                            .getSdkSearchServers()) {
                        if (ServerState.UP.equals(sdkSearchServer.getState())
                                && sdkSearchServer.getWeight() > 0) {
                            int weight = sdkSearchServer.getWeight()
                                    / DIVISOR_10;
                            while (weight-- > 0) {
                                sdkSearchServersPollingList
                                        .add(sdkSearchServer);
                            }
                        }
                    }
                    if (sdkSearchServersPollingList.size() > 0) {
                        schedulerConfiguration
                                .setCurrentPollingSdkSearchServerIndex(r
                                        .nextInt(sdkSearchServersPollingList
                                                .size()));
                        schedulerConfiguration
                                .setSdkSearchServersPollingList(sdkSearchServersPollingList);
                        schedulerConfiguration
                                .setSdkSearchServersPollingListSize(sdkSearchServersPollingList
                                        .size());
                    } else {
                        schedulerConfiguration
                                .setCurrentPollingSdkSearchServerIndex(-1);
                    }
                }
            } else if (schedulerConfiguration.getSdkSearchServers() != null) {
                schedulerConfiguration.getSdkSearchServers().clear();
                schedulerConfiguration.setSdkSearchServers(null);
                schedulerConfiguration.getSdkSearchServersPollingList().clear();
                schedulerConfiguration.setSdkSearchServersPollingList(null);
                schedulerConfiguration
                        .setCurrentPollingSdkSearchServerIndex(-1);
            }

            // config other sdk search servers
        }

        emailParam.setMsg(emailParam.getMsg() + "old sdk search servers:\n"
                + oldSdkSearchServers + "\n\n new sdk search servers:\n"
                + configData.toString() + "\n\n\n");
        oldSdkSearchServers = configData.toString();
    }

    private void configHandwriteOcrServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取handwrite_ocr_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/handwrite_ocr_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config handwrite_ocr_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config handwrite_ocr_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config handwrite_ocr_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config handwrite_ocr_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old handwrite ocr servers: {}", oldHandwriteOcrServers);
        logger.info("new handwrite ocr servers: {}", configData.toString());

        List<OcrServer> handwriteOcrServers;
        JSONArray handwriteOcrServersArrary = configData
                .getJSONArray("handwriteOcrServers");
        if (handwriteOcrServersArrary.size() > 0) {
            handwriteOcrServers = new ArrayList<OcrServer>();
            for (int i = 0; i < handwriteOcrServersArrary.size(); i++) {
                JSONObject handwriteOcrServerJson = (JSONObject) handwriteOcrServersArrary
                        .get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getHandwriteOcrServers() != null
                        && schedulerConfiguration.getHandwriteOcrServers()
                                .size() > 0) {
                    String id = handwriteOcrServerJson.getString("id");
                    String ip = handwriteOcrServerJson.getString("ip");
                    String port = handwriteOcrServerJson.getString("port");
                    String url = handwriteOcrServerJson.getString("url");
                    for (OcrServer handwriteOcrServer : schedulerConfiguration
                            .getHandwriteOcrServers()) {
                        if (id.equals(handwriteOcrServer.getId())
                                && ip.equals(handwriteOcrServer.getIp())
                                && port.equals(handwriteOcrServer.getPort())
                                && url.equals(handwriteOcrServer.getUrl())) {
                            if (handwriteOcrServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的手写识别服务器权值为： {}, 该权值必须大于等于0!",
                                        handwriteOcrServer.getId(),
                                        handwriteOcrServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + handwriteOcrServer.getId()
                                        + "的手写识别服务器权值为： "
                                        + handwriteOcrServerJson
                                                .getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            handwriteOcrServer.setWeight(handwriteOcrServerJson
                                    .getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    handwriteOcrServerJson.getString("state"))) {
                                if (ServerState.DOWN.equals(handwriteOcrServer
                                        .getState())) {
                                    handwriteOcrServer
                                            .setMarks(SchedulerConstants.MARKS_100);
                                }
                                handwriteOcrServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    handwriteOcrServerJson.getString("state"))) {
                                handwriteOcrServer.setState(ServerState.DOWN);
                                handwriteOcrServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的手写识别服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        handwriteOcrServer.getId(),
                                        handwriteOcrServerJson
                                                .getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + handwriteOcrServer.getId()
                                        + "的手写识别服务器状态为： "
                                        + handwriteOcrServerJson
                                                .getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            handwriteOcrServers.add(handwriteOcrServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    OcrServer handwriteOcrServer = new OcrServer();
                    handwriteOcrServer.setId(handwriteOcrServerJson
                            .getString("id"));
                    if (ServerState.UP.toString().equals(
                            handwriteOcrServerJson.getString("state"))) {
                        handwriteOcrServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            handwriteOcrServerJson.getString("state"))) {
                        handwriteOcrServer.setState(ServerState.DOWN);
                    } else {
                        logger.error("设置的ID为{}的手写识别服务器状态为： {}, 该值必须为UP或者DOWN!",
                                handwriteOcrServerJson.getString("id"),
                                handwriteOcrServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + handwriteOcrServerJson.getString("id")
                                + "的手写识别服务器状态为： "
                                + handwriteOcrServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    handwriteOcrServer.setIp(handwriteOcrServerJson
                            .getString("ip"));
                    handwriteOcrServer.setPort(handwriteOcrServerJson
                            .getString("port"));
                    handwriteOcrServer.setUrl(handwriteOcrServerJson
                            .getString("url"));
                    handwriteOcrServer.setServerType(ServerType.HANDWRITE_OCR);
                    handwriteOcrServer.setFailConnNum(0);
                    if (handwriteOcrServerJson.getInt("weight") < 0) {
                        logger.error("设置的ID为{}的手写识别服务器权值为： {}, 该权值必须大于等于0!",
                                handwriteOcrServerJson.getString("id"),
                                handwriteOcrServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + handwriteOcrServerJson.getString("id")
                                + "的手写识别服务器权值为： "
                                + handwriteOcrServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    handwriteOcrServer.setWeight(handwriteOcrServerJson
                            .getInt("weight"));
                    handwriteOcrServer.setAbsoluteWeight(0);
                    OcrServerStatistics handwriteOcrServerStatistics = new OcrServerStatistics();
                    handwriteOcrServer
                            .setOcrServerStatistics(handwriteOcrServerStatistics);
                    handwriteOcrServers.add(handwriteOcrServer);
                }
            }
            schedulerConfiguration.setHandwriteOcrServers(handwriteOcrServers);
            if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<OcrServer> handwriteOcrServersPollingList = new ArrayList<OcrServer>();
                for (OcrServer handwriteOcrServer : schedulerConfiguration
                        .getHandwriteOcrServers()) {
                    if (ServerState.UP.equals(handwriteOcrServer.getState())
                            && handwriteOcrServer.getWeight() > 0) {
                        int weight = handwriteOcrServer.getWeight()
                                / DIVISOR_10;
                        while (weight-- > 0) {
                            handwriteOcrServersPollingList
                                    .add(handwriteOcrServer);
                        }
                    }
                }
                if (handwriteOcrServersPollingList.size() > 0) {
                    schedulerConfiguration
                            .setCurrentPollingHandwriteOcrServerIndex(r
                                    .nextInt(handwriteOcrServersPollingList
                                            .size()));
                    schedulerConfiguration
                            .setHandwriteOcrServersPollingList(handwriteOcrServersPollingList);
                    schedulerConfiguration
                            .setHandwriteOcrServersPollingListSize(handwriteOcrServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration
                            .setCurrentPollingHandwriteOcrServerIndex(-1);
                }
            }
        } else if (schedulerConfiguration.getHandwriteOcrServers() != null) {
            schedulerConfiguration.getHandwriteOcrServers().clear();
            schedulerConfiguration.setHandwriteOcrServers(null);
            schedulerConfiguration.getHandwriteOcrServersPollingList().clear();
            schedulerConfiguration.setHandwriteOcrServersPollingList(null);
            schedulerConfiguration.setCurrentPollingHandwriteOcrServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old handwrite ocr servers:\n"
                + oldHandwriteOcrServers + "\n\n new handwrite ocr servers:\n"
                + configData.toString() + "\n\n\n");
        oldHandwriteOcrServers = configData.toString();
    }

    private void configUgcConfig(SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取ugc_config文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/ugc_config");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config ugc_config IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config ugc_config IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config ugc_config JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config ugc_config JE!\n");
                    return;
                }
            }
        }
        logger.info("old ugc config: {}", oldUgcConfig);
        logger.info("new ugc config: {}", configData.toString());
        UgcConfigConfiguration ugcConfigConfiguration = schedulerConfiguration
                .getUgcConfigConfiguration() != null ? schedulerConfiguration
                .getUgcConfigConfiguration() : new UgcConfigConfiguration();

        // config region
        String region = configData.getString("region");
        ugcConfigConfiguration.setRegion(region);

        // config expire in
        int expireIn = configData.getInt("expireIn");
        ugcConfigConfiguration.setExpireIn(expireIn);

        // config public key
        String publicKey = configData.getString("publicKey");
        ugcConfigConfiguration.setPublicKey(publicKey);

        // config private key
        String privateKey = configData.getString("privateKey");
        ugcConfigConfiguration.setPrivateKey(privateKey);

        // config common api url
        String commonApiUrl = configData.getString("commonApiUrl");
        ugcConfigConfiguration.setCommonApiUrl(commonApiUrl);

        // config task api url
        String taskApiUrl = configData.getString("taskApiUrl");
        ugcConfigConfiguration.setTaskApiUrl(taskApiUrl);

        // config image name
        String imageName = configData.getString("imageName");
        ugcConfigConfiguration.setImageName(imageName);

        schedulerConfiguration
                .setUgcConfigConfiguration(ugcConfigConfiguration);

        emailParam.setMsg(emailParam.getMsg() + "old ugc config:\n"
                + oldUgcConfig + "\n\n new ugc config:\n"
                + configData.toString() + "\n\n\n");
        oldUgcConfig = configData.toString();
    }

    private void configSearchMatrixServers(
            SchedulerConfiguration schedulerConfiguration,
            ConfigParam configParam, EmailParam emailParam) {
        if (!emailParam.isSuccess()) {
            return;
        }

        JSONObject configData = null;
        if (configParam.getConfigData() != null) {
            // 读取config mail中的配置信息
            configData = configParam.getConfigData();
        } else {
            // 读取search_matrix_servers文件
            String configDataStr = "";
            try {
                String s = null;
                File config = new File("configFile/search_matrix_servers");
                FileReader fr = new FileReader(config);
                BufferedReader br = new BufferedReader(fr);
                while ((s = br.readLine()) != null) {
                    configDataStr += s;
                }
                br.close();
            } catch (IOException e) {
                logger.error("config search_matrix_servers IOE!");
                emailParam.setSuccess(false);
                emailParam.setMsg("config search_matrix_servers IOE!\n");
                return;
            }

            if (!"".equals(configDataStr)) {
                try {
                    configData = JSONObject.fromObject(configDataStr);
                } catch (JSONException e) {
                    logger.error("config search_matrix_servers JE!");
                    emailParam.setSuccess(false);
                    emailParam.setMsg("config search_matrix_servers JE!\n");
                    return;
                }
            }
        }
        logger.info("old search matrix servers: {}", oldSearchMatrixServers);
        logger.info("new search matrix servers: {}", configData.toString());

        List<SearchServer> searchMatrixServers;
        JSONArray searchMatrixServersArrary = configData
                .getJSONArray("searchMatrixServers");
        if (searchMatrixServersArrary.size() > 0) {
            searchMatrixServers = new ArrayList<SearchServer>();
            for (int i = 0; i < searchMatrixServersArrary.size(); i++) {
                JSONObject searchMatrixServerJson = (JSONObject) searchMatrixServersArrary
                        .get(i);
                boolean addNewServer = true;
                if (schedulerConfiguration.getSearchMatrixServers() != null
                        && schedulerConfiguration.getSearchMatrixServers()
                                .size() > 0) {
                    String id = searchMatrixServerJson.getString("id");
                    String ip = searchMatrixServerJson.getString("ip");
                    String port = searchMatrixServerJson.getString("port");
                    String url = searchMatrixServerJson.getString("url");
                    for (SearchServer searchMatrixServer : schedulerConfiguration
                            .getSearchMatrixServers()) {
                        if (id.equals(searchMatrixServer.getId())
                                && ip.equals(searchMatrixServer.getIp())
                                && port.equals(searchMatrixServer.getPort())
                                && url.equals(searchMatrixServer.getUrl())) {
                            if (searchMatrixServerJson.getInt("weight") < 0) {
                                logger.error(
                                        "设置的ID为{}的Search Matrix服务器权值为： {}, 该权值必须大于等于0!",
                                        searchMatrixServer.getId(),
                                        searchMatrixServerJson.getInt("weight"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchMatrixServer.getId()
                                        + "的Search Matrix服务器权值为： "
                                        + searchMatrixServerJson
                                                .getInt("weight")
                                        + ", 该权值必须大于等于0!\n");
                                return;
                            }
                            searchMatrixServer.setWeight(searchMatrixServerJson
                                    .getInt("weight"));
                            if (ServerState.UP.toString().equals(
                                    searchMatrixServerJson.getString("state"))) {
                                if (ServerState.DOWN.equals(searchMatrixServer
                                        .getState())) {
                                    searchMatrixServer
                                            .setMarks(SchedulerConstants.MARKS_100);
                                }
                                searchMatrixServer.setState(ServerState.UP);
                            } else if (ServerState.DOWN.toString().equals(
                                    searchMatrixServerJson.getString("state"))) {
                                searchMatrixServer.setState(ServerState.DOWN);
                                searchMatrixServer.setAbsoluteWeight(0);
                            } else {
                                logger.error(
                                        "设置的ID为{}的Search Matrix服务器状态为： {}, 该值必须为UP或者DOWN!",
                                        searchMatrixServer.getId(),
                                        searchMatrixServerJson
                                                .getString("state"));
                                emailParam.setSuccess(false);
                                emailParam.setMsg("设置的ID为"
                                        + searchMatrixServer.getId()
                                        + "的Search Matrix服务器状态为： "
                                        + searchMatrixServerJson
                                                .getString("state")
                                        + ", 该值必须为UP或者DOWN!\n");
                                return;
                            }
                            searchMatrixServers.add(searchMatrixServer);
                            addNewServer = false;
                            break;
                        }
                    }
                }
                if (addNewServer) {
                    SearchServer searchMatrixServer = new SearchServer();
                    searchMatrixServer.setId(searchMatrixServerJson
                            .getString("id"));
                    if (ServerState.UP.toString().equals(
                            searchMatrixServerJson.getString("state"))) {
                        searchMatrixServer.setState(ServerState.UP);
                    } else if (ServerState.DOWN.toString().equals(
                            searchMatrixServerJson.getString("state"))) {
                        searchMatrixServer.setState(ServerState.DOWN);
                    } else {
                        logger.error(
                                "设置的ID为{}的Search Matrix服务器状态为： {}, 该值必须为UP或者DOWN!",
                                searchMatrixServerJson.getString("id"),
                                searchMatrixServerJson.getString("state"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchMatrixServerJson.getString("id")
                                + "的Search Matrix服务器状态为： "
                                + searchMatrixServerJson.getString("state")
                                + ", 该值必须为UP或者DOWN!\n");
                        return;
                    }
                    searchMatrixServer.setIp(searchMatrixServerJson
                            .getString("ip"));
                    searchMatrixServer.setPort(searchMatrixServerJson
                            .getString("port"));
                    searchMatrixServer.setUrl(searchMatrixServerJson
                            .getString("url"));
                    searchMatrixServer.setServerType(ServerType.SEARCH_MATRIX);
                    searchMatrixServer.setFailConnNum(0);
                    if (searchMatrixServerJson.getInt("weight") < 0) {
                        logger.error(
                                "设置的ID为{}的Search Matrix服务器权值为： {}, 该权值必须大于等于0!",
                                searchMatrixServerJson.getString("id"),
                                searchMatrixServerJson.getInt("weight"));
                        emailParam.setSuccess(false);
                        emailParam.setMsg("设置的ID为"
                                + searchMatrixServerJson.getString("id")
                                + "的Search Matrix服务器权值为： "
                                + searchMatrixServerJson.getInt("weight")
                                + ", 该权值必须大于等于0!\n");
                        return;
                    }
                    searchMatrixServer.setWeight(searchMatrixServerJson
                            .getInt("weight"));
                    searchMatrixServer.setAbsoluteWeight(0);
                    SearchServerStatistics searchMatrixServerStatistics = new SearchServerStatistics();
                    searchMatrixServer
                            .setSearchServerStatistics(searchMatrixServerStatistics);
                    searchMatrixServers.add(searchMatrixServer);
                }
            }
            schedulerConfiguration.setSearchMatrixServers(searchMatrixServers);
            if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                    .getSystemDataConfiguration().getSchedulerStrategy())) {
                List<SearchServer> searchMatrixServersPollingList = new ArrayList<SearchServer>();
                for (SearchServer searchMatrixServer : schedulerConfiguration
                        .getSearchMatrixServers()) {
                    if (ServerState.UP.equals(searchMatrixServer.getState())
                            && searchMatrixServer.getWeight() > 0) {
                        int weight = searchMatrixServer.getWeight()
                                / DIVISOR_10;
                        while (weight-- > 0) {
                            searchMatrixServersPollingList
                                    .add(searchMatrixServer);
                        }
                    }
                }
                if (searchMatrixServersPollingList.size() > 0) {
                    schedulerConfiguration
                            .setCurrentPollingSearchMatrixServerIndex(r
                                    .nextInt(searchMatrixServersPollingList
                                            .size()));
                    schedulerConfiguration
                            .setSearchMatrixServersPollingList(searchMatrixServersPollingList);
                    schedulerConfiguration
                            .setSearchMatrixServersPollingListSize(searchMatrixServersPollingList
                                    .size());
                } else {
                    schedulerConfiguration
                            .setCurrentPollingSearchMatrixServerIndex(-1);
                }
            }
        } else if (schedulerConfiguration.getSearchMatrixServers() != null) {
            schedulerConfiguration.getSearchMatrixServers().clear();
            schedulerConfiguration.setSearchMatrixServers(null);
            schedulerConfiguration.getSearchMatrixServersPollingList().clear();
            schedulerConfiguration.setSearchMatrixServersPollingList(null);
            schedulerConfiguration.setCurrentPollingSearchMatrixServerIndex(-1);
        }

        emailParam.setMsg(emailParam.getMsg() + "old search matrix servers:\n"
                + oldSearchMatrixServers + "\n\n new search matrix servers:\n"
                + configData.toString() + "\n\n\n");
        oldSearchMatrixServers = configData.toString();
    }
}

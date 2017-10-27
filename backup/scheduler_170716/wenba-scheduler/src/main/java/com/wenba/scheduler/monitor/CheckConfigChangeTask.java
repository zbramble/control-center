package com.wenba.scheduler.monitor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wenba.scheduler.ISchedulerStrategy;
import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.ConfigParam;
import com.wenba.scheduler.config.ConfigParam.ConfigFileType;
import com.wenba.scheduler.config.ConfigResult;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.ocr.OcrHbaseResult;
import com.wenba.scheduler.search.SearchArticleHbaseResult;
import com.wenba.scheduler.search.SearchHbaseResult;
import com.wenba.scheduler.statistics.BIParam;

/**
 * check config file change or not
 * 
 * @author zhangbo
 *
 */
@Component("checkConfigChangeTask")
public class CheckConfigChangeTask {

    // 成员变量
    private Logger logger = LogManager.getLogger(CheckConfigChangeTask.class);
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private SchedulerControllerStatistics schedulerControllerStatistics;
    @Resource
    private ISchedulerStrategy<ConfigParam, ConfigResult> configStrategy;
    @Resource
    private ConcurrentLinkedQueue<OcrHbaseResult> ocrHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchHbaseResult> searchHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<OcrHbaseResult> ocrHbaseWordSearchQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchHbaseResult> searchHbaseWordSearchQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchArticleHbaseResult> searchArticleHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<BIParam> queryBiQueue;
    @Resource
    private ConcurrentLinkedQueue<Long> hbaseMonitorQueue;
    @Resource
    private ConcurrentLinkedQueue<OcrHbaseResult> miguOcrHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchHbaseResult> miguSearchHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<OcrHbaseResult> sdkOcrHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchHbaseResult> sdkSearchHbaseQueue;
    private long mailConfigurationLastModified;
    private long systemDataLastModified;
    private long systemSwitchLastModified;
    private long debugSwitchLastModified;
    private long accessServersLastModified;
    private long cnnServersLastModified;
    private long javaServersLastModified;
    private long searchServersLastModified;
    private long searchHomeworkServersLastModified;
    private long searchArticleServersLastModified;
    private long searchByIdServersLastModified;
    private long nlpServersLastModified;
    private long biServersLastModified;
    private long timeoutDataLastModified;
    private long emServersLastModified;
    private long ieServersLastModified;
    private long sdkSearchServersLastModified;
    private long handwriteOcrServersLastModified;
    private long ugcConfigLastModified;
    private long searchMatrixServersLastModified;

    public CheckConfigChangeTask() {
        // 初始化mail_configuration文件最后更新日期
        File mailConfiguration = new File("configFile/mail_configuration");
        mailConfigurationLastModified = mailConfiguration.lastModified();

        // 初始化system_data文件最后更新日期
        File systemData = new File("configFile/system_data");
        systemDataLastModified = systemData.lastModified();

        // 初始化system_switch文件最后更新日期
        File systemSwitch = new File("configFile/system_switch");
        systemSwitchLastModified = systemSwitch.lastModified();

        // 初始化debug_switch文件最后更新日期
        File debugSwitch = new File("configFile/debug_switch");
        debugSwitchLastModified = debugSwitch.lastModified();

        // 初始化access_servers文件最后更新日期
        File accessServers = new File("configFile/access_servers");
        accessServersLastModified = accessServers.lastModified();

        // 初始化cnn_servers文件最后更新日期
        File cnnServers = new File("configFile/cnn_servers");
        cnnServersLastModified = cnnServers.lastModified();

        // 初始化java_servers文件最后更新日期
        File javaServers = new File("configFile/java_servers");
        javaServersLastModified = javaServers.lastModified();

        // 初始化search_servers文件最后更新日期
        File searchServers = new File("configFile/search_servers");
        searchServersLastModified = searchServers.lastModified();

        // 初始化search_homework_servers文件最后更新日期
        File searchHomeworkServers = new File(
                "configFile/search_homework_servers");
        searchHomeworkServersLastModified = searchHomeworkServers
                .lastModified();

        // 初始化search_article_servers文件最后更新日期
        File searchArticleServers = new File(
                "configFile/search_article_servers");
        searchArticleServersLastModified = searchArticleServers.lastModified();

        // 初始化search_by_id_servers文件最后更新日期
        File searchByIdServers = new File("configFile/search_by_id_servers");
        searchByIdServersLastModified = searchByIdServers.lastModified();

        // 初始化nlp_servers文件最后更新日期
        File nlpServers = new File("configFile/nlp_servers");
        nlpServersLastModified = nlpServers.lastModified();

        // 初始化bi_servers文件最后更新日期
        File biServers = new File("configFile/bi_servers");
        biServersLastModified = biServers.lastModified();

        // 初始化timeout_data文件最后更新日期
        File timeoutData = new File("configFile/timeout_data");
        timeoutDataLastModified = timeoutData.lastModified();

        // 初始化em_servers文件最后更新日期
        File emServers = new File("configFile/em_servers");
        emServersLastModified = emServers.lastModified();

        // 初始化ie_servers文件最后更新日期
        File ieServers = new File("configFile/ie_servers");
        ieServersLastModified = ieServers.lastModified();

        // 初始化sdk_search_servers文件最后更新日期
        File sdkSearchServers = new File("configFile/sdk_search_servers");
        sdkSearchServersLastModified = sdkSearchServers.lastModified();

        // 初始化handwrite_ocr_servers文件最后更新日期
        File handwriteOcrServers = new File("configFile/handwrite_ocr_servers");
        handwriteOcrServersLastModified = handwriteOcrServers.lastModified();

        // 初始化ugc_config文件最后更新日期
        File ugcConfig = new File("configFile/ugc_config");
        ugcConfigLastModified = ugcConfig.lastModified();

        // 初始化search_matrix_servers文件最后更新日期
        File searchMatrixServers = new File("configFile/search_matrix_servers");
        searchMatrixServersLastModified = searchMatrixServers.lastModified();
    }

    @Scheduled(cron = "0/15 * * * * *")
    public void execute() {
        // 手动执行GC
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isGcDebugSwitch()) {
            System.gc();
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(date);
            logger.info("Debug GC exec at {}!", currentTime);
        }

        // 获取mail_configuration文件最后更新日期
        File mailConfiguration = new File("configFile/mail_configuration");
        if (mailConfigurationLastModified != mailConfiguration.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.MAIL_CONFIGURATION);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            mailConfigurationLastModified = mailConfiguration.lastModified();
        }

        // 获取system_data文件最后更新日期
        File systemData = new File("configFile/system_data");
        if (systemDataLastModified != systemData.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.SYSTEM_DATA);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            systemDataLastModified = systemData.lastModified();
        }

        // 获取system_switch文件最后更新日期
        File systemSwitch = new File("configFile/system_switch");
        if (systemSwitchLastModified != systemSwitch.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.SYSTEM_SWITCH);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            systemSwitchLastModified = systemSwitch.lastModified();
        }

        // 获取debug_switch文件最后更新日期
        File debugSwitch = new File("configFile/debug_switch");
        if (debugSwitchLastModified != debugSwitch.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.DEBUG_SWITCH);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            debugSwitchLastModified = debugSwitch.lastModified();
        }

        // 获取access_servers文件最后更新日期
        File accessServers = new File("configFile/access_servers");
        if (accessServersLastModified != accessServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.ACCESS_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            accessServersLastModified = accessServers.lastModified();
        }

        // 获取cnn_servers文件最后更新日期
        File cnnServers = new File("configFile/cnn_servers");
        if (cnnServersLastModified != cnnServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.CNN_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            cnnServersLastModified = cnnServers.lastModified();
        }

        // 获取java_servers文件最后更新日期
        File javaServers = new File("configFile/java_servers");
        if (javaServersLastModified != javaServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.JAVA_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            javaServersLastModified = javaServers.lastModified();
        }

        // 获取search_servers文件最后更新日期
        File searchServers = new File("configFile/search_servers");
        if (searchServersLastModified != searchServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.SEARCH_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            searchServersLastModified = searchServers.lastModified();
        }

        // 获取search_homework_servers文件最后更新日期
        File searchHomeworkServers = new File(
                "configFile/search_homework_servers");
        if (searchHomeworkServersLastModified != searchHomeworkServers
                .lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam
                    .setConfigFileType(ConfigFileType.SEARCH_HOMEWORK_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            searchHomeworkServersLastModified = searchHomeworkServers
                    .lastModified();
        }

        // 获取search_article_servers文件最后更新日期
        File searchArticleServers = new File(
                "configFile/search_article_servers");
        if (searchArticleServersLastModified != searchArticleServers
                .lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam
                    .setConfigFileType(ConfigFileType.SEARCH_ARTICLE_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            searchArticleServersLastModified = searchArticleServers
                    .lastModified();
        }

        // 获取search_by_id_servers文件最后更新日期
        File searchByIdServers = new File("configFile/search_by_id_servers");
        if (searchByIdServersLastModified != searchByIdServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.SEARCH_BY_ID_SERVERS);
            configStrategy.excute(configParam);
            searchByIdServersLastModified = searchByIdServers.lastModified();
        }

        // 获取nlp_servers文件最后更新日期
        File nlpServers = new File("configFile/nlp_servers");
        if (nlpServersLastModified != nlpServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.NLP_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            nlpServersLastModified = nlpServers.lastModified();
        }

        // 获取bi_servers文件最后更新日期
        File biServers = new File("configFile/bi_servers");
        if (biServersLastModified != biServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.BI_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            biServersLastModified = biServers.lastModified();
        }

        // 获取timeout_data文件最后更新日期
        File timeoutData = new File("configFile/timeout_data");
        if (timeoutDataLastModified != timeoutData.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.TIMEOUT_DATA);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            timeoutDataLastModified = timeoutData.lastModified();
        }

        // 获取em_servers文件最后更新日期
        File emServers = new File("configFile/em_servers");
        if (emServersLastModified != emServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.EM_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            emServersLastModified = emServers.lastModified();
        }

        // 获取ie_servers文件最后更新日期
        File ieServers = new File("configFile/ie_servers");
        if (ieServersLastModified != ieServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.IE_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            ieServersLastModified = ieServers.lastModified();
        }

        // 获取sdk_search_servers文件最后更新日期
        File sdkSearchServers = new File("configFile/sdk_search_servers");
        if (sdkSearchServersLastModified != sdkSearchServers.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.SDK_SEARCH_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            sdkSearchServersLastModified = sdkSearchServers.lastModified();
        }

        // 获取handwrite_ocr_servers文件最后更新日期
        File handwriteOcrServers = new File("configFile/handwrite_ocr_servers");
        if (handwriteOcrServersLastModified != handwriteOcrServers
                .lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.HANDWRITE_OCR_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            handwriteOcrServersLastModified = handwriteOcrServers
                    .lastModified();
        }

        // 获取ugc_config文件最后更新日期
        File ugcConfig = new File("configFile/ugc_config");
        if (ugcConfigLastModified != ugcConfig.lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.UGC_CONFIG);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            ugcConfigLastModified = ugcConfig.lastModified();
        }

        // 获取search_matrix_servers文件最后更新日期
        File searchMatrixServers = new File("configFile/search_matrix_servers");
        if (searchMatrixServersLastModified != searchMatrixServers
                .lastModified()) {
            ConfigParam configParam = new ConfigParam();
            configParam.setSchedulerConfiguration(schedulerConfiguration);
            configParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            configParam.setConfigFileType(ConfigFileType.SEARCH_MATRIX_SERVERS);
            configParam.setConfigData(null);
            configStrategy.excute(configParam);
            searchMatrixServersLastModified = searchMatrixServers
                    .lastModified();
        }

        // 判断异步队列是否超过阈值
        if (ocrHbaseQueue.size() >= schedulerConfiguration
                .getSystemDataConfiguration().getAsyncQueueSize()
                || searchHbaseQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()
                || queryBiQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()
                || ocrHbaseWordSearchQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()
                || searchHbaseWordSearchQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()
                || searchArticleHbaseQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()
                || hbaseMonitorQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()
                || miguOcrHbaseQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()
                || miguSearchHbaseQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()
                || sdkOcrHbaseQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()
                || sdkSearchHbaseQueue.size() >= schedulerConfiguration
                        .getSystemDataConfiguration().getAsyncQueueSize()) {
            schedulerConfiguration.setHbaseOnSwitch(false);
            schedulerConfiguration.setQueryBiModeSwitch("Timer");
        } else {
            schedulerConfiguration.setHbaseOnSwitch(true);
            schedulerConfiguration.setQueryBiModeSwitch("Async");
        }
    }
}

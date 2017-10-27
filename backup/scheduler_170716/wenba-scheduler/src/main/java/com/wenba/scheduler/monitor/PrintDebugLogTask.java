package com.wenba.scheduler.monitor;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.nlp.NlpServer;
import com.wenba.scheduler.ocr.OcrHbaseResult;
import com.wenba.scheduler.ocr.OcrServer;
import com.wenba.scheduler.search.SearchArticleHbaseResult;
import com.wenba.scheduler.search.SearchArticleLog;
import com.wenba.scheduler.search.SearchHbaseResult;
import com.wenba.scheduler.search.SearchServer;
import com.wenba.scheduler.statistics.BIParam;
import com.wenba.scheduler.statistics.BIServer;

/**
 * print debug log(warning:do not use often!only if needed!)
 * 
 * @author zhangbo
 *
 */
@Component("printDebugLogTask")
public class PrintDebugLogTask {

    // 成员变量
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private SchedulerControllerStatistics schedulerControllerStatistics;
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
    private ConcurrentLinkedQueue<SearchArticleLog> searchArticleQueryLogQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchArticleLog> searchArticleAutoCompleteLogQueue;
    @Resource
    private ConcurrentLinkedQueue<BIParam> queryBiQueue;
    @Resource
    private ConcurrentLinkedQueue<OcrHbaseResult> miguOcrHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchHbaseResult> miguSearchHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<OcrHbaseResult> sdkOcrHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchHbaseResult> sdkSearchHbaseQueue;
    private static Logger logger = LogManager.getLogger("debugInfo");

    @Scheduled(cron = "0/1 * * * * *")
    public void execute() {
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAllRequestAndHbaseQueueDebugSwitch()) {
            logger.info("ARN:{}",
                    schedulerControllerStatistics.getAllRequestNum());
            logger.info("CRN:{}",
                    schedulerControllerStatistics.getCurrentRequestNum());

            logger.info("CCRN:{}",
                    schedulerControllerStatistics.getCurrentCnnRequestNum());
            logger.info("CSRN:{}",
                    schedulerControllerStatistics.getCurrentSearchRequestNum());

            int currentNlpRequestNum = 0;
            for (NlpServer nlpServer : schedulerConfiguration.getNlpServers()) {
                currentNlpRequestNum += nlpServer.getNlpServerStatistics()
                        .getCurrentRequestNum();
            }
            logger.info("CNRN:{}", currentNlpRequestNum);

            logger.info("OHRN:{}",
                    schedulerControllerStatistics.getOcrHbaseResultNum());
            logger.info("SHRN:{}",
                    schedulerControllerStatistics.getSearchHbaseResultNum());
            logger.info("SAHRN:{}", schedulerControllerStatistics
                    .getSearchArticleHbaseResultNum());
            logger.info("MOHRN:{}",
                    schedulerControllerStatistics.getMiguOcrHbaseResultNum());
            logger.info("MSHRN:{}",
                    schedulerControllerStatistics.getMiguSearchHbaseResultNum());

            logger.info("OHQN:{}", ocrHbaseQueue.size());
            logger.info("SHQN:{}", searchHbaseQueue.size());
            logger.info("QBQN:{}", queryBiQueue.size());
            logger.info("MOHQN:{}", miguOcrHbaseQueue.size());
            logger.info("MSHQN:{}", miguSearchHbaseQueue.size());
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgQueryExecTimeDebugSwitch()) {
            if (schedulerControllerStatistics.getExecTimeNum() > 0) {
                logger.info("AET:{}", (schedulerControllerStatistics
                        .getExecTime() / schedulerControllerStatistics
                        .getExecTimeNum()));
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgQueryByIdExecTimeDebugSwitch()) {
            if (schedulerControllerStatistics.getExecTimeByIdNum() > 0) {
                logger.info("AET By Id:{}", (schedulerControllerStatistics
                        .getExecTimeById() / schedulerControllerStatistics
                        .getExecTimeByIdNum()));
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgHomeworkExecTimeDebugSwitch()) {
            if (schedulerControllerStatistics.getExecTimeByHomeworkNum() > 0) {
                logger.info(
                        "AET By Homework:{}",
                        (schedulerControllerStatistics.getExecTimeByHomework() / schedulerControllerStatistics
                                .getExecTimeByHomeworkNum()));
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchExecTimeDebugSwitch()) {
            if (schedulerControllerStatistics.getExecTimeBySearchNum() > 0) {
                logger.info("AET By Search:{}", (schedulerControllerStatistics
                        .getExecTimeBySearch() / schedulerControllerStatistics
                        .getExecTimeBySearchNum()));
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgWordSearchExecTimeDebugSwitch()) {
            if (schedulerControllerStatistics.getExecTimeByWordSearchNum() > 0) {
                logger.info(
                        "AET By Word Search:{}",
                        (schedulerControllerStatistics
                                .getExecTimeByWordSearch() / schedulerControllerStatistics
                                .getExecTimeByWordSearchNum()));
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgArticleExecTimeDebugSwitch()) {
            if (schedulerControllerStatistics.getExecTimeByArticleNum() > 0) {
                logger.info("AET By Article:{}", (schedulerControllerStatistics
                        .getExecTimeByArticle() / schedulerControllerStatistics
                        .getExecTimeByArticleNum()));
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgOcrHbaseExecTimeDebugSwitch()) {
            if (schedulerControllerStatistics.getOcrHbaseExecTimeNum() > 0) {
                logger.info("AOHET:{}", (schedulerControllerStatistics
                        .getOcrHbaseExecTime() / schedulerControllerStatistics
                        .getOcrHbaseExecTimeNum()));
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchHbaseExecTimeDebugSwitch()) {
            if (schedulerControllerStatistics.getSearchHbaseExecTimeNum() > 0) {
                logger.info(
                        "ASHET:{}",
                        (schedulerControllerStatistics.getSearchHbaseExecTime() / schedulerControllerStatistics
                                .getSearchHbaseExecTimeNum()));
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchArticleHbaseExecTimeDebugSwitch()) {
            if (schedulerControllerStatistics
                    .getSearchArticleHbaseExecTimeNum() > 0) {
                logger.info(
                        "ASAHET:{}",
                        (schedulerControllerStatistics
                                .getSearchArticleHbaseExecTime() / schedulerControllerStatistics
                                .getSearchArticleHbaseExecTimeNum()));
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgCnnServerExecTimeDebugSwitch()
                && schedulerConfiguration.getCnnServers() != null) {
            for (OcrServer ocrServer : schedulerConfiguration.getCnnServers()) {
                if (ocrServer.getOcrServerStatistics().getExecTimeNum() > 0) {
                    logger.info(
                            "CNN ID:{}, AET:{}",
                            ocrServer.getId(),
                            (ocrServer.getOcrServerStatistics().getExecTime() / ocrServer
                                    .getOcrServerStatistics().getExecTimeNum()));
                }
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgJavaServerExecTimeDebugSwitch()
                && schedulerConfiguration.getJavaServers() != null) {
            for (OcrServer ocrServer : schedulerConfiguration.getJavaServers()) {
                if (ocrServer.getOcrServerStatistics().getExecTimeNum() > 0) {
                    logger.info(
                            "JAVA ID:{}, AET:{}",
                            ocrServer.getId(),
                            (ocrServer.getOcrServerStatistics().getExecTime() / ocrServer
                                    .getOcrServerStatistics().getExecTimeNum()));
                }
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchServerExecTimeDebugSwitch()
                && schedulerConfiguration.getSearchServers() != null) {
            for (SearchServer searchServer : schedulerConfiguration
                    .getSearchServers()) {
                if (searchServer.getSearchServerStatistics().getExecTimeNum() > 0) {
                    logger.info("Search ID:{}, AET:{}", searchServer.getId(),
                            (searchServer.getSearchServerStatistics()
                                    .getExecTime() / searchServer
                                    .getSearchServerStatistics()
                                    .getExecTimeNum()));
                }
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgWordSearchServerExecTimeDebugSwitch()
                && schedulerConfiguration.getSearchServers() != null) {
            for (SearchServer searchServer : schedulerConfiguration
                    .getSearchServers()) {
                if (searchServer.getSearchServerStatistics()
                        .getExecTimeByWordNum() > 0) {
                    logger.info("Search ID:{}, AET By Word:{}", searchServer
                            .getId(),
                            (searchServer.getSearchServerStatistics()
                                    .getExecTimeByWord() / searchServer
                                    .getSearchServerStatistics()
                                    .getExecTimeByWordNum()));
                }
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchByIdServerExecTimeDebugSwitch()
                && schedulerConfiguration.getSearchServers() != null) {
            for (SearchServer searchServer : schedulerConfiguration
                    .getSearchServers()) {
                if (searchServer.getSearchServerStatistics()
                        .getExecTimeByIdNum() > 0) {
                    logger.info("Search ID:{}, AET By Id:{}", searchServer
                            .getId(), (searchServer.getSearchServerStatistics()
                            .getExecTimeById() / searchServer
                            .getSearchServerStatistics().getExecTimeByIdNum()));
                }
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchHomeworkServerExecTimeDebugSwitch()
                && schedulerConfiguration.getSearchHomeworkServers() != null) {
            for (SearchServer searchHomeworkServer : schedulerConfiguration
                    .getSearchHomeworkServers()) {
                if (searchHomeworkServer.getSearchServerStatistics()
                        .getExecTimeByHomeworkNum() > 0) {
                    logger.info(
                            "Search Homework ID:{}, AET:{}",
                            searchHomeworkServer.getId(),
                            (searchHomeworkServer.getSearchServerStatistics()
                                    .getExecTimeByHomework() / searchHomeworkServer
                                    .getSearchServerStatistics()
                                    .getExecTimeByHomeworkNum()));
                }
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchArticleServerExecTimeDebugSwitch()
                && schedulerConfiguration.getSearchArticleServers() != null) {
            for (SearchServer searchArticleServer : schedulerConfiguration
                    .getSearchArticleServers()) {
                if (searchArticleServer.getSearchServerStatistics()
                        .getExecTimeByArticleNum() > 0) {
                    logger.info(
                            "Search Article ID:{}, AET:{}",
                            searchArticleServer.getId(),
                            (searchArticleServer.getSearchServerStatistics()
                                    .getExecTimeByArticle() / searchArticleServer
                                    .getSearchServerStatistics()
                                    .getExecTimeByArticleNum()));
                }
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgNlpServerExecTimeDebugSwitch()
                && schedulerConfiguration.getNlpServers() != null) {
            for (NlpServer nlpServer : schedulerConfiguration.getNlpServers()) {
                if (nlpServer.getNlpServerStatistics().getExecTimeNum() > 0) {
                    logger.info(
                            "Nlp ID:{}, ANET:{}",
                            nlpServer.getId(),
                            (nlpServer.getNlpServerStatistics().getExecTime() / nlpServer
                                    .getNlpServerStatistics().getExecTimeNum()));
                }
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isCurrentCnnOcrRequestDebugSwitch()
                && schedulerConfiguration.getCnnServers() != null) {
            for (OcrServer ocrServer : schedulerConfiguration.getCnnServers()) {
                logger.info("CNN ID:{}, CRN:{}", ocrServer.getId(), ocrServer
                        .getOcrServerStatistics().getCurrentRequestNum());
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isCurrentSearchRequestDebugSwitch()
                && schedulerConfiguration.getSearchServers() != null) {
            for (SearchServer searchServer : schedulerConfiguration
                    .getSearchServers()) {
                logger.info("Search ID:{}, CRN:{}", searchServer.getId(),
                        searchServer.getSearchServerStatistics()
                                .getCurrentRequestNum());
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isCurrentNlpRequestDebugSwitch()
                && schedulerConfiguration.getNlpServers() != null) {
            for (NlpServer nlpServer : schedulerConfiguration.getNlpServers()) {
                logger.info("Nlp ID:{}, CRN:{}", nlpServer.getId(), nlpServer
                        .getNlpServerStatistics().getCurrentRequestNum());
            }
        }

        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isDisplayMarksDebugSwitch()) {
            if (schedulerConfiguration.getCnnServers() != null) {
                for (OcrServer ocrServer : schedulerConfiguration
                        .getCnnServers()) {
                    logger.info("CNN ID:{}, Marks:{}", ocrServer.getId(),
                            ocrServer.getMarks());
                }
            }

            if (schedulerConfiguration.getJavaServers() != null) {
                for (OcrServer ocrServer : schedulerConfiguration
                        .getJavaServers()) {
                    logger.info("JAVA ID:{}, Marks:{}", ocrServer.getId(),
                            ocrServer.getMarks());
                }
            }

            if (schedulerConfiguration.getSearchServers() != null) {
                for (SearchServer searchServer : schedulerConfiguration
                        .getSearchServers()) {
                    logger.info("Search ID:{}, Marks:{}", searchServer.getId(),
                            searchServer.getMarks());
                }
            }

            if (schedulerConfiguration.getSearchHomeworkServers() != null) {
                for (SearchServer searchHomeworkServer : schedulerConfiguration
                        .getSearchHomeworkServers()) {
                    logger.info("Search Homework ID:{}, Marks:{}",
                            searchHomeworkServer.getId(),
                            searchHomeworkServer.getMarks());
                }
            }

            if (schedulerConfiguration.getSearchArticleServers() != null) {
                for (SearchServer searchArticleServer : schedulerConfiguration
                        .getSearchArticleServers()) {
                    logger.info("Search Article ID:{}, Marks:{}",
                            searchArticleServer.getId(),
                            searchArticleServer.getMarks());
                }
            }

            if (schedulerConfiguration.getNlpServers() != null) {
                for (NlpServer nlpServer : schedulerConfiguration
                        .getNlpServers()) {
                    logger.info("Nlp ID:{}, Marks:{}", nlpServer.getId(),
                            nlpServer.getMarks());
                }
            }

            if (schedulerConfiguration.getBiServers() != null) {
                for (BIServer biServer : schedulerConfiguration.getBiServers()) {
                    logger.info("BI ID:{}, Marks:{}", biServer.getId(),
                            biServer.getMarks());
                }
            }

        }
    }
}

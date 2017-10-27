package com.wenba.scheduler.monitor;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.nlp.NlpServer;
import com.wenba.scheduler.ocr.OcrServer;
import com.wenba.scheduler.search.SearchServer;
import com.wenba.scheduler.statistics.BIServer;

/**
 * print error log
 * 
 * @author zhangbo
 *
 */
@Component("printStatisticsLogTask")
public class PrintStatisticsLogTask {

    // 成员变量
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private SchedulerControllerStatistics schedulerControllerStatistics;
    private static Logger logger = LogManager
            .getLogger(PrintStatisticsLogTask.class);

    @Scheduled(cron = "0 0/15 * * * *")
    public void execute() {
        // TODO errorLogger.error("IIN:"
        // + schedulerControllerStatistics.getImgNullNum());

        int ocrCnnJeNum = 0;
        int ocrCnnHhceNum = 0;
        int ocrCnnCteNum = 0;
        int ocrCnnNhreNum = 0;
        int ocrCnnSteNum = 0;
        int ocrCnnCpeNum = 0;
        int ocrCnnUeeNum = 0;
        int ocrCnnIoeNum = 0;
        int ocrCnnHsneNum = 0;
        int ocrCnnHeneNum = 0;
        int ocrCnnHeseNum = 0;
        int ocrCnnOfteNum = 0;
        int ocrCnnIoobeNum = 0;
        int ocrCnnOeNum = 0;
        if (schedulerConfiguration.getCnnServers() != null) {
            for (OcrServer ocrServer : schedulerConfiguration.getCnnServers()) {
                logger.error(
                        "CNN ID:{}, JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, OFTE:{}, IOOBE:{}, OE:{}",
                        ocrServer.getId(), ocrServer.getOcrServerStatistics()
                                .getJeNum(), ocrServer.getOcrServerStatistics()
                                .getHhceNum(), ocrServer
                                .getOcrServerStatistics().getCteNum(),
                        ocrServer.getOcrServerStatistics().getNhreNum(),
                        ocrServer.getOcrServerStatistics().getSteNum(),
                        ocrServer.getOcrServerStatistics().getCpeNum(),
                        ocrServer.getOcrServerStatistics().getUeeNum(),
                        ocrServer.getOcrServerStatistics().getIoeNum(),
                        ocrServer.getOcrServerStatistics().getHsneNum(),
                        ocrServer.getOcrServerStatistics().getHeneNum(),
                        ocrServer.getOcrServerStatistics().getHeseNum(),
                        ocrServer.getOcrServerStatistics().getOfteNum(),
                        ocrServer.getOcrServerStatistics().getIoobeNum(),
                        ocrServer.getOcrServerStatistics().getOeNum());

                ocrCnnJeNum += ocrServer.getOcrServerStatistics().getJeNum();
                ocrCnnHhceNum += ocrServer.getOcrServerStatistics()
                        .getHhceNum();
                ocrCnnCteNum += ocrServer.getOcrServerStatistics().getCteNum();
                ocrCnnNhreNum += ocrServer.getOcrServerStatistics()
                        .getNhreNum();
                ocrCnnSteNum += ocrServer.getOcrServerStatistics().getSteNum();
                ocrCnnCpeNum += ocrServer.getOcrServerStatistics().getCpeNum();
                ocrCnnUeeNum += ocrServer.getOcrServerStatistics().getUeeNum();
                ocrCnnIoeNum += ocrServer.getOcrServerStatistics().getIoeNum();
                ocrCnnHsneNum += ocrServer.getOcrServerStatistics()
                        .getHsneNum();
                ocrCnnHeneNum += ocrServer.getOcrServerStatistics()
                        .getHeneNum();
                ocrCnnHeseNum += ocrServer.getOcrServerStatistics()
                        .getHeseNum();
                ocrCnnOfteNum += ocrServer.getOcrServerStatistics()
                        .getOfteNum();
                ocrCnnIoobeNum += ocrServer.getOcrServerStatistics()
                        .getIoobeNum();
                ocrCnnOeNum += ocrServer.getOcrServerStatistics().getOeNum();
            }
            logger.error(
                    "TOE of CNN: JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, OFTE:{}, IOOBE:{}, OE:{}",
                    ocrCnnJeNum, ocrCnnHhceNum, ocrCnnCteNum, ocrCnnNhreNum,
                    ocrCnnSteNum, ocrCnnCpeNum, ocrCnnUeeNum, ocrCnnIoeNum,
                    ocrCnnHsneNum, ocrCnnHeneNum, ocrCnnHeseNum, ocrCnnOfteNum,
                    ocrCnnIoobeNum, ocrCnnOeNum);
        }

        int ocrJavaJeNum = 0;
        int ocrJavaHhceNum = 0;
        int ocrJavaCteNum = 0;
        int ocrJavaNhreNum = 0;
        int ocrJavaSteNum = 0;
        int ocrJavaCpeNum = 0;
        int ocrJavaUeeNum = 0;
        int ocrJavaIoeNum = 0;
        int ocrJavaHsneNum = 0;
        int ocrJavaHeneNum = 0;
        int ocrJavaHeseNum = 0;
        int ocrJavaOfteNum = 0;
        int ocrJavaIoobeNum = 0;
        int ocrJavaOeNum = 0;
        if (schedulerConfiguration.getJavaServers() != null) {
            for (OcrServer ocrServer : schedulerConfiguration.getJavaServers()) {
                logger.error(
                        "Java ID:{}, JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, OFTE:{}, IOOBE:{}, OE:{}",
                        ocrServer.getId(), ocrServer.getOcrServerStatistics()
                                .getJeNum(), ocrServer.getOcrServerStatistics()
                                .getHhceNum(), ocrServer
                                .getOcrServerStatistics().getCteNum(),
                        ocrServer.getOcrServerStatistics().getNhreNum(),
                        ocrServer.getOcrServerStatistics().getSteNum(),
                        ocrServer.getOcrServerStatistics().getCpeNum(),
                        ocrServer.getOcrServerStatistics().getUeeNum(),
                        ocrServer.getOcrServerStatistics().getIoeNum(),
                        ocrServer.getOcrServerStatistics().getHsneNum(),
                        ocrServer.getOcrServerStatistics().getHeneNum(),
                        ocrServer.getOcrServerStatistics().getHeseNum(),
                        ocrServer.getOcrServerStatistics().getOfteNum(),
                        ocrServer.getOcrServerStatistics().getIoobeNum(),
                        ocrServer.getOcrServerStatistics().getOeNum());

                ocrJavaJeNum += ocrServer.getOcrServerStatistics().getJeNum();
                ocrJavaHhceNum += ocrServer.getOcrServerStatistics()
                        .getHhceNum();
                ocrJavaCteNum += ocrServer.getOcrServerStatistics().getCteNum();
                ocrJavaNhreNum += ocrServer.getOcrServerStatistics()
                        .getNhreNum();
                ocrJavaSteNum += ocrServer.getOcrServerStatistics().getSteNum();
                ocrJavaCpeNum += ocrServer.getOcrServerStatistics().getCpeNum();
                ocrJavaUeeNum += ocrServer.getOcrServerStatistics().getUeeNum();
                ocrJavaIoeNum += ocrServer.getOcrServerStatistics().getIoeNum();
                ocrJavaHsneNum += ocrServer.getOcrServerStatistics()
                        .getHsneNum();
                ocrJavaHeneNum += ocrServer.getOcrServerStatistics()
                        .getHeneNum();
                ocrJavaHeseNum += ocrServer.getOcrServerStatistics()
                        .getHeseNum();
                ocrJavaOfteNum += ocrServer.getOcrServerStatistics()
                        .getOfteNum();
                ocrJavaIoobeNum += ocrServer.getOcrServerStatistics()
                        .getIoobeNum();
                ocrJavaOeNum += ocrServer.getOcrServerStatistics().getOeNum();
            }
            logger.error(
                    "TOE of JAVA: JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, OFTE:{}, IOOBE:{}, OE:{}",
                    ocrJavaJeNum, ocrJavaHhceNum, ocrJavaCteNum,
                    ocrJavaNhreNum, ocrJavaSteNum, ocrJavaCpeNum,
                    ocrJavaUeeNum, ocrJavaIoeNum, ocrJavaHsneNum,
                    ocrJavaHeneNum, ocrJavaHeseNum, ocrJavaOfteNum,
                    ocrJavaIoobeNum, ocrJavaOeNum);
        }

        int ocrJeNum = ocrCnnJeNum + ocrJavaJeNum;
        int ocrHhceNum = ocrCnnHhceNum + ocrJavaHhceNum;
        int ocrCteNum = ocrCnnCteNum + ocrJavaCteNum;
        int ocrNhreNum = ocrCnnNhreNum + ocrJavaNhreNum;
        int ocrSteNum = ocrCnnSteNum + ocrJavaSteNum;
        int ocrCpeNum = ocrCnnCpeNum + ocrJavaCpeNum;
        int ocrUeeNum = ocrCnnUeeNum + ocrJavaUeeNum;
        int ocrIoeNum = ocrCnnIoeNum + ocrJavaIoeNum;
        int ocrHsneNum = ocrCnnHsneNum + ocrJavaHsneNum;
        int ocrHeneNum = ocrCnnHeneNum + ocrJavaHeneNum;
        int ocrHeseNum = ocrCnnHeseNum + ocrJavaHeseNum;
        int ocrOfteNum = ocrCnnOfteNum + ocrJavaOfteNum;
        int ocrIoobeNum = ocrCnnIoobeNum + ocrJavaIoobeNum;
        int ocrOeNum = ocrCnnOeNum + ocrJavaOeNum;
        if (schedulerConfiguration.getCnnServers() != null
                || schedulerConfiguration.getJavaServers() != null) {
            logger.error(
                    "TOE of Ocr: JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, OFTE:{}, IOOBE:{}, OE:{}",
                    ocrJeNum, ocrHhceNum, ocrCteNum, ocrNhreNum, ocrSteNum,
                    ocrCpeNum, ocrUeeNum, ocrIoeNum, ocrHsneNum, ocrHeneNum,
                    ocrHeseNum, ocrOfteNum, ocrIoobeNum, ocrOeNum);
        }

        if (schedulerConfiguration.getSearchServers() != null) {
            int searchJeNum = 0;
            int searchHhceNum = 0;
            int searchCteNum = 0;
            int searchNhreNum = 0;
            int searchSteNum = 0;
            int searchCpeNum = 0;
            int searchUeeNum = 0;
            int searchIoeNum = 0;
            int searchHsneNum = 0;
            int searchHeneNum = 0;
            int searchHeseNum = 0;
            int searchIoobeNum = 0;
            int searchOeNum = 0;
            for (SearchServer searchServer : schedulerConfiguration
                    .getSearchServers()) {
                logger.error(
                        "Search ID:{}, JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, IOOBE:{}, OE:{}",
                        searchServer.getId(), searchServer
                                .getSearchServerStatistics().getJeNum(),
                        searchServer.getSearchServerStatistics().getHhceNum(),
                        searchServer.getSearchServerStatistics().getCteNum(),
                        searchServer.getSearchServerStatistics().getNhreNum(),
                        searchServer.getSearchServerStatistics().getSteNum(),
                        searchServer.getSearchServerStatistics().getCpeNum(),
                        searchServer.getSearchServerStatistics().getUeeNum(),
                        searchServer.getSearchServerStatistics().getIoeNum(),
                        searchServer.getSearchServerStatistics().getHsneNum(),
                        searchServer.getSearchServerStatistics().getHeneNum(),
                        searchServer.getSearchServerStatistics().getHeseNum(),
                        searchServer.getSearchServerStatistics().getIoobeNum(),
                        searchServer.getSearchServerStatistics().getOeNum());

                searchJeNum += searchServer.getSearchServerStatistics()
                        .getJeNum();
                searchHhceNum += searchServer.getSearchServerStatistics()
                        .getHhceNum();
                searchCteNum += searchServer.getSearchServerStatistics()
                        .getCteNum();
                searchNhreNum += searchServer.getSearchServerStatistics()
                        .getNhreNum();
                searchSteNum += searchServer.getSearchServerStatistics()
                        .getSteNum();
                searchCpeNum += searchServer.getSearchServerStatistics()
                        .getCpeNum();
                searchUeeNum += searchServer.getSearchServerStatistics()
                        .getUeeNum();
                searchIoeNum += searchServer.getSearchServerStatistics()
                        .getIoeNum();
                searchHsneNum += searchServer.getSearchServerStatistics()
                        .getHsneNum();
                searchHeneNum += searchServer.getSearchServerStatistics()
                        .getHeneNum();
                searchHeseNum += searchServer.getSearchServerStatistics()
                        .getHeseNum();
                searchIoobeNum += searchServer.getSearchServerStatistics()
                        .getIoobeNum();
                searchOeNum += searchServer.getSearchServerStatistics()
                        .getOeNum();
            }
            logger.error(
                    "TSE: JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, IOOBE:{}, OE:{}",
                    searchJeNum, searchHhceNum, searchCteNum, searchNhreNum,
                    searchSteNum, searchCpeNum, searchUeeNum, searchIoeNum,
                    searchHsneNum, searchHeneNum, searchHeseNum,
                    searchIoobeNum, searchOeNum);
        }

        if (schedulerConfiguration.getSearchHomeworkServers() != null) {
            int searchHomeworkJeNum = 0;
            int searchHomeworkHhceNum = 0;
            int searchHomeworkCteNum = 0;
            int searchHomeworkNhreNum = 0;
            int searchHomeworkSteNum = 0;
            int searchHomeworkCpeNum = 0;
            int searchHomeworkUeeNum = 0;
            int searchHomeworkIoeNum = 0;
            int searchHomeworkHsneNum = 0;
            int searchHomeworkHeneNum = 0;
            int searchHomeworkHeseNum = 0;
            int searchHomeworkIoobeNum = 0;
            int searchHomeworkOeNum = 0;
            for (SearchServer searchHomeworkServer : schedulerConfiguration
                    .getSearchHomeworkServers()) {
                logger.error(
                        "Search Homework ID:{}, JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, IOOBE:{}, OE:{}",
                        searchHomeworkServer.getId(), searchHomeworkServer
                                .getSearchServerStatistics().getJeNum(),
                        searchHomeworkServer.getSearchServerStatistics()
                                .getHhceNum(), searchHomeworkServer
                                .getSearchServerStatistics().getCteNum(),
                        searchHomeworkServer.getSearchServerStatistics()
                                .getNhreNum(), searchHomeworkServer
                                .getSearchServerStatistics().getSteNum(),
                        searchHomeworkServer.getSearchServerStatistics()
                                .getCpeNum(), searchHomeworkServer
                                .getSearchServerStatistics().getUeeNum(),
                        searchHomeworkServer.getSearchServerStatistics()
                                .getIoeNum(), searchHomeworkServer
                                .getSearchServerStatistics().getHsneNum(),
                        searchHomeworkServer.getSearchServerStatistics()
                                .getHeneNum(), searchHomeworkServer
                                .getSearchServerStatistics().getHeseNum(),
                        searchHomeworkServer.getSearchServerStatistics()
                                .getIoobeNum(), searchHomeworkServer
                                .getSearchServerStatistics().getOeNum());

                searchHomeworkJeNum += searchHomeworkServer
                        .getSearchServerStatistics().getJeNum();
                searchHomeworkHhceNum += searchHomeworkServer
                        .getSearchServerStatistics().getHhceNum();
                searchHomeworkCteNum += searchHomeworkServer
                        .getSearchServerStatistics().getCteNum();
                searchHomeworkNhreNum += searchHomeworkServer
                        .getSearchServerStatistics().getNhreNum();
                searchHomeworkSteNum += searchHomeworkServer
                        .getSearchServerStatistics().getSteNum();
                searchHomeworkCpeNum += searchHomeworkServer
                        .getSearchServerStatistics().getCpeNum();
                searchHomeworkUeeNum += searchHomeworkServer
                        .getSearchServerStatistics().getUeeNum();
                searchHomeworkIoeNum += searchHomeworkServer
                        .getSearchServerStatistics().getIoeNum();
                searchHomeworkHsneNum += searchHomeworkServer
                        .getSearchServerStatistics().getHsneNum();
                searchHomeworkHeneNum += searchHomeworkServer
                        .getSearchServerStatistics().getHeneNum();
                searchHomeworkHeseNum += searchHomeworkServer
                        .getSearchServerStatistics().getHeseNum();
                searchHomeworkIoobeNum += searchHomeworkServer
                        .getSearchServerStatistics().getIoobeNum();
                searchHomeworkOeNum += searchHomeworkServer
                        .getSearchServerStatistics().getOeNum();
            }
            logger.error(
                    "TSHE: JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, IOOBE:{}, OE:{}",
                    searchHomeworkJeNum, searchHomeworkHhceNum,
                    searchHomeworkCteNum, searchHomeworkNhreNum,
                    searchHomeworkSteNum, searchHomeworkCpeNum,
                    searchHomeworkUeeNum, searchHomeworkIoeNum,
                    searchHomeworkHsneNum, searchHomeworkHeneNum,
                    searchHomeworkHeseNum, searchHomeworkIoobeNum,
                    searchHomeworkOeNum);
        }

        if (schedulerConfiguration.getSearchArticleServers() != null) {
            int searchArticleJeNum = 0;
            int searchArticleHhceNum = 0;
            int searchArticleCteNum = 0;
            int searchArticleNhreNum = 0;
            int searchArticleSteNum = 0;
            int searchArticleCpeNum = 0;
            int searchArticleUeeNum = 0;
            int searchArticleIoeNum = 0;
            int searchArticleHsneNum = 0;
            int searchArticleHeneNum = 0;
            int searchArticleHeseNum = 0;
            int searchArticleIoobeNum = 0;
            int searchArticleOeNum = 0;
            for (SearchServer searchArticleServer : schedulerConfiguration
                    .getSearchArticleServers()) {
                logger.error(
                        "Search Article ID:{}, JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, IOOBE:{}, OE:{}",
                        searchArticleServer.getId(), searchArticleServer
                                .getSearchServerStatistics().getJeNum(),
                        searchArticleServer.getSearchServerStatistics()
                                .getHhceNum(), searchArticleServer
                                .getSearchServerStatistics().getCteNum(),
                        searchArticleServer.getSearchServerStatistics()
                                .getNhreNum(), searchArticleServer
                                .getSearchServerStatistics().getSteNum(),
                        searchArticleServer.getSearchServerStatistics()
                                .getCpeNum(), searchArticleServer
                                .getSearchServerStatistics().getUeeNum(),
                        searchArticleServer.getSearchServerStatistics()
                                .getIoeNum(), searchArticleServer
                                .getSearchServerStatistics().getHsneNum(),
                        searchArticleServer.getSearchServerStatistics()
                                .getHeneNum(), searchArticleServer
                                .getSearchServerStatistics().getHeseNum(),
                        searchArticleServer.getSearchServerStatistics()
                                .getIoobeNum(), searchArticleServer
                                .getSearchServerStatistics().getOeNum());

                searchArticleJeNum += searchArticleServer
                        .getSearchServerStatistics().getJeNum();
                searchArticleHhceNum += searchArticleServer
                        .getSearchServerStatistics().getHhceNum();
                searchArticleCteNum += searchArticleServer
                        .getSearchServerStatistics().getCteNum();
                searchArticleNhreNum += searchArticleServer
                        .getSearchServerStatistics().getNhreNum();
                searchArticleSteNum += searchArticleServer
                        .getSearchServerStatistics().getSteNum();
                searchArticleCpeNum += searchArticleServer
                        .getSearchServerStatistics().getCpeNum();
                searchArticleUeeNum += searchArticleServer
                        .getSearchServerStatistics().getUeeNum();
                searchArticleIoeNum += searchArticleServer
                        .getSearchServerStatistics().getIoeNum();
                searchArticleHsneNum += searchArticleServer
                        .getSearchServerStatistics().getHsneNum();
                searchArticleHeneNum += searchArticleServer
                        .getSearchServerStatistics().getHeneNum();
                searchArticleHeseNum += searchArticleServer
                        .getSearchServerStatistics().getHeseNum();
                searchArticleIoobeNum += searchArticleServer
                        .getSearchServerStatistics().getIoobeNum();
                searchArticleOeNum += searchArticleServer
                        .getSearchServerStatistics().getOeNum();
            }
            logger.error(
                    "TSAE: JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, IOOBE:{}, OE:{}",
                    searchArticleJeNum, searchArticleHhceNum,
                    searchArticleCteNum, searchArticleNhreNum,
                    searchArticleSteNum, searchArticleCpeNum,
                    searchArticleUeeNum, searchArticleIoeNum,
                    searchArticleHsneNum, searchArticleHeneNum,
                    searchArticleHeseNum, searchArticleIoobeNum,
                    searchArticleOeNum);
        }

        if (schedulerConfiguration.getNlpServers() != null) {
            int nlpJeNum = 0;
            int nlpHhceNum = 0;
            int nlpCteNum = 0;
            int nlpNhreNum = 0;
            int nlpSteNum = 0;
            int nlpUseNum = 0;
            int nlpCpeNum = 0;
            int nlpIoeNum = 0;
            int nlpHsneNum = 0;
            int nlpHeneNum = 0;
            int nlpHeseNum = 0;
            int nlpIoobeNum = 0;
            int nlpOeNum = 0;
            for (NlpServer nlpServer : schedulerConfiguration.getNlpServers()) {
                logger.error(
                        "NLP ID:{}, JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, USE:{}, CPE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, IOOBE:{}, OE:{}",
                        nlpServer.getId(), nlpServer.getNlpServerStatistics()
                                .getJeNum(), nlpServer.getNlpServerStatistics()
                                .getHhceNum(), nlpServer
                                .getNlpServerStatistics().getCteNum(),
                        nlpServer.getNlpServerStatistics().getNhreNum(),
                        nlpServer.getNlpServerStatistics().getSteNum(),
                        nlpServer.getNlpServerStatistics().getUseNum(),
                        nlpServer.getNlpServerStatistics().getCpeNum(),
                        nlpServer.getNlpServerStatistics().getIoeNum(),
                        nlpServer.getNlpServerStatistics().getHsneNum(),
                        nlpServer.getNlpServerStatistics().getHeneNum(),
                        nlpServer.getNlpServerStatistics().getHeseNum(),
                        nlpServer.getNlpServerStatistics().getIoobeNum(),
                        nlpServer.getNlpServerStatistics().getOeNum());

                nlpJeNum += nlpServer.getNlpServerStatistics().getJeNum();
                nlpHhceNum += nlpServer.getNlpServerStatistics().getHhceNum();
                nlpCteNum += nlpServer.getNlpServerStatistics().getCteNum();
                nlpNhreNum += nlpServer.getNlpServerStatistics().getNhreNum();
                nlpSteNum += nlpServer.getNlpServerStatistics().getSteNum();
                nlpUseNum += nlpServer.getNlpServerStatistics().getUseNum();
                nlpCpeNum += nlpServer.getNlpServerStatistics().getCpeNum();
                nlpIoeNum += nlpServer.getNlpServerStatistics().getIoeNum();
                nlpHsneNum += nlpServer.getNlpServerStatistics().getHsneNum();
                nlpHeneNum += nlpServer.getNlpServerStatistics().getHeneNum();
                nlpHeseNum += nlpServer.getNlpServerStatistics().getHeseNum();
                nlpIoobeNum += nlpServer.getNlpServerStatistics().getIoobeNum();
                nlpOeNum += nlpServer.getNlpServerStatistics().getOeNum();
            }
            logger.error(
                    "TNE: JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, USE:{}, CPE:{}, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, IOOBE:{}, OE:{}",
                    nlpJeNum, nlpHhceNum, nlpCteNum, nlpNhreNum, nlpSteNum,
                    nlpUseNum, nlpCpeNum, nlpIoeNum, nlpHsneNum, nlpHeneNum,
                    nlpHeseNum, nlpIoobeNum, nlpOeNum);
        }

        if (schedulerConfiguration.getBiServers() != null) {
            for (BIServer biServer : schedulerConfiguration.getBiServers()) {
                logger.error(
                        "BI ID:{}, HHCE:{}, CTE:{}, CPE:{}, IOE:{}, UEE:{}, IOOBE:{}, OE:{}",
                        biServer.getId(), biServer.getBiServerStatistics()
                                .getHhceNum(), biServer.getBiServerStatistics()
                                .getCteNum(), biServer.getBiServerStatistics()
                                .getCpeNum(), biServer.getBiServerStatistics()
                                .getIoeNum(), biServer.getBiServerStatistics()
                                .getUeeNum(), biServer.getBiServerStatistics()
                                .getIoobeNum(), biServer
                                .getBiServerStatistics().getOeNum());
            }
        }
    }
}

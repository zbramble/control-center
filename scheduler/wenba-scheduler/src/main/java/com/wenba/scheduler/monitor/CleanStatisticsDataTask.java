package com.wenba.scheduler.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;

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
 * clean statistics data task
 * 
 * @author zhangbo
 *
 */
@Component("cleanStatisticsDataTask")
public class CleanStatisticsDataTask {

    // 成员变量
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private SchedulerControllerStatistics schedulerControllerStatistics;
    private static Logger logger = LogManager
            .getLogger(CleanStatisticsDataTask.class);

    // 每天早上0点1秒触发一次,统计数据清0
    @Scheduled(cron = "1 0 0 ? * *")
    public void execute() {

        if (schedulerConfiguration.getCnnServers() != null) {
            for (OcrServer ocrServer : schedulerConfiguration.getCnnServers()) {
                ocrServer.getOcrServerStatistics().setJeNum(0);
                ocrServer.getOcrServerStatistics().setHhceNum(0);
                ocrServer.getOcrServerStatistics().setCteNum(0);
                ocrServer.getOcrServerStatistics().setNhreNum(0);
                ocrServer.getOcrServerStatistics().setSteNum(0);
                ocrServer.getOcrServerStatistics().setCpeNum(0);
                ocrServer.getOcrServerStatistics().setUeeNum(0);
                ocrServer.getOcrServerStatistics().setIoeNum(0);
                ocrServer.getOcrServerStatistics().setHsneNum(0);
                ocrServer.getOcrServerStatistics().setHeneNum(0);
                ocrServer.getOcrServerStatistics().setHeseNum(0);
                ocrServer.getOcrServerStatistics().setOfteNum(0);
                ocrServer.getOcrServerStatistics().setIoobeNum(0);
                ocrServer.getOcrServerStatistics().setOeNum(0);
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
            }
        }

        if (schedulerConfiguration.getJavaServers() != null) {
            for (OcrServer ocrServer : schedulerConfiguration.getJavaServers()) {
                ocrServer.getOcrServerStatistics().setJeNum(0);
                ocrServer.getOcrServerStatistics().setHhceNum(0);
                ocrServer.getOcrServerStatistics().setCteNum(0);
                ocrServer.getOcrServerStatistics().setNhreNum(0);
                ocrServer.getOcrServerStatistics().setSteNum(0);
                ocrServer.getOcrServerStatistics().setCpeNum(0);
                ocrServer.getOcrServerStatistics().setUeeNum(0);
                ocrServer.getOcrServerStatistics().setIoeNum(0);
                ocrServer.getOcrServerStatistics().setHsneNum(0);
                ocrServer.getOcrServerStatistics().setHeneNum(0);
                ocrServer.getOcrServerStatistics().setHeseNum(0);
                ocrServer.getOcrServerStatistics().setOfteNum(0);
                ocrServer.getOcrServerStatistics().setIoobeNum(0);
                ocrServer.getOcrServerStatistics().setOeNum(0);
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
            }
        }

        if (schedulerConfiguration.getSearchServers() != null) {
            for (SearchServer searchServer : schedulerConfiguration
                    .getSearchServers()) {
                searchServer.getSearchServerStatistics().setJeNum(0);
                searchServer.getSearchServerStatistics().setHhceNum(0);
                searchServer.getSearchServerStatistics().setCteNum(0);
                searchServer.getSearchServerStatistics().setNhreNum(0);
                searchServer.getSearchServerStatistics().setSteNum(0);
                searchServer.getSearchServerStatistics().setCpeNum(0);
                searchServer.getSearchServerStatistics().setUeeNum(0);
                searchServer.getSearchServerStatistics().setIoeNum(0);
                searchServer.getSearchServerStatistics().setHsneNum(0);
                searchServer.getSearchServerStatistics().setHeneNum(0);
                searchServer.getSearchServerStatistics().setHeseNum(0);
                searchServer.getSearchServerStatistics().setIoobeNum(0);
                searchServer.getSearchServerStatistics().setOeNum(0);
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
            }
        }

        if (schedulerConfiguration.getSearchHomeworkServers() != null) {
            for (SearchServer searchHomeworkServer : schedulerConfiguration
                    .getSearchHomeworkServers()) {
                searchHomeworkServer.getSearchServerStatistics().setJeNum(0);
                searchHomeworkServer.getSearchServerStatistics().setHhceNum(0);
                searchHomeworkServer.getSearchServerStatistics().setCteNum(0);
                searchHomeworkServer.getSearchServerStatistics().setNhreNum(0);
                searchHomeworkServer.getSearchServerStatistics().setSteNum(0);
                searchHomeworkServer.getSearchServerStatistics().setCpeNum(0);
                searchHomeworkServer.getSearchServerStatistics().setUeeNum(0);
                searchHomeworkServer.getSearchServerStatistics().setIoeNum(0);
                searchHomeworkServer.getSearchServerStatistics().setHsneNum(0);
                searchHomeworkServer.getSearchServerStatistics().setHeneNum(0);
                searchHomeworkServer.getSearchServerStatistics().setHeseNum(0);
                searchHomeworkServer.getSearchServerStatistics().setIoobeNum(0);
                searchHomeworkServer.getSearchServerStatistics().setOeNum(0);
                logger.error(
                        "Search Homework ID:{}, JE:{}, HHCE:{}, CTE:{}, NHRE:{}, STE:{}, CPE:{}, UEE:, IOE:{}, HSNE:{}, HENE:{}, HESE:{}, IOOBE:{}, OE:{}",
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
            }
        }

        if (schedulerConfiguration.getSearchArticleServers() != null) {
            for (SearchServer searchArticleServer : schedulerConfiguration
                    .getSearchArticleServers()) {
                searchArticleServer.getSearchServerStatistics().setJeNum(0);
                searchArticleServer.getSearchServerStatistics().setHhceNum(0);
                searchArticleServer.getSearchServerStatistics().setCteNum(0);
                searchArticleServer.getSearchServerStatistics().setNhreNum(0);
                searchArticleServer.getSearchServerStatistics().setSteNum(0);
                searchArticleServer.getSearchServerStatistics().setCpeNum(0);
                searchArticleServer.getSearchServerStatistics().setUeeNum(0);
                searchArticleServer.getSearchServerStatistics().setIoeNum(0);
                searchArticleServer.getSearchServerStatistics().setHsneNum(0);
                searchArticleServer.getSearchServerStatistics().setHeneNum(0);
                searchArticleServer.getSearchServerStatistics().setHeseNum(0);
                searchArticleServer.getSearchServerStatistics().setIoobeNum(0);
                searchArticleServer.getSearchServerStatistics().setOeNum(0);
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
            }
        }

        if (schedulerConfiguration.getNlpServers() != null) {
            for (NlpServer nlpServer : schedulerConfiguration.getNlpServers()) {
                nlpServer.getNlpServerStatistics().setJeNum(0);
                nlpServer.getNlpServerStatistics().setHhceNum(0);
                nlpServer.getNlpServerStatistics().setCteNum(0);
                nlpServer.getNlpServerStatistics().setNhreNum(0);
                nlpServer.getNlpServerStatistics().setSteNum(0);
                nlpServer.getNlpServerStatistics().setUseNum(0);
                nlpServer.getNlpServerStatistics().setCpeNum(0);
                nlpServer.getNlpServerStatistics().setIoeNum(0);
                nlpServer.getNlpServerStatistics().setHsneNum(0);
                nlpServer.getNlpServerStatistics().setHeneNum(0);
                nlpServer.getNlpServerStatistics().setHeseNum(0);
                nlpServer.getNlpServerStatistics().setIoobeNum(0);
                nlpServer.getNlpServerStatistics().setOeNum(0);
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
            }
        }

        if (schedulerConfiguration.getBiServers() != null) {
            for (BIServer biServer : schedulerConfiguration.getBiServers()) {
                biServer.getBiServerStatistics().setHhceNum(0);
                biServer.getBiServerStatistics().setCteNum(0);
                biServer.getBiServerStatistics().setCpeNum(0);
                biServer.getBiServerStatistics().setIoeNum(0);
                biServer.getBiServerStatistics().setUeeNum(0);
                biServer.getBiServerStatistics().setIoobeNum(0);
                biServer.getBiServerStatistics().setOeNum(0);
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

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(date);
        logger.info("Clean Statistics Data exec at {}!", currentTime);
    }
}

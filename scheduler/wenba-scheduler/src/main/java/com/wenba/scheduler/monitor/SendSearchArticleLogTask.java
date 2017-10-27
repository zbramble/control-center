package com.wenba.scheduler.monitor;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.search.SearchArticleLog;

/**
 * send search article log task
 * 
 * @author zhangbo
 *
 */
@Component("sendSearchArticleLogTask")
public class SendSearchArticleLogTask {

    // 成员变量
    private static Logger sendSearchArticleLoglogger = LogManager
            .getLogger(SendSearchArticleLogTask.class);
    private static Logger logger = LogManager.getLogger("exception");
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private ConcurrentLinkedQueue<SearchArticleLog> searchArticleQueryLogQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchArticleLog> searchArticleAutoCompleteLogQueue;

    // 每15分钟触发一次
    @Scheduled(cron = "0 0/15 * * * *")
    public void execute() {
        if (schedulerConfiguration == null
                || schedulerConfiguration.getSystemDataConfiguration() == null) {
            return;
        }
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(date);

        String yyyyMMdd = currentTime.split(" ")[0];
        String[] hhmmss = currentTime.split(" ")[1].split(":");
        Configuration conf = new Configuration();

        FileSystem articleQueryLogFs = null;
        FSDataOutputStream articleQueryLogOut = null;
        SearchArticleLog articleQueryLog = searchArticleQueryLogQueue.poll();

        FileSystem articleAutoCompleteLogFs = null;
        FSDataOutputStream articleAutoCompleteLogOut = null;
        SearchArticleLog articleAutoCompleteLog = searchArticleAutoCompleteLogQueue
                .poll();
        try {
            // 保存search article query log
            if (articleQueryLog != null) {
                URI articleQueryLogUri = URI.create(schedulerConfiguration
                        .getSystemDataConfiguration()
                        .getArticleQueryLogAddress()
                        + yyyyMMdd + "_" + hhmmss[0] + hhmmss[1] + hhmmss[2]);
                articleQueryLogFs = FileSystem.get(articleQueryLogUri, conf);
                articleQueryLogOut = articleQueryLogFs.create(new Path(
                        articleQueryLogUri));
                sendSearchArticleLoglogger.info(
                        "send search article query log exec at {}!",
                        currentTime);
            }
            while (articleQueryLog != null) {
                articleQueryLogOut
                        .writeUTF("\t"
                                + articleQueryLog.getUid()
                                + "\t"
                                + articleQueryLog.getKeywords()
                                + "\t"
                                + (articleQueryLog.getFilter() != null
                                        && !"".equals(articleQueryLog
                                                .getFilter()) ? articleQueryLog
                                        .getFilter() : "null")
                                + "\t"
                                + (articleQueryLog.getGrade() != null
                                        && !"".equals(articleQueryLog
                                                .getGrade()) ? articleQueryLog
                                        .getGrade() : "null")
                                + "\t"
                                + (articleQueryLog.getTagsLimit() != null ? articleQueryLog
                                        .getTagsLimit() : "null")
                                + "\t"
                                + (articleQueryLog.getPageNo() != null ? articleQueryLog
                                        .getPageNo() : "null")
                                + "\t"
                                + (articleQueryLog.getPageSize() != null ? articleQueryLog
                                        .getPageSize() : "null")
                                + "\t"
                                + (articleQueryLog.getQueryTime() != null ? articleQueryLog
                                        .getQueryTime() : "null")
                                + "\t"
                                + (articleQueryLog.getLastKeywords() != null
                                        && !"".equals(articleQueryLog
                                                .getLastKeywords()) ? articleQueryLog
                                        .getLastKeywords() : "null")
                                + "\t"
                                + (articleQueryLog.getLastQueryTime() != null ? articleQueryLog
                                        .getLastQueryTime() : "null")
                                + "\t"
                                + (articleQueryLog.getSessionId() != null
                                        && !"".equals(articleQueryLog
                                                .getSessionId()) ? articleQueryLog
                                        .getSessionId() : "null")
                                + "\t"
                                + (articleQueryLog.getPageNo() != null
                                        && articleQueryLog.getPageNo() > 0 ? (articleQueryLog
                                        .getPageNo() - 1)
                                        * (articleQueryLog.getPageSize() != null ? articleQueryLog
                                                .getPageSize() : 0)
                                        : "null")
                                + "\t"
                                + (articleQueryLog.getPageSize() != null ? articleQueryLog
                                        .getPageSize() : "null")
                                + "\t"
                                + (articleQueryLog.getSearchResult()
                                        .getSchedulerResult() != null
                                        && !"".equals(articleQueryLog
                                                .getSearchResult().toString()) ? articleQueryLog
                                        .getSearchResult().toString() : "null")
                                + "\t" + System.currentTimeMillis() + "\t"
                                + articleQueryLog.getServerIndex() + "\n");
                articleQueryLog = searchArticleQueryLogQueue.poll();
            }

            // 保存search article auto complete log
            if (articleAutoCompleteLog != null) {
                URI articleAutoCompleteLogUri = URI
                        .create(schedulerConfiguration
                                .getSystemDataConfiguration()
                                .getArticleAutoCompleteLogAddress()
                                + yyyyMMdd
                                + "_"
                                + hhmmss[0]
                                + hhmmss[1]
                                + hhmmss[2]);
                articleAutoCompleteLogFs = FileSystem.get(
                        articleAutoCompleteLogUri, conf);
                articleAutoCompleteLogOut = articleAutoCompleteLogFs
                        .create(new Path(articleAutoCompleteLogUri));
                sendSearchArticleLoglogger.info(
                        "send search article auto complete log exec at {}!",
                        currentTime);
            }
            while (articleAutoCompleteLog != null) {
                articleAutoCompleteLogOut
                        .writeUTF("\t"
                                + (articleAutoCompleteLog.getUid() != null
                                        && !"".equals(articleAutoCompleteLog
                                                .getUid()) ? articleAutoCompleteLog
                                        .getUid() : "null")
                                + "\t"
                                + articleAutoCompleteLog.getKeywords()
                                + "\t"
                                + (articleAutoCompleteLog.getSearchResult()
                                        .getSchedulerResult() != null
                                        && !"".equals(articleAutoCompleteLog
                                                .getSearchResult().toString()) ? articleAutoCompleteLog
                                        .getSearchResult().toString() : "null")
                                + "\t"
                                + (articleAutoCompleteLog.getQueryTime() != null ? articleAutoCompleteLog
                                        .getQueryTime() : "null") + "\t"
                                + System.currentTimeMillis() + "\n");
                articleAutoCompleteLog = searchArticleAutoCompleteLogQueue
                        .poll();
            }
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("SAQL IOE!");
            }
        } catch (IllegalArgumentException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("SAQL IAE!");
            }
        } catch (Exception e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("SAQL OE!");
            }
        } finally {
            try {
                if (articleQueryLogOut != null) {
                    articleQueryLogOut.close();
                }
                if (articleQueryLogFs != null) {
                    articleQueryLogFs.close();
                }
                if (articleAutoCompleteLogOut != null) {
                    articleAutoCompleteLogOut.close();
                }
                if (articleAutoCompleteLogFs != null) {
                    articleAutoCompleteLogFs.close();
                }
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("AQLC IOE!");
                }
            }
        }

    }
}

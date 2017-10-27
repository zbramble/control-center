package com.wenba.scheduler.monitor;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wenba.scheduler.RetrieveHbaseData;
import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.email.EmailParam;
import com.wenba.scheduler.email.EmailUtil;
import com.wenba.scheduler.ocr.OcrHbaseResult;
import com.wenba.scheduler.search.SearchArticleHbaseResult;
import com.wenba.scheduler.search.SearchHbaseResult;
import com.wenba.scheduler.statistics.BIParam;

/**
 * monitor hbase per 5mins
 * 
 * @author zhangbo
 *
 */
@Component("hbaseMonitorTask")
public class HbaseMonitorTask {

    // constants

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger configLogger = LogManager.getLogger("config");
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private SchedulerControllerStatistics schedulerControllerStatistics;
    @Resource
    private RetrieveHbaseData retrieveHbaseData;
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

    @Scheduled(cron = "0 0/5 * * * *")
    public void execute() {
        // 打印当前HbaseOnSwitch和QueryBiModeSwitch的值
        logger.info(
                "ForceHbaseOnSwitch: {}, HbaseOnSwitch: {}, QueryBiModeSwitch: {}",
                schedulerConfiguration.isForceHbaseOnSwitch(),
                schedulerConfiguration.isHbaseOnSwitch(),
                schedulerConfiguration.getQueryBiModeSwitch());

        // 当hbase写入超过阈值关闭后，定时发告警邮件或者写log，直到阈值恢复。
        if (!schedulerConfiguration.isHbaseOnSwitch()
                && "Timer"
                        .equals(schedulerConfiguration.getQueryBiModeSwitch())) {
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

                EmailParam emailParam = new EmailParam();
                emailParam.setSchedulerId(schedulerConfiguration
                        .getSystemDataConfiguration().getSchedulerId());
                emailParam.setReceiverAddress(schedulerConfiguration
                        .getEmailUtil().getConfigFilesMailRecipients());
                emailParam.setSub("hbase exception, ip: " + ip + ", name: "
                        + name);
                emailParam.setMsg("hbase exception!\n\nlocal IP list: "
                        + ipListStr + "\n\nocrHbaseQueue num: "
                        + ocrHbaseQueue.size() + "\nsearchHbaseQueue num: "
                        + searchHbaseQueue.size() + "\nqueryBiQueue num: "
                        + queryBiQueue.size()
                        + "\nocrHbaseWordSearchQueue num: "
                        + ocrHbaseWordSearchQueue.size()
                        + "\nsearchHbaseWordSearchQueue num: "
                        + searchHbaseWordSearchQueue.size()
                        + "\nsearchArticleHbaseQueue num: "
                        + searchArticleHbaseQueue.size()
                        + "\nhbaseMonitorQueue num: "
                        + hbaseMonitorQueue.size()
                        + "\nmiguOcrHbaseQueue num: "
                        + miguOcrHbaseQueue.size()
                        + "\nmiguSearchHbaseQueue num: "
                        + miguSearchHbaseQueue.size()
                        + "\nsdkOcrHbaseQueue num: " + sdkOcrHbaseQueue.size()
                        + "\nsdkSearchHbaseQueue num: "
                        + sdkSearchHbaseQueue.size());

                EmailUtil emailUtil = schedulerConfiguration.getEmailUtil();
                if (emailUtil != null) {
                    emailUtil.sendEmail(emailParam);
                }
            } else {
                configLogger
                        .error("hbase exception!\nocrHbaseQueue num: {}, searchHbaseQueue num: {}, queryBiQueue num: {}, ocrHbaseWordSearchQueue num: {}, searchHbaseWordSearchQueue num: {}, searchArticleHbaseQueue num: {}, hbaseMonitorQueue num: {}, miguOcrHbaseQueue num: {}, miguSearchHbaseQueue num: {}, sdkOcrHbaseQueue num: {}, sdkuSearchHbaseQueue num: {}",
                                ocrHbaseQueue.size(), searchHbaseQueue.size(),
                                queryBiQueue.size(),
                                ocrHbaseWordSearchQueue.size(),
                                searchHbaseWordSearchQueue.size(),
                                searchArticleHbaseQueue.size(),
                                hbaseMonitorQueue.size(),
                                miguOcrHbaseQueue.size(),
                                miguSearchHbaseQueue.size(),
                                sdkOcrHbaseQueue.size(),
                                sdkSearchHbaseQueue.size());
            }
        }

        // 读取监控队列里的fid，检查是否已经写入hbase
        Long fid = hbaseMonitorQueue.poll();
        if (fid != null) {
            String ocrResult = null;
            Future<String> futureOcrResult = null;
            boolean hasNoOcrResult = false;
            try {
                futureOcrResult = retrieveHbaseData
                        .retrieveOcrResultFromHbase(fid);
                ocrResult = futureOcrResult.get(schedulerConfiguration
                        .getTimeoutConfiguration().getHbaseTimeout(),
                        TimeUnit.MILLISECONDS);
                if (ocrResult == null || "".equals(ocrResult)) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase monitor no ocr result", fid);
                    }
                    hasNoOcrResult = true;
                }
            } catch (TimeoutException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase monitor ocr TE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
                hasNoOcrResult = true;
            } catch (ExecutionException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase monitor ocr EE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
                hasNoOcrResult = true;
            } catch (InterruptedException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase monitor ocr IE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
                hasNoOcrResult = true;
            }

            Future<List<com.xueba100.mining.common.SearchResult>> futureSearchResult = null;
            boolean hasNoSearchResult = false;
            try {
                futureSearchResult = retrieveHbaseData
                        .retrieveSearchResultFromHbase(fid);
                List<com.xueba100.mining.common.SearchResult> searchResultList = futureSearchResult
                        .get(schedulerConfiguration.getTimeoutConfiguration()
                                .getHbaseTimeout(), TimeUnit.MILLISECONDS);
                if (searchResultList == null || searchResultList.size() <= 0) {
                    // no search result in hbase
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase monitor no search result", fid);
                    }
                    hasNoSearchResult = true;
                }
            } catch (TimeoutException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase monitor search TE", fid);
                }
                if (futureSearchResult != null) {
                    if (!futureSearchResult.isCancelled()) {
                        futureSearchResult.cancel(true);
                    }
                }
                hasNoSearchResult = true;
            } catch (ExecutionException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase monitor search EE", fid);
                }
                if (futureSearchResult != null) {
                    if (!futureSearchResult.isCancelled()) {
                        futureSearchResult.cancel(true);
                    }
                }
                hasNoSearchResult = true;
            } catch (InterruptedException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase monitor search IE", fid);
                }
                if (futureSearchResult != null) {
                    if (!futureSearchResult.isCancelled()) {
                        futureSearchResult.cancel(true);
                    }
                }
                hasNoSearchResult = true;
            }

            logger.info(
                    "hbase monitor fid: {}, hasNoOcrResult: {}, hasNoSearchResult: {}",
                    fid, hasNoOcrResult, hasNoSearchResult);

            if ((hasNoOcrResult || hasNoSearchResult)) {
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

                    EmailParam emailParam = new EmailParam();
                    emailParam.setSchedulerId(schedulerConfiguration
                            .getSystemDataConfiguration().getSchedulerId());
                    emailParam.setReceiverAddress(schedulerConfiguration
                            .getEmailUtil().getConfigFilesMailRecipients());
                    emailParam.setSub("hbase monitor exception, ip: " + ip
                            + ", name: " + name);
                    if (hasNoOcrResult && hasNoSearchResult) {
                        emailParam
                                .setMsg("hbase monitor exception!\n\nlocal IP list: "
                                        + ipListStr
                                        + "\n\nfid: "
                                        + fid
                                        + " no ocr and search result!");
                    } else if (hasNoOcrResult) {
                        emailParam
                                .setMsg("hbase monitor exception!\n\nlocal IP list: "
                                        + ipListStr
                                        + "\n\nfid: "
                                        + fid
                                        + " no ocr result!");
                    } else {
                        emailParam
                                .setMsg("hbase monitor exception!\n\nlocal IP list: "
                                        + ipListStr
                                        + "\n\nfid: "
                                        + fid
                                        + " no search result!");
                    }

                    EmailUtil emailUtil = schedulerConfiguration.getEmailUtil();
                    if (emailUtil != null) {
                        emailUtil.sendEmail(emailParam);
                    }
                } else {
                    if (hasNoOcrResult && hasNoSearchResult) {
                        configLogger
                                .error("hbase monitor exception!\n\nfid: {} no ocr and search result!",
                                        fid);
                    } else if (hasNoOcrResult) {
                        configLogger
                                .error("hbase monitor exception!\n\nfid: {} no ocr result!",
                                        fid);
                    } else {
                        configLogger
                                .error("hbase monitor exception!\n\nfid: {} no search result!",
                                        fid);
                    }
                }
            }
        }
    }
}

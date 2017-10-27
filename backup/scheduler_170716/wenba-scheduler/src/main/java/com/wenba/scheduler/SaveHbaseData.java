package com.wenba.scheduler;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;

import com.wenba.scheduler.AbstractServer.ServerType;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.ocr.OcrHbaseResult;
import com.wenba.scheduler.search.SearchArticleHbaseResult;
import com.wenba.scheduler.search.SearchHbaseResult;
import com.xueba100.mining.common.HbClient;

/**
 * @author zhangbo
 *
 */
public class SaveHbaseData {

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
    private ConcurrentLinkedQueue<OcrHbaseResult> miguOcrHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchHbaseResult> miguSearchHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<OcrHbaseResult> miguOcrHbaseWordSearchQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchHbaseResult> miguSearchHbaseWordSearchQueue;
    @Resource
    private ConcurrentLinkedQueue<OcrHbaseResult> sdkOcrHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<SearchHbaseResult> sdkSearchHbaseQueue;
    @Resource
    private ConcurrentLinkedQueue<Long> hbaseMonitorQueue;
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");

    /**
     * save ocr hbase data
     */
    @Async
    public void saveOcrHbaseData() {
        OcrHbaseResult ocrHbaseResult = ocrHbaseQueue.poll();
        while (ocrHbaseResult != null) {
            schedulerControllerStatistics.decrementAndGetOcrHbaseResultNum();
            try {
                // save ocr result
                long hbaseStartTime = System.currentTimeMillis();
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putOcrResult(ocrHbaseResult.getUid(), ocrHbaseResult
                        .getFid(), ocrHbaseResult.getOcrType().getValue(),
                        ocrHbaseResult.getSchedulerResult(), ocrHbaseResult
                                .getRotate());
                if (schedulerConfiguration.isBkHbaseOnSwitch()) {
                    HbClient hbClientBk = schedulerConfiguration
                            .getHbClientBk();
                    hbClientBk.putOcrResult(ocrHbaseResult.getUid(),
                            ocrHbaseResult.getFid(), ocrHbaseResult
                                    .getOcrType().getValue(), ocrHbaseResult
                                    .getSchedulerResult(), ocrHbaseResult
                                    .getRotate());
                    if (schedulerConfiguration.getDebugSwitchConfiguration()
                            .isBkHbaseDebugSwitch()) {
                        debugLogger.info("save bk ocr: {} {} {}",
                                ocrHbaseResult.getUid(), ocrHbaseResult
                                        .getFid(), ocrHbaseResult
                                        .getOcrServer().getId());
                    }
                }
                long hbaseStopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgOcrHbaseExecTimeDebugSwitch()) {
                    schedulerControllerStatistics
                            .addAndGetOcrHbaseExecTime(hbaseStopTime
                                    - hbaseStartTime);
                    schedulerControllerStatistics
                            .incrementAndGetOcrHbaseExecTimeNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isOcrHbaseExecTimeDebugSwitch()) {
                    debugLogger.info("{} {} {} OHET: {}",
                            ocrHbaseResult.getUid(), ocrHbaseResult.getFid(),
                            ocrHbaseResult.getOcrServer().getId(),
                            (hbaseStopTime - hbaseStartTime));
                }
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} SO IOE", ocrHbaseResult.getUid(),
                            ocrHbaseResult.getFid(), ocrHbaseResult
                                    .getOcrServer().getId());
                }
                ocrHbaseResult.getOcrServer().getOcrServerStatistics()
                        .incrementAndGetIoeNum();
            }
            ocrHbaseResult = ocrHbaseQueue.poll();
        }
    }

    /**
     * save search hbase data
     */
    @Async
    public void saveSearchHbaseData() {
        SearchHbaseResult searchHbaseResult = searchHbaseQueue.poll();
        while (searchHbaseResult != null) {
            schedulerControllerStatistics.decrementAndGetSearchHbaseResultNum();
            try {
                // save search result
                long hbaseStartTime = System.currentTimeMillis();
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putSearchResult(searchHbaseResult.getUid(),
                        searchHbaseResult.getFid(),
                        searchHbaseResult.getSearchResultList());
                if (schedulerConfiguration.isBkHbaseOnSwitch()) {
                    HbClient hbClientBk = schedulerConfiguration
                            .getHbClientBk();
                    hbClientBk.putSearchResult(searchHbaseResult.getUid(),
                            searchHbaseResult.getFid(),
                            searchHbaseResult.getSearchResultList());
                    if (schedulerConfiguration.getDebugSwitchConfiguration()
                            .isBkHbaseDebugSwitch()) {
                        debugLogger.info("save bk search: {} {} {}",
                                searchHbaseResult.getUid(), searchHbaseResult
                                        .getFid(), searchHbaseResult
                                        .getSearchServer().getId());
                    }
                }
                long hbaseStopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgSearchHbaseExecTimeDebugSwitch()) {
                    schedulerControllerStatistics
                            .addAndGetSearchHbaseExecTime(hbaseStopTime
                                    - hbaseStartTime);
                    schedulerControllerStatistics
                            .incrementAndGetSearchHbaseExecTimeNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isSearchHbaseExecTimeDebugSwitch()) {
                    debugLogger.info("{} {} {} SHET: {}", searchHbaseResult
                            .getUid(), searchHbaseResult.getFid(),
                            searchHbaseResult.getSearchServer().getId(),
                            (hbaseStopTime - hbaseStartTime));
                }
                if ((hbaseStopTime - schedulerConfiguration
                        .getSchedulerStartTime())
                        % schedulerConfiguration.getTimeoutConfiguration()
                                .getHbaseMonitorTime() == 0) {
                    debugLogger
                            .info("hbase monitor fid: {}, stop time: {}, start time: {}",
                                    searchHbaseResult.getFid(), hbaseStopTime,
                                    schedulerConfiguration
                                            .getSchedulerStartTime());
                    hbaseMonitorQueue.offer(searchHbaseResult.getFid());
                }
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} SS IOE", searchHbaseResult.getUid(),
                            searchHbaseResult.getFid(), searchHbaseResult
                                    .getSearchServer().getId());
                }
                searchHbaseResult.getSearchServer().getSearchServerStatistics()
                        .incrementAndGetIoeNum();
            }
            searchHbaseResult = searchHbaseQueue.poll();
        }
    }

    /**
     * save word search ocr hbase data
     */
    @Async
    public void saveWordSearchOcrHbaseData() {
        OcrHbaseResult ocrHbaseResult = ocrHbaseWordSearchQueue.poll();
        while (ocrHbaseResult != null) {
            schedulerControllerStatistics.decrementAndGetOcrHbaseResultNum();
            try {
                // save ocr result
                long hbaseStartTime = System.currentTimeMillis();
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putOcrResult(ocrHbaseResult.getUid(),
                        ocrHbaseResult.getFid(),
                        ServerType.WORD_SEARCH.getValue(),
                        ocrHbaseResult.getSchedulerResult());
                if (schedulerConfiguration.isBkHbaseOnSwitch()) {
                    HbClient hbClientBk = schedulerConfiguration
                            .getHbClientBk();
                    hbClientBk.putOcrResult(ocrHbaseResult.getUid(),
                            ocrHbaseResult.getFid(),
                            ServerType.WORD_SEARCH.getValue(),
                            ocrHbaseResult.getSchedulerResult());
                    if (schedulerConfiguration.getDebugSwitchConfiguration()
                            .isBkHbaseDebugSwitch()) {
                        debugLogger.info("save bk search: {} {}",
                                ocrHbaseResult.getUid(),
                                ocrHbaseResult.getFid());
                    }
                }
                long hbaseStopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgOcrHbaseExecTimeDebugSwitch()) {
                    schedulerControllerStatistics
                            .addAndGetOcrHbaseExecTime(hbaseStopTime
                                    - hbaseStartTime);
                    schedulerControllerStatistics
                            .incrementAndGetOcrHbaseExecTimeNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isOcrHbaseExecTimeDebugSwitch()) {
                    debugLogger.info("{} {} WSOHET: {}",
                            ocrHbaseResult.getUid(), ocrHbaseResult.getFid(),
                            (hbaseStopTime - hbaseStartTime));
                }
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} SWSO IOE", ocrHbaseResult.getUid(),
                            ocrHbaseResult.getFid());
                }
            }
            ocrHbaseResult = ocrHbaseWordSearchQueue.poll();
        }
    }

    /**
     * save word search search hbase data
     */
    @Async
    public void saveWordSearchSearchHbaseData() {
        SearchHbaseResult searchHbaseResult = searchHbaseWordSearchQueue.poll();
        while (searchHbaseResult != null) {
            schedulerControllerStatistics.decrementAndGetSearchHbaseResultNum();
            try {
                // save search result
                long hbaseStartTime = System.currentTimeMillis();
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putSearchResult(searchHbaseResult.getUid(),
                        searchHbaseResult.getFid(),
                        searchHbaseResult.getSearchResultList());
                if (schedulerConfiguration.isBkHbaseOnSwitch()) {
                    HbClient hbClientBk = schedulerConfiguration
                            .getHbClientBk();
                    hbClientBk.putSearchResult(searchHbaseResult.getUid(),
                            searchHbaseResult.getFid(),
                            searchHbaseResult.getSearchResultList());
                }
                long hbaseStopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgSearchHbaseExecTimeDebugSwitch()) {
                    schedulerControllerStatistics
                            .addAndGetSearchHbaseExecTime(hbaseStopTime
                                    - hbaseStartTime);
                    schedulerControllerStatistics
                            .incrementAndGetSearchHbaseExecTimeNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isSearchHbaseExecTimeDebugSwitch()) {
                    debugLogger.info("{} {} WSSHET: {}",
                            searchHbaseResult.getUid(),
                            searchHbaseResult.getFid(),
                            (hbaseStopTime - hbaseStartTime));
                }
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} SWSS IOE", searchHbaseResult.getUid(),
                            searchHbaseResult.getFid());
                }
            }
            searchHbaseResult = searchHbaseWordSearchQueue.poll();
        }
    }

    /**
     * save search article hbase data
     */
    @Async
    public void saveSearchArticleHbaseData() {
        SearchArticleHbaseResult searchArticleHbaseResult = searchArticleHbaseQueue
                .poll();
        while (searchArticleHbaseResult != null) {
            schedulerControllerStatistics
                    .decrementAndGetSearchArticleHbaseResultNum();
            try {
                // save search article result
                long hbaseStartTime = System.currentTimeMillis();
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putArticleResult(
                        Integer.parseInt(searchArticleHbaseResult.getUid()),
                        searchArticleHbaseResult.getKeywords());
                long hbaseStopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgSearchArticleHbaseExecTimeDebugSwitch()) {
                    schedulerControllerStatistics
                            .addAndGetSearchArticleHbaseExecTime(hbaseStopTime
                                    - hbaseStartTime);
                    schedulerControllerStatistics
                            .incrementAndGetSearchArticleHbaseExecTimeNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isSearchArticleHbaseExecTimeDebugSwitch()) {
                    debugLogger.info("{} SA {} SAHET: {}",
                            searchArticleHbaseResult.getUid(),
                            searchArticleHbaseResult.getSearchArticleServer()
                                    .getId(), (hbaseStopTime - hbaseStartTime));
                }
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} SA {} SSA IOE", searchArticleHbaseResult
                            .getUid(), searchArticleHbaseResult
                            .getSearchArticleServer().getId());
                }
                searchArticleHbaseResult.getSearchArticleServer()
                        .getSearchServerStatistics().incrementAndGetIoeNum();
            }
            searchArticleHbaseResult = searchArticleHbaseQueue.poll();
        }
    }

    /**
     * save migu ocr hbase data
     */
    @Async
    public void saveMiguOcrHbaseData() {
        OcrHbaseResult miguOcrHbaseResult = miguOcrHbaseQueue.poll();
        while (miguOcrHbaseResult != null) {
            schedulerControllerStatistics
                    .decrementAndGetMiguOcrHbaseResultNum();
            try {
                // save migu ocr result
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putMiguOcrResult(miguOcrHbaseResult.getUid(),
                        miguOcrHbaseResult.getFid(), miguOcrHbaseResult
                                .getOcrType().getValue(), miguOcrHbaseResult
                                .getSchedulerResult(), miguOcrHbaseResult
                                .getRotate());
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} SMO IOE", miguOcrHbaseResult
                            .getUid(), miguOcrHbaseResult.getFid(),
                            miguOcrHbaseResult.getOcrServer().getId());
                }
                miguOcrHbaseResult.getOcrServer().getOcrServerStatistics()
                        .incrementAndGetIoeNum();
            }
            miguOcrHbaseResult = miguOcrHbaseQueue.poll();
        }
    }

    /**
     * save migu search hbase data
     */
    @Async
    public void saveMiguSearchHbaseData() {
        SearchHbaseResult miguSearchHbaseResult = miguSearchHbaseQueue.poll();
        while (miguSearchHbaseResult != null) {
            schedulerControllerStatistics
                    .decrementAndGetMiguSearchHbaseResultNum();
            try {
                // save migu search result
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putMiguSearchResult(miguSearchHbaseResult.getUid(),
                        miguSearchHbaseResult.getFid(),
                        miguSearchHbaseResult.getSearchResultList());
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} SMS IOE",
                            miguSearchHbaseResult.getUid(),
                            miguSearchHbaseResult.getFid(),
                            miguSearchHbaseResult.getSearchServer().getId());
                }
                miguSearchHbaseResult.getSearchServer()
                        .getSearchServerStatistics().incrementAndGetIoeNum();
            }
            miguSearchHbaseResult = miguSearchHbaseQueue.poll();
        }
    }

    /**
     * save migu word search ocr hbase data
     */
    @Async
    public void saveMiguWordSearchOcrHbaseData() {
        OcrHbaseResult miguOcrHbaseResult = miguOcrHbaseWordSearchQueue.poll();
        while (miguOcrHbaseResult != null) {
            schedulerControllerStatistics
                    .decrementAndGetMiguOcrHbaseResultNum();
            try {
                // save migu ocr result
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putMiguOcrResult(miguOcrHbaseResult.getUid(),
                        miguOcrHbaseResult.getFid(),
                        ServerType.WORD_SEARCH.getValue(),
                        miguOcrHbaseResult.getSchedulerResult(), 0);
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} SMWSO IOE",
                            miguOcrHbaseResult.getUid(),
                            miguOcrHbaseResult.getFid());
                }
            }
            miguOcrHbaseResult = miguOcrHbaseWordSearchQueue.poll();
        }
    }

    /**
     * save migu word search search hbase data
     */
    @Async
    public void saveMiguWordSearchSearchHbaseData() {
        SearchHbaseResult miguSearchHbaseResult = miguSearchHbaseWordSearchQueue
                .poll();
        while (miguSearchHbaseResult != null) {
            schedulerControllerStatistics
                    .decrementAndGetMiguSearchHbaseResultNum();
            try {
                // save migu search result
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putMiguSearchResult(miguSearchHbaseResult.getUid(),
                        miguSearchHbaseResult.getFid(),
                        miguSearchHbaseResult.getSearchResultList());
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} SMWSS IOE",
                            miguSearchHbaseResult.getUid(),
                            miguSearchHbaseResult.getFid());
                }
            }
            miguSearchHbaseResult = miguSearchHbaseWordSearchQueue.poll();
        }
    }

    /**
     * save sdk ocr hbase data
     */
    @Async
    public void saveSdkOcrHbaseData() {
        OcrHbaseResult sdkOcrHbaseResult = sdkOcrHbaseQueue.poll();
        while (sdkOcrHbaseResult != null) {
            schedulerControllerStatistics.decrementAndGetSdkOcrHbaseResultNum();
            try {
                // save sdk ocr result
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putSdkOcrResult(sdkOcrHbaseResult.getUid(),
                        sdkOcrHbaseResult.getFid(), sdkOcrHbaseResult
                                .getOcrType().getValue(), sdkOcrHbaseResult
                                .getSchedulerResult(), sdkOcrHbaseResult
                                .getRotate());
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} SSDKO IOE", sdkOcrHbaseResult
                            .getUid(), sdkOcrHbaseResult.getFid(),
                            sdkOcrHbaseResult.getOcrServer().getId());
                }
                sdkOcrHbaseResult.getOcrServer().getOcrServerStatistics()
                        .incrementAndGetIoeNum();
            }
            sdkOcrHbaseResult = sdkOcrHbaseQueue.poll();
        }
    }

    /**
     * save sdk search hbase data
     */
    @Async
    public void saveSdkSearchHbaseData() {
        SearchHbaseResult sdkSearchHbaseResult = sdkSearchHbaseQueue.poll();
        while (sdkSearchHbaseResult != null) {
            schedulerControllerStatistics
                    .decrementAndGetSdkSearchHbaseResultNum();
            try {
                // save sdk search result
                HbClient hbClient = schedulerConfiguration.getHbClient();
                hbClient.putSdkSearchResult(sdkSearchHbaseResult.getUid(),
                        sdkSearchHbaseResult.getFid(),
                        sdkSearchHbaseResult.getSearchResultList());
            } catch (IOException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} {} {} SSDKS IOE", sdkSearchHbaseResult
                            .getUid(), sdkSearchHbaseResult.getFid(),
                            sdkSearchHbaseResult.getSearchServer().getId());
                }
                sdkSearchHbaseResult.getSearchServer()
                        .getSearchServerStatistics().incrementAndGetIoeNum();
            }
            sdkSearchHbaseResult = sdkSearchHbaseQueue.poll();
        }
    }
}

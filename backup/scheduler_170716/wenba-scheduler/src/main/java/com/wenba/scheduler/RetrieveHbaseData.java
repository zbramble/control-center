package com.wenba.scheduler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import com.wenba.scheduler.config.SchedulerConfiguration;
import com.xueba100.mining.common.Feed;
import com.xueba100.mining.common.HbClient;

/**
 * @author zhangbo
 *
 */
public class RetrieveHbaseData {

    // 成员变量
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    private static Logger logger = LogManager.getLogger("exception");

    /**
     * retrieve ocr result from hbase
     */
    @Async
    public Future<String> retrieveOcrResultFromHbase(long fid) {
        String ocrResult = null;
        try {
            HbClient hbClient = schedulerConfiguration.getHbClient();
            ocrResult = hbClient.getOcrResult(fid);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} ROR IOE", fid);
            }
            return null;
        }
        return new AsyncResult<String>(ocrResult);
    }

    /**
     * retrieve rotate from hbase
     */
    @Async
    public Future<Integer> retrieveRotateFromHbase(long fid) {
        Integer rotate = 0;
        try {
            HbClient hbClient = schedulerConfiguration.getHbClient();
            rotate = hbClient.getRotate(fid);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} RR IOE", fid);
            }
            return null;
        }
        return new AsyncResult<Integer>(rotate);
    }

    /**
     * retrieve search result from hbase
     */
    @Async
    public Future<List<com.xueba100.mining.common.SearchResult>> retrieveSearchResultFromHbase(
            long fid) {
        List<com.xueba100.mining.common.SearchResult> searchResultList = null;
        try {
            HbClient hbClient = schedulerConfiguration.getHbClient();
            searchResultList = hbClient.getSearchResult(fid);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} RSR IOE", fid);
            }
            return null;
        }
        return new AsyncResult<List<com.xueba100.mining.common.SearchResult>>(
                searchResultList);
    }

    /**
     * retrieve feeds from hbase
     */
    @Async
    public Future<List<Feed>> retrieveFeedsFromHbase(int uid) {
        List<Feed> feedList = null;
        try {
            HbClient hbClient = schedulerConfiguration.getHbClient();
            feedList = hbClient.scanWithUidFid(uid, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} RFS IOE", uid);
            }
            return null;
        }
        return new AsyncResult<List<Feed>>(feedList);
    }

    /**
     * retrieve feed from hbase
     */
    @Async
    public Future<Feed> retrieveFeedFromHbase(long fid) {
        Feed feed = null;
        try {
            HbClient hbClient = schedulerConfiguration.getHbClient();
            feed = hbClient.getFeed(fid);
        } catch (IOException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} RF IOE", fid);
            }
            return null;
        }
        return new AsyncResult<Feed>(feed);
    }
}

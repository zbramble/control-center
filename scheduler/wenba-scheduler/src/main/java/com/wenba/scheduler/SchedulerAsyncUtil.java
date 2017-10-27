package com.wenba.scheduler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.wenba.scheduler.config.SchedulerConfiguration;
import com.xueba100.mining.common.ArticleResult;
import com.xueba100.mining.common.Feed;
import com.xueba100.mining.common.HbClient;
import com.xueba100.mining.common.SubjectFilterInfo;

/**
 * @author zhangbo
 *
 */
public class SchedulerAsyncUtil {

    // constants
    private static final String SEARCH_RESULT_QUESTIONS = "questions";
    private static final String SEARCH_RESULT_SIMILARITY = "similarity";
    private static final String SEARCH_RESULT_ID = "id";
    private static final String SEARCH_RESULT_STEM_HTML = "stem_html";
    private static final String SEARCH_RESULT_ANSWER = "answer";
    private static final String SEARCH_RESULT_POINTS = "points";
    private static final String SEARCH_RESULT_SUBJECT = "subject";
    private static final String SEARCH_RESULT_SUBJECT_ORIGINAL = "subject_original";
    private static final String SEARCH_RESULT_ORIGIN = "origin";
    private static final int NUM_999999 = 999999;

    // 成员变量
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private ConcurrentLinkedQueue<BasicNameValuePair> queryResultToRedisQueue;
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");

    /**
     * save query result to redis
     */
    @Async
    public void saveQueryResultToRedis() {
        Jedis jedis = null;
        int getRedisTime = 0;
        while (getRedisTime < SchedulerConstants.GET_REDIS_TIMES) {
            try {
                jedis = schedulerConfiguration.getJedisPool().getResource();
                break;
            } catch (JedisConnectionException e) {
                ++getRedisTime;
                try {
                    Thread.sleep(SchedulerConstants.SLEEP_TIME);
                } catch (InterruptedException ie) {
                    logger.error("Thread ID: {}, IE", Thread.currentThread()
                            .getId());
                    continue;
                }
                logger.error("Thread ID: {}, JCE", Thread.currentThread()
                        .getId());
                continue;
            }
        }

        if (jedis != null) {
            BasicNameValuePair queryResult = queryResultToRedisQueue.poll();
            int redisExpireSeconds = schedulerConfiguration
                    .getSystemDataConfiguration().getRedisExpireSeconds();
            while (queryResult != null) {
                JSONObject redisResultJson = new JSONObject();
                JSONObject queryResultJson = JSONObject.fromObject(queryResult
                        .getValue());
                redisResultJson.put("ocrResult",
                        queryResultJson.getString("ocrResult"));
                redisResultJson.put("rotate", queryResultJson.getInt("rotate"));
                JSONObject searchResultJson = JSONObject
                        .fromObject(queryResultJson.getString("searchResult"));
                JSONArray questionsJsonArray = searchResultJson
                        .getJSONArray(SEARCH_RESULT_QUESTIONS);
                JSONArray searchResultJsonArray = new JSONArray();
                for (int i = 0; i < questionsJsonArray.size(); ++i) {
                    JSONObject questionJson = new JSONObject();
                    questionJson.put("id", questionsJsonArray.getJSONObject(i)
                            .getInt(SEARCH_RESULT_ID));
                    questionJson.put(
                            "similarity",
                            questionsJsonArray.getJSONObject(i).getDouble(
                                    SEARCH_RESULT_SIMILARITY));
                    questionJson.put("subject", questionsJsonArray
                            .getJSONObject(i).getString(SEARCH_RESULT_SUBJECT));
                    questionJson.put(
                            "subject_original",
                            questionsJsonArray.getJSONObject(i).getString(
                                    SEARCH_RESULT_SUBJECT_ORIGINAL));
                    questionJson.put("origin", questionsJsonArray
                            .getJSONObject(i).getString(SEARCH_RESULT_ORIGIN));
                    searchResultJsonArray.add(questionJson);
                }
                redisResultJson.put("searchResult", searchResultJsonArray);

                redisResultJson.put("aid", questionsJsonArray.getJSONObject(0)
                        .getInt(SEARCH_RESULT_ID));
                redisResultJson.put(
                        "answer",
                        questionsJsonArray.getJSONObject(0).getString(
                                SEARCH_RESULT_STEM_HTML)
                                + questionsJsonArray.getJSONObject(0)
                                        .getString(SEARCH_RESULT_ANSWER));
                redisResultJson.put("pure_stem", questionsJsonArray
                        .getJSONObject(0).getString(SEARCH_RESULT_STEM_HTML));
                redisResultJson.put("pure_answer", questionsJsonArray
                        .getJSONObject(0).getString(SEARCH_RESULT_ANSWER));
                redisResultJson.put(
                        "points",
                        questionsJsonArray.getJSONObject(0).getString(
                                SEARCH_RESULT_POINTS));

                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isRedisDebugSwitch()) {
                    debugLogger.info("fid: {}, redisResult: {}",
                            queryResult.getName(), redisResultJson.toString());
                }
                jedis.setex(queryResult.getName(), redisExpireSeconds,
                        redisResultJson.toString());

                queryResult = queryResultToRedisQueue.poll();
            }
            jedis.close();
        } else {
            logger.error("Thread ID: {}, Get Redis Fail!", Thread
                    .currentThread().getId());
            return;
        }
    }

    /**
     * migrate hbase feed table
     * 
     * @param startFid
     *            long
     * @param endFid
     *            long
     */
    @Async
    public void migrateFeedTable(long startFid, long endFid) {
        HbClient hbClientSrc = schedulerConfiguration.getHbClient();
        HbClient hbClientDest = schedulerConfiguration.getHbClientBk();
        int srcCount = 0;
        int destCount = 0;
        for (long fid = startFid; fid <= endFid; ++fid) {
            try {
                Feed feed = hbClientSrc.getFeed(fid);
                if (feed != null) {
                    ++srcCount;
                    logger.info("migrate feed table, succeed read fid: {}", fid);
                    hbClientDest.setFeed(feed);
                    ++destCount;
                    logger.info("migrate feed table, succeed write fid: {}",
                            fid);
                } else {
                    logger.error("migrate feed table, null fid: {}", fid);
                    continue;
                }
            } catch (Exception e) {
                logger.error("migrate feed table, exception fid: {}", fid);
                continue;
            }
        }

        if (srcCount != destCount) {
            logger.error(
                    "migrate feed table, error total count, should be {}, actually {}",
                    srcCount, destCount);
        } else {
            logger.warn(
                    "migrate feed table, total count, startFid: {}, endFid: {}, count: {}",
                    startFid, endFid, srcCount);
        }
    }

    /**
     * migrate hbase user feed table
     * 
     * @param startUid
     *            int
     * @param endUid
     *            int
     * @param startFid
     *            long
     * @param endFid
     *            long
     */
    @Async
    public void migrateUserFeedTable(int startUid, int endUid, long startFid,
            long endFid) {
        HbClient hbClientSrc = schedulerConfiguration.getHbClient();
        HbClient hbClientDest = schedulerConfiguration.getHbClientBk();
        int srcCount = 0;
        int destCount = 0;
        for (int uid = startUid; uid <= endUid; ++uid) {
            for (long fid = startFid; fid <= endFid; ++fid) {
                try {
                    Feed userFeed = hbClientSrc.getUserFeed(uid, fid);
                    if (userFeed != null) {
                        ++srcCount;
                        logger.info(
                                "migrate user feed table, succeed read uid: {}, fid: {}",
                                uid, fid);
                        hbClientDest.setUserFeed(userFeed);
                        ++destCount;
                        logger.info(
                                "migrate user feed table, succeed write uid: {}, fid: {}",
                                uid, fid);
                    } else {
                        logger.error(
                                "migrate user feed table, null uid: {}, fid: {}",
                                uid, fid);
                        continue;
                    }
                } catch (Exception e) {
                    logger.error(
                            "migrate user feed table, exception uid: {}, fid: {}",
                            uid, fid);
                    continue;
                }
            }
        }

        if (srcCount != destCount) {
            logger.error(
                    "migrate user feed table, error total count, should be {}, actually {}",
                    srcCount, destCount);
        } else {
            logger.warn(
                    "migrate user feed table, total count, startUid: {}, endUid: {}, count: {}",
                    startUid, endUid, srcCount);
        }
    }

    /**
     * migrate hbase time feed table
     * 
     * @param startTime
     *            long
     * @param endTime
     *            long
     * @param startFid
     *            long
     * @param endFid
     *            long
     */
    @Async
    public void migrateTimeFeedTable(long startTime, long endTime,
            long startFid, long endFid) {
        HbClient hbClientSrc = schedulerConfiguration.getHbClient();
        HbClient hbClientDest = schedulerConfiguration.getHbClientBk();
        int srcCount = 0;
        int destCount = 0;
        for (long time = startTime; time <= endTime; ++time) {
            for (long fid = startFid; fid <= endFid; ++fid) {
                try {
                    Feed timeFeed = hbClientSrc.getTimeFeed(time, fid);
                    if (timeFeed != null) {
                        ++srcCount;
                        logger.info(
                                "migrate time feed table, succeed read time: {}, fid: {}",
                                time, fid);
                        hbClientDest.setTimeFeed(timeFeed);
                        ++destCount;
                        logger.info(
                                "migrate time feed table, succeed write time: {}, fid: {}",
                                time, fid);
                    } else {
                        logger.error(
                                "migrate time feed table, null time: {}, fid: {}",
                                time, fid);
                        continue;
                    }
                } catch (Exception e) {
                    logger.error(
                            "migrate time feed table, exception time: {}, fid: {}",
                            time, fid);
                    continue;
                }
            }
        }

        if (srcCount != destCount) {
            logger.error(
                    "migrate time feed table, error total count, should be {}, actually {}",
                    srcCount, destCount);
        } else {
            logger.warn(
                    "migrate time feed table, total count, startTime: {}, endTime: {}, count: {}",
                    startTime, endTime, srcCount);
        }
    }

    /**
     * migrate hbase article user time table
     * 
     * @param startUid
     *            int
     * @param endUid
     *            int
     * @param startTime
     *            long
     * @param endTime
     *            long
     */
    @Async
    public void migrateArticleUserTimeTable(int startUid, int endUid,
            long startTime, long endTime) {
        HbClient hbClientSrc = schedulerConfiguration.getHbClient();
        HbClient hbClientDest = schedulerConfiguration.getHbClientBk();
        int srcCount = 0;
        int destCount = 0;
        for (int uid = startUid; uid <= endUid; ++uid) {
            for (long time = startTime; time <= endTime; ++time) {
                try {
                    ArticleResult articleResult = hbClientSrc
                            .getArticleUserTime(uid, time);
                    if (articleResult != null) {
                        ++srcCount;
                        logger.info(
                                "migrate article user time table, succeed read uid: {}, time: {}",
                                uid, time);
                        hbClientDest.setArticleUserTime(articleResult);
                        ++destCount;
                        logger.info(
                                "migrate article user time table, succeed write uid: {}, time: {}",
                                uid, time);
                    } else {
                        logger.error(
                                "migrate article user time table, null uid: {}, time: {}",
                                uid, time);
                        continue;
                    }
                } catch (Exception e) {
                    logger.error(
                            "migrate article user time table, exception uid: {}, time: {}",
                            uid, time);
                    continue;
                }
            }
        }

        if (srcCount != destCount) {
            logger.error(
                    "migrate article user time table, error total count, should be {}, actually {}",
                    srcCount, destCount);
        } else {
            logger.warn(
                    "migrate article user time table, total count, startUid: {}, endUid: {}, count: {}",
                    startUid, endUid, srcCount);
        }
    }

    /**
     * migrate hbase article time user table
     * 
     * @param startTime
     *            long
     * @param endTime
     *            long
     * @param startUid
     *            int
     * @param endUid
     *            int
     */
    @Async
    public void migrateArticleTimeUserTable(long startTime, long endTime,
            int startUid, int endUid) {
        HbClient hbClientSrc = schedulerConfiguration.getHbClient();
        HbClient hbClientDest = schedulerConfiguration.getHbClientBk();
        int srcCount = 0;
        int destCount = 0;
        for (long time = startTime; time <= endTime; ++time) {
            for (int uid = startUid; uid <= endUid; ++uid) {
                try {
                    ArticleResult articleResult = hbClientSrc
                            .getArticleTimeUser(time, uid);
                    if (articleResult != null) {
                        ++srcCount;
                        logger.info(
                                "migrate article time user table, succeed read time: {}, uid: {}",
                                time, uid);
                        hbClientDest.setArticleTimeUser(articleResult);
                        ++destCount;
                        logger.info(
                                "migrate article time user table, succeed write time: {}, uid: {}",
                                time, uid);
                    } else {
                        logger.error(
                                "migrate article time user table, null time: {}, uid: {}",
                                time, uid);
                        continue;
                    }
                } catch (Exception e) {
                    logger.error(
                            "migrate article time user table, exception time: {}, uid: {}",
                            time, uid);
                    continue;
                }
            }
        }

        if (srcCount != destCount) {
            logger.error(
                    "migrate article time user table, error total count, should be {}, actually {}",
                    srcCount, destCount);
        } else {
            logger.warn(
                    "migrate article time user table, total count, startTime: {}, endTime: {}, count: {}",
                    startTime, endTime, srcCount);
        }
    }

    /**
     * migrate hbase subject filter feed table
     * 
     * @param startOrderNo
     *            long
     * @param endOrderNo
     *            long
     * @param startImgNo
     *            int
     * @param endImgNo
     *            int
     */
    @Async
    public void migrateSubjectFilterFeedTable(long startOrderNo,
            long endOrderNo, int startImgNo, int endImgNo, boolean isOldOrderNo) {
        HbClient hbClientSrc = schedulerConfiguration.getHbClient();
        HbClient hbClientDest = schedulerConfiguration.getHbClientBk();

        if (isOldOrderNo) {
            int srcCountOld = 0;
            int destCountOld = 0;
            Calendar startTimeOld = Calendar.getInstance();
            Calendar stopTimeOld = Calendar.getInstance();
            SimpleDateFormat dfOld = new SimpleDateFormat("yyyyMMdd");
            try {
                Date startDate = dfOld.parse(String.valueOf(startOrderNo));
                startTimeOld.setTime(startDate);
                Date stopDate = dfOld.parse(String.valueOf(endOrderNo));
                stopTimeOld.setTime(stopDate);

                while (startDate.compareTo(stopDate) <= 0) {
                    for (int i = 0; i <= NUM_999999; ++i) {
                        for (int imgNo = startImgNo; imgNo <= endImgNo; ++imgNo) {
                            long orderNo = Long.valueOf(dfOld.format(startDate)
                                    + String.format("%06d", i));
                            try {
                                SubjectFilterInfo subjectFilterInfo = hbClientSrc
                                        .getSubjectFilterFeed(orderNo, imgNo);
                                if (subjectFilterInfo != null) {
                                    ++srcCountOld;
                                    logger.info(
                                            "migrate subject filter feed old table, succeed read orderNo: {}, imgNo: {}",
                                            orderNo, imgNo);
                                    hbClientDest
                                            .setSubjectFilterFeed(subjectFilterInfo);
                                    ++destCountOld;
                                    logger.info(
                                            "migrate subject filter feed old table, succeed write orderNo: {}, imgNo: {}",
                                            orderNo, imgNo);
                                } else {
                                    logger.error(
                                            "migrate subject filter feed old table, null orderNo: {}, imgNo: {}",
                                            orderNo, imgNo);
                                    continue;
                                }
                            } catch (IOException e) {
                                logger.error(
                                        "migrate subject filter feed old table, io exception orderNo: {}, imgNo: {}",
                                        orderNo, imgNo);
                                continue;
                            } catch (NumberFormatException e) {
                                logger.error(
                                        "migrate subject filter feed old table, number format exception startDate: {}, imgNo: {}",
                                        startDate, imgNo);
                                return;
                            }
                        }
                    }
                    startTimeOld.add(Calendar.DATE, 1);
                    startDate = startTimeOld.getTime();
                }
                if (srcCountOld != destCountOld) {
                    logger.error(
                            "migrate subject filter feed old table, error total count, should be {}, actually {}",
                            srcCountOld, destCountOld);
                } else {
                    logger.warn(
                            "migrate subject filter feed old table, total count, startOrderNo: {}, endOrderNo: {}, count: {}",
                            startOrderNo, endOrderNo, srcCountOld);
                }
            } catch (Exception e) {
                logger.error(
                        "migrate subject filter feed old table, number format exception startOrderNo: {}, endOrderNo: {}",
                        startOrderNo, endOrderNo);
                return;
            }
        } else {
            int srcCountNew = 0;
            int destCountNew = 0;
            Calendar startTimeNew = Calendar.getInstance();
            Calendar stopTimeNew = Calendar.getInstance();
            SimpleDateFormat dfNew = new SimpleDateFormat("yyyyMMddHHmmss");
            try {
                Date startDate = dfNew.parse(String.valueOf(startOrderNo)
                        + "000000");
                startTimeNew.setTime(startDate);
                Date stopDate = dfNew.parse(String.valueOf(endOrderNo)
                        + "000000");
                stopTimeNew.setTime(stopDate);

                while (startDate.compareTo(stopDate) <= 0) {
                    for (int i = 0; i <= NUM_999999; ++i) {
                        for (int imgNo = startImgNo; imgNo <= endImgNo; ++imgNo) {
                            String orderNo = dfNew.format(startDate)
                                    + String.format("%06d", i);
                            try {
                                SubjectFilterInfo subjectFilterInfo = hbClientSrc
                                        .getSubjectFilterFeedStr(orderNo, imgNo);
                                if (subjectFilterInfo != null) {
                                    ++srcCountNew;
                                    logger.info(
                                            "migrate subject filter feed new table, succeed read orderNo: {}, imgNo: {}",
                                            orderNo, imgNo);
                                    hbClientDest
                                            .setSubjectFilterFeedStr(subjectFilterInfo);
                                    ++destCountNew;
                                    logger.info(
                                            "migrate subject filter feed new table, succeed write orderNo: {}, imgNo: {}",
                                            orderNo, imgNo);
                                } else {
                                    logger.error(
                                            "migrate subject filter feed new table, null orderNo: {}, imgNo: {}",
                                            orderNo, imgNo);
                                    continue;
                                }
                            } catch (IOException e) {
                                logger.error(
                                        "migrate subject filter feed new table, io exception orderNo: {}, imgNo: {}",
                                        orderNo, imgNo);
                                continue;
                            } catch (NumberFormatException e) {
                                logger.error(
                                        "migrate subject filter feed new table, number format exception startDate: {}, imgNo: {}",
                                        startDate, imgNo);
                                return;
                            }
                        }
                    }
                    startTimeNew.add(Calendar.SECOND, 1);
                    startDate = startTimeNew.getTime();
                }
                if (srcCountNew != destCountNew) {
                    logger.error(
                            "migrate subject filter feed new table, error total count, should be {}, actually {}",
                            srcCountNew, destCountNew);
                } else {
                    logger.warn(
                            "migrate subject filter feed new table, total count, startOrderNo: {}, endOrderNo: {}, count: {}",
                            startOrderNo, endOrderNo, srcCountNew);
                }
            } catch (Exception e) {
                logger.error(
                        "migrate subject filter feed new table, number format exception startOrderNo: {}, endOrderNo: {}",
                        startOrderNo, endOrderNo);
                return;
            }
        }
    }

    /**
     * migrate hbase subject filter user feed table
     * 
     * @param startUid
     *            int
     * @param endUid
     *            int
     * @param startOrderNo
     *            long
     * @param endOrderNo
     *            long
     * @param startImgNo
     *            int
     * @param endImgNo
     *            int
     */
    @Async
    public void migrateSubjectFilterUserFeedTable(int startUid, int endUid,
            long startOrderNo, long endOrderNo, int startImgNo, int endImgNo,
            boolean isOldOrderNo) {
        HbClient hbClientSrc = schedulerConfiguration.getHbClient();
        HbClient hbClientDest = schedulerConfiguration.getHbClientBk();

        if (isOldOrderNo) {
            int srcCountOld = 0;
            int destCountOld = 0;
            Calendar startTimeOld = Calendar.getInstance();
            Calendar stopTimeOld = Calendar.getInstance();
            SimpleDateFormat dfOld = new SimpleDateFormat("yyyyMMdd");
            try {
                Date startDate = dfOld.parse(String.valueOf(startOrderNo));
                startTimeOld.setTime(startDate);
                Date stopDate = dfOld.parse(String.valueOf(endOrderNo));
                stopTimeOld.setTime(stopDate);

                while (startDate.compareTo(stopDate) <= 0) {
                    for (int i = 0; i <= NUM_999999; ++i) {
                        for (int uid = startUid; uid <= endUid; ++uid) {
                            for (int imgNo = startImgNo; imgNo <= endImgNo; ++imgNo) {
                                long orderNo = Long.valueOf(dfOld
                                        .format(startDate)
                                        + String.format("%06d", i));
                                try {
                                    SubjectFilterInfo subjectFilterInfo = hbClientSrc
                                            .getSubjectFilterUserFeed(uid,
                                                    orderNo, imgNo);
                                    if (subjectFilterInfo != null) {
                                        ++srcCountOld;
                                        logger.info(
                                                "migrate subject filter user feed old table, succeed read uid: {}, orderNo: {}, imgNo: {}",
                                                uid, orderNo, imgNo);
                                        hbClientDest
                                                .setSubjectFilterUserFeed(subjectFilterInfo);
                                        ++destCountOld;
                                        logger.info(
                                                "migrate subject filter user feed old table, succeed write uid: {}, orderNo: {}, imgNo: {}",
                                                uid, orderNo, imgNo);
                                    } else {
                                        logger.error(
                                                "migrate subject filter user feed old table, null uid: {}, orderNo: {}, imgNo: {}",
                                                uid, orderNo, imgNo);
                                        continue;
                                    }
                                } catch (IOException e) {
                                    logger.error(
                                            "migrate subject filter user feed old table, io exception uid: {}, orderNo: {}, imgNo: {}",
                                            uid, orderNo, imgNo);
                                    continue;
                                } catch (NumberFormatException e) {
                                    logger.error(
                                            "migrate subject filter user feed old table, number format exception uid: {}, startDate: {}, imgNo: {}",
                                            uid, startDate, imgNo);
                                    return;
                                }
                            }
                        }
                    }
                    startTimeOld.add(Calendar.DATE, 1);
                    startDate = startTimeOld.getTime();
                }
                if (srcCountOld != destCountOld) {
                    logger.error(
                            "migrate subject filter user feed old table, error total count, should be {}, actually {}",
                            srcCountOld, destCountOld);
                } else {
                    logger.warn(
                            "migrate subject filter user feed old table, total count, startUid: {}, endUid: {}, count: {}",
                            startUid, endUid, srcCountOld);
                }
            } catch (Exception e) {
                logger.error(
                        "migrate subject filter user feed old table, number format exception startOrderNo: {}, endOrderNo: {}",
                        startOrderNo, endOrderNo);
                return;
            }
        } else {
            int srcCountNew = 0;
            int destCountNew = 0;
            Calendar startTimeNew = Calendar.getInstance();
            Calendar stopTimeNew = Calendar.getInstance();
            SimpleDateFormat dfNew = new SimpleDateFormat("yyyyMMddHHmmss");
            try {
                Date startDate = dfNew.parse(String.valueOf(startOrderNo)
                        + "000000");
                startTimeNew.setTime(startDate);
                Date stopDate = dfNew.parse(String.valueOf(endOrderNo)
                        + "000000");
                stopTimeNew.setTime(stopDate);

                while (startDate.compareTo(stopDate) <= 0) {
                    for (int i = 0; i <= NUM_999999; ++i) {
                        for (int uid = startUid; uid <= endUid; ++uid) {
                            for (int imgNo = startImgNo; imgNo <= endImgNo; ++imgNo) {
                                String orderNo = dfNew.format(startDate)
                                        + String.format("%06d", i);
                                try {
                                    SubjectFilterInfo subjectFilterInfo = hbClientSrc
                                            .getSubjectFilterUserFeedStr(uid,
                                                    orderNo, imgNo);
                                    if (subjectFilterInfo != null) {
                                        ++srcCountNew;
                                        logger.info(
                                                "migrate subject filter user feed new table, succeed read uid: {}, orderNo: {}, imgNo: {}",
                                                uid, orderNo, imgNo);
                                        hbClientDest
                                                .setSubjectFilterUserFeedStr(subjectFilterInfo);
                                        ++destCountNew;
                                        logger.info(
                                                "migrate subject filter user feed new table, succeed write uid: {}, orderNo: {}, imgNo: {}",
                                                uid, orderNo, imgNo);
                                    } else {
                                        logger.error(
                                                "migrate subject filter user feed new table, null uid: {}, orderNo: {}, imgNo: {}",
                                                uid, orderNo, imgNo);
                                        continue;
                                    }
                                } catch (IOException e) {
                                    logger.error(
                                            "migrate subject filter user feed new table, io exception uid: {}, orderNo: {}, imgNo: {}",
                                            uid, orderNo, imgNo);
                                    continue;
                                } catch (NumberFormatException e) {
                                    logger.error(
                                            "migrate subject filter user feed new table, number format exception uid: {}, startDate: {}, imgNo: {}",
                                            uid, startDate, imgNo);
                                    return;
                                }
                            }
                        }
                    }
                    startTimeNew.add(Calendar.SECOND, 1);
                    startDate = startTimeNew.getTime();
                }
                if (srcCountNew != destCountNew) {
                    logger.error(
                            "migrate subject filter user feed new table, error total count, should be {}, actually {}",
                            srcCountNew, destCountNew);
                } else {
                    logger.warn(
                            "migrate subject filter user feed new table, total count, startUid: {}, endUid: {}, count: {}",
                            startUid, endUid, srcCountNew);
                }
            } catch (Exception e) {
                logger.error(
                        "migrate subject filter user feed new table, number format exception startOrderNo: {}, endOrderNo: {}",
                        startOrderNo, endOrderNo);
                return;
            }
        }
    }
}

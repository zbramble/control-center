package com.fincad.thermometer.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wenba.scheduler.AbstractResult.StatusCode;
import com.wenba.scheduler.AbstractServer.OcrType;
import com.wenba.scheduler.AbstractServer.ServerType;
import com.wenba.scheduler.config.ConfigParam;
import com.wenba.scheduler.config.ConfigParam.ConfigFileType;
import com.wenba.scheduler.config.ConfigResult;
import com.wenba.scheduler.config.ConfigServer;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.config.SystemDataConfiguration.SchedulerStrategy;
import com.wenba.scheduler.em.EmParam;
import com.wenba.scheduler.em.EmRequestParam;
import com.wenba.scheduler.em.EmResult;
import com.wenba.scheduler.em.EmServer;
import com.wenba.scheduler.jzh.IeServer;
import com.wenba.scheduler.jzh.JzhParam;
import com.wenba.scheduler.jzh.JzhResult;
import com.wenba.scheduler.login.MD5Util;
import com.wenba.scheduler.login.User;
import com.wenba.scheduler.nlp.NlpParam;
import com.wenba.scheduler.nlp.NlpResult;
import com.wenba.scheduler.ocr.OcrHbaseResult;
import com.wenba.scheduler.ocr.OcrParam;
import com.wenba.scheduler.ocr.OcrResult;
import com.wenba.scheduler.ocr.OcrServer;
import com.wenba.scheduler.search.SearchArticleHbaseResult;
import com.wenba.scheduler.search.SearchArticleLog;
import com.wenba.scheduler.search.SearchHbaseResult;
import com.wenba.scheduler.search.SearchParam;
import com.wenba.scheduler.search.SearchParam.ArticleType;
import com.wenba.scheduler.search.SearchParam.ClassicPoemType;
import com.wenba.scheduler.search.SearchResult;
import com.wenba.scheduler.search.SearchServer;
import com.wenba.scheduler.statistics.BIParam;
import com.wenba.scheduler.statistics.BIStrategy;
import com.wenba.scheduler.ugc.UgcCommonStrategy;
import com.wenba.scheduler.ugc.UgcParam;
import com.wenba.scheduler.ugc.UgcResult;
import com.xueba100.mining.common.Feed;
import com.xueba100.mining.common.SubjectFilterInfo;

/**
 * @author zhangbo
 *
 */
@Controller
@RequestMapping(value = "fincad")
public class ThermometerController {

    // constants
    private static final String SEARCH_RESULT_TYPE = "type";
    private static final String SEARCH_RESULT_TYPE_OK = "ok";
    private static final String SEARCH_RESULT_QUESTIONS = "questions";
    private static final String SEARCH_RESULT_SIMILARITY = "similarity";
    private static final String SEARCH_RESULT_STEM_HTML = "stem_html";
    private static final String SEARCH_RESULT_ID = "id";
    private static final int JEDIS_MAX_TOTAL = 200;
    private static final int MIN_THREAD_NUM = 10;
    private static final int MAX_THREAD_NUM = 50;
    private static final int IE_BUFF_LENGTH = 4096;

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger timeoutLogger = LogManager.getLogger("timeout");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");
    private static Logger redisLogger = LogManager.getLogger("redis");
    private Random r;
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private SchedulerControllerStatistics schedulerControllerStatistics;
    @Resource
    private SaveHbaseData saveHbaseData;
    @Resource
    private RetrieveHbaseData retrieveHbaseData;
    @Resource
    private SchedulerAsyncUtil schedulerAsyncUtil;
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
    private ConcurrentLinkedQueue<BasicNameValuePair> queryResultToRedisQueue;
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
    private ISchedulerStrategy<OcrParam, OcrResult> ocrStrategy;
    @Resource
    private ISchedulerStrategy<OcrParam, OcrResult> handwriteOcrStrategy;
    @Resource
    private ISchedulerStrategy<SearchParam, SearchResult> searchStrategy;
    @Resource
    private ISchedulerStrategy<SearchParam, SearchResult> sdkSearchStrategy;
    @Resource
    private ISchedulerStrategy<SearchParam, SearchResult> searchByIdStrategy;
    @Resource
    private ISchedulerStrategy<SearchParam, SearchResult> searchHomeworkStrategy;
    @Resource
    private ISchedulerStrategy<SearchParam, SearchResult> wordSearchStrategy;
    @Resource
    private ISchedulerStrategy<SearchParam, SearchResult> searchClassicPoemStrategy;
    @Resource
    private ISchedulerStrategy<SearchParam, SearchResult> searchArticleStrategy;
    @Resource
    private ISchedulerStrategy<ConfigParam, ConfigResult> configStrategy;
    @Resource
    private ISchedulerStrategy<ConfigParam, ConfigResult> configFromHtmlStrategy;
    @Resource
    private BIStrategy biStrategy;
    @Resource
    private ISchedulerStrategy<NlpParam, NlpResult> nlpStrategy;
    @Resource
    private ISchedulerStrategy<EmParam, EmResult> emStrategy;
    @Resource
    private ISchedulerStrategy<JzhParam, JzhResult> jzhStrategy;
    @Resource
    private UgcCommonStrategy ugcCommonStrategy;
    @Resource
    private ISchedulerStrategy<UgcParam, UgcResult> ugcHandwriteOcrStrategy;
    @Resource
    private ISchedulerStrategy<SearchParam, SearchResult> searchMatrixStrategy;
    @Resource
    private ObjectMapper mapper;
    private JSONObject exceptionJson;
    private Calendar c;

    @PostConstruct
    public void init() {
        r = new Random();
        exceptionJson = new JSONObject();
        exceptionJson.put("type", "exception");

        // 设置local host, ip, ip list start
        String ip = null;
        String name = null;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();
            name = addr.getHostName();
        } catch (UnknownHostException e) {
            logger.error("Unknown Host Exception");
        }
        schedulerConfiguration.setIp(ip);
        schedulerConfiguration.setName(name);

        List<String> ipList = getLocalIPList();
        if (ipList.size() > 0) {
            schedulerConfiguration.setIpList(ipList);
        } else {
            schedulerConfiguration.setIpList(null);
        }
        // 设置local host, ip, ip list end

        // 设置configFileNames
        File configFileDir = new File("configFile");
        File[] configFiles = configFileDir.listFiles();
        List<String> configFileNames = new ArrayList<String>();
        for (File configFile : configFiles) {
            configFileNames.add(configFile.getName());
        }
        schedulerConfiguration.setConfigFileNames(configFileNames);

        // 配置mapper
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        schedulerConfiguration.setMapper(mapper);

        // 通过config文件配置scheduler configuration
        ConfigParam configParam = new ConfigParam();
        configParam.setSchedulerConfiguration(schedulerConfiguration);
        configParam.setSchedulerControllerStatistics(
                schedulerControllerStatistics);
        configParam.setConfigFileType(ConfigFileType.ALL);
        configParam.setConfigData(null);
        configStrategy.excute(configParam);

        // 设置启动时间
        schedulerConfiguration
                .setSchedulerStartTime(System.currentTimeMillis());

        // 设置time zone
        c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        schedulerConfiguration.setTimeZone(c.get(Calendar.HOUR_OF_DAY));

        // unused server list init start
        schedulerConfiguration.setCnnUnusedServers(new ArrayList<OcrServer>());
        schedulerConfiguration.setJavaUnusedServers(new ArrayList<OcrServer>());
        schedulerConfiguration
                .setSearchUnusedServers(new ArrayList<SearchServer>());
        // unused server list init end

        // 设置redis start
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(JEDIS_MAX_TOTAL);
        jedisPoolConfig.setTestOnBorrow(true);

        JedisPool jedisPool = new JedisPool(jedisPoolConfig,
                schedulerConfiguration.getSystemDataConfiguration()
                        .getJedisIp(),
                schedulerConfiguration.getSystemDataConfiguration()
                        .getJedisPort());
        schedulerConfiguration.setJedisPool(jedisPool);

        JedisPool localJedisPool = new JedisPool(jedisPoolConfig,
                schedulerConfiguration.getSystemDataConfiguration()
                        .getLocalJedisIp(),
                schedulerConfiguration.getSystemDataConfiguration()
                        .getLocalJedisPort());
        schedulerConfiguration.setLocalJedisPool(localJedisPool);
        // 设置redis end

        // 获取ugc access token start
        UgcParam ugcCommonParam = new UgcParam();
        ugcCommonParam.setSchedulerConfiguration(schedulerConfiguration);
        ugcCommonStrategy.getAccessToken(ugcCommonParam);
        // 获取ugc access token end
    }

    /**
     * @param img
     *            MultipartFile
     * @param fid
     *            String
     * @param uid
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public ModelAndView handleQueryRequest(
            @RequestParam(value = "file") MultipartFile img,
            @RequestParam(value = "fid", required = false) String fid,
            @RequestParam(value = "uid", required = false) String uid,
            @RequestParam(value = "index", required = false) String index,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
        JSONObject resultJson = null;
        mv.setViewName("queryResult");

        // 当白名单开关使能后，判断请求ip是否有效
        if (schedulerConfiguration.isWhiteListOnSwitch()) {
            String requestIP = request.getRemoteAddr();
            boolean isValidIP = false;
            for (ConfigServer configServer : schedulerConfiguration
                    .getConfigServers()) {
                if (configServer.getIp().equals(requestIP)) {
                    isValidIP = true;
                    break;
                }
            }
            if (!isValidIP) {
                mv.addObject("searchResult", "who are you!");
                serverMonitorLogger.error("{} invalid ip access query!",
                        requestIP);
                return mv;
            }
        }

        // 判断请求数是否超过最大限制
        if (schedulerControllerStatistics
                .getCurrentRequestNum() >= schedulerConfiguration
                        .getSystemDataConfiguration().getQueryRequestLimit()) {
            mv.addObject("searchResult", "request denied!");
            return mv;
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // statistics
        schedulerControllerStatistics.incrementAndGetAllRequestNum();
        int cr = schedulerControllerStatistics
                .incrementAndGetCurrentRequestNum();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isCurrentRequestDebugSwitch()) {
            debugLogger.info("{} {} ICR: {}", uid, fid, cr);
        }

        // BI data
        BIParam biParam = new BIParam();
        biParam.setSchedulerConfiguration(schedulerConfiguration);
        biParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
        biParam.setFid(fid);
        biParam.setUid(uid);
        biParam.setExcuteNlp(false);
        biParam.setExcuteFirstSearch(false);
        biParam.setExcuteSecondSearch(false);

        // ocr scheduler
        UgcParam ocrParam = new UgcParam();
        ocrParam.setSchedulerConfiguration(schedulerConfiguration);
        if (img == null) {
            schedulerControllerStatistics.incrementAndGetImgNullNum();
        }
        biParam.setImgName(img.getOriginalFilename());
        ocrParam.setImg(img);
        ocrParam.setFid(fid);
        ocrParam.setUid(uid);
        ocrParam.setSchedulerControllerStatistics(
                schedulerControllerStatistics);
        ocrParam.setNlpStrategy(nlpStrategy);
        // choosing ocr server
        boolean choosingCnn = false;
        int weightForChoosingOcr = r.nextInt(schedulerConfiguration
                .getSystemDataConfiguration().getOcrServerTotalWeight());
        if (weightForChoosingOcr < schedulerConfiguration
                .getSystemDataConfiguration().getOcrServerCnnWeight()) {
            choosingCnn = true;
        }
        schedulerControllerStatistics.incrementAndGetCurrentCnnRequestNum();
        OcrServer ocrServer = chooseOcrServer(choosingCnn);
        ocrParam.setOcrServer(ocrServer);
        OcrResult firstOcrResult = ocrStrategy.excute(ocrParam);
        biParam.setOcrTime(firstOcrResult.getExcuteTime());
        if (firstOcrResult.isExcuteNlp()) {
            biParam.setExcuteNlp(true);
            biParam.setNlpTime(firstOcrResult.getNlpExcuteTime());
        }
        schedulerControllerStatistics.decrementAndGetCurrentCnnRequestNum();
        SearchResult firstSearchResult = null;
        OcrResult secondOcrResult = null;
        SearchResult secondSearchResult = null;

        if (!schedulerConfiguration.isExecOcrOnlySwitch()
                && firstOcrResult.getSchedulerResult() != null
                && !"".equals(firstOcrResult.getSchedulerResult())) {
            // first ocr success
            biParam.setStatus(0);
            biParam.setVersion("cnn" + firstOcrResult.getVersion());
            if (schedulerConfiguration.isForceHbaseOnSwitch()
                    && schedulerConfiguration.isHbaseOnSwitch() && fid != null
                    && !fid.matches("^\\s*$") && uid != null
                    && !uid.matches("^\\s*$")) {
                try {
                    OcrHbaseResult ocrHbaseResult = new OcrHbaseResult();
                    ocrHbaseResult.setFid(Long.parseLong(fid));
                    ocrHbaseResult.setUid(Integer.parseInt(uid));
                    ocrHbaseResult.setOcrType(firstOcrResult.getOcrType());
                    ocrHbaseResult.setRotate(firstOcrResult.getRotate());
                    ocrHbaseResult.setSchedulerResult(
                            firstOcrResult.getSchedulerResult());
                    ocrHbaseResult.setOcrServer(ocrServer);
                    ocrHbaseQueue.offer(ocrHbaseResult);
                    schedulerControllerStatistics
                            .incrementAndGetOcrHbaseResultNum();
                    saveHbaseData.saveOcrHbaseData();
                } catch (Exception e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} SO EX", uid, fid,
                                ocrServer.getId());
                    }
                }
            }

            schedulerControllerStatistics
                    .incrementAndGetCurrentSearchRequestNum();
            // choosing search server
            SearchServer searchServer = chooseSearchServer();
            // search scheduler
            SearchParam searchParam = new SearchParam();
            searchParam.setSchedulerConfiguration(schedulerConfiguration);
            searchParam.setOcrResult(firstOcrResult);
            searchParam.setFid(fid);
            searchParam.setUid(uid);
            searchParam.setIndex(index);
            searchParam.setUser(user);
            searchParam.setToken(token);
            searchParam.setLimit(limit != null ? limit
                    : schedulerConfiguration.getSystemDataConfiguration()
                            .getLimit());
            searchParam.setSchedulerControllerStatistics(
                    schedulerControllerStatistics);
            searchParam.setSearchServer(searchServer);
            firstSearchResult = searchStrategy.excute(searchParam);
            biParam.setExcuteFirstSearch(true);
            biParam.setFirstSearchTime(firstSearchResult.getExcuteTime());
            schedulerControllerStatistics
                    .decrementAndGetCurrentSearchRequestNum();

            // 获取search result的最大相似度
            float maxSimilarity = 0f;
            if (firstSearchResult.getSchedulerResult() != null) {
                if (firstSearchResult.getSearchResultList() != null) {
                    // first search result type:ok
                    float firstSimilarity = firstSearchResult
                            .getMaxSimilarity();
                    biParam.setSearchSuccess(true);
                    biParam.setSimilarity(firstSimilarity);
                    String docIds = "";
                    for (int i = 0; i < firstSearchResult.getSearchResultList()
                            .size(); ++i) {
                        if (i == 0) {
                            docIds += firstSearchResult.getSearchResultList()
                                    .get(i).getId();
                        } else {
                            docIds += ("," + firstSearchResult
                                    .getSearchResultList().get(i).getId());
                        }
                    }
                    biParam.setDocIds(docIds);
                    if (firstSimilarity > maxSimilarity) {
                        maxSimilarity = firstSimilarity;
                    }
                    if (schedulerConfiguration.isForceHbaseOnSwitch()
                            && schedulerConfiguration.isHbaseOnSwitch()
                            && fid != null && !fid.matches("^\\s*$")
                            && uid != null && !uid.matches("^\\s*$")) {
                        try {
                            SearchHbaseResult searchHbaseResult = new SearchHbaseResult();
                            searchHbaseResult.setFid(Long.parseLong(fid));
                            searchHbaseResult.setUid(Integer.parseInt(uid));
                            searchHbaseResult.setSearchResultList(
                                    firstSearchResult.getSearchResultList());
                            searchHbaseResult.setSearchServer(searchServer);
                            searchHbaseQueue.offer(searchHbaseResult);
                            schedulerControllerStatistics
                                    .incrementAndGetSearchHbaseResultNum();
                            saveHbaseData.saveSearchHbaseData();
                        } catch (Exception e) {
                            if (schedulerConfiguration
                                    .isSaveExceptionLogSwitch()) {
                                logger.error("{} {} {} SS EX", uid, fid,
                                        searchServer.getId());
                            }
                        }
                    }
                    if (schedulerConfiguration.isQueryResultToRedisOnSwitch()
                            && fid != null && !"".equals(fid)) {
                        JSONObject queryResult = new JSONObject();
                        queryResult.put("ocrResult",
                                firstOcrResult.getSchedulerResult());
                        queryResult.put("rotate", firstOcrResult.getRotate());
                        queryResult.put("searchResult",
                                firstSearchResult.getSchedulerResult());
                        queryResultToRedisQueue.offer(new BasicNameValuePair(
                                fid, queryResult.toString()));
                        redisLogger.info("{} {}", fid, uid);
                        schedulerAsyncUtil.saveQueryResultToRedis();
                    }
                } else {
                    // first search result type:empty | exception
                    biParam.setSearchSuccess(false);
                }
            } else {
                // no first search result
                biParam.setSearchSuccess(false);
                if (schedulerConfiguration.isBiOnSwitch()
                        && queryBiQueue.size() < schedulerConfiguration
                                .getSystemDataConfiguration()
                                .getQueryBiQueueLimit()
                        && fid != null && !"".equals(fid) && uid != null
                        && !"".equals(uid)) {
                    queryBiQueue.offer(biParam);
                    if ("Async".equals(
                            schedulerConfiguration.getQueryBiModeSwitch())) {
                        biStrategy.excute(queryBiQueue);
                    }
                }
                stopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgQueryExecTimeDebugSwitch()) {
                    schedulerControllerStatistics
                            .addAndGetExecTime(stopTime - startTime);
                    schedulerControllerStatistics.incrementAndGetExecTimeNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isQueryExecTimeDebugSwitch()) {
                    debugLogger.info("{} {} ET: {}", uid, fid,
                            (stopTime - startTime));
                }

                // statistics
                cr = schedulerControllerStatistics
                        .decrementAndGetCurrentRequestNum();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isCurrentRequestDebugSwitch()) {
                    debugLogger.info("{} {} DCR: {}", uid, fid, cr);
                }
                exceptionJson.put("ocrTime", firstOcrResult.getExcuteTime());
                exceptionJson.put("ocrType", OcrType.CNN.getValue());
                exceptionJson.put("searchTime",
                        firstSearchResult.getExcuteTime());
                exceptionJson.put("queryTime", stopTime - startTime);
                exceptionJson.put("statusCode",
                        firstSearchResult.getStatusCode().ordinal());
                exceptionJson.put("serverType", ServerType.SEARCH.ordinal());
                mv.addObject("searchResult", exceptionJson.toString());
                return mv;
            }

            // 根据search相似度，判断是否执行第2次Ocr
            if (maxSimilarity < schedulerConfiguration
                    .getSystemDataConfiguration().getThresUnusedResult()
                    && firstOcrResult
                            .getOcrAndNlpExcuteTime() < (schedulerConfiguration
                                    .getTimeoutConfiguration()
                                    .getConnectTimeout() * 2
                                    + schedulerConfiguration
                                            .getTimeoutConfiguration()
                                            .getOcrTimeout())) {
                // second ocr with handwrite ocr
                ocrServer = chooseHandwriteOcrServer();
                ocrParam.setOcrServer(ocrServer);
                ocrParam.setLayoutinfo(firstOcrResult.getLayoutinfo() != null
                        ? firstOcrResult.getLayoutinfo()
                        : "");
                ocrParam.setUseLayoutinfoOrNot(true);
                ocrParam.setOcrAndNlpExcuteTime(
                        firstOcrResult.getOcrAndNlpExcuteTime());
                if (schedulerConfiguration.isUgcHandwriteOcrOnSwitch()) {
                    secondOcrResult = ugcHandwriteOcrStrategy.excute(ocrParam);
                } else {
                    secondOcrResult = handwriteOcrStrategy.excute(ocrParam);
                }
                biParam.setHandwriteTime(secondOcrResult.getExcuteTime());

                if (secondOcrResult.getSchedulerResult() != null
                        && !"".equals(secondOcrResult.getSchedulerResult())) {
                    // handwrite ocr success
                    // second search
                    searchParam.setOcrResult(secondOcrResult);
                    schedulerControllerStatistics
                            .incrementAndGetCurrentSearchRequestNum();
                    secondSearchResult = searchStrategy.excute(searchParam);
                    biParam.setExcuteSecondSearch(true);
                    biParam.setSecondSearchTime(
                            secondSearchResult.getExcuteTime());
                    schedulerControllerStatistics
                            .decrementAndGetCurrentSearchRequestNum();

                    // 获取第2次search result的最大相似度
                    if (secondSearchResult.getSchedulerResult() != null) {
                        if (secondSearchResult.getSearchResultList() != null) {
                            // second search result type:ok
                            float secondSimilarity = secondSearchResult
                                    .getMaxSimilarity();
                            if (secondSimilarity > maxSimilarity) {
                                // save second search result as final result
                                biParam.setVersion("handwrite"
                                        + secondOcrResult.getVersion());
                                biParam.setSearchSuccess(true);
                                biParam.setSimilarity(secondSimilarity);
                                String docIds = "";
                                for (int i = 0; i < secondSearchResult
                                        .getSearchResultList().size(); ++i) {
                                    if (i == 0) {
                                        docIds += secondSearchResult
                                                .getSearchResultList().get(i)
                                                .getId();
                                    } else {
                                        docIds += ("," + secondSearchResult
                                                .getSearchResultList().get(i)
                                                .getId());
                                    }
                                }
                                biParam.setDocIds(docIds);
                                resultJson = JSONObject.fromObject(
                                        secondSearchResult.toString());
                                resultJson.put("ocrTime",
                                        firstOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrTime",
                                        secondOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrVersion",
                                        secondOcrResult.getVersion());
                                resultJson.put("ocrType",
                                        OcrType.HANDWRITE_OCR_STATUS_0
                                                .getValue());
                                resultJson.put("searchTime",
                                        firstSearchResult.getExcuteTime()
                                                + secondSearchResult
                                                        .getExcuteTime());
                                if (schedulerConfiguration
                                        .isForceHbaseOnSwitch()
                                        && schedulerConfiguration
                                                .isHbaseOnSwitch()
                                        && fid != null && !fid.matches("^\\s*$")
                                        && uid != null
                                        && !uid.matches("^\\s*$")) {
                                    try {
                                        OcrHbaseResult ocrHbaseResult = new OcrHbaseResult();
                                        ocrHbaseResult
                                                .setFid(Long.parseLong(fid));
                                        ocrHbaseResult
                                                .setUid(Integer.parseInt(uid));
                                        ocrHbaseResult.setOcrType(
                                                secondOcrResult.getOcrType());
                                        ocrHbaseResult.setRotate(
                                                secondOcrResult.getRotate());
                                        ocrHbaseResult.setSchedulerResult(
                                                secondOcrResult
                                                        .getSchedulerResult());
                                        ocrHbaseResult.setOcrServer(ocrServer);
                                        ocrHbaseQueue.offer(ocrHbaseResult);
                                        schedulerControllerStatistics
                                                .incrementAndGetOcrHbaseResultNum();
                                        saveHbaseData.saveOcrHbaseData();

                                        SearchHbaseResult searchHbaseResult = new SearchHbaseResult();
                                        searchHbaseResult
                                                .setFid(Long.parseLong(fid));
                                        searchHbaseResult
                                                .setUid(Integer.parseInt(uid));
                                        searchHbaseResult.setSearchResultList(
                                                secondSearchResult
                                                        .getSearchResultList());
                                        searchHbaseResult
                                                .setSearchServer(searchServer);
                                        searchHbaseQueue
                                                .offer(searchHbaseResult);
                                        schedulerControllerStatistics
                                                .incrementAndGetSearchHbaseResultNum();
                                        saveHbaseData.saveSearchHbaseData();
                                    } catch (Exception e) {
                                        if (schedulerConfiguration
                                                .isSaveExceptionLogSwitch()) {
                                            logger.error("{} {} SOS EX", uid,
                                                    fid);
                                        }
                                    }
                                }
                            } else {
                                // save first search result as final result
                                resultJson = JSONObject.fromObject(
                                        firstSearchResult.toString());
                                resultJson.put("ocrTime",
                                        firstOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrTime",
                                        secondOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrVersion",
                                        secondOcrResult.getVersion());
                                resultJson.put("ocrType",
                                        OcrType.HANDWRITE_OCR_STATUS_0
                                                .getValue());
                                resultJson.put("searchTime",
                                        firstSearchResult.getExcuteTime()
                                                + secondSearchResult
                                                        .getExcuteTime());
                            }
                        } else {
                            // second search result type:empty | exception
                            resultJson = JSONObject
                                    .fromObject(firstSearchResult.toString());
                            resultJson.put("ocrTime",
                                    firstOcrResult.getExcuteTime());
                            resultJson.put("handwriteOcrTime",
                                    secondOcrResult.getExcuteTime());
                            resultJson.put("handwriteOcrVersion",
                                    secondOcrResult.getVersion());
                            resultJson.put("ocrType",
                                    OcrType.HANDWRITE_OCR_STATUS_0.getValue());
                            resultJson.put("searchTime",
                                    firstSearchResult.getExcuteTime()
                                            + secondSearchResult
                                                    .getExcuteTime());
                        }
                        resultJson.put("statusCode",
                                secondSearchResult.getStatusCode().ordinal());
                        resultJson.put("serverType",
                                ServerType.SEARCH.ordinal());
                    } else {
                        // no second search result
                        resultJson = JSONObject
                                .fromObject(firstSearchResult.toString());
                        resultJson.put("ocrTime",
                                firstOcrResult.getExcuteTime());
                        resultJson.put("handwriteOcrTime",
                                secondOcrResult.getExcuteTime());
                        resultJson.put("handwriteOcrVersion",
                                secondOcrResult.getVersion());
                        resultJson.put("ocrType",
                                OcrType.HANDWRITE_OCR_STATUS_0.getValue());
                        resultJson.put("searchTime",
                                firstSearchResult.getExcuteTime()
                                        + secondSearchResult.getExcuteTime());
                        resultJson.put("statusCode",
                                secondSearchResult.getStatusCode().ordinal());
                        resultJson.put("serverType",
                                ServerType.SEARCH.ordinal());
                    }
                } else {
                    // second ocr fail
                    resultJson = JSONObject
                            .fromObject(firstSearchResult.toString());
                    resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
                    resultJson.put("handwriteOcrTime",
                            secondOcrResult.getExcuteTime());
                    resultJson.put("handwriteOcrVersion",
                            secondOcrResult.getVersion());
                    resultJson.put("ocrType", OcrType.HANDWRITE_OCR.getValue());
                    resultJson.put("searchTime",
                            firstSearchResult.getExcuteTime());
                    resultJson.put("statusCode",
                            secondOcrResult.getStatusCode().ordinal());
                    resultJson.put("serverType",
                            ocrServer.getServerType().ordinal());
                }
            } else {
                // no need to do handwrite ocr with another ocr
                resultJson = JSONObject
                        .fromObject(firstSearchResult.toString());
                resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
                resultJson.put("ocrType", OcrType.CNN.getValue());
                resultJson.put("searchTime", firstSearchResult.getExcuteTime());
                resultJson.put("statusCode",
                        firstSearchResult.getStatusCode().ordinal());
                resultJson.put("serverType", ServerType.SEARCH.ordinal());
            }
        } else if (schedulerConfiguration.isExecOcrOnlySwitch()
                && firstOcrResult.getSchedulerResult() != null) {
            debugLogger.info("{} {} Ocr result: {}", uid, fid,
                    firstOcrResult.getSchedulerResult());
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrResult", firstOcrResult.getSchedulerResult());
            resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
            resultJson.put("ocrType", OcrType.CNN.getValue());
            resultJson.put("statusCode",
                    firstOcrResult.getStatusCode().ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        } else {
            // first ocr fail
            biParam.setStatus(1);
            biParam.setVersion(
                    (firstOcrResult.getOcrType() != null ? "cnn" : "")
                            + firstOcrResult.getVersion());
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
            resultJson.put("ocrType", OcrType.CNN.getValue());
            resultJson.put("searchTime", 0);
            resultJson.put("statusCode",
                    firstOcrResult.getStatusCode().ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        }
        if (schedulerConfiguration.isBiOnSwitch() && ocrServer != null
                && queryBiQueue.size() < schedulerConfiguration
                        .getSystemDataConfiguration().getQueryBiQueueLimit()
                && fid != null && !"".equals(fid) && uid != null
                && !"".equals(uid)) {
            queryBiQueue.offer(biParam);
            if ("Async".equals(schedulerConfiguration.getQueryBiModeSwitch())) {
                biStrategy.excute(queryBiQueue);
            }
        }
        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgQueryExecTimeDebugSwitch()) {
            schedulerControllerStatistics
                    .addAndGetExecTime(stopTime - startTime);
            schedulerControllerStatistics.incrementAndGetExecTimeNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isQueryExecTimeDebugSwitch()) {
            debugLogger.info("{} {} ET: {}", uid, fid, (stopTime - startTime));
        }

        // save exception log: query exec time >= query timeout
        if (schedulerConfiguration.isSaveTimeoutLogSwitch()
                && (stopTime - startTime) >= schedulerConfiguration
                        .getTimeoutConfiguration().getQueryTimeout()) {
            timeoutLogger.error(
                    "{} {} ET: {}>={} firstOcrTime: {} secondOcrTime: {} firstSearchTime: {} secondSearchTime: {} firstNlpTime: {} secondNlpTime: {}",
                    uid, fid, (stopTime - startTime),
                    schedulerConfiguration
                            .getTimeoutConfiguration().getQueryTimeout(),
                    firstOcrResult.getExcuteTime(),
                    secondOcrResult != null ? secondOcrResult.getExcuteTime()
                            : 0,
                    firstSearchResult.getExcuteTime(),
                    secondSearchResult != null
                            ? secondSearchResult.getExcuteTime()
                            : 0,
                    firstOcrResult.getNlpExcuteTime(),
                    secondOcrResult != null ? secondOcrResult.getNlpExcuteTime()
                            : 0);
        }

        // statistics
        cr = schedulerControllerStatistics.decrementAndGetCurrentRequestNum();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isCurrentRequestDebugSwitch()) {
            debugLogger.info("{} {} DCR: {}", uid, fid, cr);
        }

        resultJson.put("queryTime", stopTime - startTime);
        mv.addObject("searchResult", resultJson.toString());
        return mv;
    }

}

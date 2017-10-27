package com.wenba.scheduler;

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
@RequestMapping(value = "wenba-scheduler")
public class SchedulerController {

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
        configParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
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
                        .getJedisIp(), schedulerConfiguration
                        .getSystemDataConfiguration().getJedisPort());
        schedulerConfiguration.setJedisPool(jedisPool);

        JedisPool localJedisPool = new JedisPool(jedisPoolConfig,
                schedulerConfiguration.getSystemDataConfiguration()
                        .getLocalJedisIp(), schedulerConfiguration
                        .getSystemDataConfiguration().getLocalJedisPort());
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
        if (schedulerControllerStatistics.getCurrentRequestNum() >= schedulerConfiguration
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
        ocrParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
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
                    ocrHbaseResult.setSchedulerResult(firstOcrResult
                            .getSchedulerResult());
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
            searchParam.setLimit(limit != null ? limit : schedulerConfiguration
                    .getSystemDataConfiguration().getLimit());
            searchParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
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
                            searchHbaseResult
                                    .setSearchResultList(firstSearchResult
                                            .getSearchResultList());
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
                                .getQueryBiQueueLimit() && fid != null
                        && !"".equals(fid) && uid != null && !"".equals(uid)) {
                    queryBiQueue.offer(biParam);
                    if ("Async".equals(schedulerConfiguration
                            .getQueryBiModeSwitch())) {
                        biStrategy.excute(queryBiQueue);
                    }
                }
                stopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgQueryExecTimeDebugSwitch()) {
                    schedulerControllerStatistics.addAndGetExecTime(stopTime
                            - startTime);
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
                exceptionJson.put("statusCode", firstSearchResult
                        .getStatusCode().ordinal());
                exceptionJson.put("serverType", ServerType.SEARCH.ordinal());
                mv.addObject("searchResult", exceptionJson.toString());
                return mv;
            }

            // 根据search相似度，判断是否执行第2次Ocr
            if (maxSimilarity < schedulerConfiguration
                    .getSystemDataConfiguration().getThresUnusedResult()
                    && firstOcrResult.getOcrAndNlpExcuteTime() < (schedulerConfiguration
                            .getTimeoutConfiguration().getConnectTimeout() * 2 + schedulerConfiguration
                            .getTimeoutConfiguration().getOcrTimeout())) {
                // second ocr with handwrite ocr
                ocrServer = chooseHandwriteOcrServer();
                ocrParam.setOcrServer(ocrServer);
                ocrParam.setLayoutinfo(firstOcrResult.getLayoutinfo() != null ? firstOcrResult
                        .getLayoutinfo() : "");
                ocrParam.setUseLayoutinfoOrNot(true);
                ocrParam.setOcrAndNlpExcuteTime(firstOcrResult
                        .getOcrAndNlpExcuteTime());
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
                    biParam.setSecondSearchTime(secondSearchResult
                            .getExcuteTime());
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
                                resultJson = JSONObject
                                        .fromObject(secondSearchResult
                                                .toString());
                                resultJson.put("ocrTime",
                                        firstOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrTime",
                                        secondOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrVersion",
                                        secondOcrResult.getVersion());
                                resultJson.put("ocrType",
                                        OcrType.HANDWRITE_OCR_STATUS_0
                                                .getValue());
                                resultJson.put(
                                        "searchTime",
                                        firstSearchResult.getExcuteTime()
                                                + secondSearchResult
                                                        .getExcuteTime());
                                if (schedulerConfiguration
                                        .isForceHbaseOnSwitch()
                                        && schedulerConfiguration
                                                .isHbaseOnSwitch()
                                        && fid != null
                                        && !fid.matches("^\\s*$")
                                        && uid != null
                                        && !uid.matches("^\\s*$")) {
                                    try {
                                        OcrHbaseResult ocrHbaseResult = new OcrHbaseResult();
                                        ocrHbaseResult.setFid(Long
                                                .parseLong(fid));
                                        ocrHbaseResult.setUid(Integer
                                                .parseInt(uid));
                                        ocrHbaseResult
                                                .setOcrType(secondOcrResult
                                                        .getOcrType());
                                        ocrHbaseResult
                                                .setRotate(secondOcrResult
                                                        .getRotate());
                                        ocrHbaseResult
                                                .setSchedulerResult(secondOcrResult
                                                        .getSchedulerResult());
                                        ocrHbaseResult.setOcrServer(ocrServer);
                                        ocrHbaseQueue.offer(ocrHbaseResult);
                                        schedulerControllerStatistics
                                                .incrementAndGetOcrHbaseResultNum();
                                        saveHbaseData.saveOcrHbaseData();

                                        SearchHbaseResult searchHbaseResult = new SearchHbaseResult();
                                        searchHbaseResult.setFid(Long
                                                .parseLong(fid));
                                        searchHbaseResult.setUid(Integer
                                                .parseInt(uid));
                                        searchHbaseResult
                                                .setSearchResultList(secondSearchResult
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
                                resultJson = JSONObject
                                        .fromObject(firstSearchResult
                                                .toString());
                                resultJson.put("ocrTime",
                                        firstOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrTime",
                                        secondOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrVersion",
                                        secondOcrResult.getVersion());
                                resultJson.put("ocrType",
                                        OcrType.HANDWRITE_OCR_STATUS_0
                                                .getValue());
                                resultJson.put(
                                        "searchTime",
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
                            resultJson.put(
                                    "searchTime",
                                    firstSearchResult.getExcuteTime()
                                            + secondSearchResult
                                                    .getExcuteTime());
                        }
                        resultJson.put("statusCode", secondSearchResult
                                .getStatusCode().ordinal());
                        resultJson.put("serverType",
                                ServerType.SEARCH.ordinal());
                    } else {
                        // no second search result
                        resultJson = JSONObject.fromObject(firstSearchResult
                                .toString());
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
                        resultJson.put("statusCode", secondSearchResult
                                .getStatusCode().ordinal());
                        resultJson.put("serverType",
                                ServerType.SEARCH.ordinal());
                    }
                } else {
                    // second ocr fail
                    resultJson = JSONObject.fromObject(firstSearchResult
                            .toString());
                    resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
                    resultJson.put("handwriteOcrTime",
                            secondOcrResult.getExcuteTime());
                    resultJson.put("handwriteOcrVersion",
                            secondOcrResult.getVersion());
                    resultJson.put("ocrType", OcrType.HANDWRITE_OCR.getValue());
                    resultJson.put("searchTime",
                            firstSearchResult.getExcuteTime());
                    resultJson.put("statusCode", secondOcrResult
                            .getStatusCode().ordinal());
                    resultJson.put("serverType", ocrServer.getServerType()
                            .ordinal());
                }
            } else {
                // no need to do handwrite ocr with another ocr
                resultJson = JSONObject
                        .fromObject(firstSearchResult.toString());
                resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
                resultJson.put("ocrType", OcrType.CNN.getValue());
                resultJson.put("searchTime", firstSearchResult.getExcuteTime());
                resultJson.put("statusCode", firstSearchResult.getStatusCode()
                        .ordinal());
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
            resultJson.put("statusCode", firstOcrResult.getStatusCode()
                    .ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        } else {
            // first ocr fail
            biParam.setStatus(1);
            biParam.setVersion((firstOcrResult.getOcrType() != null ? "cnn"
                    : "") + firstOcrResult.getVersion());
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
            resultJson.put("ocrType", OcrType.CNN.getValue());
            resultJson.put("searchTime", 0);
            resultJson.put("statusCode", firstOcrResult.getStatusCode()
                    .ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        }
        if (schedulerConfiguration.isBiOnSwitch()
                && ocrServer != null
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
            schedulerControllerStatistics.addAndGetExecTime(stopTime
                    - startTime);
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
            timeoutLogger
                    .error("{} {} ET: {}>={} firstOcrTime: {} secondOcrTime: {} firstSearchTime: {} secondSearchTime: {} firstNlpTime: {} secondNlpTime: {}",
                            uid,
                            fid,
                            (stopTime - startTime),
                            schedulerConfiguration.getTimeoutConfiguration()
                                    .getQueryTimeout(),
                            firstOcrResult.getExcuteTime(),
                            secondOcrResult != null ? secondOcrResult
                                    .getExcuteTime() : 0,
                            firstSearchResult.getExcuteTime(),
                            secondSearchResult != null ? secondSearchResult
                                    .getExcuteTime() : 0,
                            firstOcrResult.getNlpExcuteTime(),
                            secondOcrResult != null ? secondOcrResult
                                    .getNlpExcuteTime() : 0);
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

    /**
     * @param ids
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public ModelAndView handleQueryByIdRequest(
            @RequestParam(value = "ids") String ids, HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error("{} invalid ip access queryById!",
                        requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        schedulerControllerStatistics.incrementAndGetCurrentSearchRequestNum();
        // choosing search by id server
        SearchServer searchByIdServer = chooseSearchByIdServer();
        // search by id scheduler
        SearchParam searchParam = new SearchParam();
        searchParam.setSchedulerConfiguration(schedulerConfiguration);
        searchParam.setSearchServer(searchByIdServer);
        searchParam.setIds(ids);
        searchParam.setFid("QBI");
        searchParam.setUid("QBI");
        searchParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        SearchResult searchResult = searchByIdStrategy.excute(searchParam);
        schedulerControllerStatistics.decrementAndGetCurrentSearchRequestNum();
        if (searchResult.getSchedulerResult() != null
                && StatusCode.OK.equals(searchResult.getStatusCode())) {
            mv.addObject("searchResult", searchResult.toString());
        } else {
            JSONObject statusExceptionJson = new JSONObject();
            statusExceptionJson.put("statusCode", searchResult.getStatusCode()
                    .ordinal());
            mv.addObject("searchResult", statusExceptionJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgQueryByIdExecTimeDebugSwitch()) {
            schedulerControllerStatistics.addAndGetExecTimeById(stopTime
                    - startTime);
            schedulerControllerStatistics.incrementAndGetExecTimeByIdNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isQueryByIdExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By Id: {}", searchParam.getUid(),
                    searchParam.getFid(), searchByIdServer.getId(),
                    (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param uids
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/miningHomework", method = RequestMethod.POST)
    public ModelAndView handleMiningHomeworkRequest(
            @RequestParam(value = "uids") String uids,
            @RequestParam(value = "app", required = false) String app,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access miningHomework!", requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // choosing search homework server
        SearchServer searchHomeworkServer = chooseSearchHomeworkServer();
        // mining homework scheduler
        SearchParam searchHomeworkParam = new SearchParam();
        searchHomeworkParam.setSchedulerConfiguration(schedulerConfiguration);
        searchHomeworkParam.setSearchServer(searchHomeworkServer);
        searchHomeworkParam.setUids(uids);
        searchHomeworkParam.setFid("MH");
        searchHomeworkParam.setUid("MH");
        searchHomeworkParam
                .setApp((app != null && !app.matches("^\\s*$")) ? app : "xbj");
        searchHomeworkParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        SearchResult searchHomeworkResult = searchHomeworkStrategy
                .excute(searchHomeworkParam);
        if (searchHomeworkResult.getSchedulerResult() != null
                && StatusCode.OK.equals(searchHomeworkResult.getStatusCode())) {
            mv.addObject("searchResult", searchHomeworkResult.toString());
        } else {
            JSONObject statusExceptionJson = new JSONObject();
            statusExceptionJson.put("statusCode", searchHomeworkResult
                    .getStatusCode().ordinal());
            mv.addObject("searchResult", statusExceptionJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgHomeworkExecTimeDebugSwitch()) {
            schedulerControllerStatistics.addAndGetExecTimeByHomework(stopTime
                    - startTime);
            schedulerControllerStatistics
                    .incrementAndGetExecTimeByHomeworkNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isHomeworkExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} {} ET By Homework: {}",
                    searchHomeworkParam.getUid(), searchHomeworkParam.getFid(),
                    searchHomeworkParam.getApp(), searchHomeworkServer.getId(),
                    (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param keywords
     *            String
     * @param limit
     *            Integer
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/wordSearch", method = RequestMethod.POST)
    public ModelAndView handleWordSearchRequest(
            @RequestParam(value = "keywords") String keywords,
            @RequestParam(value = "index", required = false) String index,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error("{} invalid ip access wordSearch!",
                        requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        schedulerControllerStatistics.incrementAndGetCurrentSearchRequestNum();
        // choosing search server
        SearchServer searchServer = chooseSearchServer();
        // search scheduler
        SearchParam searchParam = new SearchParam();
        searchParam.setSchedulerConfiguration(schedulerConfiguration);
        searchParam.setSearchServer(searchServer);
        OcrResult ocrResult = new OcrResult();
        ocrResult.setSchedulerResult(keywords);
        searchParam.setOcrResult(ocrResult);
        searchParam.setFid("WSR");
        searchParam.setUid("WSR");
        searchParam.setIndex(index);
        searchParam.setUser(user);
        searchParam.setToken(token);
        searchParam.setLimit(limit != null ? limit : schedulerConfiguration
                .getSystemDataConfiguration().getLimit());
        searchParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        SearchResult searchResult = wordSearchStrategy.excute(searchParam);
        schedulerControllerStatistics.decrementAndGetCurrentSearchRequestNum();
        if (searchResult.getSchedulerResult() != null) {
            mv.addObject("searchResult", searchResult.toString());
        } else {
            mv.addObject("searchResult", exceptionJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgWordSearchExecTimeDebugSwitch()) {
            schedulerControllerStatistics
                    .addAndGetExecTimeByWordSearch(stopTime - startTime);
            schedulerControllerStatistics
                    .incrementAndGetExecTimeByWordSearchNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isWordSearchExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By Word Search: {}",
                    searchParam.getUid(), searchParam.getFid(),
                    searchServer.getId(), (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param fid
     *            String
     * @param uid
     *            String
     * @param keywords
     *            String
     * @param searchResult
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/wordSearchHbase", method = RequestMethod.POST)
    public ModelAndView handleWordSearchHbaseRequest(
            @RequestParam(value = "fid") String fid,
            @RequestParam(value = "uid") String uid,
            @RequestParam(value = "keywords") String keywords,
            @RequestParam(value = "searchResult") String searchResult,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access wordSearchHbase!", requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // word search scheduler
        SearchResult wordSearchResult = getWordSearchResult(fid, uid,
                searchResult);
        JSONObject statusCodeJson = new JSONObject();
        if (wordSearchResult.getSearchResultList() != null
                && wordSearchResult.getSearchResultList().size() > 0) {
            statusCodeJson.put("statusCode", StatusCode.OK.ordinal());
            mv.addObject("searchResult", statusCodeJson.toString());
            if (schedulerConfiguration.isForceHbaseOnSwitch()
                    && schedulerConfiguration.isHbaseOnSwitch() && fid != null
                    && !fid.matches("^\\s*$") && uid != null
                    && !uid.matches("^\\s*$")) {
                try {
                    OcrHbaseResult ocrHbaseResult = new OcrHbaseResult();
                    ocrHbaseResult.setFid(Long.parseLong(fid));
                    ocrHbaseResult.setUid(Integer.parseInt(uid));
                    ocrHbaseResult.setSchedulerResult(keywords);
                    ocrHbaseWordSearchQueue.offer(ocrHbaseResult);
                    schedulerControllerStatistics
                            .incrementAndGetOcrHbaseResultNum();
                    saveHbaseData.saveWordSearchOcrHbaseData();

                    SearchHbaseResult searchHbaseResult = new SearchHbaseResult();
                    searchHbaseResult.setFid(Long.parseLong(fid));
                    searchHbaseResult.setUid(Integer.parseInt(uid));
                    searchHbaseResult.setSearchResultList(wordSearchResult
                            .getSearchResultList());
                    searchHbaseWordSearchQueue.offer(searchHbaseResult);
                    schedulerControllerStatistics
                            .incrementAndGetSearchHbaseResultNum();
                    saveHbaseData.saveWordSearchSearchHbaseData();
                } catch (Exception e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} WSOS EX", uid, fid);
                    }
                }
            }
        } else {
            statusCodeJson.put("statusCode", StatusCode.NORESULT.ordinal());
            mv.addObject("searchResult", statusCodeJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isWordSearchExecTimeDebugSwitch()) {
            debugLogger.info("{} {} ET By Word Search Hbase: {}", uid, fid,
                    (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param keywords
     *            String
     * @param limit
     *            Integer
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ModelAndView handleSearchRequest(
            @RequestParam(value = "keywords") String keywords,
            @RequestParam(value = "index", required = false) String index,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error("{} invalid ip access search!",
                        requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        schedulerControllerStatistics.incrementAndGetCurrentSearchRequestNum();
        // choosing search server
        SearchServer searchServer = chooseSearchServer();
        // search scheduler
        SearchParam searchParam = new SearchParam();
        searchParam.setSchedulerConfiguration(schedulerConfiguration);
        searchParam.setSearchServer(searchServer);
        OcrResult ocrResult = new OcrResult();
        ocrResult.setSchedulerResult(keywords);
        searchParam.setOcrResult(ocrResult);
        searchParam.setIndex(index);
        searchParam.setUser(user);
        searchParam.setToken(token);
        searchParam.setLimit(limit != null ? limit : schedulerConfiguration
                .getSystemDataConfiguration().getLimit());
        searchParam.setFid("SR");
        searchParam.setUid("SR");
        searchParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        SearchResult searchResult = searchStrategy.excute(searchParam);
        schedulerControllerStatistics.decrementAndGetCurrentSearchRequestNum();
        if (searchResult.getSchedulerResult() != null) {
            mv.addObject("searchResult", searchResult.toString());
        } else {
            mv.addObject("searchResult", exceptionJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgSearchExecTimeDebugSwitch()) {
            schedulerControllerStatistics.addAndGetExecTimeBySearch(stopTime
                    - startTime);
            schedulerControllerStatistics.incrementAndGetExecTimeBySearchNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isSearchExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By Search: {}", searchParam.getUid(),
                    searchParam.getFid(), searchServer.getId(),
                    (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param uid
     *            String
     * @param keywords
     *            String
     * @param filter
     *            String
     * @param grade
     *            String
     * @param tagsLimit
     *            Integer
     * @param pageNo
     *            Integer
     * @param pageSize
     *            Integer
     * @param queryTime
     *            Long
     * @param lastKeywords
     *            String
     * @param lastQueryTime
     *            Long
     * @param sessionId
     *            String
     * @param subject
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/articleQuery", method = RequestMethod.POST)
    public ModelAndView handleArticleQueryRequest(
            @RequestParam(value = "uid") String uid,
            @RequestParam(value = "keywords") String keywords,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "grade", required = false) String grade,
            @RequestParam(value = "tagsLimit", required = false) Integer tagsLimit,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "queryTime", required = false) Long queryTime,
            @RequestParam(value = "lastKeywords", required = false) String lastKeywords,
            @RequestParam(value = "lastQueryTime", required = false) Long lastQueryTime,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "subject", required = false) String subject,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error("{} invalid ip access articleQuery!",
                        requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // choosing article query server
        int mod = schedulerConfiguration.getSearchArticleServers().size();
        int serverIndex = Integer.parseInt(uid) % mod;
        SearchServer searchArticleServer = schedulerConfiguration
                .getSearchArticleServers().get(serverIndex);
        // article query scheduler
        SearchParam searchArticleParam = new SearchParam();
        searchArticleParam.setSchedulerConfiguration(schedulerConfiguration);
        searchArticleParam.setSearchServer(searchArticleServer);
        searchArticleParam.setUid(uid);
        searchArticleParam.setKeywords(keywords);
        searchArticleParam.setFilter(filter);
        searchArticleParam.setGrade(grade);
        searchArticleParam.setTagsLimit(tagsLimit);
        searchArticleParam.setPageNo(pageNo);
        searchArticleParam.setPageSize(pageSize);
        searchArticleParam.setSubject(subject);
        searchArticleParam.setFid("AQ");
        searchArticleParam.setArticleType(ArticleType.QUERY);
        searchArticleParam.setArticleUrl(schedulerConfiguration
                .getSystemDataConfiguration().getArticleQuery());
        searchArticleParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        SearchResult searchArticleResult = searchArticleStrategy
                .excute(searchArticleParam);
        if (sessionId == null || "".equals(sessionId)) {
            sessionId = request.getSession().getId();
        }
        if (searchArticleResult.getSchedulerResult() != null
                && StatusCode.OK.equals(searchArticleResult.getStatusCode())) {
            JSONObject resultJson = JSONObject.fromObject(searchArticleResult
                    .toString());
            resultJson.put("sessionId", sessionId);
            mv.addObject("searchResult", resultJson.toString());
        } else {
            JSONObject statusExceptionJson = new JSONObject();
            statusExceptionJson.put("statusCode", searchArticleResult
                    .getStatusCode().ordinal());
            statusExceptionJson.put("sessionId", sessionId);
            mv.addObject("searchResult", statusExceptionJson.toString());
        }

        // 判断是否要写入hbase
        if ((pageNo == null || (pageNo != null && pageNo == 1))
                && ((filter == null || (filter != null && "".equals(filter))) && (grade == null || (grade != null && ""
                        .equals(grade))))) {
            if (schedulerConfiguration.isForceHbaseOnSwitch()
                    && schedulerConfiguration.isHbaseOnSwitch() && uid != null
                    && !uid.matches("^\\s*$")) {
                SearchArticleHbaseResult searchArticleHbaseResult = new SearchArticleHbaseResult();
                searchArticleHbaseResult.setUid(uid);
                searchArticleHbaseResult.setKeywords(keywords);
                searchArticleHbaseResult
                        .setSearchArticleServer(searchArticleServer);
                searchArticleHbaseQueue.offer(searchArticleHbaseResult);
                schedulerControllerStatistics
                        .incrementAndGetSearchArticleHbaseResultNum();
                saveHbaseData.saveSearchArticleHbaseData();
            }
        }

        // 判断是否要保存search article query log
        if (schedulerConfiguration.isSaveSearchArticleLogSwitch()) {
            SearchArticleLog searchArticleQueryLog = new SearchArticleLog();
            searchArticleQueryLog.setUid(uid);
            searchArticleQueryLog.setKeywords(keywords);
            searchArticleQueryLog.setFilter(filter);
            searchArticleQueryLog.setGrade(grade);
            searchArticleQueryLog.setTagsLimit(tagsLimit);
            searchArticleQueryLog.setPageNo(pageNo);
            searchArticleQueryLog.setPageSize(pageSize);
            searchArticleQueryLog.setQueryTime(queryTime);
            searchArticleQueryLog.setLastKeywords(lastKeywords);
            searchArticleQueryLog.setLastQueryTime(lastQueryTime);
            searchArticleQueryLog.setSessionId(sessionId);
            searchArticleQueryLog.setSearchResult(searchArticleResult);
            searchArticleQueryLog.setServerIndex(serverIndex);
            searchArticleQueryLogQueue.offer(searchArticleQueryLog);
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgArticleExecTimeDebugSwitch()) {
            schedulerControllerStatistics.addAndGetExecTimeByArticle(stopTime
                    - startTime);
            schedulerControllerStatistics.incrementAndGetExecTimeByArticleNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isArticleExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By Article Query: {}",
                    searchArticleParam.getUid(), searchArticleParam.getFid(),
                    searchArticleServer.getId(), (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param ids
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/articleQueryById", method = RequestMethod.POST)
    public ModelAndView handleArticleQueryByIdRequest(
            @RequestParam(value = "ids") String ids, HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access articleQueryById!", requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // choosing article query by id server
        SearchServer searchArticleServer = chooseSearchArticleServer();
        // article query by id scheduler
        SearchParam searchArticleParam = new SearchParam();
        searchArticleParam.setSchedulerConfiguration(schedulerConfiguration);
        searchArticleParam.setSearchServer(searchArticleServer);
        searchArticleParam.setIds(ids);
        searchArticleParam.setUid("AQBI");
        searchArticleParam.setFid("AQBI");
        searchArticleParam.setArticleType(ArticleType.QUERY_BY_ID);
        searchArticleParam.setArticleUrl(schedulerConfiguration
                .getSystemDataConfiguration().getArticleQueryById());
        searchArticleParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        SearchResult searchArticleResult = searchArticleStrategy
                .excute(searchArticleParam);
        if (searchArticleResult.getSchedulerResult() != null
                && StatusCode.OK.equals(searchArticleResult.getStatusCode())) {
            mv.addObject("searchResult", searchArticleResult.toString());
        } else {
            JSONObject statusExceptionJson = new JSONObject();
            statusExceptionJson.put("statusCode", searchArticleResult
                    .getStatusCode().ordinal());
            mv.addObject("searchResult", statusExceptionJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgArticleExecTimeDebugSwitch()) {
            schedulerControllerStatistics.addAndGetExecTimeByArticle(stopTime
                    - startTime);
            schedulerControllerStatistics.incrementAndGetExecTimeByArticleNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isArticleExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By Article Query By Id: {}",
                    searchArticleParam.getUid(), searchArticleParam.getFid(),
                    searchArticleServer.getId(), (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param keywords
     *            String
     * @param uid
     *            String
     * @param queryTime
     *            Long
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/articleAutoComplete", method = RequestMethod.POST)
    public ModelAndView handleArticleAutoCompleteRequest(
            @RequestParam(value = "keywords") String keywords,
            @RequestParam(value = "uid", required = false) String uid,
            @RequestParam(value = "queryTime", required = false) Long queryTime,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access articleAutoComplete!", requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // choosing article auto complete server
        SearchServer searchArticleServer = chooseSearchArticleServer();
        // article auto complete scheduler
        SearchParam searchArticleParam = new SearchParam();
        searchArticleParam.setSchedulerConfiguration(schedulerConfiguration);
        searchArticleParam.setSearchServer(searchArticleServer);
        searchArticleParam.setKeywords(keywords);
        searchArticleParam.setUid("AAC");
        searchArticleParam.setFid("AAC");
        searchArticleParam.setArticleType(ArticleType.AUTO_COMPLETE);
        searchArticleParam.setArticleUrl(schedulerConfiguration
                .getSystemDataConfiguration().getArticleAutoComplete());
        searchArticleParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        SearchResult searchArticleResult = searchArticleStrategy
                .excute(searchArticleParam);
        if (searchArticleResult.getSchedulerResult() != null
                && StatusCode.OK.equals(searchArticleResult.getStatusCode())) {
            mv.addObject("searchResult", searchArticleResult.toString());
        } else {
            JSONObject statusExceptionJson = new JSONObject();
            statusExceptionJson.put("statusCode", searchArticleResult
                    .getStatusCode().ordinal());
            mv.addObject("searchResult", statusExceptionJson.toString());
        }

        // 判断是否要保存search article auto complete log
        if (schedulerConfiguration.isSaveSearchArticleLogSwitch()) {
            SearchArticleLog searchArticleAutoCompleteLog = new SearchArticleLog();
            searchArticleAutoCompleteLog.setUid(uid);
            searchArticleAutoCompleteLog.setKeywords(keywords);
            searchArticleAutoCompleteLog.setQueryTime(queryTime);
            searchArticleAutoCompleteLog.setSearchResult(searchArticleResult);
            searchArticleAutoCompleteLogQueue
                    .offer(searchArticleAutoCompleteLog);
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgArticleExecTimeDebugSwitch()) {
            schedulerControllerStatistics.addAndGetExecTimeByArticle(stopTime
                    - startTime);
            schedulerControllerStatistics.incrementAndGetExecTimeByArticleNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isArticleExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By Article Auto Complete: {}",
                    searchArticleParam.getUid(), searchArticleParam.getFid(),
                    searchArticleServer.getId(), (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param keywords
     *            String
     * @param limit
     *            Integer
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/classicPoemQuery", method = RequestMethod.POST)
    public ModelAndView handleClassicPoemQueryRequest(
            @RequestParam(value = "keywords") String keywords,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access classicPoemQuery!", requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        schedulerControllerStatistics.incrementAndGetCurrentSearchRequestNum();
        // choosing search server
        SearchServer searchServer = chooseSearchServer();
        // classic poem query scheduler
        SearchParam searchParam = new SearchParam();
        searchParam.setSchedulerConfiguration(schedulerConfiguration);
        searchParam.setSearchServer(searchServer);
        searchParam.setKeywords(keywords);
        searchParam.setLimit((limit != null && limit > 0) ? limit : 0);
        searchParam.setFid("CPQ");
        searchParam.setUid("CPQ");
        searchParam.setClassicPoemType(ClassicPoemType.QUERY);
        searchParam.setClassicPoemUrl(schedulerConfiguration
                .getSystemDataConfiguration().getClassicPoemQuery());
        searchParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        SearchResult searchResult = searchClassicPoemStrategy
                .excute(searchParam);
        schedulerControllerStatistics.decrementAndGetCurrentSearchRequestNum();
        if (searchResult.getSchedulerResult() != null) {
            mv.addObject("searchResult", searchResult.toString());
        } else {
            mv.addObject("searchResult", exceptionJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isClassicPoemQueryExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By Classic Poem Query: {}",
                    searchParam.getUid(), searchParam.getFid(),
                    searchServer.getId(), (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param keywords
     *            String
     * @param limit
     *            Integer
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/classicPoemAutoComplete", method = RequestMethod.POST)
    public ModelAndView handleClassicPoemAutoCompleteRequest(
            @RequestParam(value = "keywords") String keywords,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access classicPoemAutoComplete!",
                        requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        schedulerControllerStatistics.incrementAndGetCurrentSearchRequestNum();
        // choosing search server
        SearchServer searchServer = chooseSearchServer();
        // classic poem auto complete scheduler
        SearchParam searchParam = new SearchParam();
        searchParam.setSchedulerConfiguration(schedulerConfiguration);
        searchParam.setSearchServer(searchServer);
        searchParam.setKeywords(keywords);
        searchParam.setLimit((limit != null && limit > 0) ? limit : 0);
        searchParam.setFid("CPAC");
        searchParam.setUid("CPAC");
        searchParam.setClassicPoemType(ClassicPoemType.AUTO_COMPLETE);
        searchParam.setClassicPoemUrl(schedulerConfiguration
                .getSystemDataConfiguration().getClassicPoemAutoComplete());
        searchParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        SearchResult searchResult = searchClassicPoemStrategy
                .excute(searchParam);
        schedulerControllerStatistics.decrementAndGetCurrentSearchRequestNum();
        if (searchResult.getSchedulerResult() != null) {
            mv.addObject("searchResult", searchResult.toString());
        } else {
            mv.addObject("searchResult", exceptionJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isClassicPoemAutoCompleteExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By Classic Poem Auto Complete: {}",
                    searchParam.getUid(), searchParam.getFid(),
                    searchServer.getId(), (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param emRequestParam
     *            EmRequestParam
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/emQuery", method = RequestMethod.POST)
    public ModelAndView handleEmQueryRequest(
            @RequestBody EmRequestParam emRequestParam,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
        mv.setViewName("queryResult");

        debugLogger.info("datum: {}", emRequestParam.getDatum());

        // 当白名单开关使能后，判断请求ip是否有效
        // if (schedulerConfiguration.isWhiteListOnSwitch()) {
        // String requestIP = request.getRemoteAddr();
        // boolean isValidIP = false;
        // for (ConfigServer configServer : schedulerConfiguration
        // .getConfigServers()) {
        // if (configServer.getIp().equals(requestIP)) {
        // isValidIP = true;
        // break;
        // }
        // }
        // if (!isValidIP) {
        // mv.addObject("searchResult", "who are you!");
        // serverMonitorLogger.error("{} invalid ip access search!",
        // requestIP);
        // return mv;
        // }
        // }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // choosing em server
        EmServer emServer = chooseEmServer();
        // em query scheduler
        EmParam emParam = new EmParam();
        emParam.setSchedulerConfiguration(schedulerConfiguration);
        emParam.setEmServer(emServer);
        JSONObject datum = new JSONObject();
        datum.put("datum", emRequestParam.getDatum());
        emParam.setDatum(datum);
        emParam.setFid("EQ");
        emParam.setUid("EQ");
        emParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
        EmResult emResult = emStrategy.excute(emParam);
        if (emResult.getSchedulerResult() != null) {
            mv.addObject("searchResult", emResult.toString());
        } else {
            mv.addObject("searchResult", exceptionJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isEmQueryExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By EM Query: {}", emParam.getUid(),
                    emParam.getFid(), emServer.getId(), (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param fids
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/queryHbaseByFid", method = { RequestMethod.GET,
            RequestMethod.POST })
    public ModelAndView handleQueryHbaseByFidRequest(
            @RequestParam(value = "fids") String fids,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
        mv.setViewName("queryResult");

        debugLogger.info("fids: {}", fids);

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
                serverMonitorLogger.error(
                        "{} invalid ip access queryHbaseByFid!", requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        String[] fidArray = fids.trim().split(",");
        JSONObject hbaseResultJson = null;
        if (fidArray.length > 0) {
            hbaseResultJson = new JSONObject();
            JSONArray hbaseResultJsonArray = new JSONArray();
            for (String fid : fidArray) {
                if (fid != null && !"".equals(fid)) {
                    JSONObject subHbaseResultJson = new JSONObject();

                    Future<String> futureOcrResult = null;
                    try {
                        subHbaseResultJson.put("fid",
                                Integer.parseInt(fid.trim()));
                        futureOcrResult = retrieveHbaseData
                                .retrieveOcrResultFromHbase(Integer
                                        .parseInt(fid.trim()));
                        String ocrResult = futureOcrResult.get(
                                schedulerConfiguration
                                        .getTimeoutConfiguration()
                                        .getHbaseTimeout(),
                                TimeUnit.MILLISECONDS);
                        if (ocrResult == null || "".equals(ocrResult)) {
                            if (schedulerConfiguration
                                    .isSaveExceptionLogSwitch()) {
                                logger.error("{} hbase no ocr result", fid);
                            }
                            subHbaseResultJson.put("ocr", false);
                        } else {
                            subHbaseResultJson.put("ocr", true);
                        }
                    } catch (TimeoutException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} hbase ocr TE", fid);
                        }
                        if (futureOcrResult != null) {
                            if (!futureOcrResult.isCancelled()) {
                                futureOcrResult.cancel(true);
                            }
                        }
                        subHbaseResultJson.put("ocr", false);
                    } catch (ExecutionException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} hbase ocr EE", fid);
                        }
                        if (futureOcrResult != null) {
                            if (!futureOcrResult.isCancelled()) {
                                futureOcrResult.cancel(true);
                            }
                        }
                        subHbaseResultJson.put("ocr", false);
                    } catch (InterruptedException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} hbase ocr IE", fid);
                        }
                        if (futureOcrResult != null) {
                            if (!futureOcrResult.isCancelled()) {
                                futureOcrResult.cancel(true);
                            }
                        }
                        subHbaseResultJson.put("ocr", false);
                    } catch (NumberFormatException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} hbase ocr NFE", fid);
                        }
                        if (futureOcrResult != null) {
                            if (!futureOcrResult.isCancelled()) {
                                futureOcrResult.cancel(true);
                            }
                        }
                        subHbaseResultJson.put("ocr", false);
                    }

                    Future<List<com.xueba100.mining.common.SearchResult>> futureSearchResult = null;
                    try {
                        futureSearchResult = retrieveHbaseData
                                .retrieveSearchResultFromHbase(Integer
                                        .parseInt(fid.trim()));
                        List<com.xueba100.mining.common.SearchResult> searchResultList = futureSearchResult
                                .get(schedulerConfiguration
                                        .getTimeoutConfiguration()
                                        .getHbaseTimeout(),
                                        TimeUnit.MILLISECONDS);
                        if (searchResultList != null
                                && searchResultList.size() > 0) {
                            subHbaseResultJson.put("search", true);
                        } else {
                            // no search result in hbase
                            if (schedulerConfiguration
                                    .isSaveExceptionLogSwitch()) {
                                logger.error("{} hbase no search result", fid);
                            }
                            subHbaseResultJson.put("search", false);
                        }
                    } catch (TimeoutException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} hbase search TE", fid);
                        }
                        if (futureSearchResult != null) {
                            if (!futureSearchResult.isCancelled()) {
                                futureSearchResult.cancel(true);
                            }
                        }
                        subHbaseResultJson.put("search", false);
                    } catch (ExecutionException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} hbase search EE", fid);
                        }
                        if (futureSearchResult != null) {
                            if (!futureSearchResult.isCancelled()) {
                                futureSearchResult.cancel(true);
                            }
                        }
                        subHbaseResultJson.put("search", false);
                    } catch (InterruptedException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} hbase search IE", fid);
                        }
                        if (futureSearchResult != null) {
                            if (!futureSearchResult.isCancelled()) {
                                futureSearchResult.cancel(true);
                            }
                        }
                        subHbaseResultJson.put("search", false);
                    } catch (NumberFormatException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} hbase search NFE", fid);
                        }
                        if (futureSearchResult != null) {
                            if (!futureSearchResult.isCancelled()) {
                                futureSearchResult.cancel(true);
                            }
                        }
                        subHbaseResultJson.put("search", false);
                    }

                    hbaseResultJsonArray.add(subHbaseResultJson);
                }
            }

            hbaseResultJson.put("type", "ok");
            hbaseResultJson.put("hbaseResults", hbaseResultJsonArray);
        }

        stopTime = System.currentTimeMillis();
        if (hbaseResultJson != null) {
            hbaseResultJson.put("queryTime", stopTime - startTime);
            mv.addObject("searchResult", hbaseResultJson.toString());
        } else {
            exceptionJson.put("queryTime", stopTime - startTime);
            mv.addObject("searchResult", exceptionJson.toString());
        }

        return mv;
    }

    /**
     * @param img
     *            MultipartFile
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/queryWithOcr", method = RequestMethod.POST)
    public ModelAndView handleQueryWithOcrRequest(
            @RequestParam(value = "file") MultipartFile img,
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
                serverMonitorLogger.error("{} invalid ip access queryWithOcr!",
                        requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // ocr scheduler
        OcrParam ocrParam = new OcrParam();
        ocrParam.setSchedulerConfiguration(schedulerConfiguration);
        if (img == null) {
            schedulerControllerStatistics.incrementAndGetImgNullNum();
        }
        ocrParam.setImg(img);
        ocrParam.setFid("QWO");
        ocrParam.setUid("QWO");
        ocrParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
        ocrParam.setNlpStrategy(nlpStrategy);
        // choosing ocr server
        OcrServer ocrServer = chooseOcrServer(true);
        ocrParam.setOcrServer(ocrServer);
        OcrResult ocrResult = ocrStrategy.excute(ocrParam);
        if (ocrResult.getSchedulerResult() != null
                && !"".equals(ocrResult.getSchedulerResult())) {
            // ocr success
            // choosing search server
            SearchServer searchServer = chooseSearchServer();
            // search scheduler
            SearchParam searchParam = new SearchParam();
            searchParam.setSchedulerConfiguration(schedulerConfiguration);
            searchParam.setOcrResult(ocrResult);
            searchParam.setFid("QWO");
            searchParam.setUid("QWO");
            searchParam.setIndex(index);
            searchParam.setUser(user);
            searchParam.setToken(token);
            searchParam.setLimit(limit != null ? limit : schedulerConfiguration
                    .getSystemDataConfiguration().getLimit());
            searchParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            searchParam.setSearchServer(searchServer);
            SearchResult searchResult = searchStrategy.excute(searchParam);

            // no search result
            if (searchResult.getSchedulerResult() == null
                    || searchResult.getSearchResultList() == null) {
                stopTime = System.currentTimeMillis();
                exceptionJson.put("ocrTime", ocrResult.getExcuteTime());
                exceptionJson.put("searchTime", searchResult.getExcuteTime());
                exceptionJson.put("queryTime", stopTime - startTime);
                exceptionJson.put("statusCode", searchResult.getStatusCode()
                        .ordinal());
                exceptionJson.put("serverType", ServerType.SEARCH.ordinal());
                mv.addObject("searchResult", exceptionJson.toString());
                return mv;
            }

            // no need to do second ocr with another ocr
            resultJson = JSONObject.fromObject(searchResult.toString());
            resultJson.put("ocrTime", ocrResult.getExcuteTime());
            resultJson.put("searchTime", searchResult.getExcuteTime());
            resultJson
                    .put("statusCode", searchResult.getStatusCode().ordinal());
            resultJson.put("serverType", ServerType.SEARCH.ordinal());
            resultJson.put("ocrResult", ocrResult.getSchedulerResult());
        } else {
            // ocr fail
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrTime", ocrResult.getExcuteTime());
            resultJson.put("searchTime", 0);
            resultJson.put("statusCode", ocrResult.getStatusCode().ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        }

        stopTime = System.currentTimeMillis();
        resultJson.put("queryTime", stopTime - startTime);
        mv.addObject("searchResult", resultJson.toString());
        return mv;
    }

    /**
     * @param tableName
     *            String
     * @param startFid
     *            Long
     * @param endFid
     *            Long
     * @param startUid
     *            Integer
     * @param endUid
     *            Integer
     * @param startTime
     *            Long
     * @param endTime
     *            Long
     * @param startOrderNo
     *            Long
     * @param endOrderNo
     *            Long
     * @param startImgNo
     *            Integer
     * @param endImgNo
     *            Integer
     * @param threadNum
     *            Integer
     * @param isOldOrderNo
     *            Boolean
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/hbaseMigration", method = RequestMethod.POST)
    public ModelAndView handleHbaseMigrationRequest(
            @RequestParam(value = "tableName") String tableName,
            @RequestParam(value = "startFid", required = false) Long startFid,
            @RequestParam(value = "endFid", required = false) Long endFid,
            @RequestParam(value = "startUid", required = false) Integer startUid,
            @RequestParam(value = "endUid", required = false) Integer endUid,
            @RequestParam(value = "startTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "startOrderNo", required = false) Long startOrderNo,
            @RequestParam(value = "endOrderNo", required = false) Long endOrderNo,
            @RequestParam(value = "startImgNo", required = false) Integer startImgNo,
            @RequestParam(value = "endImgNo", required = false) Integer endImgNo,
            @RequestParam(value = "threadNum", required = false) Integer threadNum,
            @RequestParam(value = "isOldOrderNo", required = false) Boolean isOldOrderNo,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access hbaseMigration!", requestIP);
                return mv;
            }
        }

        debugLogger
                .info("tableName: {}, startFid: {}, endFid: {}, startUid: {}, endUid: {}, startTime: {}, endTime: {}, startOrderNo: {}, endOrderNo: {}, startImgNo: {}, endImgNo: {}, threadNum: {}, isOldOrderNo: {}",
                        tableName, startFid, endFid, startUid, endUid,
                        startTime, endTime, startOrderNo, endOrderNo,
                        startImgNo, endImgNo, threadNum, isOldOrderNo);

        if (threadNum != null && (threadNum <= 0 || threadNum > MAX_THREAD_NUM)) {
            mv.addObject("searchResult", "invalid threadNum!");
            return mv;
        } else {
            threadNum = MIN_THREAD_NUM;
        }

        switch (tableName) {
        case "feed":
            if (startFid < 0 || endFid < 0 || startFid > endFid) {
                mv.addObject("searchResult", "invalid fid!");
                return mv;
            }
            long feedFid = startFid;
            long feedFidStep = (endFid - (startFid - 1)) / threadNum;
            while ((feedFid + feedFidStep) < endFid) {
                schedulerAsyncUtil.migrateFeedTable(feedFid, feedFid
                        + feedFidStep);
                feedFid = feedFid + feedFidStep + 1;
            }
            schedulerAsyncUtil.migrateFeedTable(feedFid, endFid);
            break;
        case "userFeed":
            if (startUid < 0 || endUid < 0 || startFid < 0 || endFid < 0
                    || startUid > endUid || startFid > endFid) {
                mv.addObject("searchResult", "invalid uid or fid!");
                return mv;
            }
            int userFeedUid = startUid;
            int userFeedUidStep = (endUid - (startUid - 1)) / threadNum;
            while ((userFeedUid + userFeedUidStep) < endUid) {
                schedulerAsyncUtil.migrateUserFeedTable(userFeedUid,
                        userFeedUid + userFeedUidStep, startFid, endFid);
                userFeedUid = userFeedUid + userFeedUidStep + 1;
            }
            schedulerAsyncUtil.migrateUserFeedTable(userFeedUid, endUid,
                    startFid, endFid);
            break;
        case "timeFeed":
            if (startTime < 0 || endTime < 0 || startFid < 0 || endFid < 0
                    || startTime > endTime || startFid > endFid) {
                mv.addObject("searchResult", "invalid time or fid!");
                return mv;
            }
            long timeFeedTime = startTime;
            long timeFeedTimeStep = (endTime - (startTime - 1)) / threadNum;
            while ((timeFeedTime + timeFeedTimeStep) < endTime) {
                schedulerAsyncUtil.migrateTimeFeedTable(timeFeedTime,
                        timeFeedTime + timeFeedTimeStep, startFid, endFid);
                timeFeedTime = timeFeedTime + timeFeedTimeStep + 1;
            }
            schedulerAsyncUtil.migrateTimeFeedTable(timeFeedTime, endTime,
                    startFid, endFid);
            break;
        case "articleUserTime":
            if (startUid < 0 || endUid < 0 || startTime < 0 || endTime < 0
                    || startUid > endUid || startTime > endTime) {
                mv.addObject("searchResult", "invalid uid or time!");
                return mv;
            }
            int articleUserTimeUid = startUid;
            int articleUserTimeUidStep = (endUid - (startUid - 1)) / threadNum;
            while ((articleUserTimeUid + articleUserTimeUidStep) < endUid) {
                schedulerAsyncUtil.migrateArticleUserTimeTable(
                        articleUserTimeUid, articleUserTimeUid
                                + articleUserTimeUidStep, startTime, endTime);
                articleUserTimeUid = articleUserTimeUid
                        + articleUserTimeUidStep + 1;
            }
            schedulerAsyncUtil.migrateArticleUserTimeTable(articleUserTimeUid,
                    endUid, startTime, endTime);
            break;
        case "articleTimeUser":
            if (startTime < 0 || endTime < 0 || startUid < 0 || endUid < 0
                    || startTime > endTime || startUid > endUid) {
                mv.addObject("searchResult", "invalid time or uid!");
                return mv;
            }
            long articleTimeUserTime = startTime;
            long articleTimeUserTimeStep = (endTime - (startTime - 1))
                    / threadNum;
            while ((articleTimeUserTime + articleTimeUserTimeStep) < endTime) {
                schedulerAsyncUtil.migrateArticleTimeUserTable(
                        articleTimeUserTime, articleTimeUserTime
                                + articleTimeUserTimeStep, startUid, endUid);
                articleTimeUserTime = articleTimeUserTime
                        + articleTimeUserTimeStep + 1;
            }
            schedulerAsyncUtil.migrateArticleTimeUserTable(articleTimeUserTime,
                    endTime, startUid, endUid);
            break;
        case "subjectFilterFeed":
            if (startImgNo < 0 || endImgNo < 0 || startImgNo > endImgNo) {
                mv.addObject("searchResult", "invalid imgNo!");
                return mv;
            }
            int subjectFilterFeedImgNo = startImgNo;
            int subjectFilterFeedImgNoStep = (endImgNo - (startImgNo - 1))
                    / threadNum;
            while ((subjectFilterFeedImgNo + subjectFilterFeedImgNoStep) < endImgNo) {
                schedulerAsyncUtil.migrateSubjectFilterFeedTable(startOrderNo,
                        endOrderNo, subjectFilterFeedImgNo,
                        subjectFilterFeedImgNo + subjectFilterFeedImgNoStep,
                        isOldOrderNo);
                subjectFilterFeedImgNo = subjectFilterFeedImgNo
                        + subjectFilterFeedImgNoStep + 1;
            }
            schedulerAsyncUtil.migrateSubjectFilterFeedTable(startOrderNo,
                    endOrderNo, subjectFilterFeedImgNo, endImgNo, isOldOrderNo);
            break;
        case "subjectFilterUserFeed":
            int subjectFilterUserFeedUid = startUid;
            int subjectFilterUserFeedUidStep = (endUid - (startUid - 1))
                    / threadNum;
            while ((subjectFilterUserFeedUid + subjectFilterUserFeedUidStep) < endUid) {
                schedulerAsyncUtil.migrateSubjectFilterUserFeedTable(
                        subjectFilterUserFeedUid, subjectFilterUserFeedUid
                                + subjectFilterUserFeedUidStep, startOrderNo,
                        endOrderNo, startImgNo, endImgNo, isOldOrderNo);
                subjectFilterUserFeedUid = subjectFilterUserFeedUid
                        + subjectFilterUserFeedUidStep + 1;
            }
            schedulerAsyncUtil.migrateSubjectFilterUserFeedTable(
                    subjectFilterUserFeedUid, endUid, startOrderNo, endOrderNo,
                    startImgNo, endImgNo, isOldOrderNo);
            break;
        default:
            break;
        }

        mv.addObject(
                "searchResult",
                "finish migrate "
                        + tableName
                        + " table! Whether having errors or not, please check exception.log!");
        return mv;
    }

    /**
     * @param orderNo
     *            Long
     * @param imgNo
     *            Integer
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/querySubjectFilterFeed", method = RequestMethod.POST)
    public ModelAndView handleQuerySubjectFilterFeedRequest(
            @RequestParam(value = "orderNo") Long orderNo,
            @RequestParam(value = "imgNo") Integer imgNo,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
        mv.setViewName("queryResult");

        debugLogger.info("orderNo: {}, imgNo: {}", orderNo, imgNo);

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
                serverMonitorLogger.error(
                        "{} invalid ip access querySubjectFilterFeed!",
                        requestIP);
                return mv;
            }
        }

        try {
            SubjectFilterInfo subjectFilterInfo = schedulerConfiguration
                    .getHbClient().getSubjectFilterFeed(orderNo, imgNo);
            if (subjectFilterInfo != null) {
                logger.info(
                        "migrate subject filter feed old table, succeed read orderNo: {}, imgNo: {}",
                        orderNo, imgNo);
                mv.addObject("searchResult", "success!");
            } else {
                logger.error(
                        "migrate subject filter feed old table, null orderNo: {}, imgNo: {}",
                        orderNo, imgNo);
                mv.addObject("searchResult", "fail!");
            }
        } catch (IOException e) {
            logger.error(
                    "migrate subject filter feed old table, io exception orderNo: {}, imgNo: {}",
                    orderNo, imgNo);
        }

        return mv;
    }

    /**
     * @param img
     *            MultipartFile
     * @param fid
     *            String
     * @param uid
     *            String
     * @param app
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/openQuery", method = RequestMethod.POST)
    public ModelAndView handleOpenQueryRequest(
            @RequestParam(value = "file") MultipartFile img,
            @RequestParam(value = "fid", required = false) String fid,
            @RequestParam(value = "uid", required = false) String uid,
            @RequestParam(value = "app", required = false) String app,
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
                serverMonitorLogger.error("{} invalid ip access openQuery!",
                        requestIP);
                return mv;
            }
        }

        // 判断是否是Migu
        if (app == null || !"migu".equals(app)) {
            mv.addObject("searchResult", "you are not Mxxx!");
            return mv;
        }

        // 判断请求数是否超过最大限制
        if (schedulerControllerStatistics.getCurrentRequestNum() >= schedulerConfiguration
                .getSystemDataConfiguration().getQueryRequestLimit()) {
            mv.addObject("searchResult", "request denied!");
            return mv;
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // statistics
        schedulerControllerStatistics.incrementAndGetAllRequestNum();
        // int cr = schedulerControllerStatistics
        // .incrementAndGetCurrentRequestNum();
        // if (schedulerConfiguration.getDebugSwitchConfiguration()
        // .isCurrentRequestDebugSwitch()) {
        // debugLogger.info("{} {} Migu ICR: {}", uid, fid, cr);
        // }

        // ocr scheduler
        OcrParam ocrParam = new OcrParam();
        ocrParam.setSchedulerConfiguration(schedulerConfiguration);
        ocrParam.setImg(img);
        ocrParam.setFid(fid);
        ocrParam.setUid(uid);
        ocrParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
        ocrParam.setNlpStrategy(nlpStrategy);
        // choosing ocr server
        schedulerControllerStatistics.incrementAndGetCurrentCnnRequestNum();
        OcrServer ocrServer = chooseOcrServer(true);
        ocrParam.setOcrServer(ocrServer);
        OcrResult ocrResult = ocrStrategy.excute(ocrParam);
        schedulerControllerStatistics.decrementAndGetCurrentCnnRequestNum();

        SearchResult searchResult = null;
        if (!schedulerConfiguration.isExecOcrOnlySwitch()
                && ocrResult.getSchedulerResult() != null
                && !"".equals(ocrResult.getSchedulerResult())) {
            // ocr success
            if (schedulerConfiguration.isForceHbaseOnSwitch()
                    && schedulerConfiguration.isHbaseOnSwitch() && fid != null
                    && !fid.matches("^\\s*$") && uid != null
                    && !uid.matches("^\\s*$")) {
                try {
                    OcrHbaseResult miguOcrHbaseResult = new OcrHbaseResult();
                    miguOcrHbaseResult.setFid(Long.parseLong(fid));
                    miguOcrHbaseResult.setUid(Integer.parseInt(uid));
                    miguOcrHbaseResult.setOcrType(ocrResult.getOcrType());
                    miguOcrHbaseResult.setRotate(ocrResult.getRotate());
                    miguOcrHbaseResult.setSchedulerResult(ocrResult
                            .getSchedulerResult());
                    miguOcrHbaseResult.setOcrServer(ocrServer);
                    miguOcrHbaseQueue.offer(miguOcrHbaseResult);
                    schedulerControllerStatistics
                            .incrementAndGetMiguOcrHbaseResultNum();
                    saveHbaseData.saveMiguOcrHbaseData();
                } catch (Exception e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} {} SMO EX", app, uid, fid,
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
            searchParam.setOcrResult(ocrResult);
            searchParam.setFid(fid);
            searchParam.setUid(uid);
            searchParam.setIndex(index);
            searchParam.setUser(user);
            searchParam.setToken(token);
            searchParam.setLimit(limit != null ? limit : schedulerConfiguration
                    .getSystemDataConfiguration().getLimit());
            searchParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            searchParam.setSearchServer(searchServer);
            searchResult = searchStrategy.excute(searchParam);
            schedulerControllerStatistics
                    .decrementAndGetCurrentSearchRequestNum();

            if (searchResult.getSchedulerResult() != null) {
                if (searchResult.getSearchResultList() != null) {
                    // search result type:ok
                    if (schedulerConfiguration.isForceHbaseOnSwitch()
                            && schedulerConfiguration.isHbaseOnSwitch()
                            && fid != null && !fid.matches("^\\s*$")
                            && uid != null && !uid.matches("^\\s*$")) {
                        try {
                            SearchHbaseResult miguSearchHbaseResult = new SearchHbaseResult();
                            miguSearchHbaseResult.setFid(Long.parseLong(fid));
                            miguSearchHbaseResult.setUid(Integer.parseInt(uid));
                            miguSearchHbaseResult
                                    .setSearchResultList(searchResult
                                            .getSearchResultList());
                            miguSearchHbaseResult.setSearchServer(searchServer);
                            miguSearchHbaseQueue.offer(miguSearchHbaseResult);
                            schedulerControllerStatistics
                                    .incrementAndGetMiguSearchHbaseResultNum();
                            saveHbaseData.saveMiguSearchHbaseData();
                        } catch (Exception e) {
                            if (schedulerConfiguration
                                    .isSaveExceptionLogSwitch()) {
                                logger.error("{} {} {} {} SMS EX", app, uid,
                                        fid, searchServer.getId());
                            }
                        }
                    }
                }
            } else {
                // no search result
                stopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgQueryExecTimeDebugSwitch()) {
                    schedulerControllerStatistics.addAndGetExecTime(stopTime
                            - startTime);
                    schedulerControllerStatistics.incrementAndGetExecTimeNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isQueryExecTimeDebugSwitch()) {
                    debugLogger.info("{} {} {} Open ET: {}", app, uid, fid,
                            (stopTime - startTime));
                }

                // statistics
                // cr = schedulerControllerStatistics
                // .decrementAndGetCurrentRequestNum();
                // if (schedulerConfiguration.getDebugSwitchConfiguration()
                // .isCurrentRequestDebugSwitch()) {
                // debugLogger.info("{} {} DCR: {}", uid, fid, cr);
                // }
                exceptionJson.put("ocrTime", ocrResult.getExcuteTime());
                exceptionJson.put("searchTime", searchResult.getExcuteTime());
                exceptionJson.put("queryTime", stopTime - startTime);
                exceptionJson.put("statusCode", searchResult.getStatusCode()
                        .ordinal());
                exceptionJson.put("serverType", ServerType.SEARCH.ordinal());
                mv.addObject("searchResult", exceptionJson.toString());
                return mv;
            }

            resultJson = JSONObject.fromObject(searchResult.toString());
            resultJson.put("ocrTime", ocrResult.getExcuteTime());
            resultJson.put("searchTime", searchResult.getExcuteTime());
            resultJson
                    .put("statusCode", searchResult.getStatusCode().ordinal());
            resultJson.put("serverType", ServerType.SEARCH.ordinal());
        } else if (schedulerConfiguration.isExecOcrOnlySwitch()
                && ocrResult.getSchedulerResult() != null) {
            debugLogger.info("{} {} {} Open Ocr result: {}", app, uid, fid,
                    ocrResult.getSchedulerResult());
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrResult", ocrResult.getSchedulerResult());
            resultJson.put("ocrTime", ocrResult.getExcuteTime());
            resultJson.put("searchTime", 0);
            resultJson.put("statusCode", ocrResult.getStatusCode().ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        } else {
            // ocr fail
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrTime", ocrResult.getExcuteTime());
            resultJson.put("searchTime", 0);
            resultJson.put("statusCode", ocrResult.getStatusCode().ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        }
        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgQueryExecTimeDebugSwitch()) {
            schedulerControllerStatistics.addAndGetExecTime(stopTime
                    - startTime);
            schedulerControllerStatistics.incrementAndGetExecTimeNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isQueryExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} Open ET: {}", app, uid, fid,
                    (stopTime - startTime));
        }

        // cr =
        // schedulerControllerStatistics.decrementAndGetCurrentRequestNum();
        // if (schedulerConfiguration.getDebugSwitchConfiguration()
        // .isCurrentRequestDebugSwitch()) {
        // debugLogger.info("{} {} DCR: {}", uid, fid, cr);
        // }

        resultJson.put("queryTime", stopTime - startTime);
        mv.addObject("searchResult", resultJson.toString());
        return mv;
    }

    /**
     * @param img
     *            MultipartFile
     * @param fid
     *            String
     * @param uid
     *            String
     * @param app
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/sdkQuery", method = RequestMethod.POST)
    public ModelAndView handleSdkQueryRequest(
            @RequestParam(value = "file") MultipartFile img,
            @RequestParam(value = "fid", required = false) String fid,
            @RequestParam(value = "uid", required = false) String uid,
            @RequestParam(value = "app", required = false) String app,
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
                serverMonitorLogger.error("{} invalid ip access sdkQuery!",
                        requestIP);
                return mv;
            }
        }

        // 判断是否是Sdk
        if (app == null || !"sdk".equals(app)) {
            mv.addObject("searchResult", "you are not Sxx!");
            return mv;
        }

        // 判断请求数是否超过最大限制
        if (schedulerControllerStatistics.getCurrentRequestNum() >= schedulerConfiguration
                .getSystemDataConfiguration().getQueryRequestLimit()) {
            mv.addObject("searchResult", "request denied!");
            return mv;
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // statistics
        schedulerControllerStatistics.incrementAndGetAllRequestNum();
        // int cr = schedulerControllerStatistics
        // .incrementAndGetCurrentRequestNum();
        // if (schedulerConfiguration.getDebugSwitchConfiguration()
        // .isCurrentRequestDebugSwitch()) {
        // debugLogger.info("{} {} Sdk ICR: {}", uid, fid, cr);
        // }

        // ocr scheduler
        OcrParam ocrParam = new OcrParam();
        ocrParam.setSchedulerConfiguration(schedulerConfiguration);
        ocrParam.setImg(img);
        ocrParam.setFid(fid);
        ocrParam.setUid(uid);
        ocrParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
        ocrParam.setNlpStrategy(nlpStrategy);
        // choosing ocr server
        schedulerControllerStatistics.incrementAndGetCurrentCnnRequestNum();
        OcrServer ocrServer = chooseOcrServer(true);
        ocrParam.setOcrServer(ocrServer);
        OcrResult ocrResult = ocrStrategy.excute(ocrParam);
        schedulerControllerStatistics.decrementAndGetCurrentCnnRequestNum();

        SearchResult searchResult = null;
        if (!schedulerConfiguration.isExecOcrOnlySwitch()
                && ocrResult.getSchedulerResult() != null
                && !"".equals(ocrResult.getSchedulerResult())) {
            // ocr success
            if (schedulerConfiguration.isForceHbaseOnSwitch()
                    && schedulerConfiguration.isHbaseOnSwitch() && fid != null
                    && !fid.matches("^\\s*$") && uid != null
                    && !uid.matches("^\\s*$")) {
                try {
                    OcrHbaseResult sdkOcrHbaseResult = new OcrHbaseResult();
                    sdkOcrHbaseResult.setFid(Long.parseLong(fid));
                    sdkOcrHbaseResult.setUid(Integer.parseInt(uid));
                    sdkOcrHbaseResult.setOcrType(ocrResult.getOcrType());
                    sdkOcrHbaseResult.setRotate(ocrResult.getRotate());
                    sdkOcrHbaseResult.setSchedulerResult(ocrResult
                            .getSchedulerResult());
                    sdkOcrHbaseResult.setOcrServer(ocrServer);
                    sdkOcrHbaseQueue.offer(sdkOcrHbaseResult);
                    schedulerControllerStatistics
                            .incrementAndGetSdkOcrHbaseResultNum();
                    saveHbaseData.saveSdkOcrHbaseData();
                } catch (Exception e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} SSDKO EX", uid, fid,
                                ocrServer.getId());
                    }
                }
            }

            schedulerControllerStatistics
                    .incrementAndGetCurrentSearchRequestNum();
            // choosing sdk search server
            SearchServer searchServer = chooseSdkSearchServer();
            // search scheduler
            SearchParam searchParam = new SearchParam();
            searchParam.setSchedulerConfiguration(schedulerConfiguration);
            searchParam.setOcrResult(ocrResult);
            searchParam.setFid(fid);
            searchParam.setUid(uid);
            searchParam.setLimit(schedulerConfiguration
                    .getSystemDataConfiguration().getLimit());
            searchParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            searchParam.setSearchServer(searchServer);
            searchResult = sdkSearchStrategy.excute(searchParam);
            schedulerControllerStatistics
                    .decrementAndGetCurrentSearchRequestNum();

            if (searchResult.getSchedulerResult() != null) {
                if (searchResult.getSearchResultList() != null) {
                    // search result type:ok
                    if (schedulerConfiguration.isForceHbaseOnSwitch()
                            && schedulerConfiguration.isHbaseOnSwitch()
                            && fid != null && !fid.matches("^\\s*$")
                            && uid != null && !uid.matches("^\\s*$")) {
                        try {
                            SearchHbaseResult sdkSearchHbaseResult = new SearchHbaseResult();
                            sdkSearchHbaseResult.setFid(Long.parseLong(fid));
                            sdkSearchHbaseResult.setUid(Integer.parseInt(uid));
                            sdkSearchHbaseResult
                                    .setSearchResultList(searchResult
                                            .getSearchResultList());
                            sdkSearchHbaseResult.setSearchServer(searchServer);
                            sdkSearchHbaseQueue.offer(sdkSearchHbaseResult);
                            schedulerControllerStatistics
                                    .incrementAndGetSdkSearchHbaseResultNum();
                            saveHbaseData.saveSdkSearchHbaseData();
                        } catch (Exception e) {
                            if (schedulerConfiguration
                                    .isSaveExceptionLogSwitch()) {
                                logger.error("{} {} {} SSDKS EX", uid, fid,
                                        searchServer.getId());
                            }
                        }
                    }
                }
            } else {
                // no search result
                stopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgQueryExecTimeDebugSwitch()) {
                    schedulerControllerStatistics.addAndGetExecTime(stopTime
                            - startTime);
                    schedulerControllerStatistics.incrementAndGetExecTimeNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isQueryExecTimeDebugSwitch()) {
                    debugLogger.info("{} {} Sdk ET: {}", uid, fid,
                            (stopTime - startTime));
                }

                // statistics
                // cr = schedulerControllerStatistics
                // .decrementAndGetCurrentRequestNum();
                // if (schedulerConfiguration.getDebugSwitchConfiguration()
                // .isCurrentRequestDebugSwitch()) {
                // debugLogger.info("{} {} DCR: {}", uid, fid, cr);
                // }
                exceptionJson.put("ocrTime", ocrResult.getExcuteTime());
                exceptionJson.put("searchTime", searchResult.getExcuteTime());
                exceptionJson.put("queryTime", stopTime - startTime);
                exceptionJson.put("statusCode", searchResult.getStatusCode()
                        .ordinal());
                exceptionJson.put("serverType", ServerType.SEARCH.ordinal());
                mv.addObject("searchResult", exceptionJson.toString());
                return mv;
            }

            resultJson = JSONObject.fromObject(searchResult.toString());
            resultJson.put("ocrTime", ocrResult.getExcuteTime());
            resultJson.put("searchTime", searchResult.getExcuteTime());
            resultJson
                    .put("statusCode", searchResult.getStatusCode().ordinal());
            resultJson.put("serverType", ServerType.SEARCH.ordinal());
        } else if (schedulerConfiguration.isExecOcrOnlySwitch()
                && ocrResult.getSchedulerResult() != null) {
            debugLogger.info("{} {} Sdk Ocr result: {}", uid, fid,
                    ocrResult.getSchedulerResult());
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrResult", ocrResult.getSchedulerResult());
            resultJson.put("ocrTime", ocrResult.getExcuteTime());
            resultJson.put("searchTime", 0);
            resultJson.put("statusCode", ocrResult.getStatusCode().ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        } else {
            // ocr fail
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrTime", ocrResult.getExcuteTime());
            resultJson.put("searchTime", 0);
            resultJson.put("statusCode", ocrResult.getStatusCode().ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        }
        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgQueryExecTimeDebugSwitch()) {
            schedulerControllerStatistics.addAndGetExecTime(stopTime
                    - startTime);
            schedulerControllerStatistics.incrementAndGetExecTimeNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isQueryExecTimeDebugSwitch()) {
            debugLogger.info("{} {} Sdk ET: {}", uid, fid,
                    (stopTime - startTime));
        }

        // cr =
        // schedulerControllerStatistics.decrementAndGetCurrentRequestNum();
        // if (schedulerConfiguration.getDebugSwitchConfiguration()
        // .isCurrentRequestDebugSwitch()) {
        // debugLogger.info("{} {} DCR: {}", uid, fid, cr);
        // }

        resultJson.put("queryTime", stopTime - startTime);
        mv.addObject("searchResult", resultJson.toString());
        return mv;
    }

    /**
     * @param beginFid
     *            Long
     * @param endFid
     *            Long
     * @param beginSimilarity
     *            Float
     * @param endSimilarity
     *            Float
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/filterHbaseByFidSimilarity", method = RequestMethod.POST)
    public ModelAndView handleFilterHbaseByFidSimilarityRequest(
            @RequestParam(value = "beginFid") Long beginFid,
            @RequestParam(value = "endFid") Long endFid,
            @RequestParam(value = "beginSimilarity") Float beginSimilarity,
            @RequestParam(value = "endSimilarity") Float endSimilarity,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access filterHbaseByFidSimilarity!",
                        requestIP);
                return mv;
            }
        }

        // invalid fid or similarity
        if (beginFid > endFid) {
            mv.addObject("searchResult",
                    "beginFid must less than or equal to endFid!");
            return mv;
        }
        if (beginSimilarity > endSimilarity) {
            mv.addObject("searchResult",
                    "beginSimilarity must less than or equal to endSimilarity!");
            return mv;
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        JSONObject hbaseResultJson = new JSONObject();
        JSONArray hbaseResultJsonArray = new JSONArray();
        for (long fid = beginFid; fid <= endFid; ++fid) {
            JSONObject subHbaseResultJson = new JSONObject();
            subHbaseResultJson.put("fid", fid);
            boolean isValid = false;

            Future<String> futureOcrResult = null;
            try {
                futureOcrResult = retrieveHbaseData
                        .retrieveOcrResultFromHbase(fid);
                String ocrResult = futureOcrResult.get(schedulerConfiguration
                        .getTimeoutConfiguration().getHbaseTimeout(),
                        TimeUnit.MILLISECONDS);
                if (ocrResult == null || "".equals(ocrResult)) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase no ocr result", fid);
                    }
                    isValid = false;
                } else {
                    subHbaseResultJson.put("ocr", ocrResult);
                    isValid = true;
                }
            } catch (TimeoutException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase ocr TE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
                isValid = false;
            } catch (ExecutionException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase ocr EE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
                isValid = false;
            } catch (InterruptedException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase ocr IE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
                isValid = false;
            } catch (NumberFormatException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase ocr NFE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
                isValid = false;
            }

            Future<List<com.xueba100.mining.common.SearchResult>> futureSearchResult = null;
            try {
                futureSearchResult = retrieveHbaseData
                        .retrieveSearchResultFromHbase(fid);
                List<com.xueba100.mining.common.SearchResult> searchResultList = futureSearchResult
                        .get(schedulerConfiguration.getTimeoutConfiguration()
                                .getHbaseTimeout(), TimeUnit.MILLISECONDS);
                if (searchResultList != null
                        && searchResultList.size() > 0
                        && searchResultList.get(0).getSimilarity() >= beginSimilarity
                        && searchResultList.get(0).getSimilarity() <= endSimilarity) {
                    subHbaseResultJson.put("similarity", searchResultList
                            .get(0).getSimilarity());
                    isValid = true;
                } else {
                    // no search result in hbase
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase no search result", fid);
                    }
                    isValid = false;
                }
            } catch (TimeoutException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase search TE", fid);
                }
                if (futureSearchResult != null) {
                    if (!futureSearchResult.isCancelled()) {
                        futureSearchResult.cancel(true);
                    }
                }
                isValid = false;
            } catch (ExecutionException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase search EE", fid);
                }
                if (futureSearchResult != null) {
                    if (!futureSearchResult.isCancelled()) {
                        futureSearchResult.cancel(true);
                    }
                }
                isValid = false;
            } catch (InterruptedException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase search IE", fid);
                }
                if (futureSearchResult != null) {
                    if (!futureSearchResult.isCancelled()) {
                        futureSearchResult.cancel(true);
                    }
                }
                isValid = false;
            } catch (NumberFormatException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("{} hbase search NFE", fid);
                }
                if (futureSearchResult != null) {
                    if (!futureSearchResult.isCancelled()) {
                        futureSearchResult.cancel(true);
                    }
                }
                isValid = false;
            }
            if (isValid) {
                hbaseResultJsonArray.add(subHbaseResultJson);
                debugLogger.info("fid: {} is valid!", fid);
            } else {
                debugLogger.warn("fid: {} is invalid!", fid);
            }
        }

        stopTime = System.currentTimeMillis();
        if (hbaseResultJsonArray.size() > 0) {
            hbaseResultJson.put("queryTime", stopTime - startTime);
            hbaseResultJson.put("results", hbaseResultJsonArray);
            // 写入fidResults文件
            try {
                File fidResults = new File("logs/fidResults_" + beginFid + "_"
                        + endFid);
                FileWriter fw = new FileWriter(fidResults);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(hbaseResultJson.toString());
                bw.close();
            } catch (IOException e) {
                logger.error("write to fidResults_{}_{} IOE!", beginFid, endFid);
            }
            mv.addObject("searchResult", "ok for fid from " + beginFid + " to "
                    + endFid);
        } else {
            hbaseResultJson.put("queryTime", stopTime - startTime);
            mv.addObject("searchResult", "no result for fid from " + beginFid
                    + " to " + endFid);
        }

        return mv;
    }

    /**
     * @param ocrResults
     *            MultipartFile
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/wordSearchByFile", method = RequestMethod.POST)
    public ModelAndView handleWordSearchByFileRequest(
            @RequestParam(value = "file") MultipartFile ocrResults,
            @RequestParam(value = "index", required = false) String index,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access wordSearchByFile!", requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // save file
        File file = new File("logs/wordSearchByFile_" + startTime);
        try {
            ocrResults.transferTo(file);
            String s = null;
            JSONObject resultJson = new JSONObject();
            JSONArray resultJsonArray = new JSONArray();
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while ((s = br.readLine()) != null) {
                JSONObject subResultJson = new JSONObject();
                subResultJson.put("ocr", s);
                schedulerControllerStatistics
                        .incrementAndGetCurrentSearchRequestNum();
                // choosing search server
                SearchServer searchServer = chooseSearchServer();
                // search scheduler
                SearchParam searchParam = new SearchParam();
                searchParam.setSchedulerConfiguration(schedulerConfiguration);
                searchParam.setSearchServer(searchServer);
                OcrResult ocrResult = new OcrResult();
                ocrResult.setSchedulerResult(s);
                searchParam.setOcrResult(ocrResult);
                searchParam.setIndex(index);
                searchParam.setUser(user);
                searchParam.setToken(token);
                searchParam.setLimit(limit != null ? limit
                        : schedulerConfiguration.getSystemDataConfiguration()
                                .getLimit());
                searchParam.setFid("WSBFR");
                searchParam.setUid("WSBFR");
                searchParam
                        .setSchedulerControllerStatistics(schedulerControllerStatistics);
                SearchResult searchResult = searchStrategy.excute(searchParam);
                schedulerControllerStatistics
                        .decrementAndGetCurrentSearchRequestNum();
                if (searchResult.getSchedulerResult() != null) {
                    JSONObject searchResultJson = null;
                    JSONArray questionsJsonArray = new JSONArray();
                    try {
                        // 把json字符串转换成json对象
                        searchResultJson = JSONObject.fromObject(searchResult
                                .getSchedulerResult());
                        if (SEARCH_RESULT_TYPE_OK.equals(searchResultJson
                                .getString(SEARCH_RESULT_TYPE))) {
                            questionsJsonArray = searchResultJson
                                    .getJSONArray(SEARCH_RESULT_QUESTIONS);
                            subResultJson
                                    .put("search",
                                            questionsJsonArray
                                                    .getJSONObject(0)
                                                    .getString(
                                                            SEARCH_RESULT_STEM_HTML));
                            subResultJson
                                    .put("similarity",
                                            questionsJsonArray
                                                    .getJSONObject(0)
                                                    .getDouble(
                                                            SEARCH_RESULT_SIMILARITY));
                        } else {
                            // search result is empty
                            subResultJson.put("search", "");
                            subResultJson.put("similarity", -1);
                        }
                    } catch (JSONException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} {} {} JE", searchParam.getUid(),
                                    searchParam.getFid(), searchServer.getId());
                        }
                        searchServer.getSearchServerStatistics()
                                .incrementAndGetJeNum();
                        subResultJson.put("search", "");
                        subResultJson.put("similarity", -1);
                    }
                } else {
                    subResultJson.put("search", "");
                    subResultJson.put("similarity", -1);
                }
                resultJsonArray.add(subResultJson);

                stopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgWordSearchExecTimeDebugSwitch()) {
                    schedulerControllerStatistics
                            .addAndGetExecTimeByWordSearch(stopTime - startTime);
                    schedulerControllerStatistics
                            .incrementAndGetExecTimeByWordSearchNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isWordSearchExecTimeDebugSwitch()) {
                    debugLogger.info("{} {} {} ET By Word Search By File: {}",
                            searchParam.getUid(), searchParam.getFid(),
                            searchServer.getId(), (stopTime - startTime));
                }
            }
            if (resultJsonArray.size() > 0) {
                stopTime = System.currentTimeMillis();
                resultJson.put("queryTime", stopTime - startTime);
                resultJson.put("results", resultJsonArray);
                mv.addObject("searchResult", resultJson.toString());
            } else {
                mv.addObject("searchResult",
                        "no result for word search by file!");
            }
            br.close();
        } catch (IOException e) {
            logger.error("Original file:{}, Transfer file:{} IOE!",
                    ocrResults.getOriginalFilename(), file.getName());
            mv.addObject("searchResult", "IOE!");
        } finally {
            file.delete();
        }

        return mv;
    }

    /**
     * @param img
     *            MultipartFile
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/imgEnhance", method = RequestMethod.POST)
    public ModelAndView handleImgEnhanceRequest(
            @RequestParam(value = "file") MultipartFile img,
            HttpServletRequest request, HttpServletResponse response) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error("{} invalid ip access imgEnhance!",
                        requestIP);
                return mv;
            }
        }

        // long startTime = System.currentTimeMillis();
        // long stopTime;

        // choosing ie server
        IeServer ieServer = chooseIeServer();
        // ie query scheduler
        JzhParam ieParam = new JzhParam();
        ieParam.setSchedulerConfiguration(schedulerConfiguration);
        ieParam.setIeServer(ieServer);
        ieParam.setImg(img);
        ieParam.setFid("IED");
        ieParam.setUid("IED");
        ieParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
        JzhResult ieResult = jzhStrategy.excute(ieParam);
        if (StatusCode.OK.equals(ieResult.getStatusCode())) {
            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition", "attachment;fileName="
                    + img.getOriginalFilename());
            response.setHeader("Content-Length",
                    String.valueOf(ieResult.getEntityContentAsBytes().length));
            debugLogger.info("contentLength: {}",
                    ieResult.getEntityContentAsBytes().length); // TODO
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            boolean hasException = false;
            try {
                bis = new BufferedInputStream(new ByteArrayInputStream(
                        ieResult.getEntityContentAsBytes()));
                bos = new BufferedOutputStream(response.getOutputStream());
                byte[] buff = new byte[IE_BUFF_LENGTH];
                int bytesRead;
                while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                    bos.write(buff, 0, bytesRead);
                    logger.error("buff length: {}", bytesRead); // TODO
                }
            } catch (IOException e) {
                logger.error("IE open file name: {}, IOE!",
                        img.getOriginalFilename());
                mv.addObject("searchResult", "IOE!");
                hasException = true;
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException e) {
                    logger.error("IE close file name: {}, IOE!"
                            + img.getOriginalFilename());
                    mv.addObject("searchResult", "IOE!");
                    hasException = true;
                }
            }
            if (hasException) {
                return mv;
            } else {
                return null;
            }
        } else {
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }

        // stopTime = System.currentTimeMillis();

    }

    /**
     * @param beginFid
     *            Long
     * @param endFid
     *            Long
     * @param step
     *            Integer
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/filterHbaseByFidStep", method = RequestMethod.POST)
    public ModelAndView handleFilterHbaseByFidStepRequest(
            @RequestParam(value = "beginFid") Long beginFid,
            @RequestParam(value = "endFid") Long endFid,
            @RequestParam(value = "step", required = false) Integer step,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger
                        .error("{} invalid ip access filterHbaseByFidStep!",
                                requestIP);
                return mv;
            }
        }

        // invalid fid or similarity
        if (beginFid > endFid) {
            mv.addObject("searchResult",
                    "beginFid must less than or equal to endFid!");
            return mv;
        }
        int fidStep = 1;
        if (step > 0) {
            fidStep = step;
        }

        // 写入similarityResults文件
        try {
            File fidResults = new File("logs/similarityResults_" + beginFid
                    + "_" + endFid);
            FileWriter fw = new FileWriter(fidResults);
            BufferedWriter bw = new BufferedWriter(fw);
            for (long fid = beginFid; fid <= endFid; fid += fidStep) {
                Future<List<com.xueba100.mining.common.SearchResult>> futureSearchResult = null;
                try {
                    futureSearchResult = retrieveHbaseData
                            .retrieveSearchResultFromHbase(fid);
                    List<com.xueba100.mining.common.SearchResult> searchResultList = futureSearchResult
                            .get(schedulerConfiguration
                                    .getTimeoutConfiguration()
                                    .getHbaseTimeout(), TimeUnit.MILLISECONDS);
                    if (searchResultList != null && searchResultList.size() > 0) {
                        bw.write(String.valueOf(searchResultList.get(0)
                                .getSimilarity()));
                        bw.newLine();
                    }
                } catch (TimeoutException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase search TE", fid);
                    }
                    if (futureSearchResult != null) {
                        if (!futureSearchResult.isCancelled()) {
                            futureSearchResult.cancel(true);
                        }
                    }
                } catch (ExecutionException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase search EE", fid);
                    }
                    if (futureSearchResult != null) {
                        if (!futureSearchResult.isCancelled()) {
                            futureSearchResult.cancel(true);
                        }
                    }
                } catch (InterruptedException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase search IE", fid);
                    }
                    if (futureSearchResult != null) {
                        if (!futureSearchResult.isCancelled()) {
                            futureSearchResult.cancel(true);
                        }
                    }
                } catch (NumberFormatException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase search NFE", fid);
                    }
                    if (futureSearchResult != null) {
                        if (!futureSearchResult.isCancelled()) {
                            futureSearchResult.cancel(true);
                        }
                    }
                }
            }
            bw.close();
        } catch (IOException e) {
            logger.error("write to similarityResults_{}_{} IOE!", beginFid,
                    endFid);
            mv.addObject("searchResult", "IOE for fid from " + beginFid
                    + " to " + endFid);
            return mv;
        }

        mv.addObject("searchResult", "ok for fid from " + beginFid + " to "
                + endFid);
        return mv;
    }

    /**
     * @param fid
     *            String
     * @param uid
     *            String
     * @param keywords
     *            String
     * @param searchResult
     *            String
     * @param app
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/openWordSearchHbase", method = RequestMethod.POST)
    public ModelAndView handleOpenWordSearchHbaseRequest(
            @RequestParam(value = "fid") String fid,
            @RequestParam(value = "uid") String uid,
            @RequestParam(value = "keywords") String keywords,
            @RequestParam(value = "searchResult") String searchResult,
            @RequestParam(value = "app", required = false) String app,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access openWordSearchHbase!", requestIP);
                return mv;
            }
        }

        // 判断是否是Migu
        if (app == null || !"migu".equals(app)) {
            mv.addObject("searchResult", "you are not Mxxx!");
            return mv;
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // open word search scheduler
        SearchResult openWordSearchResult = getWordSearchResult(fid, uid,
                searchResult);
        JSONObject statusCodeJson = new JSONObject();
        if (openWordSearchResult.getSearchResultList() != null
                && openWordSearchResult.getSearchResultList().size() > 0) {
            statusCodeJson.put("statusCode", StatusCode.OK.ordinal());
            mv.addObject("searchResult", statusCodeJson.toString());
            if (schedulerConfiguration.isForceHbaseOnSwitch()
                    && schedulerConfiguration.isHbaseOnSwitch() && fid != null
                    && !fid.matches("^\\s*$") && uid != null
                    && !uid.matches("^\\s*$")) {
                try {
                    OcrHbaseResult openOcrHbaseResult = new OcrHbaseResult();
                    openOcrHbaseResult.setFid(Long.parseLong(fid));
                    openOcrHbaseResult.setUid(Integer.parseInt(uid));
                    openOcrHbaseResult.setSchedulerResult(keywords);
                    miguOcrHbaseWordSearchQueue.offer(openOcrHbaseResult);
                    schedulerControllerStatistics
                            .incrementAndGetMiguOcrHbaseResultNum();
                    saveHbaseData.saveMiguWordSearchOcrHbaseData();

                    SearchHbaseResult openSearchHbaseResult = new SearchHbaseResult();
                    openSearchHbaseResult.setFid(Long.parseLong(fid));
                    openSearchHbaseResult.setUid(Integer.parseInt(uid));
                    openSearchHbaseResult
                            .setSearchResultList(openWordSearchResult
                                    .getSearchResultList());
                    miguSearchHbaseWordSearchQueue.offer(openSearchHbaseResult);
                    schedulerControllerStatistics
                            .incrementAndGetMiguSearchHbaseResultNum();
                    saveHbaseData.saveMiguWordSearchSearchHbaseData();
                } catch (Exception e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} {} {} MWSOS EX", app, uid, fid);
                    }
                }
            }
        } else {
            statusCodeJson.put("statusCode", StatusCode.NORESULT.ordinal());
            mv.addObject("searchResult", statusCodeJson.toString());
        }

        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isWordSearchExecTimeDebugSwitch()) {
            debugLogger.info("{} {} {} ET By Open Word Search Hbase: {}", app,
                    uid, fid, (stopTime - startTime));
        }

        return mv;
    }

    /**
     * @param prepareLessonResults
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/savePrepareLessonResults", method = RequestMethod.POST)
    public ModelAndView handleSavePrepareLessonResultsRequest(
            @RequestParam(value = "prepareLessonResults") String prepareLessonResults,
            HttpServletRequest request) {
        // return value
        JSONObject resultJson = new JSONObject();
        resultJson.put("type", "ok");

        ModelAndView mv = new ModelAndView();
        mv.setViewName("queryResult");
        mv.addObject("searchResult", resultJson.toString());
        debugLogger.info("prepareLessonResults: {}", prepareLessonResults);

        if (prepareLessonResults == null
                || prepareLessonResults.matches("^\\s*$")) {
            logger.error("prepareLessonResults is empty!");
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }

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
                    logger.error("savePrepareLessonResults IE!");
                    continue;
                }
                logger.error("savePrepareLessonResults JCE!");
                continue;
            }
        }

        if (jedis != null) {
            JSONObject prepareLessonResultsJson = JSONObject
                    .fromObject(prepareLessonResults);
            String teacherPenSerial = prepareLessonResultsJson
                    .getString("teacherPenSerial");
            String transferFileNameAndPage = prepareLessonResultsJson
                    .getString("transferFileNameAndPage");

            // 保存题目信息
            jedis.set(teacherPenSerial + ":" + transferFileNameAndPage,
                    prepareLessonResults);
            debugLogger
                    .info("teacherPenSerial: {}, transferFileNameAndPage: {}, prepareLessonResults: {}",
                            teacherPenSerial, transferFileNameAndPage,
                            prepareLessonResults); // TODO
            jedis.close();
        } else {
            logger.error("savePrepareLessonResults Get Redis Fail!");
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }

        return mv;
    }

    /**
     * @param teacherPenSerial
     *            String
     * @param transferFileName
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/retrievePrepareLessonResults", method = RequestMethod.POST)
    public ModelAndView handleRetrievePrepareLessonResultsRequest(
            @RequestParam(value = "teacherPenSerial") String teacherPenSerial,
            @RequestParam(value = "transferFileNameAndPage") String transferFileNameAndPage,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
        mv.setViewName("queryResult");
        debugLogger.info("teacherPenSerial: {}, transferFileNameAndPage: {}",
                teacherPenSerial, transferFileNameAndPage);

        if (teacherPenSerial == null || teacherPenSerial.matches("^\\s*$")) {
            logger.error("teacherPenSerial is empty!");
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }

        if (transferFileNameAndPage == null
                || transferFileNameAndPage.matches("^\\s*$")) {
            logger.error("transferFileNameAndPage is empty!");
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }

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
                    logger.error("retrievePrepareLessonResults IE!");
                    continue;
                }
                logger.error("retrievePrepareLessonResults JCE!");
                continue;
            }
        }

        if (jedis != null) {
            String prepareLessonResults = jedis.get(teacherPenSerial + ":"
                    + transferFileNameAndPage);
            jedis.close();
            if (prepareLessonResults == null
                    || prepareLessonResults.matches("^\\s*$")) {
                logger.error("prepareLessonResults is empty!");
                mv.addObject("searchResult", exceptionJson.toString());
                return mv;
            } else {
                JSONObject prepareLessonResultsJson = JSONObject
                        .fromObject(prepareLessonResults);
                prepareLessonResultsJson.put("type", "ok");
                debugLogger
                        .info("teacherPenSerial: {}, transferFileNameAndPage: {}, prepareLessonResults: {}",
                                teacherPenSerial, transferFileNameAndPage,
                                prepareLessonResults); // TODO
                mv.addObject("searchResult",
                        prepareLessonResultsJson.toString());
                return mv;
            }
        } else {
            logger.error("retrievePrepareLessonResults Get Redis Fail!");
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }
    }

    /**
     * @param img
     *            MultipartFile
     * @param rectIndex
     *            Integer
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/handwriteOcr", method = RequestMethod.POST)
    public ModelAndView handleHandwriteOcrRequest(
            @RequestParam(value = "file") MultipartFile img,
            @RequestParam(value = "rectIndex") Integer rectIndex,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
        mv.setViewName("queryResult");
        JSONObject resultJson = null;

        // 当白名单开关使能后，判断请求ip是否有效
        // if (schedulerConfiguration.isWhiteListOnSwitch()) {
        // String requestIP = request.getRemoteAddr();
        // boolean isValidIP = false;
        // for (ConfigServer configServer : schedulerConfiguration
        // .getConfigServers()) {
        // if (configServer.getIp().equals(requestIP)) {
        // isValidIP = true;
        // break;
        // }
        // }
        // if (!isValidIP) {
        // mv.addObject("searchResult", "who are you!");
        // serverMonitorLogger.error("{} invalid ip access query!",
        // requestIP);
        // return mv;
        // }
        // }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // handwrite ocr scheduler
        OcrParam handwriteOcrParam = new OcrParam();
        handwriteOcrParam.setSchedulerConfiguration(schedulerConfiguration);
        if (img == null) {
            schedulerControllerStatistics.incrementAndGetImgNullNum();
        }
        handwriteOcrParam.setImg(img);
        handwriteOcrParam.setFid("HWO");
        handwriteOcrParam.setUid("HWO");
        handwriteOcrParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        handwriteOcrParam.setUseLayoutinfoOrNot(false);
        OcrServer handwriteOcrServer = chooseHandwriteOcrServer();
        handwriteOcrParam.setOcrServer(handwriteOcrServer);
        OcrResult handwriteOcrResult = handwriteOcrStrategy
                .excute(handwriteOcrParam);

        if (handwriteOcrResult.getSchedulerResult() != null
                && !"".equals(handwriteOcrResult.getSchedulerResult())) {
            debugLogger.info("Handwrite Ocr result: {}",
                    handwriteOcrResult.getSchedulerResult());
            resultJson = new JSONObject();
            resultJson.put("type", "ok");
            resultJson.put("handwriteOcrResult",
                    handwriteOcrResult.getSchedulerResult());
            resultJson.put("rectIndex", rectIndex);
            resultJson.put("handwriteOcrTime",
                    handwriteOcrResult.getExcuteTime());
            resultJson.put("statusCode", handwriteOcrResult.getStatusCode()
                    .ordinal());
            resultJson.put("serverType", handwriteOcrServer.getServerType()
                    .ordinal());
        } else {
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("handwriteOcrResult",
                    handwriteOcrResult.getSchedulerResult());
            resultJson.put("handwriteOcrTime",
                    handwriteOcrResult.getExcuteTime());
            resultJson.put("statusCode", handwriteOcrResult.getStatusCode()
                    .ordinal());
            resultJson.put("serverType", handwriteOcrServer.getServerType()
                    .ordinal());
        }

        stopTime = System.currentTimeMillis();
        resultJson.put("queryTime", stopTime - startTime);
        mv.addObject("searchResult", resultJson.toString());
        return mv;
    }

    /**
     * @param prepareLessonDirectory
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/savePrepareLessonDirectory", method = RequestMethod.POST)
    public ModelAndView handleSavePrepareLessonDirectoryRequest(
            @RequestParam(value = "prepareLessonDirectory") String prepareLessonDirectory,
            HttpServletRequest request) {
        // return value
        JSONObject resultJson = new JSONObject();
        resultJson.put("type", "ok");

        ModelAndView mv = new ModelAndView();
        mv.setViewName("queryResult");

        debugLogger.info("prepareLessonDirectory: {}", prepareLessonDirectory);

        if (prepareLessonDirectory == null
                || prepareLessonDirectory.matches("^\\s*$")) {
            logger.error("prepareLessonDirectory is empty!");
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }

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
                    logger.error("savePrepareLessonDirectory IE!");
                    continue;
                }
                logger.error("savePrepareLessonDirectory JCE!");
                continue;
            }
        }

        if (jedis != null) {
            JSONObject prepareLessonDirectoryJson = JSONObject
                    .fromObject(prepareLessonDirectory);
            String teacherPenSerial = prepareLessonDirectoryJson
                    .getString("teacherPenSerial");
            String transferFileName = prepareLessonDirectoryJson
                    .getString("transferFileName");
            String fileName = prepareLessonDirectoryJson.getString("fileName");
            int pageNum = prepareLessonDirectoryJson.getInt("pageNum");
            JSONArray pageSerials = prepareLessonDirectoryJson
                    .getJSONArray("pageSerials");

            // 保存目录信息 TODO
            JSONObject penSerialDirectoryJson = null;
            JSONArray penSerialDirectoryJsonArray = null;
            JSONObject subPenSerialDirectoryJson = new JSONObject();
            String penSerialDirectory = jedis.get(teacherPenSerial);
            if (penSerialDirectory == null
                    || penSerialDirectory.matches("^\\s*$")) {
                penSerialDirectoryJson = new JSONObject();
                penSerialDirectoryJsonArray = new JSONArray();
            } else {
                penSerialDirectoryJson = JSONObject
                        .fromObject(penSerialDirectory);
                penSerialDirectoryJsonArray = penSerialDirectoryJson
                        .getJSONArray("penSerial");
                for (int i = 0; i < penSerialDirectoryJsonArray.size(); ++i) {
                    if (fileName.equals(penSerialDirectoryJsonArray
                            .getJSONObject(i).getString("fileName"))) {
                        penSerialDirectoryJsonArray.remove(i);
                        break;
                    }
                }
            }
            subPenSerialDirectoryJson.put("fileName", fileName);
            subPenSerialDirectoryJson.put("transferFileName", transferFileName);
            subPenSerialDirectoryJson.put("pageNum", pageNum);
            subPenSerialDirectoryJson.put("pageSerials", pageSerials);
            penSerialDirectoryJsonArray.add(subPenSerialDirectoryJson);
            penSerialDirectoryJson
                    .put("penSerial", penSerialDirectoryJsonArray);
            debugLogger.info("teacherPenSerial: {}, penSerialDirectory: {}",
                    teacherPenSerial, penSerialDirectoryJson.toString()); // TODO
            jedis.set(teacherPenSerial, penSerialDirectoryJson.toString());
            jedis.close();
            resultJson.put(teacherPenSerial, penSerialDirectoryJson.toString()); // TODO
            mv.addObject("searchResult", resultJson.toString());
        } else {
            logger.error("savePrepareLessonDirectory Get Redis Fail!");
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }

        return mv;
    }

    /**
     * @param teacherPenSerial
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/retrievePrepareLessonDirectory", method = RequestMethod.POST)
    public ModelAndView handleRetrievePrepareLessonDirectoryRequest(
            @RequestParam(value = "teacherPenSerial") String teacherPenSerial,
            HttpServletRequest request) {
        // return value

        ModelAndView mv = new ModelAndView();
        mv.setViewName("queryResult");

        debugLogger.info("teacherPenSerial: {}", teacherPenSerial);

        if (teacherPenSerial == null || teacherPenSerial.matches("^\\s*$")) {
            logger.error("teacherPenSerial is empty!");
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }

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
                    logger.error("retrievePrepareLessonDirectory IE!");
                    continue;
                }
                logger.error("retrievePrepareLessonDirectory JCE!");
                continue;
            }
        }

        if (jedis != null) {
            // 获取目录信息 TODO
            String penSerialDirectory = jedis.get(teacherPenSerial);
            jedis.close();
            if (penSerialDirectory == null
                    || penSerialDirectory.matches("^\\s*$")) {
                logger.error(
                        "teacherPenSerial: {}, penSerialDirectory is empty!",
                        teacherPenSerial);
                mv.addObject("searchResult", exceptionJson.toString());
                return mv;
            } else {
                JSONObject resultJson = JSONObject
                        .fromObject(penSerialDirectory);
                resultJson.put("type", "ok");
                debugLogger.info(
                        "teacherPenSerial: {}, penSerialDirectory: {}",
                        teacherPenSerial, resultJson.toString()); // TODO
                mv.addObject("searchResult", resultJson.toString());
                return mv;
            }
        } else {
            logger.error("retrievePrepareLessonDirectory Get Redis Fail!");
            mv.addObject("searchResult", exceptionJson.toString());
            return mv;
        }
    }

    /**
     * @param beginFid
     *            Long
     * @param endFid
     *            Long
     * @param beginSimilarity
     *            Float
     * @param endSimilarity
     *            Float
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/countHbaseByFidSimilarityWithFile", method = RequestMethod.POST)
    public ModelAndView handleCountHbaseByFidSimilarityWithFileRequest(
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "beginSimilarity") Float beginSimilarity,
            @RequestParam(value = "endSimilarity") Float endSimilarity,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger
                        .error("{} invalid ip access countHbaseByFidSimilarityWithFile!",
                                requestIP);
                return mv;
            }
        }

        // invalid similarity
        if (beginSimilarity > endSimilarity) {
            mv.addObject("searchResult",
                    "beginSimilarity must less than or equal to endSimilarity!");
            return mv;
        }

        long startTime = System.currentTimeMillis();
        int count = 0;

        // save file
        File fidsFile = new File("logs/countHbaseByFidSimilarityWithFile_"
                + startTime);
        try {
            file.transferTo(fidsFile);
            String s = null;
            JSONObject resultJson = new JSONObject();
            FileReader fr = new FileReader(fidsFile);
            BufferedReader br = new BufferedReader(fr);
            Future<List<com.xueba100.mining.common.SearchResult>> futureSearchResult = null;
            while ((s = br.readLine()) != null) {
                try {
                    futureSearchResult = retrieveHbaseData
                            .retrieveSearchResultFromHbase(Long.parseLong(s));
                    List<com.xueba100.mining.common.SearchResult> searchResultList = futureSearchResult
                            .get(schedulerConfiguration
                                    .getTimeoutConfiguration()
                                    .getHbaseTimeout(), TimeUnit.MILLISECONDS);
                    if (searchResultList != null
                            && searchResultList.size() > 0
                            && searchResultList.get(0).getSimilarity() >= beginSimilarity
                            && searchResultList.get(0).getSimilarity() < endSimilarity) {
                        debugLogger.info("fid: {}, similarity {}", s,
                                searchResultList.get(0).getSimilarity());
                        ++count;
                    } else {
                        // no search result in hbase
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("{} hbase no search result", s);
                        }
                    }
                } catch (TimeoutException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase search TE", s);
                    }
                    if (futureSearchResult != null) {
                        if (!futureSearchResult.isCancelled()) {
                            futureSearchResult.cancel(true);
                        }
                    }
                } catch (ExecutionException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase search EE", s);
                    }
                    if (futureSearchResult != null) {
                        if (!futureSearchResult.isCancelled()) {
                            futureSearchResult.cancel(true);
                        }
                    }
                } catch (InterruptedException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase search IE", s);
                    }
                    if (futureSearchResult != null) {
                        if (!futureSearchResult.isCancelled()) {
                            futureSearchResult.cancel(true);
                        }
                    }
                } catch (NumberFormatException e) {
                    if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                        logger.error("{} hbase search NFE", s);
                    }
                    if (futureSearchResult != null) {
                        if (!futureSearchResult.isCancelled()) {
                            futureSearchResult.cancel(true);
                        }
                    }
                }
            }
            long stopTime = System.currentTimeMillis();
            if (count > 0) {
                resultJson.put("type", "ok");
                resultJson.put("count", count);
                resultJson.put("fileName", file.getOriginalFilename());
                resultJson.put("queryTime", stopTime - startTime);
                mv.addObject("searchResult", resultJson.toString());
                debugLogger.info("total count: {} in {}.", count,
                        file.getOriginalFilename());
            } else {
                resultJson.put("type", "empty");
                resultJson.put("fileName", file.getOriginalFilename());
                resultJson.put("queryTime", stopTime - startTime);
                mv.addObject("searchResult", resultJson.toString());
                debugLogger
                        .info("no result in {}.", file.getOriginalFilename());
            }
            br.close();
        } catch (IOException e) {
            logger.error("Transfer file:{} IOE!", file.getName());
            logger.error("Original file:{}, Transfer file:{} IOE!",
                    file.getOriginalFilename(), fidsFile.getName());
            mv.addObject("searchResult", "IOE!");
        } finally {
            fidsFile.delete();
        }

        return mv;
    }

    /**
     * @param fid
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/queryOcrResultByFid", method = RequestMethod.POST)
    public ModelAndView handleQueryOcrResultByFidRequest(
            @RequestParam(value = "fid") String fid, HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access queryOcrResultByFid!", requestIP);
                return mv;
            }
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        JSONObject hbaseResultJson = new JSONObject();
        hbaseResultJson.put("type", "exception");
        hbaseResultJson.put("ocrResult", "");
        if (fid != null && !fid.matches("^\\s*$")) {
            Future<String> futureOcrResult = null;
            try {
                futureOcrResult = retrieveHbaseData
                        .retrieveOcrResultFromHbase(Long.parseLong(fid.trim()));
                String ocrResult = futureOcrResult.get(schedulerConfiguration
                        .getTimeoutConfiguration().getHbaseTimeout(),
                        TimeUnit.MILLISECONDS);
                if (ocrResult != null && !ocrResult.matches("^\\s*$")) {
                    hbaseResultJson.put("type", "ok");
                    hbaseResultJson.put("ocrResult", ocrResult);
                } else {
                    hbaseResultJson.put("type", "empty");
                }
            } catch (TimeoutException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("queryOcrResultByFid fid:{} hbase ocr TE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
            } catch (ExecutionException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("queryOcrResultByFid fid:{} hbase ocr EE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
            } catch (InterruptedException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("queryOcrResultByFid fid:{} hbase ocr IE", fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
            } catch (NumberFormatException e) {
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("queryOcrResultByFid fid:{} hbase ocr NFE",
                            fid);
                }
                if (futureOcrResult != null) {
                    if (!futureOcrResult.isCancelled()) {
                        futureOcrResult.cancel(true);
                    }
                }
            }
        } else {
            mv.addObject("searchResult", "invalid fid!");
            return mv;
        }

        stopTime = System.currentTimeMillis();
        hbaseResultJson.put("queryTime", stopTime - startTime);
        mv.addObject("searchResult", hbaseResultJson.toString());

        return mv;
    }

    /**
     * @param img
     *            MultipartFile
     * @param limit
     *            Integer
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/queryWithLimit", method = RequestMethod.POST)
    public ModelAndView handleQueryWithLimitRequest(
            @RequestParam(value = "file") MultipartFile img,
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
        if (schedulerControllerStatistics.getCurrentRequestNum() >= schedulerConfiguration
                .getSystemDataConfiguration().getQueryRequestLimit()) {
            mv.addObject("searchResult", "request denied!");
            return mv;
        }

        long startTime = System.currentTimeMillis();
        long stopTime;

        // ocr scheduler
        OcrParam ocrParam = new OcrParam();
        ocrParam.setSchedulerConfiguration(schedulerConfiguration);
        if (img == null) {
            schedulerControllerStatistics.incrementAndGetImgNullNum();
        }
        ocrParam.setImg(img);
        ocrParam.setFid("QWL");
        ocrParam.setUid("QWL");
        ocrParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
        ocrParam.setNlpStrategy(nlpStrategy);
        // choosing ocr server
        boolean choosingCnn = false;
        int weightForChoosingOcr = r.nextInt(schedulerConfiguration
                .getSystemDataConfiguration().getOcrServerTotalWeight());
        if (weightForChoosingOcr < schedulerConfiguration
                .getSystemDataConfiguration().getOcrServerCnnWeight()) {
            choosingCnn = true;
        }
        OcrServer ocrServer = chooseOcrServer(choosingCnn);
        ocrParam.setOcrServer(ocrServer);
        OcrResult firstOcrResult = ocrStrategy.excute(ocrParam);
        SearchResult firstSearchResult = null;
        OcrResult secondOcrResult = null;
        SearchResult secondSearchResult = null;

        if (!schedulerConfiguration.isExecOcrOnlySwitch()
                && firstOcrResult.getSchedulerResult() != null
                && !"".equals(firstOcrResult.getSchedulerResult())) {
            // first ocr success
            // choosing search server
            SearchServer searchServer = chooseSearchServer();
            // search scheduler
            SearchParam searchParam = new SearchParam();
            searchParam.setSchedulerConfiguration(schedulerConfiguration);
            searchParam.setOcrResult(firstOcrResult);
            searchParam.setFid("QWL");
            searchParam.setUid("QWL");
            searchParam.setIndex(index);
            searchParam.setUser(user);
            searchParam.setToken(token);
            searchParam.setLimit(limit != null ? limit : schedulerConfiguration
                    .getSystemDataConfiguration().getLimit());
            searchParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            searchParam.setSearchServer(searchServer);
            firstSearchResult = searchStrategy.excute(searchParam);

            // 获取search result的最大相似度
            float maxSimilarity = 0f;
            if (firstSearchResult.getSchedulerResult() != null) {
                if (firstSearchResult.getSearchResultList() != null) {
                    // first search result type:ok
                    float firstSimilarity = firstSearchResult
                            .getMaxSimilarity();
                    if (firstSimilarity > maxSimilarity) {
                        maxSimilarity = firstSimilarity;
                    }
                }
            } else {
                // no first search result
                stopTime = System.currentTimeMillis();
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isAvgQueryExecTimeDebugSwitch()) {
                    schedulerControllerStatistics.addAndGetExecTime(stopTime
                            - startTime);
                    schedulerControllerStatistics.incrementAndGetExecTimeNum();
                }
                if (schedulerConfiguration.getDebugSwitchConfiguration()
                        .isQueryExecTimeDebugSwitch()) {
                    debugLogger.info("QWL ET: {}", (stopTime - startTime));
                }

                // statistics
                exceptionJson.put("ocrTime", firstOcrResult.getExcuteTime());
                exceptionJson.put("ocrType", OcrType.CNN.getValue());
                exceptionJson.put("ocrResult",
                        firstOcrResult.getSchedulerResult());
                exceptionJson.put("searchTime",
                        firstSearchResult.getExcuteTime());
                exceptionJson.put("queryTime", stopTime - startTime);
                exceptionJson.put("statusCode", firstSearchResult
                        .getStatusCode().ordinal());
                exceptionJson.put("serverType", ServerType.SEARCH.ordinal());
                mv.addObject("searchResult", exceptionJson.toString());
                return mv;
            }

            // 根据search相似度，判断是否执行第2次Ocr
            if (maxSimilarity < schedulerConfiguration
                    .getSystemDataConfiguration().getThresUnusedResult()
                    && firstOcrResult.getOcrAndNlpExcuteTime() < (schedulerConfiguration
                            .getTimeoutConfiguration().getConnectTimeout() * 2 + schedulerConfiguration
                            .getTimeoutConfiguration().getOcrTimeout())) {
                // second ocr with handwrite ocr
                ocrServer = chooseHandwriteOcrServer();
                ocrParam.setOcrServer(ocrServer);
                ocrParam.setLayoutinfo(firstOcrResult.getLayoutinfo() != null ? firstOcrResult
                        .getLayoutinfo() : "");
                ocrParam.setRotate(firstOcrResult.getRotate());
                ocrParam.setUseLayoutinfoOrNot(true);
                ocrParam.setOcrAndNlpExcuteTime(firstOcrResult
                        .getOcrAndNlpExcuteTime());
                secondOcrResult = handwriteOcrStrategy.excute(ocrParam);

                if (secondOcrResult.getSchedulerResult() != null
                        && !"".equals(secondOcrResult.getSchedulerResult())) {
                    // handwrite ocr success
                    // second search
                    searchParam.setOcrResult(secondOcrResult);
                    secondSearchResult = searchStrategy.excute(searchParam);

                    // 获取第2次search result的最大相似度
                    if (secondSearchResult.getSchedulerResult() != null) {
                        if (secondSearchResult.getSearchResultList() != null) {
                            // second search result type:ok
                            float secondSimilarity = secondSearchResult
                                    .getMaxSimilarity();
                            if (secondSimilarity > maxSimilarity) {
                                // save second search result as final result
                                resultJson = JSONObject
                                        .fromObject(secondSearchResult
                                                .toString());
                                resultJson.put("ocrTime",
                                        firstOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrTime",
                                        secondOcrResult.getExcuteTime());
                                resultJson.put("ocrType",
                                        OcrType.HANDWRITE_OCR_STATUS_0
                                                .getValue());
                                resultJson.put("ocrResult",
                                        secondOcrResult.getSchedulerResult());
                                resultJson.put(
                                        "searchTime",
                                        firstSearchResult.getExcuteTime()
                                                + secondSearchResult
                                                        .getExcuteTime());
                            } else {
                                // save first search result as final result
                                resultJson = JSONObject
                                        .fromObject(firstSearchResult
                                                .toString());
                                resultJson.put("ocrTime",
                                        firstOcrResult.getExcuteTime());
                                resultJson.put("handwriteOcrTime",
                                        secondOcrResult.getExcuteTime());
                                resultJson.put("ocrType",
                                        OcrType.HANDWRITE_OCR_STATUS_0
                                                .getValue());
                                resultJson.put("ocrResult",
                                        firstOcrResult.getSchedulerResult());
                                resultJson.put(
                                        "searchTime",
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
                            resultJson.put("ocrType",
                                    OcrType.HANDWRITE_OCR_STATUS_0.getValue());
                            resultJson.put("ocrResult",
                                    firstOcrResult.getSchedulerResult());
                            resultJson.put(
                                    "searchTime",
                                    firstSearchResult.getExcuteTime()
                                            + secondSearchResult
                                                    .getExcuteTime());
                        }
                        resultJson.put("statusCode", secondSearchResult
                                .getStatusCode().ordinal());
                        resultJson.put("serverType",
                                ServerType.SEARCH.ordinal());
                    } else {
                        // no second search result
                        resultJson = JSONObject.fromObject(firstSearchResult
                                .toString());
                        resultJson.put("ocrTime",
                                firstOcrResult.getExcuteTime());
                        resultJson.put("handwriteOcrTime",
                                secondOcrResult.getExcuteTime());
                        resultJson.put("ocrType",
                                OcrType.HANDWRITE_OCR_STATUS_0.getValue());
                        resultJson.put("ocrResult",
                                firstOcrResult.getSchedulerResult());
                        resultJson.put("searchTime",
                                firstSearchResult.getExcuteTime()
                                        + secondSearchResult.getExcuteTime());
                        resultJson.put("statusCode", secondSearchResult
                                .getStatusCode().ordinal());
                        resultJson.put("serverType",
                                ServerType.SEARCH.ordinal());
                    }
                } else {
                    // second ocr fail
                    resultJson = JSONObject.fromObject(firstSearchResult
                            .toString());
                    resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
                    resultJson.put("handwriteOcrTime",
                            secondOcrResult.getExcuteTime());
                    resultJson.put("ocrType", OcrType.HANDWRITE_OCR.getValue());
                    resultJson.put("ocrResult",
                            firstOcrResult.getSchedulerResult());
                    resultJson.put("searchTime",
                            firstSearchResult.getExcuteTime());
                    resultJson.put("statusCode", secondOcrResult
                            .getStatusCode().ordinal());
                    resultJson.put("serverType", ocrServer.getServerType()
                            .ordinal());
                }
            } else {
                // no need to do handwrite ocr with another ocr
                resultJson = JSONObject
                        .fromObject(firstSearchResult.toString());
                resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
                resultJson.put("ocrType", OcrType.CNN.getValue());
                resultJson
                        .put("ocrResult", firstOcrResult.getSchedulerResult());
                resultJson.put("searchTime", firstSearchResult.getExcuteTime());
                resultJson.put("statusCode", firstSearchResult.getStatusCode()
                        .ordinal());
                resultJson.put("serverType", ServerType.SEARCH.ordinal());
            }
        } else if (schedulerConfiguration.isExecOcrOnlySwitch()
                && firstOcrResult.getSchedulerResult() != null) {
            debugLogger.info("QWL Ocr result: {}",
                    firstOcrResult.getSchedulerResult());
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrResult", firstOcrResult.getSchedulerResult());
            resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
            resultJson.put("ocrType", OcrType.CNN.getValue());
            resultJson.put("statusCode", firstOcrResult.getStatusCode()
                    .ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        } else {
            // first ocr fail
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("ocrTime", firstOcrResult.getExcuteTime());
            resultJson.put("ocrType", OcrType.CNN.getValue());
            resultJson.put("ocrResult", "");
            resultJson.put("searchTime", 0);
            resultJson.put("statusCode", firstOcrResult.getStatusCode()
                    .ordinal());
            resultJson.put("serverType", ocrServer.getServerType().ordinal());
        }
        stopTime = System.currentTimeMillis();
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isAvgQueryExecTimeDebugSwitch()) {
            schedulerControllerStatistics.addAndGetExecTime(stopTime
                    - startTime);
            schedulerControllerStatistics.incrementAndGetExecTimeNum();
        }
        if (schedulerConfiguration.getDebugSwitchConfiguration()
                .isQueryExecTimeDebugSwitch()) {
            debugLogger.info("QWL ET: {}", stopTime - startTime);
        }

        // save exception log: query exec time >= query timeout
        if (schedulerConfiguration.isSaveTimeoutLogSwitch()
                && (stopTime - startTime) >= schedulerConfiguration
                        .getTimeoutConfiguration().getQueryTimeout()) {
            timeoutLogger
                    .error("QWL ET: {}>={} firstOcrTime: {} secondOcrTime: {} firstSearchTime: {} secondSearchTime: {} firstNlpTime: {} secondNlpTime: {}",
                            (stopTime - startTime),
                            schedulerConfiguration.getTimeoutConfiguration()
                                    .getQueryTimeout(),
                            firstOcrResult.getExcuteTime(),
                            secondOcrResult != null ? secondOcrResult
                                    .getExcuteTime() : 0,
                            firstSearchResult.getExcuteTime(),
                            secondSearchResult != null ? secondSearchResult
                                    .getExcuteTime() : 0,
                            firstOcrResult.getNlpExcuteTime(),
                            secondOcrResult != null ? secondOcrResult
                                    .getNlpExcuteTime() : 0);
        }

        resultJson.put("queryTime", stopTime - startTime);
        mv.addObject("searchResult", resultJson.toString());
        return mv;
    }

    /**
     * @param uid
     *            Integer
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/getFeedsByUid", method = { RequestMethod.GET,
            RequestMethod.POST })
    public ModelAndView handleGetFeedsByUidRequest(
            @RequestParam(value = "uid") Integer uid, HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access filterHbaseByFidSimilarity!",
                        requestIP);
                return mv;
            }
        }

        // long startTime = System.currentTimeMillis();
        // long stopTime;

        // JSONObject hbaseResultJson = new JSONObject();
        JSONArray hbaseResultJsonArray = new JSONArray();
        boolean isValid = false;

        Future<List<Feed>> futureFeedList = null;
        try {
            futureFeedList = retrieveHbaseData.retrieveFeedsFromHbase(uid);
            List<Feed> feedList = futureFeedList.get(schedulerConfiguration
                    .getTimeoutConfiguration().getHbaseTimeout() * 5,
                    TimeUnit.MILLISECONDS);
            if (feedList != null && feedList.size() > 0) {
                isValid = true;
                for (int feedIndex = 0; feedIndex < feedList.size(); ++feedIndex) {
                    JSONObject subHbaseResultJson = new JSONObject();
                    subHbaseResultJson.put("createTime", feedList
                            .get(feedIndex).getCreateTime());
                    subHbaseResultJson.put("fid", feedList.get(feedIndex)
                            .getFid());
                    hbaseResultJsonArray.add(subHbaseResultJson);
                }
            } else {
                // no search result in hbase
                if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                    logger.error("getFeedsByUid:{} hbase no feed", uid);
                }
            }
        } catch (TimeoutException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getFeedsByUid:{} hbase get feed TE", uid);
            }
            if (futureFeedList != null) {
                if (!futureFeedList.isCancelled()) {
                    futureFeedList.cancel(true);
                }
            }
        } catch (ExecutionException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getFeedsByUid:{} hbase get feed EE", uid);
            }
            if (futureFeedList != null) {
                if (!futureFeedList.isCancelled()) {
                    futureFeedList.cancel(true);
                }
            }
        } catch (InterruptedException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getFeedsByUid:{} hbase get feed IE", uid);
            }
            if (futureFeedList != null) {
                if (!futureFeedList.isCancelled()) {
                    futureFeedList.cancel(true);
                }
            }
        } catch (NumberFormatException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("getFeedsByUid:{} hbase get feed NFE", uid);
            }
            if (futureFeedList != null) {
                if (!futureFeedList.isCancelled()) {
                    futureFeedList.cancel(true);
                }
            }
        }
        if (isValid) {
            mv.addObject("searchResult", hbaseResultJsonArray.toString());
        } else {
            mv.addObject("searchResult", "no feed!");
        }

        // stopTime = System.currentTimeMillis();

        return mv;
    }

    /**
     * @param fids
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/getFeedsByFids", method = { RequestMethod.GET,
            RequestMethod.POST })
    public ModelAndView handleGetFeedsByFidsRequest(
            @RequestParam(value = "fids") String fids,
            HttpServletRequest request) {
        // return value
        ModelAndView mv = new ModelAndView();
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
                serverMonitorLogger.error(
                        "{} invalid ip access queryHbaseByFid!", requestIP);
                return mv;
            }
        }

        String[] fidArray = fids.trim().split(",");
        List<Feed> feedList = new ArrayList<Feed>();
        if (fidArray.length > 0) {
            for (String fid : fidArray) {
                if (fid != null && !"".equals(fid)) {
                    Future<Feed> futureFeed = null;
                    try {
                        futureFeed = retrieveHbaseData
                                .retrieveFeedFromHbase(Long.parseLong(fid
                                        .trim()));
                        Feed feed = futureFeed.get(schedulerConfiguration
                                .getTimeoutConfiguration().getHbaseTimeout(),
                                TimeUnit.MILLISECONDS);
                        if (feed != null) {
                            feedList.add(feed);
                        }
                    } catch (TimeoutException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("getFeedsByFids:{} hbase get feed TE",
                                    fid);
                        }
                        if (futureFeed != null) {
                            if (!futureFeed.isCancelled()) {
                                futureFeed.cancel(true);
                            }
                        }
                    } catch (ExecutionException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("getFeedsByFids:{} hbase get feed EE",
                                    fid);
                        }
                        if (futureFeed != null) {
                            if (!futureFeed.isCancelled()) {
                                futureFeed.cancel(true);
                            }
                        }
                    } catch (InterruptedException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error("getFeedsByFids:{} hbase get feed IE",
                                    fid);
                        }
                        if (futureFeed != null) {
                            if (!futureFeed.isCancelled()) {
                                futureFeed.cancel(true);
                            }
                        }
                    } catch (NumberFormatException e) {
                        if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                            logger.error(
                                    "getFeedsByFids:{} hbase get feed NFE", fid);
                        }
                        if (futureFeed != null) {
                            if (!futureFeed.isCancelled()) {
                                futureFeed.cancel(true);
                            }
                        }
                    }
                }
            }
        }

        if (feedList.size() > 0) {
            try {
                mv.addObject("searchResult",
                        mapper.writeValueAsString(feedList));
            } catch (JsonProcessingException e) {
                mv.addObject("searchResult", "JsonProcessingException");
            }
        } else {
            mv.addObject("searchResult", exceptionJson.toString());
        }

        return mv;
    }

    /**
     * @param img
     *            MultipartFile
     * @param bookids
     *            String
     * @param request
     *            HttpServletRequest
     * @return result ModelAndView
     */
    @RequestMapping(value = "/matrixQuery", method = RequestMethod.POST)
    public ModelAndView handleMatrixQueryRequest(
            @RequestParam(value = "file") MultipartFile img,
            @RequestParam(value = "bookids", required = false) String bookIds,
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

        // ocr scheduler
        UgcParam ocrParam = new UgcParam();
        ocrParam.setSchedulerConfiguration(schedulerConfiguration);
        if (img == null) {
            schedulerControllerStatistics.incrementAndGetImgNullNum();
        }
        ocrParam.setImg(img);
        ocrParam.setFid("Matrix");
        ocrParam.setUid("Matrix");
        ocrParam.setSchedulerControllerStatistics(schedulerControllerStatistics);
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
        schedulerControllerStatistics.decrementAndGetCurrentCnnRequestNum();
        SearchResult firstSearchResult = null;

        if (firstOcrResult.getSchedulerResult() != null
                && !"".equals(firstOcrResult.getSchedulerResult())) {
            // first ocr success
            // choosing search matrix server
            SearchServer searchMatrixServer = chooseSearchMatrixServer();
            // search scheduler
            SearchParam searchParam = new SearchParam();
            searchParam.setSchedulerConfiguration(schedulerConfiguration);
            searchParam.setOcrResult(firstOcrResult);
            searchParam.setFid("Matrix");
            searchParam.setUid("Matrix");
            searchParam.setBookIds(bookIds);
            searchParam
                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
            searchParam.setSearchServer(searchMatrixServer);
            firstSearchResult = searchMatrixStrategy.excute(searchParam);
            if (firstSearchResult.getSchedulerResult() != null
                    && StatusCode.OK.equals(firstSearchResult.getStatusCode())) {
                mv.addObject("searchResult", firstSearchResult.toString());
                return mv;
            } else {
                resultJson = JSONObject.fromObject(exceptionJson.toString());
                resultJson
                        .put("serverType", ServerType.SEARCH_MATRIX.ordinal());
                resultJson.put("statusCode", firstSearchResult.getStatusCode()
                        .ordinal());
                mv.addObject("searchResult", resultJson.toString());
                return mv;
            }
        } else {
            // first ocr fail
            resultJson = JSONObject.fromObject(exceptionJson.toString());
            resultJson.put("serverType", ServerType.CNN.ordinal());
            resultJson.put("statusCode", firstOcrResult.getStatusCode()
                    .ordinal());
            mv.addObject("searchResult", resultJson.toString());
            return mv;
        }
    }

    /**
     * @param request
     * @return result
     */
    @RequestMapping(value = "/test")
    public ModelAndView handleTestRequest(HttpServletRequest request) {
        // FileSystem fs = FileSystem.get(conf);
        ModelAndView mv = new ModelAndView();
        mv.setViewName("queryResult");
        mv.addObject("searchResult", request.getSession().getId());
        return mv;
    }

    /**
     * @return result
     */
    @RequestMapping(value = "/login")
    public ModelAndView handleLoginRequest() {
        User user = new User();
        ModelAndView mv = new ModelAndView();
        mv.setViewName("login");
        mv.addObject(user);
        return mv;
    }

    /**
     * @param loginParam
     *            Login Parameter
     * @return result
     */
    @RequestMapping(value = "/loginProcess", method = RequestMethod.POST)
    public ModelAndView handleLoginProcess(@ModelAttribute("user") User user) {
        ModelAndView mv = new ModelAndView();
        String s = null;
        try {
            File users = new File("users");
            FileReader fr = new FileReader(users);
            BufferedReader br = new BufferedReader(fr);
            s = br.readLine();
            br.close();
        } catch (IOException e) {
            logger.error("IOE!");
        }

        JSONObject usersData = JSONObject.fromObject(s);
        JSONArray usersArrary = usersData.getJSONArray("users");
        if (usersArrary.size() > 0) {
            boolean hasUser = false;
            for (int i = 0; i < usersArrary.size(); i++) {
                JSONObject userData = (JSONObject) usersArrary.get(i);
                if (userData.getString("userName").equals(
                        MD5Util.md5(user.getUserName()))
                        && userData.getString("password").equals(
                                MD5Util.md5(user.getPassword()))) {
                    hasUser = true;
                    break;
                }
            }
            if (hasUser) {
                mv.setViewName("config");
                mv.addObject("schedulerId", schedulerConfiguration
                        .getSystemDataConfiguration().getSchedulerId());
                mv.addObject("ocrServerCnnWeight", schedulerConfiguration
                        .getSystemDataConfiguration().getOcrServerCnnWeight());
                mv.addObject("thresUnusedResult", schedulerConfiguration
                        .getSystemDataConfiguration().getThresUnusedResult());
                mv.addObject("limit", schedulerConfiguration
                        .getSystemDataConfiguration().getLimit());
                mv.addObject("cnnServerList",
                        schedulerConfiguration.getCnnServers());
                mv.addObject("cnnUnusedServerList",
                        schedulerConfiguration.getCnnUnusedServers());
                mv.addObject("javaServerList",
                        schedulerConfiguration.getJavaServers());
                mv.addObject("javaUnusedServerList",
                        schedulerConfiguration.getJavaUnusedServers());
                mv.addObject("searchServerList",
                        schedulerConfiguration.getSearchServers());
                mv.addObject("biServerList",
                        schedulerConfiguration.getBiServers());
            } else {
                mv.setViewName("error");
                mv.addObject("message", "用户名或者密码错误！");
            }
        } else {
            mv.setViewName("error");
            mv.addObject("message", "无可用用户！");
        }
        return mv;
    }

    /**
     * @param request
     *            HttpServletRequest
     * @return result
     */
    @RequestMapping(value = "/configProcess", method = RequestMethod.POST)
    public ModelAndView handleConfigProcess(HttpServletRequest request) {
        ConfigParam configParam = new ConfigParam();
        configParam.setSchedulerConfiguration(schedulerConfiguration);
        configParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        JSONObject configData = JSONObject.fromObject((request
                .getParameter("configData")));
        configParam.setConfigData(configData);
        configFromHtmlStrategy.excute(configParam);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("info");
        mv.addObject("message", "配置成功！");
        return mv;
    }

    /**
     * @param request
     *            HttpServletRequest
     * @return result
     */
    @RequestMapping(value = "/bi")
    public ModelAndView handleBIRequest() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("bi");
        mv.addObject("schedulerId", schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerId());
        mv.addObject("allRequestNum",
                schedulerControllerStatistics.getAllRequestNum());
        mv.addObject("cnnServerList", schedulerConfiguration.getCnnServers());
        mv.addObject("javaServerList", schedulerConfiguration.getJavaServers());
        mv.addObject("searchServerList",
                schedulerConfiguration.getSearchServers());
        return mv;
    }

    /**
     * @param host
     *            server host ip
     * @return result
     */
    // @RequestMapping(value = "/heartbeat")
    public void handleServerHeartBeat(HttpServletRequest request) {
        // @RequestHeader String host
        // String host = request.getRemoteHost();
        // logger.info(host);
    }

    /**
     * @param choosingCnn
     * @return ocr server
     */
    private OcrServer chooseOcrServer(boolean choosingCnn) {
        OcrServer ocrServer = null;
        if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (choosingCnn) {
                if (schedulerConfiguration.getCnnTotalWeight() == 0) {
                    return ocrServer;
                }
                int weightForChooingOcr = r.nextInt(schedulerConfiguration
                        .getCnnTotalWeight());
                for (Entry<Integer, OcrServer> entry : schedulerConfiguration
                        .getCnnWeightTable().entrySet()) {
                    if (weightForChooingOcr < entry.getKey()) {
                        ocrServer = entry.getValue();
                        break;
                    }
                }
            } else {
                if (schedulerConfiguration.getJavaTotalWeight() == 0) {
                    return ocrServer;
                }
                int weightForChooingOcr = r.nextInt(schedulerConfiguration
                        .getJavaTotalWeight());
                for (Entry<Integer, OcrServer> entry : schedulerConfiguration
                        .getJavaWeightTable().entrySet()) {
                    if (weightForChooingOcr < entry.getKey()) {
                        ocrServer = entry.getValue();
                        break;
                    }
                }
            }
        } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (choosingCnn) {
                if (schedulerConfiguration.getCurrentPollingCnnServerIndex() == -1) {
                    return ocrServer;
                }
                do {
                    int index = (int) (schedulerConfiguration
                            .getAndIncrementCurrentPollingCnnServerIndex() % schedulerConfiguration
                            .getCnnServersPollingListSize());
                    ocrServer = schedulerConfiguration
                            .getCnnServersPollingList().get(index);
                } while (ocrServer.getOcrServerStatistics()
                        .getCurrentRequestNum() >= schedulerConfiguration
                        .getSystemDataConfiguration().getMaxCnnProcessNum());
            } else {
                if (schedulerConfiguration.getCurrentPollingJavaServerIndex() == -1) {
                    return ocrServer;
                }
                int index = (int) (schedulerConfiguration
                        .getAndIncrementCurrentPollingJavaServerIndex() % schedulerConfiguration
                        .getJavaServersPollingListSize());
                ocrServer = schedulerConfiguration.getJavaServersPollingList()
                        .get(index);
            }
        }
        return ocrServer;
    }

    /**
     * @return search server
     */
    private SearchServer chooseSearchServer() {
        SearchServer searchServer = null;
        if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getSearchTotalWeight() == 0) {
                return searchServer;
            }
            int weightForChooingSearch = r.nextInt(schedulerConfiguration
                    .getSearchTotalWeight());
            for (Entry<Integer, SearchServer> entry : schedulerConfiguration
                    .getSearchWeightTable().entrySet()) {
                if (weightForChooingSearch < entry.getKey()) {
                    searchServer = entry.getValue();
                    break;
                }
            }
        } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getCurrentPollingSearchServerIndex() == -1) {
                return searchServer;
            }
            do {
                int index = (int) (schedulerConfiguration
                        .getAndIncrementCurrentPollingSearchServerIndex() % schedulerConfiguration
                        .getSearchServersPollingListSize());
                searchServer = schedulerConfiguration
                        .getSearchServersPollingList().get(index);
            } while (searchServer.getSearchServerStatistics()
                    .getCurrentRequestNum() >= schedulerConfiguration
                    .getSystemDataConfiguration().getMaxSearchProcessNum());
        }
        return searchServer;
    }

    /**
     * @return search homework server
     */
    private SearchServer chooseSearchHomeworkServer() {
        SearchServer searchHomeworkServer = null;
        if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getSearchHomeworkTotalWeight() == 0) {
                return searchHomeworkServer;
            }
            int weightForChooingSearchHomework = r
                    .nextInt(schedulerConfiguration
                            .getSearchHomeworkTotalWeight());
            for (Entry<Integer, SearchServer> entry : schedulerConfiguration
                    .getSearchHomeworkWeightTable().entrySet()) {
                if (weightForChooingSearchHomework < entry.getKey()) {
                    searchHomeworkServer = entry.getValue();
                    break;
                }
            }
        } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration
                    .getCurrentPollingSearchHomeworkServerIndex() == -1) {
                return searchHomeworkServer;
            }
            int index = (int) (schedulerConfiguration
                    .getAndIncrementCurrentPollingSearchHomeworkServerIndex() % schedulerConfiguration
                    .getSearchHomeworkServersPollingListSize());
            searchHomeworkServer = schedulerConfiguration
                    .getSearchHomeworkServersPollingList().get(index);
        }
        return searchHomeworkServer;
    }

    /**
     * @return search article server
     */
    private SearchServer chooseSearchArticleServer() {
        SearchServer searchArticleServer = null;
        if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getSearchArticleTotalWeight() == 0) {
                return searchArticleServer;
            }
            int weightForChooingSearchArticle = r
                    .nextInt(schedulerConfiguration
                            .getSearchArticleTotalWeight());
            for (Entry<Integer, SearchServer> entry : schedulerConfiguration
                    .getSearchArticleWeightTable().entrySet()) {
                if (weightForChooingSearchArticle < entry.getKey()) {
                    searchArticleServer = entry.getValue();
                    break;
                }
            }
        } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration
                    .getCurrentPollingSearchArticleServerIndex() == -1) {
                return searchArticleServer;
            }
            int index = (int) (schedulerConfiguration
                    .getAndIncrementCurrentPollingSearchArticleServerIndex() % schedulerConfiguration
                    .getSearchArticleServersPollingListSize());
            searchArticleServer = schedulerConfiguration
                    .getSearchArticleServersPollingList().get(index);
        }
        return searchArticleServer;
    }

    /**
     * @return search by id server
     */
    private SearchServer chooseSearchByIdServer() {
        SearchServer searchByIdServer = null;
        if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getSearchByIdTotalWeight() == 0) {
                return searchByIdServer;
            }
            int weightForChooingSearchById = r.nextInt(schedulerConfiguration
                    .getSearchByIdTotalWeight());
            for (Entry<Integer, SearchServer> entry : schedulerConfiguration
                    .getSearchByIdWeightTable().entrySet()) {
                if (weightForChooingSearchById < entry.getKey()) {
                    searchByIdServer = entry.getValue();
                    break;
                }
            }
        } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getCurrentPollingSearchByIdServerIndex() == -1) {
                return searchByIdServer;
            }
            int index = (int) (schedulerConfiguration
                    .getAndIncrementCurrentPollingSearchByIdServerIndex() % schedulerConfiguration
                    .getSearchByIdServersPollingListSize());
            searchByIdServer = schedulerConfiguration
                    .getSearchByIdServersPollingList().get(index);
        }
        return searchByIdServer;
    }

    /**
     * @return em server
     */
    private EmServer chooseEmServer() {
        EmServer emServer = null;
        if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getEmTotalWeight() == 0) {
                return emServer;
            }
            int weightForChooingEm = r.nextInt(schedulerConfiguration
                    .getEmTotalWeight());
            for (Entry<Integer, EmServer> entry : schedulerConfiguration
                    .getEmWeightTable().entrySet()) {
                if (weightForChooingEm < entry.getKey()) {
                    emServer = entry.getValue();
                    break;
                }
            }
        } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getCurrentPollingEmServerIndex() == -1) {
                return emServer;
            }
            int index = (int) (schedulerConfiguration
                    .getAndIncrementCurrentPollingEmServerIndex() % schedulerConfiguration
                    .getEmServersPollingListSize());
            emServer = schedulerConfiguration.getEmServersPollingList().get(
                    index);
        }
        return emServer;
    }

    /**
     * @return ie server
     */
    private IeServer chooseIeServer() {
        IeServer ieServer = null;
        if (SchedulerStrategy.RANDOM.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getIeTotalWeight() == 0) {
                return ieServer;
            }
            int weightForChooingIe = r.nextInt(schedulerConfiguration
                    .getIeTotalWeight());
            for (Entry<Integer, IeServer> entry : schedulerConfiguration
                    .getIeWeightTable().entrySet()) {
                if (weightForChooingIe < entry.getKey()) {
                    ieServer = entry.getValue();
                    break;
                }
            }
        } else if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getCurrentPollingIeServerIndex() == -1) {
                return ieServer;
            }
            int index = (int) (schedulerConfiguration
                    .getAndIncrementCurrentPollingIeServerIndex() % schedulerConfiguration
                    .getIeServersPollingListSize());
            ieServer = schedulerConfiguration.getIeServersPollingList().get(
                    index);
        }
        return ieServer;
    }

    /**
     * @return sdk search server
     */
    private SearchServer chooseSdkSearchServer() {
        SearchServer sdkSearchServer = null;
        if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration.getCurrentPollingSdkSearchServerIndex() == -1) {
                return sdkSearchServer;
            }
            int index = (int) (schedulerConfiguration
                    .getAndIncrementCurrentPollingSdkSearchServerIndex() % schedulerConfiguration
                    .getSdkSearchServersPollingListSize());
            sdkSearchServer = schedulerConfiguration
                    .getSdkSearchServersPollingList().get(index);
        }
        return sdkSearchServer;
    }

    /**
     * @return handwrite ocr server
     */
    private OcrServer chooseHandwriteOcrServer() {
        OcrServer handwriteServer = null;
        if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration
                    .getCurrentPollingHandwriteOcrServerIndex() == -1) {
                return handwriteServer;
            }
            int index = (int) (schedulerConfiguration
                    .getAndIncrementCurrentPollingHandwriteOcrServerIndex() % schedulerConfiguration
                    .getHandwriteOcrServersPollingListSize());
            handwriteServer = schedulerConfiguration
                    .getHandwriteOcrServersPollingList().get(index);
        }
        return handwriteServer;
    }

    /**
     * @return search matrix server
     */
    private SearchServer chooseSearchMatrixServer() {
        SearchServer searchMatrixServer = null;
        if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())) {
            if (schedulerConfiguration
                    .getCurrentPollingSearchMatrixServerIndex() == -1) {
                return searchMatrixServer;
            }
            int index = (int) (schedulerConfiguration
                    .getAndIncrementCurrentPollingSearchMatrixServerIndex() % schedulerConfiguration
                    .getSearchMatrixServersPollingListSize());
            searchMatrixServer = schedulerConfiguration
                    .getSearchMatrixServersPollingList().get(index);
        }
        return searchMatrixServer;
    }

    /**
     * @param fid
     * @param uid
     * @param searchResult
     * @return word search result
     */
    private SearchResult getWordSearchResult(String fid, String uid,
            String searchResult) {
        SearchResult wordSearchResult = new SearchResult();

        JSONObject searchResultJson = null;
        JSONArray questionsJsonArray = new JSONArray();
        try {
            // 把json字符串转换成json对象
            searchResultJson = JSONObject.fromObject(searchResult);
            questionsJsonArray = searchResultJson
                    .getJSONArray(SEARCH_RESULT_QUESTIONS);

            // set search result
            List<com.xueba100.mining.common.SearchResult> searchResultList = new ArrayList<com.xueba100.mining.common.SearchResult>();
            for (int i = 0; i < questionsJsonArray.size(); ++i) {
                searchResultList
                        .add(new com.xueba100.mining.common.SearchResult(
                                Integer.parseInt(questionsJsonArray
                                        .getJSONObject(i).getString(
                                                SEARCH_RESULT_ID)),
                                questionsJsonArray.getJSONObject(i).getDouble(
                                        SEARCH_RESULT_SIMILARITY)));
            }
            wordSearchResult.setSearchResultList(searchResultList);
        } catch (JSONException e) {
            if (schedulerConfiguration.isSaveExceptionLogSwitch()) {
                logger.error("{} {} WSJE", uid, fid);
            }
        }

        return wordSearchResult;
    }

    /**
     * 获取本地IP列表（针对多网卡情况）
     *
     * @return
     */
    private static List<String> getLocalIPList() {
        List<String> ipList = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null
                            && inetAddress instanceof Inet4Address) {
                        ip = inetAddress.getHostAddress();
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipList;
    }
}

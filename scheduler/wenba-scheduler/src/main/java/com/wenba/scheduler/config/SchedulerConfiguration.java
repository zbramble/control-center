package com.wenba.scheduler.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.hadoop.conf.Configuration;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;

import redis.clients.jedis.JedisPool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wenba.scheduler.AbstractServer;
import com.wenba.scheduler.AbstractServer.ServerState;
import com.wenba.scheduler.em.EmServer;
import com.wenba.scheduler.email.EmailParam;
import com.wenba.scheduler.email.EmailUtil;
import com.wenba.scheduler.jzh.IeServer;
import com.wenba.scheduler.nlp.NlpServer;
import com.wenba.scheduler.ocr.OcrServer;
import com.wenba.scheduler.search.SearchServer;
import com.wenba.scheduler.statistics.BIServer;
import com.xueba100.mining.common.HbClient;

/**
 * @author zhangbo
 *
 */
public class SchedulerConfiguration {

    // constants
    private static final int MARKS_100 = 100;

    // 成员变量
    private static Logger logger = LogManager.getLogger("exception");
    // private static Logger debugLogger = LogManager.getLogger("debugInfo");
    private static Logger serverMonitorLogger = LogManager
            .getLogger("serverMonitor");

    /**
     * config file names
     */
    private List<String> configFileNames;

    /**
     * local ip
     */
    private String ip;

    /**
     * host name
     */
    private String name;

    /**
     * local ip list
     */
    private List<String> ipList;

    /**
     * jedis pool
     */
    private JedisPool jedisPool;

    /**
     * local jedis pool
     */
    private JedisPool localJedisPool;

    /**
     * email utility
     */
    private EmailUtil emailUtil;

    /**
     * system data configuration
     */
    private SystemDataConfiguration systemDataConfiguration;

    /**
     * timeout configuration
     */
    private TimeoutConfiguration timeoutConfiguration;

    /**
     * debug switch configuration
     */
    private DebugSwitchConfiguration debugSwitchConfiguration;

    /**
     * ugc config configuration
     */
    private UgcConfigConfiguration ugcConfigConfiguration;

    /**
     * http client
     */
    private CloseableHttpClient httpClient;

    /**
     * hbase on switch
     */
    private boolean hbaseOnSwitch;

    /**
     * nlp on switch
     */
    private boolean nlpOnSwitch;

    /**
     * bi on switch
     */
    private boolean biOnSwitch;

    /**
     * save server monitor log switch
     */
    private boolean saveServerMonitorLogSwitch;

    /**
     * save exception log switch
     */
    private boolean saveExceptionLogSwitch;

    /**
     * white list on switch
     */
    private boolean whiteListOnSwitch;

    /**
     * server down on switch
     */
    private boolean serverDownOnSwitch;

    /**
     * save search article log switch
     */
    private boolean saveSearchArticleLogSwitch;

    /**
     * save timeout log switch
     */
    private boolean saveTimeoutLogSwitch;

    /**
     * mail on switch
     */
    private boolean mailOnSwitch;

    /**
     * exec ocr only switch
     */
    private boolean execOcrOnlySwitch;

    /**
     * query bi mode switch
     */
    private String queryBiModeSwitch;

    /**
     * save query result to redis on switch
     */
    private boolean queryResultToRedisOnSwitch;

    /**
     * force hbase on switch
     */
    private boolean forceHbaseOnSwitch;

    /**
     * backup hbase on switch
     */
    private boolean bkHbaseOnSwitch;

    /**
     * ugc handwrite ocr on switch
     */
    private boolean ugcHandwriteOcrOnSwitch;

    /**
     * permitted request servers
     */
    private List<ConfigServer> configServers;

    /**
     * permitted request servers(state:down)
     */
    private List<ConfigServer> configUnusedServers;

    /**
     * cnn ocr servers
     */
    private List<OcrServer> cnnServers;

    /**
     * cnn ocr servers(state:down)
     */
    private List<OcrServer> cnnUnusedServers;

    /**
     * java ocr servers
     */
    private List<OcrServer> javaServers;

    /**
     * java ocr servers(state:down)
     */
    private List<OcrServer> javaUnusedServers;

    /**
     * search servers
     */
    private List<SearchServer> searchServers;

    /**
     * search servers(state:down)
     */
    private List<SearchServer> searchUnusedServers;

    /**
     * search homework servers
     */
    private List<SearchServer> searchHomeworkServers;

    /**
     * search homework servers(state:down)
     */
    private List<SearchServer> searchHomeworkUnusedServers;

    /**
     * search article servers
     */
    private List<SearchServer> searchArticleServers;

    /**
     * search article servers(state:down)
     */
    private List<SearchServer> searchArticleUnusedServers;

    /**
     * search by id servers
     */
    private List<SearchServer> searchByIdServers;

    /**
     * search by id servers(state:down)
     */
    private List<SearchServer> searchByIdUnusedServers;

    /**
     * nlp servers
     */
    private List<NlpServer> nlpServers;

    /**
     * nlp servers(state:down)
     */
    private List<NlpServer> nlpUnusedServers;

    /**
     * BI servers
     */
    private List<BIServer> biServers;

    /**
     * BI servers(state:down)
     */
    private List<BIServer> biUnusedServers;

    /**
     * em servers
     */
    private List<EmServer> emServers;

    /**
     * em servers(state:down)
     */
    private List<EmServer> emUnusedServers;

    /**
     * ie servers
     */
    private List<IeServer> ieServers;

    /**
     * ie servers(state:down)
     */
    private List<IeServer> ieUnusedServers;

    /**
     * sdk search servers
     */
    private List<SearchServer> sdkSearchServers;

    /**
     * handwrite ocr servers
     */
    private List<OcrServer> handwriteOcrServers;

    /**
     * search matrix servers
     */
    private List<SearchServer> searchMatrixServers;

    /**
     * cnn ocr server weight table
     */
    private Map<Integer, OcrServer> cnnWeightTable;

    /**
     * cnn ocr server total weight
     */
    private int cnnTotalWeight;

    /**
     * cnn ocr server polling list
     */
    private List<OcrServer> cnnServersPollingList;

    /**
     * current polling cnn ocr server index
     */
    private AtomicLong currentPollingCnnServerIndex;

    /**
     * cnn ocr server polling list size
     */
    private int cnnServersPollingListSize;

    /**
     * valid cnn servers size
     */
    private int validCnnServersSize;

    /**
     * java ocr server weight table
     */
    private Map<Integer, OcrServer> javaWeightTable;

    /**
     * java ocr server total weight
     */
    private int javaTotalWeight;

    /**
     * java ocr server polling list
     */
    private List<OcrServer> javaServersPollingList;

    /**
     * current polling java ocr server index
     */
    private AtomicLong currentPollingJavaServerIndex;

    /**
     * java ocr server polling list size
     */
    private int javaServersPollingListSize;

    /**
     * search server weight table
     */
    private Map<Integer, SearchServer> searchWeightTable;

    /**
     * search server total weight
     */
    private int searchTotalWeight;

    /**
     * search server polling list
     */
    private List<SearchServer> searchServersPollingList;

    /**
     * current polling search server index
     */
    private AtomicLong currentPollingSearchServerIndex;

    /**
     * search server polling list size
     */
    private int searchServersPollingListSize;

    /**
     * valid search servers size
     */
    private int validSearchServersSize;

    /**
     * search homework server weight table
     */
    private Map<Integer, SearchServer> searchHomeworkWeightTable;

    /**
     * search homework server total weight
     */
    private int searchHomeworkTotalWeight;

    /**
     * search homework server polling list
     */
    private List<SearchServer> searchHomeworkServersPollingList;

    /**
     * current polling search homework server index
     */
    private AtomicLong currentPollingSearchHomeworkServerIndex;

    /**
     * search homework server polling list size
     */
    private int searchHomeworkServersPollingListSize;

    /**
     * search article server weight table
     */
    private Map<Integer, SearchServer> searchArticleWeightTable;

    /**
     * search article server total weight
     */
    private int searchArticleTotalWeight;

    /**
     * search article server polling list
     */
    private List<SearchServer> searchArticleServersPollingList;

    /**
     * current polling search article server index
     */
    private AtomicLong currentPollingSearchArticleServerIndex;

    /**
     * search article server polling list size
     */
    private int searchArticleServersPollingListSize;

    /**
     * search by id server weight table
     */
    private Map<Integer, SearchServer> searchByIdWeightTable;

    /**
     * search by id server total weight
     */
    private int searchByIdTotalWeight;

    /**
     * search by id server polling list
     */
    private List<SearchServer> searchByIdServersPollingList;

    /**
     * current polling search by id server index
     */
    private AtomicLong currentPollingSearchByIdServerIndex;

    /**
     * search by id server polling list size
     */
    private int searchByIdServersPollingListSize;

    /**
     * nlp server weight table
     */
    private Map<Integer, NlpServer> nlpWeightTable;

    /**
     * nlp server total weight
     */
    private int nlpTotalWeight;

    /**
     * nlp server polling list
     */
    private List<NlpServer> nlpServersPollingList;

    /**
     * current polling nlp server index
     */
    private AtomicLong currentPollingNlpServerIndex;

    /**
     * nlp server polling list size
     */
    private int nlpServersPollingListSize;

    /**
     * em server weight table
     */
    private Map<Integer, EmServer> emWeightTable;

    /**
     * em server total weight
     */
    private int emTotalWeight;

    /**
     * em server polling list
     */
    private List<EmServer> emServersPollingList;

    /**
     * current polling em server index
     */
    private AtomicLong currentPollingEmServerIndex;

    /**
     * em server polling list size
     */
    private int emServersPollingListSize;

    /**
     * ie server weight table
     */
    private Map<Integer, IeServer> ieWeightTable;

    /**
     * ie server total weight
     */
    private int ieTotalWeight;

    /**
     * ie server polling list
     */
    private List<IeServer> ieServersPollingList;

    /**
     * current polling ie server index
     */
    private AtomicLong currentPollingIeServerIndex;

    /**
     * ie server polling list size
     */
    private int ieServersPollingListSize;

    /**
     * sdk search server polling list
     */
    private List<SearchServer> sdkSearchServersPollingList;

    /**
     * current polling sdk search server index
     */
    private AtomicLong currentPollingSdkSearchServerIndex;

    /**
     * search sdk server polling list size
     */
    private int sdkSearchServersPollingListSize;

    /**
     * handwrite ocr server polling list
     */
    private List<OcrServer> handwriteOcrServersPollingList;

    /**
     * current polling handwrite ocr server index
     */
    private AtomicLong currentPollingHandwriteOcrServerIndex;

    /**
     * handwrite ocr server polling list size
     */
    private int handwriteOcrServersPollingListSize;

    /**
     * search matrix server polling list
     */
    private List<SearchServer> searchMatrixServersPollingList;

    /**
     * current polling search matrix server index
     */
    private AtomicLong currentPollingSearchMatrixServerIndex;

    /**
     * search matrix server polling list size
     */
    private int searchMatrixServersPollingListSize;

    /**
     * scheduler start time
     */
    private long schedulerStartTime;

    /**
     * time zone(0-23)
     */
    private int timeZone;

    /**
     * hbase configuration
     */
    private Configuration hbaseConfig;

    /**
     * hbase client
     */
    private HbClient hbClient;

    /**
     * hbase configuration backup
     */
    private Configuration hbaseConfigBk;

    /**
     * hbase client backup
     */
    private HbClient hbClientBk;

    /**
     * jackson mapper
     */
    private ObjectMapper mapper;

    public SchedulerConfiguration() {
        currentPollingCnnServerIndex = new AtomicLong(0);
        currentPollingJavaServerIndex = new AtomicLong(0);
        currentPollingSearchServerIndex = new AtomicLong(0);
        currentPollingSearchHomeworkServerIndex = new AtomicLong(0);
        currentPollingSearchArticleServerIndex = new AtomicLong(0);
        currentPollingSearchByIdServerIndex = new AtomicLong(0);
        currentPollingNlpServerIndex = new AtomicLong(0);
        currentPollingEmServerIndex = new AtomicLong(0);
        currentPollingIeServerIndex = new AtomicLong(0);
        currentPollingSdkSearchServerIndex = new AtomicLong(0);
        currentPollingHandwriteOcrServerIndex = new AtomicLong(0);
        currentPollingSearchMatrixServerIndex = new AtomicLong(0);
    }

    /**
     * @param server
     * @param isExecSuccess
     * @param isForeverOnLine
     */
    @Async
    public void calcMarks(AbstractServer server, boolean isExecSuccess,
            boolean isForeverOnLine) {
        if (serverDownOnSwitch) {
            if (isExecSuccess) {
                if (server.getMarks() >= MARKS_100) {
                    return;
                }
                server.incrementAndGetMarks();
            } else {
                synchronized (server) {
                    if (server.getMarks() <= 0) {
                        return;
                    }

                    if (ServerState.UP.equals(server.getState())
                            && server.decrementAndGetMarks() < systemDataConfiguration
                                    .getMarksThreshold()) {
                        if (isForeverOnLine) {
                            return;
                        }
                        if (saveServerMonitorLogSwitch) {
                            serverMonitorLogger
                                    .error("{} marks is lower than threshold {}!DOWN!",
                                            server.getId(),
                                            systemDataConfiguration
                                                    .getMarksThreshold());
                        }

                        server.setState(ServerState.DOWN);
                        server.setAbsoluteWeight(0);
                        switch (server.getServerType()) {
                        case CNN:
                            Map<Integer, OcrServer> tempCnnWeightTable = new TreeMap<Integer, OcrServer>();
                            int currentCnnWeight = 0;
                            for (OcrServer cnnServer : cnnServers) {
                                if (ServerState.UP.equals(cnnServer.getState())
                                        && cnnServer.getWeight() > 0) {
                                    currentCnnWeight += cnnServer.getWeight();
                                    tempCnnWeightTable.put(currentCnnWeight,
                                            cnnServer);
                                    cnnServer
                                            .setAbsoluteWeight(currentCnnWeight);
                                }
                            }
                            cnnTotalWeight = currentCnnWeight;
                            cnnWeightTable = tempCnnWeightTable;
                            writeCnnServersConfigFile();
                            if (mailOnSwitch) {
                                EmailParam emailParam = new EmailParam();
                                emailParam
                                        .setSchedulerId(systemDataConfiguration
                                                .getSchedulerId());
                                emailParam.setReceiverAddress(emailUtil
                                        .getCnnServersMailRecipients());
                                emailParam.setSub("server down");
                                emailParam.setMsg(server.getId()
                                        + " marks is lower than threshold "
                                        + systemDataConfiguration
                                                .getMarksThreshold()
                                        + "!DOWN!\n");
                                emailUtil.sendEmail(emailParam);
                            }
                            break;
                        case JAVA:
                            Map<Integer, OcrServer> tempJavaWeightTable = new TreeMap<Integer, OcrServer>();
                            int currentJavaWeight = 0;
                            for (OcrServer javaServer : javaServers) {
                                if (ServerState.UP
                                        .equals(javaServer.getState())
                                        && javaServer.getWeight() > 0) {
                                    currentJavaWeight += javaServer.getWeight();
                                    tempJavaWeightTable.put(currentJavaWeight,
                                            javaServer);
                                    javaServer
                                            .setAbsoluteWeight(currentJavaWeight);
                                }
                            }
                            javaTotalWeight = currentJavaWeight;
                            javaWeightTable = tempJavaWeightTable;
                            writeJavaServersConfigFile();
                            break;
                        case NLP:
                            Map<Integer, NlpServer> tempNlpWeightTable = new TreeMap<Integer, NlpServer>();
                            int currentNlpWeight = 0;
                            for (NlpServer nlpServer : nlpServers) {
                                if (ServerState.UP.equals(nlpServer.getState())
                                        && nlpServer.getWeight() > 0) {
                                    currentNlpWeight += nlpServer.getWeight();
                                    tempNlpWeightTable.put(currentNlpWeight,
                                            nlpServer);
                                    nlpServer
                                            .setAbsoluteWeight(currentNlpWeight);
                                }
                            }
                            nlpTotalWeight = currentNlpWeight;
                            nlpWeightTable = tempNlpWeightTable;
                            writeNlpServersConfigFile();
                            if (mailOnSwitch) {
                                EmailParam emailParam = new EmailParam();
                                emailParam
                                        .setSchedulerId(systemDataConfiguration
                                                .getSchedulerId());
                                emailParam.setReceiverAddress(emailUtil
                                        .getNlpServersMailRecipients());
                                emailParam.setSub("server down");
                                emailParam.setMsg(server.getId()
                                        + " marks is lower than threshold "
                                        + systemDataConfiguration
                                                .getMarksThreshold()
                                        + "!DOWN!\n");
                                emailUtil.sendEmail(emailParam);
                            }
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        }
    }

    public synchronized void writeCnnServersConfigFile() {
        // 写入cnn_servers文件
        try {
            File config = new File("configFile/cnn_servers");
            FileWriter fw = new FileWriter(config);
            BufferedWriter bw = new BufferedWriter(fw);
            if (cnnServers.size() > 0) {
                bw.write("{");
                bw.newLine();
                bw.write("\"cnnServers\":[");
                int cnnServersNum = 0;
                OcrServer cnnServer;
                for (; cnnServersNum < cnnServers.size() - 1; ++cnnServersNum) {
                    cnnServer = cnnServers.get(cnnServersNum);
                    bw.newLine();
                    bw.write("{\"id\":\"" + cnnServer.getId()
                            + "\",\"state\":\""
                            + cnnServer.getState().toString() + "\",\"ip\":\""
                            + cnnServer.getIp() + "\",\"port\":\""
                            + cnnServer.getPort() + "\",\"url\":\""
                            + cnnServer.getUrl() + "\",\"weight\":"
                            + cnnServer.getWeight() + "},");
                }
                cnnServer = cnnServers.get(cnnServersNum);
                bw.newLine();
                bw.write("{\"id\":\"" + cnnServer.getId() + "\",\"state\":\""
                        + cnnServer.getState().toString() + "\",\"ip\":\""
                        + cnnServer.getIp() + "\",\"port\":\""
                        + cnnServer.getPort() + "\",\"url\":\""
                        + cnnServer.getUrl() + "\",\"weight\":"
                        + cnnServer.getWeight() + "}]");
                bw.write("}");
                bw.close();
            }
        } catch (IOException e) {
            logger.error("write to cnn_servers IOE!");
        }
    }

    public synchronized void writeJavaServersConfigFile() {
        // 写入java_servers文件
        try {
            File config = new File("configFile/java_servers");
            FileWriter fw = new FileWriter(config);
            BufferedWriter bw = new BufferedWriter(fw);
            if (javaServers.size() > 0) {
                bw.write("{");
                bw.newLine();
                bw.write("\"javaServers\":[");
                int javaServersNum = 0;
                OcrServer javaServer;
                for (; javaServersNum < javaServers.size() - 1; ++javaServersNum) {
                    javaServer = javaServers.get(javaServersNum);
                    bw.newLine();
                    bw.write("{\"id\":\"" + javaServer.getId()
                            + "\",\"state\":\""
                            + javaServer.getState().toString() + "\",\"ip\":\""
                            + javaServer.getIp() + "\",\"port\":\""
                            + javaServer.getPort() + "\",\"url\":\""
                            + javaServer.getUrl() + "\",\"weight\":"
                            + javaServer.getWeight() + "},");
                }
                javaServer = javaServers.get(javaServersNum);
                bw.newLine();
                bw.write("{\"id\":\"" + javaServer.getId() + "\",\"state\":\""
                        + javaServer.getState().toString() + "\",\"ip\":\""
                        + javaServer.getIp() + "\",\"port\":\""
                        + javaServer.getPort() + "\",\"url\":\""
                        + javaServer.getUrl() + "\",\"weight\":"
                        + javaServer.getWeight() + "}]");
                bw.write("}");
                bw.close();
            }
        } catch (IOException e) {
            logger.error("write to java_servers IOE!");
        }
    }

    public synchronized void writeNlpServersConfigFile() {
        // 写入nlp_servers文件
        try {
            File config = new File("configFile/nlp_servers");
            FileWriter fw = new FileWriter(config);
            BufferedWriter bw = new BufferedWriter(fw);
            if (nlpServers.size() > 0) {
                bw.write("{");
                bw.newLine();
                bw.write("\"nlpServers\":[");
                int nlpServersNum = 0;
                NlpServer nlpServer;
                for (; nlpServersNum < nlpServers.size() - 1; ++nlpServersNum) {
                    nlpServer = nlpServers.get(nlpServersNum);
                    bw.newLine();
                    bw.write("{\"id\":\"" + nlpServer.getId()
                            + "\",\"state\":\""
                            + nlpServer.getState().toString() + "\",\"ip\":\""
                            + nlpServer.getIp() + "\",\"port\":\""
                            + nlpServer.getPort() + "\",\"url\":\""
                            + nlpServer.getUrl() + "\",\"weight\":"
                            + nlpServer.getWeight() + "},");
                }
                nlpServer = nlpServers.get(nlpServersNum);
                bw.newLine();
                bw.write("{\"id\":\"" + nlpServer.getId() + "\",\"state\":\""
                        + nlpServer.getState().toString() + "\",\"ip\":\""
                        + nlpServer.getIp() + "\",\"port\":\""
                        + nlpServer.getPort() + "\",\"url\":\""
                        + nlpServer.getUrl() + "\",\"weight\":"
                        + nlpServer.getWeight() + "}]");
                bw.write("}");
                bw.close();
            }
        } catch (IOException e) {
            logger.error("write to nlp_servers IOE!");
        }
    }

    public long getSchedulerStartTime() {
        return schedulerStartTime;
    }

    public void setSchedulerStartTime(long schedulerStartTime) {
        this.schedulerStartTime = schedulerStartTime;
    }

    public List<ConfigServer> getConfigServers() {
        return configServers;
    }

    public void setConfigServers(List<ConfigServer> configServers) {
        this.configServers = configServers;
    }

    public List<ConfigServer> getConfigUnusedServers() {
        return configUnusedServers;
    }

    public void setConfigUnusedServers(List<ConfigServer> configUnusedServers) {
        this.configUnusedServers = configUnusedServers;
    }

    public List<OcrServer> getJavaServers() {
        return javaServers;
    }

    public void setJavaServers(List<OcrServer> javaServers) {
        this.javaServers = javaServers;
    }

    public List<OcrServer> getCnnServers() {
        return cnnServers;
    }

    public void setCnnServers(List<OcrServer> cnnServers) {
        this.cnnServers = cnnServers;
    }

    public List<SearchServer> getSearchServers() {
        return searchServers;
    }

    public void setSearchServers(List<SearchServer> searchServers) {
        this.searchServers = searchServers;
    }

    public List<OcrServer> getJavaUnusedServers() {
        return javaUnusedServers;
    }

    public void setJavaUnusedServers(List<OcrServer> javaUnusedServers) {
        this.javaUnusedServers = javaUnusedServers;
    }

    public List<OcrServer> getCnnUnusedServers() {
        return cnnUnusedServers;
    }

    public void setCnnUnusedServers(List<OcrServer> cnnUnusedServers) {
        this.cnnUnusedServers = cnnUnusedServers;
    }

    public List<SearchServer> getSearchUnusedServers() {
        return searchUnusedServers;
    }

    public void setSearchUnusedServers(List<SearchServer> searchUnusedServers) {
        this.searchUnusedServers = searchUnusedServers;
    }

    public List<SearchServer> getSearchHomeworkServers() {
        return searchHomeworkServers;
    }

    public void setSearchHomeworkServers(
            List<SearchServer> searchHomeworkServers) {
        this.searchHomeworkServers = searchHomeworkServers;
    }

    public List<SearchServer> getSearchHomeworkUnusedServers() {
        return searchHomeworkUnusedServers;
    }

    public void setSearchHomeworkUnusedServers(
            List<SearchServer> searchHomeworkUnusedServers) {
        this.searchHomeworkUnusedServers = searchHomeworkUnusedServers;
    }

    public List<SearchServer> getSearchArticleServers() {
        return searchArticleServers;
    }

    public void setSearchArticleServers(List<SearchServer> searchArticleServers) {
        this.searchArticleServers = searchArticleServers;
    }

    public List<SearchServer> getSearchArticleUnusedServers() {
        return searchArticleUnusedServers;
    }

    public void setSearchArticleUnusedServers(
            List<SearchServer> searchArticleUnusedServers) {
        this.searchArticleUnusedServers = searchArticleUnusedServers;
    }

    public List<BIServer> getBiServers() {
        return biServers;
    }

    public void setBiServers(List<BIServer> biServers) {
        this.biServers = biServers;
    }

    public List<BIServer> getBiUnusedServers() {
        return biUnusedServers;
    }

    public void setBiUnusedServers(List<BIServer> biUnusedServers) {
        this.biUnusedServers = biUnusedServers;
    }

    public List<NlpServer> getNlpServers() {
        return nlpServers;
    }

    public void setNlpServers(List<NlpServer> nlpServers) {
        this.nlpServers = nlpServers;
    }

    public List<NlpServer> getNlpUnusedServers() {
        return nlpUnusedServers;
    }

    public void setNlpUnusedServers(List<NlpServer> nlpUnusedServers) {
        this.nlpUnusedServers = nlpUnusedServers;
    }

    public Map<Integer, OcrServer> getCnnWeightTable() {
        return cnnWeightTable;
    }

    public void setCnnWeightTable(Map<Integer, OcrServer> cnnWeightTable) {
        this.cnnWeightTable = cnnWeightTable;
    }

    public int getCnnTotalWeight() {
        return cnnTotalWeight;
    }

    public void setCnnTotalWeight(int cnnTotalWeight) {
        this.cnnTotalWeight = cnnTotalWeight;
    }

    public Map<Integer, OcrServer> getJavaWeightTable() {
        return javaWeightTable;
    }

    public void setJavaWeightTable(Map<Integer, OcrServer> javaWeightTable) {
        this.javaWeightTable = javaWeightTable;
    }

    public int getJavaTotalWeight() {
        return javaTotalWeight;
    }

    public void setJavaTotalWeight(int javaTotalWeight) {
        this.javaTotalWeight = javaTotalWeight;
    }

    public Map<Integer, SearchServer> getSearchWeightTable() {
        return searchWeightTable;
    }

    public void setSearchWeightTable(
            Map<Integer, SearchServer> searchWeightTable) {
        this.searchWeightTable = searchWeightTable;
    }

    public int getSearchTotalWeight() {
        return searchTotalWeight;
    }

    public void setSearchTotalWeight(int searchTotalWeight) {
        this.searchTotalWeight = searchTotalWeight;
    }

    public Map<Integer, SearchServer> getSearchHomeworkWeightTable() {
        return searchHomeworkWeightTable;
    }

    public void setSearchHomeworkWeightTable(
            Map<Integer, SearchServer> searchHomeworkWeightTable) {
        this.searchHomeworkWeightTable = searchHomeworkWeightTable;
    }

    public int getSearchHomeworkTotalWeight() {
        return searchHomeworkTotalWeight;
    }

    public void setSearchHomeworkTotalWeight(int searchHomeworkTotalWeight) {
        this.searchHomeworkTotalWeight = searchHomeworkTotalWeight;
    }

    public Map<Integer, SearchServer> getSearchArticleWeightTable() {
        return searchArticleWeightTable;
    }

    public void setSearchArticleWeightTable(
            Map<Integer, SearchServer> searchArticleWeightTable) {
        this.searchArticleWeightTable = searchArticleWeightTable;
    }

    public int getSearchArticleTotalWeight() {
        return searchArticleTotalWeight;
    }

    public void setSearchArticleTotalWeight(int searchArticleTotalWeight) {
        this.searchArticleTotalWeight = searchArticleTotalWeight;
    }

    public Map<Integer, NlpServer> getNlpWeightTable() {
        return nlpWeightTable;
    }

    public void setNlpWeightTable(Map<Integer, NlpServer> nlpWeightTable) {
        this.nlpWeightTable = nlpWeightTable;
    }

    public int getNlpTotalWeight() {
        return nlpTotalWeight;
    }

    public void setNlpTotalWeight(int nlpTotalWeight) {
        this.nlpTotalWeight = nlpTotalWeight;
    }

    public int getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(int timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isHbaseOnSwitch() {
        return hbaseOnSwitch;
    }

    public void setHbaseOnSwitch(boolean hbaseOnSwitch) {
        this.hbaseOnSwitch = hbaseOnSwitch;
    }

    public boolean isSaveExceptionLogSwitch() {
        return saveExceptionLogSwitch;
    }

    public void setSaveExceptionLogSwitch(boolean saveExceptionLogSwitch) {
        this.saveExceptionLogSwitch = saveExceptionLogSwitch;
    }

    public boolean isSaveServerMonitorLogSwitch() {
        return saveServerMonitorLogSwitch;
    }

    public void setSaveServerMonitorLogSwitch(boolean saveServerMonitorLogSwitch) {
        this.saveServerMonitorLogSwitch = saveServerMonitorLogSwitch;
    }

    public boolean isNlpOnSwitch() {
        return nlpOnSwitch;
    }

    public void setNlpOnSwitch(boolean nlpOnSwitch) {
        this.nlpOnSwitch = nlpOnSwitch;
    }

    public boolean isBiOnSwitch() {
        return biOnSwitch;
    }

    public void setBiOnSwitch(boolean biOnSwitch) {
        this.biOnSwitch = biOnSwitch;
    }

    public boolean isWhiteListOnSwitch() {
        return whiteListOnSwitch;
    }

    public void setWhiteListOnSwitch(boolean whiteListOnSwitch) {
        this.whiteListOnSwitch = whiteListOnSwitch;
    }

    public boolean isServerDownOnSwitch() {
        return serverDownOnSwitch;
    }

    public void setServerDownOnSwitch(boolean serverDownOnSwitch) {
        this.serverDownOnSwitch = serverDownOnSwitch;
    }

    public boolean isSaveSearchArticleLogSwitch() {
        return saveSearchArticleLogSwitch;
    }

    public void setSaveSearchArticleLogSwitch(boolean saveSearchArticleLogSwitch) {
        this.saveSearchArticleLogSwitch = saveSearchArticleLogSwitch;
    }

    public boolean isSaveTimeoutLogSwitch() {
        return saveTimeoutLogSwitch;
    }

    public void setSaveTimeoutLogSwitch(boolean saveTimeoutLogSwitch) {
        this.saveTimeoutLogSwitch = saveTimeoutLogSwitch;
    }

    public boolean isMailOnSwitch() {
        return mailOnSwitch;
    }

    public void setMailOnSwitch(boolean mailOnSwitch) {
        this.mailOnSwitch = mailOnSwitch;
    }

    public boolean isExecOcrOnlySwitch() {
        return execOcrOnlySwitch;
    }

    public void setExecOcrOnlySwitch(boolean execOcrOnlySwitch) {
        this.execOcrOnlySwitch = execOcrOnlySwitch;
    }

    public String getQueryBiModeSwitch() {
        return queryBiModeSwitch;
    }

    public void setQueryBiModeSwitch(String queryBiModeSwitch) {
        this.queryBiModeSwitch = queryBiModeSwitch;
    }

    public boolean isQueryResultToRedisOnSwitch() {
        return queryResultToRedisOnSwitch;
    }

    public void setQueryResultToRedisOnSwitch(boolean queryResultToRedisOnSwitch) {
        this.queryResultToRedisOnSwitch = queryResultToRedisOnSwitch;
    }

    public boolean isForceHbaseOnSwitch() {
        return forceHbaseOnSwitch;
    }

    public void setForceHbaseOnSwitch(boolean forceHbaseOnSwitch) {
        this.forceHbaseOnSwitch = forceHbaseOnSwitch;
    }

    public boolean isBkHbaseOnSwitch() {
        return bkHbaseOnSwitch;
    }

    public void setBkHbaseOnSwitch(boolean bkHbaseOnSwitch) {
        this.bkHbaseOnSwitch = bkHbaseOnSwitch;
    }

    public boolean isUgcHandwriteOcrOnSwitch() {
        return ugcHandwriteOcrOnSwitch;
    }

    public void setUgcHandwriteOcrOnSwitch(boolean ugcHandwriteOcrOnSwitch) {
        this.ugcHandwriteOcrOnSwitch = ugcHandwriteOcrOnSwitch;
    }

    public SystemDataConfiguration getSystemDataConfiguration() {
        return systemDataConfiguration;
    }

    public void setSystemDataConfiguration(
            SystemDataConfiguration systemDataConfiguration) {
        this.systemDataConfiguration = systemDataConfiguration;
    }

    public EmailUtil getEmailUtil() {
        return emailUtil;
    }

    public void setEmailUtil(EmailUtil emailUtil) {
        this.emailUtil = emailUtil;
    }

    public TimeoutConfiguration getTimeoutConfiguration() {
        return timeoutConfiguration;
    }

    public void setTimeoutConfiguration(
            TimeoutConfiguration timeoutConfiguration) {
        this.timeoutConfiguration = timeoutConfiguration;
    }

    public DebugSwitchConfiguration getDebugSwitchConfiguration() {
        return debugSwitchConfiguration;
    }

    public void setDebugSwitchConfiguration(
            DebugSwitchConfiguration debugSwitchConfiguration) {
        this.debugSwitchConfiguration = debugSwitchConfiguration;
    }

    public UgcConfigConfiguration getUgcConfigConfiguration() {
        return ugcConfigConfiguration;
    }

    public void setUgcConfigConfiguration(
            UgcConfigConfiguration ugcConfigConfiguration) {
        this.ugcConfigConfiguration = ugcConfigConfiguration;
    }

    public List<OcrServer> getCnnServersPollingList() {
        return cnnServersPollingList;
    }

    public void setCnnServersPollingList(List<OcrServer> cnnServersPollingList) {
        this.cnnServersPollingList = cnnServersPollingList;
    }

    public long getCurrentPollingCnnServerIndex() {
        return currentPollingCnnServerIndex.get();
    }

    public void setCurrentPollingCnnServerIndex(
            long currentPollingCnnServerIndex) {
        this.currentPollingCnnServerIndex.set(currentPollingCnnServerIndex);
    }

    public long incrementAndGetCurrentPollingCnnServerIndex() {
        return currentPollingCnnServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingCnnServerIndex() {
        return currentPollingCnnServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingCnnServerIndex() {
        return currentPollingCnnServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingCnnServerIndex() {
        return currentPollingCnnServerIndex.getAndDecrement();
    }

    public List<OcrServer> getJavaServersPollingList() {
        return javaServersPollingList;
    }

    public void setJavaServersPollingList(List<OcrServer> javaServersPollingList) {
        this.javaServersPollingList = javaServersPollingList;
    }

    public long getCurrentPollingJavaServerIndex() {
        return currentPollingJavaServerIndex.get();
    }

    public void setCurrentPollingJavaServerIndex(
            long currentPollingJavaServerIndex) {
        this.currentPollingJavaServerIndex.set(currentPollingJavaServerIndex);
    }

    public long incrementAndGetCurrentPollingJavaServerIndex() {
        return currentPollingJavaServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingJavaServerIndex() {
        return currentPollingJavaServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingJavaServerIndex() {
        return currentPollingJavaServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingJavaServerIndex() {
        return currentPollingJavaServerIndex.getAndDecrement();
    }

    public List<SearchServer> getSearchServersPollingList() {
        return searchServersPollingList;
    }

    public void setSearchServersPollingList(
            List<SearchServer> searchServersPollingList) {
        this.searchServersPollingList = searchServersPollingList;
    }

    public long getCurrentPollingSearchServerIndex() {
        return currentPollingSearchServerIndex.get();
    }

    public void setCurrentPollingSearchServerIndex(
            long currentPollingSearchServerIndex) {
        this.currentPollingSearchServerIndex
                .set(currentPollingSearchServerIndex);
    }

    public long incrementAndGetCurrentPollingSearchServerIndex() {
        return currentPollingSearchServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingSearchServerIndex() {
        return currentPollingSearchServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingSearchServerIndex() {
        return currentPollingSearchServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingSearchServerIndex() {
        return currentPollingSearchServerIndex.getAndDecrement();
    }

    public List<SearchServer> getSearchHomeworkServersPollingList() {
        return searchHomeworkServersPollingList;
    }

    public void setSearchHomeworkServersPollingList(
            List<SearchServer> searchHomeworkServersPollingList) {
        this.searchHomeworkServersPollingList = searchHomeworkServersPollingList;
    }

    public long getCurrentPollingSearchHomeworkServerIndex() {
        return currentPollingSearchHomeworkServerIndex.get();
    }

    public void setCurrentPollingSearchHomeworkServerIndex(
            long currentPollingSearchHomeworkServerIndex) {
        this.currentPollingSearchHomeworkServerIndex
                .set(currentPollingSearchHomeworkServerIndex);
    }

    public long incrementAndGetCurrentPollingSearchHomeworkServerIndex() {
        return currentPollingSearchHomeworkServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingSearchHomeworkServerIndex() {
        return currentPollingSearchHomeworkServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingSearchHomeworkServerIndex() {
        return currentPollingSearchHomeworkServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingSearchHomeworkServerIndex() {
        return currentPollingSearchHomeworkServerIndex.getAndDecrement();
    }

    public List<SearchServer> getSearchArticleServersPollingList() {
        return searchArticleServersPollingList;
    }

    public void setSearchArticleServersPollingList(
            List<SearchServer> searchArticleServersPollingList) {
        this.searchArticleServersPollingList = searchArticleServersPollingList;
    }

    public long getCurrentPollingSearchArticleServerIndex() {
        return currentPollingSearchArticleServerIndex.get();
    }

    public void setCurrentPollingSearchArticleServerIndex(
            long currentPollingSearchArticleServerIndex) {
        this.currentPollingSearchArticleServerIndex
                .set(currentPollingSearchArticleServerIndex);
    }

    public long incrementAndGetCurrentPollingSearchArticleServerIndex() {
        return currentPollingSearchArticleServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingSearchArticleServerIndex() {
        return currentPollingSearchArticleServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingSearchArticleServerIndex() {
        return currentPollingSearchArticleServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingSearchArticleServerIndex() {
        return currentPollingSearchArticleServerIndex.getAndDecrement();
    }

    public long getCurrentPollingSearchByIdServerIndex() {
        return currentPollingSearchByIdServerIndex.get();
    }

    public void setCurrentPollingSearchByIdServerIndex(
            long currentPollingSearchByIdServerIndex) {
        this.currentPollingSearchByIdServerIndex
                .set(currentPollingSearchByIdServerIndex);
    }

    public long incrementAndGetCurrentPollingSearchByIdServerIndex() {
        return currentPollingSearchByIdServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingSearchByIdServerIndex() {
        return currentPollingSearchByIdServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingSearchByIdServerIndex() {
        return currentPollingSearchByIdServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingSearchByIdServerIndex() {
        return currentPollingSearchByIdServerIndex.getAndDecrement();
    }

    public Map<Integer, SearchServer> getSearchByIdWeightTable() {
        return searchByIdWeightTable;
    }

    public void setSearchByIdWeightTable(
            Map<Integer, SearchServer> searchByIdWeightTable) {
        this.searchByIdWeightTable = searchByIdWeightTable;
    }

    public int getSearchByIdTotalWeight() {
        return searchByIdTotalWeight;
    }

    public void setSearchByIdTotalWeight(int searchByIdTotalWeight) {
        this.searchByIdTotalWeight = searchByIdTotalWeight;
    }

    public List<SearchServer> getSearchByIdServersPollingList() {
        return searchByIdServersPollingList;
    }

    public void setSearchByIdServersPollingList(
            List<SearchServer> searchByIdServersPollingList) {
        this.searchByIdServersPollingList = searchByIdServersPollingList;
    }

    public int getSearchByIdServersPollingListSize() {
        return searchByIdServersPollingListSize;
    }

    public void setSearchByIdServersPollingListSize(
            int searchByIdServersPollingListSize) {
        this.searchByIdServersPollingListSize = searchByIdServersPollingListSize;
    }

    public List<NlpServer> getNlpServersPollingList() {
        return nlpServersPollingList;
    }

    public void setNlpServersPollingList(List<NlpServer> nlpServersPollingList) {
        this.nlpServersPollingList = nlpServersPollingList;
    }

    public long getCurrentPollingNlpServerIndex() {
        return currentPollingNlpServerIndex.get();
    }

    public void setCurrentPollingNlpServerIndex(
            long currentPollingNlpServerIndex) {
        this.currentPollingNlpServerIndex.set(currentPollingNlpServerIndex);
    }

    public long incrementAndGetCurrentPollingNlpServerIndex() {
        return currentPollingNlpServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingNlpServerIndex() {
        return currentPollingNlpServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingNlpServerIndex() {
        return currentPollingNlpServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingNlpServerIndex() {
        return currentPollingNlpServerIndex.getAndDecrement();
    }

    public long getCnnServersPollingListSize() {
        return cnnServersPollingListSize;
    }

    public void setCnnServersPollingListSize(int cnnServersPollingListSize) {
        this.cnnServersPollingListSize = cnnServersPollingListSize;
    }

    public int getJavaServersPollingListSize() {
        return javaServersPollingListSize;
    }

    public void setJavaServersPollingListSize(int javaServersPollingListSize) {
        this.javaServersPollingListSize = javaServersPollingListSize;
    }

    public int getSearchServersPollingListSize() {
        return searchServersPollingListSize;
    }

    public void setSearchServersPollingListSize(int searchServersPollingListSize) {
        this.searchServersPollingListSize = searchServersPollingListSize;
    }

    public int getValidSearchServersSize() {
        return validSearchServersSize;
    }

    public void setValidSearchServersSize(int validSearchServersSize) {
        this.validSearchServersSize = validSearchServersSize;
    }

    public int getSearchHomeworkServersPollingListSize() {
        return searchHomeworkServersPollingListSize;
    }

    public void setSearchHomeworkServersPollingListSize(
            int searchHomeworkServersPollingListSize) {
        this.searchHomeworkServersPollingListSize = searchHomeworkServersPollingListSize;
    }

    public int getSearchArticleServersPollingListSize() {
        return searchArticleServersPollingListSize;
    }

    public void setSearchArticleServersPollingListSize(
            int searchArticleServersPollingListSize) {
        this.searchArticleServersPollingListSize = searchArticleServersPollingListSize;
    }

    public int getNlpServersPollingListSize() {
        return nlpServersPollingListSize;
    }

    public void setNlpServersPollingListSize(int nlpServersPollingListSize) {
        this.nlpServersPollingListSize = nlpServersPollingListSize;
    }

    public int getValidCnnServersSize() {
        return validCnnServersSize;
    }

    public void setValidCnnServersSize(int validCnnServersSize) {
        this.validCnnServersSize = validCnnServersSize;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public JedisPool getLocalJedisPool() {
        return localJedisPool;
    }

    public void setLocalJedisPool(JedisPool localJedisPool) {
        this.localJedisPool = localJedisPool;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public List<String> getConfigFileNames() {
        return configFileNames;
    }

    public void setConfigFileNames(List<String> configFileNames) {
        this.configFileNames = configFileNames;
    }

    public List<EmServer> getEmServers() {
        return emServers;
    }

    public void setEmServers(List<EmServer> emServers) {
        this.emServers = emServers;
    }

    public List<EmServer> getEmUnusedServers() {
        return emUnusedServers;
    }

    public void setEmUnusedServers(List<EmServer> emUnusedServers) {
        this.emUnusedServers = emUnusedServers;
    }

    public Map<Integer, EmServer> getEmWeightTable() {
        return emWeightTable;
    }

    public void setEmWeightTable(Map<Integer, EmServer> emWeightTable) {
        this.emWeightTable = emWeightTable;
    }

    public int getEmTotalWeight() {
        return emTotalWeight;
    }

    public void setEmTotalWeight(int emTotalWeight) {
        this.emTotalWeight = emTotalWeight;
    }

    public List<EmServer> getEmServersPollingList() {
        return emServersPollingList;
    }

    public void setEmServersPollingList(List<EmServer> emServersPollingList) {
        this.emServersPollingList = emServersPollingList;
    }

    public int getEmServersPollingListSize() {
        return emServersPollingListSize;
    }

    public void setEmServersPollingListSize(int emServersPollingListSize) {
        this.emServersPollingListSize = emServersPollingListSize;
    }

    public long getCurrentPollingEmServerIndex() {
        return currentPollingEmServerIndex.get();
    }

    public void setCurrentPollingEmServerIndex(long currentPollingEmServerIndex) {
        this.currentPollingEmServerIndex.set(currentPollingEmServerIndex);
    }

    public long incrementAndGetCurrentPollingEmServerIndex() {
        return currentPollingEmServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingEmServerIndex() {
        return currentPollingEmServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingEmServerIndex() {
        return currentPollingEmServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingEmServerIndex() {
        return currentPollingEmServerIndex.getAndDecrement();
    }

    public Configuration getHbaseConfig() {
        return hbaseConfig;
    }

    public void setHbaseConfig(Configuration hbaseConfig) {
        this.hbaseConfig = hbaseConfig;
    }

    public Configuration getHbaseConfigBk() {
        return hbaseConfigBk;
    }

    public void setHbaseConfigBk(Configuration hbaseConfigBk) {
        this.hbaseConfigBk = hbaseConfigBk;
    }

    public HbClient getHbClient() {
        return hbClient;
    }

    public void setHbClient(HbClient hbClient) {
        this.hbClient = hbClient;
    }

    public HbClient getHbClientBk() {
        return hbClientBk;
    }

    public void setHbClientBk(HbClient hbClientBk) {
        this.hbClientBk = hbClientBk;
    }

    public List<SearchServer> getSearchByIdServers() {
        return searchByIdServers;
    }

    public void setSearchByIdServers(List<SearchServer> searchByIdServers) {
        this.searchByIdServers = searchByIdServers;
    }

    public List<SearchServer> getSearchByIdUnusedServers() {
        return searchByIdUnusedServers;
    }

    public void setSearchByIdUnusedServers(
            List<SearchServer> searchByIdUnusedServers) {
        this.searchByIdUnusedServers = searchByIdUnusedServers;
    }

    public List<IeServer> getIeServers() {
        return ieServers;
    }

    public void setIeServers(List<IeServer> ieServers) {
        this.ieServers = ieServers;
    }

    public List<IeServer> getIeUnusedServers() {
        return ieUnusedServers;
    }

    public void setIeUnusedServers(List<IeServer> ieUnusedServers) {
        this.ieUnusedServers = ieUnusedServers;
    }

    public Map<Integer, IeServer> getIeWeightTable() {
        return ieWeightTable;
    }

    public void setIeWeightTable(Map<Integer, IeServer> ieWeightTable) {
        this.ieWeightTable = ieWeightTable;
    }

    public int getIeTotalWeight() {
        return ieTotalWeight;
    }

    public void setIeTotalWeight(int ieTotalWeight) {
        this.ieTotalWeight = ieTotalWeight;
    }

    public List<IeServer> getIeServersPollingList() {
        return ieServersPollingList;
    }

    public void setIeServersPollingList(List<IeServer> ieServersPollingList) {
        this.ieServersPollingList = ieServersPollingList;
    }

    public int getIeServersPollingListSize() {
        return ieServersPollingListSize;
    }

    public void setIeServersPollingListSize(int ieServersPollingListSize) {
        this.ieServersPollingListSize = ieServersPollingListSize;
    }

    public long getCurrentPollingIeServerIndex() {
        return currentPollingIeServerIndex.get();
    }

    public void setCurrentPollingIeServerIndex(long currentPollingIeServerIndex) {
        this.currentPollingIeServerIndex.set(currentPollingIeServerIndex);
    }

    public long incrementAndGetCurrentPollingIeServerIndex() {
        return currentPollingIeServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingIeServerIndex() {
        return currentPollingIeServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingIeServerIndex() {
        return currentPollingIeServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingIeServerIndex() {
        return currentPollingIeServerIndex.getAndDecrement();
    }

    public List<SearchServer> getSdkSearchServers() {
        return sdkSearchServers;
    }

    public void setSdkSearchServers(List<SearchServer> sdkSearchServers) {
        this.sdkSearchServers = sdkSearchServers;
    }

    public List<SearchServer> getSdkSearchServersPollingList() {
        return sdkSearchServersPollingList;
    }

    public void setSdkSearchServersPollingList(
            List<SearchServer> sdkSearchServersPollingList) {
        this.sdkSearchServersPollingList = sdkSearchServersPollingList;
    }

    public int getSdkSearchServersPollingListSize() {
        return sdkSearchServersPollingListSize;
    }

    public void setSdkSearchServersPollingListSize(
            int sdkSearchServersPollingListSize) {
        this.sdkSearchServersPollingListSize = sdkSearchServersPollingListSize;
    }

    public long getCurrentPollingSdkSearchServerIndex() {
        return currentPollingSdkSearchServerIndex.get();
    }

    public void setCurrentPollingSdkSearchServerIndex(
            long currentPollingSdkSearchServerIndex) {
        this.currentPollingSdkSearchServerIndex
                .set(currentPollingSdkSearchServerIndex);
    }

    public long incrementAndGetCurrentPollingSdkSearchServerIndex() {
        return currentPollingSdkSearchServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingSdkSearchServerIndex() {
        return currentPollingSdkSearchServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingSdkSearchServerIndex() {
        return currentPollingSdkSearchServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingSdkSearchServerIndex() {
        return currentPollingSdkSearchServerIndex.getAndDecrement();
    }

    public List<OcrServer> getHandwriteOcrServers() {
        return handwriteOcrServers;
    }

    public void setHandwriteOcrServers(List<OcrServer> handwriteOcrServers) {
        this.handwriteOcrServers = handwriteOcrServers;
    }

    public List<OcrServer> getHandwriteOcrServersPollingList() {
        return handwriteOcrServersPollingList;
    }

    public void setHandwriteOcrServersPollingList(
            List<OcrServer> handwriteOcrServersPollingList) {
        this.handwriteOcrServersPollingList = handwriteOcrServersPollingList;
    }

    public int getHandwriteOcrServersPollingListSize() {
        return handwriteOcrServersPollingListSize;
    }

    public void setHandwriteOcrServersPollingListSize(
            int handwriteOcrServersPollingListSize) {
        this.handwriteOcrServersPollingListSize = handwriteOcrServersPollingListSize;
    }

    public long getCurrentPollingHandwriteOcrServerIndex() {
        return currentPollingHandwriteOcrServerIndex.get();
    }

    public void setCurrentPollingHandwriteOcrServerIndex(
            long currentPollingHandwriteOcrServerIndex) {
        this.currentPollingHandwriteOcrServerIndex
                .set(currentPollingHandwriteOcrServerIndex);
    }

    public long incrementAndGetCurrentPollingHandwriteOcrServerIndex() {
        return currentPollingHandwriteOcrServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingHandwriteOcrServerIndex() {
        return currentPollingHandwriteOcrServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingHandwriteOcrServerIndex() {
        return currentPollingHandwriteOcrServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingHandwriteOcrServerIndex() {
        return currentPollingHandwriteOcrServerIndex.getAndDecrement();
    }

    public List<SearchServer> getSearchMatrixServers() {
        return searchMatrixServers;
    }

    public void setSearchMatrixServers(List<SearchServer> searchMatrixServers) {
        this.searchMatrixServers = searchMatrixServers;
    }

    public List<SearchServer> getSearchMatrixServersPollingList() {
        return searchMatrixServersPollingList;
    }

    public void setSearchMatrixServersPollingList(
            List<SearchServer> searchMatrixServersPollingList) {
        this.searchMatrixServersPollingList = searchMatrixServersPollingList;
    }

    public int getSearchMatrixServersPollingListSize() {
        return searchMatrixServersPollingListSize;
    }

    public void setSearchMatrixServersPollingListSize(
            int searchMatrixServersPollingListSize) {
        this.searchMatrixServersPollingListSize = searchMatrixServersPollingListSize;
    }

    public long getCurrentPollingSearchMatrixServerIndex() {
        return currentPollingHandwriteOcrServerIndex.get();
    }

    public void setCurrentPollingSearchMatrixServerIndex(
            long currentPollingSearchMatrixServerIndex) {
        this.currentPollingSearchMatrixServerIndex
                .set(currentPollingSearchMatrixServerIndex);
    }

    public long incrementAndGetCurrentPollingSearchMatrixServerIndex() {
        return currentPollingSearchMatrixServerIndex.incrementAndGet();
    }

    public long decrementAndGetCurrentPollingSearchMatrixServerIndex() {
        return currentPollingSearchMatrixServerIndex.decrementAndGet();
    }

    public long getAndIncrementCurrentPollingSearchMatrixServerIndex() {
        return currentPollingSearchMatrixServerIndex.getAndIncrement();
    }

    public long getAndDecrementCurrentPollingSearchMatrixServerIndex() {
        return currentPollingSearchMatrixServerIndex.getAndDecrement();
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

}

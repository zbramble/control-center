package com.wenba.scheduler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangbo
 *
 */
public class AbstractServer {

    // 成员变量
    /**
     * server id
     */
    private String id;

    /**
     * server state
     */
    private ServerState state;

    /**
     * ip
     */
    private String ip;

    /**
     * port
     */
    private String port;

    /**
     * server url
     */
    private String url;

    /**
     * search level
     */
    private String level;

    /**
     * server type
     */
    private ServerType serverType;

    /**
     * fail connection num
     */
    private AtomicLong failConnNum;

    /**
     * server weight
     */
    private int weight;

    /**
     * server absolute weight(key in weight table)
     */
    private int absoluteWeight;

    /**
     * used for monitor server state and decided whether sending warning email
     */
    private AtomicInteger marks;

    /**
     * server start time
     */
    private long startTime;

    public AbstractServer() {
        failConnNum = new AtomicLong(0);
        marks = new AtomicInteger(SchedulerConstants.MARKS_100);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getFailConnNum() {
        return failConnNum.get();
    }

    public void setFailConnNum(long failConnNum) {
        this.failConnNum.set(failConnNum);
    }

    public long incrementAndGetFailConnNum() {
        return failConnNum.incrementAndGet();
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getAbsoluteWeight() {
        return absoluteWeight;
    }

    public void setAbsoluteWeight(int absoluteWeight) {
        this.absoluteWeight = absoluteWeight;
    }

    public int getMarks() {
        return marks.get();
    }

    public void setMarks(int marks) {
        this.marks.set(marks);
    }

    public int incrementAndGetMarks() {
        return marks.incrementAndGet();
    }

    public int decrementAndGetMarks() {
        return marks.decrementAndGet();
    }

    /**
     * @author zhangbo
     *
     */
    public enum ServerType {
        JAVA(0), CNN(1), HANDWRITE_OCR(2), SEARCH(3), BI(4), NLP(5), CONFIG(6), SEARCH_ARTICLE(
                7), SEARCH_BY_ID(8), SEARCH_HOMEWORK(9), EM(10), IE(11), SDK_SEARCH(
                12), UGC_HANDWRITE_OCR(13), SEARCH_MATRIX(14), WORD_SEARCH(50);

        private final int value;

        public int getValue() {
            return value;
        }

        ServerType(int value) {
            this.value = value;
        }
    }

    /**
     * @author zhangbo
     *
     */
    public enum ServerState {
        UP, DOWN;
    }

    /**
     * @author zhangbo
     *
     */
    public enum OcrType {
        CNN(1), HANDWRITE_OCR(2), HANDWRITE_OCR_STATUS_0(3);

        private final int value;

        public int getValue() {
            return value;
        }

        OcrType(int value) {
            this.value = value;
        }
    }

}

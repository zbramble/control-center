package com.wenba.scheduler.search;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.wenba.scheduler.AbstractServerStatistics;

/**
 * @author zhangbo
 *
 */
public class SearchServerStatistics extends AbstractServerStatistics {

    /**
     * exec time by word search
     */
    private AtomicLong execTimeByWord;

    /**
     * exec time by word search num
     */
    private AtomicInteger execTimeByWordNum;

    /**
     * exec time by id
     */
    private AtomicLong execTimeById;

    /**
     * exec time by id num
     */
    private AtomicInteger execTimeByIdNum;

    /**
     * exec time by mining homework
     */
    private AtomicLong execTimeByHomework;

    /**
     * exec time by mining homework num
     */
    private AtomicInteger execTimeByHomeworkNum;

    /**
     * exec time by search article
     */
    private AtomicLong execTimeByArticle;

    /**
     * exec time by search article num
     */
    private AtomicInteger execTimeByArticleNum;

    public SearchServerStatistics() {
        execTimeByWord = new AtomicLong(0);
        execTimeByWordNum = new AtomicInteger(0);
        execTimeById = new AtomicLong(0);
        execTimeByIdNum = new AtomicInteger(0);
        execTimeByHomework = new AtomicLong(0);
        execTimeByHomeworkNum = new AtomicInteger(0);
        execTimeByArticle = new AtomicLong(0);
        execTimeByArticleNum = new AtomicInteger(0);
    }

    public int getExecTimeByArticleNum() {
        return execTimeByArticleNum.get();
    }

    public void setExecTimeByArticleNum(int execTimeByArticleNum) {
        this.execTimeByArticleNum.set(execTimeByArticleNum);
    }

    public int incrementAndGetExecTimeByArticleNum() {
        return execTimeByArticleNum.incrementAndGet();
    }

    public long getExecTimeByArticle() {
        return execTimeByArticle.get();
    }

    public void setExecTimeByArticle(long execTimeByArticle) {
        this.execTimeByArticle.set(execTimeByArticle);
    }

    public long addAndGetExecTimeByArticle(long execTimeByArticle) {
        return this.execTimeByArticle.addAndGet(execTimeByArticle);
    }

    public int getExecTimeByHomeworkNum() {
        return execTimeByHomeworkNum.get();
    }

    public void setExecTimeByHomeworkNum(int execTimeByHomeworkNum) {
        this.execTimeByHomeworkNum.set(execTimeByHomeworkNum);
    }

    public int incrementAndGetExecTimeByHomeworkNum() {
        return execTimeByHomeworkNum.incrementAndGet();
    }

    public long getExecTimeByHomework() {
        return execTimeByHomework.get();
    }

    public void setExecTimeByHomework(long execTimeByHomework) {
        this.execTimeByHomework.set(execTimeByHomework);
    }

    public long addAndGetExecTimeByHomework(long execTimeByHomework) {
        return this.execTimeByHomework.addAndGet(execTimeByHomework);
    }

    public int getExecTimeByIdNum() {
        return execTimeByIdNum.get();
    }

    public void setExecTimeByIdNum(int execTimeByIdNum) {
        this.execTimeByIdNum.set(execTimeByIdNum);
    }

    public int incrementAndGetExecTimeByIdNum() {
        return execTimeByIdNum.incrementAndGet();
    }

    public long getExecTimeById() {
        return execTimeById.get();
    }

    public void setExecTimeById(long execTimeById) {
        this.execTimeById.set(execTimeById);
    }

    public long addAndGetExecTimeById(long execTimeById) {
        return this.execTimeById.addAndGet(execTimeById);
    }

    public int getExecTimeByWordNum() {
        return execTimeByWordNum.get();
    }

    public void setExecTimeByWordNum(int execTimeByWordNum) {
        this.execTimeByWordNum.set(execTimeByWordNum);
    }

    public int incrementAndGetExecTimeByWordNum() {
        return execTimeByWordNum.incrementAndGet();
    }

    public long getExecTimeByWord() {
        return execTimeByWord.get();
    }

    public void setExecTimeByWord(long execTimeByWord) {
        this.execTimeByWord.set(execTimeByWord);
    }

    public long addAndGetExecTimeByWord(long execTimeByWord) {
        return this.execTimeByWord.addAndGet(execTimeByWord);
    }
}

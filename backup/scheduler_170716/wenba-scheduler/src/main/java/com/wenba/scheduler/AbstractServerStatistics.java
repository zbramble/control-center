package com.wenba.scheduler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangbo
 *
 */
public class AbstractServerStatistics {

    // 成员变量
    /**
     * current request
     */
    private AtomicInteger currentRequestNum;

    /**
     * server access num
     */
    private AtomicLong accessNum;

    /**
     * server first fail num
     */
    private AtomicLong firstFailNum;

    /**
     * server second fail num
     */
    private AtomicLong secondFailNum;

    /**
     * server first success num
     */
    private AtomicLong firstSuccessNum;

    /**
     * server second success num
     */
    private AtomicLong secondSuccessNum;

    /**
     * server fail num used for warning
     */
    private AtomicInteger failNum;

    /**
     * exec time
     */
    private AtomicLong execTime;

    /**
     * exec time num
     */
    private AtomicInteger execTimeNum;

    /**
     * http host connect exception num
     */
    private AtomicInteger hhceNum;

    /**
     * connect timeout exception num
     */
    private AtomicInteger cteNum;

    /**
     * no http response exception num
     */
    private AtomicInteger nhreNum;

    /**
     * socket timeout exception num
     */
    private AtomicInteger steNum;

    /**
     * client protocol exception num
     */
    private AtomicInteger cpeNum;

    /**
     * URI syntax exception num
     */
    private AtomicInteger useNum;

    /**
     * IO exception num
     */
    private AtomicInteger ioeNum;

    /**
     * JSON exception num
     */
    private AtomicInteger jeNum;

    /**
     * unsupported encoding exception num
     */
    private AtomicInteger ueeNum;

    /**
     * Http Status Not ok Exception
     */
    private AtomicInteger hsneNum;

    /**
     * Http Entity is Null Exception
     */
    private AtomicInteger heneNum;

    /**
     * Http Entity to String is null Exception
     */
    private AtomicInteger heseNum;

    /**
     * ocr fail type exception
     */
    private AtomicInteger ofteNum;

    /**
     * index out of bounds exception
     */
    private AtomicInteger ioobeNum;

    /**
     * other exception
     */
    private AtomicInteger oeNum;

    public AbstractServerStatistics() {
        currentRequestNum = new AtomicInteger(0);
        accessNum = new AtomicLong(0);
        firstFailNum = new AtomicLong(0);
        secondFailNum = new AtomicLong(0);
        firstSuccessNum = new AtomicLong(0);
        secondSuccessNum = new AtomicLong(0);
        failNum = new AtomicInteger(0);
        execTime = new AtomicLong(0);
        execTimeNum = new AtomicInteger(0);
        hhceNum = new AtomicInteger(0);
        cteNum = new AtomicInteger(0);
        nhreNum = new AtomicInteger(0);
        steNum = new AtomicInteger(0);
        cpeNum = new AtomicInteger(0);
        useNum = new AtomicInteger(0);
        ioeNum = new AtomicInteger(0);
        jeNum = new AtomicInteger(0);
        ueeNum = new AtomicInteger(0);
        hsneNum = new AtomicInteger(0);
        heneNum = new AtomicInteger(0);
        heseNum = new AtomicInteger(0);
        ofteNum = new AtomicInteger(0);
        ioobeNum = new AtomicInteger(0);
        oeNum = new AtomicInteger(0);
    }

    public int getCurrentRequestNum() {
        return currentRequestNum.get();
    }

    public void setCurrentRequestNum(int currentRequestNum) {
        this.currentRequestNum.set(currentRequestNum);
    }

    public int incrementAndGetCurrentRequestNum() {
        return currentRequestNum.incrementAndGet();
    }

    public int decrementAndGetCurrentRequestNum() {
        return currentRequestNum.decrementAndGet();
    }

    public int getIoobeNum() {
        return ioobeNum.get();
    }

    public void setIoobeNum(int ioobeNum) {
        this.ioobeNum.set(ioobeNum);
    }

    public int incrementAndGetIoobeNum() {
        return ioobeNum.incrementAndGet();
    }

    public int getOeNum() {
        return oeNum.get();
    }

    public void setOeNum(int oeNum) {
        this.oeNum.set(oeNum);
    }

    public int incrementAndGetOeNum() {
        return oeNum.incrementAndGet();
    }

    public int getOfteNum() {
        return ofteNum.get();
    }

    public void setOfteNum(int ofteNum) {
        this.ofteNum.set(ofteNum);
    }

    public int incrementAndGetOfteNum() {
        return ofteNum.incrementAndGet();
    }

    public int getHeseNum() {
        return heseNum.get();
    }

    public void setHeseNum(int heseNum) {
        this.heseNum.set(heseNum);
    }

    public int incrementAndGetHeseNum() {
        return heseNum.incrementAndGet();
    }

    public int getHeneNum() {
        return heneNum.get();
    }

    public void setHeneNum(int heneNum) {
        this.heneNum.set(heneNum);
    }

    public int incrementAndGetHeneNum() {
        return heneNum.incrementAndGet();
    }

    public int getHsneNum() {
        return hsneNum.get();
    }

    public void setHsneNum(int hsneNum) {
        this.hsneNum.set(hsneNum);
    }

    public int incrementAndGetHsneNum() {
        return hsneNum.incrementAndGet();
    }

    public int getUeeNum() {
        return ueeNum.get();
    }

    public void setUeeNum(int ueeNum) {
        this.ueeNum.set(ueeNum);
    }

    public int incrementAndGetUeeNum() {
        return ueeNum.incrementAndGet();
    }

    public int getJeNum() {
        return jeNum.get();
    }

    public void setJeNum(int jeNum) {
        this.jeNum.set(jeNum);
    }

    public int incrementAndGetJeNum() {
        return jeNum.incrementAndGet();
    }

    public int getIoeNum() {
        return ioeNum.get();
    }

    public void setIoeNum(int ioeNum) {
        this.ioeNum.set(ioeNum);
    }

    public int incrementAndGetIoeNum() {
        return ioeNum.incrementAndGet();
    }

    public int getUseNum() {
        return useNum.get();
    }

    public void setUseNum(int useNum) {
        this.useNum.set(useNum);
    }

    public int incrementAndGetUseNum() {
        return useNum.incrementAndGet();
    }

    public int getCpeNum() {
        return cpeNum.get();
    }

    public void setCpeNum(int cpeNum) {
        this.cpeNum.set(cpeNum);
    }

    public int incrementAndGetCpeNum() {
        return cpeNum.incrementAndGet();
    }

    public int getSteNum() {
        return steNum.get();
    }

    public void setSteNum(int steNum) {
        this.steNum.set(steNum);
    }

    public int incrementAndGetSteNum() {
        return steNum.incrementAndGet();
    }

    public int getNhreNum() {
        return nhreNum.get();
    }

    public void setNhreNum(int nhreNum) {
        this.nhreNum.set(nhreNum);
    }

    public int incrementAndGetNhreNum() {
        return nhreNum.incrementAndGet();
    }

    public int getCteNum() {
        return cteNum.get();
    }

    public void setCteNum(int cteNum) {
        this.cteNum.set(cteNum);
    }

    public int incrementAndGetCteNum() {
        return cteNum.incrementAndGet();
    }

    public int getHhceNum() {
        return hhceNum.get();
    }

    public void setHhceNum(int hhceNum) {
        this.hhceNum.set(hhceNum);
    }

    public int incrementAndGetHhceNum() {
        return hhceNum.incrementAndGet();
    }

    public int getExecTimeNum() {
        return execTimeNum.get();
    }

    public void setExecTimeNum(int execTimeNum) {
        this.execTimeNum.set(execTimeNum);
    }

    public int incrementAndGetExecTimeNum() {
        return execTimeNum.incrementAndGet();
    }

    public long getExecTime() {
        return execTime.get();
    }

    public void setExecTime(long execTime) {
        this.execTime.set(execTime);
    }

    public long addAndGetExecTime(long execTime) {
        return this.execTime.addAndGet(execTime);
    }

    public long getAccessNum() {
        return accessNum.get();
    }

    public void setAccessNum(long accessNum) {
        this.accessNum.set(accessNum);
    }

    public long incrementAndGetAccessNum() {
        return accessNum.incrementAndGet();
    }

    public long getFirstFailNum() {
        return firstFailNum.get();
    }

    public void setFirstFailNum(long firstFailNum) {
        this.firstFailNum.set(firstFailNum);
    }

    public long incrementAndGetFirstFailNum() {
        return firstFailNum.incrementAndGet();
    }

    public long getSecondFailNum() {
        return secondFailNum.get();
    }

    public void setSecondFailNum(long secondFailNum) {
        this.secondFailNum.set(secondFailNum);
    }

    public long incrementAndGetSecondFailNum() {
        return secondFailNum.incrementAndGet();
    }

    public long getFirstSuccessNum() {
        return firstSuccessNum.get();
    }

    public void setFirstSuccessNum(long firstSuccessNum) {
        this.firstSuccessNum.set(firstSuccessNum);
    }

    public long incrementAndGetFirstSuccessNum() {
        return firstSuccessNum.incrementAndGet();
    }

    public long getSecondSuccessNum() {
        return secondSuccessNum.get();
    }

    public void setSecondSuccessNum(long secondSuccessNum) {
        this.secondSuccessNum.set(secondSuccessNum);
    }

    public long incrementAndGetSecondSuccessNum() {
        return secondSuccessNum.incrementAndGet();
    }

    public int getFailNum() {
        return failNum.get();
    }

    public void setFailNum(int failNum) {
        this.failNum.set(failNum);
    }

    public int incrementAndGetFailNum() {
        return failNum.incrementAndGet();
    }

}

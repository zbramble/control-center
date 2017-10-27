package com.wenba.scheduler.email;

import com.wenba.scheduler.AbstractParam;
import com.wenba.scheduler.ISchedulerStrategy;
import com.wenba.scheduler.config.ConfigParam;
import com.wenba.scheduler.config.ConfigResult;

/**
 * @author zhangbo
 *
 */
public class EmailParam extends AbstractParam {

    // 成员变量
    /**
     * Scheduler ID
     */
    private String schedulerId;

    /**
     * the recipient email address
     */
    private String receiverAddress;

    /**
     * the sender email address
     */
    private String senderAddress;

    /**
     * the subject of the email
     */
    private String sub;

    /**
     * the message content of the email
     */
    private String msg;

    /**
     * success or not
     */
    private boolean success;

    /**
     * config strategy
     */
    private ISchedulerStrategy<ConfigParam, ConfigResult> configStrategy;

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ISchedulerStrategy<ConfigParam, ConfigResult> getConfigStrategy() {
        return configStrategy;
    }

    public void setConfigStrategy(
            ISchedulerStrategy<ConfigParam, ConfigResult> configStrategy) {
        this.configStrategy = configStrategy;
    }

}

package com.wenba.scheduler;

/**
 * @author zhangbo
 *
 */
public class AbstractResult {

    // 成员变量
    /**
     * return result
     */
    private String schedulerResult;

    /**
     * excute time
     */
    private long excuteTime;

    /**
     * statusCode
     */
    private StatusCode statusCode;

    /**
     * version
     */
    private String version;

    public String getSchedulerResult() {
        return schedulerResult;
    }

    public void setSchedulerResult(String schedulerResult) {
        this.schedulerResult = schedulerResult;
    }

    public long getExcuteTime() {
        return excuteTime;
    }

    public void setExcuteTime(long excuteTime) {
        this.excuteTime = excuteTime;
    }

    public String toString() {
        return schedulerResult;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @author zhangbo
     *
     */
    public enum StatusCode {
        OK(0), STATUS_NOT_200(1), REQUEST_TIMEOUT(2), RESPONSE_TIMEOUT(3), NOSERVER(
                4), NORESULT(5), OCR_FAILED_IMG(6), OCR_FAILED_OCR(7), JSON_EXCEPTION(
                8), IMG_NAME_LENGTH_ERROR(9), NO_IMG_NAME_ERROR(10), GET_IMG_FAIL(
                11), OTHER(12);

        private final int value;

        public int getValue() {
            return value;
        }

        StatusCode(int value) {
            this.value = value;
        }
    }

}

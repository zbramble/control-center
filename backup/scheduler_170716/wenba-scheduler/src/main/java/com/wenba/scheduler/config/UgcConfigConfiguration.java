package com.wenba.scheduler.config;

/**
 * @author zhangbo
 *
 */
public class UgcConfigConfiguration {

    // 成员变量
    /**
     * region
     */
    private String region;

    /**
     * expire in
     */
    private int expireIn;

    /**
     * public key
     */
    private String publicKey;

    /**
     * private key
     */
    private String privateKey;

    /**
     * common api url
     */
    private String commonApiUrl;

    /**
     * task api url
     */
    private String taskApiUrl;

    /**
     * image name
     */
    private String imageName;

    /**
     * access token
     */
    private String accessToken;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(int expireIn) {
        this.expireIn = expireIn;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getCommonApiUrl() {
        return commonApiUrl;
    }

    public void setCommonApiUrl(String commonApiUrl) {
        this.commonApiUrl = commonApiUrl;
    }

    public String getTaskApiUrl() {
        return taskApiUrl;
    }

    public void setTaskApiUrl(String taskApiUrl) {
        this.taskApiUrl = taskApiUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}

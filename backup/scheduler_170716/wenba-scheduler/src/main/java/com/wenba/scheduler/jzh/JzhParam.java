package com.wenba.scheduler.jzh;

import org.springframework.web.multipart.MultipartFile;

import com.wenba.scheduler.AbstractParam;

/**
 * @author zhangbo
 *
 */
public class JzhParam extends AbstractParam {

    // 成员变量
    /**
     * ie server for excute ie
     */
    private IeServer ieServer;

    /**
     * img file
     */
    private MultipartFile img;

    public IeServer getIeServer() {
        return ieServer;
    }

    public void setIeServer(IeServer ieServer) {
        this.ieServer = ieServer;
    }

    public MultipartFile getImg() {
        return img;
    }

    public void setImg(MultipartFile img) {
        this.img = img;
    }

}

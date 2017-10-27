package com.wenba.scheduler.ocr;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

import com.wenba.scheduler.AbstractParam;
import com.wenba.scheduler.ISchedulerStrategy;
import com.wenba.scheduler.nlp.NlpParam;
import com.wenba.scheduler.nlp.NlpResult;

/**
 * @author zhangbo
 *
 */
public class OcrParam extends AbstractParam {

    // 成员变量
    /**
     * ocr server for excute ocr
     */
    private OcrServer ocrServer;

    /**
     * img file
     */
    private MultipartFile img;

    /**
     * nlp Strategy
     */
    private ISchedulerStrategy<NlpParam, NlpResult> nlpStrategy;

    /**
     * img file
     */
    private File imgFile;

    /**
     * layoutinfo
     */
    private String layoutinfo;

    /**
     * rotate
     */
    private int rotate;

    /**
     * use layoutinfo or not
     */
    private boolean useLayoutinfoOrNot;

    /**
     * ocr & nlp excute time
     */
    private long ocrAndNlpExcuteTime;

    public OcrServer getOcrServer() {
        return ocrServer;
    }

    public void setOcrServer(OcrServer ocrServer) {
        this.ocrServer = ocrServer;
    }

    public MultipartFile getImg() {
        return img;
    }

    public void setImg(MultipartFile img) {
        this.img = img;
    }

    public ISchedulerStrategy<NlpParam, NlpResult> getNlpStrategy() {
        return nlpStrategy;
    }

    public void setNlpStrategy(
            ISchedulerStrategy<NlpParam, NlpResult> nlpStrategy) {
        this.nlpStrategy = nlpStrategy;
    }

    public File getImgFile() {
        return imgFile;
    }

    public void setImgFile(File imgFile) {
        this.imgFile = imgFile;
    }

    public String getLayoutinfo() {
        return layoutinfo;
    }

    public void setLayoutinfo(String layoutinfo) {
        this.layoutinfo = layoutinfo;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public boolean isUseLayoutinfoOrNot() {
        return useLayoutinfoOrNot;
    }

    public void setUseLayoutinfoOrNot(boolean useLayoutinfoOrNot) {
        this.useLayoutinfoOrNot = useLayoutinfoOrNot;
    }

    public long getOcrAndNlpExcuteTime() {
        return ocrAndNlpExcuteTime;
    }

    public void setOcrAndNlpExcuteTime(long ocrAndNlpExcuteTime) {
        this.ocrAndNlpExcuteTime = ocrAndNlpExcuteTime;
    }

}

package com.wenba.scheduler.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * config from html
 * 
 * @author zhangbo
 *
 */
public class ConfigFromHtmlStrategy implements
        ISchedulerStrategy<ConfigParam, ConfigResult> {

    // 成员变量
    private static Logger logger = LogManager
            .getLogger(ConfigFromHtmlStrategy.class);

    public ConfigResult excute(ConfigParam configParam) {
        ConfigResult configResult = null;

        JSONObject configData = configParam.getConfigData();
        logger.info(configData.toString());

        File config = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            config = new File("config");
            fw = new FileWriter(config);
            bw = new BufferedWriter(fw);
            bw.write(configData.toString());
            bw.close();
        } catch (IOException e) {
            logger.error("IOE!");
        } finally {
            config = null;
            fw = null;
            bw = null;
        }

        return configResult;
    }

}

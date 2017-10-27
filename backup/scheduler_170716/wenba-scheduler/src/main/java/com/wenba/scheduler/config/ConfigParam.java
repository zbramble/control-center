package com.wenba.scheduler.config;

import net.sf.json.JSONObject;

import com.wenba.scheduler.AbstractParam;

/**
 * @author zhangbo
 *
 */
public class ConfigParam extends AbstractParam {

    // 成员变量
    /**
     * config data from html
     */
    private JSONObject configData;

    /**
     * config file type
     */
    private ConfigFileType configFileType;

    public JSONObject getConfigData() {
        return configData;
    }

    public void setConfigData(JSONObject configData) {
        this.configData = configData;
    }

    public ConfigFileType getConfigFileType() {
        return configFileType;
    }

    public void setConfigFileType(ConfigFileType configFileType) {
        this.configFileType = configFileType;
    }

    /**
     * @author zhangbo
     *
     */
    public enum ConfigFileType {
        ALL("all"), MAIL_CONFIGURATION("mail_configuration"), SYSTEM_DATA(
                "system_data"), SYSTEM_SWITCH("system_switch"), DEBUG_SWITCH(
                "debug_switch"), ACCESS_SERVERS("access_servers"), CNN_SERVERS(
                "cnn_servers"), JAVA_SERVERS("java_servers"), SEARCH_SERVERS(
                "search_servers"), SEARCH_HOMEWORK_SERVERS(
                "search_homework_servers"), SEARCH_ARTICLE_SERVERS(
                "search_article_servers"), NLP_SERVERS("nlp_servers"), BI_SERVERS(
                "bi_servers"), TIMEOUT_DATA("timeout_data"), EM_SERVERS(
                "em_servers"), HBASE_CONFIG("hbase_config"), HBASE_CONFIG_BK(
                "hbase_config_bk"), SEARCH_BY_ID_SERVERS("search_by_id_servers"), IE_SERVERS(
                "ie_servers"), SDK_SEARCH_SERVERS("sdk_search_servers"), HANDWRITE_OCR_SERVERS(
                "handwrite_ocr_servers"), UGC_CONFIG("ugc_config"), SEARCH_MATRIX_SERVERS(
                "search_matrix_servers");

        private final String value;

        public String getValue() {
            return value;
        }

        ConfigFileType(String value) {
            this.value = value;
        }
    }
}

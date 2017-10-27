package com.wenba.scheduler;

/**
 * Scheduler Constants
 * 
 * @author zhangbo
 *
 */
public interface SchedulerConstants {

    // constants
    String HTTP_URL = "http://";
    String COLON = ":";

    int MIN_IDLE = 100;
    int MAX_IDLE = 300;
    int MAX_TOTAL = 500;

    int MARKS_100 = 100;

    String ALL = "all";
    String MAIL_CONFIGURATION = "mail_configuration";
    String SYSTEM_DATA = "system_data";
    String SYSTEM_SWITCH = "system_switch";
    String DEBUG_SWITCH = "debug_switch";
    String ACCESS_SERVERS = "access_servers";
    String CNN_SERVERS = "cnn_servers";
    String JAVA_SERVERS = "java_servers";
    String SEARCH_SERVERS = "search_servers";
    String SEARCH_HOMEWORK_SERVERS = "search_homework_servers";
    String SEARCH_ARTICLE_SERVERS = "search_article_servers";
    String SEARCH_BY_ID_SERVERS = "search_by_id_servers";
    String NLP_SERVERS = "nlp_servers";
    String BI_SERVERS = "bi_servers";
    String TIMEOUT_DATA = "timeout_data";
    
    String APP_MIGU = "migu";
    String APP_SDK = "sdk";
    
    int GET_REDIS_TIMES = 3;
    int SLEEP_TIME = 1000;
}

package com.flink.streaming.web.common;

import com.flink.streaming.common.constant.SystemConstant;

public class FlinkStandaloneRestUriConstants {

  public static final String URI_CONFIG = "config";

  public static final String URI_JOBMANAGER = "jobmanager";


  public static String getUriJobsForStandalone() {
    return URI_JOBMANAGER + SystemConstant.VIRGULE  + URI_CONFIG;
  }

}

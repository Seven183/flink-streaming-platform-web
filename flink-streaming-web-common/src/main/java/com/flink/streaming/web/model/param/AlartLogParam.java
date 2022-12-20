package com.flink.streaming.web.model.param;

import com.flink.streaming.web.model.page.PageParam;
import lombok.Data;


@Data
public class AlartLogParam extends PageParam {

  private Long jobConfigId;

  /**
   * 1:钉钉  2:回调  3:企业微信
   */
  private Integer type;

  /**
   * 1:成功 0:失败
   */
  private Integer status;
  private String jobName;

}

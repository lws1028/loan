package com.zlhj.unifiedInputPlatform.ant.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClueApplyOutput {

    /**
     * 接收状态
     * 1：接收成功
     * 2：接收失败
     */
    private String status;

    /**
     * 星河侧唯一业务编号
     */
    private String applyNo;

    /**
     * 机构侧唯一业务编号，机构接收成功时必传
     */
    private String outApplyNo;

    /**
     * 接收失败原因，接收失败时必传
     */
    private String refuseMsg;
}

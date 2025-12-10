package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.dto;

import lombok.Data;

/**
 * 运维发送萨摩耶接口参数
 *
 * @author : LXY
 * @date : 2023/6/8 23:17
 */
@Data
public class OperationSendSmyStatusDto {

    /**
     * 申请编号
     */
    private String applyNumber;

    /**
     * 发送状态
     */
    private String status;
}

package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 线索信息结构 接受参数
 */
@Data
@ToString
public class CluesInformationRequest {

    /**
     * 订单编号
     */
    private Long orderId;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 用户手机号
     */
    private String phone;
    /**
     * 车牌号
     */
    private String licensePlateNum;
    /**
     * 所在省编码
     */
    private String provinceCode;
    /**
     * 所在市编码
     */
    private String cityCode;
    /**
     * 车辆价值
     */
    private String carValue;
}

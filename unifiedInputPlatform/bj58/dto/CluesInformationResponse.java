package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Getter;
import lombok.ToString;

/**
 * 线索信息结构 返回参数
 */
@Getter
@ToString
public class CluesInformationResponse {

    //重复提交的数据
    public static final CluesInformationResponse repeatCommit;

    //成功提交的数据
    public static final CluesInformationResponse successCommit;

    //失败
    public static final CluesInformationResponse failedCommit;

    //非白名单
    public static final CluesInformationResponse whiteCommit;

    static {
        repeatCommit = new CluesInformationResponse(1, "申请编号重复");
        successCommit = new CluesInformationResponse(0, "");
        failedCommit = new CluesInformationResponse(1, "失败");
        whiteCommit = new CluesInformationResponse(1, "省市受限，无法受理");
    }
    //状态 0：提交成功。1:提交失败
    private final Integer status;
    //原因描述
    private final String reason;

    private CluesInformationResponse(Integer status, String reason) {
        this.status = status;
        this.reason = reason;
    }
}

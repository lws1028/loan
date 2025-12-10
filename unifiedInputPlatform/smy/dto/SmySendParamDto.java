package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.dto;

import lombok.Data;

/**
 * 萨摩耶回调借口发送参数
 *
 * @author : LXY
 * @date : 2023/6/5 16:51
 */
@Data
public class SmySendParamDto {

    /**
     * 客户Id唯⼀标识,推送接⼝3.2.2中传⼊的
     * custId
     */
    private String custId;

    /**
     * 用户注册手机号
     */
    private String phoneNo;

    /**
     * 状态说明300-线索已收集 306-电话跟进中 302-客户⽆意向 301-客户有意向 P010-终审通过 P054-终审拒绝 P053-合同已放款
     */
    private String status;

    /**
     * 放款金额(放款成功时必传)
     */
    private String loanAmount;

    /**
     * 回调时间(格式如：2023-03-30 14:37:06)
     */
    private String statusTime;
}

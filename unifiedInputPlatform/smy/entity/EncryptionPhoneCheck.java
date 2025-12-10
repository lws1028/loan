package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 加密手机号防撞表
 *
 * @author : LXY
 * @date : 2023/6/5 14:32
 */
@Data
@TableName(value = "ENCRYPTION_PHONE_CHECK")
@KeySequence(value = "ENCRYPTION_PHONE_CHECK_SEQ")
public class EncryptionPhoneCheck {
    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.INPUT)
    private Integer id;

    /**
     * 数据来源(SPL-线索、CREDIT-征信授权、LOAN-贷款)
     */
    private String dataSources;

    /**
     * 渠道来源
     */
    @TableField(exist = false)
    private String channelPartner;

    /**
     * 原始手机号
     */
    private String originalPhoneNumber;

    /**
     * MD5加密后号码
     */
    private String md5PhoneNumber;

    /**
     * SHA256加密后手机号
     */
    private String sha256PhoneNumber;

    /**
     * 原始手机号创建时间
     */
    private Date originalCreateTime;

    /**
     * 创建时间
     */
    private Date createTime;

}

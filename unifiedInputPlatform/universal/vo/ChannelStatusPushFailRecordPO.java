package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universal.vo;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author gongwei
 * @描述
 * @since 2020/4/23
 */
@Data
@TableName("CHANNEL_PUSH_FAIL_RECORD")
@KeySequence("CHANNEL_PUSH_FAIL_RECORD_SEQ")
public class ChannelStatusPushFailRecordPO {
    /**
     * 主键
     */
    @Id
    @TableId("ID")
    private Integer id;
    /**
     * 申请编号
     */
    private String applyNum;
    /**
     * 最大动作编号
     */
    @TableField("PUSH_ACTION_NUM")
    private String pushActionNum;

    /**
     * loanId
     */
    private Integer loanId;

    /**
     * branchLoanId
     */
    private Integer branchLoanId;

//    /**
//     * 拒绝原因
//     */
//    private String splRefuseReason;
    /**
     * 合作渠道：1-京东  2-上汽财务  3-58同城  4-萨摩耶 5-小米
     */
    private Integer channelPartner;

    /**
     * 默认为0，已成功不在补发 1成功/0失败
     */
    private Integer isSuccess;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

    public ChannelStatusPushFailRecordPO(String applyNum, String pushActionNum, Integer loanId, Integer branchLoanId, Integer channelPartner) {
        this.applyNum = applyNum;
        this.pushActionNum = pushActionNum;
        this.loanId = loanId;
        this.branchLoanId = branchLoanId;
        this.channelPartner = channelPartner;
        this.createTime = new Timestamp(new Date().getTime());
        this.updateTime = new Timestamp(new Date().getTime());
    }

    public ChannelStatusPushFailRecordPO() {
    }
}

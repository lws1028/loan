package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import com.apollo.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zlhj.unifiedInputPlatform.smy.entity.EncryptionPhoneCheck;
import lombok.Data;

import java.util.Date;

@Data
public class ClueFilterOutput {

    /**
     * 接收状态
     * 1：命中
     * 2：未命中
     */
    private String status;

    /**
     * 客户失效时间，
     * yyyy-MM-dd
     * HH:mm:ss
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date invalidDate;

    /**
     * 客户接收失败原因
     */
    private String refuseMsg;

    public static ClueFilterOutput getInstance(EncryptionPhoneCheck check) {

        ClueFilterOutput output = new ClueFilterOutput();
        if (check != null){
            Date invalidDate = DateUtil.nowPlusDays(check.getCreateTime(), 30);
            output.setStatus("1");
            output.setInvalidDate(invalidDate);
            output.setRefuseMsg("30天已有该客户");
        } else {
            Date invalidDate = DateUtil.nowPlusDays(new Date(), 30);
            output.setStatus("2");
            output.setInvalidDate(invalidDate);
        }
        return output;
    }
}

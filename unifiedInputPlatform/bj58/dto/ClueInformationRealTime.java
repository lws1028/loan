package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ClueInformationRealTime {

    private final Integer loanId;

    //58状态节点
    private final Integer status;

    private final String applyNum;

    //渠道
    private Integer channel;

    private Integer branchLoanId;

    private ClueInformationRealTime(Integer loanId, Integer status, String applyNum, Integer channel, Integer branchLoanId) {
        this.loanId = loanId;
        this.status = status;
        this.applyNum = applyNum;
        this.channel = channel;
        this.branchLoanId = branchLoanId;
    }

    public static ClueInformationRealTime createRealTime(Integer loanId, Integer status, String applyNum, Integer channel,Integer branchLoanId) {
        return new ClueInformationRealTime(loanId, status, applyNum, channel, branchLoanId);
    }

    public boolean channelIsBJ58() {
        return null != this.channel && 13 == this.channel;
    }

    public boolean channelIsAnt() {
        return null != this.channel && 16 == this.channel;
    }
    public void setChannel(Integer channel) {
        this.channel = channel;
    }
}

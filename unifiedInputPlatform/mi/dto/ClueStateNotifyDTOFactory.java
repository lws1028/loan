package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.dto;

import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.user.vo.MultipleLoanObject;

import java.math.BigDecimal;

public class ClueStateNotifyDTOFactory {
    //取消
    public ClueStateNotifyDTO cancel(DirectCustomerStatusDto directCustomerStatusDto, String applyNum){
        ClueStateNotifyDTO clueStateNotifiDTO = new ClueStateNotifyDTO();
        clueStateNotifiDTO.setApplyId(Long.valueOf(applyNum));
        clueStateNotifiDTO.setOpenId(directCustomerStatusDto.getOpenId());
        clueStateNotifiDTO.setChangeTime(System.currentTimeMillis());
        clueStateNotifiDTO.setClueType(1);
        clueStateNotifiDTO.setOrderStatus(2);
        clueStateNotifiDTO.setReason("客户取消进件");
        return clueStateNotifiDTO;
    }
    //取消
    public ClueStateNotifyDTO cancel(DirectCustomerStatusDto directCustomerStatusDto, SplBussinessBasicObject splBussinessBasicObject){
        ClueStateNotifyDTO clueStateNotifiDTO = new ClueStateNotifyDTO();
        clueStateNotifiDTO.setApplyId(Long.valueOf(splBussinessBasicObject.getApplyNum()));
        clueStateNotifiDTO.setOpenId(directCustomerStatusDto.getOpenId());
        clueStateNotifiDTO.setChangeTime(System.currentTimeMillis());
        clueStateNotifiDTO.setClueType(1);
        clueStateNotifiDTO.setOrderStatus(2);
        clueStateNotifiDTO.setReason("客户取消进件");
        return clueStateNotifiDTO;
    }

    //审批拒绝
    public ClueStateNotifyDTO approvalRejection(DirectCustomerStatusDto directCustomerStatusDto, SplBussinessBasicObject splBussinessBasicObject){
        ClueStateNotifyDTO clueStateNotifiDTO = new ClueStateNotifyDTO();
        clueStateNotifiDTO.setApplyId(Long.valueOf(splBussinessBasicObject.getApplyNum()));
        clueStateNotifiDTO.setOpenId(directCustomerStatusDto.getOpenId());
        clueStateNotifiDTO.setChangeTime(System.currentTimeMillis());
        clueStateNotifiDTO.setClueType(1);
        clueStateNotifiDTO.setOrderStatus(4);
        clueStateNotifiDTO.setReason("审批拒绝");
        return clueStateNotifiDTO;
    }

    /**
     *
     * @param directCustomerStatusDto
     * @param splBussinessBasicObject
     * @param loanInfo  汇总贷款
     * @param mainLoanInfo  主带款
     * @param money  车辆款+加绒费
     * @return
     */
    //审批通过
    public ClueStateNotifyDTO approve(DirectCustomerStatusDto directCustomerStatusDto,
                                      SplBussinessBasicObject splBussinessBasicObject,
                                      MultipleLoanObject loanInfo,
                                      MultipleLoanObject mainLoanInfo,
                                      String money){
        ClueStateNotifyDTO clueStateNotifiDTO = new ClueStateNotifyDTO();
        clueStateNotifiDTO.setApplyId(Long.valueOf(splBussinessBasicObject.getApplyNum()));
        clueStateNotifiDTO.setOpenId(directCustomerStatusDto.getOpenId());
        clueStateNotifiDTO.setChangeTime(System.currentTimeMillis());
        clueStateNotifiDTO.setClueType(1);
        clueStateNotifiDTO.setOrderStatus(3);
        clueStateNotifiDTO.setContractNumber(loanInfo.getM_contractNumber());

        BigDecimal orderAmount = new BigDecimal(money).multiply(BigDecimal.valueOf(100L));
        clueStateNotifiDTO.setOrderAmount(orderAmount.longValue());

        clueStateNotifiDTO.setTerm(mainLoanInfo.getM_term());

        long rate = BigDecimal.valueOf(mainLoanInfo.getM_commonRate()).multiply(new BigDecimal("1000000")).longValue();
        clueStateNotifiDTO.setRate(rate);

        clueStateNotifiDTO.setRateType(3);
        return clueStateNotifiDTO;
    }
}

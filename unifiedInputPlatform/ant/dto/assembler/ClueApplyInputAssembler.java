package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto.assembler;

import com.apollo.alds.util.ConvertionUtil;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.domain.cule.CuleApplyCommand;
import com.zlhj.commonLoan.util.StringUtil;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.externalChannel.vo.SplUserInfoObject;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.unifiedInputPlatform.ant.dto.ClueApplyInput;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClueApplyInputAssembler {

    public CuleApplyCommand toCuleApply(ClueApplyInput input) {
        return new CuleApplyCommand(
                this.toDirectCustomerStatus(input),
                this.toSplBussinessBasicObject(input),
                this.toSplUserInfoObject(input)
        );
    }

    private DirectCustomerStatusDto toDirectCustomerStatus(ClueApplyInput input) {

        Assert.notNull(input, "进件信息为空！");
        if (StringUtil.isEmpty(input.getApplyNo())) {
            throw new BusinessException("[星河侧唯一业务编号]为空！");
        }

        DirectCustomerStatusDto directCustomerStatus = new DirectCustomerStatusDto();
        directCustomerStatus.setSplApplyNum(input.getApplyNo());
        directCustomerStatus.setSplMaxActionNum(LoanStatusChangeEnum.FOLLOWING.getValue());
        directCustomerStatus.setSplChannelPartner(6);
        directCustomerStatus.setSplCreatetime(new Timestamp(System.currentTimeMillis()));
        return directCustomerStatus;
    }

    private SplBussinessBasicObject toSplBussinessBasicObject(ClueApplyInput input) {

        Assert.notNull(input, "进件信息为空！");

        if (StringUtil.isEmpty(input.getPayMethod())) {
            throw new BusinessException("[客户购车方式]为空！");
        }
        if (StringUtil.isEmpty(input.getCarLicense())) {
            throw new BusinessException("[客户车牌号]为空！");
        }
        if (StringUtil.isEmpty(input.getResidenceProvinceCode())) {
            throw new BusinessException("[常住省份国标码]为空！");
        }
        if (StringUtil.isEmpty(input.getResidenceCityCode())) {
            throw new BusinessException("[常住城市国标码]为空！");
        }

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        String remark3 = "";
        if ("1".equals(input.getPayMethod())) {
            remark3 = "客户购车方式：全款";
        } else if ("2".equals(input.getPayMethod())) {
            remark3 = "客户购车方式：分期-已结清";
        } else if ("3".equals(input.getPayMethod())) {
            remark3 = "客户购车方式：分期-未结清";
        }

        SplBussinessBasicObject splBussinessBasicObject = new SplBussinessBasicObject();
        splBussinessBasicObject.setRemark1(input.getCarLicense());
        splBussinessBasicObject.setRemark3(remark3);
        splBussinessBasicObject.setApplyNum(input.getApplyNo());
        splBussinessBasicObject.setProvince(input.getResidenceProvinceCode());
        splBussinessBasicObject.setCity(input.getResidenceCityCode());
        splBussinessBasicObject.setRemark4(input.getCarExt());
        splBussinessBasicObject.setRemark5(input.getUserExt());
        splBussinessBasicObject.setBussinessType(3);
        splBussinessBasicObject.setChannelPartner(16);
        splBussinessBasicObject.setCustomerModel(1);
        splBussinessBasicObject.setLoanState(0);
        splBussinessBasicObject.setLoanTerm(36);
        splBussinessBasicObject.setLoanPrice(1);
        splBussinessBasicObject.setIsMainClue("1");
        splBussinessBasicObject.setSubmitDateTi(new Timestamp(date.getTime()));
        splBussinessBasicObject.setSubmitDate(ConvertionUtil.getSimpleIntegerWithNull(sdf.format(date)));
        return splBussinessBasicObject;
    }

    private SplUserInfoObject toSplUserInfoObject(ClueApplyInput input) {

        Assert.notNull(input, "进件信息为空！");

        if (StringUtil.isEmpty(input.getMobile())) {
            throw new BusinessException("[客户手机号]为空！");
        }
        if (StringUtil.isEmpty(input.getIdNum())) {
            throw new BusinessException("[客户身份证号]为空！");
        }
        if (StringUtil.isEmpty(input.getName())) {
            throw new BusinessException("[客户姓名]为空！");
        }
        if (StringUtil.isEmpty(input.getResidenceProvinceCode())) {
            throw new BusinessException("[常住省份国标码]为空！");
        }
        if (StringUtil.isEmpty(input.getResidenceCityCode())) {
            throw new BusinessException("[常住城市国标码]为空！");
        }

        SplUserInfoObject splUserInfoObject = new SplUserInfoObject();
        splUserInfoObject.setPhone(input.getMobile());
        splUserInfoObject.setIdNum(input.getIdNum());
        splUserInfoObject.setIdType(1);
        splUserInfoObject.setCustomerName(input.getName());
        splUserInfoObject.setUprovince(Integer.valueOf(input.getResidenceProvinceCode()));
        splUserInfoObject.setUcity(Integer.valueOf(input.getResidenceCityCode()));
        return splUserInfoObject;
    }


}

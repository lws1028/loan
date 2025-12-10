package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.apollo.alds.util.DAOUtil;
import com.zlhj.commonLoan.util.StringUtil;
import com.zlhj.externalChannel.InterfaceDatum;
import com.zlhj.externalChannel.vo.InterfaceInfoObject;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.externalChannel.vo.SplUserInfoObject;
import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.infrastructure.message.publisher.ClueApplicationPublisher;
import com.zlhj.infrastructure.repository.SmyLoanCityConfigurationRepository;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.mapper.EncryptionPhoneCheckMapper;
import com.zlhj.mapper.SmyMapper;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.unifiedInputPlatform.LoanStatusInteraction;
import com.zlhj.unifiedInputPlatform.bj58.dto.CluesInformationResponse;
import com.zlhj.unifiedInputPlatform.smy.dto.CustomerCheckParam;
import com.zlhj.unifiedInputPlatform.smy.dto.InfoSubmitParam;
import com.zlhj.unifiedInputPlatform.smy.dto.OperationSendSmyStatusDto;
import com.zlhj.unifiedInputPlatform.smy.dto.SmyResponse;
import com.zlhj.unifiedInputPlatform.smy.entity.SmyLoanCityConfiguration;
import com.zlhj.unifiedInputPlatform.smy.service.SmyService;
import com.zlhj.unifiedInputPlatform.smy.transition.SmyTransition;
import com.zlhj.unifiedInputPlatform.smy.util.ParamCheckUtil;
import com.zlhj.util.ToolsUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * 萨摩耶大额导流 服务接口实现
 *
 * @author LXY
 * 2023年6月2日14:57:31
 */
@AllArgsConstructor
@Slf4j
@Service
public class SmyServiceImpl implements SmyService {

    private final SmyTransition smyTransition;
    private final ParamCheckUtil paramCheckUtil;
    private final SmyLoanCityConfigurationRepository smyLoanCityConfigurationRepository;
    private final InterfaceDatum interfaceDatum;
    private final SplBussinessbasicMapper splBussinessbasicMapper;
    private final LoanStatusInteraction loanStatusInteraction;
    private final EncryptionPhoneCheckMapper encryptionPhoneCheckMapper;
    private final ClueApplicationPublisher clueApplicationPublisher;

    /**
     * 用户准入接口
     *
     * @param customerCheckParam
     * @return
     */
    @Override
    public SmyResponse customerCheck(CustomerCheckParam customerCheckParam) {
        log.info("萨摩耶用户准入接口-助贷接收中转数据：customerCheckParam = [{}]", customerCheckParam);
        SmyResponse response = SmyResponse.builder()
                .code("000000")
                .build();
        try {
            if (customerCheckParam == null) {
                return SmyResponse.builder()
                        .code("000016")
                        .msg("参数为空")
                        .build();
            }
            this.clueApplicationPublisher.cluePhoneFiltered(customerCheckParam.getPhoneNo(), "14");
            //校验参数
            if (StringUtil.isEmpty(customerCheckParam.getPhoneNo())) {
                return SmyResponse.builder()
                        .code("000004")
                        .msg("{phoneNo}不可为空")
                        .build();
            }
            //撞库结果【是否命中】
            boolean hit = true;
            int count = encryptionPhoneCheckMapper.secrchEncryptionPhoneCheckInfoCount(customerCheckParam.getPhoneNo());
            //查询所有客户手机号
            if (count > 0) {
                hit = false;
            }
            if (hit) {
                response.setDealStatus("0000");
                response.setDealDesc("准入成功");
            } else {
                response.setDealStatus("9999");
                response.setDealDesc("准入拒绝");
            }
            log.info("萨摩耶用户准入接口查询结果：result = [{}]", response);
        } catch (Exception e) {
            log.error("萨摩耶用户准入接口异常，原因：e = [{}]", e.getMessage(), e);
            response = SmyResponse.builder()
                    .code("999999").msg("处理异常")
                    .build();
        }
        // 取消防撞，允许任何线索流入
        response.setDealStatus("0000");
        response.setDealDesc("准入成功");
        return response;
    }


    /**
     * 信息提交接口
     *
     * @param content
     * @return
     */
    @Override
    public SmyResponse infoSubmit(String content) {
        log.info("萨摩耶信息提交接口-助贷接收中转数据：infoSubmitParam = [{}]", content);
        Map<String, Object> resMap = new HashMap<>(16);
        int branchLoanId = 0;
        SmyResponse response = SmyResponse.builder()
                .code("000000")
                .dealDesc("提交成功")
                .dealStatus("0000")
                .build();

        JSONObject jsonObject = JSONObject.parseObject(content);
        JSONObject extraContent = JSONObject.parseObject(jsonObject.get("extraContent").toString());
        jsonObject.put("extraContent", extraContent);
        InfoSubmitParam infoSubmitParam = JSONObject.parseObject(jsonObject.toJSONString(), InfoSubmitParam.class);
        if (infoSubmitParam == null) {
            SmyResponse returnResponse = SmyResponse.builder()
                    .code("000016")
                    .msg("参数为空")
                    .build();
            this.saveInterfaceInfo(JSONObject.toJSONString(returnResponse), branchLoanId, infoSubmitParam, "萨摩耶信息提交接口");
            return returnResponse;
        }
        try {
            String errorMsg = checkParam(infoSubmitParam);
            //校验参数必填
            if (!"".equals(errorMsg)) {
                SmyResponse returnResponse = SmyResponse.builder()
                        .code("000004")
                        .msg(errorMsg)
                        .build();
                this.saveInterfaceInfo(JSONObject.toJSONString(returnResponse), branchLoanId, infoSubmitParam, "萨摩耶信息提交接口");
                return returnResponse;
            }
            DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(infoSubmitParam.getCustId(), 4);
            if (directCustomerStatusDto != null) {
                SmyResponse returnResponse = SmyResponse.builder()
                        .code("000017")
                        .msg("客户Id已存在")
                        .build();
                //保存接口信息表
                this.saveInterfaceInfo(JSONObject.toJSONString(returnResponse), branchLoanId, infoSubmitParam, "萨摩耶信息提交接口");
                //有重复
                log.info("萨摩耶提交进件信息接口：CustId订单编号重复，orderId：{}，返回结果：{}", infoSubmitParam.getCustId(), CluesInformationResponse.repeatCommit.toString());
                return returnResponse;
            }
            //省市白名单校验/转换
            SmyLoanCityConfiguration smyLoanCityConfiguration = smyLoanCityConfigurationRepository.getSmyLoanCityConfiguration(infoSubmitParam.getAddressCity());
            if (smyLoanCityConfiguration == null) {
                SmyResponse returnResponse = SmyResponse.builder()
                        .code("000017")
                        .msg("提交失败")
                        .build();
                this.saveInterfaceInfo(JSONObject.toJSONString(returnResponse), branchLoanId, infoSubmitParam, "萨摩耶信息提交接口");
                return returnResponse;
            }
            //参数转换、保存核心系统数据
            SplBussinessBasicObject splBussinessBasicObject = smyTransition.translateSplBussiness(infoSubmitParam, smyLoanCityConfiguration);
            DAOUtil.store(splBussinessBasicObject);
            branchLoanId = splBussinessBasicObject.getBranchLoanId();
            SplUserInfoObject splUserInfoObject = smyTransition.translateSplUserInfo(infoSubmitParam, smyLoanCityConfiguration);
            splUserInfoObject.setBranchLoanId(branchLoanId);
            DAOUtil.store(splUserInfoObject);
            //发送电销系统
            log.info("萨摩耶信息提交接口,同步记录至spl_bussinessbasic、spl_userinfojd成功，custId：{}, branchLoanID: {}", infoSubmitParam.getCustId(), branchLoanId);

            resMap.put("BRANCHID", branchLoanId);
            resMap.put("RESULT", 1);
            resMap.put("MESSAGE", "成功");

            //保存接口信息表
            this.saveInterfaceInfo(JSONObject.toJSONString(resMap), branchLoanId, infoSubmitParam, "萨摩耶信息提交接口");

            log.info("萨摩耶信息提交接口,同步电销操作开始，custId：{}", infoSubmitParam.getCustId());
            // 商机落库操作结束后，进行一次数据同步电销操作
            interfaceDatum.synchronizationBusinessOpportunity(resMap);
            log.info("萨摩耶信息提交接口,同步电销操作结束，custId：{}", infoSubmitParam.getCustId());
        } catch (Exception e) {
            log.error("萨摩耶信息提交接口异常，原因：e = [{}]", e.getMessage(), e);
            response = SmyResponse.builder()
                    .code("999999").msg("处理异常")
                    .build();
        }
        return response;
    }

    /**
     * 校验参数必填
     *
     * @param infoSubmitParam
     * @return
     */
    public String checkParam(InfoSubmitParam infoSubmitParam) {
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(infoSubmitParam));
        StringBuffer errorMsg = new StringBuffer();
        paramCheckUtil.ifNull(jsonObject, "custId", errorMsg);
        paramCheckUtil.ifNull(jsonObject, "custName", errorMsg);
        paramCheckUtil.ifNull(jsonObject, "gender", errorMsg);
        paramCheckUtil.ifNull(jsonObject, "phoneNo", errorMsg);
        paramCheckUtil.ifNull(jsonObject, "addressCity", errorMsg);
        paramCheckUtil.ifNull(jsonObject, "house", errorMsg);
        paramCheckUtil.ifNull(jsonObject, "car", errorMsg);
        paramCheckUtil.ifNull(jsonObject, "housingFund", errorMsg);
        paramCheckUtil.ifNull(jsonObject, "businessLicense", errorMsg);
        paramCheckUtil.ifNull(jsonObject, "socialSecurity", errorMsg);
        return errorMsg.toString();
    }

    /**
     * 保存接口信息表
     *
     * @param resMap
     * @param branchLoanID
     * @param infoSubmitParam
     * @param interfaceName
     * @return
     */
    private boolean saveInterfaceInfo(String resMap, int branchLoanID, InfoSubmitParam infoSubmitParam, String interfaceName) {
        log.info("接收萨摩耶线索信息提交的数据:" + infoSubmitParam);
        InterfaceInfoObject interfaceInfo = new InterfaceInfoObject(null, branchLoanID, 0, interfaceName,
                2, JSONObject.toJSONString(resMap == null ? infoSubmitParam : resMap, SerializerFeature.WriteMapNullValue),
                new Timestamp(System.currentTimeMillis()));
        return DAOUtil.store(interfaceInfo);
    }

    /**
     * 运维推送萨摩耶状态同步接口
     *
     * @param operationSendSmyStatusDto
     * @return
     */
    @Override
    public ResultDto operationSendSmyStatusInterface(OperationSendSmyStatusDto operationSendSmyStatusDto) throws Exception {
        ResultDto resultDto = new ResultDto(0, "发送失败");
        log.info("运维推送萨摩耶状态同步接口,业务层，入参：operationSendSmyStatusDto = [{}]", operationSendSmyStatusDto);
        resultDto = loanStatusInteraction.sendOperationSendSmyStatusInterface(operationSendSmyStatusDto);
        log.info("运维推送萨摩耶状态同步接口,业务层，返回：result = [{}]", resultDto.toString());
        return resultDto;
    }
}

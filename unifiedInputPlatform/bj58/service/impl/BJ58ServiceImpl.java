package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.apollo.alds.util.ConvertionUtil;
import com.apollo.alds.util.DAOUtil;
import com.apollo.sap.reason.ReasonInfo;
import com.apollo.util.DateUtil;
import com.mybatis.DbSqlSessionFactory;
import com.zlhj.Interface.util.AllSaveRecord;
import com.zlhj.approve.dao.ApproveMapper;
import com.zlhj.commonLoan.util.StringUtil;
import com.zlhj.entity.BusinessBasicEntity;
import com.zlhj.externalChannel.InterfaceDatum;
import com.zlhj.externalChannel.vo.InterfaceInfoObject;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.externalChannel.vo.SplUserInfoObject;
import com.zlhj.infrastructure.message.publisher.ClueApplicationPublisher;
import com.zlhj.interf.TokenInterface;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.loan.SendEmailMessage;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.tikTok.dto.ApplyPlusDto;
import com.zlhj.unifiedInputPlatform.LoanStatusInteraction;
import com.zlhj.unifiedInputPlatform.bj58.dto.*;
import com.zlhj.unifiedInputPlatform.bj58.service.BJ58Service;
import com.zlhj.unifiedInputPlatform.bj58.transform.BJ58Transform;
import com.zlhj.user.vo.BeCodeAssoRepository;
import com.zlhj.user.vo.BeCodeAssoVo;
import com.zlhj.user.vo.MultipleLoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 58同城 服务接口实现
 */
@Slf4j
@Service
public class BJ58ServiceImpl implements BJ58Service {

    private final static String SUCCESS = "SUCCESS";
    private final static String ERROR = "ERROR";

    private final SplBussinessbasicMapper splBussinessbasicMapper;

    private final BJ58Transform bj58Transform;

    private final InterfaceDatum interfaceDatum;

    private final MultipleLoanRepository loanInfoRepository;

    private final AllSaveRecord allSaveRecord;

    private final BeCodeAssoRepository beCodeAssoRepository;

    private final SendEmailMessage sendEmailMessage;

    private final ReasonInfo reasonInfoService;

    private final BJ58Validate validate;

    private final ClueApplicationPublisher clueApplicationPublisher;

    public BJ58ServiceImpl(SplBussinessbasicMapper splBussinessbasicMapper,
                           BJ58Transform bj58Transform,
                           InterfaceDatum interfaceDatum,
                           MultipleLoanRepository loanInfoRepository,
                           AllSaveRecord allSaveRecord,
                           BeCodeAssoRepository beCodeAssoRepository,
                           SendEmailMessage sendEmailMessage,
                           ReasonInfo reasonInfoService,
                           BJ58Validate validate,
                           ClueApplicationPublisher clueApplicationPublisher) {
        this.splBussinessbasicMapper = splBussinessbasicMapper;
        this.bj58Transform = bj58Transform;
        this.interfaceDatum = interfaceDatum;
        this.loanInfoRepository = loanInfoRepository;
        this.allSaveRecord = allSaveRecord;
        this.beCodeAssoRepository = beCodeAssoRepository;
        this.sendEmailMessage = sendEmailMessage;
        this.reasonInfoService = reasonInfoService;
        this.validate = validate;
	    this.clueApplicationPublisher = clueApplicationPublisher;
    }

    /**
     *  提交进件信息
     */
    @Override
    public CluesInformationResponse submit(CluesInformationRequest request) {
        this.clueApplicationPublisher.cluePhoneFiltered(request.getPhone(), "13");
        //判断orderId订单编号是否重复
        //唯一：视为接口成功，且提交成功
        //重复：视为接口成功，但提交失败。reason默认：申请编号重复
        DirectCustomerStatusDto dcsd = new DirectCustomerStatusDto();
        Map<String, Object> resMap = new HashMap<>();
        int branchLoanID = 0;
        try{
            if (!validate.whiteList(BJ58WhiteList.createWhiteList(request.getProvinceCode(), request.getCityCode()))) {
                log.info("58同城提交进件信息接口：数据非白名单数据，入参：{}", request.toString());
                this.saveInterfaceInfo(null, branchLoanID, request);
                return CluesInformationResponse.whiteCommit;
            }
            log.info("58同城提交进件信息接口：进入58同城提交进件信息接口，入参：{}", request.toString());
            DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(request.getOrderId() + "", bj58Transform.getChannelPartner58());
            if (directCustomerStatusDto != null) {
                //保存接口信息表
                this.saveInterfaceInfo(null, branchLoanID, request);
                //有重复
                log.info("58同城提交进件信息接口：orderId订单编号重复，orderId：{}，返回结果：{}", request.getOrderId(), CluesInformationResponse.repeatCommit.toString());
                return CluesInformationResponse.repeatCommit;
            }
            //唯一
            //数据解析存储至spl_direct_customer_status表中，作为主表数据
            dcsd = bj58Transform.cir2Dcsd(request);
            splBussinessbasicMapper.insert58CustomerStatusInfo(
                    dcsd
            );

            //同步记录至spl_bussinessbasic表
            SplBussinessBasicObject splBussinessBasicObject = bj58Transform.cir2sbbo(request);
            this.addBusinessBasic(splBussinessBasicObject);

            //同步记录至spl_userinfojd表
            this.addUserInfoJD(
                    bj58Transform.cir2Suio(request),
                    splBussinessBasicObject
            );

            branchLoanID = splBussinessBasicObject.getBranchLoanId();
            log.info("58同城提交进件信息接口,同步记录至spl_bussinessbasic、spl_userinfojd成功，orderId：{}, branchLoanID: {}", request.getOrderId(),branchLoanID);

            resMap.put("BRANCHID", branchLoanID);
            resMap.put("RESULT", 1);
            resMap.put("MESSAGE", "成功");
        }catch (Exception e) {
            resMap.put("BRANCHID", branchLoanID);
            resMap.put("RESULT", 0);
            resMap.put("MESSAGE", "失败");
            log.error("提交进件信息接口,异常 {}", e.getMessage(), e);
        }

        //保存接口信息表
        this.saveInterfaceInfo(resMap, branchLoanID, request);

        log.info("58同城提交进件信息接口,同步电销操作开始，orderId：{}", request.getOrderId());
        // 商机落库操作结束后，进行一次数据同步电销操作
        interfaceDatum.synchronizationBusinessOpportunity(resMap);
        log.info("58同城提交进件信息接口,同步电销操作结束，orderId：{}", request.getOrderId());

        if ("1".equals(resMap.get("RESULT")+"")) {
            int maxActionNum = Integer.parseInt(dcsd.getSplMaxActionNum());
            Timestamp updateTime = dcsd.getSplUpdateTime();
            int branch = branchLoanID;
            log.info("58同城提交进件信息接口,发起异步调用58线索回调，orderId：{}, branchLoanID: {}", request.getOrderId(), branch);
            //再回调58线索状态回调通知接口
            Thread thread = new Thread(() -> {
                CluesInformationOtherInfo otherInfo =
                        this.getOtherInfo(maxActionNum, 0);
                CluesInformationNotify notify = CluesInformationNotify.createNotify(request.getOrderId(), maxActionNum,
                        updateTime, "", otherInfo.getProductName(), otherInfo.getAmount(),
                        otherInfo.getTerm(), otherInfo.getRate(), otherInfo.getLoanTime(), "", "");
                CluesInformationNotifySend send =
                        CluesInformationNotifySend.createSend(request.getOrderId(), null, notify, null, null, branch, null);
                this.callbackNotify(send);
            });
            thread.start();
            log.info("58同城提交进件信息接口,结束，orderId：{}, branchLoanID: {}，返回结果:{}", request.getOrderId(), branchLoanID,CluesInformationResponse.successCommit.toString());
            return CluesInformationResponse.successCommit;
        }
        log.info("58同城提交进件信息接口,结束，orderId：{}, branchLoanID: {}，返回结果:{}", request.getOrderId(), branchLoanID,CluesInformationResponse.successCommit.toString());
        return CluesInformationResponse.failedCommit;
    }

    @Override
    public CluesInformationSearchResponse query(CluesInformationSearch search) throws Exception {
        log.info("58同城线索状态查询接口：进入58同城线索状态查询接口，入参orderId：{}", search.getOrderId());
        //status线索状态：不查贷款，直接返回当前线索编号的最大动作编号。(必填)
        //changeTime	状态变更时间	：spl_direct_customer_status表更新时间。(必填)
        DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(search.getOrderId() + "", bj58Transform.getChannelPartner58());
        if (directCustomerStatusDto == null) {
            throw new Exception(String.format("58同城线索状态查询接口：未查询到最大动作编号, 订单id：[%s]", search.getOrderId()));
        }
        if (directCustomerStatusDto.getSplMaxActionNum() == null || directCustomerStatusDto.getSplUpdateTime() == null) {
            throw new Exception(String.format("58同城线索状态查询接口：线索状态或者状态更新时间为空, 订单id[%s]", search.getOrderId()));
        }

        CluesInformationSearchResponse.Builder response = new CluesInformationSearchResponse.Builder();
        int status = Integer.parseInt(directCustomerStatusDto.getSplMaxActionNum());
        response.status(status)
                .changeTime(directCustomerStatusDto.getSplUpdateTime().getTime());

        //remark	状态说明	：拒绝原因，status=11、31、41、51时提供。否则默认空
        if (BJ58Node.getSuccessNode(status) == null) {
            response.remark(directCustomerStatusDto.getSplRefuseReason());
        }

        //status=40、50、60 查询loaninfo的信息
        if (status == 40 || status == 50 || status == 60) {
            ApplyPlusDto applyPlusDto = loanInfoRepository.getApplyPlusLoanInfo(directCustomerStatusDto.getSplLoanId());

            if (applyPlusDto == null) {
                throw new Exception(String.format("58同城线索状态查询接口：查询loaninfo数据为空, 订单id[%s]， 当前贷款状态status：[%s]", search.getOrderId(), status));
            }
            //productName	贷款产品名称：Loaninfo表的产品名称。status=40、50、60时提供。否则默认空
            response.productName(applyPlusDto.getProductName());
            if (status == 60) {
                //amount	放款金额：Loaninfo表的贷款金额-GPS加融。status=60时提供。否则默认空
                //term	借款期数：Loaninfo表的贷款期数。status=60时提供。否则默认空
                //rate	利率：Loaninfo表的通用利率，单位：%。status=60时提供。否则默认空
                //loanTime	放款时间：放款日期（需要租赁放款的业务，租赁放款日期；不需要租赁放款的业务（含银行贷款业务、消费贷业务），银行放款日期）。格式：时间戳，时分秒默认000000。status=60时提供。否则默认空
                response.amount(applyPlusDto.getApplymoney() == null ? "" : Double.valueOf(applyPlusDto.getApplymoney()).intValue()+"")
                        .term(applyPlusDto.getLoanTerm())
                        .rate(applyPlusDto.getCommonRate())
                        .loanTime(applyPlusDto.getLendingDate() == null ? "" :
                                DateUtil.changeToTimeStamp(applyPlusDto.getLendingDate() + " 00:00:00").getTime()+"");
            }
        }

        return response.build();
    }

    @Override
    public CluesInformationResult callbackNotify(CluesInformationNotifySend send) {
        String statusInterface = "";
        String param = JSON.toJSONString(send.getNotify(), SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullListAsEmpty);
        //服务id
        String serviceId = "BJ58_CLUE_STATUS_NOTICE";
        //服务名
        String serviceName = "APPLY_NOTICE_SERVICE";
        //接口类型
        String interfaceName = "58线索状态回调接口";
        try {
            //报文拼接
            String res = TokenInterface.spellMessageBody(param, serviceName, serviceId);
            log.info("发送58线索状态回调接口,推送中转贷款状态报文：" + res);
            Thread.sleep(1000);
            //发送报文
            String receiveResult = LoanStatusInteraction.send(res);
            log.debug("发送58线索状态回调接口,推送结束,返回报文：" + receiveResult);
            JSONObject jsonObject = JSONObject.parseObject(receiveResult);
            statusInterface = jsonObject.getString("STATUS");
            String msgInterface = jsonObject.getString("MSG");
            log.info("发送上58线索状态回调接口, 存履历信息");
            if (send.getLoanId() == null) {
                allSaveRecord.saveRecordBeforeAccepting("Y".equals(statusInterface)?1:0, send.getBranchLoanId() == null ? 0 : send.getBranchLoanId(), interfaceName, "发送", param, receiveResult);

            }else {
                allSaveRecord.saveRecord(statusInterface, send.getLoanId() == null ? 0 : send.getLoanId(), interfaceName, "发送", param, receiveResult);
            }
            if ("Y".equals(statusInterface)) {
                CluesInformationNotifyResponse response = JSONObject.parseObject(msgInterface, CluesInformationNotifyResponse.class);
                if (response.success()) {
                    log.info("发送上58线索状态回调接口, 成功");
                    return CluesInformationResult.success;
                }else {
                    return CluesInformationResult.createCluesInformationError(response.getMsg());
                }
            }
            log.info("发送上58线索状态回调接口, 失败");
            return CluesInformationResult.createCluesInformationError(receiveResult);
        } catch (Exception e) {
            log.error("发送上58线索状态回调接口异常信息：[{}]", e.getMessage());
            if (send.getLoanId() == null) {
                allSaveRecord.saveRecordBeforeAccepting("Y".equals(statusInterface)?1:0, send.getBranchLoanId() == null ? 0 : send.getBranchLoanId(), interfaceName, "发送", param, e.getMessage());
            }else {
                allSaveRecord.saveRecord(statusInterface, send.getLoanId() == null ? 0 : send.getLoanId(), interfaceName, "发送", param, e.getMessage());
            }
            log.info("发送上58线索状态回调接口, 失败: {}", e.getMessage());
            return CluesInformationResult.createCluesInformationError(e.getMessage());
        }
    }

    /**
     * 58同城 30天超期定时任务
     */
    @Override
    public void bj58OverdueScheduleTask() throws Exception {
        //筛选线索表spl_direct_customer_status中58同城的线索的“最大动作编号”，
        // 不为11、31、41、50、51、60的，创建时间超期30天的线索对应的贷款数据
        // （若有多笔贷款取贷款状态最大的该笔）。
        log.info("58同城 30天超期定时任务开始");
        List<DirectCustomerStatusDto> directCustomerStatusDtos = splBussinessbasicMapper.selectStatusByChannelPartner(DateUtil.getNowBeforeDate(30), bj58Transform.getChannelPartner58());
        List<DirectCustomerStatusDto> statusDtos = directCustomerStatusDtos.stream()
                .filter(d -> this.notIn(d.getSplMaxActionNum(), "11", "31", "41", "50", "51", "60"))
                .collect(Collectors.toList());
        if (statusDtos.size() == 0) {
            return;
        }
        //去重 取最大动作编号的数据
       // Map<String, DirectCustomerStatusDto> directCustomerStatusDtoMap = this.distinctDirectCustomerStatus(statusDtos);
        //处理发送数据
        Map<String, List<CluesInformationNotifySend>> map = this.handleSendData(statusDtos);

        //回调发送
        List<CluesInformationNotifySend> errorSends = this.send58Notify(map.get(SUCCESS));

        //失败的发邮件数据
        errorSends.addAll(map.get(ERROR));
        this.sendEmail(errorSends);
    }

    /**
     * @return 1-成功 0-失败
     */
    @Override
    public int bj58RealTimeInteraction(ClueInformationRealTime realTime) {
        String applyNum;
        Integer branchLoanId;
        if (StringUtil.isEmpty(realTime.getApplyNum())) {
            SplBussinessBasicObject splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(realTime.getLoanId());
            if (splBussinessBasicObject == null) {
                log.info("58同城实时交互触发接口，没有查询到进件贷款基础信息表信息, 参数：{}", realTime.toString());
                return 0;
            }
            applyNum = splBussinessBasicObject.getApplyNum();
            branchLoanId = splBussinessBasicObject.getBranchLoanId();
            if (realTime.getChannel() == null) {
                realTime.setChannel(splBussinessBasicObject.getChannelSource());
            }
        }else {
            applyNum = realTime.getApplyNum();
            branchLoanId = realTime.getBranchLoanId();
        }

        if (!realTime.channelIsBJ58()) {
            return 0;
        }
        log.info("进入58同城实时交互触发接口, 参数： {}" , realTime.toString());
        //当前传的状态必须大于数据库状态，且只能大一级
        DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(applyNum, bj58Transform.getChannelPartner58());
        Node successNode = BJ58Node.getSuccessNode(Integer.parseInt(directCustomerStatusDto.getSplMaxActionNum()));
        if (successNode != null) {
            log.info("58同城实时交互触发接口, 当前节点： {}，目标更改节点" , successNode.getStatus(), realTime.getStatus());
            if (11 == realTime.getStatus() || (successNode.getSuccess() != null
                    && successNode.getStatus() < realTime.getStatus())) {
                //执行回调
                String remark = "";
                if (11 == realTime.getStatus()) {
                    remark = "个人资质不符";
                }
                CluesInformationOtherInfo otherInfo = this.getOtherInfo(realTime.getStatus(), realTime.getLoanId());

                CluesInformationNotify notify = CluesInformationNotify.createNotify(Long.parseLong(directCustomerStatusDto.getSplApplyNum()), realTime.getStatus(),
                        new Timestamp(System.currentTimeMillis()), remark, otherInfo.getProductName(), otherInfo.getAmount(),
                        otherInfo.getTerm(), otherInfo.getRate(), otherInfo.getLoanTime(), "", "");
                CluesInformationNotifySend send =
                        CluesInformationNotifySend.createSend(Long.parseLong(directCustomerStatusDto.getSplApplyNum()), realTime.getLoanId(), notify, null, null, branchLoanId, null);
                //58回调
                this.asyncBJ58CallBack(send);
                //更新线索
                splBussinessbasicMapper.updateDirectCustomerStatus(notify.getRemark(), notify.getStatus(), notify.getChangeTime(), notify.getOrderId().toString(), send.getLoanId());
                return 1;
            }
        }
        log.info("58同城实时交互触发接口,未更改线索，参数：{}", realTime.toString());
        return 0;
    }

    @Override
    public CluesInformationOperationResponse operationsBJ58(CluesInformationOperationRequest request) {
        CluesInformationOperationResponse res = new CluesInformationOperationResponse(request.getOrderId().length);
        if (request.getOrderId().length == 0) {
            return res;
        }
        List<DirectCustomerStatusDto> dtoList = new ArrayList<>();
        for (String orderId : request.getOrderId()) {
            if (!StringUtils.isNumeric(orderId)) {
                res.setOrder(orderId, "申请编号不存在");
                continue;
            }
            DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(orderId, bj58Transform.getChannelPartner58());
            if (directCustomerStatusDto != null) {
                dtoList.add(directCustomerStatusDto);
            }else {
                res.setOrder(orderId, "申请编号不存在");
            }
        }
        //处理发送数据
        Map<String, List<CluesInformationNotifySend>> map = this.handleOperationsSendData(dtoList);

        //回调发送(运维无论如何都要发送，但是更新spl_bussinessbasic，走30天超期逻辑)
        List<CluesInformationNotifySend> errorSends = this.sendOperationsNotify(map.get(SUCCESS));
        List<CluesInformationNotifySend> cluesInformationNotifySends = map.get(ERROR);
        for (CluesInformationNotifySend cluesInformationNotifySend : cluesInformationNotifySends) {
            res.setOrder(cluesInformationNotifySend.getOrderId()+"", cluesInformationNotifySend.getErrorMsg());
        }
        for (CluesInformationNotifySend errorSend : errorSends) {
            res.setOrder(errorSend.getOrderId()+"", errorSend.getErrorMsg());

        }
        return res;
    }


    private boolean addBusinessBasic(SplBussinessBasicObject entity) throws Exception {
        try {
            boolean store = DAOUtil.store(entity);
            log.info("同步记录至spl_bussinessbasic，结果： [{}]", store);
            return store;
        } catch (Exception e) {
            log.error("同步记录至spl_bussinessbasic异常，异常内容： [{}]", e.getMessage());
            throw new Exception(e.getCause());
        }
    }

    private boolean addUserInfoJD(SplUserInfoObject object, SplBussinessBasicObject businessBasicEntity) {
        //spl_userinfojd的id是spl_bussinessbasic的id
        object.setBranchLoanId(businessBasicEntity.getBranchLoanId());
        boolean store = DAOUtil.store(object);
        log.info("同步记录至spl_userinfojd，结果： [{}]", store);
        return store;
    }

    //保存接口信息表
    private boolean saveInterfaceInfo(Map<String, Object> resMap, int branchLoanID, CluesInformationRequest request) {
        log.info("接收58同城线索信息提交的数据:" + request);
        InterfaceInfoObject interfaceInfo = new InterfaceInfoObject(null, branchLoanID, 0, "58同城线索信息提交接口",
                2, JSONObject.toJSONString(resMap == null ? request : resMap, SerializerFeature.WriteMapNullValue), new Timestamp
                (System.currentTimeMillis()));
        return DAOUtil.store(interfaceInfo);
    }

    private boolean notIn(String param, String ... vars) {
        for (String var : vars) {
            if (var.equals(param)) {
                return false;
            }
        }
        return true;
    }

    private Map<String, List<CluesInformationNotifySend>> handleSendData(List<DirectCustomerStatusDto> directCustomerStatusDtos) {
        List<CluesInformationNotifySend> successList = new ArrayList<>();
        List<CluesInformationNotifySend> errorList = new ArrayList<>();
        Timestamp updateTimeLong = new Timestamp(System.currentTimeMillis());
        String remark;
        Integer loanState;
        Integer branchLoanId;
        Integer status;
        Integer loanId;
        for (DirectCustomerStatusDto value : directCustomerStatusDtos) {
            CluesInformationOtherInfo otherInfo = CluesInformationOtherInfo.createNull();
            remark = "";
            status = null;
            loanId = null;
            List<Map<String, Object>> maxLoanStatuses = splBussinessbasicMapper.getMaxLoanStatus(value.getSplApplyNum(), bj58Transform.getBusinessChannelPartner58());
            if (maxLoanStatuses != null && maxLoanStatuses.size() > 0) {
                Map<String, Object> maxLoanStatus = maxLoanStatuses.get(0);
                status = ConvertionUtil.getSimpleIntegerWithNull(maxLoanStatus.get("status"));
                loanId = ConvertionUtil.getSimpleIntegerWithNull(maxLoanStatus.get("loanId"));

                //节点>=245的时候，特殊，查60最大编号的数据
                if (status >= 245) {
                    //获取数据
                    otherInfo = getOtherInfo(60, value.getSplLoanId());
                }else {
                    //获取数据
                    otherInfo = getOtherInfo(Integer.valueOf(value.getSplMaxActionNum()),
                            value.getSplLoanId());
                }
                SplBussinessBasicObject splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId);
                loanState = splBussinessBasicObject.getLoanState();
                branchLoanId = splBussinessBasicObject.getBranchLoanId();
                remark = getRemark(loanId, status);
            }else {
                List<BusinessBasicEntity> businessMaxLoanStatusByApplyNum = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(value.getSplApplyNum(), bj58Transform.getBusinessChannelPartner58());
                if (businessMaxLoanStatusByApplyNum.size() == 0) {
                    CluesInformationNotifySend errorSend = CluesInformationNotifySend.createSend(Long.parseLong(value.getSplApplyNum()), null, null, null,null,null, String.format("订单id：{%s}， 进件贷款基础信息数据查询失败", value.getSplApplyNum()));
                    errorList.add(errorSend);
                    continue;
                }else {
                    loanState = businessMaxLoanStatusByApplyNum.get(0).getLoanState();
                    branchLoanId = businessMaxLoanStatusByApplyNum.get(0).getBranchLoanId();
                }
            }

            CluesInformationNotify notify = CluesInformationNotify.createNotify(Long.parseLong(value.getSplApplyNum()), Integer.valueOf(value.getSplMaxActionNum()), updateTimeLong,
                    remark, otherInfo.getProductName(), otherInfo.getAmount(), otherInfo.getTerm(),
                    otherInfo.getRate(), otherInfo.getLoanTime(), "", "");
            CluesInformationNotifySend successSend = CluesInformationNotifySend.createSend(Long.parseLong(value.getSplApplyNum()), loanId,
                    notify, status,loanState,branchLoanId, "");
            successList.add(successSend);
            }
        Map<String, List<CluesInformationNotifySend>> map = new HashMap<>();
        map.put(SUCCESS, successList);
        map.put(ERROR, errorList);
        return map;
    }


    /**
     * 获取拒绝原因
     */
    private String getRemark(Integer loanId, Integer status) {
        if (loanId == null || status == null) {
            return "";
        }
        //若为我司审批拒绝（初审拒绝、终审拒绝、面签拒绝、放款申请拒绝、放款审批拒绝），取：拒绝原因下拉框对应的中文。
        String approveLevel = null;
        String approvelType;
        if (status == -110 || status == -130 || status == -210 || status == -230 || status == -235 || status == -200) {
            if (status == -110) {
                approveLevel = "1";
                approvelType = "1";
            }else if (status == -130) {
                approveLevel = "9";
                approvelType = "1";
            }else if (status == -210) {
                approvelType = "4";
            }else if (status == -230) {
                approvelType = "2";
            }else if (status == -200) {
                approvelType = "1";
                approveLevel = "12";
            }else {
                approvelType = "3";
            }
            SqlSessionFactory sqlSessionFactory = DbSqlSessionFactory.getSqlSessionFactory();
            try(SqlSession sqlSession = sqlSessionFactory.openSession(true)){
                ApproveMapper mapper = sqlSession.getMapper(ApproveMapper.class);
                ClueInformationRefuseReason clueInformationRefuseReason = mapper.getApproveRefuseReasonByLevelAndType(loanId, approveLevel, approvelType);
                if (clueInformationRefuseReason == null) {
                    return "";
                }
                String refuseReason = clueInformationRefuseReason.getReason(status);
                if (StringUtils.isEmpty(refuseReason)) {
                    return "";
                }

                if(status == -110 || status == -130 || status == -200) {
                    List<String[]> refuseReasonList = reasonInfoService.getRufuseReason(1, 1, 0, 0);
                    Optional<String[]> refuseReasonOptional = refuseReasonList.stream().filter(r -> r[0].equals(refuseReason)).findFirst();
                    return refuseReasonOptional.isPresent() ? refuseReasonOptional.get()[1] : "";
                }
                BeCodeAssoVo beCodeAssoVo = beCodeAssoRepository.selectBeCodeName("SPYY",refuseReason);
                return beCodeAssoVo != null ? beCodeAssoVo.getCodeName() : "";
            }

        }
        return "";
    }


    /**
     *
     * @param sends
     * @return
     */
    private List<CluesInformationNotifySend> send58Notify(List<CluesInformationNotifySend> sends) {
        List<CluesInformationNotifySend> errorSend = new ArrayList<>();
        //若贷款状态为终态（≥245），按60-合同已放款状态拼装接口数据
        //若贷款状态为终态（＜0，或业务受理拒绝、业务受理超期），按当前贷款状态的数据拼装参数
        //若还无贷款状态（待业务申请），做超期取消处理（更新spl_bussinessbasic 表loan_state为9-系统取消）
        for (CluesInformationNotifySend send : sends) {
            CluesInformationNotify notify = send.getNotify();
            if (send.getStatus() == null) {
                if (send.getLoanState() == 2 || send.getLoanState() == 9 || send.getLoanState() == 3) {
                    Node failed = BJ58Node.getSuccessNode(notify.getStatus()).getFailed();
                    notify.setStatus(failed.getStatus());
                    if (StringUtils.isEmpty(notify.getRemark())) {
                        notify.setRemark(failed.getDefaultErrorMsg());
                    }
                    //更新线索
                    splBussinessbasicMapper.updateDirectCustomerStatus(notify.getRemark(), notify.getStatus(), notify.getChangeTime(), notify.getOrderId().toString(), send.getLoanId());
                    if (send.getLoanState() == 3) {
                        //更新spl_bussinessbasic 表loan_state为9-系统取消
                        splBussinessbasicMapper.updateBusinessBasicLoanStateByOrderId(send.getOrderId().toString(), 9);
                    }

                    //调用回调
                    //this.asyncBJ58CallBack(send);
                    CluesInformationResult cluesInformationResult = this.callbackNotify(send);
                    if (!cluesInformationResult.success()) {
                        send.setErrorMsg(cluesInformationResult.getMsg());
                        errorSend.add(send);
                    }
                }
            }else {
                if (send.getStatus() >= 245) {
                    notify.setStatus(60);
                }else if (send.getStatus() < 0 || send.getLoanState() == 3) {
                    Node failed = BJ58Node.getSuccessNode(notify.getStatus()).getFailed();
                    notify.setStatus(failed.getStatus());
                    if (StringUtils.isEmpty(notify.getRemark())) {
                        notify.setRemark(failed.getDefaultErrorMsg());
                    }
                    if (send.getLoanState() == 3) {
                        //更新spl_bussinessbasic 表loan_state为9-系统取消
                        splBussinessbasicMapper.updateBusinessBasicLoanStateByOrderId(send.getOrderId().toString(), 9);
                    }
                }
                if (send.getStatus() >= 245 || send.getStatus() < 0 || send.getLoanState() == 3) {
                    //更新线索
                    splBussinessbasicMapper.updateDirectCustomerStatus(notify.getRemark(), notify.getStatus(), notify.getChangeTime(), notify.getOrderId().toString(), send.getLoanId());
                    //调用回调
//                    this.asyncBJ58CallBack(send);
                    CluesInformationResult cluesInformationResult = this.callbackNotify(send);
                    if (!cluesInformationResult.success()) {
                        send.setErrorMsg(cluesInformationResult.getMsg());
                        errorSend.add(send);
                    }
                }
            }
        }
        return errorSend;


}
    private void sendEmail(List<CluesInformationNotifySend> errorSend) {
        if (errorSend == null || errorSend.size() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        String newLine = "\n";
        for (CluesInformationNotifySend send : errorSend) {
            sb.append("申请编号：")
                    .append(send.getOrderId())
                    .append("，")
                    .append("错误信息：")
                    .append(send.getErrorMsg())
                    .append(newLine);
        }
        //失败发送邮件
        String emailContent = "各位好：" + newLine +
                "以下贷款发送58同城接口失败，请及时处理：" + newLine +
                sb.toString();

        sendEmailMessage.sendEmail("58同城接口接口发送失败", emailContent, "58同城接口接口发送失败邮件推送");
    }

    private CluesInformationOtherInfo getOtherInfo(Integer maxActionNum, Integer loanId) {
        CluesInformationOtherInfo.Builder builder = new CluesInformationOtherInfo.Builder();
        if (maxActionNum == 40 || maxActionNum == 50 || maxActionNum == 60) {
            ApplyPlusDto applyPlusDto = loanInfoRepository.getApplyPlusLoanInfo(loanId);
            if (applyPlusDto == null) {
                log.info(String.format("58同城接口：查询loaninfo数据为空, loanId[%s]", loanId));
            }else {
                builder.productName(applyPlusDto.getProductName());
                if (maxActionNum == 60) {
                    builder.amount(applyPlusDto.getApplymoney() == null ? null : Double.valueOf(applyPlusDto.getApplymoney()).intValue())
                            .term(applyPlusDto.getLoanTerm() == null ? null : Integer.parseInt(applyPlusDto.getLoanTerm()))
                            .rate(applyPlusDto.getCommonRate())
                            .loanTime((applyPlusDto.getLendingDate() == null || "0".equals(applyPlusDto.getLendingDate())) ? null :
                                    DateUtil.changeToTimeStamp(applyPlusDto.getLendingDate() + " 00:00:00").getTime());
                }
            }
        }
        return builder.build();
    }

    //58同城线索状态回调通知接口,不管他们的返回内容成功失败，继续走流程
    private void asyncBJ58CallBack(CluesInformationNotifySend send) {
        Runnable task = () -> {
            CluesInformationResult cluesInformationResult = callbackNotify(send);
            log.info("58同城线索状态回调通知接口，返回内容：{}", cluesInformationResult.toString());
        };
        Thread task58 = new Thread(task, "task58");
        task58.start();
    }

    //运维拼接数据
    private Map<String, List<CluesInformationNotifySend>> handleOperationsSendData(List<DirectCustomerStatusDto> directCustomerStatusDtos) {
        //直接用loanid取数据
        List<CluesInformationNotifySend> successList = new ArrayList<>();
        List<CluesInformationNotifySend> errorList = new ArrayList<>();
        Timestamp updateTimeLong = new Timestamp(System.currentTimeMillis());

        for (DirectCustomerStatusDto value : directCustomerStatusDtos) {
            Integer branchLoanId = null;
            //获取数据
            CluesInformationOtherInfo otherInfo = getOtherInfo(Integer.valueOf(value.getSplMaxActionNum()), value.getSplLoanId());

            if (Integer.valueOf(value.getSplMaxActionNum()) < 20) {
                //获取branchLoanId
                List<BusinessBasicEntity> businessMaxLoanStatusByApplyNum = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(value.getSplApplyNum(), bj58Transform.getBusinessChannelPartner58());
                if (businessMaxLoanStatusByApplyNum.size() == 0) {
                    CluesInformationNotifySend errorSend = CluesInformationNotifySend.createSend(Long.parseLong(value.getSplApplyNum()), null, null, null,null,null, String.format("订单id：{%s}， 进件贷款基础信息数据查询失败", value.getSplApplyNum()));
                    errorList.add(errorSend);
                    continue;
                }else {
                    branchLoanId = businessMaxLoanStatusByApplyNum.get(0).getBranchLoanId();
                }
            }
            CluesInformationNotify notify = CluesInformationNotify.createNotify(Long.parseLong(value.getSplApplyNum()), Integer.valueOf(value.getSplMaxActionNum()),
                    updateTimeLong, value.getSplRefuseReason(), otherInfo.getProductName(), otherInfo.getAmount(), otherInfo.getTerm(),
                    otherInfo.getRate(), otherInfo.getLoanTime(), "", "");
            CluesInformationNotifySend successSend = CluesInformationNotifySend.createSend(Long.parseLong(value.getSplApplyNum()), value.getSplLoanId(),
                    notify, null,null,branchLoanId, "");
            successList.add(successSend);
        }
        Map<String, List<CluesInformationNotifySend>> map = new HashMap<>();
        map.put(SUCCESS, successList);
        map.put(ERROR, errorList);
        return map;
    }

    private List<CluesInformationNotifySend> sendOperationsNotify(List<CluesInformationNotifySend> sends) {
        List<CluesInformationNotifySend> errorSend = new ArrayList<>();
        for (CluesInformationNotifySend send : sends) {
            CluesInformationResult cluesInformationResult = this.callbackNotify(send);
            if (!cluesInformationResult.success()) {
                send.setErrorMsg(cluesInformationResult.getMsg());
                errorSend.add(send);
            }
        }
        return errorSend;
    }
}

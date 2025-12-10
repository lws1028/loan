package com.zlhj.unifiedInputPlatform.unifiedInputPlatform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.apollo.alds.util.ConvertionUtil;
import com.apollo.alds.util.DAOUtil;
import com.apollo.pazl.interf.util.Setting;
import com.apollo.sap.reason.ReasonInfo;
import com.apollo.util.DateUtil;
import com.apollo.util.JDBCUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zlhj.Interface.util.AllSaveRecord;
import com.zlhj.Interface.vo.CommonInterfaceMessageObject;
import com.zlhj.Interface.vo.CommonInterfaceMessageRepository;
import com.zlhj.application.HBCreditApprovalService;
import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.business.clue.translate.LoanStatusRealTimePushTranslate;
import com.zlhj.commonLoan.business.gtzl.service.GtzlCloseOrderService;
import com.zlhj.commonLoan.business.jjyh.service.JYXFSednInterfaceService;
import com.zlhj.commonLoan.business.jjyh.service.LoanAppStatusService;
import com.zlhj.commonLoan.business.jjyh.service.LoanApprovalServce;
import com.zlhj.commonLoan.business.ktyh.service.KTCancelApplyService;
import com.zlhj.commonLoan.business.lgzl.pojo.ApplyCancelInput;
import com.zlhj.commonLoan.business.lgzl.service.LGZLApplyCancelService;
import com.zlhj.commonLoan.business.syfl.application.SYFLManagerCancelApplyService;
import com.zlhj.commonLoan.business.zjk.pojo.CreditApprovalParamDto;
import com.zlhj.commonLoan.business.zjk.service.ZjkCreditApprovalService;
import com.zlhj.commonLoan.business.zsh.service.SinopecService;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.commonLoan.interfaces.bxyh.service.BXYHSendInterfaceService;
import com.zlhj.commonLoan.interfaces.ksyh.service.KSYHSendInterfaceService;
import com.zlhj.commonLoan.util.SHA256Encryptor;
import com.zlhj.commonLoan.util.StringUtil;
import com.zlhj.entity.BusinessBasicEntity;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.externalChannel.vo.SplUserInfoObject;
import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.hrxj.business.service.HrxjCreditMessageService;
import com.zlhj.hrxj.interfaces.dto.CarInfoDto;
import com.zlhj.hrxj.interfaces.dto.CarInfoRepository;
import com.zlhj.hrxj.interfaces.service.send.HrxjSendInterfaceService;
import com.zlhj.infrastructure.mapper.SupplySuppleMapper;
import com.zlhj.infrastructure.persistence.UserInfoRepositoryMybatis;
import com.zlhj.infrastructure.repository.EncryptionPhoneCheckRepository;
import com.zlhj.interf.TokenInterface;
import com.zlhj.interfaces.hb.facade.dto.RejectionDTO;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.jjyh.inter.JJApplyInterface;
import com.zlhj.jjyh.interf.util.HttpClientPostUtils;
import com.zlhj.kaiTaiBank.interfaces.service.send.KaiTaiSendInterfaceService;
import com.zlhj.kaiTaiBank.plusMapper.BankApprovalRecordMapper;
import com.zlhj.loan.*;
import com.zlhj.loan.entity.Sapdcslas;
import com.zlhj.loan.entity.SapdcslasRepository;
import com.zlhj.loan.service.BankApprovalRecordService;
import com.zlhj.loan.vo.BankApprovalRecordPo;
import com.zlhj.loan.vo.BankHeadOfficeVo;
import com.zlhj.main.fumin.enums.BankOrgNameType;
import com.zlhj.main.fumin.service.BankCodeService;
import com.zlhj.mapper.EncryptionPhoneCheckMapper;
import com.zlhj.mapper.QrCityInfoMapper;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.mq.provider.Sender;
import com.zlhj.msfl.service.MSFLService;
import com.zlhj.telemarketing.service.TelemarketingBusinessService;
import com.zlhj.tikTok.dto.ApplyPlusDto;
import com.zlhj.tikTok.dto.SqcwSendParamDto;
import com.zlhj.tikTok.util.SqcwCodeUtils;
import com.zlhj.unifiedInputPlatform.bj58.dto.ClueInformationRealTime;
import com.zlhj.unifiedInputPlatform.bj58.service.BJ58Service;
import com.zlhj.unifiedInputPlatform.bj58.utils.BJ58Util;
import com.zlhj.unifiedInputPlatform.mi.dto.ClueStateNotifyDTO;
import com.zlhj.unifiedInputPlatform.mi.service.MiService;
import com.zlhj.unifiedInputPlatform.smy.dto.OperationSendSmyStatusDto;
import com.zlhj.unifiedInputPlatform.smy.dto.SmySendParamDto;
import com.zlhj.unifiedInputPlatform.smy.entity.EncryptionPhoneCheck;
import com.zlhj.unifiedInputPlatform.smy.util.StatusCompareUtil;
import com.zlhj.unifiedInputPlatform.universal.service.UnifiedInputPlatformService;
import com.zlhj.user.vo.*;
import com.zlhj.util.ToolsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ADD BY HLX 贷款状态与中转交互的类
 */
@Slf4j
@Service
public class LoanStatusInteraction {

    @Autowired
    private LoanApprovalServce loanApprovalServce;
    @Autowired
    LoanAppStatusService loanAppStatusService;
    @Autowired
    ApproveLogic approveLogic;

    private final CommonInterfaceMessageRepository commonInterfaceMessageRepository;
    private final MultipleLoanRepository loanInfoRepository;
    private final AllSaveRecord allSaveRecord;
    private final MainLoanRepository mainLoanRepository;
    private final CarInfoRepository carInfoRepository;
    private final UserInfoRepositoryMybatis userInfoRepository;
    private final UserHousePropertyRepository userHousePropertyRepository;
    private final QrCityInfoMapper qrCityInfoMapper;
    private final SupplySuppleMapper supplySuppleMapper;
    private final SplBussinessbasicMapper splBussinessbasicMapper;
    private final SendEmailMessage sendEmailMessage;
    private final BankApprovalRecordService bankApprovalRecordService;
    private final BankApprovalRecordMapper bankApprovalRecordMapper;
    private final SinopecService sinopecService;
    private final EncryptionPhoneCheckMapper encryptionPhoneCheckMapper;
    private final EncryptionPhoneCheckRepository encryptionPhoneCheckRepository;

    @Autowired
    private BJ58Service bj58Service;
    @Autowired
    private CreditOverDueSchedule creditOverDueSchedule;
    @Autowired
    private MainLoanData mainLoanData;
    @Autowired
    private BankCodeService bankCodeService;
    @Autowired
    private SubLoanRepository subLoanRepository;
    @Autowired
    private JJApplyInterface jjApplyInterface;
    @Autowired
    private JYXFSednInterfaceService jyxfSednInterfaceService;
    @Autowired
    private MSFLService msflService;
    @Autowired
    private HrxjCreditMessageService hrxjCreditMessageService;
    @Autowired
    private HrxjSendInterfaceService hrxjSendInterfaceService;
    @Autowired
    private KaiTaiSendInterfaceService kaiTaiSendInterfaceService;
    @Autowired
    private SapdcslasRepository sapdcslasRepository;
    @Autowired
    private BXYHSendInterfaceService bxyhSendInterfaceService;
    @Autowired
    private KSYHSendInterfaceService ksyhSendInterfaceService;
    @Autowired
    private TelemarketingBusinessService telemarketingBusinessService;
    @Autowired
    private ZjkCreditApprovalService zjkCreditApprovalService;
    @Autowired
    private KTCancelApplyService ktCancelApplyService;
    @Autowired
    private HBCreditApprovalService hbCreditApprovalService;
    @Autowired
    private SYFLManagerCancelApplyService syflManagerCancelApplyService;
    @Autowired
    private MiService miService;
    @Autowired
    private ApproveRepository approveRepository;
    @Autowired
    private ReasonInfo reasonInfo;
    @Autowired
    private BeCodeAssoRepository beCodeAssoRepository;
    @Resource
    private GtzlCloseOrderService gtzlCloseOrderService;
    @Autowired
    private UnifiedInputPlatformService unifiedInputPlatformService;
    @Autowired
    private LGZLApplyCancelService lgzlApplyCancelService;
    @Autowired
    private Sender sender;

    public LoanStatusInteraction(CommonInterfaceMessageRepository commonInterfaceMessageRepository,
                                 MultipleLoanRepository loanInfoRepository, AllSaveRecord allSaveRecord,
                                 MainLoanRepository mainLoanRepository, CarInfoRepository carInfoRepository,
                                 UserInfoRepositoryMybatis userInfoRepository, UserHousePropertyRepository userHousePropertyRepository,
                                 QrCityInfoMapper qrCityInfoMapper, SupplySuppleMapper supplySuppleMapper,
                                 SplBussinessbasicMapper splBussinessbasicMapper, SendEmailMessage sendEmailMessage,
                                 BankApprovalRecordService bankApprovalRecordService, BankApprovalRecordMapper bankApprovalRecordMapper, SinopecService sinopecService, EncryptionPhoneCheckMapper encryptionPhoneCheckMapper, EncryptionPhoneCheckRepository encryptionPhoneCheckRepository) {
        this.commonInterfaceMessageRepository = commonInterfaceMessageRepository;
        this.loanInfoRepository = loanInfoRepository;
        this.allSaveRecord = allSaveRecord;
        this.mainLoanRepository = mainLoanRepository;
        this.carInfoRepository = carInfoRepository;
        this.userInfoRepository = userInfoRepository;
        this.userHousePropertyRepository = userHousePropertyRepository;
        this.qrCityInfoMapper = qrCityInfoMapper;
        this.supplySuppleMapper = supplySuppleMapper;
        this.splBussinessbasicMapper = splBussinessbasicMapper;
        this.sendEmailMessage = sendEmailMessage;
        this.bankApprovalRecordService = bankApprovalRecordService;
        this.bankApprovalRecordMapper = bankApprovalRecordMapper;
        this.sinopecService = sinopecService;
        this.encryptionPhoneCheckMapper = encryptionPhoneCheckMapper;
        this.encryptionPhoneCheckRepository = encryptionPhoneCheckRepository;
    }

    /**
     * add by HLX 2019-08-29 向中转推送方法
     */
    public static String send(String message) throws Exception {
        String result = "";
        try {
            //中转服务地址
            String urlStr = Setting.get("zlhj.interface.postUrl");
            result = (String) HttpClientPostUtils.post(urlStr, message, "123");
        } catch (Exception e) {
            log.error("TokenInterface.send error :{}", e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
        return result;
    }

    /**
     * 根据汇总贷款ID判断业务大类，分别取不同值
     *
     * @param loanID
     * @return
     */
    public static Object getAmountByLoanID(Integer loanID) {
        //初始化存放金额的实体
        Object applymoney = new Object();

        try {
            MultipleLoanObject multipleLoanObject = new MultipleLoanObject();
            multipleLoanObject.setMLoanid(loanID);
            multipleLoanObject = (MultipleLoanObject) DAOUtil.getObject(multipleLoanObject);
            if (multipleLoanObject == null) {
                return 0;
            }
            if ("1".equals(multipleLoanObject.getM_loanFlag())) {         //银行贷款
                String sql3 = "SELECT * FROM LOANINFO WHERE ML_LOANTYPE='2' and ML_MAINLOANID='" + loanID + "'";
                List<Map<String, Object>> query3 = JDBCUtil.query(sql3);
                //主贷款贷款金额
                if (query3.size() <= 0 || query3 == null) {
                    applymoney = 0;
                } else {
                    for (int i = 0; i < query3.size(); i++) {
                        applymoney = query3.get(i).get("ML_APPLYMONEY");
                    }
                }

            } else if ("2".equals(multipleLoanObject.getM_loanFlag())) {     //租金贷
                String sql4 = "select * from LE_LEASINGPLAN where LEL_LOANID =" + loanID + " and LEL_FEETYPE=1";
                List<Map<String, Object>> query4 = JDBCUtil.query(sql4);
                //车辆款金额
                if (query4.size() <= 0 || query4 == null) {
                    applymoney = 0;
                } else {
                    for (int i = 0; i < query4.size(); i++) {
                        applymoney = query4.get(i).get("LEL_MONEEY");
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return applymoney;
    }

    /**
     * 当贷款状态是银行审批通过或者银行放款成功时，向中转推送贷款状态，并保存记录到COM_INTERFACEMESSAGE表
     *
     * @param loanId
     * @param loanStatus
     * @param remark
     */
    public int pushLoanStatusToInterface(Integer loanId, String loanStatus, String remark) {
        //根据loanId查询当前贷款合作渠道
        SplBussinessBasicObject splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId);
        if (splBussinessBasicObject != null) {
            Integer channel = splBussinessBasicObject.getChannelSource();
            String applyNum = splBussinessBasicObject.getApplyNum();
            if (channel != null) {
                if (12 == channel) {
                    //渠道为上汽，进行推送
                    return sendSqcwStatusNotice(loanId, SqcwCodeUtils.loanStatusTranslate(loanStatus), 0, 0);
                } else if (13 == channel) {
                    //渠道为58同城
                    return bj58Service.bj58RealTimeInteraction(
                            ClueInformationRealTime.createRealTime(loanId, BJ58Util.loanStatusTranslate(loanStatus), null, channel, null)
                    );
                } else if (92 == channel && "放款成功".equals(loanStatus)) {
                    //渠道为92 中石化
                    try {
                        sinopecService.handlingClues(loanId);
                        return 1;
                    } catch (Exception e) {
                        log.error("{}", e);
                        return 0;
                    }
                } else if (14 == channel) {
                    //进件提交后，同步萨摩耶
                    return sendSmyStatusNotice(loanId, StatusCompareUtil.loanStatusTranslate(loanStatus), 0);
                } else if (15 == channel && "放款成功".equals(loanStatus)) {
                    //渠道为15 小米贷款超市
                    try {
                        miService.pushApproveClueState(new LoanId(loanId));
                        return 1;
                    } catch (Exception e) {
                        log.error("{}", e);
                        return 0;
                    }
                } else {
                    return pushLoanStatusToInterfaceOther(loanId, loanStatus, remark);
                }
            }
        }
        return 1;

    }

    /**
     * 当贷款状态是银行审批通过或者银行放款成功时，向中转推送贷款状态，并保存记录到COM_INTERFACEMESSAGE表
     *
     * @param loanId
     * @param loanStatus
     */
    public void releaseLoanStatusChange(Integer loanId, String loanStatus, Integer status) {
        //根据loanId查询当前贷款合作渠道
        SplBussinessBasicObject splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId);
        if (splBussinessBasicObject != null) {
            Integer channel = splBussinessBasicObject.getChannelSource();
            String applyNum = splBussinessBasicObject.getApplyNum();
            if (channel != null && (channel == 16 || channel == 23 || channel == 24 || channel == 28 || channel == 35 || channel == 30 || channel == 36)) {
                Integer directCustomerStatusChannel = channel == 16 ? 6 : channel;//splBussinessBasic中蚂蚁码值为16，DirectCustomerStatusDto为6
                DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(applyNum, directCustomerStatusChannel);
                String splMaxActionNum = "";
                if (directCustomerStatusDto != null) {
                    splMaxActionNum = directCustomerStatusDto.getSplMaxActionNum();
                }
                LoanStatusChangeEnum enumsByValue = LoanStatusChangeEnum.getEnumsByValue(splMaxActionNum);
                LoanStatusChangeEnum loanStatusChangeEnum = LoanStatusRealTimePushTranslate.translateEnum(loanStatus, enumsByValue);
                if (loanStatusChangeEnum == null) {
                    log.info("贷款状态变化未能成功解析：{}", loanStatus);
                    return;
                }
                if (!LoanStatusChangeEnum.LEND_SUC.equals(enumsByValue)) {
                    if (channel == 16){
                        unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, loanStatusChangeEnum.getValue(), channel));
                    }
                    if (channel == 30){
                        unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, loanStatusChangeEnum.getValue(), channel));
                    }
                }
                if (!"放款成功".equals(loanStatus)){
                    if (status == 310 || status == 320 || status == 330){
                        if (channel == 23){
                            unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, LoanStatusChangeEnum.SETTLE.getValue(), channel,"JD_NOTICE_AUTO_FINANCE"));
                        }
                        if (channel == 35 || channel == 36){
                            unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, LoanStatusChangeEnum.SETTLE.getValue(), channel,"JDJT_NOTICE"));
                        }
                    } else if (!LoanStatusChangeEnum.LEND_SUC.equals(enumsByValue)){
                        if (channel == 23) {
                            unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, loanStatusChangeEnum.getValue(), channel,"JD_NOTICE_AUTO_FINANCE"));
                        }
                        if (channel == 35 || channel == 36) {
                            unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, loanStatusChangeEnum.getValue(), channel,"JDJT_NOTICE"));
                        }
                    }

                }

                if (channel == 24) {
                    unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, loanStatusChangeEnum.getValue(), channel,"JD_NOTICE_CAR_LIFE"));
                }

                if (channel == 28) {
                    List<BusinessBasicEntity> businessBasicEntities = splBussinessbasicMapper.getBusinessByApplyNum(applyNum);
                    if (businessBasicEntities != null && !businessBasicEntities.isEmpty()) {
                        splBussinessbasicMapper.updateDirectCustomerStrStatus("", loanStatusChangeEnum.getValue(), Timestamp.valueOf(LocalDateTime.now()), applyNum, loanId);
                    }
                }

            } else if (channel != null && channel == 21) {
                this.pushAcceptLoanStatusToInterface(loanId, applyNum, loanStatus);
            } else if (channel != null && channel == 15) {
                // 拼接报文
                ClueStateNotifyDTO dto = joinMiParam(loanId, applyNum, status);
                if (dto != null) {
                    Map<String, String> map = this.sendMiClueStateNotify(dto, new LoanId(loanId));
                    String interfaceFlag = map.get("interfaceFlag");
                    if ("true".equals(interfaceFlag)) {
                        afterMiNotify(dto, status);
                    }
                }
            }
        }
    }


    public void afterMiNotify(ClueStateNotifyDTO dto, Integer status) {
        //更新状态表
        String applyNum = dto.getApplyId().toString();
        splBussinessbasicMapper.updateMaxActionNum(applyNum, String.valueOf(dto.getOrderStatus()));
        String reason = dto.getReason();
        splBussinessbasicMapper.updateDirectCustomer2(
                dto.getOrderStatus(),
                reason,
                status,
                Timestamp.valueOf(LocalDateTime.now()),
                applyNum);
        List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, 15);
        if (maxLoanStatus != null && !maxLoanStatus.isEmpty()) {
            maxLoanStatus.forEach(r -> {
                Integer id = Integer.valueOf(r.get("loanId").toString());
                Integer maxStatus = Integer.valueOf(r.get("status").toString());
                if (id == null || id == 0) {
                    return;
                }
                //其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
                loanAppStatusService.updateState(id, -15, maxStatus);
                loanAppStatusService.saveNode(id, -15, maxStatus, 0, reason, 4);
                //添加节点
                loanAppStatusService.saveNode(id, -15, -15, 0, reason, 4);
            });
        }
        List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, 15);
        if (basicEntity != null && !basicEntity.isEmpty()) {
            basicEntity.forEach(r -> {
                //受理阶段做取消操作
                if (r.getLoanState() == 0 || r.getLoanState() == 3) {
                    splBussinessbasicMapper.updateSplBussinessBasic(9, reason, r.getBranchLoanId());
                }
            });
        }

    }

    /**
     * 小米拼接审批拒绝参数
     *
     * @param loanId
     * @param applyNum
     * @param loanStatus
     * @return
     */
    public ClueStateNotifyDTO joinMiParam(Integer loanId, String applyNum, Integer loanStatus) {
        if (loanStatus >= 0) {
            return null;
        }
        DirectCustomerStatusDto directCustomerStatus = splBussinessbasicMapper.selectoldStatusByLoanId(loanId);
        ClueStateNotifyDTO clueStateNotifiDTO = new ClueStateNotifyDTO();

        String remark= null;

        // 资方审批拒绝
        if (loanStatus == -160 || loanStatus == -135) {
            clueStateNotifiDTO.setOrderStatus(4);
            remark = "资方拒绝,资方信用评分不足";
        }
        if (loanStatus == -10 || loanStatus == -9 || loanStatus == -15) {
            clueStateNotifiDTO.setOrderStatus(2);
            remark = "客户取消进件";
        }

        // 我司审批拒绝
        if (loanStatus == -90 || loanStatus == -110 || loanStatus == -130 || loanStatus == -200 || loanStatus == -210 || loanStatus < -210) {

            clueStateNotifiDTO.setOrderStatus(4);

            List<ApproveVo> approveVos = approveRepository.list(loanId);
            Optional<String> optional = approveVos.stream()
//                    .filter(r -> "2".equals(r.getApproveType()) || "3".equals(r.getApproveType()) || "4".equals(r.getApproveType()))
                    .filter(r -> "2".equals(r.getConclusion()) || "3".equals(r.getConclusion()))
                    .filter(r -> StringUtil.isNotEmpty(r.getApproveReason()) || StringUtil.isNotEmpty(r.getRejectReason()))
                    .map(r -> StringUtil.isEmpty(r.getApproveReason()) ? r.getRejectReason() : r.getApproveReason())
                    .findFirst();
            String reason = optional.orElse("99");

            if (loanStatus <= -210) {
                List<BeCodeAssoVo> beCodeAssoVos = beCodeAssoRepository.getByType("SPYY");
                remark = beCodeAssoVos.stream()
                        .filter(x -> x.getCode().equals(reason))
                        .map(BeCodeAssoVo::getCodeName)
                        .findFirst()
                        .orElse("其他");
            } else {
                List<String[]> rufuseReason = reasonInfo.getRufuseReason(1, 1, 0, 0);
                remark = rufuseReason.stream()
                        .filter(x -> x[0].equals(reason)).map(x -> x[1])
                        .findFirst()
                        .orElse("其他");
            }
        }
        if (StringUtils.isEmpty(remark)) {
            return null;
        }

        clueStateNotifiDTO.setApplyId(Long.valueOf(applyNum));
        clueStateNotifiDTO.setOpenId(directCustomerStatus.getOpenId());
        clueStateNotifiDTO.setChangeTime(System.currentTimeMillis());
        clueStateNotifiDTO.setClueType(1);
        clueStateNotifiDTO.setReason(remark);
        return clueStateNotifiDTO;
    }


    /**
     * 统一进件状态推送
     *
     * @param loanId     汇总贷款id
     * @param loanStatus 节点状态
     * @param remark     备注
     * @return
     */
    public int pushLoanStatusToInterfaceOther(Integer loanId, String loanStatus, String remark) {
        int result = 0;
        try {
            String sql = "SELECT * FROM SPL_BUSSINESSBASIC WHERE LOAN_ID='" + loanId + "' and CHANNEL_PARTNER in (98,96,18,21) and ORG_CODE != 'HLJRH5'";

            //查传入的loanid是否有合作渠道为98或96的数据
            List<Map<String, Object>> query = JDBCUtil.query(sql);

            //如果合作渠道不是98或96直接返回
            if (query == null || query.size() == 0) {
                log.info("合作渠道不是98或96,不推送中转" + query);
                result = 1;
                return result;
            }

            //服务名
            String serviceName = "APPLY_NOTICE_SERVICE";
            //服务id
            String serviceID = "APPLY_NOTICE_SERVICE";
            //发送报文
            String res = "";
            //接收报文
            String receiveResult = "";
            //存放发送内容
            JSONObject jsonObj = new JSONObject();
            String channelPartner = ConvertionUtil.getSimpleStringWithNull(query.get(0).get("CHANNEL_PARTNER"));
            jsonObj.put("APPLY_ID", query.get(0).get("APPLYNUM"));
            //判断贷款状态是审批通过还是放款成功
            if ("审批通过".equals(loanStatus)) {
                log.debug("审批通过,推送开始");
                jsonObj.put("RESULT_CODE", "S");

                //RQ01033 add by LXY 若为资方审批通过：贷款金额：贷款金额－GPS加融费用
                ApplyPlusDto applyPlusLoanInfo = getApplyPlusLoanInfo(loanId);
                if (applyPlusLoanInfo != null) {
                    //loaninfo表的贷款期限
                    jsonObj.put("LOAN_TERM", applyPlusLoanInfo.getLoanTerm());
                    //loaninfo表的利率
                    jsonObj.put("COMMON_RATE", applyPlusLoanInfo.getCommonRate());
                    //loaninfo的贷款金额-GPS加融费用
                    jsonObj.put("LOAN_AMOUNT", applyPlusLoanInfo.getApplymoney());
                }

            } else if ("放款成功".equals(loanStatus)) {
                // 放款成功的定义调整：需要租赁放款的业务，租赁放款成功（租赁日期不为空）视为放款成功；
                // 不需要租赁放款的业务（含银行贷款业务、消费贷业务），资方放款成功（资方放款日期不为空）视为放款成功。
                boolean isLoanSuccess = isleasinglending(loanId);
                if (!isLoanSuccess) {
                    return result;
                }
                log.debug("放款成功,推送开始");
                //RQ01033 add by LXY 放款成功后新增放款日期、期限、利率传参
                ApplyPlusDto applyPlusLoanInfo = getApplyPlusLoanInfo(loanId);
                if (applyPlusLoanInfo != null) {
                    jsonObj.put("LENDING_DATE", applyPlusLoanInfo.getLendingDate());
                    jsonObj.put("LOAN_TERM", applyPlusLoanInfo.getLoanTerm());
                    jsonObj.put("COMMON_RATE", applyPlusLoanInfo.getCommonRate());
                    jsonObj.put("LOAN_AMOUNT", applyPlusLoanInfo.getApplymoney());
                }
                jsonObj.put("RESULT_CODE", "L");
            } else if ("审批中".equals(loanStatus)) {
                log.debug("审批中,推送开始");
                if ("96".equals(channelPartner)) {
                    //RQ01071 add by hyh
                    ApplyPlusDto applyPlusLoanInfo = getApplyPlusLoanInfo(loanId);
                    if (applyPlusLoanInfo != null) {
                        //loaninfo表的贷款期限
                        jsonObj.put("LOAN_TERM", applyPlusLoanInfo.getLoanTerm());
                        //loaninfo表的利率
                        jsonObj.put("COMMON_RATE", applyPlusLoanInfo.getCommonRate());
                        //loaninfo的贷款金额-GPS加融费用
                        jsonObj.put("LOAN_AMOUNT", applyPlusLoanInfo.getApplymoney());
                    }
                }
                //审批结果
                jsonObj.put("RESULT_CODE", "W");
            } else {
                return result;
            }

            //98-额外需传参数 RQ01071 add by hyh 2022年1月12日
            if (!"98".equals(channelPartner)) {
                //资方名称
                String bankName = supplySuppleMapper.getBankName(loanId);
                //资方
                jsonObj.put("FUNDING_SOURCE", bankName);
                //根据汇总贷款id查询LoanInfo主贷款信息
                ApplyPlusDto applyPlusLoanInfo = getApplyPlusLoanInfo(loanId);
                //还款方式
                jsonObj.put("REPAYMENT_METHOD", applyPlusLoanInfo.getRePaymentWay());

                //----------车辆信息start--------
                //主贷款Id
                Integer loanIdZ = LeaseData.queryLoanID(loanId, "2");
                //根据主贷款id查询carInfo信息
                CarInfoDto carInfo = carInfoRepository.getCarInfoByLoanId(loanIdZ);
                //车辆上牌城市：获取前两位
                String license = ConvertionUtil.getSimpleStringWithNull(carInfo.getCiLicense());
                if (!"".equals(license)) {
                    carInfo.setCiLicense(license.substring(0, 2));
                }
                jsonObj.put("carInfo", carInfo);
                //----------车辆信息end--------

                //查询userInfo主借人信息
                UserInfoObject userInfo = userInfoRepository.getUserInfo(loanId);
                //申请人所在省市
                UserHousePropertyObject house = new UserHousePropertyObject();
                house.setMUserID(userInfo.getUserId());
                house = userHousePropertyRepository.getObject(house);
                //省市码值
                String liveProvince = ConvertionUtil.getSimpleStringWithNull(house.getM_liveprovince());
                String liveCity = ConvertionUtil.getSimpleStringWithNull(house.getM_livecity());
                if (!"".equals(liveProvince) || !"".equals(liveCity)) {
                    //根据省市id查询省市name
                    liveProvince = qrCityInfoMapper.selectProvinceNameByProvinceId(liveProvince);
                    liveCity = qrCityInfoMapper.selectCityNameByCityId(liveCity);
                }
                jsonObj.put("PROVINCE", liveProvince);
                jsonObj.put("CITY", liveCity);
            }

            //传入备注
            jsonObj.put("RESULT_DESC", remark);

            try {
                //报文拼接
                res = TokenInterface.spellMessageBody(jsonObj.toJSONString(), serviceName, serviceID);
                log.info("=======================推送中转贷款状态报文： ==========" + res);
                Thread.sleep(1000);
                //发送报文
                receiveResult = send(res);
                log.debug("推送结束");
                log.info("=======================推送中转贷款状态发送结果： ==========" + receiveResult);
                JSONObject jsonObject = JSONObject.parseObject(receiveResult);
                log.info("=======================保存CommonInterfaceMessageObject开始： ==========");
                CommonInterfaceMessageObject commonInterfaceMessageObject = new CommonInterfaceMessageObject();
                commonInterfaceMessageObject.setMLoanID(loanId);
                commonInterfaceMessageObject.setMInterfaceType("贷款状态推送接口");
                commonInterfaceMessageObject.setMType("发送");
                commonInterfaceMessageObject.setMMessage(ToolsUtil.getWordCountCode(jsonObj.toJSONString(), "UTF-8")
                        > 4000 ? "" : jsonObj.toJSONString());
                commonInterfaceMessageObject.setMReceiveMessage(ToolsUtil.getWordCountCode(receiveResult, "UTF-8") >
                        4000 ? "" : receiveResult);
                //commonInterfaceMessageObject.setMMidMessage(jsonObject.getString("MSG"));
                commonInterfaceMessageObject.setMCreationTime(ToolsUtil.getNewDate(new Date()));
                if ("Y".equals(jsonObject.getString("STATUS"))) {
                    commonInterfaceMessageObject.setMMidMessage("成功");
                } else {
                    commonInterfaceMessageObject.setMMidMessage("失败");
                }
                commonInterfaceMessageRepository.store(commonInterfaceMessageObject);
                log.info("=======================保存CommonInterfaceMessageObject结束： ==========" +
                        commonInterfaceMessageObject.toString());
                result = 1;
            } catch (Exception e) {
                log.error("=======================异常信息：{} ==========", e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("=======================异常信息：{} ==========", e.getMessage(), e);
        }

        return result;
    }

    /**
     * add by hlx 20190829 根据申请单编号查询贷款状态
     *
     * @param map
     * @param map
     * @return
     */
    public Map QueryLoanStatus(Map map) {
        //初始化要返回的结果
        Map<String, Object> result = new HashMap();
        CommonInterfaceMessageObject commonInterfaceMessageObject = new CommonInterfaceMessageObject();
        commonInterfaceMessageObject.setMInterfaceType("中转跑批查询接口");
        commonInterfaceMessageObject.setMType("发送");
        commonInterfaceMessageObject.setMCreationTime(ToolsUtil.getNewDate(new Date()));
        String loanID = "";
        String loanStates = "";
        try {

            log.info("=======================接收跑批查询开始 =========================");


            //传入接收成功
            result.put("STATUS", "Y");
            //获取申请编号
            String Applynum = ConvertionUtil.getSimpleStringWithNull(map.get("applyNum"));
            //传入申请编号
            result.put("APPLY_ID", Applynum);

            commonInterfaceMessageObject.setMMidMessage("成功");

            //根据传入的申请编号查询loanID
            String sql = "SELECT * FROM SPL_BUSSINESSBASIC WHERE APPLYNUM='" + Applynum + "'";
            List<Map<String, Object>> query = JDBCUtil.query(sql);
            if (query.size() <= 0 || query == null) {
                log.info("=======================保存CommonInterfaceMessageObject结束： ==========" + query.size());
                //查询不到loanID，所以直接返回
                //传入贷款节点信息
                result.put("SDLAS_STATUS", 0);
                //传入金额
                result.put("LOAN_AMOUNT", 0);
                //传入审批意见
                result.put("RESULT_DESC", "");
                //传入备注
                result.put("MSG", "");
                log.info("=======================接收跑批保存CommonInterfaceMessageObject开始： ==========");
                commonInterfaceMessageObject.setMReceiveMessage(ToolsUtil.getWordCountCode(result.toString(),
                        "UTF-8") > 4000 ? "" : result.toString());
                commonInterfaceMessageObject.setMLoanID(0);
                commonInterfaceMessageObject.setMRemark("申请单号不存在");
                commonInterfaceMessageRepository.store(commonInterfaceMessageObject);
                log.info("=======================接收跑批保存CommonInterfaceMessageObject结束：loanID： " + loanID + " " +
                        "==========" + commonInterfaceMessageObject.toString());
                return result;
            }

            for (int i = 0; i < query.size(); i++) {
                loanID = ConvertionUtil.getSimpleStringWithNull(query.get(i).get("LOAN_ID"));
                loanStates = ConvertionUtil.getSimpleStringWithNull(query.get(i).get("LOAN_STATE"));
            }

            if ("2".equals(loanStates)) {
                //业务受理拒绝 所以直接返回
                //传入贷款节点信息
                result.put("SDLAS_STATUS", -1);
                //传入金额
                result.put("LOAN_AMOUNT", 0);
                //传入审批意见
                result.put("RESULT_DESC", "受理拒绝");
                //传入备注
                result.put("MSG", "");
                log.info("=======================受理拒绝保存CommonInterfaceMessageObject开始： ==========");
                commonInterfaceMessageObject.setMReceiveMessage(ToolsUtil.getWordCountCode(result.toString(),
                        "UTF-8") > 4000 ? "" : result.toString());
                commonInterfaceMessageObject.setMLoanID(0);
                DAOUtil.store(commonInterfaceMessageObject);
                log.info("=======================受理拒绝保存CommonInterfaceMessageObject结束： loanID： " + loanID +
                        "==========" + commonInterfaceMessageObject.toString());
                return result;
            }

            if (loanID != null && !"".equals(loanID)) {
                //查传入的loanid是否有合作渠道为98的数据
                String sql2 = "SELECT * FROM SAPDCSLAS where SDLAS_LOANID='" + loanID + "'";
                List<Map<String, Object>> query2 = JDBCUtil.query(sql2);
                String sdlaStatus = "";
                //传入贷款节点信息
                if (query2.size() <= 0 || query2 == null) {
                    //因为查询不到节点状态，返回的节点状态设为0
                    log.info("=======================保存CommonInterfaceMessageObject结束： ==========" + query2.size());
                    result.put("SDLAS_STATUS", 0);
                } else {
                    //查询到了，循环拿到节点状态
                    for (int i = 0; i < query2.size(); i++) {
                        if (!"".equals(ConvertionUtil.getSimpleStringWithNull(query2.get(i).get("SDLAS_STATUS")))) {
                            sdlaStatus = ConvertionUtil.getSimpleStringWithNull(query2.get(i).get("SDLAS_STATUS"));
                            result.put("SDLAS_STATUS", sdlaStatus);
                        } else {
                            log.info("=======================保存CommonInterfaceMessageObject||query22结束： ==========" +
                                    query2.size());
                            result.put("SDLAS_STATUS", 0);
                        }
                    }
                }

                //传入金额
                result.put("LOAN_AMOUNT", getAmountByLoanID(Integer.parseInt(loanID)));

                //查询SAPLOANNODE表的审批意见，并传入
                //String sql3 = "SELECT * FROM SAPLOANNODE where SLN_LOANID='"+loanID+"'";
                if ("-110".equals(sdlaStatus) || "-130".equals(sdlaStatus)) {
                    String approveLever = "";
                    if ("-110".equals(sdlaStatus)) {
                        approveLever = "1";
                    } else {
                        approveLever = "9";
                    }
                    String sql3 = "SELECT s.S_KEYVALUE1\n" +
                            "  FROM APPROVE a\n" +
                            "  LEFT JOIN SELECTDATA s\n" +
                            "    ON a.AR_REJECTREASON = s.S_KEYID1\n" +
                            "   AND s.S_TOP = '1'\n" +
                            " WHERE a.AR_LOAN_ID = " + loanID + "\n" +
                            "   AND a.AR_APPROVELEVEL = " + approveLever + "";
                    List<Map<String, Object>> query3 = JDBCUtil.query(sql3);
                    if (query3.size() <= 0 || query3 == null) {
                        result.put("RESULT_DESC", "");
                    } else {
                        for (int i = 0; i < query3.size(); i++) {
                            result.put("RESULT_DESC", ConvertionUtil.getSimpleStringWithNull(query3.get(i).get
                                    ("S_KEYVALUE1")));
                        }
                    }
                } else {
                    if (ConvertionUtil.getSimpleIntegerWithNull(sdlaStatus) > 0) {
                        result.put("RESULT_DESC", "");
                    } else {
                        String sql3 = "select a.* from be_codeasso a where a.be_type ='LCJD' and a.be_code='" +
                                sdlaStatus + "'";
                        List<Map<String, Object>> query3 = JDBCUtil.query(sql3);

                        if (query3.size() <= 0 || query3 == null) {
                            result.put("RESULT_DESC", "");
                        } else {
                            for (int i = 0; i < query3.size(); i++) {
                                result.put("RESULT_DESC", ConvertionUtil.getSimpleStringWithNull(query3.get(i).get
                                        ("BE_CODENAME")));
                            }
                        }
                    }
                }
            } else {
                log.info("=======================保存CommonInterfaceMessageObject||query22结束loanID： ==========" + loanID);
                //传入贷款节点信息
                result.put("SDLAS_STATUS", 0);
                //传入金额
                result.put("LOAN_AMOUNT", 0);
                //传入审批意见
                result.put("RESULT_DESC", "");
            }

            //传入备注
            result.put("MSG", "");

            commonInterfaceMessageObject.setMReceiveMessage(ToolsUtil.getWordCountCode(result.toString(), "UTF-8") >
                    4000 ? "" : result.toString());
            commonInterfaceMessageObject.setMLoanID(ConvertionUtil.getSimpleIntegerWithNull(loanID));
            commonInterfaceMessageRepository.store(commonInterfaceMessageObject);
            log.info("=======================保存CommonInterfaceMessageObject结束loanID： " + loanID + "==========");
            log.info("=======================接收跑批查询结束 =========================");
        } catch (Exception e) {
            log.error("=======================异常信息： {}==========", e.getMessage(), e);
            //传入异常信息
            result.put("MSG", e);
            //传入贷款节点信息
            result.put("SDLAS_STATUS", 0);
            //传入金额
            result.put("LOAN_AMOUNT", 0);
            //传入审批意见
            result.put("RESULT_DESC", "");
            commonInterfaceMessageObject.setMMidMessage("失败");
            try {
                commonInterfaceMessageObject.setMReceiveMessage(ToolsUtil.getWordCountCode(result.toString(),
                        "UTF-8") > 4000 ? "" : result.toString());
            } catch (UnsupportedEncodingException el) {
                log.error(el.getMessage(), el);
            }
            commonInterfaceMessageObject.setMLoanID(ConvertionUtil.getSimpleIntegerWithNull(loanID));
            commonInterfaceMessageRepository.store(commonInterfaceMessageObject);
            log.info("=======================保存CommonInterfaceMessageObject结束loanID： " + loanID + "： ==========" +
                    commonInterfaceMessageObject.toString());
        }

        return result;
    }

    /**
     * 根据汇总贷款id查询LoanInfo主贷款的放款信息
     * add by LXY 2021年11月19日10:46:20
     *
     * @param loanId
     * @return
     */
    public ApplyPlusDto getApplyPlusLoanInfo(Integer loanId) {
        return loanInfoRepository.getApplyPlusLoanInfo(loanId);
    }

    /**
     * 放款成功的定义调整：需要租赁放款的业务，租赁放款成功（租赁日期不为空）视为放款成功；
     * 不需要租赁放款的业务（含银行贷款业务、消费贷业务），资方放款成功（资方放款日期不为空）视为放款成功。
     * add by LXY 2021年11月19日18:37:26
     */
    public boolean isleasinglending(Integer loanId) {
        boolean result = false;
        String isleasinglending = loanApprovalServce.searchIsLeasingLending(loanId);
        if ("1".equals(isleasinglending)) {
            //需要租赁放款,租赁放款成功（租赁日期不为空）视为放款成功
            String lessingLoanDate = ConvertionUtil.getSimpleStringWithNull(loanInfoRepository.getLessingLoanDate(loanId));
            if (!"".equals(lessingLoanDate)) {
                result = true;
            }
        } else {
            //不需要租赁放款,资方放款成功（资方放款日期不为空）视为放款成功。
            String loanDate = ConvertionUtil.getSimpleStringWithNull(loanInfoRepository.getLoanDate(loanId));
            if (!"".equals(loanDate)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 业务受理拒绝推送中转贷款状态
     * add by LXY 2021年11月23日20:03:55
     *
     * @param branchLoanId 受里前id
     * @param applyId      申请编号
     * @return
     */
    public int pushAcceptLoanStatusToInterface(Integer branchLoanId, String applyId) {
        int result = 0;
        //服务名
        String serviceName = "APPLY_NOTICE_SERVICE";
        //服务id
        String serviceID = "APPLY_NOTICE_SERVICE";
        //发送报文
        String res = "";
        //接收报文
        String receiveResult = "";
        //存放发送内容
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("APPLY_ID", applyId);
        //判断贷款状态是审批通过还是放款成功
        jsonObj.put("RESULT_CODE", "R");
        //传入备注
        jsonObj.put("RESULT_DESC", "业务拒绝受理");
        try {
            //报文拼接
            res = TokenInterface.spellMessageBody(jsonObj.toJSONString(), serviceName, serviceID);
            log.info("业务受理拒绝推送中转贷款状态报文：[{}]", res);
            Thread.sleep(1000);
            //发送报文
            receiveResult = send(res);
            log.info("=======================业务受理拒绝推送中转贷款状态发送结果： ==========" + receiveResult);
            JSONObject jsonObject = JSONObject.parseObject(receiveResult);
            if ("Y".equals(jsonObject.getString("STATUS"))) {
                result = 1;
            }
            this.allSaveRecord.saveRecordBeforeAccepting(result, branchLoanId, "电销业务受理拒绝推送中转接口", "发送", res, receiveResult);
        } catch (Exception e) {
            log.error("业务受理拒绝推送中转贷款状态异常,信息：[{}]", e.getMessage());
            this.allSaveRecord.saveRecordBeforeAccepting(result, branchLoanId, "电销业务受理拒绝推送中转接口", "发送", res, receiveResult);
        }

        return result;
    }

    /**
     * 统一进件渠道审批拒绝推送中转贷款状态
     */
    public int pushAcceptLoanStatusToInterface(Integer loanId, String applyId, String remark) {
        int result = 0;
        //服务名
        String serviceName = "APPLY_NOTICE_SERVICE";
        //服务id
        String serviceID = "APPLY_NOTICE_SERVICE";
        //发送报文
        String res = "";
        //接收报文
        String receiveResult = "";
        //存放发送内容
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("APPLY_ID", applyId);
        if ("取消".equals(remark)||"审批拒绝".equals(remark)) {
            //判断贷款状态是审批通过还是放款成功
            jsonObj.put("RESULT_CODE", "R");
        } else if ("面签拒绝".equals(remark)||"放款申请拒绝".equals(remark)) {
            jsonObj.put("RESULT_CODE", "F");
        } else {
            return result;
        }
        //传入备注
        jsonObj.put("RESULT_DESC", remark);
        try {
            //报文拼接
            res = TokenInterface.spellMessageBody(jsonObj.toJSONString(), serviceName, serviceID);
            log.info("业务受理拒绝推送中转贷款状态报文：[{}]", res);
            Thread.sleep(1000);
            //发送报文
            receiveResult = send(res);
            JSONObject jsonObject = JSONObject.parseObject(receiveResult);

            CommonInterfaceMessageObject commonInterfaceMessageObject = new CommonInterfaceMessageObject();
            commonInterfaceMessageObject.setMLoanID(loanId);
            commonInterfaceMessageObject.setMInterfaceType("贷款状态推送接口");
            commonInterfaceMessageObject.setMType("发送");
            commonInterfaceMessageObject.setMMessage(ToolsUtil.getWordCountCode(jsonObj.toJSONString(), "UTF-8")
                    > 4000 ? "" : jsonObj.toJSONString());
            commonInterfaceMessageObject.setMReceiveMessage(ToolsUtil.getWordCountCode(receiveResult, "UTF-8") >
                    4000 ? "" : receiveResult);
            commonInterfaceMessageObject.setMCreationTime(ToolsUtil.getNewDate(new Date()));
            if ("Y".equals(jsonObject.getString("STATUS"))) {
                commonInterfaceMessageObject.setMMidMessage("成功");
                result = 1;
            } else {
                commonInterfaceMessageObject.setMMidMessage("失败");
            }
            commonInterfaceMessageRepository.store(commonInterfaceMessageObject);

        } catch (Exception e) {
            log.error("业务受理拒绝推送中转贷款状态异常,信息：[{}]", e.getMessage());
        }

        return result;
    }

    /**
     * 同步上汽财务渠道状态接口
     *
     * @param loanId       贷款loanId
     * @param status       需要同步的状态
     * @param branchLoanId 受理branchLoanId
     * @param useMark      0:实时推送  1：非实时推送  2：H5提交推送
     * @return
     */
    public int sendSqcwStatusNotice(Integer loanId, String status, Integer branchLoanId, Integer useMark) {
        log.info("同步上汽财务渠道状态接口,入参：loanId：[{}]；loanStatus：[{}]；branchLoanId：[{}]；useMark：[{}]", loanId, status, branchLoanId, useMark);
        int result = 0;
        String str = "";
        String applyNum = "";
        //获取发送参数
        String sqcwContentMsg = "";
        String reveiveMsg = "";
        try {
            //查询贷款当前状态码值
            DirectCustomerStatusDto directCustomerStatusDto = new DirectCustomerStatusDto();
            if (0 == branchLoanId) {
                directCustomerStatusDto = splBussinessbasicMapper.selectoldStatusByLoanId(loanId);
                str = "loanId：" + loanId + "；";
            } else {
                directCustomerStatusDto = splBussinessbasicMapper.selectoldStatusByBranchLoanId(branchLoanId);
                str = "branchLoanId：" + branchLoanId + "；";
            }
            applyNum = directCustomerStatusDto.getSplApplyNum();
            boolean comparisonResult = true;
            if (useMark != 2) {
                //进行状态比对
                comparisonResult = SqcwCodeUtils.comparisonStstus(status, directCustomerStatusDto.getSplMaxActionNum());
            }
            if ("L".equals(status)) {
                comparisonResult = isleasinglending(loanId);
            }
            if (comparisonResult) {
                //获取发送参数
                sqcwContentMsg = getSendSqcwContent(loanId, status, branchLoanId, useMark);
                //发送接口
                Map<String, String> sendResult = sendSqcwStstusInterface(sqcwContentMsg);
                String interfaceFlag = sendResult.get("interfaceFlag");
                reveiveMsg = sendResult.get("reveiveMsg");
                if ("true".equals(interfaceFlag)) {
                    result = 1;
                    //更新状态表
                    splBussinessbasicMapper.updateMaxActionNum(applyNum, status);
                } else {
                    //失败发送邮件
                    //定义换行符
                    String newLine = "\n";
                    String emailContent = "各位好：" + newLine +
                            "以下贷款发送上汽财务渠道状态同步接口失败，请及时处理：" + newLine +
                            str +
                            "申请编号：" + applyNum + "；" +
                            "需要同步的状态：" + status + "；" +
                            "接口返回信息：" + reveiveMsg;

                    sendEmailMessage.sendEmail("上汽财务渠道状态同步接口发送失败", emailContent, "上汽财务渠道状态同步接口发送失败邮件推送");
                }
            }
        } catch (Exception e) {
            log.error("同步上汽财务渠道状态接口,异常：[{}]", e.getMessage());
            //失败发送邮件
            //定义换行符
            String newLine = "\n";
            String emailContent = "各位好：" + newLine +
                    "以下贷款发送上汽财务渠道状态同步接口失败，请及时处理：" + newLine +
                    str +
                    "申请编号：" + applyNum + "；" +
                    "需要同步的状态：" + status + "；" +
                    "错误信息：" + e.getMessage();

            sendEmailMessage.sendEmail("上汽财务渠道状态同步接口发送失败", emailContent, "上汽财务渠道状态同步接口发送失败邮件推送");
        }
        //保存接口履历
        if (0 == branchLoanId) {
            allSaveRecord.saveRecord(result, loanId, "上汽财务渠道状态同步接口", "发送", sqcwContentMsg, reveiveMsg);
        } else {
            allSaveRecord.saveRecordBeforeAccepting(result, branchLoanId, "上汽财务渠道状态同步接口", "发送", sqcwContentMsg, reveiveMsg);
        }
        return result;
    }

    /**
     * 根据loanId和当前状态获取发送报文
     *
     * @param loanId  汇总贷款loanId
     * @param status  当前状态
     * @param useMark 0:实时推送  1：非实时推送
     * @return
     */
    public String getSendSqcwContent(Integer loanId, String status, Integer branchLoanId, Integer useMark) throws Exception {
        //根据loanId查询直客信息
        SplBussinessBasicObject splBussinessBasicObject;
        if (0 == branchLoanId) {
            splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId);
        } else {
            splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByBranchLoannId(branchLoanId);
        }
        SqcwSendParamDto sqcwSendParamDto = new SqcwSendParamDto();
        sqcwSendParamDto.setOrderId(splBussinessBasicObject.getApplyNum());
        sqcwSendParamDto.setApplyStatus("是");
        // 设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        sqcwSendParamDto.setApplyTime(df.format(splBussinessBasicObject.getSubmitDateTi()));
        //O、W、N、S、R、L、F
        if (!"O".equals(status)) {
            sqcwSendParamDto.setCallTime(df.format(splBussinessBasicObject.getFirstAcceptTime()));
            sqcwSendParamDto.setCallStatus("是");
            if ("N".equals(status)) {
                //业务受理拒绝
                sqcwSendParamDto.setCallResult("拒绝进件");
            } else {
                //W、S、R、L、F
                sqcwSendParamDto.setCallResult("同意进件");
                if (!"W".equals(status)) {
                    //S、R、L、F
                    if ("R".equals(status)) {
                        //资方审批拒绝
                        sqcwSendParamDto.setAuditStatus("未通过");
                        sqcwSendParamDto.setAuditResult("审批拒绝");
                        //获取拒绝轨迹中，主键最大的一条的创建时间
                        String completiontime = splBussinessbasicMapper.selectCompletiontimeByLoanId(loanId);
                        Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(completiontime);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        sqcwSendParamDto.setAuditTime(sdf.format(parse));
                    } else {
                        //S、L、F
                        sqcwSendParamDto.setAuditResult("通过");
                        sqcwSendParamDto.setAuditStatus("审批通过");
                        if (0 == useMark && !"L".equals(status) && !"F".equals(status)) {
                            sqcwSendParamDto.setAuditTime(df.format(new Timestamp(System.currentTimeMillis())));
                        } else {
                            BankApprovalRecordPo bankApprovalRecord = this.bankApprovalRecordService.getBankApprovalRecord(loanId);
                            Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bankApprovalRecord.getCreateTime());
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                            sqcwSendParamDto.setAuditTime(sdf.format(parse));
                        }

                        if ("L".equals(status)) {
                            //放款成功
                            sqcwSendParamDto.setLoanStatus("已放款");
                            ApplyPlusDto applyPlusLoanInfo = getApplyPlusLoanInfo(loanId);
                            if (applyPlusLoanInfo != null) {
                                String loanTime = "";
                                String time = applyPlusLoanInfo.getLendingDate();
                                //将放款时间格式化成yyyyMMddHHmmss
                                try {
                                    Date parse = new SimpleDateFormat("yyyy-MM-dd").parse(time);
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                                    loanTime = sdf.format(parse);
                                } catch (ParseException e) {
                                    log.error("上汽财务获取参数报文，时间转换异常，loanId:[{}]", loanId);
                                    e.printStackTrace();
                                }
                                sqcwSendParamDto.setLoanTime(loanTime);
                                String loanAmount = applyPlusLoanInfo.getApplymoney();
                                //将贷款金额保留两位小数
                                BigDecimal bigDecimal = new BigDecimal(loanAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
                                sqcwSendParamDto.setLoanAmount(bigDecimal.toString());
                            }
                        } else if ("F".equals(status)) {
                            sqcwSendParamDto.setLoanStatus("未放款");
                        }
                    }
                }
            }
        }
        return JSON.toJSONString(sqcwSendParamDto, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullListAsEmpty);
    }

    /**
     * 发送上汽财务状态同步接口
     *
     * @param sqcwContentMsg
     * @return
     */
    public Map<String, String> sendSqcwStstusInterface(String sqcwContentMsg) {
        Map<String, String> map = new HashMap<>(16);
        map.put("interfaceFlag", "false");
        //服务名
        String serviceName = "APPLY_NOTICE_SERVICE";
        //服务id
        String serviceId = "sendExternalChannelStatus";
        //发送报文
        String res;
        //接收报文
        String receiveResult;
        //存放发送内容
        try {
            //报文拼接
            res = TokenInterface.spellMessageBody(sqcwContentMsg, serviceName, serviceId);
            log.info("发送上汽财务状态同步接口,推送中转贷款状态报文：" + res);
            Thread.sleep(1000);
            //发送报文
            receiveResult = send(res);
            log.debug("发送上汽财务状态同步接口,推送结束,返回报文：" + receiveResult);
            JSONObject jsonObject = JSONObject.parseObject(receiveResult);
            String ststusInterface = jsonObject.getString("STATUS");
            String msgInterface = jsonObject.getString("MSG");
            if ("Y".equals(ststusInterface)) {
                JSONObject sqcwMsg = JSONObject.parseObject(msgInterface);
                String sqcwCode = sqcwMsg.getString("code");
                if ("0000".equals(sqcwCode)) {
                    map.put("interfaceFlag", "true");
                }
            }
            map.put("reveiveMsg", receiveResult);
        } catch (Exception e) {
            map.put("reveiveMsg", e.getMessage());
            log.error("发送上汽财务状态同步接口异常信息：[{}]", e.getMessage());
        }
        return map;
    }

    /**
     * 合作渠道=12-上汽财务的线索-30天超期定时任务
     */
    public void sqcwExternalChannelSchedule() throws Exception {
        log.info("合作渠道=12-上汽财务的线索-30天超期定时任务开始：[{}]", DateUtil.formatToYYYYMMDDHHMMSS2(new Date()));
        //获取30天前的日期
        String date = DateUtil.getNowBeforeDate(30);
        log.info("合作渠道=12-上汽财务的线索-30天超期定时任务，实际执行的创建时间[{}]", date);
        //合作渠道
        int channelPartner = 12;
        //直客状态表合作渠道
        int directChannelPartner = 2;
        //筛选线索进件时间超过30天、且未发送过终态（N、R、L、F）的线索
        List<String> applyNums = splBussinessbasicMapper.selectSqcwExternalChannelScheduleList(date, directChannelPartner);
        log.info("筛选线索进件时间超过30天、且未发送过终态（N、R、L、F）的线索:[{}]", applyNums);
        if (applyNums.size() > 0) {
            for (String applyNum : applyNums) {
                //查询该线索进件业务的当前贷款状态,若有多笔贷款取贷款状态最大的该笔
                Map<String, Object> maxStatus = getMaxStatus(applyNum, channelPartner);
                String isSendMark = ConvertionUtil.getSimpleStringWithNull(maxStatus.get("isSendMark"));
                String status = ConvertionUtil.getSimpleStringWithNull(maxStatus.get("status"));
                if ("true".equals(isSendMark)) {
                    Integer loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
                    Integer branchLoanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("branchLoanId"));
                    //发送接口
                    sendSqcwStatusNotice(loanId, status, branchLoanId, 1);
                } else {
                    log.info("当前订单编号：[{}]；状态为：[{}]；无需发送接口", applyNum, status);
                }
            }
        } else {
            log.info("合作渠道=12-上汽财务的线索-30天超期定时任务,查询无数据，不需要处理");
        }
    }

    /**
     * 获取定时任务订单的当前最大状态,并转换码值
     * 1. 若为终态（＜0或大于等于245），直接调用接口，按当前状态拼装参数
     * 2. 若＞235，视为进行中的状态，不做任何处理
     * 3. 其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数。
     *
     * @param applyNum
     * @param channelPartner
     * @return
     * @throws Exception
     */
    public Map<String, Object> getMaxStatus(String applyNum, Integer channelPartner) throws Exception {
        log.info("获取定时任务订单的当前最大状态方法入参，订单编号：[{}]，合作渠道：[{}]", applyNum, channelPartner);
        Map<String, Object> result = new HashMap<>(16);
        Integer loanId;
        Integer loanStates;
        //获取最大状态
        List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, channelPartner);
        if (maxLoanStatus != null && maxLoanStatus.size() > 0) {
            //如果有多条，则取第一条
            Map<String, Object> maxStatus = maxLoanStatus.get(0);
            //sapdcslas表中状态
            loanStates = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("status"));
            //sapdcslas表中汇总贷款id
            loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
            if (loanStates >= 245) {
                result.put("status", "L");
                result.put("isSendMark", "true");
                result.put("loanId", loanId);
                result.put("branchLoanId", 0);
            } else if (loanStates > 235) {
                result.put("isSendMark", "false");
                result.put("status", loanStates);
            } else {
                result.put("isSendMark", "true");
                result.put("loanId", loanId);
                result.put("branchLoanId", 0);
                //查询贷款当前状态码值
                DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectoldStatusByLoanId(loanId);
                if ("S".equals(directCustomerStatusDto.getSplMaxActionNum())) {
                    result.put("status", "F");
                } else {
                    result.put("status", "R");
                }
                if (loanStates > 0) {
                    log.info("合作渠道-12上汽财务，超期定时任务，系统取消处理，loanId：[{}]；当前状态：[{}]", loanId, loanStates);
                    //其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
                    loanAppStatusService.updateState(loanId, -15, loanStates);
                    loanAppStatusService.saveNode(loanId, -15, loanStates, 0, "线索超期取消", 4);
                    //添加节点
                    loanAppStatusService.saveNode(loanId, -15, -15, 0, "线索超期取消", 4);
                    //发送资方撤销接口
                    sendBankCancelInterface(loanId, "线索超期取消", "上汽财务");
                }
            }
        } else {
            log.info("如果sapdcslas表中状态为空，则查询businessBasic表中的状态");
            //如果sapdcslas表中状态为空，则查询businessBasic表中的状态
            List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, channelPartner);
            if (basicEntity != null && basicEntity.size() > 0) {
                //获取第一条
                BusinessBasicEntity businessBasicEntity = basicEntity.get(0);
                //Business表中状态
                loanStates = ConvertionUtil.getSimpleIntegerWithNull(businessBasicEntity.getLoanState());
                //0-未受理  1-接受近件  2-拒绝近件  3-贷申请业务受理
                if (loanStates == 2) {
                    result.put("status", "N");
                    result.put("isSendMark", "true");
                } else {
                    if (loanStates == 3) {
                        result.put("status", "R");
                    } else {
                        result.put("status", "N");
                    }
                    result.put("isSendMark", "true");
                    //受理阶段做取消操作
                    splBussinessbasicMapper.updateSplBussinessBasic(2, "线索超期取消", businessBasicEntity.getBranchLoanId());
                }
                result.put("loanId", 0);
                result.put("branchLoanId", businessBasicEntity.getBranchLoanId());
            } else {
                result.put("status", "查询无数据");
                result.put("isSendMark", "false");
            }
        }
        log.info("获取定时任务订单的当前最大状态方法最终返回getMaxStatus：[{}]", result);
        return result;
    }

    /**
     * 运维发送上汽财务状态推送接口
     *
     * @param applyNum
     * @return
     */
    public int sendoperationSqcwStstusInterface(String applyNum) throws Exception {
        log.info("运维发送上汽财务状态推送接口,入参：applyNum：[{}]", applyNum);
        int result = 0;
        //获取订单当前最大状态
        Map<String, Object> orderStatusNow = getOrderStatusNow(applyNum);
        if (orderStatusNow != null) {
            String status = ConvertionUtil.getSimpleStringWithNull(orderStatusNow.get("status"));
            Integer loanId = ConvertionUtil.getSimpleIntegerWithNull(orderStatusNow.get("loanId"));
            Integer branchLoanId = ConvertionUtil.getSimpleIntegerWithNull(orderStatusNow.get("branchLoanId"));
            if (loanId == 0 && branchLoanId == 0) {
                log.info("运维发送上汽财务状态推送接口，根据申请编号未查询到信息，applyNum:[{}]", applyNum);
                return result;
            }
            //获取发送参数
            String sqcwContentMsg = getSendSqcwContent(loanId, status, branchLoanId, 1);
            //发送接口
            Map<String, String> sendResult = sendSqcwStstusInterface(sqcwContentMsg);
            String interfaceFlag = sendResult.get("interfaceFlag");
            String reveiveMsg = sendResult.get("reveiveMsg");
            if ("true".equals(interfaceFlag)) {
                result = 1;
                //查询贷款当前状态码值
                DirectCustomerStatusDto directCustomerStatusDto;
                if (0 == branchLoanId) {
                    directCustomerStatusDto = splBussinessbasicMapper.selectoldStatusByLoanId(loanId);
                } else {
                    directCustomerStatusDto = splBussinessbasicMapper.selectoldStatusByBranchLoanId(branchLoanId);
                }
                //进行状态比对
                boolean comparisonResult = SqcwCodeUtils.comparisonStstus(status, directCustomerStatusDto.getSplMaxActionNum());
                if ("L".equals(status)) {
                    comparisonResult = isleasinglending(loanId);
                }
                if (comparisonResult) {
                    //更新状态表
                    splBussinessbasicMapper.updateMaxActionNum(applyNum, status);
                }
            }
            //保存接口履历
            allSaveRecord.saveRecord(result, loanId, "上汽财务渠道状态同步接口", "发送", sqcwContentMsg, reveiveMsg);
        }
        return result;
    }

    /**
     * 运维获取订单当前最大状态
     *
     * @param applyNum
     * @return
     */
    public Map<String, Object> getOrderStatusNow(String applyNum) {
        Map<String, Object> result = new HashMap<>(16);
        try {
            Integer loanId;
            Integer loanStates;
            Integer channelPartner = 12;
            //获取当前最大状态 //W、S、R、L、F
            List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, channelPartner);
            if (maxLoanStatus != null && maxLoanStatus.size() > 0) {
                //如果有多条，则取第一条
                Map<String, Object> maxStatus = maxLoanStatus.get(0);
                //sapdcslas表中状态
                loanStates = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("status"));
                //sapdcslas表中汇总贷款id
                loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
                boolean loanSuccess = isleasinglending(loanId) && loanStates >= 245;
                if (loanSuccess) {
                    result.put("status", "L");
                    result.put("loanId", loanId);
                    result.put("branchLoanId", 0);
                } else {
                    result.put("loanId", loanId);
                    result.put("branchLoanId", 0);
                    if (loanStates < 0) {
                        //查询贷款当前状态码值
                        DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectoldStatusByLoanId(loanId);
                        if ("S".equals(directCustomerStatusDto.getSplMaxActionNum())) {
                            result.put("status", "F");
                        } else {
                            result.put("status", "R");
                        }
                    } else {
                        //查询是否银行审批通过
                        Map isBankMap = approveLogic.CheckOrIsBack(loanId);
                        String bankFlag = ConvertionUtil.getSimpleStringWithNull(isBankMap.get("back"));
                        if ("2".equals(bankFlag)) {
                            result.put("status", "S");
                        } else {
                            result.put("status", "W");
                        }
                    }
                }
            } else {
                //O、W、N、
                //如果sapdcslas表中状态为空，则查询businessBasic表中的状态
                List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, channelPartner);
                if (basicEntity != null && basicEntity.size() > 0) {
                    //获取第一条
                    BusinessBasicEntity businessBasicEntity = basicEntity.get(0);
                    //Business表中状态
                    loanStates = ConvertionUtil.getSimpleIntegerWithNull(businessBasicEntity.getLoanState());
                    //0-未受理  1-接受近件  2-拒绝近件  3-贷申请业务受理
                    result.put("loanId", 0);
                    result.put("branchLoanId", businessBasicEntity.getBranchLoanId());
                    if (loanStates == 0) {
                        result.put("status", "O");
                    } else if (loanStates == 2) {
                        result.put("status", "N");
                    } else if (loanStates == 3) {
                        result.put("status", "W");
                    }
                }
            }
        } catch (Exception e) {
            log.info("运维获取订单当前最大状态,异常：[{}]；applyNum：[{}]", e.getMessage(), applyNum);
        }
        return result;
    }

    /**
     * 发送资方撤销接口
     *
     * @param loanId
     * @param cancelReason
     * @param channelName
     */
    public void sendBankCancelInterface(Integer loanId, String cancelReason, String channelName) {
        log.info(channelName + "渠道，超期取消发送资方撤销接口，入参：loanId：[{}]；cancelReason：[{}]", loanId, cancelReason);
        String str = "";
        Integer bankCode = 0;
        try {

            BankHeadOfficeVo bankCodeByLoanId = this.bankCodeService.getBankCodeByLoanId(loanId);
            bankCode = bankCodeByLoanId == null ? null : bankCodeByLoanId.getBaOrgId();
            log.info(channelName + "渠道，超期取消发送资方撤销接口,当前资方是：[{}]", bankCode);
            if (BankOrgNameType.JIUJIANG_BANK.getKey().equals(bankCode)
                    || BankOrgNameType.JIUJIANGZHONGSHANXIAOLAN_BANK.getKey().equals(bankCode)) {
                Map<String, Object> map = new HashMap<>(16);
                //发送放款前取消接口
                SubLoanObject subLoan = new SubLoanObject();
                subLoan.setMLoanID(loanId);
                subLoan = this.subLoanRepository.getObject(subLoan);
                if (subLoan != null) {
                    map = jjApplyInterface.removeCredit(loanId, "C", cancelReason);
                }
                log.info(channelName + "渠道，超期取消发送九江放款前取消接口返回结果：[{}]", map);
                int noSendJJApplyInterface = ConvertionUtil.getSimpleIntegerWithNull(map.get("noSend"));
                int result = ConvertionUtil.getSimpleIntegerWithNull(map.get("result"));
                String message = ConvertionUtil.getSimpleStringWithNull(map.get("remark"));
                if (noSendJJApplyInterface != 1) {
                    if (result != 1) {
                        str = channelName + "渠道，超期取消发送九江放款前取消接口失败，loanId：" + loanId + "；返回信息为：" + message;
                        log.error(channelName + "渠道，超期取消发送九江放款前取消接口失败，汇总loanId = [{}]", loanId);
                    }
                }
            } else if (BankOrgNameType.JIUYIN_CONSUMER_LOAN.getKey().equals(bankCode)) {
                Map<String, Object> map = jyxfSednInterfaceService.sendJJYHLoanCancellationInterface(loanId, "上汽财务超期跑批", cancelReason);
                log.info(channelName + "渠道，超期取消发送九江银行消费贷贷款取消接口返回结果：[{}]", map);
                Integer flag = ConvertionUtil.getSimpleIntegerWithNull(map.get("flag"));
                if (flag != 1) {
                    String message = ConvertionUtil.getSimpleStringWithNull(map.get("message"));
                    str = channelName + "渠道，超期取消发送九江银行消费贷贷款取消接口失败，loanId：" + loanId + "；返回信息为：" + message;
                    log.error(channelName + "渠道，超期取消发送九江银行消费贷贷款取消接口失败，汇总loanId = [{}]", loanId);
                }
            } else if (BankOrgNameType.MINSHENG_FINANCE_LEASE.getKey().equals(bankCode)) {
                Map map = msflService.determineCancleApply(loanId, "系统取消", cancelReason);
                log.info(channelName + "渠道，超期取消发送民生撤销申请接口返回结果：[{}]", map);
                int result = ConvertionUtil.getSimpleIntegerWithNull(map.get("flag"));
                String message = ConvertionUtil.getSimpleStringWithNull(map.get("msg"));
                if (result != 1) {
                    str = channelName + "渠道，超期取消发送民生撤销申请接口失败，loanId：" + loanId + "；返回信息为：" + message;
                    log.error(channelName + "渠道，超期取消发送民生撤销申请接口失败，汇总loanId = [{}]", loanId);
                }
            } else if (BankOrgNameType.HUARONGXIANGJIANG_BANK.getKey().equals(bankCode)) {
                Boolean isAppro = hrxjCreditMessageService.checkLoanInfoBankApprover(loanId);
                log.info(channelName + "渠道，超期取消发送华融湘江放款提交接口，判断是否已经完成过资方终审结果：[{}]", isAppro);
                if (isAppro) {
                    Map<String, Object> map = hrxjSendInterfaceService.sendHrxjLoanSubmitInterface(loanId, cancelReason, "02");
                    log.info(channelName + "渠道，超期取消发送华融湘江放款提交接口返回结果：[{}]", map);
                    int result = ConvertionUtil.getSimpleIntegerWithNull(map.get("flag"));
                    String message = ConvertionUtil.getSimpleStringWithNull(map.get("msg"));
                    if (result != 1) {
                        str = channelName + "渠道，超期取消发送华融湘江放款提交接口失败，loanId：" + loanId + "；返回信息为：" + message;
                        log.error(channelName + "渠道，超期取消发送华融湘江放款提交接口失败，汇总loanId = [{}]", loanId);
                    }
                }
            } else if (BankOrgNameType.KAITAI_BANK.getKey().equals(bankCode)) {
                //查询资方进件接口状态
                int interfaceNum = sapdcslasRepository.searchApplyInInterfaceState(loanId);
                if (interfaceNum == 1) {
                    Map<String, Object> map = kaiTaiSendInterfaceService.sendKaiTaiOrderCancellation(loanId);
                    log.info(channelName + "渠道，超期取消发送开泰订单取消申请接口返回结果：[{}]", map);
                    MainLoanObject mainLoan = mainLoanRepository.selectByLoanId(loanId);
                    //车抵贷取消写在了前台
                    if ("3".equals(mainLoan.getM_ProductModel())) {
                        try {
                            ktCancelApplyService.cancelApply(loanId);
                            map.put("resultFlag", "1");
                        } catch (Exception e) {
                            map.put("resultFlag", "0");
                        }
                    } else {
                        map = kaiTaiSendInterfaceService.sendKaiTaiOrderCancellation(loanId);
                    }
                    log.info("上汽财务渠道，超期取消发送开泰订单取消申请接口返回结果：[{}]", map);
                }
            } else if (BankOrgNameType.BAIXIN_BANK.getKey().equals(bankCode)) {
                //根据loanId查询贷款状态
                Sapdcslas sapdcslas = sapdcslasRepository.getSapdcslasByLoanId(loanId);
                //资方贷款状态
                String loanStatus = ConvertionUtil.getSimpleStringWithNull(sapdcslas.getManagementLoanStatus());
                //资方审批完成日期
                Integer createTime = ConvertionUtil.getSimpleIntegerWithNull(bankApprovalRecordMapper.selectBankApprovalRecordTime(loanId));
                log.info(channelName + "渠道，超期取消发送百信关单接口，判断是否有资方贷款状态：[{}]", loanStatus);
                //若资方贷款状态为空，则不调接口
                if (!StringUtil.isEmpty(loanStatus) && !loanStatus.equals("0600") && Integer.valueOf(loanStatus) <= 801) {
                    //百信关单接口
                    ResultDto map = bxyhSendInterfaceService.closeOrderInterface(loanId);
                    log.info(channelName + "渠道，超期取消发送百信关单接口返回结果：[{}]", map);
                    if (map.getCode() != 1) {
                        str = channelName + "渠道，超期取消发送百信关单接口失败，loanId：" + loanId + "；返回信息为：" + map.getMsg();
                        log.error(channelName + "渠道，超期取消发送百信关单接口失败，汇总loanId = [{}]", loanId);
                    }
                } else if (!StringUtil.isEmpty(loanStatus) && createTime < 30) {
                    return;
                }
            } else if (BankOrgNameType.KESHANG_BANK.getKey().equals(bankCode)) {
                //客商进件撤销接口
                ResultDto map = ksyhSendInterfaceService.incomingCancellationDealLogic(loanId, 1);
                log.info(channelName + "渠道，超期取消发送客商进件撤销接口返回结果：[{}]", map);
                if (map.getCode() != 1) {
                    str = channelName + "渠道，超期取消发送客商进件撤销接口失败，loanId：" + loanId + "；返回信息为：" + map.getMsg();
                    log.error(channelName + "渠道，超期取消发送客商进件撤销接口失败，汇总loanId = [{}]", loanId);
                }
            } else if (BankOrgNameType.ZHANGJIAKOU_BANK.getKey().equals(bankCode)) {
                //张家口银行取消申请接口
                ResultDto resultDto = zjkCreditApprovalService.accountManagerCancel
                        (new CreditApprovalParamDto().setLoanId(loanId).setRevokeReason("初筛超期，自动取消"));
                log.info(channelName + "渠道，超期取消发送张家口银行取消申请接口返回结果：[{}]", resultDto);
                if (resultDto.getCode() != 1) {
                    str = channelName + "渠道，超期取消发送张家口银行取消申请接口失败，loanId：" + loanId + "；返回信息为：" + resultDto.getMsg();
                    log.error(channelName + "渠道，超期取消发送张家口银行取消申请接口失败，汇总loanId = [{}]", loanId);
                }
            } else if (BankOrgNameType.HEBEI_BANK.getKey().equals(bankCode)) {
                //河北银行取消申请接口
                try {
                    hbCreditApprovalService.rejection(new RejectionDTO(loanId));
                } catch (Exception e) {
                    str = channelName + "渠道，超期取消发送河北银行取消申请接口失败，loanId：" + loanId + "；";
                    log.error(channelName + "渠道，超期取消发送河北银行取消申请接口失败，汇总loanId = [{}]", loanId);
                }
            } else if (BankOrgNameType.SYFL_BANK.getKey().equals(bankCode)) {
                //苏银金租未生效合同取消接口
                try {
                    syflManagerCancelApplyService.cancelApply(loanId);
                } catch (Exception e) {
                    str = channelName + "渠道，超期取消发送苏银金租未生效合同取消接口失败，loanId：" + loanId + "；";
                    log.error(channelName + "渠道，超期取消发送苏银金租未生效合同取消接口失败，汇总loanId = [{}]", loanId);

                }
            }else if (BankOrgNameType.GTZL_BANK.getKey().equals(bankCode)){
                //国泰租赁合同取消接口
                try {
                    gtzlCloseOrderService.cancelApply(loanId,cancelReason);
                } catch (Exception e) {
                    str = channelName + "渠道，超期取消发送国泰租赁合同取消接口失败，loanId：" + loanId + "；";
                    log.error("{}渠道，超期取消发送国泰租赁合同取消接口失败，汇总loanId = [{}]", channelName, loanId);

                }
            }else if (BankOrgNameType.LGZL_BANK.getKey().equals(bankCode)) {
                try {
                    lgzlApplyCancelService.applyCancel(new ApplyCancelInput(loanId,"客户取消。"));
                } catch (Exception e) {
                    //发送邮件
                    str = channelName + "渠道，超期取消发送立根租赁合同取消接口失败，loanId：" + loanId + "；";
                    log.error("{}渠道，超期取消发送立根租赁合同取消接口失败，汇总loanId = [{}]", channelName, loanId);
                }
            }

            //调用租赁拒绝通知接口
            Map sendLeaseMap = mainLoanData.sendLeaseLending(loanId, 0, -15, "", cancelReason, "-3", cancelReason);
            log.info(channelName + "渠道超期取消，发送租赁拒绝通知接口返回结果：[{}]", sendLeaseMap);
            int sendLeaseResult = ConvertionUtil.getSimpleIntegerWithNull(sendLeaseMap.get("RESULT"));
            int noSend = ConvertionUtil.getSimpleIntegerWithNull(sendLeaseMap.get("noSend"));
            if (noSend != 1 && sendLeaseResult != 1) {
                str += "；" + channelName + "渠道超期取消,发送租赁拒绝通知接口失败，loanId：" + loanId + "；返回信息为：" + sendLeaseMap;
                log.error("自动审批超期取消，调用租赁拒绝通知接口失败，汇总loanId = [{}]", loanId);
            }

            //同步电销接口
            if (creditOverDueSchedule.needSynchronization(loanId)) {
                //同步电销结果
                Boolean synResult = telemarketingBusinessService.synchronizationLoanStatus(loanId, "-15", null, null, true);
                log.info(channelName + "渠道超期取消，发送同步电销结果接口返回结果：[{}]", sendLeaseMap);
                if (!synResult) {
                    str += "；" + channelName + "渠道超期取消,发送同步电销结果接口失败，loanId：" + loanId + "；返回信息为：" + synResult;
                    log.error("自动审批超期取消，同步电销接口失败，汇总loanId = [{}]", loanId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            str = channelName + "渠道，超期取消发送资方撤销接口,异常，loanId：" + loanId + "；资方为：" + bankCode + "异常信息为：" + e.getMessage();
            log.error(channelName + "渠道，超期取消发送资方撤销接口,异常：[{}]", e.getMessage());
        } finally {
            if (!"".equals(str)) {
                sendEmailMessage.sendEmail(channelName + "超期取消发送资方撤销接口失败", str, channelName + "超期取消发送资方撤销失败邮件推送");
            }
        }
        log.info(channelName + "渠道，超期取消发送资方撤销接口，结束：loanId：[{}]；cancelReason：[{}]", loanId, cancelReason);
    }

    /**
     * 萨摩耶回调提交接口
     *
     * @param loanId       贷款loanId
     * @param status       需要同步的状态
     * @param branchLoanId 受理branchLoanId
     * @return
     */
    public int sendSmyStatusNotice(Integer loanId, String status, Integer branchLoanId) {
        log.info("萨摩耶回调提交接口,入参：loanId：[{}]；loanStatus：[{}]；branchLoanId：[{}]；", loanId, status, branchLoanId);
        int result = 0;
        String str = "";
        String applyNum = "";
        //获取发送参数
        String smyContentMsg = "";
        String reveiveMsg = "";
        boolean comparisonResult = false;
        try {
            //查询贷款当前状态码值
            DirectCustomerStatusDto directCustomerStatusDto = new DirectCustomerStatusDto();
            if (0 == branchLoanId) {
                directCustomerStatusDto = splBussinessbasicMapper.selectoldStatusByLoanId(loanId);
                str = "loanId：" + loanId + "；";
            } else {
                directCustomerStatusDto = splBussinessbasicMapper.selectoldStatusByBranchLoanId(branchLoanId);
                str = "branchLoanId：" + branchLoanId + "；";
            }
            applyNum = directCustomerStatusDto.getSplApplyNum();
            //进行状态比对
            comparisonResult = StatusCompareUtil.comparisonStstus(status, directCustomerStatusDto.getSplMaxActionNum());
            if ("P053".equals(status)) {
                comparisonResult = isleasinglending(loanId);
            }
            if (comparisonResult) {
                //获取发送参数
                smyContentMsg = getSendSmyContent(loanId, status, branchLoanId);
                //发送接口
                Map<String, String> sendResult = sendSmyStstusInterface(smyContentMsg);
                String interfaceFlag = sendResult.get("interfaceFlag");
                reveiveMsg = sendResult.get("reveiveMsg");
                if ("true".equals(interfaceFlag)) {
                    result = 1;
                    //更新状态表
                    splBussinessbasicMapper.updateMaxActionNum(applyNum, status);
                } else {
                    //失败发送邮件
                    //定义换行符
                    String newLine = "\n";
                    String emailContent = "各位好：" + newLine +
                            "以下贷款发送萨摩耶回调提交接口失败，请及时处理：" + newLine +
                            str +
                            "申请编号：" + applyNum + "；" +
                            "需要同步的状态：" + status + "；" +
                            "接口返回信息：" + reveiveMsg;

                    sendEmailMessage.sendEmail("萨摩耶回调提交接口发送失败", emailContent, "萨摩耶回调提交接口发送失败邮件推送");
                }
            }
        } catch (Exception e) {
            log.error("萨摩耶回调提交接口,异常：[{}]", e.getMessage());
            //失败发送邮件
            //定义换行符
            String newLine = "\n";
            String emailContent = "各位好：" + newLine +
                    "以下贷款发送萨摩耶回调提交接口失败，请及时处理：" + newLine +
                    str +
                    "申请编号：" + applyNum + "；" +
                    "需要同步的状态：" + status + "；" +
                    "错误信息：" + e.getMessage();

            sendEmailMessage.sendEmail("萨摩耶回调提交接口发送失败", emailContent, "萨摩耶回调提交接口发送失败邮件推送");
        }
        if (comparisonResult) {
            //保存接口履历
            if (0 == branchLoanId) {
                allSaveRecord.saveRecord(result, loanId, "萨摩耶回调提交接口", "发送", smyContentMsg, reveiveMsg);
            } else {
                allSaveRecord.saveRecordBeforeAccepting(result, branchLoanId, "萨摩耶回调提交接口", "发送", smyContentMsg, reveiveMsg);
            }
        }
        return result;
    }

    /**
     * 获取萨摩耶发送参数
     *
     * @param loanId
     * @param status
     * @param branchLoanId
     * @return
     * @throws Exception
     */
    public String getSendSmyContent(Integer loanId, String status, Integer branchLoanId) throws Exception {
        //根据loanId查询直客信息
        SplBussinessBasicObject splBussinessBasicObject;
        SplUserInfoObject splUserInfoObject;
        if (0 == branchLoanId) {
            splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId);
            splUserInfoObject = splBussinessbasicMapper.selectSplUserInfoByLoanId(loanId);
        } else {
            splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByBranchLoannId(branchLoanId);
            splUserInfoObject = splBussinessbasicMapper.selectSplUserInfoByBranchId(branchLoanId);
        }
        SmySendParamDto smySendParamDto = new SmySendParamDto();
        smySendParamDto.setCustId(splBussinessBasicObject.getApplyNum());
        // 设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        smySendParamDto.setStatusTime(df.format(new Date()));
        smySendParamDto.setPhoneNo(splUserInfoObject.getPhone());
        smySendParamDto.setStatus(status);
        if ("P053".equals(status)) {
            //放款成功 放款金额：车辆款+车牌加融款
            String money = splBussinessbasicMapper.getLoanMoneyBuLoanId(loanId);
            smySendParamDto.setLoanAmount(money);
        }
        return JSON.toJSONString(smySendParamDto, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullListAsEmpty);
    }

    /**
     * 发送萨摩耶状态同步接口
     *
     * @param sqcwContentMsg
     * @return
     */
    public Map<String, String> sendSmyStstusInterface(String sqcwContentMsg) {
        Map<String, String> map = new HashMap<>(16);
        map.put("interfaceFlag", "false");
        //服务名
        String serviceName = "APPLY_NOTICE_SERVICE";
        //服务id
        String serviceId = "sendSendSmyNotify";
        //发送报文
        String res;
        //接收报文
        String receiveResult;
        //存放发送内容
        try {
            //报文拼接
            res = TokenInterface.spellMessageBody(sqcwContentMsg, serviceName, serviceId);
            log.info("发送萨摩耶状态同步接口,推送中转贷款状态报文：" + res);
            Thread.sleep(1000);
            //发送报文
            receiveResult = send(res);
            log.debug("发送萨摩耶状态同步接口,推送结束,返回报文：" + receiveResult);
            JSONObject jsonObject = JSONObject.parseObject(receiveResult);
            String ststusInterface = jsonObject.getString("STATUS");
            String msgInterface = jsonObject.getString("MSG");
            if ("Y".equals(ststusInterface)) {
                JSONObject smyMsg = JSONObject.parseObject(msgInterface);
                String smyCode = smyMsg.getString("code");
                if ("000000".equals(smyCode)) {
                    map.put("interfaceFlag", "true");
                }
            }
            map.put("reveiveMsg", receiveResult);
        } catch (Exception e) {
            map.put("reveiveMsg", e.getMessage());
            log.error("发送萨摩耶状态同步接口异常信息：[{}]", e.getMessage());
        }
        return map;
    }

    /**
     * 合作渠道=14-萨摩耶的线索没有进件提交-30天超期定时任务
     */
    public void smyNotSubmitSchedule() throws Exception {
        log.info("合作渠道=14-萨摩耶的线索没有进件提交-30天超期定时任务开始：[{}]", DateUtil.formatToYYYYMMDDHHMMSS2(new Date()));
        //获取30天前的日期
        String date = DateUtil.getNowBeforeDate(30);
        log.info("合作渠道=14-萨摩耶的线索没有进件提交-30天超期定时任务，实际执行的创建时间[{}]", date);
        //合作渠道
        int channelPartner = 14;
        //筛选线索进件时间超过30天、没有进件提交线索
        List<String> applyNums = splBussinessbasicMapper.selectSmyNotSubmitList(date, channelPartner);
        log.info("筛选线索进件时间超过30天、没有进件提交线索:[{}]", applyNums);
        if (applyNums.size() > 0) {
            for (String applyNum : applyNums) {
                Integer loanId = 0;
                Integer branchLoanId = 0;
                Integer loanStates = 0;
                //查询该线索进件业务的当前贷款状态,若有多笔贷款取贷款状态最大的该笔
                List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, channelPartner);
                if (maxLoanStatus != null && maxLoanStatus.size() > 0) {
                    //如果有多条，则取第一条
                    Map<String, Object> maxStatus = maxLoanStatus.get(0);
                    //sapdcslas表中汇总贷款id
                    loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
                    //sapdcslas表中状态
                    loanStates = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("status"));
                    if (loanStates > 0) {
                        log.info("合作渠道-14萨摩耶的线索没有进件提交-30天超期定时任务，系统取消处理，loanId：[{}]；当前状态：[{}]", loanId, loanStates);
                        //其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
                        loanAppStatusService.updateState(loanId, -15, loanStates);
                        loanAppStatusService.saveNode(loanId, -15, loanStates, 0, "线索超期取消", 4);
                        //添加节点
                        loanAppStatusService.saveNode(loanId, -15, -15, 0, "线索超期取消", 4);
                    }
                } else {
                    List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, channelPartner);
                    if (basicEntity != null && basicEntity.size() > 0) {
                        //获取第一条
                        BusinessBasicEntity businessBasicEntity = basicEntity.get(0);
                        branchLoanId = businessBasicEntity.getBranchLoanId();
                        //受理阶段做取消操作
                        splBussinessbasicMapper.updateSplBussinessBasic(2, "线索超期取消", businessBasicEntity.getBranchLoanId());
                    }
                }
                sendSmyStatusNotice(loanId, "302", branchLoanId);
            }
        } else {
            log.info("合作渠道=14-萨摩耶的线索-30天超期定时任务,查询无数据，不需要处理");
        }
    }

    /**
     * 合作渠道=14-萨摩耶的线索已经提交过，但是未资方审批通过-30天超期定时任务
     */
    public void smyNotBankApproveSchedule() {
        log.info("合作渠道=14-萨摩耶的线索已经提交过，但是未资方审批通过-30天超期定时任务开始：[{}]", DateUtil.formatToYYYYMMDDHHMMSS2(new Date()));
        //获取30天前的日期
        String date = DateUtil.getNowBeforeDate(30);
        log.info("合作渠道=14-萨摩耶的线索已经提交过，但是未资方审批通过-30天超期定时任务，实际执行的创建时间[{}]", date);
        //合作渠道
        int channelPartner = 14;
        //筛选线索进件时间超过30天、已经提交过，但是未资方审批通过的线索
        List<String> applyNums = splBussinessbasicMapper.selectSmyNotBankApproveList(date, channelPartner);
        log.info("筛选线索进件时间超过30天、萨摩耶的线索已经提交过，但是未资方审批通过线索:[{}]", applyNums);
        if (applyNums.size() > 0) {
            for (String applyNum : applyNums) {
                Integer loanId;
                Integer branchLoanId = 0;
                Integer loanStates;
                //查询该线索进件业务的当前贷款状态,若有多笔贷款取贷款状态最大的该笔
                List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, channelPartner);
                if (maxLoanStatus != null && maxLoanStatus.size() > 0) {
                    //如果有多条，则取第一条
                    Map<String, Object> maxStatus = maxLoanStatus.get(0);
                    //sapdcslas表中汇总贷款id
                    loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
                    //sapdcslas表中状态
                    loanStates = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("status"));
                    //查询是否银行审批通过
                    Map isBankMap = approveLogic.CheckOrIsBack(loanId);
                    String bankFlag = ConvertionUtil.getSimpleStringWithNull(isBankMap.get("back"));
                    log.info("筛选线索进件时间超过30天、萨摩耶的线索已经提交过，但是未资方审批通过线索,bankFlag:[{}]", bankFlag);
                    if (!"2".equals(bankFlag)) {
                        log.info("筛选线索进件时间超过30天、萨摩耶的线索已经提交过，但是未资方审批通过线索,loanId:[{}]", loanId);
                        sendSmyStatusNotice(loanId, "P054", branchLoanId);
                    }
                    if (loanStates > 0) {
                        log.info("合作渠道-14萨摩耶，超期定时任务，系统取消处理，loanId：[{}]；当前状态：[{}]", loanId, loanStates);
                        //其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
                        loanAppStatusService.updateState(loanId, -15, loanStates);
                        loanAppStatusService.saveNode(loanId, -15, loanStates, 0, "线索超期取消", 4);
                        //添加节点
                        loanAppStatusService.saveNode(loanId, -15, -15, 0, "线索超期取消", 4);
                        //发送资方撤销接口
                        sendBankCancelInterface(loanId, "线索超期取消", "萨摩耶");
                    }
                }
            }
        } else {
            log.info("合作渠道=14-萨摩耶的线索已经提交过，但是未资方审批通过定时任务,查询无数据，不需要处理");
        }
    }

    /**
     * 加密防撞表-定时任务做数据同步
     */
    public ResultDto encryptionPhoneSchedule(String param) {
        //16        蚂蚁金服
        //23        新京东金融
        //28        靠谱金服
        //35        京东金条
        //36        京东金条-自营
        List<String> channelPartnerListNotNeedPush = Arrays.asList("16", "23", "28", "35","36","999");
        ResultDto resultDto = new ResultDto(0, "处理失败");
        log.info("加密防撞表-数据同步定时任务开始：[{}]", DateUtil.formatToYYYYMMDDHHMMSS2(new Date()));
        try {
            long millisecond = 330000L;
            if (StringUtil.isNotEmpty(param)) {
                millisecond = Long.parseLong(param) * 60000;
            }
            //查询符合条件的手机号集合
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            log.info("当前时间：" + simpleDateFormat.format(date));
            Date beforeDate = new Date(date.getTime() - millisecond);
            String searchTime = simpleDateFormat.format(beforeDate);
            log.info("加密防撞表-定时任务最终执行的时间：[{}]", simpleDateFormat.format(beforeDate));
            List<EncryptionPhoneCheck> encryptionPhoneChecks = encryptionPhoneCheckMapper.getAllCustomerPhoneByTime(searchTime);
            if (!encryptionPhoneChecks.isEmpty()) {
                for (EncryptionPhoneCheck encryptionPhoneCheck : encryptionPhoneChecks) {
                    String md5Phone = getMD5(encryptionPhoneCheck.getOriginalPhoneNumber());
                    String sha256Phone = SHA256Encryptor.encrypt(encryptionPhoneCheck.getOriginalPhoneNumber());
                    encryptionPhoneCheck.setMd5PhoneNumber(md5Phone);
                    encryptionPhoneCheck.setSha256PhoneNumber(sha256Phone);
                    encryptionPhoneCheckRepository.saveEncryptionPhoneCheckInfo(encryptionPhoneCheck);
                    if (!channelPartnerListNotNeedPush.contains(encryptionPhoneCheck.getChannelPartner())) {
                        sender.encryptionPhonePush(encryptionPhoneCheck.getOriginalPhoneNumber());
                    }
                }
            } else {
                log.info("加密防撞表-数据同步定时任务,查询无数据，不需要处理");
            }
            resultDto = new ResultDto(1, "处理成功");
        } catch (Exception e) {
            log.error("加密防撞表-数据同步定时任务,异常：[{}]", e.getMessage());
            //失败发送邮件
            if (StringUtil.isEmpty(param)) {
                //定义换行符
                String newLine = "\n";
                String emailContent = "各位好：" + newLine +
                        "以下加密防撞表-数据同步定时任务失败，请及时处理：" + newLine +
                        "当前时间：" + DateUtil.formatToYYYYMMDDHHMMSS2(new Date()) + "；" +
                        "错误信息：" + e.getMessage();

                sendEmailMessage.sendEmail("加密防撞表-数据同步定时任务失败", emailContent, "加密防撞表-数据同步定时任务发送失败邮件推送");
            }
        }
        log.info("加密防撞表-数据同步定时任务,执行完成");
        return resultDto;
    }

    /**
     * 对字符串md5加密(小写+字母)
     *
     * @param str 传入要加密的字符串
     * @return MD5加密后的字符串
     */
    public String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16).toLowerCase();
        } catch (Exception e) {
            log.error("对字符串md5加密(小写+字母)异常：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 运维发送萨摩耶状态推送接口
     *
     * @param operationSendSmyStatusDto
     * @return
     */
    public ResultDto sendOperationSendSmyStatusInterface(OperationSendSmyStatusDto operationSendSmyStatusDto) throws Exception {
        log.info("运维发送萨摩耶状态推送接口,入参：operationSendSmyStatusDto：[{}]", operationSendSmyStatusDto);
        ResultDto resultDto = new ResultDto(0, "发送失败");
        int result = 0;
        //获取订单当前最大状态
        Map<String, Object> smyOrderStatusNow = getSmyOrderStatusNow(operationSendSmyStatusDto.getApplyNumber());
        Integer loanId = ConvertionUtil.getSimpleIntegerWithNull(smyOrderStatusNow.get("loanId"));
        Integer branchLoanId = ConvertionUtil.getSimpleIntegerWithNull(smyOrderStatusNow.get("branchLoanId"));
        if (loanId == 0 && branchLoanId == 0) {
            log.info("运维发送萨摩耶状态推送接口，根据申请编号未查询到信息，applyNum:[{}]", operationSendSmyStatusDto.getApplyNumber());
            return new ResultDto(0, "根据申请编号未查询到信息");
        }
        //获取发送参数
        String smyContentMsg = getSendSmyContent(loanId, operationSendSmyStatusDto.getStatus(), branchLoanId);
        //发送接口
        Map<String, String> sendResult = sendSmyStstusInterface(smyContentMsg);
        String interfaceFlag = sendResult.get("interfaceFlag");
        String reveiveMsg = sendResult.get("reveiveMsg");
        if ("true".equals(interfaceFlag)) {
            //更新状态表
            splBussinessbasicMapper.updateMaxActionNum(operationSendSmyStatusDto.getApplyNumber(), operationSendSmyStatusDto.getStatus());
            resultDto = new ResultDto(1, "同步成功");
            result = 1;
        } else {
            resultDto = new ResultDto(0, reveiveMsg);
        }
        //保存接口履历
        if (0 == branchLoanId) {
            allSaveRecord.saveRecord(result, loanId, "萨摩耶回调提交接口", "发送", smyContentMsg, reveiveMsg);
        } else {
            allSaveRecord.saveRecordBeforeAccepting(result, branchLoanId, "萨摩耶回调提交接口", "发送", smyContentMsg, reveiveMsg);
        }
        return resultDto;
    }

    /**
     * 运维获取订单当前最大状态
     *
     * @param applyNum
     * @return
     */
    public Map<String, Object> getSmyOrderStatusNow(String applyNum) {
        Map<String, Object> result = new HashMap<>(16);
        try {
            Integer loanId;
            Integer channelPartner = 14;
            //获取当前最大状态
            List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, channelPartner);
            if (maxLoanStatus != null && maxLoanStatus.size() > 0) {
                //如果有多条，则取第一条
                Map<String, Object> maxStatus = maxLoanStatus.get(0);
                //sapdcslas表中汇总贷款id
                loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
                result.put("loanId", loanId);
                result.put("branchLoanId", 0);
            } else {
                //O、W、N、
                //如果sapdcslas表中状态为空，则查询businessBasic表中的状态
                List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, channelPartner);
                if (basicEntity != null && basicEntity.size() > 0) {
                    //获取第一条
                    BusinessBasicEntity businessBasicEntity = basicEntity.get(0);
                    //Business表中状态
                    result.put("loanId", 0);
                    result.put("branchLoanId", businessBasicEntity.getBranchLoanId());
                }
            }
        } catch (Exception e) {
            log.info("运维获取订单当前最大状态,异常：[{}]；applyNum：[{}]", e.getMessage(), applyNum);
        }
        return result;
    }

    /**
     * 发送小米贷款超市线索状态通知接口
     *
     * @param clueStateNotify
     * @return
     */
    public Map<String, String> sendMiClueStateNotify(ClueStateNotifyDTO clueStateNotify, LoanId loanId) {
        Map<String, String> map = new HashMap<>(16);
        map.put("interfaceFlag", "false");
        //服务名
        String serviceName = "APPLY_NOTICE_SERVICE";
        //服务id
        String serviceId = "sendMiNotify";
        //发送报文
        String res = "";
        //接收报文
        String receiveResult = "";
        //存放发送内容
        try {
            //报文拼接
            res = TokenInterface.spellMessageBody(JSON.toJSONString(clueStateNotify, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullListAsEmpty), serviceName, serviceId);
            log.info("发送小米贷款超市状态同步接口,推送中转贷款状态报文：" + res);
            Thread.sleep(1000);
            //发送报文
            receiveResult = send(res);
            log.debug("发送小米贷款超市状态同步接口,推送结束,返回报文：" + receiveResult);
            JSONObject jsonObject = JSONObject.parseObject(receiveResult);
            String ststusInterface = jsonObject.getString("STATUS");
            String msgInterface = jsonObject.getString("MSG");
            if ("Y".equals(ststusInterface)) {
                JSONObject miMsg = JSONObject.parseObject(msgInterface);
                String miCode = miMsg.getString("code");
                if ("0".equals(miCode)) {
                    map.put("interfaceFlag", "true");
                } else {
                    map.put("reveiveMsg", miMsg.getString("desc"));
                }
            } else {
                map.put("reveiveMsg", receiveResult);
            }

        } catch (Exception e) {
            map.put("reveiveMsg", e.getMessage());
            log.error("发送小米贷款超市状态同步接口异常信息：[{}]", e.getMessage());
        }

        int resFlag = "true".equals(map.get("interfaceFlag")) ? 1 : 0;

        if (loanId.get() == 0) {
            LambdaQueryWrapper<BusinessBasicEntity> wrapper = new QueryWrapper<BusinessBasicEntity>()
                    .lambda()
                    .eq(BusinessBasicEntity::getApplyNum, clueStateNotify.getApplyId().toString());

            List<BusinessBasicEntity> entities = splBussinessbasicMapper.selectList(wrapper);
            for (BusinessBasicEntity entity : entities) {
                allSaveRecord.saveRecordBeforeAccepting(resFlag, entity.getBranchLoanId(), "小米贷款超市状态同步接口", "发送", res, receiveResult);
            }
        } else {
            allSaveRecord.saveRecord(resFlag, loanId.get(), "小米贷款超市状态同步接口", "发送", res, receiveResult);
        }
        return map;
    }

    public void pushLoanStatusToChannel(int loanId, int currentStatus, int loanStatus, int approveResult, String comment) {
        log.info("订单状态变化推送触发点{}-{}-{}-{}", loanId, currentStatus, loanStatus, approveResult);
        if (currentStatus > 0 && (loanStatus == -10 || loanStatus == -9)) {
            //客户经理取消
            this.releaseLoanStatusChange(loanId, "取消", loanStatus);
        } else if (currentStatus > 0 && loanStatus == -15 && !"预审批自动拒绝".equals(comment)) {
            //因无法区分超期取消-15及预审自动拒绝-15故暂时已固定传参的备注作为判断条件
            //超期取消
            this.releaseLoanStatusChange(loanId, "取消", loanStatus);
        } else if (currentStatus > 0 && loanStatus == -16) {
            //系统取消
            this.releaseLoanStatusChange(loanId, "取消", loanStatus);
        } else if (currentStatus > 0 && loanStatus == -206){
            //面签前取消
            this.releaseLoanStatusChange(loanId, "取消", loanStatus);
        } else if (currentStatus > 0 && currentStatus <= 70 && loanStatus > 70) {
            //我司/资方审批通过
            this.releaseLoanStatusChange(loanId, "审批中", loanStatus);
//        } else if (loanStatus == 110 || loanStatus == 130 || loanStatus == 160 || loanStatus == 135 || loanStatus == 138) {
            //我司资方审批通过
//            this.releaseLoanStatusChange(loanId, "审批通过");
        } else if (currentStatus == 160 && (loanStatus == 170 || loanStatus == 200 || loanStatus == 205 || loanStatus == 210)) {
            //160-110 approveResult == 2
            //160-205 民生路由管控
            //资方审批通过
            this.releaseLoanStatusChange(loanId, "审批通过", loanStatus);
        } else if (currentStatus == 160 && (loanStatus == 110 || loanStatus == 130) && approveResult == 2) {
            //160-110 approveResult == 2
            //资方审批通过
            this.releaseLoanStatusChange(loanId, "审批通过", loanStatus);
        } else if (currentStatus > 0 && loanStatus == -160 || loanStatus == -135) {
            //资方审批拒绝
            this.releaseLoanStatusChange(loanId, "审批拒绝", loanStatus);
        } else if (currentStatus > 0 && (loanStatus == -130 || loanStatus == -110 || loanStatus == -200 || loanStatus == -90)) {
            //我司审批拒绝
            this.releaseLoanStatusChange(loanId, "审批拒绝", loanStatus);
//        } else if (loanStatus == 210) {
            //开始面签 todo 需嵌套在业务流程中
//            this.releaseLoanStatusChange(loanId,"开始面签","");
//        } else if (currentStatus == 210 && (loanStatus == 200 || loanStatus == 130 || loanStatus == 111 || loanStatus == 70)) {
            //面签退回
//            this.releaseLoanStatusChange(loanId,"面签退回","");
        } else if (loanStatus == -180) {
            // BD分配拒绝
            this.releaseLoanStatusChange(loanId, "审批拒绝", loanStatus);
        } else if (loanStatus == -210) {
            //面签拒绝
            this.releaseLoanStatusChange(loanId, "面签拒绝", loanStatus);
        } else if (loanStatus == 230) {
//            面签完成
            this.releaseLoanStatusChange(loanId,"面签完成",loanStatus);
        } else if (loanStatus == -230) {
            //放款申请拒绝
            this.releaseLoanStatusChange(loanId, "放款申请拒绝", loanStatus);
        } else if (loanStatus == 235) {
            //放款申请通过
            this.releaseLoanStatusChange(loanId, "放款申请通过", loanStatus);
//        } else if (currentStatus == 235 && (loanStatus == 239 || loanStatus == 240 || loanStatus == 241 || loanStatus == 236)) { //240-信用卡录入  241-待放款确认
//            if (approveResult == 1) {
//                this.releaseLoanStatusChange(loanId, "放款审批通过");
//            }
        } else if (currentStatus == 235 && loanStatus == -235) {
            //放款审批拒绝
            this.releaseLoanStatusChange(loanId, "放款审批拒绝", loanStatus);
        } else if (currentStatus == 235 && loanStatus == 242) {
            //放款失败
//            this.releaseLoanStatusChange(loanId, "放款失败");
//        } else if (loanStatus == 245){
//            this.releaseLoanStatusChange(loanId, "放款成功");
        } else if (loanStatus == 310 || loanStatus == 320 || loanStatus == 330) {
            //放款申请通过
            this.releaseLoanStatusChange(loanId, "已结清", loanStatus);
//        } else if (currentStatus == 235 && (loanStatus == 239 || loanStatus == 240 || loanStatus == 241 || loanStatus == 236)) { //240-信用卡录入  241-待放款确认
//            if (approveResult == 1) {
//                this.releaseLoanStatusChange(loanId, "放款审批通过");
//            }
        }
    }

}

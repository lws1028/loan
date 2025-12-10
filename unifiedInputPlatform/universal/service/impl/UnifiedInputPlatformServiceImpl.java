package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universal.service.impl;

import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.business.clue.service.LoanStatePushService;
import com.zlhj.commonLoan.domain.cule.ClueNumber;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.commonLoan.util.StringUtil;
import com.zlhj.entity.BusinessBasicEntity;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.jd.vo.ClueApproveInfoDto;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.mq.provider.Sender;
import com.zlhj.unifiedInputPlatform.ant.service.AntBackInfoService;
import com.zlhj.unifiedInputPlatform.bj58.dto.ClueInformationRealTime;
import com.zlhj.unifiedInputPlatform.bj58.service.BJ58Service;
import com.zlhj.unifiedInputPlatform.bj58.utils.BJ58Util;
import com.zlhj.unifiedInputPlatform.universal.service.ClueApproveInfoService;
import com.zlhj.unifiedInputPlatform.universal.service.UnifiedInputPlatformService;
import com.zlhj.unifiedInputPlatform.universal.vo.ChannelStatusPushFailRecordPO;
import com.zlhj.user.vo.ChannelStatusPushFailRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UnifiedInputPlatformServiceImpl implements UnifiedInputPlatformService {
    @Autowired
    private BJ58Service bj58Service;

    @Autowired
    private ChannelStatusPushFailRecordRepository channelStatusPushFailRecordRepository;

    private final SplBussinessbasicMapper splBussinessbasicMapper;
    @Autowired
    private AntBackInfoService antBackInfoService;
    @Autowired
    private LoanStatePushService loanStatePushService;
    @Autowired
    private ClueApproveInfoService clueApproveInfoService;
    @Autowired
    private Sender sender;

    public UnifiedInputPlatformServiceImpl(SplBussinessbasicMapper splBussinessbasicMapper) {
        this.splBussinessbasicMapper = splBussinessbasicMapper;
    }

    @Override
    public LoanId getLoanIdByClueSystem(ClueNumber clueNumber, Integer splChannelPattern) {
        DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderIdOrBoId(
                clueNumber.getNumber(),
                splChannelPattern
        );

        if (directCustomerStatusDto == null || directCustomerStatusDto.getSplLoanId() == null) {
            return null;
        }

        return new LoanId(directCustomerStatusDto.getSplLoanId());
    }

    @Override
    public int preApproveRealTimeInteraction(LoanId loanId) {

        SplBussinessBasicObject splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId.get());
        if (splBussinessBasicObject == null) {
            log.info("实时交互触发接口，没有查询到进件贷款基础信息表信息, 参数：{}", loanId.get());
            return 0;
        }
        if (splBussinessBasicObject.getChannelSource() != null) {
            if (splBussinessBasicObject.getChannelSource() == 13) {
                bj58Service.bj58RealTimeInteraction(
                        ClueInformationRealTime.createRealTime(loanId.get(), BJ58Util.loanStatusTranslate("预审通过"), null, null, null)
                );
            } else if (16 == splBussinessBasicObject.getChannelSource()) {
                //LoanStatusChangeEnum.PRE_PASS 该状态埋点调整到规则引擎
                // com/zlhj/electronicCredit/interfaces/impl/ElectronicCreditInterfaceServiceImpl.java:1653
//                this.realTimeInteraction(new LoanStatePushToClueDTO(splBussinessBasicObject.getApplyNum(), loanId.get(), LoanStatusChangeEnum.PRE_PASS.getValue(), splBussinessBasicObject.getChannelSource()));
                //信息回传埋点  保留
                antBackInfoService.backInfo(splBussinessBasicObject);
            }


        }
        return 0;
    }

    @Override
    public void preApproveRefuseRealTimeInteraction(LoanId loanId) {
        SplBussinessBasicObject splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId.get());
        if (splBussinessBasicObject == null) {
            log.info("实时交互触发接口，没有查询到进件贷款基础信息表信息, 参数：{}", loanId.get());
            return;
        }
        if (splBussinessBasicObject.getChannelSource() != null) {
            if (splBussinessBasicObject.getChannelSource() == 13) {
                bj58Service.bj58RealTimeInteraction(
                        ClueInformationRealTime.createRealTime(loanId.get(), BJ58Util.loanStatusTranslate("预审拒绝"), null, null, null)
                );
            } else if (16 == splBussinessBasicObject.getChannelSource()) {
                //LoanStatusChangeEnum.PRE_PASS 该状态埋点调整到规则引擎
                // com/zlhj/electronicCredit/interfaces/impl/ElectronicCreditInterfaceServiceImpl.java:1653
//                this.realTimeInteraction(new LoanStatePushToClueDTO(splBussinessBasicObject.getApplyNum(), loanId.get(), LoanStatusChangeEnum.PRE_REJECT.getValue(), splBussinessBasicObject.getChannelSource()));
                //信息回传埋点  保留
                antBackInfoService.backInfo(splBussinessBasicObject);
            }
        }
    }

    @Override
    public void LendingApprovePassRealTimeInteraction(LoanId loanId) {
        SplBussinessBasicObject splBussinessBasicObject = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId.get());
        if (splBussinessBasicObject == null) {
            log.info("实时交互触发接口，没有查询到进件贷款基础信息表信息, 参数：{}", loanId.get());
            return;
        }
        if (splBussinessBasicObject.getChannelSource() != null) {
            if (splBussinessBasicObject.getChannelSource() == 13) {
                //发送58外部渠道
                bj58Service.bj58RealTimeInteraction(
                        ClueInformationRealTime.createRealTime(loanId.get(), BJ58Util.loanStatusTranslate("放款审批通过"), null, null, null)
                );
            } else if (splBussinessBasicObject.getChannelSource() == 16) {
                this.realTimeInteraction(new LoanStatePushToClueDTO(splBussinessBasicObject.getApplyNum(), loanId.get(), LoanStatusChangeEnum.LEND_APPROVE_PASS.getValue(), splBussinessBasicObject.getChannelSource()));
            } else if (splBussinessBasicObject.getChannelSource() == 23) {
                this.realTimeInteraction(new LoanStatePushToClueDTO(splBussinessBasicObject.getApplyNum(), loanId.get(), LoanStatusChangeEnum.LEND_APPROVE_PASS.getValue(), splBussinessBasicObject.getChannelSource(), "JD_NOTICE_AUTO_FINANCE"));
            } else if (splBussinessBasicObject.getChannelSource() == 24) {
                this.realTimeInteraction(new LoanStatePushToClueDTO(splBussinessBasicObject.getApplyNum(), loanId.get(), LoanStatusChangeEnum.LEND_APPROVE_PASS.getValue(), splBussinessBasicObject.getChannelSource(), "JD_NOTICE_CAR_LIFE"));
            } else if (splBussinessBasicObject.getChannelSource() == 30) {
                this.realTimeInteraction(new LoanStatePushToClueDTO(splBussinessBasicObject.getApplyNum(), loanId.get(), LoanStatusChangeEnum.LEND_APPROVE_PASS.getValue(), splBussinessBasicObject.getChannelSource(), ""));
            }
        }
    }


    @Override
    public void realTimeInteraction(LoanStatePushToClueDTO loanStatePushToClueDTO) {
        try {
            if (loanStatePushToClueDTO.getChannelPartner() == 24
                    || loanStatePushToClueDTO.getChannelPartner() == 23
                    || loanStatePushToClueDTO.getChannelPartner() == 35
                    || loanStatePushToClueDTO.getChannelPartner() == 36
                    || loanStatePushToClueDTO.getChannelPartner() == 30) {
                DirectCustomerStatusDto directCustomerStatus = splBussinessbasicMapper.selectStatusByOrderId(loanStatePushToClueDTO.getApplyNumber(), loanStatePushToClueDTO.getChannelPartner());
                if (directCustomerStatus != null) {
                    splBussinessbasicMapper.updateDirectCustomerStrStatus("", loanStatePushToClueDTO.getEventType(), Timestamp.valueOf(LocalDateTime.now()), loanStatePushToClueDTO.getApplyNumber(), loanStatePushToClueDTO.getLoanId());
                } else {
                    DirectCustomerStatusDto directCustomerStatusDto = new DirectCustomerStatusDto();
                    directCustomerStatusDto.setSplApplyNum(loanStatePushToClueDTO.getApplyNumber());
                    log.info("存储直客表channelPartner: {}", loanStatePushToClueDTO.getChannelPartner());
                    directCustomerStatusDto.setSplChannelPartner(loanStatePushToClueDTO.getChannelPartner());
                    directCustomerStatusDto.setSplCreatetime(new Timestamp(System.currentTimeMillis()));
                    directCustomerStatusDto.setSplMaxActionNum(loanStatePushToClueDTO.getEventType());
                    splBussinessbasicMapper.insertDirectCustomerStatusInfo(directCustomerStatusDto);
                }
            } else {
                if (loanStatePushToClueDTO.getChannelPartner() == 16) {
                    log.info("蚂蚁初终审合并交互逻辑, 参数：{}", loanStatePushToClueDTO);
                    log.info("实时交互触发接口，进件贷款基础信息表信息, 参数：{}", loanStatePushToClueDTO);
                    ClueApproveInfoDto clueApproveInfo = clueApproveInfoService.getClueApproveInfo(loanStatePushToClueDTO.getApplyNumber());
                    log.info("实时交互触发接口，进件贷款基础信息表信息, 参数：{}", clueApproveInfo);
                    if (clueApproveInfo != null && "1".equals(clueApproveInfo.getInitialFinalJointReview())) {
                        if (LoanStatusChangeEnum.APPROVE_PASS.getValue().equals(loanStatePushToClueDTO.getEventType())) {
                            loanStatePushToClueDTO.setEventType(LoanStatusChangeEnum.MERGE_APPROVE_PASS_T.getValue());
                        } else if (LoanStatusChangeEnum.APPROVE_REJECT.getValue().equals(loanStatePushToClueDTO.getEventType())) {
                            loanStatePushToClueDTO.setEventType(LoanStatusChangeEnum.MERGE_APPROVE_REJECT_T.getValue());
                        }
                    }
                }
                List<BusinessBasicEntity> businessBasicEntities = splBussinessbasicMapper.getBusinessByApplyNum(loanStatePushToClueDTO.getApplyNumber());
                if (businessBasicEntities != null && !businessBasicEntities.isEmpty()) {
                    splBussinessbasicMapper.updateDirectCustomerStrStatus("", loanStatePushToClueDTO.getEventType(), Timestamp.valueOf(LocalDateTime.now()), loanStatePushToClueDTO.getApplyNumber(), loanStatePushToClueDTO.getLoanId());
                }
            }
            loanStatePushService.loanStateChangePush(loanStatePushToClueDTO);
        } catch (Exception e) {
            ChannelStatusPushFailRecordPO channelStatusPushFailRecordPO = new ChannelStatusPushFailRecordPO(loanStatePushToClueDTO.getApplyNumber(), loanStatePushToClueDTO.getEventType(), loanStatePushToClueDTO.getLoanId(), null, loanStatePushToClueDTO.getChannelPartner());
            channelStatusPushFailRecordRepository.insert(channelStatusPushFailRecordPO);
        }
    }


    @Override
    public void channelStatusRePush(String param) {
        List<ChannelStatusPushFailRecordPO> channelStatusPushFailRecordPOS;
        if (StringUtil.isNotEmpty(param)) {
            channelStatusPushFailRecordPOS = channelStatusPushFailRecordRepository.searchFailRecordByApplyNumOrderByTimeDesc(param);
        } else {
            channelStatusPushFailRecordPOS = channelStatusPushFailRecordRepository.searchFailRecordOverTenMinOrderByTimeDesc();
        }

        Map<String, List<ChannelStatusPushFailRecordPO>> collect = channelStatusPushFailRecordPOS.stream().collect(Collectors.groupingBy(po -> po.getApplyNum()));

        for (Map.Entry<String, List<ChannelStatusPushFailRecordPO>> entryUser : collect.entrySet()) {
            String applyNum = entryUser.getKey();
            List<ChannelStatusPushFailRecordPO> statusPushFailRecordPOS = entryUser.getValue();
            log.info("渠道进件状态推送失败补发定时,申请编号：{} 开始", applyNum);
            try {
                statusPushFailRecordPOS.forEach(channelStatusPushFailRecordPO -> {
                    if (channelStatusPushFailRecordPO.getChannelPartner() == 16) {
                        this.realTimeInteraction(new LoanStatePushToClueDTO(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getBranchLoanId(), channelStatusPushFailRecordPO.getPushActionNum(), channelStatusPushFailRecordPO.getChannelPartner()));
                        channelStatusPushFailRecordRepository.updateChannelStatusPushFailRecordSuccess(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getPushActionNum());
                    } else if (channelStatusPushFailRecordPO.getChannelPartner() == 23) {
                        this.realTimeInteraction(new LoanStatePushToClueDTO(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getBranchLoanId(), channelStatusPushFailRecordPO.getPushActionNum(), channelStatusPushFailRecordPO.getChannelPartner(), "JD_NOTICE_AUTO_FINANCE"));
                        channelStatusPushFailRecordRepository.updateChannelStatusPushFailRecordSuccess(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getPushActionNum());
                    } else if (channelStatusPushFailRecordPO.getChannelPartner() == 24) {
                        this.realTimeInteraction(new LoanStatePushToClueDTO(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getBranchLoanId(), channelStatusPushFailRecordPO.getPushActionNum(), channelStatusPushFailRecordPO.getChannelPartner(), "JD_NOTICE_CAR_LIFE"));
                        channelStatusPushFailRecordRepository.updateChannelStatusPushFailRecordSuccess(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getPushActionNum());
                    } else if (channelStatusPushFailRecordPO.getChannelPartner() == 35 || channelStatusPushFailRecordPO.getChannelPartner() == 36) {
                        this.realTimeInteraction(new LoanStatePushToClueDTO(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getBranchLoanId(), channelStatusPushFailRecordPO.getPushActionNum(), channelStatusPushFailRecordPO.getChannelPartner(), "JDJT_NOTICE"));
                        channelStatusPushFailRecordRepository.updateChannelStatusPushFailRecordSuccess(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getPushActionNum());
                    } else if (channelStatusPushFailRecordPO.getChannelPartner() == 30) {
                        this.realTimeInteraction(new LoanStatePushToClueDTO(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getBranchLoanId(), channelStatusPushFailRecordPO.getPushActionNum(), channelStatusPushFailRecordPO.getChannelPartner(), ""));
                        channelStatusPushFailRecordRepository.updateChannelStatusPushFailRecordSuccess(channelStatusPushFailRecordPO.getApplyNum(), channelStatusPushFailRecordPO.getPushActionNum());
                    }
                });
            } catch (Exception e) {
                log.info("渠道进件状态推送失败补发定时,申请编号：{} 异常{}", applyNum, e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void prePassPushTelemarketing(String boId, LoanStatusChangeEnum loanStatusChangeEnum) {
        ClueApproveInfoDto clueApproveInfo = clueApproveInfoService.getClueApproveInfo(boId);
        if (clueApproveInfo != null) {
            sender.prePassPush(new LoanStatePushToClueDTO(boId, null, loanStatusChangeEnum.getValue(), clueApproveInfo.getIncomeProofNeeded(), clueApproveInfo.getInitialFinalJointReview(), 16));
        } else {
            sender.prePassPush(new LoanStatePushToClueDTO(boId, null, loanStatusChangeEnum.getValue(), 16));
        }
    }

}

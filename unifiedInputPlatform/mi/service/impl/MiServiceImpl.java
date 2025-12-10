package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.service.impl;

import com.apollo.alds.util.DAOUtil;
import com.zlhj.commonLoan.business.jjyh.service.LoanAppStatusService;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.entity.BusinessBasicEntity;
import com.zlhj.externalChannel.InterfaceDatum;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.externalChannel.vo.SplBussinessBasicObjectFactory;
import com.zlhj.externalChannel.vo.SplUserInfoObject;
import com.zlhj.externalChannel.vo.SplUserInfoObjectFactory;
import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.infrastructure.message.publisher.ClueApplicationPublisher;
import com.zlhj.infrastructure.repository.SmyLoanCityConfigurationRepository;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.mapper.TikTokMapper;
import com.zlhj.unifiedInputPlatform.LoanStatusInteraction;
import com.zlhj.unifiedInputPlatform.mi.dto.CarApplyDTO;
import com.zlhj.unifiedInputPlatform.mi.dto.ClueStateNotifyDTO;
import com.zlhj.unifiedInputPlatform.mi.dto.ClueStateNotifyDTOFactory;
import com.zlhj.unifiedInputPlatform.mi.service.MiSendEmailService;
import com.zlhj.unifiedInputPlatform.mi.service.MiService;
import com.zlhj.unifiedInputPlatform.mi.service.SqlInterfaceService;
import com.zlhj.unifiedInputPlatform.smy.entity.SmyLoanCityConfiguration;
import com.zlhj.user.vo.MultipleLoanObject;
import com.zlhj.user.vo.MultipleLoanRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@AllArgsConstructor
@Slf4j
@Service
public class MiServiceImpl implements MiService {

    private final SplBussinessbasicMapper splBussinessbasicMapper;
    private final SqlInterfaceService sqlInterfaceService;
    private final InterfaceDatum interfaceDatum;
    private final MultipleLoanRepository multipleLoanRepository;
    private final LoanStatusInteraction loanStatusInteraction;
    private final MiSendEmailService miSendEmailService;
    private final LoanAppStatusService loanAppStatusService;
    private final SmyLoanCityConfigurationRepository smyLoanCityConfigurationRepository;
    private final TikTokMapper tikTokMapper;
    private final ClueApplicationPublisher clueApplicationPublisher;

    @Override
    public void clueCarApply(CarApplyDTO carApplyDTO) {
        this.clueApplicationPublisher.cluePhoneFiltered(carApplyDTO.getUserInfo().getMobile(), "15");
        carApplyDTO.verify();
        DirectCustomerStatusDto directCustomer = splBussinessbasicMapper.selectStatusByOrderId(String.valueOf(carApplyDTO.getApplyId()), 5);
        if (directCustomer != null){
            throw new IllegalArgumentException("申请编号重复提交");
        }

        SmyLoanCityConfiguration smyLoanCityConfiguration = smyLoanCityConfigurationRepository.getMiLoanCityConfiguration(
                carApplyDTO.getUserInfo().getAddress());
        if (smyLoanCityConfiguration == null) {
            throw new IllegalArgumentException("城市名称:"+ carApplyDTO.getUserInfo().getAddress() + "识别失败");
        }
        //参数转换、保存核心系统数据
        SplBussinessBasicObjectFactory splBusinessFactory = new SplBussinessBasicObjectFactory();
        SplBussinessBasicObject splBussinessBasicObject = splBusinessFactory.create(carApplyDTO,smyLoanCityConfiguration);
        DAOUtil.store(splBussinessBasicObject);

        SplUserInfoObjectFactory splUserFactory = new SplUserInfoObjectFactory();
        SplUserInfoObject splUserInfoObject = splUserFactory.create(
                splBussinessBasicObject.getBranchLoanId(),
                carApplyDTO,
                smyLoanCityConfiguration);
        DAOUtil.store(splUserInfoObject);
        log.info("小米车贷线索信息提交接口,同步记录至spl_bussinessbasic、spl_userinfojd成功，applyId：{}, branchLoanID: {}", carApplyDTO.getApplyId(), splBussinessBasicObject.getBranchLoanId());


        log.info("小米车贷线索信息提交接口,同步电销操作开始，applyId：{}", carApplyDTO.getApplyId());
        TreeMap<String, Object> resMap = new TreeMap<>();
        resMap.put("BRANCHID", splBussinessBasicObject.getBranchLoanId());
        resMap.put("RESULT", 1);
        resMap.put("openId", carApplyDTO.getOpenId());
        resMap.put("MESSAGE", "成功");
        interfaceDatum.synchronizationBusinessOpportunity(resMap);
        log.info("小米车贷线索信息提交接口,同步电销操作结束，applyId：{}", carApplyDTO.getApplyId());

        sqlInterfaceService.saveInterfaceInfo(
                String.valueOf(new ResultDto(0,"成功")),
                splBussinessBasicObject.getBranchLoanId(),
                String.valueOf(carApplyDTO),"小米车抵贷线索");
    }

    @Override
    public void pushCancelClueState(LoanId loanId,Integer loanStatus) {
        SplBussinessBasicObject splBussinessBasic = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId.get());
        try {
            DirectCustomerStatusDto directCustomerStatus = splBussinessbasicMapper.selectoldStatusByLoanId(loanId.get());

            ClueStateNotifyDTOFactory factory = new ClueStateNotifyDTOFactory();
            ClueStateNotifyDTO cancel = factory.cancel(directCustomerStatus, splBussinessBasic);
            Map<String, String> sendResult = loanStatusInteraction.sendMiClueStateNotify(cancel,loanId);
            String interfaceFlag = sendResult.get("interfaceFlag");
            if ("true".equals(interfaceFlag)) {
                //更新状态表
                splBussinessbasicMapper.updateMaxActionNum(splBussinessBasic.getApplyNum(), "2");
                splBussinessbasicMapper.updateDirectCustomer(
                        2,
                        "客户取消进件",
                        -15,
                        loanId.get(),
                        Timestamp.valueOf(LocalDateTime.now()),
                        splBussinessBasic.getApplyNum());
                List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(splBussinessBasic.getApplyNum(), 15);
                if (maxLoanStatus != null && !maxLoanStatus.isEmpty()) {
                    maxLoanStatus.forEach(r -> {
                        Integer id = Integer.valueOf(r.get("loanId").toString());
                        Integer status = Integer.valueOf(r.get("status").toString());
                        //其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
                        loanAppStatusService.updateState(id, -15, status);
                        loanAppStatusService.saveNode(id, -15, status, 0, "线索超期取消", 4);
                        //添加节点
                        loanAppStatusService.saveNode(id, -15, -15, 0, "线索超期取消", 4);
                    });
                }
                List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(splBussinessBasic.getApplyNum(), 15);
                if (basicEntity != null && basicEntity.size() > 0) {
                    basicEntity.forEach(r -> {
                        //受理阶段做取消操作
                        if (r.getLoanState() == 0 || r.getLoanState() == 3){
                            splBussinessbasicMapper.updateSplBussinessBasic(9, "线索超期取消", r.getBranchLoanId());
                        }
                    });
                }
            } else {
                miSendEmailService.send(splBussinessBasic,"已取消");
            }
        } catch (Exception e) {
            log.error("小米线索状态通知(取消)推送异常,{}",e.getMessage(),e);
            miSendEmailService.send(splBussinessBasic,"已取消");
        }
    }

    @Override
    public void pushApprovalRejectionClueState(LoanId loanId,Integer loanStatus) {
        SplBussinessBasicObject splBussinessBasic = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId.get());
        try {
            DirectCustomerStatusDto directCustomerStatus = splBussinessbasicMapper.selectoldStatusByLoanId(loanId.get());
            ClueStateNotifyDTOFactory factory = new ClueStateNotifyDTOFactory();
            ClueStateNotifyDTO approvalRejection = factory.approvalRejection(directCustomerStatus, splBussinessBasic);
            Map<String, String> sendResult = loanStatusInteraction.sendMiClueStateNotify(approvalRejection,loanId);
            String interfaceFlag = sendResult.get("interfaceFlag");
            if ("true".equals(interfaceFlag)) {
                log.info("合作渠道=15-小米贷款超市超期定时任务，系统取消处理，loanId：[{}]；当前状态：[{}]", loanId, loanStatus);
                //更新状态表
                splBussinessbasicMapper.updateDirectCustomer(
                        4,
                        "审批拒绝",
                        loanStatus,
                        loanId.get(),
                        Timestamp.valueOf(LocalDateTime.now()),
                        splBussinessBasic.getApplyNum());
                List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(splBussinessBasic.getApplyNum(), 15);
                if (maxLoanStatus != null && !maxLoanStatus.isEmpty()) {
                    maxLoanStatus.forEach(r -> {
                        Integer id = Integer.valueOf(r.get("loanId").toString());
                        Integer status = Integer.valueOf(r.get("status").toString());
                        //其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
                        loanAppStatusService.updateState(id, -15, status);
                        loanAppStatusService.saveNode(id, -15, status, 0, "线索超期取消", 4);
                        //添加节点
                        loanAppStatusService.saveNode(id, -15, -15, 0, "线索超期取消", 4);
                    });
                }
                List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(splBussinessBasic.getApplyNum(), 15);
                if (basicEntity != null && basicEntity.size() > 0) {
                    basicEntity.forEach(r -> {
                        //受理阶段做取消操作
                        if (r.getLoanState() == 0 || r.getLoanState() == 3){
                            splBussinessbasicMapper.updateSplBussinessBasic(9, "线索超期取消", r.getBranchLoanId());
                        }
                    });
                }
            } else {
                miSendEmailService.send(splBussinessBasic,"审批拒绝");
            }
        } catch (Exception e) {
            log.error("小米线索状态通知(审批拒绝)推送异常,{}",e.getMessage(),e);
            miSendEmailService.send(splBussinessBasic,"审批拒绝");
        }
    }

    @Override
    public void pushApproveClueState(LoanId loanId) {
        SplBussinessBasicObject splBussinessBasic = splBussinessbasicMapper.selectSplBussinessbasicByLoanId(loanId.get());
        try {
            DirectCustomerStatusDto directCustomerStatus = splBussinessbasicMapper.selectoldStatusByLoanId(loanId.get());
            String money = splBussinessbasicMapper.getLoanMoneyBuLoanId(loanId.get());
            MultipleLoanObject loanInfo = multipleLoanRepository.selectByLoanId(loanId.get());
            MultipleLoanObject mainLoanInfo = multipleLoanRepository.getDataByLoanIdAndLoanType(loanId.get(), 2);
            ClueStateNotifyDTOFactory factory = new ClueStateNotifyDTOFactory();
            ClueStateNotifyDTO approve = factory.approve(
                                                    directCustomerStatus,
                                                    splBussinessBasic,
                                                    loanInfo,
                                                    mainLoanInfo,
                                                    money);
            Map<String, String> sendResult = loanStatusInteraction.sendMiClueStateNotify(approve,loanId);
            String interfaceFlag = sendResult.get("interfaceFlag");
            if ("true".equals(interfaceFlag)) {
                //更新状态表
                splBussinessbasicMapper.updateMaxActionNum(splBussinessBasic.getApplyNum(), "3");
            } else {
                miSendEmailService.send(splBussinessBasic,"审批通过");
            }
        } catch (Exception e) {
            log.error("小米线索状态通知(审批通过)推送异常,{}",e.getMessage(),e);
            miSendEmailService.send(splBussinessBasic,"审批通过");
        }
    }

    @Override
    public void pushCancelClueState(String applyNum) {
        try {
            DirectCustomerStatusDto directCustomerStatus = tikTokMapper.getStatusInfoByApplyNum(applyNum);
            ClueStateNotifyDTOFactory factory = new ClueStateNotifyDTOFactory();
            ClueStateNotifyDTO cancel = factory.cancel(directCustomerStatus, applyNum);
            Map<String, String> sendResult = loanStatusInteraction.sendMiClueStateNotify(cancel,new LoanId(0));
            String interfaceFlag = sendResult.get("interfaceFlag");
            if ("true".equals(interfaceFlag)) {
                //更新状态表
                splBussinessbasicMapper.updateMaxActionNum(applyNum, "2");
                splBussinessbasicMapper.updateDirectCustomer2(
                        2,
                        "客户取消进件",
                        -15,
                        Timestamp.valueOf(LocalDateTime.now()),
                        applyNum);
                List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, 15);
                if (maxLoanStatus != null && !maxLoanStatus.isEmpty()) {
                    maxLoanStatus.forEach(r -> {
                        Integer id = Integer.valueOf(r.get("loanId").toString());
                        Integer status = Integer.valueOf(r.get("status").toString());
                        if (id == null || id == 0){
                            return;
                        }
                        //其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
                        loanAppStatusService.updateState(id, -15, status);
                        loanAppStatusService.saveNode(id, -15, status, 0, "线索超期取消", 4);
                        //添加节点
                        loanAppStatusService.saveNode(id, -15, -15, 0, "线索超期取消", 4);
                    });
                }
                List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, 15);
                if (basicEntity != null && basicEntity.size() > 0) {
                    basicEntity.forEach(r -> {
                        //受理阶段做取消操作
                        if (r.getLoanState() == 0 || r.getLoanState() == 3){
                            splBussinessbasicMapper.updateSplBussinessBasic(9, "线索超期取消", r.getBranchLoanId());
                        }
                    });
                }
            } else {
                SplBussinessBasicObject splBussinessBasicObject = new SplBussinessBasicObject();
                splBussinessBasicObject.setApplyNum(applyNum);
                miSendEmailService.send(splBussinessBasicObject,"已取消");
            }
        } catch (Exception e) {
            log.error("小米线索状态通知(取消)推送异常,{}",e.getMessage(),e);
            SplBussinessBasicObject splBussinessBasicObject = new SplBussinessBasicObject();
            splBussinessBasicObject.setApplyNum(applyNum);
            miSendEmailService.send(splBussinessBasicObject,"已取消");
        }
    }

}

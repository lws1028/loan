package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zlhj.commonLoan.business.common.exception.AreaInadmissibleException;
import com.zlhj.commonLoan.business.common.exception.BlackListWarningException;
import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.business.common.exception.ClueResubmissionException;
import com.zlhj.commonLoan.domain.cule.*;
import com.zlhj.unifiedInputPlatform.ant.dto.*;
import com.zlhj.unifiedInputPlatform.ant.dto.assembler.ClueApplyInputAssembler;
import com.zlhj.unifiedInputPlatform.ant.service.AntService;
import com.zlhj.unifiedInputPlatform.ant.service.AntServiceFacade;
import com.zlhj.unifiedInputPlatform.ant.utils.ZlhjToAntBatchNumber;
import com.zlhj.unifiedInputPlatform.mi.service.SqlInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AntServiceFacadeImpl implements AntServiceFacade {

    @Autowired
    private AntService antService;

    @Autowired
    private SqlInterfaceService sqlInterfaceService;

    @Autowired
    private ZlhjToAntBatchNumber zlhjToAntBatchNumber;

    @Override
    public ClueApplyOutput culeApply(ClueApplyInput input) throws ClueResubmissionException, AreaInadmissibleException {

        ClueApplyInputAssembler clueApplyInputAssembler = new ClueApplyInputAssembler();
        ClueApplyOutput output = null;
        BranchLoanId branchLoanId = null;

        try {
            CuleApplyCommand command = clueApplyInputAssembler.toCuleApply(input);
            CuleApplyResult culeApplyResult = antService.culeApply(command);

            branchLoanId = culeApplyResult.getBranchLoanId();
            ZLHJClueNumber zlhjClueNumber = culeApplyResult.getZlhjClueNumber();
            ClueNumber clueNumber = culeApplyResult.getClueNumber();

            output = new ClueApplyOutput()
                    .setApplyNo(clueNumber.getNumber())
                    .setOutApplyNo(zlhjClueNumber.getNumber())
                    .setStatus("1");
            return output;
        } catch (AreaInadmissibleException e) {
            output = new ClueApplyOutput()
                    .setApplyNo(input.getApplyNo())
                    .setStatus("2")
                    .setRefuseMsg("机构对应地区未展业。");
            return output;
        } catch (BlackListWarningException e) {
            output = new ClueApplyOutput()
                    .setApplyNo(input.getApplyNo())
                    .setStatus("2")
                    .setRefuseMsg("黑名单自动拒绝。");
            return output;
        } catch (BusinessException e) {
            output = new ClueApplyOutput()
                    .setApplyNo(input.getApplyNo())
                    .setStatus("2")
                    .setRefuseMsg(e.getMessage());
            return output;
        } finally {
            sqlInterfaceService.saveInterfaceInfo(
                    output != null ? JSONObject.toJSONString(output) : "系统异常",
                    branchLoanId != null ? branchLoanId.getValue() : 0,
                    JSONObject.toJSONString(input),
                    "蚂蚁车抵贷线索进件"
            );
        }
    }

    @Override
    public ClueStatusNotifyDTO queryClue(AntQueryCluePreDTO applyNo) {
        return antService.queryClue(applyNo);
    }

    @Override
    public List<AntRepaymentPlanDTO> queryRepayPlan(String applyNo) {
        return antService.queryRepayPlan(new ClueNumber(applyNo));
    }

    @Override
    public ClueStatusNotifyDTO queryClueForOrgApprove(AntQueryCluePreDTO preDTO) {
        return antService.queryClueForOrgApprove(preDTO);
    }

    @Override
    public ClueAdditionalResult queryIncomeProof(Integer loanId) {
        return antService.queryIncomeProof(loanId);
    }

    @Override
    public ClueAdditionalResult queryIncomeProof(String boId) {
        return antService.queryIncomeProof(boId);
    }

}

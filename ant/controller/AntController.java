package com.zlhj.unifiedInputPlatform.ant.controller;

import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.util.JSONUtil;
import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.unifiedInputPlatform.ant.dto.*;
import com.zlhj.unifiedInputPlatform.ant.service.AntService;
import com.zlhj.unifiedInputPlatform.ant.service.AntServiceFacade;
import com.zlhj.unifiedInputPlatform.ant.utils.AntFlowSwitcher;
import com.zlhj.unifiedInputPlatform.mi.service.SqlInterfaceService;
import com.zlhj.unifiedInputPlatform.smy.entity.EncryptionPhoneCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@CrossOrigin
@RequestMapping("/ant")
@RestController
public class AntController {

    @Autowired
    private AntService antService;

    @Autowired
    private AntServiceFacade antServiceFacade;

    @GetMapping("/apply/filter")
    public ResultDto filterClue(@RequestParam("mobile") String mobileSHA256) {

        String interfaceFunction = "蚂蚁金融线索防撞";
        log.info("{} 请求参数 = {}", interfaceFunction, mobileSHA256);

        try {
            Optional<EncryptionPhoneCheck> optional = antService.matchSha256PhoneNumber(mobileSHA256);
            ClueFilterOutput output = ClueFilterOutput.getInstance(optional.orElse(null));
            return ResultDto.searchSuccess(output);
        } catch (BusinessException | IllegalArgumentException e) {
            return ResultDto.searchFail(e.getMessage());
        } catch (Exception e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail("系统异常");
        }
    }

    @PostMapping("/apply")
    public ResultDto applyClue(@RequestBody ClueApplyInput input) {

        String interfaceFunction = "蚂蚁金融线索进件";
        log.info("{} 请求参数 = {}", interfaceFunction, input);

        try {
            ClueApplyOutput output = antServiceFacade.culeApply(input);
            return ResultDto.searchSuccess(output);
        } catch (BusinessException | IllegalArgumentException e) {
            return ResultDto.searchFail(e.getMessage());
        } catch (Exception e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail("系统异常");
        }
    }

    @GetMapping("/apply/query")
    public ResultDto queryClue(AntQueryCluePreDTO queryDTO) {

        String interfaceFunction = "蚂蚁金融线索进件查询";
        log.info("{} 请求参数 = {}", interfaceFunction, queryDTO);

        try {
            ClueStatusNotifyDTO output = antServiceFacade.queryClue(queryDTO);
            log.info("{} 返回参数 = {}", interfaceFunction, JSONUtil.toJsonStringCommon(output));
            return ResultDto.searchSuccess(output);
        } catch (BusinessException | IllegalArgumentException e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail(e.getMessage());
        } catch (Exception e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail("系统异常");
        }
    }
    @GetMapping("/apply/query/pre")
    public ResultDto queryCluePre(AntQueryCluePreDTO preDTO) {

        String interfaceFunction = "蚂蚁金融线索进件查询";
        log.info("{} 请求参数 = {}", interfaceFunction, preDTO);

        try {
            ClueStatusNotifyDTO output = antServiceFacade.queryClueForOrgApprove(preDTO);
            log.info("{} 返回参数 = {}", interfaceFunction, JSONUtil.toJsonStringCommon(output));
            return ResultDto.searchSuccess(output);
        } catch (BusinessException | IllegalArgumentException e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail(e.getMessage());
        } catch (Exception e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail("系统异常");
        }
    }
    @GetMapping("/handleSHA256")
    public ResultDto handleSHA256() {

        String interfaceFunction = "蚂蚁金融线索进件查询";
        log.info("{} 请求参数 = {}");

        try {
            antService.handleSHA256();
            return ResultDto.searchSuccess();
        } catch (BusinessException | IllegalArgumentException e) {
            return ResultDto.searchFail(e.getMessage());
        } catch (Exception e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail("系统异常");
        }
    }

    @GetMapping("/repayPlan/query")
    public ResultDto queryRepayPlan(@RequestParam("applyNo") String applyNo) {

        String interfaceFunction = "蚂蚁金融还款计划信息查询";
        log.info("{} 请求参数 = {}", interfaceFunction, applyNo);

        try {
            List<AntRepaymentPlanDTO> antRepaymentDTOS = antServiceFacade.queryRepayPlan(applyNo);
            log.info("{} 返回参数 = {}", interfaceFunction);
            return ResultDto.searchSuccess(antRepaymentDTOS);
        } catch (BusinessException | IllegalArgumentException e) {
            return ResultDto.searchFail(e.getMessage());
        } catch (Exception e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail("系统异常");
        }
    }

    @GetMapping("/incomeProof/loanId/query/{loanId}")
    public ResultDto queryIncomeProof(@PathVariable("loanId") Integer loanId) {

        String interfaceFunction = "蚂蚁金融收入证明查询";
        log.info("{} 请求参数 = {}", interfaceFunction, loanId);

        try {
            ClueAdditionalResult clueAdditionalDTO = antServiceFacade.queryIncomeProof(loanId);
            log.info("{} 返回参数 = {}", interfaceFunction);
            return ResultDto.searchSuccess(clueAdditionalDTO);
        } catch (BusinessException | IllegalArgumentException e) {
            return ResultDto.searchFail(e.getMessage());
        } catch (Exception e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail("系统异常");
        }
    }

    @GetMapping("/incomeProof/query/{boId}")
    public ResultDto queryIncomeProof(@PathVariable("boId") String boId) {

        String interfaceFunction = "蚂蚁金融收入证明查询";
        log.info("{} 请求参数 = {}", interfaceFunction, boId);

        try {
            ClueAdditionalResult clueAdditionalDTO = antServiceFacade.queryIncomeProof(boId);
            log.info("{} 返回参数 = {}", interfaceFunction);
            return ResultDto.searchSuccess(clueAdditionalDTO);
        } catch (BusinessException | IllegalArgumentException e) {
            return ResultDto.searchFail(e.getMessage());
        } catch (Exception e) {
            log.error("{} 程序异常 = {}", interfaceFunction, e);
            return ResultDto.searchFail("系统异常");
        }
    }

}

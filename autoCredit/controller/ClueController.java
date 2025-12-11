package com.zlhj.unifiedInputPlatform.autoCredit.controller;

import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.util.JSONUtil;
import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.unifiedInputPlatform.autoCredit.core.service.ClueQueryService;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.ClueStatusInfoDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueBillDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueInfoDTO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.VehicleEvaluationNotifyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RequestMapping("/clue")
@RestController
public class ClueController {

	@Autowired
	private ClueQueryService clueQueryService;

	@GetMapping("/status/query")
	public ResultDto queryClue(QueryClueInfoDTO queryDTO) {

		String interfaceFunction = "线索状态查询";
		log.info("{} 请求参数 = {}", interfaceFunction, queryDTO);

		try {
			ClueStatusInfoDTO notifyDTO = clueQueryService.queryClueInfo(queryDTO);
			log.info("{} 返回参数 = {}", interfaceFunction, JSONUtil.toJsonStringCommon(notifyDTO));
			return ResultDto.searchSuccess(notifyDTO);
		} catch (BusinessException | IllegalArgumentException e) {
			log.error("{} 程序异常 = {}", interfaceFunction, e.getMessage(), e);
			return ResultDto.searchFail(e.getMessage());
		} catch (Exception e) {
			log.error("{} 程序异常 = {}", interfaceFunction, e.getMessage(), e);
			return ResultDto.searchFail("系统异常");
		}
	}

	/**
	 * 查询账务详情
	 */
	@GetMapping("/repayPlan/query")
	public ResultDto queryBill(@RequestParam("applyNo") String applyNo) {
		String interfaceFunction = "线索账务详情";
		log.info("{} 请求参数 = {}", interfaceFunction, applyNo);

		try {
			QueryClueBillDTO jdClueQueryBillVO = clueQueryService.clueQueryBill(applyNo);
			log.info("{} 返回参数 = {}", interfaceFunction, jdClueQueryBillVO);
			return ResultDto.searchSuccess(jdClueQueryBillVO);
		} catch (BusinessException | IllegalArgumentException e) {
			log.error("{} 程序异常 = {}", interfaceFunction, e.getMessage(), e);
			return ResultDto.searchFail(e.getMessage());
		} catch (Exception e) {
			log.error("{} 程序异常 = {}", interfaceFunction, e.getMessage(), e);
			return ResultDto.searchFail("系统异常");
		}
	}

	/**
	 * 查询车估值信息
	 */
	@GetMapping("/vehicleEvaluation/query")
	public ResultDto queryVehicleEvaluation(@RequestParam("applyNo") String applyNo) {
		String interfaceFunction = "线索车价评估查询";
		log.info("{} 请求参数 = {}", interfaceFunction, applyNo);

		try {
			VehicleEvaluationNotifyDTO evaluationNotifyDTO = clueQueryService.queryVehicleEvaluation(applyNo);
			log.info("{} 返回参数 = {}", interfaceFunction, evaluationNotifyDTO);
			return ResultDto.searchSuccess(evaluationNotifyDTO);
		} catch (BusinessException | IllegalArgumentException e) {
			return ResultDto.searchFail(e.getMessage());
		} catch (Exception e) {
			log.error("{} 程序异常 = {}", interfaceFunction, e.getMessage(), e);
			return ResultDto.searchFail("系统异常");
		}
	}
}

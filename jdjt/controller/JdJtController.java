package com.zlhj.unifiedInputPlatform.jdjt.controller;

import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.util.JSONUtil;
import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.unifiedInputPlatform.ant.dto.*;
import com.zlhj.unifiedInputPlatform.ant.service.AntService;
import com.zlhj.unifiedInputPlatform.jd.vo.JDClueQueryBillVO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.JdJtClueStatusNotifyDTO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.JdJtQueryClueInfoDTO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.VehicleEvaluationNotifyDTO;
import com.zlhj.unifiedInputPlatform.jdjt.service.JdJtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RequestMapping("/jdjt")
@RestController
public class JdJtController {

	@Autowired
	private JdJtService jdJtService;

    @Autowired
    private AntService antService;
	@GetMapping("/clue/query")
	public ResultDto queryClue(JdJtQueryClueInfoDTO queryDTO) {

		String interfaceFunction = "京东金条线索查询";
		log.info("{} 请求参数 = {}", interfaceFunction, queryDTO);

		try {
			JdJtClueStatusNotifyDTO notifyDTO = jdJtService.queryClueInfo(queryDTO);
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
		String interfaceFunction = "京东金条查询账务详情";
		log.info("{} 请求参数 = {}", interfaceFunction, applyNo);

		try {
			JDClueQueryBillVO jdClueQueryBillVO = jdJtService.clueQueryBill(applyNo);
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
	 * 查询账务详情
	 */
	@GetMapping("/vehicleEvaluation/query")
	public ResultDto queryVehicleEvaluation(@RequestParam("applyNo") String applyNo) {
		String interfaceFunction = "京东金条车架评估查询";
		log.info("{} 请求参数 = {}", interfaceFunction, applyNo);

		try {
			VehicleEvaluationNotifyDTO evaluationNotifyDTO = jdJtService.queryVehicleEvaluation(applyNo);
			log.info("{} 返回参数 = {}", interfaceFunction, evaluationNotifyDTO);
			return ResultDto.searchSuccess(evaluationNotifyDTO);
		} catch (BusinessException | IllegalArgumentException e) {
			return ResultDto.searchFail(e.getMessage());
		} catch (Exception e) {
			log.error("{} 程序异常 = {}", interfaceFunction, e.getMessage(), e);
			return ResultDto.searchFail("系统异常");
		}
	}

	/**
	 * 查询账务详情
	 */
	@PostMapping("/test")
	public ResultDto queryVehicleEvaluation(@RequestBody FddRASignVO fddRASignVO) {
		try {
            antService.fddSignComplete(fddRASignVO);
		} catch (Exception e) {
			log.error("法大大签约完成核心处理失败, msg: [{}] ", e.getMessage(), e);
			throw e;
		}
        return ResultDto.searchSuccess("处理成功");
	}
}

package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.controller;

import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.domain.cule.ClueNumber;
import com.zlhj.commonLoan.util.JSONUtil;
import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.unifiedInputPlatform.ant.dto.ClueStatusNotifyDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueBillDTO;
import com.zlhj.unifiedInputPlatform.jd.dto.JDQueryClueStatusCommand;
import com.zlhj.unifiedInputPlatform.jd.service.JDService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RequestMapping("/jd/clue")
@RestController
public class JDController {
	private final JDService jdService;

	public JDController(JDService jdService) {
		this.jdService = jdService;
	}

	/**
	 * 京东车抵贷查询申请状态
	 */
	@GetMapping("/status/query")
	public ResultDto queryClueStatus(JDQueryClueStatusCommand command) {

		String interfaceFunction = "京东车金融线索进件查询";
		log.info("{} 请求参数 = {}", interfaceFunction, command);

		try {
			ClueStatusNotifyDTO output = jdService.queryClueStatus(command);
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

	/**
	 * 查询账务详情
	 */
	@GetMapping("/bill/query")
	public ResultDto queryBill(@RequestParam("applyNo") String applyNo) {
		String interfaceFunction = "京东租赁 查询账务详情";
		log.info("{} 请求参数 = {}", interfaceFunction, applyNo);

		try {
			QueryClueBillDTO jdClueQueryBillVO = jdService.clueQueryBill(new ClueNumber(applyNo));
			log.info("{} 返回参数 = {}", interfaceFunction, jdClueQueryBillVO);
			return ResultDto.searchSuccess(jdClueQueryBillVO);
		} catch (BusinessException | IllegalArgumentException e) {
			return ResultDto.searchFail(e.getMessage());
		} catch (Exception e) {
			log.error("{} 程序异常 = {}", interfaceFunction, e);
			return ResultDto.searchFail("系统异常");
		}
	}

	/**
	 * 京东车生活查询申请状态
	 */
	@GetMapping("car/life/status/query")
	public ResultDto carLifeQueryClueStatus(JDQueryClueStatusCommand command) {

		String interfaceFunction = "京东车生活线索进件查询";
		log.info("{} 请求参数 = {}", interfaceFunction, command);

		try {
			ClueStatusNotifyDTO output = jdService.carLifeQueryClueStatus(command);
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
}

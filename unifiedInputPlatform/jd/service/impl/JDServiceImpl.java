package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.apollo.alds.util.ConvertionUtil;
import com.apollo.util.DateUtil;
import com.zlhj.InterfaceMessage.dto.MainLeasing;
import com.zlhj.InterfaceMessage.dto.MainLeasingRepository;
import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.business.jjyh.service.LoanAppStatusService;
import com.zlhj.commonLoan.domain.cule.ClueNumber;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.loan.dao.SplUserInfoMapper;
import com.zlhj.loan.entity.SapdcslasRepository;
import com.zlhj.mapper.LoanInitialRepaymentScheduleMapper;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.mq.provider.Sender;
import com.zlhj.unifiedInputPlatform.ant.dto.AntRepaymentChangeNotifyDTO;
import com.zlhj.unifiedInputPlatform.jd.dto.JDLoanSuccessDTO;
import com.zlhj.entity.BusinessBasicEntity;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.loan.service.BankApprovalRecordService;
import com.zlhj.loan.vo.BankApprovalRecordPo;
import com.zlhj.tianfu.Util.Tool;
import com.zlhj.unifiedInputPlatform.ant.dto.ClueStatusNotifyDTO;
import com.zlhj.unifiedInputPlatform.jd.dto.JDQueryClueStatusCommand;
import com.zlhj.unifiedInputPlatform.jd.service.JDClueQueryBillService;
import com.zlhj.unifiedInputPlatform.jd.service.JDService;
import com.zlhj.unifiedInputPlatform.jd.vo.JDClueQueryBillVO;
import com.zlhj.unifiedInputPlatform.universal.service.impl.UnifiedInputPlatformServiceImpl;
import com.zlhj.user.vo.ExhibitionAndPayInfoRepository;
import com.zlhj.user.vo.ExhibitionAndPayInfoVo;
import com.zlhj.user.vo.MultipleLoanObject;
import com.zlhj.user.vo.MultipleLoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class JDServiceImpl implements JDService {
	@Autowired
	private SplBussinessbasicMapper splBussinessbasicMapper;
	@Autowired
	private UnifiedInputPlatformServiceImpl unifiedInputPlatformService;
	@Autowired
	private LoanAppStatusService loanAppStatusService;
	@Autowired
	private MultipleLoanRepository loanInfoRepository;
	@Autowired
	private MainLeasingRepository mainLeasingRepository;
	@Autowired
	private ExhibitionAndPayInfoRepository exhibitionAndPayInfoRepository;
	@Autowired
	private BankApprovalRecordService bankApprovalRecordService;
	@Autowired
	private LoanInitialRepaymentScheduleMapper loanInitialRepaymentScheduleMapper;
	@Autowired
	private SapdcslasRepository sapdcslasRepository;
	@Autowired
	private JDClueQueryBillService jdClueQueryBillService;

	@Autowired
	private Sender sender;

	@Override
	public ClueStatusNotifyDTO queryClueStatus(JDQueryClueStatusCommand clueNumber) {
		ClueStatusNotifyDTO input = new ClueStatusNotifyDTO();
		input.setOutApplyNo(clueNumber.getApplyNo());
		input.setStatus(clueNumber.getStatus());
		LoanStatusChangeEnum enumsByValue = LoanStatusChangeEnum.getEnumsByValue(clueNumber.getStatus());
		if ("JD_NOTICE_AUTO_FINANCE".equals(clueNumber.getSource())) {
			DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(
					clueNumber.getApplyNo(),
					23
			);
			if (directCustomerStatusDto == null) {
				return input;
			}
			enumsByValue = LoanStatusChangeEnum.getEnumsByValue(directCustomerStatusDto.getSplMaxActionNum());
			if (enumsByValue != null) {
				input.setStatus(enumsByValue.getValue());
			}

		}
		if (enumsByValue == null) {
			return input;
		}

		if (enumsByValue != LoanStatusChangeEnum.APPROVE_PASS && enumsByValue != LoanStatusChangeEnum.LEND_SUC) {
			return input;
		}

		DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(clueNumber.getApplyNo(), 23);
		if (directCustomerStatusDto == null) {
			return input;
		}

		if (enumsByValue == LoanStatusChangeEnum.APPROVE_PASS) {
			Integer loanId = directCustomerStatusDto.getSplLoanId();
			MultipleLoanObject loanInfo = loanInfoRepository.getLoanIdByMainLoanId(loanId);
			if (loanInfo != null) {
				BigDecimal commonRate = new BigDecimal(loanInfo.getM_commonRate().toString());
				input.setLoanRate(commonRate.setScale(2, BigDecimal.ROUND_HALF_UP));
				BankApprovalRecordPo bankApprovalRecord = bankApprovalRecordService.getBankApprovalRecord(loanId);
				if (bankApprovalRecord != null) {
					input.setCreditAmt(new BigDecimal(bankApprovalRecord.getApplyLimit()));
					input.setApproveDate(bankApprovalRecord.getCreateTime());
				}
			}
		}
		if (enumsByValue == LoanStatusChangeEnum.LEND_SUC) {
			Integer loanId = directCustomerStatusDto.getSplLoanId();
			MultipleLoanObject loanInfo = loanInfoRepository.getLoanIdByMainLoanId(loanId);
			if (loanInfo != null) {
				BigDecimal commonRate = new BigDecimal(loanInfo.getM_commonRate().toString());
				input.setOrgDrawdownNo(loanInfo.getM_loanNumber());
				input.setLoanRate(commonRate.setScale(2, BigDecimal.ROUND_HALF_UP));
				// 是否需要租赁放款
				boolean isleasinglending = "1".equals(Tool.searchIsleasinglending(loanId));
				MainLeasing mainLeasing = mainLeasingRepository.getDataByLoanId(loanId);
				BankApprovalRecordPo bankApprovalRecord = bankApprovalRecordService.getBankApprovalRecord(loanId);
				if (bankApprovalRecord != null) {
					input.setCreditAmt(new BigDecimal(bankApprovalRecord.getApplyLimit()));
					input.setApproveDate(bankApprovalRecord.getCreateTime());
				}
				if (isleasinglending) {
					ExhibitionAndPayInfoVo exhibitionAndPayInfoVo = exhibitionAndPayInfoRepository.getObject(loanId);
					if (mainLeasing != null) {
						if (null != mainLeasing.getGrantMoneyDate()) {
							Date loanDate = DateUtil.strToDate1(mainLeasing.getGrantMoneyDate());
							input.setLoanDate(DateUtil.dateToStrLong(loanDate));
						}
					}
					if (exhibitionAndPayInfoVo != null) {
						input.setLoanAmt(exhibitionAndPayInfoVo.getM_payAmount());
					}
				} else {
					if (null != loanInfo.getM_grantMoneyDate()) {
						Date loanDate = DateUtil.strToDate1(loanInfo.getM_grantMoneyDate());
						input.setLoanDate(DateUtil.dateToStrLong(loanDate));
					}
					BigDecimal applyMoney = new BigDecimal(loanInfo.getM_applyMoney().toString());
					input.setLoanAmt(applyMoney);
				}
			}

		}
		return input;
	}

	public void jdAutoFinanceNotSubmitSchedule() {
		log.info("合作渠道=23-京东车抵贷-30天超期定时任务开始：[{}]", DateUtil.formatToYYYYMMDDHHMMSS2(new Date()));
		//获取30天前的日期
		String date = DateUtil.getNowBeforeDate(30);
		log.info("合作渠道=23-京东车抵贷-30天超期定时任务，实际执行的创建时间[{}]", date);

		//筛选线索进件时间超过30天、没有进件提交线索
		List<String> applyNums = splBussinessbasicMapper.selectJDFinaceNotSubmitList(date);
		log.info("筛选线索进件时间超过30天、没有进件提交线索:[{}]", applyNums);
		if (!applyNums.isEmpty()) {
			for (String applyNum : applyNums) {
				//查询该线索进件业务的当前贷款状态,若有多笔贷款取贷款状态最大的该笔
				List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, 23);
				DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(applyNum, 23);
				String maxActionNum = ConvertionUtil.getSimpleStringWithNull(directCustomerStatusDto.getSplMaxActionNum());
				if (maxLoanStatus != null && !maxLoanStatus.isEmpty()) {
					//如果有多条，则取第一条
					Map<String, Object> maxStatus = maxLoanStatus.get(0);
					//sapdcslas表中汇总贷款id
					Integer loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
					//sapdcslas表中状态
					int status = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("status"));

					LoanStatusChangeEnum loanStatusChangeEnum = null;
					if (status >= 245) {
						unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, LoanStatusChangeEnum.LEND_SUC.getValue(), 23, "JD_NOTICE_AUTO_FINANCE"));
					} else if (status < 0) {
						loanStatusChangeEnum = LoanStatusChangeEnum.valueOf(maxActionNum).nextReject();
						unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, loanStatusChangeEnum.getValue(), 23, "JD_NOTICE_AUTO_FINANCE"));
						maxLoanStatus.remove(0);
						if (!maxLoanStatus.isEmpty()) {
							maxLoanStatus.forEach(r -> {
								Integer id = Integer.valueOf(r.get("loanId").toString());
								Integer status2 = Integer.valueOf(r.get("status").toString());
								//其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
								loanAppStatusService.updateState(id, -15, status2);
								loanAppStatusService.saveNode(id, -15, status2, 0, "线索超期取消", 4);
								//添加节点
								loanAppStatusService.saveNode(id, -15, -15, 0, "线索超期取消", 4);
							});
						}

						List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, 23);

						basicEntity.forEach(r -> {
							//受理阶段做取消操作
							if (r.getLoanState() == 0 || r.getLoanState() == 3) {
								splBussinessbasicMapper.updateSplBussinessBasic(9, "线索超期取消", r.getBranchLoanId());
							}
						});

					}
				} else {
					List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, 23);
					if (basicEntity != null && !basicEntity.isEmpty()) {
						unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, null, LoanStatusChangeEnum.valueOf(maxActionNum).nextReject().getValue(), 23, "JD_NOTICE_AUTO_FINANCE"));
					}
					basicEntity.forEach(r -> {
						//受理阶段做取消操作
						if (r.getLoanState() == 0 || r.getLoanState() == 3) {
							splBussinessbasicMapper.updateSplBussinessBasic(9, "线索超期取消", r.getBranchLoanId());
						}
					});
				}

			}
		} else {
			log.info("合作渠道=23-京东车抵贷-30天超期定时任务,查询无数据，不需要处理");
		}
	}

	@Override
	public void jdCarLifeNotSubmitSchedule() {
		log.info("合作渠道=24-京东车生活-30天超期定时任务开始：[{}]", DateUtil.formatToYYYYMMDDHHMMSS2(new Date()));
		//获取30天前的日期
		String date = DateUtil.getNowBeforeDate(30);
		log.info("合作渠道=24-京东车生活-30天超期定时任务，实际执行的创建时间[{}]", date);

		//筛选线索进件时间超过30天、没有进件提交线索
		HashSet<String> applyNums = splBussinessbasicMapper.selectJDCarLifeNotSubmitList(date);
		HashSet<String> notSubmission = splBussinessbasicMapper.selectJDCarLifeNotSubmissionList(date);
		if (applyNums == null){
			applyNums = new HashSet<>();
		}
		applyNums.addAll(notSubmission);
		log.info("筛选线索进件时间超过30天、没有进件提交线索:[{}]", applyNums);
		if (!applyNums.isEmpty()) {
			for (String applyNum : applyNums) {
				//查询该线索进件业务的当前贷款状态,若有多笔贷款取贷款状态最大的该笔
				List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, 24);
				if (maxLoanStatus != null && !maxLoanStatus.isEmpty()) {
					//如果有多条，则取第一条
					Map<String, Object> maxStatus = maxLoanStatus.get(0);
					//sapdcslas表中汇总贷款id
					Integer loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
					//sapdcslas表中状态
					int status = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("status"));

					if (status >= 245) {
						unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, LoanStatusChangeEnum.LEND_SUC.getValue(), 24, "JD_NOTICE_CAR_LIFE"));
					} else {

						unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, LoanStatusChangeEnum.JD_CAR_LIFE_OVERTIME_CANCEL.getValue(), 24, "JD_NOTICE_CAR_LIFE"));
						maxLoanStatus.remove(0);
						if (!maxLoanStatus.isEmpty()) {
							maxLoanStatus.forEach(r -> {
								Integer id = Integer.valueOf(r.get("loanId").toString());
								Integer status2 = Integer.valueOf(r.get("status").toString());
								//其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
								loanAppStatusService.updateState(id, -15, status2);
								loanAppStatusService.saveNode(id, -15, status2, 0, "线索超期取消", 4);
								//添加节点
								loanAppStatusService.saveNode(id, -15, -15, 0, "线索超期取消", 4);
							});
						}

						List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, 24);

						basicEntity.forEach(r -> {
							//受理阶段做取消操作
							if (r.getLoanState() == 0 || r.getLoanState() == 3) {
								splBussinessbasicMapper.updateSplBussinessBasic(9, "线索超期取消", r.getBranchLoanId());
							}
						});

					}
				} else {
					List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, 24);
					if (basicEntity != null && !basicEntity.isEmpty()) {
						unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, null, LoanStatusChangeEnum.JD_CAR_LIFE_OVERTIME_CANCEL.getValue(), 24, "JD_NOTICE_CAR_LIFE"));
					}
					basicEntity.forEach(r -> {
						//受理阶段做取消操作
						if (r.getLoanState() == 0 || r.getLoanState() == 3) {
							splBussinessbasicMapper.updateSplBussinessBasic(9, "线索超期取消", r.getBranchLoanId());
						}
					});
				}

			}
		} else {
			log.info("合作渠道=24-京东车生活-30天超期定时任务,查询无数据，不需要处理");
		}
	}

	@Override
	public void loanSuccessNotify() {
		List<JDLoanSuccessDTO> loanSuccessData = splBussinessbasicMapper.loanSuccessData();

		loanSuccessData.stream()
				.filter(data -> !CollectionUtils.isEmpty(loanInitialRepaymentScheduleMapper.selectList(data.getLoanId())))
				.forEach(data -> {
					unifiedInputPlatformService.realTimeInteraction(
							new LoanStatePushToClueDTO(
									data.getBoId(),
									null,
									LoanStatusChangeEnum.LEND_SUC.getValue(),
									23,
									"JD_NOTICE_AUTO_FINANCE"
							)
					);
					sapdcslasRepository.updateNotifyStatus(data.getMainLoanId(), "Y");
				});
	}

	@Override
	public void repaymentPlanChangeNotify() {
		List<AntRepaymentChangeNotifyDTO> jdRepayPlanChangeData = splBussinessbasicMapper.jdRepayPlanChangeData();
		if (jdRepayPlanChangeData.isEmpty()) {
			return;
		}
		jdRepayPlanChangeData.forEach(sender::repaymentChangePush);
	}

	/**
	 * 查询账单详情
	 */
	@Override
	public JDClueQueryBillVO clueQueryBill(ClueNumber clueNumber) {

		Integer jdChannelPattern = 23;
		LoanId loanId = unifiedInputPlatformService.getLoanIdByClueSystem(clueNumber, jdChannelPattern);

		if (ObjectUtil.isEmpty(loanId)) {
			throw new BusinessException("线索没有匹配的贷款数据");
		}
        log.info("当前Loanid为：{}",loanId);

		return jdClueQueryBillService.create(loanId);
	}

	@Override
	public ClueStatusNotifyDTO carLifeQueryClueStatus(JDQueryClueStatusCommand command) {
		ClueStatusNotifyDTO input = new ClueStatusNotifyDTO();
		input.setOutApplyNo(command.getApplyNo());

		List<BusinessBasicEntity> businessBasicEntities = splBussinessbasicMapper.getBusinessByApplyNum(command.getApplyNo());

		if (businessBasicEntities != null && !businessBasicEntities.isEmpty()) {
			BusinessBasicEntity businessBasicEntity = businessBasicEntities.stream()
					.max(Comparator.comparing(BusinessBasicEntity::getSubmitDate)).get();
			input.setCarLicenseNo(businessBasicEntity.getRemark1());
			Map splUserInfoByBranchLoanId = splBussinessbasicMapper.getSplUserInfoByBranchLoanId(businessBasicEntity.getBranchLoanId());
			if (splUserInfoByBranchLoanId != null) {
				input.setCustomerName(splUserInfoByBranchLoanId.get("CUSTOMERNAME") == null ? "" : splUserInfoByBranchLoanId.get("CUSTOMERNAME").toString());
				input.setCustomerPhone(splUserInfoByBranchLoanId.get("CUSTOMERCONTACTWAY") == null ? "" : splUserInfoByBranchLoanId.get("CUSTOMERCONTACTWAY").toString());

                DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(
                        command.getApplyNo(),
                        24
                );
				if (directCustomerStatusDto != null){
					LoanStatusChangeEnum enumsByValue = LoanStatusChangeEnum.getEnumsByValue(directCustomerStatusDto.getSplMaxActionNum());
					if (enumsByValue == LoanStatusChangeEnum.LEND_SUC) {
						Integer loanId = directCustomerStatusDto.getSplLoanId();
						MultipleLoanObject loanInfo = loanInfoRepository.getLoanIdByMainLoanId(loanId);
						if (loanInfo != null) {
							BigDecimal commonRate = new BigDecimal(loanInfo.getM_commonRate().toString());
							input.setOrgDrawdownNo(loanInfo.getM_loanNumber());
							input.setLoanRate(commonRate.setScale(2, BigDecimal.ROUND_HALF_UP));
							// 是否需要租赁放款
							boolean isleasinglending = "1".equals(Tool.searchIsleasinglending(loanId));
							MainLeasing mainLeasing = mainLeasingRepository.getDataByLoanId(loanId);

							if (isleasinglending) {
								ExhibitionAndPayInfoVo exhibitionAndPayInfoVo = exhibitionAndPayInfoRepository.getObject(loanId);
								if (mainLeasing != null) {
									if (null != mainLeasing.getGrantMoneyDate()) {
										Date loanDate = DateUtil.strToDate1(mainLeasing.getGrantMoneyDate());
										input.setLoanDate(DateUtil.dateToStrLong(loanDate));
									}
								}
								if (exhibitionAndPayInfoVo != null) {
									input.setLoanAmt(exhibitionAndPayInfoVo.getM_payAmount());
								}
							} else {
								if (null != loanInfo.getM_grantMoneyDate()) {
									Date loanDate = DateUtil.strToDate1(loanInfo.getM_grantMoneyDate());
									input.setLoanDate(DateUtil.dateToStrLong(loanDate));
								}
								BigDecimal applyMoney = new BigDecimal(loanInfo.getM_applyMoney().toString());
								input.setLoanAmt(applyMoney);
							}
						}
					}
				}

			}
		}
		return input;
	}
}

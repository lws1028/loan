package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;


import com.zlhj.Interface.batchVo.LoanRepayDetailObject;
import com.zlhj.Interface.batchVo.LoanRepayDetailRepository;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeInformation;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeInformationRepository;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeRepayPlan;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeRepayPlanRepository;
import com.zlhj.loan.entity.Sapdcslas;
import com.zlhj.loan.entity.SapdcslasRepository;
import com.zlhj.unifiedInputPlatform.ant.dto.AntRepaymentPlanDTO;
import com.zlhj.user.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class AntRepaymentPlanFactory{
	private static final String MAIN_LOAN = "2";
	private static final String ADDITIONAL_SECURITY_DEPOSIT = "1";//附加担保费
	private static final String GPS_SECURITY_DEPOSIT = "2";//gps担保费

	@Autowired
	private MultipleLoanRepository multipleLoanRepository;

	@Autowired
	private SapdcslasRepository sapdcslasRepository;

	@Autowired
	private AdditionalFeeInformationRepository additionalFeeInformationRepository;

	@Autowired
	private AdditionalFeeRepayPlanRepository additionalFeeRepayPlanRepository;

	@Autowired
	private LoanRepayDetailRepository loanRepayDetailRepository;

	public List<com.zlhj.unifiedInputPlatform.ant.dto.AntRepaymentPlanDTO> create(LoanId loanId){
		Sapdcslas sapdcslas = getSapdcslas(loanId.get());//贷款节点信息
		if(sapdcslas == null){
			return null;
		}
		if (sapdcslas.getSdlasStatus() < 245 ){//只拿节点在245的贷款数据
			return null;
		}

		MultipleLoanObject mainLoan = getLoanObject(loanId.get(), MAIN_LOAN);//主贷款信息

		AdditionalFeeInformation additionalFeeInformation = getYuanEuroguaranteeInFormation(loanId.get(),ADDITIONAL_SECURITY_DEPOSIT);//元欧附加担保费信息

		AdditionalFeeInformation gpsFeeInformation = getYuanEuroguaranteeInFormation(loanId.get(),GPS_SECURITY_DEPOSIT);//元欧GPS担保费信息

		List<LoanRepayDetailObject> mainLoanRepayment =getLoanRepaymentObject(mainLoan);//主业务还款信息


		List<AdditionalFeeRepayPlan> additionalFeeRepayPlans = getAdditionalPremiumRepaymentObject(additionalFeeInformation);//附加担保费还款信息
		Map<Integer, AdditionalFeeRepayPlan> additionalFeeRepayPlanMap = additionalFeeRepayPlans.stream()
				.collect(Collectors.toMap(AdditionalFeeRepayPlan::getApTerm, r -> r));

		List<AdditionalFeeRepayPlan> gpsFeeRepayPlans = getGPSPremiumRepaymentObject(gpsFeeInformation);//gps担保费还款信息
		Map<Integer, AdditionalFeeRepayPlan> gpsFeeRepayPlanMap = gpsFeeRepayPlans.stream()
				.collect(Collectors.toMap(AdditionalFeeRepayPlan::getApTerm, r -> r));

		return mainLoanRepayment.stream().map(rs -> new AntRepaymentPlanDTO(
				rs.getM_actualRepaymentDate(),//实际还款⽇期
				allRepaidInterest(rs),//计算所有 //实还利息
				allRepaidCompound(rs),//计算所有  实还利息罚息 即实还复利
				allRepaidPrincipal(rs),//计算所有  实还本金
				allRepaidPenalty(rs), //实还本⾦罚息
				BigDecimal.ZERO,//实还违约罚息
				repaidServiceFee(rs,additionalFeeRepayPlanMap,gpsFeeRepayPlanMap),//实还服务费
				BigDecimal.ZERO,//提前结清应还违约⾦
				allShouldInterest(rs),//计算所有  应还利息
				allShouldCompound(rs),//计算所有  应还利息罚息  即应还复利
				squareFlag(rs,additionalFeeRepayPlanMap,gpsFeeRepayPlanMap),//结清标志   1代表结清，0代表未结清
				allShouldPrincipal(rs),//计算所有  应还本金
				allShouldPenalty(rs),//计算所有  应还本⾦罚息
				BigDecimal.ZERO,//应还违约罚息
				BigDecimal.ZERO,//提前结清实还违约⾦
				rs.getM_repaymentDate(),//应还款⽇期 yyyy/mm/dd
				rs.getM_thisTerm().intValue(),//还款期次
				shouldServiceFee(rs,additionalFeeRepayPlanMap,gpsFeeRepayPlanMap),//应还服务费
				getOverdue(rs,additionalFeeRepayPlanMap,gpsFeeRepayPlanMap)
		)).collect(Collectors.toList());
	}

	/**
	 * 所有应还罚息
	 */
	private BigDecimal allShouldPenalty(LoanRepayDetailObject loanRepayDetailObject){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}
		return loanRepayDetailObject.getM_thisPenalty() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisPenalty();
	}

	/**
	 * 所有实还罚息
	 */
	private BigDecimal allRepaidPenalty(LoanRepayDetailObject loanRepayDetailObject){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}
		return loanRepayDetailObject.getM_penalty() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_penalty();
	}

	/**
	 * 所有应还复利
	 */
	private BigDecimal allShouldCompound(LoanRepayDetailObject loanRepayDetailObject){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}
		return loanRepayDetailObject.getM_thisCompound() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisCompound();
	}

	/**
	 * 所有实还复利
	 */
	private BigDecimal allRepaidCompound(LoanRepayDetailObject loanRepayDetailObject){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}
		return loanRepayDetailObject.getM_compound() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_compound();
	}
	/**
	 * 所有应还利息
	 */
	private BigDecimal allShouldInterest(LoanRepayDetailObject loanRepayDetailObject){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}
		return loanRepayDetailObject.getM_thisInterest() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisInterest();
	}

	/**
	 * 所有实还利息
	 */
	private BigDecimal allRepaidInterest(LoanRepayDetailObject loanRepayDetailObject){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}
		return loanRepayDetailObject.getM_interest() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_interest();
	}


	/**
	 * 所有应还本金
	 */
	private BigDecimal allShouldPrincipal(LoanRepayDetailObject loanRepayDetailObject){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}
		return loanRepayDetailObject.getM_thisCapital() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisCapital();
	}

	/**
	 * 所有实还本金
	 */
	private BigDecimal allRepaidPrincipal(LoanRepayDetailObject loanRepayDetailObject){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}

		return loanRepayDetailObject.getM_capital() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_capital();
	}

	/**
	 * 是否结清  1-结清
	 */
	private String squareFlag(LoanRepayDetailObject loanRepayDetailObject,
							  Map<Integer, AdditionalFeeRepayPlan> additionalFeeRepayPlanMap,
							  Map<Integer, AdditionalFeeRepayPlan> gpsFeeRepayPlanMap){
		if (loanRepayDetailObject == null || loanRepayDetailObject.getM_repaymentDate() == null){
			return "0";
		}

		BigDecimal mThisInterest = loanRepayDetailObject.getM_thisInterest() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisInterest();
		BigDecimal mThisCapital = loanRepayDetailObject.getM_thisCapital() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisCapital();
		BigDecimal mThisPremium = loanRepayDetailObject.getM_thisPremium() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisPremium();
		BigDecimal mThisPenalty = loanRepayDetailObject.getM_thisPenalty() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisPenalty();
		BigDecimal mThisCompound = loanRepayDetailObject.getM_thisCompound() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisCompound();
		BigDecimal mInterest = loanRepayDetailObject.getM_interest() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_interest();
		BigDecimal mPenalty = loanRepayDetailObject.getM_penalty() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_penalty();
		BigDecimal mCompound = loanRepayDetailObject.getM_compound() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_compound();
		BigDecimal mCapital = loanRepayDetailObject.getM_capital() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_capital();
		if (mThisInterest
				.add(mThisCapital)
				.add(mThisPremium)
				.add(mThisPenalty)
				.add(mThisCompound)
				.subtract(mInterest)
				.subtract(mPenalty)
				.subtract(mCompound)
				.subtract(mCapital).compareTo(BigDecimal.ZERO) > 0){
			return "0";
		}


		AdditionalFeeRepayPlan additionalFeeRepayPlan = additionalFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
		if (additionalFeeRepayPlan != null){
			if (additionalFeeRepayPlan.getApOverdueStatus() != null && additionalFeeRepayPlan.getApOverdueStatus()){
				return "0";
			}
			BigDecimal should1 = additionalFeeRepayPlan.getApRepayAmount() == null ? BigDecimal.ZERO : additionalFeeRepayPlan.getApRepayAmount();
			BigDecimal should2 = additionalFeeRepayPlan.getApPenalSum() == null ? BigDecimal.ZERO : additionalFeeRepayPlan.getApPenalSum();

			BigDecimal repaid1 = additionalFeeRepayPlan.getApActualRepayAmount() == null ? BigDecimal.ZERO : additionalFeeRepayPlan.getApActualRepayAmount();
			BigDecimal repaid2 = additionalFeeRepayPlan.getApRealPenalSum() == null ? BigDecimal.ZERO : additionalFeeRepayPlan.getApRealPenalSum();

			if (should1.add(should2).subtract(repaid1).subtract(repaid2).compareTo(BigDecimal.ZERO) > 0 ){
				return "0";
			}
		}

		AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan = gpsFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
		if (gpsAdditionalFeeRepayPlan != null){
			if (gpsAdditionalFeeRepayPlan.getApOverdueStatus() != null && gpsAdditionalFeeRepayPlan.getApOverdueStatus()){
				return "0";
			}

			BigDecimal should1 = gpsAdditionalFeeRepayPlan.getApRepayAmount() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApRepayAmount();
			BigDecimal should2 = gpsAdditionalFeeRepayPlan.getApPenalSum() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApPenalSum();

			BigDecimal repaid1 = gpsAdditionalFeeRepayPlan.getApActualRepayAmount() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApActualRepayAmount();
			BigDecimal repaid2 = gpsAdditionalFeeRepayPlan.getApRealPenalSum() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApRealPenalSum();

			if (should1.add(should2).subtract(repaid1).subtract(repaid2).compareTo(BigDecimal.ZERO) > 0 ){
				return "0";
			}
		}

		return "1";
	}
	/**
	 * 逾期 状态
	 */
	private String getOverdue(LoanRepayDetailObject loanRepayDetailObject,
							  Map<Integer, AdditionalFeeRepayPlan> additionalFeeRepayPlanMap,
							  Map<Integer, AdditionalFeeRepayPlan> gpsFeeRepayPlanMap) {

		if (loanRepayDetailObject == null || loanRepayDetailObject.getM_repaymentDate() == null){
			return "NORMAL";
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		if (new Date().compareTo(dateFormat.parse(loanRepayDetailObject.getM_repaymentDate(), pos)) > 0 && "1".equals(loanRepayDetailObject.getM_repaymentState())) {
			return "OVD";
		}

		AdditionalFeeRepayPlan additionalFeeRepayPlan = additionalFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
		if (additionalFeeRepayPlan != null){

			if (additionalFeeRepayPlan.getApOverdueStatus() != null && additionalFeeRepayPlan.getApOverdueStatus()){
				return "OVD";
			}
		}

		AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan = gpsFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
		if (gpsAdditionalFeeRepayPlan != null){
			if (gpsAdditionalFeeRepayPlan.getApOverdueStatus() != null && gpsAdditionalFeeRepayPlan.getApOverdueStatus()){
				return "OVD";
			}
		}

		BigDecimal overdueMoney = getOverdueMoney(loanRepayDetailObject);

		BigDecimal additionalFee = actualPremiumOverdueAmount(additionalFeeRepayPlan);
		BigDecimal gpsAdditionalFee = actualPremiumOverdueAmount(gpsAdditionalFeeRepayPlan);

		if (overdueMoney
				.add(additionalFee)
				.add(gpsAdditionalFee)
				.compareTo(BigDecimal.ZERO) == 0 ){

			return "CLEAR";
		}

		return "NORMAL";
	}
	/**
	 * 逾期金额（附加担保费)
	 */
	private BigDecimal actualPremiumOverdueAmount(AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan){
		if (gpsAdditionalFeeRepayPlan == null){
			return BigDecimal.ZERO;
		}

		if (gpsAdditionalFeeRepayPlan.getApOverdueStatus() != null && !gpsAdditionalFeeRepayPlan.getApOverdueStatus()){
			return BigDecimal.ZERO;
		}
		return this.shouldPremiumRepayAmount(gpsAdditionalFeeRepayPlan).subtract(this.actualPremiumRepaymentAmount(gpsAdditionalFeeRepayPlan));
	}
	/**
	 * 应还总额(附加担保费)
	 */
	private BigDecimal shouldPremiumRepayAmount(AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan){
		if (gpsAdditionalFeeRepayPlan == null){
			return BigDecimal.ZERO;
		}

		return (gpsAdditionalFeeRepayPlan.getApRepayAmount() == null ? BigDecimal.ZERO :gpsAdditionalFeeRepayPlan.getApRepayAmount())
				.add(
						(gpsAdditionalFeeRepayPlan.getApPenalSum() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApPenalSum())
				);
	}
	/**
	 * 当期实还总额(附加担保费用)
	 */
	private BigDecimal actualPremiumRepaymentAmount(AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan){
		if (gpsAdditionalFeeRepayPlan == null){
			return BigDecimal.ZERO;
		}

		return (gpsAdditionalFeeRepayPlan.getApActualRepayAmount() == null ? BigDecimal.ZERO :gpsAdditionalFeeRepayPlan.getApActualRepayAmount())
				.add(
						(gpsAdditionalFeeRepayPlan.getApRealPenalSum() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApRealPenalSum())
				);
	}
	/**
	 * 剩余应还金额
	 */
	private BigDecimal getOverdueMoney(LoanRepayDetailObject loanRepayDetailObject) {
		if (loanRepayDetailObject == null || loanRepayDetailObject.getM_repaymentDate() == null){
			return BigDecimal.ZERO;
		}

		BigDecimal mThisInterest = loanRepayDetailObject.getM_thisInterest() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisInterest();
		BigDecimal mThisCapital = loanRepayDetailObject.getM_thisCapital() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisCapital();
		BigDecimal mThisPremium = loanRepayDetailObject.getM_thisPremium() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisPremium();
		BigDecimal mThisPenalty = loanRepayDetailObject.getM_thisPenalty() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisPenalty();
		BigDecimal mThisCompound = loanRepayDetailObject.getM_thisCompound() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisCompound();
		BigDecimal mInterest = loanRepayDetailObject.getM_interest() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_interest();
		BigDecimal mPenalty = loanRepayDetailObject.getM_penalty() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_penalty();
		BigDecimal mCompound = loanRepayDetailObject.getM_compound() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_compound();
		BigDecimal mCapital = loanRepayDetailObject.getM_capital() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_capital();
		return mThisInterest
				.add(mThisCapital)
				.add(mThisPremium)
				.add(mThisPenalty)
				.add(mThisCompound)
				.subtract(mInterest)
				.subtract(mPenalty)
				.subtract(mCompound)
				.subtract(mCapital);
	}

	/**
	 * 主或附加贷款信息
	 */
	private MultipleLoanObject getLoanObject(Integer loanId, String loanType) {
		MultipleLoanObject multipleLoanObject = new MultipleLoanObject();
		multipleLoanObject.setMMainLoanID(loanId);
		multipleLoanObject.setMLoanType(loanType);
		return multipleLoanRepository.getObject(multipleLoanObject);
	}

	/**
	 * 当前贷款所处状态
	 */
	private Sapdcslas getSapdcslas(Integer loanId){
		return sapdcslasRepository.getSapdcslasByLoanId(loanId);
	}

	/**
	 * 元欧担保费信息
	 */
	private AdditionalFeeInformation getYuanEuroguaranteeInFormation(Integer loanId,String expenseType){
		return additionalFeeInformationRepository.getAdditionalFeeInformationByLoanIdAndType(loanId,expenseType);
	}

	/**
	 * 还款计划信息
	 */
	private List<LoanRepayDetailObject> getLoanRepaymentObject(MultipleLoanObject multipleLoanObject){
		if (multipleLoanObject == null){
			return new ArrayList<>();
		}
		return loanRepayDetailRepository.getListOrderByTerm(multipleLoanObject.getM_loanid());
	}


	/**
	 * 附加担保费还款计划
	 */

	private List<AdditionalFeeRepayPlan> getAdditionalPremiumRepaymentObject(AdditionalFeeInformation additionalFeeInformation){
		if (additionalFeeInformation == null){
			return new ArrayList<>();
		}
		return additionalFeeRepayPlanRepository.getByAdditionalFeeId(additionalFeeInformation.getId());
	}

	/**
	 * gps担保费还款计划
	 */
	private List<AdditionalFeeRepayPlan> getGPSPremiumRepaymentObject(AdditionalFeeInformation gpsFeeInformation){
		if (gpsFeeInformation == null){
			return new ArrayList<>();
		}
		return additionalFeeRepayPlanRepository.getByAdditionalFeeId(gpsFeeInformation.getId());
	}

	/**
	 * 应还服务费
	 */
	private BigDecimal shouldServiceFee(LoanRepayDetailObject loanRepayDetailObject, Map<Integer, AdditionalFeeRepayPlan> additionalFeeRepayPlanMap, Map<Integer, AdditionalFeeRepayPlan> gpsFeeRepayPlanMap){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}

		AdditionalFeeRepayPlan additionalFeeRepayPlan = additionalFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
		BigDecimal a2 = additionalFeeRepayPlan == null || additionalFeeRepayPlan.getApRepayAmount() == null ? BigDecimal.ZERO : additionalFeeRepayPlan.getApRepayAmount();
		BigDecimal a3 = additionalFeeRepayPlan == null || additionalFeeRepayPlan.getApPenalSum() == null ? BigDecimal.ZERO : additionalFeeRepayPlan.getApPenalSum();

		AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan = gpsFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
		BigDecimal a4 = gpsAdditionalFeeRepayPlan == null || gpsAdditionalFeeRepayPlan.getApRepayAmount() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApRepayAmount();
		BigDecimal a5 = gpsAdditionalFeeRepayPlan == null || gpsAdditionalFeeRepayPlan.getApPenalSum() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApPenalSum();

		return a2.add(a3).add(a4).add(a5);
	}

	/**
	 * 实还服务费
	 */
	private BigDecimal repaidServiceFee(LoanRepayDetailObject loanRepayDetailObject, Map<Integer, AdditionalFeeRepayPlan> additionalFeeRepayPlanMap, Map<Integer, AdditionalFeeRepayPlan> gpsFeeRepayPlanMap){
		if (loanRepayDetailObject == null){
			return BigDecimal.ZERO;
		}

		AdditionalFeeRepayPlan additionalFeeRepayPlan = additionalFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
		BigDecimal a2 = additionalFeeRepayPlan == null || additionalFeeRepayPlan.getApActualRepayAmount() == null ? BigDecimal.ZERO : additionalFeeRepayPlan.getApActualRepayAmount();
		BigDecimal a3 = additionalFeeRepayPlan == null || additionalFeeRepayPlan.getApRealPenalSum() == null ? BigDecimal.ZERO : additionalFeeRepayPlan.getApRealPenalSum();

		AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan = gpsFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
		BigDecimal a4 = gpsAdditionalFeeRepayPlan == null || gpsAdditionalFeeRepayPlan.getApActualRepayAmount() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApActualRepayAmount();
		BigDecimal a5 = gpsAdditionalFeeRepayPlan == null || gpsAdditionalFeeRepayPlan.getApRealPenalSum() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApRealPenalSum();

		return a2.add(a3).add(a4).add(a5);
	}
}


package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import com.zlhj.unifiedInputPlatform.ant.dto.CreditDTO;
import com.zlhj.unifiedInputPlatform.ant.dto.SupplementCategoryInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ClueStatusNotifyDTO {

    /**
     * 机构侧唯一业务编号
     */
    private String outApplyNo;

    /**
     * 星河侧唯一业务编号
     */
    private String applyNo;

    /**
     * 状态
     */
    private String status;

    /**
     * 拒绝原因错误码
     */
    private String refuseCode;

    /**
     * 拒绝原因
     */
    private String refuseMsg;

    /**
     * 授信金额，单位分
     */
    private BigDecimal creditAmt;

    /**
     * 资方机构名称
     */
    private String finOrg;

    /**
     * 资方支用号
     */
    private String finDrawdownNo;

    /**
     * 机构支用号
     */
    private String orgDrawdownNo;

    /**
     * 放款金额，单位分
     */
    private BigDecimal loanAmt;

    /**
     * 放款利率
     */
    private BigDecimal loanRate;

    /**
     * 客户放款成功日期，yyyyMM-dd HH:mm:ss
     */
    private String loanDate;

    /**
     * 贷款期次
     */
    private Integer loanTerm;

    /**
     * 客户贷款期次类型，Y、M、D分别代表年月日
     */
    private String loanTermUnit;

    /**
     * 还款方式
     * 1：等额本金
     * 2：等额本息
     * 3：先息后本
     * 4：一次性结清本息
     */
    private String repayType;

    /**
     * 抵押率，授信金额/车辆估值金额
     */
    private String mortgageRate;

    /**
     * 车辆估值金额，单位分
     */
    private String valuatePrice;

    private List<CreditDTO> creditList;//授信信息列表

    private String approveDate;
    private String customerName;
    private String customerPhone;
    private String carLicenseNo;
    private String initialFinalJointReview;
    private String incomeProofNeeded;
    private List<SupplementCategoryInfo> supplementCategoryInfo;

}

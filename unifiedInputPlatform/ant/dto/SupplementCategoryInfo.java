package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplementCategoryInfo {
    /**
     * 需要补充信息的类别枚举:
     * payment_log_list: 交易流水信息【此项只支持初审阶段进行补充】
     * ffirst_contact: 联系人1
     * secondary_contact: 联系人2
     * residence_address: 居住地址
     * employer_info: 单位信息
     * driving_license_info: 行驶证信息
     * registration_certificate_info: 登记证信息
     * borrower_marital_info: 婚姻信息
     * vehicle_insurance_policy_info: 车险保单
     * other_supplementary_info: 其他补充信息
     * 后续可能继续扩展, 请做好兼容
     */
    private String supplementCategory;
    /**
     * 需要补充的流水金额，当supplement_category_code="payment_log_list"可选
     */
    private BigDecimal supplementPaymentAmt;

    public SupplementCategoryInfo(BigDecimal supplementPaymentAmt) {
        this.supplementCategory = "payment_log_list";
        this.supplementPaymentAmt = supplementPaymentAmt;
    }
}

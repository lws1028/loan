package com.zlhj.unifiedInputPlatform.ant.dto;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreditEvaluationResult {
    @JSONField(name = "id")
    private Integer id;
    
    @JSONField(name = "created_by")
    private String createdBy;

    @JSONField(name = "created_date")
    private String createdDate;
    
    @JSONField(name = "updated_by")
    private String updatedBy;
    

    @JSONField(name = "updated_date")
    private String updatedDate;
    
    @JSONField(name = "result_code")
    private String resultCode;
    
    @JSONField(name = "result_desc")
    private String resultDesc;
    
    @JSONField(name = "city")
    private String city;
    
    @JSONField(name = "province")
    private String province;
    
    @JSONField(name = "bo_id")
    private String boId;
    
    @JSONField(name = "id_card")
    private String idCard;
    
    @JSONField(name = "name")
    private String name;
    
    @JSONField(name = "phone")
    private String phone;
    
    @JSONField(name = "car_brand_name")
    private String carBrandName;
    
    @JSONField(name = "car_license")
    private String carLicense;

    @JSONField(name = "car_license_date")
    private String carLicenseDate;
    
    @JSONField(name = "car_mileage")
    private BigDecimal carMileage;
    
    @JSONField(name = "car_model_name")
    private String carModelName;
    
    @JSONField(name = "car_price")
    private BigDecimal carPrice;
    
    @JSONField(name = "purchase_method")
    private String purchaseMethod;
    
    @JSONField(name = "car_series_name")
    private String carSeriesName;
    
    @JSONField(name = "car_year")
    private Integer carYear;
    
    @JSONField(name = "loan_amount")
    private BigDecimal loanAmount;
    
    @JSONField(name = "clue_channel_category")
    private String clueChannelCategory;
    
    @JSONField(name = "order_id")
    private String orderId;
    
    @JSONField(name = "clue_channel_code")
    private Integer clueChannelCode;
    
    @JSONField(name = "apply_type")
    private String applyType;
    
    @JSONField(name = "identity_verified")
    private String identityVerified;
}
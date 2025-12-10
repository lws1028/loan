package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ApplicationInfoDTO {
    @ApiModelProperty(value = "认证申请单号", required = true)
    private String applyNO;

    @ApiModelProperty(value = "认证类型", required = true)
    private String verifyType;

    @ApiModelProperty(value = "认证时间", required = true)
    private String verifyTime;

    @ApiModelProperty(value = "认证供应商", required = true)
    private String verifySupplierName;
}

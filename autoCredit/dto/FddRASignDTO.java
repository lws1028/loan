package com.zlhj.unifiedInputPlatform.autoCredit.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class FddRASignDTO {

    @ApiModelProperty(value = "认证信息", required = true)
    ApplicationInfoDTO applicationInfo;

    @ApiModelProperty(value = "申请人信息", required = true)
    UserInfoDTO userInfo;

}

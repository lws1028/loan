package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class FddRASignQueryDTO {

    @ApiModelProperty(value = "签约事务流水号")
    String followId;

    public FddRASignQueryDTO() {
    }

    public FddRASignQueryDTO(String followId) {
        this.followId = followId;
    }
}

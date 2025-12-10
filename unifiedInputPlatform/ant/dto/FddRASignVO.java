package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;


import com.zlhj.unifiedInputPlatform.ant.dto.FileDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel
public class FddRASignVO {

    @ApiModelProperty(value = "签约结果  1-完成，2-签署中，3-失败" ,required = true)
    String state;

    @ApiModelProperty(value = "结果信息")
    String message;

    @ApiModelProperty(value = "签约事务流水号")
    String followId;

    @ApiModelProperty(value = "已签约文件清单")
    List<FileDTO> fileList;

}

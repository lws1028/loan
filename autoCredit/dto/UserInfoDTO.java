package com.zlhj.unifiedInputPlatform.autoCredit.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UserInfoDTO {

    @ApiModelProperty(value = "姓名", required = true)
    private String name;

    @ApiModelProperty(value = "身份证号码", required = true)
    private String idCertNo;

    @ApiModelProperty(value = "身份证人像面影像凭证号", required = true)
    private String idCardHeadPicture;

    @ApiModelProperty(value = "身份证国徽面影像凭证号", required = true)
    private String idCardBackPicture;

    @ApiModelProperty(value = "人脸核身图片", required = true)
    private String  userFaceCheckPicture;

}

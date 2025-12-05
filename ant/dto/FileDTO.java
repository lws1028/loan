package com.zlhj.unifiedInputPlatform.ant.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class FileDTO {

    @ApiModelProperty(value = "文件名称", required = true)
    private String fileName;

    @ApiModelProperty(value = "文件下载地址", required = true)
    private String fileUrl;

    @ApiModelProperty(value = "文件类型", required = true)
    private String fileType;

    public FileDTO() {
    }

    public FileDTO(String fileName, String fileUrl, String fileType) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
    }
}

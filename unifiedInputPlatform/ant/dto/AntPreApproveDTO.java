package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import lombok.Data;

@Data
public class AntPreApproveDTO {
    private String boId;
    private String identityVerification;
    private String vehicle;
    private String vehicleValuation;
    private String fileList;
    private String other;
}
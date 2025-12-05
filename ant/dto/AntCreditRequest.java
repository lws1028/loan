package com.zlhj.unifiedInputPlatform.ant.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AntCreditRequest {
    private String boId;//线索编号
    private IdentityVerification identityVerification;//核身信息
    private Vehicle vehicle;//⻋辆信息
    private VehicleValuation vehicleValuation;//⻋辆估值信息
    private Map<String, Object> other;//其他信息
}
package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import com.zlhj.unifiedInputPlatform.ant.dto.Vehicle;
import com.zlhj.unifiedInputPlatform.ant.dto.VehicleValuation;
import lombok.Data;

import java.util.Map;

@Data
public class AntCreditRequest {
    private String boId;//线索编号
    private com.zlhj.unifiedInputPlatform.ant.dto.IdentityVerification identityVerification;//核身信息
    private Vehicle vehicle;//⻋辆信息
    private VehicleValuation vehicleValuation;//⻋辆估值信息
    private Map<String, Object> other;//其他信息
}
package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import lombok.Data;

@Data
public class VehicleValuation {
    private String valuateTime;//评估时间，yyyy-MM-dd hh:mm:ss
    private Integer value;//评估价值，单位：分
}
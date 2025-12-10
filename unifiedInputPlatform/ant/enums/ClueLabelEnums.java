package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClueLabelEnums {

    RUNNING_WATER_NEED_REPLENISHED("0", "流水待补充", "RUNNING_WATER_TO_BE_REPLENISHED"),
    RUNNING_WATER_TIMEOUT("3", "流水待补充-超时", "RUNNING_WATER_TO_BE_TIMEOUT"),
    RUNNING_WATER_REMINDER("2", "流水待补充-催办", "RUNNING_WATER_TO_BE_REMINDER"),
    RUNNING_WATER_REPLENISHED("1", "流水已补充", "RUNNING_WATER_REPLENISHED"),
    RUNNING_WATER_CANCEL("4", "流水待补充-取消", "RUNNING_WATER_CANCEL");

    private final String code;

    private final String name;

    private final String labelType;

    public static ClueLabelEnums getByLabelType(String labelType) {
        for (ClueLabelEnums clueLabelEnums : values()) {
            if (clueLabelEnums.getLabelType().equals(labelType)) {
                return clueLabelEnums;
            }
        }
        return null;
    }

}
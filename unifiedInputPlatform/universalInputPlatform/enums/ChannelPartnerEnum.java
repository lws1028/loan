package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.enums;

import com.zlhj.unifiedInputPlatform.ant.exceptions.IllegalChannelPartnerException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum ChannelPartnerEnum {
    ANT("16", "蚂蚁金融"),
    ZGC("22", "中关村银行"),
    JD("23", "新京东金融"),
    JD_CAR_FILE("24", "京东车生活"),
	KP("28", "靠谱金服"),
	YSF_XA("30", "云闪付-新安");

    private final String code;
    private final String description;

    ChannelPartnerEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescriptionByChannelPartnerEnumName(String enumName) {
        return Arrays.stream(ChannelPartnerEnum.values())
                .filter(value -> Objects.equals(value.getCode(), enumName))
                .map(ChannelPartnerEnum::getDescription)
                .findFirst().orElseThrow(() -> new IllegalChannelPartnerException(enumName));
    }
}
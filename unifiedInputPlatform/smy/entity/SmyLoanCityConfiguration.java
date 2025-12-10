package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 萨摩耶进件城市配置
 *
 * @author : LXY
 * @date : 2023/6/5 14:32
 */
@Data
@TableName(value = "SMY_LOAN_CITY_CONFIGURATION")
@KeySequence(value = "SMY_CITY_CONFIGURATION_SEQ")
public class SmyLoanCityConfiguration {

    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.INPUT)
    private Integer id;

    /**
     * 城市id
     */
    private String cityId;

    /**
     * 省Id
     */
    private String superiorId;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 萨摩耶城市名称
     */
    private String smyCityName;

    /**
     * 小米城市名称
     */
    private String miCityName;

}

package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 58同城工具类
 */
@Slf4j
public class BJ58Util {

    private BJ58Util() { }

    /**
     * 对调用状态进行转码
     */
    public static Integer loanStatusTranslate(String status) {
        Integer str = null;
        switch (status) {
            case "审批通过":
                str = 40;
                break;
            case "放款成功":
                str = 60;
                break;
            case "业务受理拒绝":
                str = 11;
                break;
            case "业务受理通过":
                str = 20;
                break;
            case "放款审批通过":
                str = 50;
                break;
            //预审批自动审批或人工审批
            case "预审通过":
                str = 30;
                break;
            default:
                break;
        }
        log.info("同步58同城渠道状态接口,入参转换后码值为：[{}]", str);
        return str;
    }

}

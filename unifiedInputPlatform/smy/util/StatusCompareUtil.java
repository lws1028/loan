package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : LXY
 * @date : 2023/6/5 16:05
 */
@Component
@Slf4j
public class StatusCompareUtil {

    /**
     * 根据传入状态与当前订单状态进行对比
     * true:可以调用接口，数据更新操作  false:同级别状态不调用接口，不进行数据更新
     * 同级别状态不能共存，级别依次增大
     *
     * @param status    当前状态
     * @param oldStatus 数据库中状态
     * @return
     */
    public static boolean comparisonStstus(String status, String oldStatus) {
        log.info("萨摩耶状态对比传入参数：status:[{}];oldStatus:[{}]", status, oldStatus);
        boolean result = false;
        //N：拒绝进件 W：同意进件 S：审批通过 R：审批拒绝 F：放款失败 L：放款成功
        // 300-线索已收集 306-电话跟进中 302-客户⽆意向 301-客户有意向 P010-终审通过 P054-终审拒绝 P053-合同已放款
        if (!"P054".equals(oldStatus) && !"P053".equals(oldStatus) && !"302".equals(oldStatus)) {
            if ("306".equals(status) || "300".equals(status)) {
                if (!"P010".equals(oldStatus) && !"301".equals(oldStatus) && !"306".equals(oldStatus)) {
                    result = true;
                }
            } else if ("301".equals(status) ||"302".equals(status)) {
                if (!"P010".equals(oldStatus) && !"301".equals(oldStatus)) {
                    result = true;
                }
            } else if ("P010".equals(status)) {
                if (!"P010".equals(oldStatus)) {
                    result = true;
                }
            } else if ("P053".equals(status) || "P054".equals(status)) {
                if (!"P053".equals(oldStatus) && !"302".equals(oldStatus) && !"P054".equals(oldStatus)) {
                    result = true;
                }
            }
        }
        log.info("萨摩耶状态对比结果：result:[{}]", result);
        return result;
    }

    /**
     * 对调用状态进行转码
     *
     * @param status
     * @return
     */
    public static String loanStatusTranslate(String status) {
        String str = "";
        switch (status) {
            case "线索已收集":
                str = "300";
                break;
            case "电话跟进中":
                str = "360";
                break;
            case "业务受理拒绝":
                str = "302";
                break;
            case "客户有意向":
                str = "301";
                break;
            case "审批通过":
                str = "P010";
                break;
            case "审批拒绝":
                str = "P054";
                break;
            case "放款成功":
                str = "P053";
                break;
            default:
                break;
        }
        log.info("同步萨摩耶渠道状态接口,入参转换后码值为：[{}]", str);
        return str;
    }
}

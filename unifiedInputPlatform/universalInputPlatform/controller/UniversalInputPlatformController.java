package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.controller;

import cn.hutool.json.JSONUtil;
import com.zlhj.commonLoan.business.gtzl.annotation.RequirePkg;
import com.zlhj.unifiedInputPlatform.ant.dto.ClueStatusNotifyDTO;
import com.zlhj.unifiedInputPlatform.ant.dto.UnifiedInputPlatformBillReq;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.enums.ChannelPartnerEnum;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.service.UniversalInputPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 渠道统一进件控制器
 *
 * @author : wangwenhao
 * @since : 2025/9/3 11:14
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("universalInputPlatform")
public class UniversalInputPlatformController {
    @Resource
    private UniversalInputPlatformService universalInputPlatformService;

    @RequirePkg
    @GetMapping("bill/status")
    public ClueStatusNotifyDTO billStatus(UnifiedInputPlatformBillReq unifiedInputPlatformBillReq) {
        String source = unifiedInputPlatformBillReq.getSource();
        String channelPartner = ChannelPartnerEnum.getDescriptionByChannelPartnerEnumName(source);
        String interfaceFunction = channelPartner + "线索进件查询";
        log.info("{} 请求参数 = {}", interfaceFunction, unifiedInputPlatformBillReq);
        ClueStatusNotifyDTO clueStatus = universalInputPlatformService.billStatus(unifiedInputPlatformBillReq);
        log.info("{} 返回参数 = {}", interfaceFunction, JSONUtil.toJsonPrettyStr(clueStatus));
        return clueStatus;
    }
}

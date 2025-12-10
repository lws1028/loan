package com.zlhj.unifiedInputPlatform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zlhj.commonLoan.business.appCommon.pojo.CreditDistributionDTO;
import com.zlhj.commonLoan.business.appCommon.service.CreditAuthorizationService;
import com.zlhj.commonLoan.business.common.service.ChannelClueConsumerService;
import com.zlhj.mq.dto.*;
import com.zlhj.unifiedInputPlatform.ant.dto.FddRASignVO;
import com.zlhj.unifiedInputPlatform.ant.service.AntService;
import com.zlhj.unifiedInputPlatform.autoCredit.core.CreditPreApproveHandleFactory;
import com.zlhj.unifiedInputPlatform.jdjr.service.JdJrService;
import com.zlhj.unifiedInputPlatform.jdjt.service.JdJtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ChannelClueConsumer {
    private final ChannelClueConsumerService channelClueConsumerService;
    private final AntService antService;
    private final CreditAuthorizationService creditAuthorizationService;
    private final JdJtService jdJtService;
    private final JdJrService jdJrService;
    public ChannelClueConsumer(ChannelClueConsumerService channelClueConsumerService, AntService antService, CreditAuthorizationService creditAuthorizationService, JdJtService jdJtService, JdJtService jdJtService1, JdJrService jdJrService) {
        this.channelClueConsumerService = channelClueConsumerService;
		this.antService = antService;
		this.creditAuthorizationService = creditAuthorizationService;
		this.jdJtService = jdJtService1;
		this.jdJrService = jdJrService;
	}

    @RabbitListener(queues = "channelClueQueue")
    public void  channelClue(String source){
        log.info("处理渠道申请贷款线索开始，线索 = {}",source);
        ChannelClueDTO channelClueDTO = JSON.parseObject(source, ChannelClueDTO.class);
        channelClueConsumerService.consumer(channelClueDTO);
    }
    //该方法生产方为电销平台
    @RabbitListener(queues = "clue-preApprove-retry-core-queue")
    public void antPreApproveRetry(AntPreApproveMessage message) {
        log.info("处理蚂蚁03接口预审通知，参数={}", JSON.toJSONString(message));
        try {
            antService.antPreApprove(message);
        } catch (Exception e) {
            log.error("处理蚂蚁03接口预审通知失败, msg: [{}] ", e.getMessage(),e);
            throw e;
        }
    }

    //该方法生产方为签约平台
    @RabbitListener(queues = "ant-fdd-sign-complete-core-queue")
    public void antFddSignComplete(String message) {
        log.info("蚂蚁法大大签约完成核心系统收到通知开始处理，参数={}", message);
        try {
            antService.fddSignComplete(JSONObject.parseObject(message,FddRASignVO.class));
        } catch (Exception e) {
            log.error("蚂蚁法大大签约完成核心处理失败, msg: [{}] ", e.getMessage(),e);
            throw e;
        }
    }
    //该方法生产方为电销平台
    @RabbitListener(queues = "credit-distribution-queue")
    public void clueDistribution(List<CreditDistributionDTO> message) {
        log.info("电销重新分配线索后去除原坐席的征信等保护关系核心系统收到通知开始处理，参数={}", message);
        try {
            creditAuthorizationService.reallocateCreditReports(message);
        } catch (Exception e) {
            log.error("电销重新分配线索后去除原坐席的征信等保护关系处理失败, msg: [{}] ", e.getMessage(),e);
            throw e;
        }
    }

    //该方法生产方为电销平台
    @RabbitListener(queues = "clue-show-queue")
    public void clueDistribution(ClueShowPushMessage message) {
        log.info("线索展示核心系统收到通知开始处理，参数={}", message);
        try {
            antService.incomeProofFinished(message);
        } catch (Exception e) {
            log.error("线索展示处理失败, msg: [{}] ", e.getMessage(),e);
            throw e;
        }
    }

    //该方法生产方为电销平台
    @RabbitListener(queues = "jd-clue-preApprove-core-queue")
    public void jdCluePreApprove(JdJtPreApproveMessage message) {
        log.info("处理京东预审通知，参数={}", JSON.toJSONString(message));
        try {
            jdJtService.autoPreApprove(message);
        } catch (Exception e) {
            log.error("处理京东预审通知失败, msg: [{}] ", e.getMessage(),e);
            throw e;
        }
    }
    //该方法生产方为电销平台
    @RabbitListener(queues = "clue-preApprove-core-queue")
    public void cluePreApprove(PreApproveMessage message) {
        log.info("处理自动预审通知，参数={}", JSON.toJSONString(message));
        try {
            new CreditPreApproveHandleFactory()
                    .create(message)
                    .executePreApprove(message);
        } catch (Exception e) {
            log.error("处理自动预审通知失败, msg: [{}] ", e.getMessage(),e);
            throw e;
        }
    }
}

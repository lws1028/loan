package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.service;

import com.zlhj.unifiedInputPlatform.bj58.dto.*;

/**
 * 58同城 服务接口
 */
public interface BJ58Service {

    /**
     *  提交进件信息
     */
    public CluesInformationResponse submit(CluesInformationRequest request);

    /**
     * 线索状态查询接口
     */
    public CluesInformationSearchResponse query(CluesInformationSearch search) throws Exception;

    /**
     * 线索状态回调通知接口
     */
    public CluesInformationResult callbackNotify(CluesInformationNotifySend send);

    /**
     * 58同城 30天超期定时任务
     */
    public void bj58OverdueScheduleTask() throws Exception;

    /**
     * 58 实时交互触发
     */
    public int bj58RealTimeInteraction(ClueInformationRealTime realTime);


    /**
     * 58同城运维 推送数据（可传入多个orderId，通过，区分即可）
     */
    public CluesInformationOperationResponse operationsBJ58(CluesInformationOperationRequest request);
}

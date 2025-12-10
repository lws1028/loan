package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.service;

import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.unifiedInputPlatform.smy.dto.CustomerCheckParam;
import com.zlhj.unifiedInputPlatform.smy.dto.OperationSendSmyStatusDto;
import com.zlhj.unifiedInputPlatform.smy.dto.SmyResponse;

/**
 * 萨摩耶大额导流 服务接口
 *
 * @author LXY
 * 2023年6月2日14:57:53
 */
public interface SmyService {

    /**
     * 用户准入（撞库）
     *
     * @param customerCheckParam
     * @return
     */
    SmyResponse customerCheck(CustomerCheckParam customerCheckParam);

    /**
     * 信息提交接口
     *
     * @param content
     * @return
     */
    SmyResponse infoSubmit(String content);

    /**
     * 运维推送萨摩耶状态同步接口
     *
     * @param operationSendSmyStatusDto
     * @return
     */
    ResultDto operationSendSmyStatusInterface(OperationSendSmyStatusDto operationSendSmyStatusDto) throws Exception;
}

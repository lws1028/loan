package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.controller;

import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.unifiedInputPlatform.smy.dto.CustomerCheckParam;
import com.zlhj.unifiedInputPlatform.smy.dto.OperationSendSmyStatusDto;
import com.zlhj.unifiedInputPlatform.smy.dto.SmyResponse;
import com.zlhj.unifiedInputPlatform.smy.service.SmyService;
import com.zlhj.util.ToolsUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 萨摩耶大额导流 控制层
 *
 * @author LXY
 * 2023年6月2日14:59:08
 */
@Slf4j
@RequestMapping("/smy/clue")
@RestController
@CrossOrigin
public class SmyController {

    private final SmyService smyService;

    public SmyController(SmyService smyService) {
        this.smyService = smyService;
    }

    /**
     * 用户准入（撞库）
     */
    @PostMapping("/customerCheck")
    public SmyResponse customerCheck(@RequestBody CustomerCheckParam customerCheckParam) {
        return smyService.customerCheck(customerCheckParam);
    }

    /**
     * 信息提交接口
     */
    @PostMapping("/infoSubmit")
    public SmyResponse infoSubmit(HttpServletRequest request) {
        SmyResponse response = SmyResponse.builder()
                .code("999999").msg("处理异常")
                .build();
        try {
            String content = ToolsUtil.getContent(request);
            response = smyService.infoSubmit(content);
        } catch (Exception e) {
            log.error("客户防撞查询接口异常，原因：e = [{}]", e.getMessage(), e);
        }
        return response;
    }

    @PostMapping("operationSendSmyStatusInterface")
    @ApiOperation("运维推送萨摩耶状态同步接口")
    public ResultDto operationSendSmyStatusInterface(@RequestBody OperationSendSmyStatusDto operationSendSmyStatusDto){
        log.info("运维推送萨摩耶状态同步接口,入参：[{}]", operationSendSmyStatusDto.toString());
        try {
            return smyService.operationSendSmyStatusInterface(operationSendSmyStatusDto);
        } catch (Exception e) {
            log.error("运维推送萨摩耶状态同步接口异常，原因：e = [{}]",e.getMessage(),e);
            return new ResultDto(0, "操作失败");
        }
    }
}

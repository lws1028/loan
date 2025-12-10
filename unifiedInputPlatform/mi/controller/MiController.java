package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.controller;

import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.unifiedInputPlatform.LoanStatusInteraction;
import com.zlhj.unifiedInputPlatform.mi.dto.CarApplyDTO;
import com.zlhj.unifiedInputPlatform.mi.dto.ClueStateNotifyDTO;
import com.zlhj.unifiedInputPlatform.mi.dto.MiClueDTO;
import com.zlhj.unifiedInputPlatform.mi.service.MiOMService;
import com.zlhj.unifiedInputPlatform.mi.service.MiService;
import com.zlhj.unifiedInputPlatform.mi.service.SqlInterfaceService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小米贷款线索 控制层
 */
@Slf4j
@RequestMapping("/mi/clue")
@RestController
@CrossOrigin
public class MiController {

    private final MiService miService;
    private final SqlInterfaceService sqlInterfaceService;
    private final LoanStatusInteraction loanStatusInteraction;
    private final MiOMService miOMService;
    public MiController(MiService miService,
                        SqlInterfaceService sqlInterfaceService,
                        LoanStatusInteraction loanStatusInteraction,
                        MiOMService miOMService) {
        this.miService = miService;
        this.sqlInterfaceService = sqlInterfaceService;
        this.loanStatusInteraction = loanStatusInteraction;
        this.miOMService = miOMService;
    }


    /**
     * ⻋抵贷线索
     */
    @PostMapping("/carApply")
    public ResultDto customerCheck(@RequestBody CarApplyDTO carApplyDTO) {
        log.info("小米车抵贷线索接收.....入参={}",carApplyDTO);
        try {
            miService.clueCarApply(carApplyDTO);
            log.info("小米车抵贷线索接收.....处理成功");
            return new ResultDto(0,"成功");
        } catch (IllegalArgumentException e){
            log.info("小米车抵贷线索接收验证参数未通过校验,verifyInfo = {}",e.getMessage());
            ResultDto resultDto = new ResultDto(10002, e.getMessage());
            sqlInterfaceService.saveInterfaceInfo(String.valueOf(resultDto),0,String.valueOf(carApplyDTO),"小米车抵贷线索");
            return resultDto;
        }catch (Exception e) {
            log.info("小米车抵贷线索接收处理数据异常,errorInfo = []",e);
            ResultDto resultDto = new ResultDto(90000,"未知错误");
            sqlInterfaceService.saveInterfaceInfo(String.valueOf(resultDto),0,String.valueOf(carApplyDTO),"小米车抵贷线索");
            return resultDto;
        }
    }
    @PostMapping("operationSendMiStatusInterface")
    @ApiOperation("运维推送小米贷款超市状态同步接口")
    public ResultDto operationSendMiStatusInterface(@RequestBody MiClueDTO miClueLoanId){
        log.info("运维推送小米贷款超市状态同步接口,入参：[{}]", miClueLoanId);
        try {
            miOMService.operationSendMiStatusInterface(miClueLoanId.getMiApplyNumber());
            return ResultDto.submitSuccess();
        } catch (Exception e) {
            log.error("运维推送小米贷款超市状态同步接口异常，原因：e = [{}]",e.getMessage(),e);
            return new ResultDto(0, "操作失败");
        }
    }

    @PostMapping("testSendMiStatusInterface")
    @ApiOperation("测试推送小米贷款超市状态同步接口")
    public ResultDto testSendMiStatusInterface(@RequestBody ClueStateNotifyDTO carApplyDTO){
        try {
            Map<String, String> sendResult = loanStatusInteraction.sendMiClueStateNotify(carApplyDTO,null);

            return ResultDto.submitSuccess(sendResult);
        } catch (Exception e) {
            log.error("测试推送小米贷款超市状态同步接口异常，原因：e = [{}]",e.getMessage(),e);
            return new ResultDto(0, "操作失败");
        }
    }
}

package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.controller;

import com.zlhj.hrxj.business.dto.ResultDto;
import com.zlhj.unifiedInputPlatform.bj58.dto.CluesInformationOperationRequest;
import com.zlhj.unifiedInputPlatform.bj58.dto.CluesInformationOperationResponse;
import com.zlhj.unifiedInputPlatform.bj58.dto.CluesInformationRequest;
import com.zlhj.unifiedInputPlatform.bj58.dto.CluesInformationSearch;
import com.zlhj.unifiedInputPlatform.bj58.service.BJ58Service;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/BJ58/clue")
@RestController
@CrossOrigin
public class BJ58Controller {

    private final BJ58Service bj58Service;

    public BJ58Controller(BJ58Service bj58Service) {
        this.bj58Service = bj58Service;
    }


    /**
     *  提交进件信息
     */
    @PostMapping("/submit")
    public ResultDto submit(@RequestBody CluesInformationRequest request) {
        return new ResultDto(200,
                "成功",
                bj58Service.submit(request));
    }

    /**
     * 线索状态查询接口
     */
    @PostMapping("/query")
    public ResultDto query(@RequestBody CluesInformationSearch search) {
        try {
            return new ResultDto(200,
                    "成功",
                    bj58Service.query(search));
        } catch (Exception e) {
            return new ResultDto(500,
                    e.getMessage(),
                    null);
        }
    }

    @PostMapping("/operationsBJ58")
    public CluesInformationOperationResponse operationsBJ58(@RequestBody CluesInformationOperationRequest request) {
        return bj58Service.operationsBJ58(request);
    }

}

package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LXY
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmyResponse {

    /**
     * 接口状态码
     */
    private String code;
    /**
     * 接口状态码描述
     */
    private String msg;
    /**
     * 业务状态码
     */
    private String dealStatus;
    /**
     * 业务状态码描述
     */
    private String dealDesc;


}

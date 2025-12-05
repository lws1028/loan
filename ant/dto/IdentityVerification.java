package com.zlhj.unifiedInputPlatform.ant.dto;

import lombok.Data;

@Data
class IdentityVerification {
    private String verifyType;//核身⽅式枚举    FACE：⼈脸
    private String verifyTime;//核身时间，yyyy-MM-dd hh:mm:ss
}
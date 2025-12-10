package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.service;

import com.zlhj.externalChannel.vo.SplBussinessBasicObject;

public interface MiSendEmailService {
    void send(SplBussinessBasicObject splBussinessBasic,String pullState);
}

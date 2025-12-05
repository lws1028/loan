package com.zlhj.unifiedInputPlatform.ant.utils;

import com.apollo.util.DateUtil;
import com.zlhj.redis.service.CommonSeq;
import com.zlhj.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZlhjToAntBatchNumber {

    @Autowired
    private RedisService redisService;

    /**
     * 生成机构侧编号（提供于蚂蚁金融方）
     *
     * @return
     */
    public String createBatchNumber() {
        CommonSeq commonSeq = new CommonSeq();
        String currDate = DateUtil.getDate();
        return "ZQHT" + currDate + commonSeq.addZeroLeft(
                String.valueOf(
                        redisService.getIncrementNum("alipay" + currDate, DateUtil.currentMidNight())
                ), 8);
    }
}

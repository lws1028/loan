package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

/**
 * @author : LXY
 * @date : 2023/6/2 18:13
 */
@Component
public class ParamCheckUtil {

    /**
     * 校验参数为空
     *
     * @param json
     * @param paramName
     * @param errorMsg
     * @return
     */
    public String ifNull(JSONObject json, String paramName, StringBuffer errorMsg) {
        if (null == json.getString(paramName) || "".equals(json.getString(paramName))) {
            errorMsg.append("{" + paramName + "}不可为空；");
        }
        return json.getString(paramName);
    }

}

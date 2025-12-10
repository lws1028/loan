package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.apollo.alds.util.DAOUtil;
import com.zlhj.externalChannel.vo.InterfaceInfoObject;
import com.zlhj.unifiedInputPlatform.mi.service.SqlInterfaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
@Service
@Slf4j
public class SqlInterfaceServiceImpl implements SqlInterfaceService {
    public boolean saveInterfaceInfo(String resMap, int branchLoanID, String infoSubmitParam, String interfaceName) {
        InterfaceInfoObject interfaceInfo = new InterfaceInfoObject(null, branchLoanID, 0, interfaceName,
                2, JSONObject.toJSONString(resMap == null ? infoSubmitParam : resMap, SerializerFeature.WriteMapNullValue),
                new Timestamp(System.currentTimeMillis()));
        return DAOUtil.store(interfaceInfo);
    }

    @Override
    public boolean saveSendInterfaceInfo(String message, Integer branchLoanID, String interfaceName) {
        InterfaceInfoObject interfaceInfo = new InterfaceInfoObject(null, branchLoanID, 0, interfaceName,
                1, JSONObject.toJSONString(message, SerializerFeature.WriteMapNullValue),
                new Timestamp(System.currentTimeMillis()));
        return DAOUtil.store(interfaceInfo);
    }
}

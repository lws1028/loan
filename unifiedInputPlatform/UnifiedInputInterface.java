package com.zlhj.unifiedInputPlatform.unifiedInputPlatform;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.apollo.alds.util.ConvertionUtil;
import com.apollo.alds.util.DAOUtil;
import com.zlhj.externalChannel.vo.InterfaceInfoObject;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.externalChannel.vo.SplUserInfoObject;
import com.zlhj.infrastructure.message.publisher.ClueApplicationPublisher;
import com.zlhj.util.json.FastJsonConversionUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 接收统一进件资料数据
 *
 * @author ahs
 * @Date 2019-8-28
 */
@AllArgsConstructor
@Slf4j
@Service
public class UnifiedInputInterface {

    private final ClueApplicationPublisher clueApplicationPublisher;

    /**
     * 接收统一进件接口数据
     *
     * @param map
     * @return
     */
    public Map<String, Object> saveBasicData(Map map) {

        log.info("接收中转发送数据：" + map);
        Map<String, Object> resMap = new HashMap<String, Object>(16);

        //解析中转发送数据
        SplBussinessBasicObject businessBasicObj = FastJsonConversionUtil.mapToBean(map, SplBussinessBasicObject.class);
        SplUserInfoObject userInfoObj = FastJsonConversionUtil.mapToBean(map, SplUserInfoObject.class);
        this.clueApplicationPublisher.cluePhoneFiltered(userInfoObj.getPhone(), "98");
        //判断接口传参是否有申请单号(流水号)
        if ("".equals(ConvertionUtil.getSimpleStringWithNull(businessBasicObj.getApplyNum()))) {
            //流水号为空，直接返回错误信息
            resMap.put("STATUS", "N");
            resMap.put("APPLY_ID", ConvertionUtil.getSimpleStringWithNull(businessBasicObj.getApplyNum()));
            resMap.put("MSG", "进件申请单号(流水号不能为空)");
            InterfaceInfoObject interfaceInfo = new InterfaceInfoObject(null, 0, 0, "save.unifiedInputPlatform.loan",
                    2, JSONObject.toJSONString(resMap, SerializerFeature.WriteMapNullValue), new Timestamp(System
                    .currentTimeMillis()), ConvertionUtil.getSimpleStringWithNull(businessBasicObj.getApplyNum()),
                    null, null);
            DAOUtil.store(interfaceInfo);
        } else {
            //时间格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            try {
                //对生日进行日期格式转换
                if (null != userInfoObj.getBirthday() && !"".equals(userInfoObj.getBirthday().trim())) {
                    userInfoObj.setBirthday(sdf.format(sdf.parse(userInfoObj.getBirthday())));
                }
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }
            Date date = new Date();
            //时间戳格式的时间
            Timestamp submitDateTi = new Timestamp(date.getTime());
            //格式化后的时间
            Integer submitDate = ConvertionUtil.getSimpleIntegerWithNull(sdf.format(date));
            //贷款期数默认36期
            businessBasicObj.setLoanTerm(36);
            businessBasicObj.setSubmitDateTi(submitDateTi);
            businessBasicObj.setSubmitDate(submitDate);

            businessBasicObj.setProvince(ConvertionUtil.getSimpleStringWithNull(userInfoObj.getUprovince()));
            businessBasicObj.setCity(ConvertionUtil.getSimpleStringWithNull(userInfoObj.getUcity()));

            //直客申请模式 1-简易模式 2-复杂模式 3-京东模式 4-天府模式（默认1）
            businessBasicObj.setCustomerModel(1);
            //合作渠道 2-人人贷 4-行云天下 5-银联pos机 6-京东金融 7-天府熊猫贷 98-统一进件平台
            businessBasicObj.setChannelSource(98);

            Integer branchLoanID = 0;
            String applyNum = "";
            try {

                /**businessBasicObj = handlP2PBusiness(businessBasicObj, userInfoObj);*/
                businessBasicObj.setLoanState(0);

                //保存直客主表
                DAOUtil.store(businessBasicObj);

                //直客主键
                branchLoanID = businessBasicObj.getBranchLoanId();
                applyNum = ConvertionUtil.getSimpleStringWithNull(businessBasicObj.getApplyNum());
                userInfoObj.setBranchLoanId(branchLoanID);
                //保存直客信息表
                DAOUtil.store(userInfoObj);

                resMap.put("STATUS", "Y");
                resMap.put("APPLY_ID", ConvertionUtil.getSimpleStringWithNull(businessBasicObj.getApplyNum()));
                resMap.put("MSG", "进件成功");

            } catch (Exception e) {
                //删除 spl_business直客 主表
                if (branchLoanID != 0) {
                    Criteria crit1 = new Criteria();
                    crit1.addEqualTo("branchLoanId", branchLoanID);
                    QueryByCriteria query = new QueryByCriteria(SplBussinessBasicObject.class, crit1);
                    DAOUtil.remove(query);
                    log.info("remove SplBussinessBasicObject SUCCESS");
                }
                resMap.put("STATUS", "N");
                resMap.put("APPLY_ID", ConvertionUtil.getSimpleStringWithNull(businessBasicObj.getApplyNum()));
                resMap.put("MSG", "进件失败");
                log.error("save SplBussinessBasicObject exception::{}", e.getMessage(), e);
            } finally {
                log.info("接收到-统一进件平台接口提交数据:" + map);
                InterfaceInfoObject interfaceInfo = new InterfaceInfoObject(null, branchLoanID, 0, "save" +
                        ".unifiedInputPlatform.loan", 2, JSONObject.toJSONString(resMap, SerializerFeature
                        .WriteMapNullValue), new Timestamp(System.currentTimeMillis()), applyNum, null, null);
                DAOUtil.store(interfaceInfo);
            }
        }
        return resMap;
    }

}

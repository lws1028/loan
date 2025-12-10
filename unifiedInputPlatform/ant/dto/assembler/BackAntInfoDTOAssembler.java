package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto.assembler;

import com.alibaba.fastjson.JSONObject;
import com.zlhj.commonLoan.util.DateUtil;
import com.zlhj.electronicCredit.pojo.CollectionInterfaces;
import com.zlhj.hrxj.interfaces.dto.CarInfoDto;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.unifiedInputPlatform.ant.dto.BackAntInfoDTO;
import com.zlhj.unifiedInputPlatform.ant.enums.XingHeDrivingFileType;
import com.zlhj.unifiedInputPlatform.ant.enums.XingHePostBackType;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class BackAntInfoDTOAssembler {
    public BackAntInfoDTO to(CarInfoDto carInfoDto,
                             DirectCustomerStatusDto directCustomerStatusDto,
                             CollectionInterfaces carAssessPriceInfo) throws ParseException, IOException {
        String str = carAssessPriceInfo.getMsg();
        JSONObject info = JSONObject.parseObject(str, JSONObject.class);
        List<JSONObject> priceInfoList = JSONObject.parseArray(info.getJSONObject("priceInfo").getString("eval_prices"), JSONObject.class);
        JSONObject condition = priceInfoList.stream().filter(r -> "good".equals(r.get("condition"))).findFirst().orElse(new JSONObject());
        BigDecimal dealerPrice = condition.getBigDecimal("dealer_price");

        ArrayList<BackAntInfoDTO.FileInfo> fileList = new ArrayList<>();
        BackAntInfoDTO.FileInfo fileInfo = BackAntInfoDTO.FileInfo.builder()
                .type(XingHeDrivingFileType.DRIVING_LICENSE_FRONT)
                .name("行驶证正面.png")
                .id(carInfoDto.getOcrDrivingPermit()).build();
        fileList.add(fileInfo);

        ArrayList<String> postbackList = new ArrayList<>();
        postbackList.add(XingHePostBackType.valuation_info.name());
        postbackList.add(XingHePostBackType.car_info.name());
        postbackList.add(XingHePostBackType.file_list.name());

        String carDate = DateFormatUtils.format(DateUtils.parseDate(carInfoDto.getFirstDate(), new String[]{"yyyyMMdd"}), "yyyy-MM-dd");
        String format = DateFormatUtils.format(carAssessPriceInfo.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
        return BackAntInfoDTO.builder()
                .outApplyNo(directCustomerStatusDto.getSplApplyNum())
                .valuationInfo(
                        BackAntInfoDTO.ValuationInfo.builder()
                                .price(dealerPrice.multiply(new BigDecimal("10000")).longValue())
                                .time(DateUtil.parseDate(format, "yyyy-MM-dd HH:mm:ss"))
                                .supplier("车300")
                                .build()
                )
                .carInfo(
                        BackAntInfoDTO.CarInfo.builder()
                                .license(carInfoDto.getCiLicense())
                                .owner(carInfoDto.getOwnerName())
                                .address("未知")
                                .brand(carInfoDto.getCiBrandName())
                                .series(carInfoDto.getCiCarSeriesName())
                                .type(carInfoDto.getCiCarModelName())
                                .vin(carInfoDto.getCiVin())
                                .engineNo(carInfoDto.getCiEngineNo())
                                .firstRegisterDate(carDate)
                                .issueDate(carDate)
                                .build()
                )
                .fileList(fileList)
                .postbackList(postbackList)
                .build();

    }
}

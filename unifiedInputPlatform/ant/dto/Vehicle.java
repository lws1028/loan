package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import lombok.Data;

@Data
public class Vehicle {
    private String licenseNo;//⻋牌号码
    private String vinNo;//⻋辆识别代码  ⻋架号
    private String brandId;//品牌ID
    private String brandName;//品牌
    private String seriesId;//车系ID
    private String seriesName;//车系
    private String modelId;//⻋型ID
    private String modelName;//⻋型
    private String engineNo;//发动机号码
    private String regDate;//注册⽇期 yyyy-MM-dd
    private String issueDate;//发证⽇期 yyyy-MM-dd
    private String useType;//营运性质 F：⾮营运H：货运K：客运Z：租赁J：教练O：其他
}
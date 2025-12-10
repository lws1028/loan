package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.util.Assert;

import java.io.Serializable;

public class CarApplyDTO implements Serializable{
    private Long applyId;  //申请流⽔号
    private String openId;  //⽤⼾唯⼀标识
    private String applyTime;//申请时间，格式为yyyyMMddHHmmss,如20180905130200
    private UserInfo userInfo;//申请⼈信息
    private CarInfo carInfo;//⻋的信息

    public void verify(){
        Assert.notNull(applyId,"申请流⽔号不能为空");
        Assert.hasLength(openId,"⽤⼾唯⼀标识不能为空");
        Assert.hasLength(applyTime,"申请时间不能为空");
        Assert.hasLength(userInfo.getName(),"申请⼈姓名不能为空");
        Assert.hasLength(userInfo.getMobile(),"申请⼈⼿机号不能为空");
        Assert.hasLength(userInfo.getAddress(),"所在地不能为空");
        Assert.hasLength(carInfo.getCarPlate(),"牌号不能为空");
    }
    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public CarInfo getCarInfo() {
        return carInfo;
    }

    public void setCarInfo(CarInfo carInfo) {
        this.carInfo = carInfo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    public static class UserInfo implements Serializable{
        private String name;//申请⼈姓名
        private String mobile;//申请⼈⼿机号
        private String address;//所在地，具体到区/县，⽰例：河北省唐⼭市丰南区

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    public static class CarInfo implements Serializable {
        private String carPlate;//⻋牌号

        public String getCarPlate() {
            return carPlate;
        }

        public void setCarPlate(String carPlate) {
            this.carPlate = carPlate;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}

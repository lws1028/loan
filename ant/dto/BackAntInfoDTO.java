package com.zlhj.unifiedInputPlatform.ant.dto;

import com.zlhj.unifiedInputPlatform.ant.enums.XingHeDrivingFileType;
import com.zlhj.unifiedInputPlatform.ant.enums.XingHePostBackType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class BackAntInfoDTO {
    /**
     * 机构侧唯一业务编号
     */

    private String outApplyNo;

    /**
     * ⻋辆估值信息
     */
    private ValuationInfo valuationInfo;
    /**
     * ⻋辆信息
     */
    private CarInfo carInfo;
    /**
     * ⽂件列表
     */
    private List<FileInfo> fileList;
    /**
     * 传输信息列表
     * @see XingHePostBackType
     */
    private List<String> postbackList;

    @Data
    @Builder
    public static class ValuationInfo{
        /**
         * ⻋辆估值⾦额，单位元
         */
        private Long price;
        /**
         * ⻋辆估值时间，格式：yyyy-MM-dd HH:mm:ss
         */
        private Date time;
        /**
         * ⻋辆估值供应商
         */
        private String supplier;
    }

    @Data
    @Builder
    public static class CarInfo{
        /**
         * ⻋牌号
         */
        private String license;
        /**
         * 所有⼈
         */
        private String owner;
        /**
         * 地址
         */
        private String address;
        /**
         * ⻋辆品牌
         */
        private String brand;
        /**
         * ⻋辆⻋系
         */
        private String series;
        /**
         * ⻋辆型号
         */
        private String type;
        /**
         * ⻋架号
         */
        private String vin;
        /**
         * 发动机号码
         */
        private String engineNo;
        /**
         * ⾸次注册⽇期  yyyy-MM-dd
         */
        private String firstRegisterDate;
        /**
         * 发证⽇期  yyyy-MM-dd
         */
        private String issueDate;
    }

    @Data
    @Builder
    public static class FileInfo{
        /**
         * ⽂件类型
         */
        private XingHeDrivingFileType type;
        /**
         * ⽂件名称（带后缀）
         */
        private String name;
        /**
         * 影像平台文件id
         */
        private String id;
    }

}

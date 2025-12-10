package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.service.impl;

import com.zlhj.area.city.po.QRCityPO;
import com.zlhj.area.city.repository.QRCityRepository;
import com.zlhj.area.province.po.QRProvincePO;
import com.zlhj.area.province.repository.QRProvinceRepository;
import com.zlhj.unifiedInputPlatform.bj58.dto.BJ58WhiteList;
import org.springframework.stereotype.Service;

@Service
class BJ58ValidateImpl implements BJ58Validate {

    private final QRCityRepository cityRepository;

    private final QRProvinceRepository provinceRepository;

    BJ58ValidateImpl(QRCityRepository cityRepository,
                     QRProvinceRepository provinceRepository) {
        this.cityRepository = cityRepository;
        this.provinceRepository = provinceRepository;
    }

    @Override
    public boolean whiteList(BJ58WhiteList whiteList) {
        if (whiteList.isEmpty()) {
            return false;
        }
        QRProvincePO provinceCoding = provinceRepository.getByCoding(whiteList.getProvince());
        if (provinceCoding == null || provinceCoding.getQrpBj58() == null || !provinceCoding.getQrpBj58()) {
            return false;
        }
        QRCityPO cityCoding = cityRepository.getByCoding(whiteList.getCity());
        return cityCoding != null && cityCoding.getQrcBj58() != null && cityCoding.getQrcBj58();
    }
}

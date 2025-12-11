package com.zlhj.unifiedInputPlatform.autoCredit.universal.transform;


import com.apollo.util.DateUtil;
import com.zlhj.infrastructure.po.AuthorizedImage;
import com.zlhj.infrastructure.po.CreditAuthorization;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveApiRespDTO;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveDTO;
import com.zlhj.infrastructure.routing.dto.clue.JdJtCluePreApproveDTO;
import com.zlhj.unifiedInputPlatform.ant.exceptions.AntPreApproveException;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.ApplicationInfoDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.FddRASignDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.UserInfoDTO;

public class FddRASignDTOAssembler {

    public FddRASignDTO toDto(CreditAuthorization creditAuthorization,
                              CluePreApproveApiRespDTO queryApprove,
                              AuthorizedImage sfzzmImage,
                              AuthorizedImage sfzfmImage,
                              AuthorizedImage rxzpImage) throws AntPreApproveException {

        FddRASignDTO fddRASignDTO = new FddRASignDTO();
        ApplicationInfoDTO applicationInfoDTO = new ApplicationInfoDTO();
        applicationInfoDTO.setApplyNO(queryApprove.getOrderId());
        applicationInfoDTO.setVerifyType(queryApprove.getIdentityVerification().getVerifyType());
        applicationInfoDTO.setVerifyTime(queryApprove.getIdentityVerification().getVerifyTime());
        applicationInfoDTO.setVerifySupplierName("蚂蚁星河");
        fddRASignDTO.setApplicationInfo(applicationInfoDTO);

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setName(creditAuthorization.getCustomerName());
        userInfoDTO.setIdCertNo(creditAuthorization.getIdNum());
        userInfoDTO.setIdCardHeadPicture(sfzzmImage.getFilePath());
        userInfoDTO.setIdCardBackPicture(sfzfmImage.getFilePath());
        userInfoDTO.setUserFaceCheckPicture(rxzpImage.getFilePath());
        fddRASignDTO.setUserInfo(userInfoDTO);
        return fddRASignDTO;
    }

    public FddRASignDTO toDto(CreditAuthorization creditAuthorization,
                              JdJtCluePreApproveDTO queryApprove,
                              AuthorizedImage sfzzmImage,
                              AuthorizedImage sfzfmImage,
                              AuthorizedImage rxzpImage) throws AntPreApproveException {

        FddRASignDTO fddRASignDTO = new FddRASignDTO();
        ApplicationInfoDTO applicationInfoDTO = new ApplicationInfoDTO();
        applicationInfoDTO.setApplyNO(queryApprove.getBoId());
        applicationInfoDTO.setVerifyType("FACE");
        applicationInfoDTO.setVerifyTime(String.valueOf(DateUtil.getNowDate()));
        applicationInfoDTO.setVerifySupplierName("京东金条");
        fddRASignDTO.setApplicationInfo(applicationInfoDTO);

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setName(creditAuthorization.getCustomerName());
        userInfoDTO.setIdCertNo(creditAuthorization.getIdNum());
        userInfoDTO.setIdCardHeadPicture(sfzzmImage.getFilePath());
        userInfoDTO.setIdCardBackPicture(sfzfmImage.getFilePath());
        userInfoDTO.setUserFaceCheckPicture(rxzpImage.getFilePath());
        fddRASignDTO.setUserInfo(userInfoDTO);
        return fddRASignDTO;
    }
    public FddRASignDTO toDto(CreditAuthorization creditAuthorization,
                              CluePreApproveDTO queryApprove,
                              AuthorizedImage sfzzmImage,
                              AuthorizedImage sfzfmImage,
                              AuthorizedImage rxzpImage){

        FddRASignDTO fddRASignDTO = new FddRASignDTO();
        ApplicationInfoDTO applicationInfoDTO = new ApplicationInfoDTO();
        applicationInfoDTO.setApplyNO(queryApprove.getBoId());
        applicationInfoDTO.setVerifyType("FACE");
        applicationInfoDTO.setVerifyTime(String.valueOf(DateUtil.getNowDate()));
        applicationInfoDTO.setVerifySupplierName("京东金条");
        fddRASignDTO.setApplicationInfo(applicationInfoDTO);

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setName(creditAuthorization.getCustomerName());
        userInfoDTO.setIdCertNo(creditAuthorization.getIdNum());
        userInfoDTO.setIdCardHeadPicture(sfzzmImage.getFilePath());
        userInfoDTO.setIdCardBackPicture(sfzfmImage.getFilePath());
        userInfoDTO.setUserFaceCheckPicture(rxzpImage.getFilePath());
        fddRASignDTO.setUserInfo(userInfoDTO);
        return fddRASignDTO;
    }
}

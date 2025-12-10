package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.service.impl;

import com.zlhj.Interface.creditReport.vo.CreditMessageObject;
import com.zlhj.Interface.creditReport.vo.CreditMessageRepository;
import com.zlhj.electronicCredit.pojo.CollectionInterfaces;
import com.zlhj.electronicCredit.pojo.CollectionInterfacesRepository;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.hrxj.interfaces.dto.CarInfoDto;
import com.zlhj.hrxj.interfaces.dto.CarInfoRepository;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.mq.provider.Sender;
import com.zlhj.redis.service.RedisService;
import com.zlhj.unifiedInputPlatform.ant.dto.BackAntInfoDTO;
import com.zlhj.unifiedInputPlatform.ant.dto.assembler.BackAntInfoDTOAssembler;
import com.zlhj.unifiedInputPlatform.ant.service.AntBackInfoService;
import com.zlhj.user.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AntBackInfoServiceImpl implements AntBackInfoService {
    @Autowired
    private SplBussinessbasicMapper splBussinessbasicMapper;
    @Autowired
    private Sender sender;
    @Autowired
    private CarInfoRepository carInfoRepository;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MainLoanRepository mainLoanRepository;
    @Autowired
    private CreditMessageRepository creditMessageRepository;
    @Autowired
    private CollectionInterfacesRepository collectionInterfacesRepository;
    @Autowired
    @Qualifier("UserInfoRepositoryMybatis")
    private UserInfoRepository userInfoRepository;
    @Override
    public void backInfo(SplBussinessBasicObject splBussinessBasicObject)  {
        try {
            DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(
                    splBussinessBasicObject.getApplyNum(),
                    6
            );

            MainLoanObject mainLoanObject = mainLoanRepository.getByMLoanIdAndLoanType(splBussinessBasicObject.getLoanId(), "2");
            CarInfoDto carInfoDto = carInfoRepository.getCarInfoByLoanId(mainLoanObject.getM_loanID());
            UserInfoObject user = userInfoRepository.getUserByUserType(splBussinessBasicObject.getLoanId(), "1");
            CreditMessageObject creditMessage = creditMessageRepository.getCreditMessageByLoanIdAndUserId(splBussinessBasicObject.getLoanId(),user.getUserId());
            CollectionInterfaces collectionInterfaces = new CollectionInterfaces();
            collectionInterfaces.setCreditId(creditMessage.getCreditOrderId());
            collectionInterfaces.setType(14);
            collectionInterfaces.setResult(1);
            CollectionInterfaces carPrice = collectionInterfacesRepository.getObject(collectionInterfaces);
            BackAntInfoDTO backAntInfoDTO = new BackAntInfoDTOAssembler()
                    .to(carInfoDto, directCustomerStatusDto, carPrice);
            sender.backInfoPush(backAntInfoDTO);
        } catch (Exception e) {
            redisService.set("back:ant:info" + splBussinessBasicObject.getApplyNum(),splBussinessBasicObject.getApplyNum());
            log.error("蚂蚁信息回传失败，申请编号={},原因=[{}]",splBussinessBasicObject.getApplyNum(),e.getMessage(), e);
        }
    }
}

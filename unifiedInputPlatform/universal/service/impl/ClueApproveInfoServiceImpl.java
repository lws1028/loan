package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zlhj.commonLoan.domain.creditBusiness.CreditOrderId;
import com.zlhj.jd.vo.ClueApproveInfoDto;
import com.zlhj.mapper.ClueApproveInfoMapper;
import com.zlhj.unifiedInputPlatform.universal.service.ClueApproveInfoService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class ClueApproveInfoServiceImpl extends ServiceImpl<ClueApproveInfoMapper, ClueApproveInfoDto> implements ClueApproveInfoService {


    @Override
    public void saveClueApproveInfo(ClueApproveInfoDto clueApproveInfoDto) {
        int insert = this.baseMapper.insertClueApproveInfo(clueApproveInfoDto);
//        if (insert == 0) {
//            clueApproveInfoDto.setUpdateTime(new Timestamp(System.currentTimeMillis()));
//            UpdateWrapper<ClueApproveInfoDto> updateWrapper = new UpdateWrapper<ClueApproveInfoDto>().eq("bo_id", clueApproveInfoDto.getBoId());
//            this.baseMapper.update(clueApproveInfoDto, updateWrapper);
//        }
    }

    @Override
    public ClueApproveInfoDto getClueApproveInfo(String boId) {
        return this.baseMapper.selectOne(new QueryWrapper<ClueApproveInfoDto>().eq("bo_id", boId));
    }

    @Override
    public void updateIncomeProofFinished(String boId,String incomeProofMessage) {
        UpdateWrapper<ClueApproveInfoDto> updateWrapper = new UpdateWrapper<ClueApproveInfoDto>().eq("bo_id", boId);
        ClueApproveInfoDto clueApproveInfoDto = new ClueApproveInfoDto();
        clueApproveInfoDto.setIncomeProofFinished("1");
        clueApproveInfoDto.setIncomeProofMessage(incomeProofMessage);
        clueApproveInfoDto.setIncomeProofFinishedTime(new Timestamp(System.currentTimeMillis()));
        clueApproveInfoDto.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        this.baseMapper.update(clueApproveInfoDto, updateWrapper);
    }

    @Override
    public void updateIncomeProofFinished(ClueApproveInfoDto clueApproveInfoDto) {
        UpdateWrapper<ClueApproveInfoDto> updateWrapper = new UpdateWrapper<ClueApproveInfoDto>().eq("bo_id", clueApproveInfoDto.getBoId());
        clueApproveInfoDto.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        this.baseMapper.update(clueApproveInfoDto, updateWrapper);
    }

    @Override
    public ClueApproveInfoDto getClueApproveInfoByCreditOrderId(CreditOrderId creditOrderId) {
        return this.baseMapper.getClueApproveInfoByCreditOrderId(creditOrderId.get());
    }

}

package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizCheckin;
import com.ruoyi.biz.mapper.BizCheckinMapper;
import com.ruoyi.biz.service.IBizCheckinService;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;

@Service
public class BizCheckinServiceImpl implements IBizCheckinService
{
    @Autowired
    private BizCheckinMapper checkinMapper;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizConfigService configService;

    @Override
    public List<BizCheckin> selectCheckinList(BizCheckin checkin)
    {
        return checkinMapper.selectCheckinList(checkin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizCheckin checkin(Long memberId)
    {
        Date today = DateUtils.parseDate(DateUtils.getDate());
        if (checkinMapper.selectByMemberAndDate(memberId, today) != null)
        {
            throw new ServiceException("今日已签到");
        }
        BigDecimal amount = configService.getCheckinAmount();
        BizCheckin checkin = new BizCheckin();
        checkin.setMemberId(memberId);
        checkin.setCheckinDate(today);
        checkin.setAmount(amount);
        checkin.setCurrency(BizConstants.CURRENCY_CNY);
        checkinMapper.insertCheckin(checkin);
        walletService.credit(memberId, BizConstants.CURRENCY_CNY, amount, BizConstants.BIZ_CHECKIN,
                checkin.getCheckinId(), "每日签到");
        return checkin;
    }
}

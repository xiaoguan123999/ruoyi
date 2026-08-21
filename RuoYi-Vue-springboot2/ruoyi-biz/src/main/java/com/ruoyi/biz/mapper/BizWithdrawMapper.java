package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizWithdraw;

public interface BizWithdrawMapper
{
    BizWithdraw selectWithdrawById(Long withdrawId);

    List<BizWithdraw> selectWithdrawList(BizWithdraw withdraw);

    int insertWithdraw(BizWithdraw withdraw);

    int updateWithdraw(BizWithdraw withdraw);

    int updateWithdrawIfPending(BizWithdraw withdraw);
}

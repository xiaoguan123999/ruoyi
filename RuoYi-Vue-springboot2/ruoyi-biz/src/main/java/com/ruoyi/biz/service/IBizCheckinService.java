package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizCheckin;

public interface IBizCheckinService
{
    List<BizCheckin> selectCheckinList(BizCheckin checkin);

    BizCheckin checkin(Long memberId);
}

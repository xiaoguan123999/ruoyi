package com.ruoyi.biz.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizCheckin;

public interface BizCheckinMapper
{
    BizCheckin selectByMemberAndDate(@Param("memberId") Long memberId, @Param("checkinDate") Date checkinDate);

    List<BizCheckin> selectCheckinList(BizCheckin checkin);

    int insertCheckin(BizCheckin checkin);
}

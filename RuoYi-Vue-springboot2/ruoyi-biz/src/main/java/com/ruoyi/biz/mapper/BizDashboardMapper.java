package com.ruoyi.biz.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizDashboardActivity;
import com.ruoyi.biz.domain.BizDashboardRow;
import com.ruoyi.biz.domain.BizDashboardTrendPoint;

public interface BizDashboardMapper
{
    BizDashboardRow selectStats(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd,
            @Param("dayText") String dayText);

    List<BizDashboardTrendPoint> selectTrendRegister(@Param("begin") Date begin, @Param("end") Date end);

    List<BizDashboardTrendPoint> selectTrendOrder(@Param("begin") Date begin, @Param("end") Date end);

    List<BizDashboardTrendPoint> selectTrendOrderUsers(@Param("begin") Date begin, @Param("end") Date end);

    List<BizDashboardTrendPoint> selectTrendRechargeCny(@Param("begin") Date begin, @Param("end") Date end);

    List<BizDashboardTrendPoint> selectTrendWithdrawCny(@Param("begin") Date begin, @Param("end") Date end);

    List<BizDashboardActivity> selectRecent(@Param("limit") int limit);
}

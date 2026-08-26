package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.domain.BizDashboardActivity;
import com.ruoyi.biz.domain.BizDashboardCount;
import com.ruoyi.biz.domain.BizDashboardMoney;
import com.ruoyi.biz.domain.BizDashboardRow;
import com.ruoyi.biz.domain.BizDashboardStats;
import com.ruoyi.biz.domain.BizDashboardTrend;
import com.ruoyi.biz.domain.BizDashboardTrendPoint;
import com.ruoyi.biz.mapper.BizDashboardMapper;
import com.ruoyi.biz.service.IBizDashboardService;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizDashboardServiceImpl implements IBizDashboardService
{
    private static final TimeZone TZ = TimeZone.getTimeZone("Asia/Shanghai");

    @Autowired
    private BizDashboardMapper dashboardMapper;

    @Override
    public BizDashboardStats selectStats(String dateText)
    {
        DateRange range = resolveDay(dateText);
        BizDashboardRow row = dashboardMapper.selectStats(range.start, range.end, range.text);
        if (row == null)
        {
            row = new BizDashboardRow();
        }
        BizDashboardStats stats = new BizDashboardStats();
        stats.setDate(range.text);
        stats.setRegister(new BizDashboardCount(row.getRegisterToday(), row.getRegisterTotal()));
        stats.setKyc(new BizDashboardCount(row.getKycToday(), row.getKycTotal()));
        stats.setCheckin(new BizDashboardCount(row.getCheckinToday(), row.getCheckinTotal()));
        stats.setCheckinReward(money(row.getCheckinRewardTodayCny(), row.getCheckinRewardTodayUsdt(),
                row.getCheckinRewardTotalCny(), row.getCheckinRewardTotalUsdt(),
                row.getCheckinToday(), row.getCheckinTotal()));
        stats.setRecharge(money(row.getRechargeTodayCny(), row.getRechargeTodayUsdt(),
                row.getRechargeTotalCny(), row.getRechargeTotalUsdt(),
                row.getRechargeTodayCount(), row.getRechargeTotalCount()));
        stats.setRechargeUsers(new BizDashboardCount(row.getRechargeTodayUsers(), row.getRechargeTotalUsers()));
        stats.setRechargeOrders(new BizDashboardCount(row.getRechargeTodayCount(), row.getRechargeTotalCount()));
        stats.setSubscribeUsers(new BizDashboardCount(row.getSubscribeTodayUsers(), row.getSubscribeTotalUsers()));
        stats.setSubscribeNewUsers(new BizDashboardCount(row.getSubscribeNewToday(), row.getSubscribeNewTotal()));
        stats.setPullCount(new BizDashboardCount(row.getPullTodayCount(), row.getPullTotalCount()));
        stats.setPullAmount(money(row.getPullTodayCny(), row.getPullTodayUsdt(),
                row.getPullTotalCny(), row.getPullTotalUsdt(),
                row.getPullTodayCount(), row.getPullTotalCount()));
        stats.setWithdrawProduct(money(row.getWdProductTodayCny(), row.getWdProductTodayUsdt(),
                row.getWdProductTotalCny(), row.getWdProductTotalUsdt(),
                row.getWdProductTodayCount(), row.getWdProductTotalCount()));
        stats.setWithdrawPromo(money(row.getWdPromoTodayCny(), row.getWdPromoTodayUsdt(),
                row.getWdPromoTotalCny(), row.getWdPromoTotalUsdt(),
                row.getWdPromoTodayCount(), row.getWdPromoTotalCount()));
        stats.setWithdrawAssist(money(row.getWdAssistTodayCny(), row.getWdAssistTodayUsdt(),
                row.getWdAssistTotalCny(), row.getWdAssistTotalUsdt(),
                row.getWdAssistTodayCount(), row.getWdAssistTotalCount()));
        stats.setWithdrawTotal(money(row.getWdTotalTodayCny(), row.getWdTotalTodayUsdt(),
                row.getWdTotalTotalCny(), row.getWdTotalTotalUsdt(),
                row.getWdTotalTodayCount(), row.getWdTotalTotalCount()));
        stats.setWithdrawCount(new BizDashboardCount(row.getWdTotalTodayCount(), row.getWdTotalTotalCount()));
        stats.setWithdrawApply(money(row.getWdApplyTodayCny(), row.getWdApplyTodayUsdt(),
                BigDecimal.ZERO, BigDecimal.ZERO, row.getWdApplyTodayCount(), 0L));
        stats.setRebate(money(row.getRebateTodayCny(), row.getRebateTodayUsdt(),
                row.getRebateTotalCny(), row.getRebateTotalUsdt(),
                row.getRebateTodayCount(), row.getRebateTotalCount()));
        stats.setCommission(money(row.getCommissionTodayCny(), row.getCommissionTodayUsdt(),
                row.getCommissionTotalCny(), row.getCommissionTotalUsdt(),
                row.getCommissionTodayCount(), row.getCommissionTotalCount()));
        stats.setInvite(money(row.getInviteTodayCny(), row.getInviteTodayUsdt(),
                row.getInviteTotalCny(), row.getInviteTotalUsdt(), 0L, 0L));
        stats.setHoldingOrders(new BizDashboardCount(row.getHoldingOrders(), row.getHoldingOrders()));
        stats.setHoldingUsers(new BizDashboardCount(row.getHoldingUsers(), row.getHoldingUsers()));
        BizDashboardMoney available = new BizDashboardMoney();
        available.setTotalCny(BizDashboardCount.nvl(row.getWalletCnyAvailable()));
        available.setTotalUsdt(BizDashboardCount.nvl(row.getWalletUsdtAvailable()));
        stats.setWalletAvailable(available);
        BizDashboardMoney frozen = new BizDashboardMoney();
        frozen.setTotalCny(BizDashboardCount.nvl(row.getWalletCnyFrozen()));
        frozen.setTotalUsdt(BizDashboardCount.nvl(row.getWalletUsdtFrozen()));
        stats.setWalletFrozen(frozen);
        stats.setPendingRecharge(BizDashboardCount.nvl(row.getPendingRecharge()));
        stats.setPendingWithdraw(BizDashboardCount.nvl(row.getPendingWithdraw()));
        stats.setPendingLevelReward(BizDashboardCount.nvl(row.getPendingLevelReward()));
        stats.setPendingKyc(BizDashboardCount.nvl(row.getPendingKyc()));
        BizDashboardMoney pendingWithdrawAmount = new BizDashboardMoney();
        pendingWithdrawAmount.setTotalCny(BizDashboardCount.nvl(row.getPendingWithdrawCny()));
        pendingWithdrawAmount.setTotalUsdt(BizDashboardCount.nvl(row.getPendingWithdrawUsdt()));
        pendingWithdrawAmount.setTotalCount(BizDashboardCount.nvl(row.getPendingWithdraw()));
        stats.setPendingWithdrawAmount(pendingWithdrawAmount);
        List<BizDashboardActivity> recent = dashboardMapper.selectRecent(8);
        stats.setRecent(recent == null ? new ArrayList<BizDashboardActivity>() : recent);
        return stats;
    }

    @Override
    public BizDashboardTrend selectTrend(String dateText)
    {
        DateRange range = resolveDay(dateText);
        Calendar cal = Calendar.getInstance(TZ);
        cal.setTime(range.start);
        cal.add(Calendar.DAY_OF_MONTH, -6);
        Date begin = cal.getTime();
        Map<String, Long> register = toCountMap(dashboardMapper.selectTrendRegister(begin, range.end));
        Map<String, Long> orderCount = toCountMap(dashboardMapper.selectTrendOrder(begin, range.end));
        Map<String, Long> orderUsers = toCountMap(dashboardMapper.selectTrendOrderUsers(begin, range.end));
        Map<String, BigDecimal> rechargeCny = toAmountMap(dashboardMapper.selectTrendRechargeCny(begin, range.end));
        Map<String, BigDecimal> withdrawCny = toAmountMap(dashboardMapper.selectTrendWithdrawCny(begin, range.end));
        BizDashboardTrend trend = new BizDashboardTrend();
        Calendar cursor = Calendar.getInstance(TZ);
        cursor.setTime(begin);
        SimpleDateFormat keyFmt = dayFormat();
        SimpleDateFormat labelFmt = new SimpleDateFormat("MM-dd");
        labelFmt.setTimeZone(TZ);
        for (int i = 0; i < 7; i++)
        {
            String key = keyFmt.format(cursor.getTime());
            trend.getDates().add(labelFmt.format(cursor.getTime()));
            trend.getRegister().add(nvlLong(register.get(key)));
            trend.getOrderCount().add(nvlLong(orderCount.get(key)));
            trend.getOrderUsers().add(nvlLong(orderUsers.get(key)));
            trend.getRechargeCny().add(nvlDec(rechargeCny.get(key)));
            trend.getWithdrawCny().add(nvlDec(withdrawCny.get(key)));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        return trend;
    }

    private static BizDashboardMoney money(BigDecimal todayCny, BigDecimal todayUsdt, BigDecimal totalCny,
            BigDecimal totalUsdt, Long todayCount, Long totalCount)
    {
        return new BizDashboardMoney(todayCny, todayUsdt, totalCny, totalUsdt, todayCount, totalCount);
    }

    private static Map<String, Long> toCountMap(List<BizDashboardTrendPoint> points)
    {
        Map<String, Long> map = new HashMap<String, Long>();
        if (points == null)
        {
            return map;
        }
        for (BizDashboardTrendPoint point : points)
        {
            if (point == null || StringUtils.isEmpty(point.getDayKey()))
            {
                continue;
            }
            map.put(point.getDayKey(), nvlLong(point.getCnt()));
        }
        return map;
    }

    private static Map<String, BigDecimal> toAmountMap(List<BizDashboardTrendPoint> points)
    {
        Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
        if (points == null)
        {
            return map;
        }
        for (BizDashboardTrendPoint point : points)
        {
            if (point == null || StringUtils.isEmpty(point.getDayKey()))
            {
                continue;
            }
            map.put(point.getDayKey(), nvlDec(point.getAmount()));
        }
        return map;
    }

    private static long nvlLong(Long v)
    {
        return v == null ? 0L : v.longValue();
    }

    private static BigDecimal nvlDec(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static DateRange resolveDay(String dateText)
    {
        SimpleDateFormat fmt = dayFormat();
        Date start;
        try
        {
            if (StringUtils.isEmpty(dateText))
            {
                start = fmt.parse(fmt.format(new Date()));
            }
            else
            {
                start = fmt.parse(dateText.trim());
            }
        }
        catch (ParseException e)
        {
            try
            {
                start = fmt.parse(fmt.format(new Date()));
            }
            catch (ParseException ignored)
            {
                start = new Date();
            }
        }
        Calendar cal = Calendar.getInstance(TZ);
        cal.setTime(start);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date dayStart = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date dayEnd = cal.getTime();
        return new DateRange(dayStart, dayEnd, fmt.format(dayStart));
    }

    private static SimpleDateFormat dayFormat()
    {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        fmt.setTimeZone(TZ);
        fmt.setLenient(false);
        return fmt;
    }

    private static class DateRange
    {
        private final Date start;
        private final Date end;
        private final String text;

        private DateRange(Date start, Date end, String text)
        {
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }
}

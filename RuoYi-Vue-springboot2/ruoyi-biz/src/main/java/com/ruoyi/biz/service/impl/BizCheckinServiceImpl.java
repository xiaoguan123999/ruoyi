package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizCheckin;
import com.ruoyi.biz.domain.BizCheckinPrize;
import com.ruoyi.biz.domain.CheckinPrizeRule;
import com.ruoyi.biz.domain.CheckinResult;
import com.ruoyi.biz.domain.CheckinRule;
import com.ruoyi.biz.mapper.BizCheckinMapper;
import com.ruoyi.biz.mapper.BizCheckinPrizeMapper;
import com.ruoyi.biz.service.IBizCheckinService;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;

@Service
public class BizCheckinServiceImpl implements IBizCheckinService
{
    @Autowired
    private BizCheckinMapper checkinMapper;

    @Autowired
    private BizCheckinPrizeMapper prizeMapper;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizConfigService configService;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Override
    public List<BizCheckin> selectCheckinList(BizCheckin checkin)
    {
        return checkinMapper.selectCheckinList(checkin);
    }

    @Override
    public List<BizCheckinPrize> selectPrizeList(BizCheckinPrize prize)
    {
        return prizeMapper.selectPrizeList(prize);
    }

    @Override
    public CheckinRule getCheckinRule()
    {
        CheckinRule rule = new CheckinRule();
        rule.setAmount(configService.getCheckinAmount());
        rule.setOncePerDay(true);
        List<CheckinPrizeRule> prizes = new ArrayList<CheckinPrizeRule>();
        prizes.add(buildPrize(BizConstants.CONFIG_CHECKIN_PRIZE1_DAYS, "180",
                BizConstants.CONFIG_CHECKIN_PRIZE1_NAME, "华为手机",
                BizConstants.CONFIG_CHECKIN_PRIZE1_RATE, "1",
                BizConstants.CONFIG_CHECKIN_PRIZE1_ENABLED));
        prizes.add(buildPrize(BizConstants.CONFIG_CHECKIN_PRIZE2_DAYS, "365",
                BizConstants.CONFIG_CHECKIN_PRIZE2_NAME, "华硕ROG笔记本电脑",
                BizConstants.CONFIG_CHECKIN_PRIZE2_RATE, "0.5",
                BizConstants.CONFIG_CHECKIN_PRIZE2_ENABLED));
        rule.setPrizes(prizes);
        return rule;
    }

    @Override
    public void saveCheckinRule(CheckinRule rule)
    {
        if (rule == null || rule.getAmount() == null || rule.getAmount().compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("每日签到金额不能为空");
        }
        if (rule.getPrizes() == null || rule.getPrizes().size() < 2)
        {
            throw new ServiceException("请配置两档连续签到奖品");
        }
        CheckinPrizeRule p1 = rule.getPrizes().get(0);
        CheckinPrizeRule p2 = rule.getPrizes().get(1);
        validatePrize(p1, "第一档");
        validatePrize(p2, "第二档");
        saveConfig(BizConstants.CONFIG_CHECKIN_AMOUNT, "签到奖励金额", rule.getAmount().stripTrailingZeros().toPlainString(), "每日签到奖励人民币金额");
        savePrizeConfig(1, p1);
        savePrizeConfig(2, p2);
    }

    @Override
    public CheckinResult getCheckinInfo(Long memberId)
    {
        java.util.Date today = DateUtils.parseDate(DateUtils.getDate());
        CheckinResult result = new CheckinResult();
        result.setAmount(configService.getCheckinAmount());
        result.setCurrency(BizConstants.CURRENCY_CNY);
        result.setCheckedToday(checkinMapper.selectByMemberAndDate(memberId, today) != null);
        result.setStreakDays(calcStreak(memberId, today));
        result.setRule(getCheckinRule());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinResult checkin(Long memberId)
    {
        java.util.Date today = DateUtils.parseDate(DateUtils.getDate());
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

        int streak = calcStreak(memberId, today);
        CheckinResult result = new CheckinResult();
        result.setCheckinId(checkin.getCheckinId());
        result.setCheckinDate(today);
        result.setAmount(amount);
        result.setCurrency(BizConstants.CURRENCY_CNY);
        result.setStreakDays(streak);
        result.setCheckedToday(true);
        result.setPrizeDrawn(false);
        result.setPrizeWon(false);
        result.setRule(getCheckinRule());

        CheckinRule rule = result.getRule();
        if (rule.getPrizes() != null)
        {
            for (CheckinPrizeRule prize : rule.getPrizes())
            {
                if (prize == null || prize.getEnabled() == null || !prize.getEnabled())
                {
                    continue;
                }
                if (prize.getDays() == null || prize.getDays().intValue() != streak)
                {
                    continue;
                }
                boolean won = hit(prize.getRate());
                BizCheckinPrize log = new BizCheckinPrize();
                log.setMemberId(memberId);
                log.setCheckinId(checkin.getCheckinId());
                log.setStreakDays(streak);
                log.setPrizeName(prize.getName());
                log.setWon(won ? "1" : "0");
                prizeMapper.insertPrize(log);
                result.setPrizeDrawn(true);
                result.setPrizeDays(prize.getDays());
                result.setPrizeName(prize.getName());
                result.setPrizeWon(won);
            }
        }
        return result;
    }

    private CheckinPrizeRule buildPrize(String daysKey, String daysDef, String nameKey, String nameDef,
            String rateKey, String rateDef, String enabledKey)
    {
        CheckinPrizeRule prize = new CheckinPrizeRule();
        prize.setDays(intVal(daysKey, Integer.parseInt(daysDef)));
        prize.setName(strVal(nameKey, nameDef));
        prize.setRate(decimalVal(rateKey, rateDef));
        prize.setEnabled(boolVal(enabledKey, true));
        return prize;
    }

    private void validatePrize(CheckinPrizeRule prize, String label)
    {
        if (prize.getDays() == null || prize.getDays().intValue() <= 0)
        {
            throw new ServiceException(label + "连续天数必须大于0");
        }
        if (StringUtils.isEmpty(prize.getName()))
        {
            throw new ServiceException(label + "奖品名称不能为空");
        }
        if (prize.getRate() == null || prize.getRate().compareTo(BigDecimal.ZERO) < 0
                || prize.getRate().compareTo(new BigDecimal("100")) > 0)
        {
            throw new ServiceException(label + "中奖概率需在 0 到 100 之间");
        }
        if (prize.getEnabled() == null)
        {
            prize.setEnabled(Boolean.TRUE);
        }
    }

    private void savePrizeConfig(int index, CheckinPrizeRule prize)
    {
        String prefix = index == 1 ? "第一档" : "第二档";
        String daysKey = index == 1 ? BizConstants.CONFIG_CHECKIN_PRIZE1_DAYS : BizConstants.CONFIG_CHECKIN_PRIZE2_DAYS;
        String nameKey = index == 1 ? BizConstants.CONFIG_CHECKIN_PRIZE1_NAME : BizConstants.CONFIG_CHECKIN_PRIZE2_NAME;
        String rateKey = index == 1 ? BizConstants.CONFIG_CHECKIN_PRIZE1_RATE : BizConstants.CONFIG_CHECKIN_PRIZE2_RATE;
        String enabledKey = index == 1 ? BizConstants.CONFIG_CHECKIN_PRIZE1_ENABLED : BizConstants.CONFIG_CHECKIN_PRIZE2_ENABLED;
        saveConfig(daysKey, "签到" + prefix + "连续天数", String.valueOf(prize.getDays()), "连续签到满该天数触发抽奖");
        saveConfig(nameKey, "签到" + prefix + "奖品", prize.getName(), "连续签到奖品名称");
        saveConfig(rateKey, "签到" + prefix + "中奖概率", prize.getRate().stripTrailingZeros().toPlainString(), "百分数，100表示必中");
        saveConfig(enabledKey, "签到" + prefix + "开关", prize.getEnabled() ? "true" : "false", "false表示关闭该档抽奖");
    }

    private void saveConfig(String key, String name, String value, String remark)
    {
        SysConfig existing = sysConfigMapper.checkConfigKeyUnique(key);
        if (existing == null)
        {
            SysConfig config = new SysConfig();
            config.setConfigName(name);
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType("N");
            config.setRemark(remark);
            sysConfigService.insertConfig(config);
        }
        else
        {
            existing.setConfigName(name);
            existing.setConfigValue(value);
            existing.setRemark(remark);
            sysConfigService.updateConfig(existing);
        }
    }

    private int calcStreak(Long memberId, java.util.Date today)
    {
        List<java.util.Date> dates = checkinMapper.selectDatesByMemberId(memberId);
        if (dates == null || dates.isEmpty())
        {
            return 0;
        }
        Set<LocalDate> set = new HashSet<LocalDate>();
        for (java.util.Date date : dates)
        {
            if (date != null)
            {
                set.add(new Date(date.getTime()).toLocalDate());
            }
        }
        LocalDate cursor = new Date(today.getTime()).toLocalDate();
        int streak = 0;
        while (set.contains(cursor))
        {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private boolean hit(BigDecimal ratePercent)
    {
        if (ratePercent == null || ratePercent.compareTo(BigDecimal.ZERO) <= 0)
        {
            return false;
        }
        if (ratePercent.compareTo(new BigDecimal("100")) >= 0)
        {
            return true;
        }
        double roll = ThreadLocalRandom.current().nextDouble(0D, 100D);
        return roll < ratePercent.doubleValue();
    }

    private int intVal(String key, int defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value))
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(value.trim());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    private BigDecimal decimalVal(String key, String defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value))
        {
            value = defaultValue;
        }
        try
        {
            return new BigDecimal(value.trim());
        }
        catch (NumberFormatException e)
        {
            return new BigDecimal(defaultValue);
        }
    }

    private String strVal(String key, String defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    private boolean boolVal(String key, boolean defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value))
        {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }
}

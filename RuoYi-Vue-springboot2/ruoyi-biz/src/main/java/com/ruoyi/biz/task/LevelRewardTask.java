package com.ruoyi.biz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.biz.service.IBizMemberService;

/**
 * 星链伙伴成长激励金每日核算
 */
@Component("levelRewardTask")
public class LevelRewardTask
{
    private static final Logger log = LoggerFactory.getLogger(LevelRewardTask.class);

    @Autowired
    private IBizMemberService memberService;

    public void execute()
    {
        int count = memberService.refreshAllLevels();
        log.info("成长激励金核算完成，处理{}名会员", count);
    }
}

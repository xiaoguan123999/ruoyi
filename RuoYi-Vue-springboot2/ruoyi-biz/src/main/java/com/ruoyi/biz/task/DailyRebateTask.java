package com.ruoyi.biz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.biz.service.IBizOrderService;

/**
 * 产品每日返利
 */
@Component("dailyRebateTask")
public class DailyRebateTask
{
    private static final Logger log = LoggerFactory.getLogger(DailyRebateTask.class);

    @Autowired
    private IBizOrderService orderService;

    public void execute()
    {
        int count = orderService.processDailyRebate();
        log.info("产品每日返利完成，成功处理{}笔", count);
    }
}

package com.ruoyi.biz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.biz.service.IBizOrderService;

/**
 * ASSIST 产品到期退本（本金回 BALANCE，不可提现）
 */
@Component("assistPrincipalReturnTask")
public class AssistPrincipalReturnTask
{
    private static final Logger log = LoggerFactory.getLogger(AssistPrincipalReturnTask.class);

    @Autowired
    private IBizOrderService orderService;

    public void execute()
    {
        int count = orderService.processAssistPrincipalReturn();
        log.info("助力产品退本完成，成功处理{}笔", count);
    }
}

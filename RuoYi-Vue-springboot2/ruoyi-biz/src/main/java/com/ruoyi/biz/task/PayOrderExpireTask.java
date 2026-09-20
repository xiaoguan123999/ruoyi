package com.ruoyi.biz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.biz.service.IBizOnlinePayService;

/**
 * 关闭已过期的待付支付单（拉起收银台未付）
 */
@Component("payOrderExpireTask")
public class PayOrderExpireTask
{
    private static final Logger log = LoggerFactory.getLogger(PayOrderExpireTask.class);

    @Autowired
    private IBizOnlinePayService onlinePayService;

    public void execute()
    {
        int count = onlinePayService.closeExpiredOrders();
        if (count > 0)
        {
            log.info("支付超时关单完成，关闭{}笔", count);
        }
    }
}

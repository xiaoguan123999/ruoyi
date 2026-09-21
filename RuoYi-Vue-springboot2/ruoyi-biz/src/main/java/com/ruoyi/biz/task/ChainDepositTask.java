package com.ruoyi.biz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.biz.service.IBizChainDepositService;

/**
 * Poll TronGrid / BscScan and credit USDT by amount fingerprint.
 */
@Component("chainDepositTask")
public class ChainDepositTask
{
    private static final Logger log = LoggerFactory.getLogger(ChainDepositTask.class);

    @Autowired
    private IBizChainDepositService chainDepositService;

    public void scan()
    {
        chainDepositService.scan();
        log.debug("chain deposit scan done");
    }
}

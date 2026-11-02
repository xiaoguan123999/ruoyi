package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizCommissionLog;
import com.ruoyi.biz.domain.BizOrder;
import com.ruoyi.biz.domain.BizRecharge;

public interface IBizCommissionService
{
    List<BizCommissionLog> selectCommissionList(BizCommissionLog log);

    void grantForSubscribe(BizOrder order);

    /** 按认购单追回已发三级返佣，余额不够扣多少算多少 */
    void clawbackForSubscribe(Long orderId);

    void grantForRecharge(BizRecharge recharge);
}

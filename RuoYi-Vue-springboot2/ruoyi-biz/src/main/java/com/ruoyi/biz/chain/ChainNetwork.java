package com.ruoyi.biz.chain;

import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

public final class ChainNetwork
{
    private ChainNetwork()
    {
    }

    public static String normalize(String raw)
    {
        String n = raw == null ? "" : raw.trim().toUpperCase();
        if (StringUtils.isEmpty(n) || "TRON".equals(n) || BizConstants.CHAIN_NETWORK_TRC20.equals(n))
        {
            return BizConstants.CHAIN_NETWORK_TRC20;
        }
        if (BizConstants.CHAIN_NETWORK_BEP20.equals(n) || "BSC".equals(n) || "BNB".equals(n) || "BSC20".equals(n))
        {
            return BizConstants.CHAIN_NETWORK_BEP20;
        }
        throw new ServiceException("不支持的网络，请传 TRC20 或 BEP20");
    }

    public static String normalizeOrDefault(String raw)
    {
        try
        {
            return normalize(raw);
        }
        catch (ServiceException e)
        {
            return BizConstants.CHAIN_NETWORK_TRC20;
        }
    }

    public static boolean isBep20(String network)
    {
        return BizConstants.CHAIN_NETWORK_BEP20.equals(normalizeOrDefault(network));
    }
}

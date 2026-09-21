package com.ruoyi.biz.chain;

import java.util.regex.Pattern;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

public final class TronAddress
{
    private static final Pattern BASE58 = Pattern.compile("^T[1-9A-HJ-NP-Za-km-z]{33}$");

    private TronAddress()
    {
    }

    public static String normalize(String raw)
    {
        return raw == null ? "" : raw.trim();
    }

    public static String requireValidOrEmpty(String raw)
    {
        String address = normalize(raw);
        if (StringUtils.isEmpty(address))
        {
            return "";
        }
        if (!BASE58.matcher(address).matches())
        {
            throw new ServiceException("USDT-TRC20收款地址格式不正确");
        }
        return address;
    }
}

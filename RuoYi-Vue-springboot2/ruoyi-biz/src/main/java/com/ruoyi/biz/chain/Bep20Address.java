package com.ruoyi.biz.chain;

import java.util.regex.Pattern;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

public final class Bep20Address
{
    private static final Pattern HEX = Pattern.compile("^0x[0-9a-fA-F]{40}$");

    private Bep20Address()
    {
    }

    public static String normalize(String raw)
    {
        if (raw == null)
        {
            return "";
        }
        String address = raw.trim();
        return StringUtils.isEmpty(address) ? "" : address.toLowerCase();
    }

    public static String requireValidOrEmpty(String raw)
    {
        String address = raw == null ? "" : raw.trim();
        if (StringUtils.isEmpty(address))
        {
            return "";
        }
        if (!HEX.matcher(address).matches())
        {
            throw new ServiceException("USDT-BEP20收款地址格式不正确");
        }
        return address.toLowerCase();
    }
}

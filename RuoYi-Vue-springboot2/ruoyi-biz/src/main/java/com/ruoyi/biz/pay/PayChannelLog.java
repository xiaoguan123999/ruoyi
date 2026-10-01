package com.ruoyi.biz.pay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.utils.StringUtils;

/**
 * Pay channel in/out logs. Dedicated logger so prod can enable only this package at INFO.
 */
public final class PayChannelLog
{
    private static final Logger log = LoggerFactory.getLogger("com.ruoyi.biz.pay.channel");
    private static final int MAX = 2000;

    private PayChannelLog()
    {
    }

    public static void io(String action, String url, String req, String resp, Integer httpStatus, long costMs)
    {
        log.info("pay {} url={} http={} costMs={} req={} resp={}",
                nvl(action), nvl(url), httpStatus, Long.valueOf(costMs), cut(mask(req)), cut(mask(resp)));
    }

    public static void in(String action, String detail)
    {
        log.info("pay {} in {}", nvl(action), cut(mask(detail)));
    }

    public static void out(String action, String detail)
    {
        log.info("pay {} out {}", nvl(action), cut(mask(detail)));
    }

    private static String mask(String raw)
    {
        if (StringUtils.isEmpty(raw))
        {
            return "";
        }
        return raw.replaceAll("(?i)((?:secret|secret_key|secretKey|appSecret|privateKey|key)(?:\"\\s*:\\s*\"|=))([^&\"\\s]+)",
                "$1***");
    }

    private static String cut(String raw)
    {
        if (raw == null)
        {
            return "";
        }
        return raw.length() <= MAX ? raw : raw.substring(0, MAX);
    }

    private static String nvl(String raw)
    {
        return raw == null ? "" : raw;
    }
}

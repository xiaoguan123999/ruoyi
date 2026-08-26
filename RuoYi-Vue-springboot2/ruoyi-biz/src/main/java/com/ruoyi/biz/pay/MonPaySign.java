package com.ruoyi.biz.pay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MonPay 系签名：百付 / 宝利 / 牛付 / 沙付共用。
 * 非空参数按 key 字典序拼接 key=value&amp;...&amp;key=secret，再 MD5 小写。
 */
public final class MonPaySign
{
    private MonPaySign() {}

    public static String sign(Map<String, String> params, String secret)
    {
        List<String> keys = new ArrayList<String>();
        if (params != null)
        {
            for (Map.Entry<String, String> e : params.entrySet())
            {
                if (e.getKey() == null || "sign".equals(e.getKey()))
                {
                    continue;
                }
                if (e.getValue() == null || e.getValue().length() == 0)
                {
                    continue;
                }
                keys.add(e.getKey());
            }
        }
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++)
        {
            if (i > 0)
            {
                sb.append('&');
            }
            sb.append(keys.get(i)).append('=').append(params.get(keys.get(i)));
        }
        sb.append("&key=").append(secret == null ? "" : secret);
        return md5Hex(sb.toString());
    }

    public static boolean verify(Map<String, String> params, String secret, String provided)
    {
        if (provided == null || provided.length() == 0)
        {
            return false;
        }
        return sign(params, secret).equalsIgnoreCase(provided.trim());
    }

    public static String md5Hex(String raw)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes)
            {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("MD5 unavailable", e);
        }
    }
}

package com.ruoyi.biz.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * App 实名：姓名、身份证号格式校验。
 */
public final class KycUtils
{
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5·]{2,20}$");

    private static final int[] WEIGHTS = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

    private static final char[] CHECK_CODES = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };

    private KycUtils()
    {
    }

    public static String normalizeName(String realName)
    {
        return realName == null ? "" : realName.trim();
    }

    public static String normalizeIdCard(String idCard)
    {
        if (idCard == null)
        {
            return "";
        }
        return idCard.trim().replace(" ", "").replace("x", "X").toUpperCase();
    }

    public static boolean isValidName(String realName)
    {
        return NAME_PATTERN.matcher(realName).matches();
    }

    public static boolean isValidIdCard(String idCard)
    {
        if (idCard == null || idCard.length() != 18)
        {
            return false;
        }
        for (int i = 0; i < 17; i++)
        {
            char c = idCard.charAt(i);
            if (c < '0' || c > '9')
            {
                return false;
            }
        }
        char last = idCard.charAt(17);
        if (!((last >= '0' && last <= '9') || last == 'X'))
        {
            return false;
        }
        if (!isValidDate(idCard.substring(6, 14)))
        {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++)
        {
            sum += (idCard.charAt(i) - '0') * WEIGHTS[i];
        }
        return CHECK_CODES[sum % 11] == last;
    }

    private static boolean isValidDate(String ymd)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        format.setLenient(false);
        try
        {
            format.parse(ymd);
            return true;
        }
        catch (ParseException e)
        {
            return false;
        }
    }
}

package com.ruoyi.biz.util;

import java.util.regex.Pattern;

/**
 * Mainland China mobile number: 11 digits, 1 then 3-9.
 */
public final class PhoneUtils
{
    private static final Pattern CN_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

    private PhoneUtils()
    {
    }

    public static String normalize(String phone)
    {
        return phone == null ? "" : phone.trim();
    }

    public static boolean isValidCnMobile(String phone)
    {
        return phone != null && CN_MOBILE.matcher(phone).matches();
    }
}

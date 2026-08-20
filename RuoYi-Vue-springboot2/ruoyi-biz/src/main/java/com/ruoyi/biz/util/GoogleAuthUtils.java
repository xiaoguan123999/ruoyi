package com.ruoyi.biz.util;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.ruoyi.common.utils.StringUtils;

/**
 * Google Authenticator TOTP (RFC 6238), 6 digits / 30 seconds / HMAC-SHA1.
 */
public final class GoogleAuthUtils
{
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();

    private GoogleAuthUtils()
    {
    }

    public static String generateSecret()
    {
        byte[] buf = new byte[20];
        RANDOM.nextBytes(buf);
        return base32Encode(buf);
    }

    public static String otpAuthUrl(String issuer, String account, String secret)
    {
        String safeIssuer = StringUtils.isEmpty(issuer) ? "App" : issuer;
        String label = url(safeIssuer) + ":" + url(account);
        return "otpauth://totp/" + label + "?secret=" + secret
                + "&issuer=" + url(safeIssuer)
                + "&algorithm=SHA1&digits=6&period=30";
    }

    public static boolean verify(String secret, String code)
    {
        if (StringUtils.isEmpty(secret) || StringUtils.isEmpty(code) || !code.matches("\\d{6}"))
        {
            return false;
        }
        int expected = Integer.parseInt(code);
        long timeIndex = System.currentTimeMillis() / 1000L / 30L;
        for (int i = -1; i <= 1; i++)
        {
            if (generateCode(secret, timeIndex + i) == expected)
            {
                return true;
            }
        }
        return false;
    }

    static int generateCode(String secret, long timeIndex)
    {
        try
        {
            byte[] key = base32Decode(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(timeIndex).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            return binary % 1000000;
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    private static String url(String value)
    {
        try
        {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        }
        catch (Exception e)
        {
            return value;
        }
    }

    private static String base32Encode(byte[] data)
    {
        StringBuilder sb = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data)
        {
            buffer = (buffer << 8) | (b & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5)
            {
                bitsLeft -= 5;
                sb.append(BASE32.charAt((buffer >> bitsLeft) & 0x1f));
            }
        }
        if (bitsLeft > 0)
        {
            sb.append(BASE32.charAt((buffer << (5 - bitsLeft)) & 0x1f));
        }
        return sb.toString();
    }

    private static byte[] base32Decode(String secret)
    {
        String src = secret.toUpperCase().replace("=", "").replace(" ", "");
        int buffer = 0;
        int bitsLeft = 0;
        byte[] out = new byte[src.length() * 5 / 8];
        int index = 0;
        for (int i = 0; i < src.length(); i++)
        {
            int val = BASE32.indexOf(src.charAt(i));
            if (val < 0)
            {
                continue;
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8)
            {
                bitsLeft -= 8;
                out[index++] = (byte) ((buffer >> bitsLeft) & 0xff);
            }
        }
        if (index == out.length)
        {
            return out;
        }
        byte[] trimmed = new byte[index];
        System.arraycopy(out, 0, trimmed, 0, index);
        return trimmed;
    }
}

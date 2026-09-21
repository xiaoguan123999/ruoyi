package com.ruoyi.biz.pay;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * Short-timeout HTTP helper for third-party pay gateways.
 */
public final class PayHttp
{
    private static final Logger log = LoggerFactory.getLogger(PayHttp.class);

    private PayHttp()
    {
    }

    public static String postJson(String url, String json)
    {
        return post(url, json, "application/json;charset=UTF-8");
    }

    public static String get(String url, Map<String, String> headers)
    {
        HttpURLConnection conn = null;
        try
        {
            log.info("http GET {}", cut(url, 300));
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setDoInput(true);
            conn.setRequestProperty("Accept", "application/json");
            if (headers != null)
            {
                for (Map.Entry<String, String> e : headers.entrySet())
                {
                    if (e.getKey() != null && e.getValue() != null)
                    {
                        conn.setRequestProperty(e.getKey(), e.getValue());
                    }
                }
            }
            int code = conn.getResponseCode();
            InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String text = read(in);
            log.info("http GET RESP {} {}", Integer.valueOf(code), cut(text, 300));
            if (code >= 400)
            {
                throw new ServiceException("链上查询失败");
            }
            return text == null ? "" : text;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("http GET fail {}", url, e);
            throw new ServiceException("链上查询失败");
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    public static String postForm(String url, String form)
    {
        return post(url, form, "application/x-www-form-urlencoded;charset=UTF-8");
    }

    public static String post(String url, String body, String contentType)
    {
        HttpURLConnection conn = null;
        long start = System.currentTimeMillis();
        Integer httpStatus = null;
        String text = "";
        try
        {
            log.info("pay POST {} body={}", url, cut(body, 500));
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Content-Type", contentType);
            byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
            int code = conn.getResponseCode();
            httpStatus = Integer.valueOf(code);
            InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            text = read(in);
            log.info("pay RESP {} {}", Integer.valueOf(code), cut(text, 500));
            if (StringUtils.isEmpty(text))
            {
                throw new ServiceException("支付网关无响应");
            }
            return text;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("pay POST fail {}", url, e);
            throw new ServiceException("支付网关请求失败");
        }
        finally
        {
            PayHttpExchange.record(url, body, text, httpStatus, System.currentTimeMillis() - start);
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    public static String joinUrl(String base, String path)
    {
        if (StringUtils.isEmpty(base))
        {
            return path;
        }
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        if (StringUtils.isEmpty(path))
        {
            return b;
        }
        return path.startsWith("/") ? b + path : b + "/" + path;
    }

    public static String fen(java.math.BigDecimal yuan)
    {
        if (yuan == null)
        {
            return "0";
        }
        return yuan.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    public static String yuan(java.math.BigDecimal amount)
    {
        if (amount == null)
        {
            return "0.00";
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static String read(InputStream in) throws Exception
    {
        if (in == null)
        {
            return "";
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        int n;
        while ((n = in.read(tmp)) >= 0)
        {
            buf.write(tmp, 0, n);
        }
        in.close();
        return new String(buf.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String cut(String raw, int max)
    {
        if (raw == null)
        {
            return "";
        }
        return raw.length() <= max ? raw : raw.substring(0, max);
    }
}

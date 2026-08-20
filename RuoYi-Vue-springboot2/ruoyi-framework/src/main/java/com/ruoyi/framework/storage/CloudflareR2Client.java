package com.ruoyi.framework.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.properties.CloudflareR2Properties;

/**
 * Cloudflare R2 via AWS SigV4 + Apache HttpClient (JDK 8).
 */
@Component
public class CloudflareR2Client
{
    private static final Logger log = LoggerFactory.getLogger(CloudflareR2Client.class);

    private static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private static final Pattern SAFE_KEY = Pattern.compile("^[A-Za-z0-9._\\-/]+$");

    @Autowired
    private CloudflareR2Properties properties;

    private CloseableHttpClient httpClient;

    @PostConstruct
    public void initHttpClient()
    {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(15000)
                .setSocketTimeout(60000)
                .setConnectionRequestTimeout(15000)
                .setExpectContinueEnabled(false)
                .build();
        httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    @PreDestroy
    public void closeHttpClient()
    {
        if (httpClient != null)
        {
            try
            {
                httpClient.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }

    public void putObject(String key, byte[] body, String contentType) throws IOException
    {
        assertSafeKey(key);
        if (body == null)
        {
            body = new byte[0];
        }
        if (StringUtils.isEmpty(contentType))
        {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        HttpPut put = new HttpPut(objectUrl(key));
        sign(put, "PUT", key, contentType, body);
        put.setEntity(new ByteArrayEntity(body, ContentType.parse(contentType)));
        CloseableHttpResponse resp = httpClient.execute(put);
        try
        {
            int code = resp.getStatusLine().getStatusCode();
            if (code < 200 || code >= 300)
            {
                throw new IOException("R2 PUT failed HTTP " + code + ": " + errorBody(resp));
            }
        }
        finally
        {
            EntityUtils.consumeQuietly(resp.getEntity());
            resp.close();
        }
    }

    public void writeObject(String key, HttpServletResponse response) throws IOException
    {
        assertSafeKey(key);
        HttpGet get = new HttpGet(objectUrl(key));
        sign(get, "GET", key, null, new byte[0]);
        CloseableHttpResponse resp = httpClient.execute(get);
        try
        {
            int code = resp.getStatusLine().getStatusCode();
            if (code == 404)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (code < 200 || code >= 300)
            {
                throw new IOException("R2 GET failed HTTP " + code + ": " + errorBody(resp));
            }
            HttpEntity entity = resp.getEntity();
            Header contentType = entity == null ? null : entity.getContentType();
            if (contentType != null && StringUtils.isNotEmpty(contentType.getValue()))
            {
                response.setContentType(contentType.getValue());
            }
            if (entity != null && entity.getContentLength() >= 0)
            {
                response.setContentLengthLong(entity.getContentLength());
            }
            if (entity != null)
            {
                InputStream in = entity.getContent();
                copy(in, response.getOutputStream());
            }
            response.flushBuffer();
        }
        finally
        {
            resp.close();
        }
    }

    public void deleteObject(String key)
    {
        if (StringUtils.isEmpty(key))
        {
            return;
        }
        try
        {
            assertSafeKey(key);
            HttpDelete del = new HttpDelete(objectUrl(key));
            sign(del, "DELETE", key, null, new byte[0]);
            CloseableHttpResponse resp = httpClient.execute(del);
            try
            {
                int code = resp.getStatusLine().getStatusCode();
                if (code != 404 && (code < 200 || code >= 300))
                {
                    log.warn("R2 DELETE HTTP {}: {}", code, errorBody(resp));
                }
            }
            finally
            {
                EntityUtils.consumeQuietly(resp.getEntity());
                resp.close();
            }
        }
        catch (Exception e)
        {
            log.warn("R2 DELETE skipped for {}: {}", key, e.getMessage());
        }
    }

    public static void assertSafeKey(String key)
    {
        if (StringUtils.isEmpty(key) || key.contains("..") || key.startsWith("/") || !SAFE_KEY.matcher(key).matches())
        {
            throw new IllegalArgumentException("illegal object key");
        }
    }

    private void sign(HttpRequestBase request, String method, String key, String contentType, byte[] body)
    {
        String host = hostOf(properties.trimmedEndpoint());
        String bucket = properties.getBucket().trim();
        String region = StringUtils.isEmpty(properties.getRegion()) ? "auto" : properties.getRegion().trim();
        String accessKey = properties.getAccessKey().trim();
        String secretKey = properties.getSecretKey().trim();
        String canonicalUri = "/" + uriEncode(bucket, false) + "/" + uriEncode(key, false);
        String amzDate = amzDate();
        String dateStamp = amzDate.substring(0, 8);
        String payloadHash = (body == null || body.length == 0) ? EMPTY_SHA256 : sha256Hex(body);

        StringBuilder headerPairs = new StringBuilder();
        if (StringUtils.isNotEmpty(contentType))
        {
            headerPairs.append("content-type:").append(contentType).append('\n');
        }
        headerPairs.append("host:").append(host).append('\n');
        headerPairs.append("x-amz-content-sha256:").append(payloadHash).append('\n');
        headerPairs.append("x-amz-date:").append(amzDate).append('\n');
        String signedHeaders = StringUtils.isNotEmpty(contentType)
                ? "content-type;host;x-amz-content-sha256;x-amz-date"
                : "host;x-amz-content-sha256;x-amz-date";

        String canonicalRequest = method + "\n" + canonicalUri + "\n\n" + headerPairs + "\n" + signedHeaders + "\n"
                + payloadHash;
        String credentialScope = dateStamp + "/" + region + "/s3/aws4_request";
        String stringToSign = "AWS4-HMAC-SHA256\n" + amzDate + "\n" + credentialScope + "\n"
                + sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));
        String signature = hex(hmac(signingKey(secretKey, dateStamp, region),
                stringToSign.getBytes(StandardCharsets.UTF_8)));
        String authorization = "AWS4-HMAC-SHA256 Credential=" + accessKey + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;

        request.setHeader("Accept", "*/*");
        request.setHeader("User-Agent", "ruoyi-r2");
        request.setHeader("x-amz-date", amzDate);
        request.setHeader("x-amz-content-sha256", payloadHash);
        request.setHeader("Authorization", authorization);
        if (StringUtils.isNotEmpty(contentType))
        {
            request.setHeader("Content-Type", contentType);
        }
    }

    private String objectUrl(String key)
    {
        return properties.trimmedEndpoint() + "/" + properties.getBucket().trim() + "/" + key;
    }

    private static byte[] signingKey(String secret, String dateStamp, String region)
    {
        byte[] kDate = hmac(("AWS4" + secret).getBytes(StandardCharsets.UTF_8), dateStamp.getBytes(StandardCharsets.UTF_8));
        byte[] kRegion = hmac(kDate, region.getBytes(StandardCharsets.UTF_8));
        byte[] kService = hmac(kRegion, "s3".getBytes(StandardCharsets.UTF_8));
        return hmac(kService, "aws4_request".getBytes(StandardCharsets.UTF_8));
    }

    private static String hostOf(String endpoint)
    {
        String value = endpoint;
        if (value.startsWith("https://"))
        {
            value = value.substring(8);
        }
        else if (value.startsWith("http://"))
        {
            value = value.substring(7);
        }
        int slash = value.indexOf('/');
        return slash >= 0 ? value.substring(0, slash) : value;
    }

    private static String amzDate()
    {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
        fmt.setTimeZone(new SimpleTimeZone(0, "UTC"));
        return fmt.format(new Date());
    }

    private static String uriEncode(String input, boolean encodeSlash)
    {
        StringBuilder result = new StringBuilder();
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++)
        {
            int ch = bytes[i] & 0xff;
            if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
                    || ch == '_' || ch == '-' || ch == '~' || ch == '.')
            {
                result.append((char) ch);
            }
            else if (ch == '/' && !encodeSlash)
            {
                result.append('/');
            }
            else
            {
                result.append(String.format("%%%02X", ch));
            }
        }
        return result.toString();
    }

    private static byte[] hmac(byte[] key, byte[] data)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    private static String sha256Hex(byte[] data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return hex(md.digest(data));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String hex(byte[] data)
    {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (int i = 0; i < data.length; i++)
        {
            sb.append(String.format("%02x", data[i] & 0xff));
        }
        return sb.toString();
    }

    private static String errorBody(CloseableHttpResponse resp)
    {
        try
        {
            HttpEntity entity = resp.getEntity();
            if (entity == null)
            {
                return "";
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            entity.writeTo(out);
            String text = new String(out.toByteArray(), StandardCharsets.UTF_8);
            return text.length() > 500 ? text.substring(0, 500) : text;
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) >= 0)
        {
            out.write(buf, 0, n);
        }
    }
}

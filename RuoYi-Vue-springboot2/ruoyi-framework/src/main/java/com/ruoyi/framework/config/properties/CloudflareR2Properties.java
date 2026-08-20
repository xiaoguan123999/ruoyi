package com.ruoyi.framework.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;

/**
 * Cloudflare R2 (S3 compatible) storage settings.
 * Keys come from env R2_ACCESS_KEY / R2_SECRET_KEY / R2_PUBLIC_URL.
 */
@Component
@ConfigurationProperties(prefix = "ruoyi.r2")
public class CloudflareR2Properties
{
    /** Switch. When true but keys are empty, upload falls back to local disk. */
    private boolean enabled = false;

    /** https://&lt;account_id&gt;.r2.cloudflarestorage.com */
    private String endpoint;

    private String bucket;

    /** R2 always uses region auto */
    private String region = "auto";

    private String accessKey;

    private String secretKey;

    /**
     * Public base URL after enabling r2.dev / custom domain, e.g. https://pub-xxxx.r2.dev
     * Empty: files are served via GET /common/r2/{key}
     */
    private String publicUrl;

    /** Default object-key prefix for /common/upload */
    private String prefix = "upload";

    public boolean isReady()
    {
        return enabled
                && StringUtils.isNotEmpty(endpoint)
                && StringUtils.isNotEmpty(bucket)
                && StringUtils.isNotEmpty(accessKey)
                && StringUtils.isNotEmpty(secretKey);
    }

    public String trimmedPublicUrl()
    {
        if (StringUtils.isEmpty(publicUrl))
        {
            return "";
        }
        String value = publicUrl.trim();
        if (value.endsWith("/"))
        {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    public String trimmedEndpoint()
    {
        if (StringUtils.isEmpty(endpoint))
        {
            return "";
        }
        String value = endpoint.trim();
        if (value.endsWith("/"))
        {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getBucket()
    {
        return bucket;
    }

    public void setBucket(String bucket)
    {
        this.bucket = bucket;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public void setAccessKey(String accessKey)
    {
        this.accessKey = accessKey;
    }

    public String getSecretKey()
    {
        return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getPublicUrl()
    {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl)
    {
        this.publicUrl = publicUrl;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }
}

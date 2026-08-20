package com.ruoyi.framework.storage;

/**
 * Result of a stored upload (R2 or local disk).
 */
public class StoredFile
{
    /** Value stored in DB / returned as fileName. Relative path or full https URL. */
    private String fileName;

    /** Absolute URL for browsers when possible. */
    private String url;

    /** R2 object key, empty when stored locally. */
    private String objectKey;

    private String originalFilename;

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getObjectKey()
    {
        return objectKey;
    }

    public void setObjectKey(String objectKey)
    {
        this.objectKey = objectKey;
    }

    public String getOriginalFilename()
    {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename)
    {
        this.originalFilename = originalFilename;
    }
}

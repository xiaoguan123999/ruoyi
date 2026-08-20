package com.ruoyi.framework.storage;

import java.util.Locale;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.file.FileNameLengthLimitExceededException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.framework.config.properties.CloudflareR2Properties;

/**
 * Upload to Cloudflare R2 when configured, otherwise local disk.
 */
@Service
public class FileStorageService
{
    public static final String PROXY_PREFIX = "/common/r2/";

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private CloudflareR2Properties r2Properties;

    @Autowired
    private CloudflareR2Client r2Client;

    @Autowired
    private ServerConfig serverConfig;

    @PostConstruct
    public void init()
    {
        if (r2Properties.isReady())
        {
            log.info("Cloudflare R2 enabled, bucket={}, endpoint={}", r2Properties.getBucket(),
                    r2Properties.trimmedEndpoint());
        }
        else if (r2Properties.isEnabled())
        {
            log.warn("ruoyi.r2.enabled=true but R2_ACCESS_KEY/R2_SECRET_KEY empty, upload uses local disk");
        }
    }

    public boolean isR2Ready()
    {
        return r2Properties.isReady();
    }

    public StoredFile upload(MultipartFile file) throws Exception
    {
        return upload(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION, defaultFolder(), true);
    }

    public StoredFile uploadImage(MultipartFile file, String folder) throws Exception
    {
        return upload(file, MimeTypeUtils.IMAGE_EXTENSION, folder, true);
    }

    public StoredFile upload(MultipartFile file, String[] allowedExtension, String folder, boolean uuidName)
            throws Exception
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("file is empty");
        }
        String original = file.getOriginalFilename();
        if (original != null && original.length() > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH)
        {
            throw new FileNameLengthLimitExceededException(
                    FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }
        FileUploadUtils.assertAllowed(file, allowedExtension);
        if (isR2Ready())
        {
            return uploadToR2(file, folder, uuidName);
        }
        return uploadLocal(file, folder, allowedExtension, uuidName);
    }

    public void writeR2(String key, javax.servlet.http.HttpServletResponse response) throws Exception
    {
        r2Client.writeObject(key, response);
    }

    /**
     * Delete a previously stored file. Ignores local files that are not under /profile.
     */
    public void deleteQuietly(String storedName)
    {
        if (StringUtils.isEmpty(storedName))
        {
            return;
        }
        String key = extractR2Key(storedName);
        if (StringUtils.isNotEmpty(key))
        {
            if (isR2Ready())
            {
                r2Client.deleteObject(key);
            }
            return;
        }
        if (storedName.startsWith(Constants.RESOURCE_PREFIX))
        {
            FileUtils.deleteFile(RuoYiConfig.getProfile() + FileUtils.stripPrefix(storedName));
        }
    }

    public String extractR2Key(String storedName)
    {
        if (StringUtils.isEmpty(storedName))
        {
            return null;
        }
        String publicUrl = r2Properties.trimmedPublicUrl();
        if (StringUtils.isNotEmpty(publicUrl) && storedName.startsWith(publicUrl + "/"))
        {
            return storedName.substring(publicUrl.length() + 1);
        }
        int idx = storedName.indexOf(PROXY_PREFIX);
        if (idx >= 0)
        {
            return storedName.substring(idx + PROXY_PREFIX.length());
        }
        if (storedName.startsWith("upload/") || storedName.startsWith("avatar/") || storedName.startsWith("app/"))
        {
            return storedName;
        }
        return null;
    }

    private StoredFile uploadToR2(MultipartFile file, String folder, boolean uuidName) throws Exception
    {
        String objectKey = buildObjectKey(folder, file, uuidName);
        r2Client.putObject(objectKey, file.getBytes(), contentType(file));
        StoredFile stored = new StoredFile();
        stored.setObjectKey(objectKey);
        stored.setOriginalFilename(file.getOriginalFilename());
        String publicUrl = r2Properties.trimmedPublicUrl();
        if (StringUtils.isNotEmpty(publicUrl))
        {
            String url = publicUrl + "/" + objectKey;
            stored.setFileName(url);
            stored.setUrl(url);
        }
        else
        {
            String fileName = PROXY_PREFIX + objectKey;
            stored.setFileName(fileName);
            stored.setUrl(serverConfig.getUrl() + fileName);
        }
        return stored;
    }

    private StoredFile uploadLocal(MultipartFile file, String folder, String[] allowedExtension, boolean uuidName)
            throws Exception
    {
        String baseDir = RuoYiConfig.getProfile() + "/" + folder;
        String fileName = FileUploadUtils.upload(baseDir, file, allowedExtension, uuidName);
        StoredFile stored = new StoredFile();
        stored.setFileName(fileName);
        stored.setUrl(serverConfig.getUrl() + fileName);
        stored.setOriginalFilename(file.getOriginalFilename());
        return stored;
    }

    private String defaultFolder()
    {
        return StringUtils.isEmpty(r2Properties.getPrefix()) ? "upload" : r2Properties.getPrefix();
    }

    private String buildObjectKey(String folder, MultipartFile file, boolean uuidName)
    {
        String safeFolder = StringUtils.isEmpty(folder) ? defaultFolder() : folder.trim();
        if (safeFolder.startsWith("/"))
        {
            safeFolder = safeFolder.substring(1);
        }
        if (safeFolder.endsWith("/"))
        {
            safeFolder = safeFolder.substring(0, safeFolder.length() - 1);
        }
        String name;
        if (uuidName)
        {
            name = IdUtils.fastSimpleUUID() + "." + FileUploadUtils.getExtension(file);
        }
        else
        {
            name = FileUploadUtils.extractFilename(file);
            int slash = name.lastIndexOf('/');
            if (slash >= 0)
            {
                name = name.substring(slash + 1);
            }
        }
        return safeFolder + "/" + DateUtils.datePath() + "/" + name;
    }

    private String contentType(MultipartFile file)
    {
        String type = file.getContentType();
        if (StringUtils.isNotEmpty(type) && !"application/octet-stream".equalsIgnoreCase(type))
        {
            return type;
        }
        String ext = FileUploadUtils.getExtension(file);
        if (StringUtils.isEmpty(ext))
        {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        ext = ext.toLowerCase(Locale.ROOT);
        if ("png".equals(ext))
        {
            return "image/png";
        }
        if ("jpg".equals(ext) || "jpeg".equals(ext))
        {
            return "image/jpeg";
        }
        if ("gif".equals(ext))
        {
            return "image/gif";
        }
        if ("bmp".equals(ext))
        {
            return "image/bmp";
        }
        if ("webp".equals(ext))
        {
            return "image/webp";
        }
        if ("pdf".equals(ext))
        {
            return "application/pdf";
        }
        if ("mp4".equals(ext))
        {
            return "video/mp4";
        }
        String byName = FilenameUtils.getName(file.getOriginalFilename());
        if (StringUtils.isNotEmpty(byName) && byName.contains("."))
        {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}

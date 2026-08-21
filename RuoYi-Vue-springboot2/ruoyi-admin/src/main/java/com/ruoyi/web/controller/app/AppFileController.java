package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.biz.api.AppUploadResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.framework.storage.FileStorageService;
import com.ruoyi.framework.storage.StoredFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-文件")
@RestController
@RequestMapping("/app")
public class AppFileController extends BaseController
{
    @Autowired
    private FileStorageService fileStorageService;

    @ApiOperation(value = "上传图片", notes = "multipart 字段名 file。返回 url、fileName 在响应根上，不在 data 里。")
    @PostMapping("/upload")
    public AppUploadResult upload(@RequestParam("file") MultipartFile file) throws Exception
    {
        StoredFile stored = fileStorageService.uploadImage(file, "app");
        return AppUploadResult.of(stored.getUrl(), stored.getFileName(),
                FileUtils.getName(stored.getFileName()), stored.getOriginalFilename());
    }
}

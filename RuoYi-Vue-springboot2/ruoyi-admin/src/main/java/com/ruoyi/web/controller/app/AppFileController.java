package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
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

    @ApiOperation("上传图片到 Cloudflare R2，返回 url / fileName")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws Exception
    {
        StoredFile stored = fileStorageService.uploadImage(file, "app");
        AjaxResult ajax = AjaxResult.success();
        ajax.put("url", stored.getUrl());
        ajax.put("fileName", stored.getFileName());
        ajax.put("newFileName", FileUtils.getName(stored.getFileName()));
        ajax.put("originalFilename", stored.getOriginalFilename());
        return ajax;
    }
}

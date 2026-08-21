package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("上传结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUploadResult extends AppOkResult
{
    @ApiModelProperty("可访问的完整URL或代理路径")
    private String url;
    @ApiModelProperty("存储路径，例如 /common/r2/app/2026/08/21/xxx.png")
    private String fileName;
    @ApiModelProperty("新文件名")
    private String newFileName;
    @ApiModelProperty("原始文件名")
    private String originalFilename;

    public static AppUploadResult of(String url, String fileName, String newFileName, String originalFilename)
    {
        AppUploadResult r = new AppUploadResult();
        r.setCode(Integer.valueOf(200));
        r.setMsg("操作成功");
        r.url = url;
        r.fileName = fileName;
        r.newFileName = newFileName;
        r.originalFilename = originalFilename;
        return r;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getNewFileName() { return newFileName; }
    public void setNewFileName(String newFileName) { this.newFileName = newFileName; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
}

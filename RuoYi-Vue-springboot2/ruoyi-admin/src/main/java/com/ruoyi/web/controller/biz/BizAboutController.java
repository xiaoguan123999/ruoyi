package com.ruoyi.web.controller.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizAbout;
import com.ruoyi.biz.service.IBizAboutService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-关于我们")
@RestController
@RequestMapping("/biz/about")
public class BizAboutController extends BaseController
{
    @Autowired
    private IBizAboutService aboutService;

    @ApiOperation("查询关于我们（全局一条）")
    @PreAuthorize("@ss.hasPermi('biz:about:list')")
    @GetMapping
    public AjaxResult getInfo()
    {
        return success(aboutService.getSingleton());
    }

    @ApiOperation("保存关于我们（全局一条）")
    @PreAuthorize("@ss.hasPermi('biz:about:edit')")
    @Log(title = "关于我们", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult save(@RequestBody BizAbout about)
    {
        about.setUpdateBy(getUsername());
        return toAjax(aboutService.saveSingleton(about));
    }
}

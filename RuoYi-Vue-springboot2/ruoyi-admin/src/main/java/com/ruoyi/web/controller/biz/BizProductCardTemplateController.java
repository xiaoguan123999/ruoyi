package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizProductCardTemplate;
import com.ruoyi.biz.service.IBizProductCardTemplateService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-产品卡片模板")
@RestController
@RequestMapping("/biz/productCardTemplate")
public class BizProductCardTemplateController extends BaseController
{
    @Autowired
    private IBizProductCardTemplateService templateService;

    @ApiOperation("模板列表")
    @PreAuthorize("@ss.hasPermi('biz:productCardTemplate:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizProductCardTemplate query)
    {
        startPage();
        List<BizProductCardTemplate> list = templateService.selectTemplateList(query);
        return getDataTable(list);
    }

    @ApiOperation("启用模板下拉")
    @PreAuthorize("@ss.hasAnyPermi('biz:productCardTemplate:query,biz:product:list,biz:productCategory:list')")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(templateService.selectEnabledOptions());
    }

    @ApiOperation("模板详情")
    @PreAuthorize("@ss.hasPermi('biz:productCardTemplate:query')")
    @GetMapping("/{templateId}")
    public AjaxResult getInfo(@PathVariable Long templateId)
    {
        return success(templateService.selectTemplateById(templateId));
    }

    @ApiOperation("新增模板")
    @PreAuthorize("@ss.hasPermi('biz:productCardTemplate:add')")
    @Log(title = "产品卡片模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizProductCardTemplate template)
    {
        template.setCreateBy(getUsername());
        return toAjax(templateService.insertTemplate(template));
    }

    @ApiOperation("修改模板")
    @PreAuthorize("@ss.hasPermi('biz:productCardTemplate:edit')")
    @Log(title = "产品卡片模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizProductCardTemplate template)
    {
        template.setUpdateBy(getUsername());
        return toAjax(templateService.updateTemplate(template));
    }

    @ApiOperation("删除模板")
    @PreAuthorize("@ss.hasPermi('biz:productCardTemplate:remove')")
    @Log(title = "产品卡片模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds)
    {
        return toAjax(templateService.deleteTemplateByIds(templateIds));
    }
}

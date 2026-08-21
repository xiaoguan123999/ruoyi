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
import com.ruoyi.biz.domain.BizProductCategory;
import com.ruoyi.biz.service.IBizProductCategoryService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-产品分类")
@RestController
@RequestMapping("/biz/productCategory")
public class BizProductCategoryController extends BaseController
{
    @Autowired
    private IBizProductCategoryService categoryService;

    @ApiOperation("分类列表")
    @PreAuthorize("@ss.hasPermi('biz:productCategory:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizProductCategory category)
    {
        startPage();
        List<BizProductCategory> list = categoryService.selectCategoryList(category);
        return getDataTable(list);
    }

    @ApiOperation("分类下拉，给产品表单用")
    @PreAuthorize("@ss.hasPermi('biz:product:list')")
    @GetMapping("/options")
    public AjaxResult options()
    {
        BizProductCategory query = new BizProductCategory();
        query.setStatus("0");
        return success(categoryService.selectCategoryList(query));
    }

    @ApiOperation("分类详情")
    @PreAuthorize("@ss.hasPermi('biz:productCategory:query')")
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId)
    {
        return success(categoryService.selectCategoryById(categoryId));
    }

    @ApiOperation("新增分类")
    @PreAuthorize("@ss.hasPermi('biz:productCategory:add')")
    @Log(title = "产品分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizProductCategory category)
    {
        category.setCreateBy(getUsername());
        return toAjax(categoryService.insertCategory(category));
    }

    @ApiOperation("修改分类")
    @PreAuthorize("@ss.hasPermi('biz:productCategory:edit')")
    @Log(title = "产品分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizProductCategory category)
    {
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.updateCategory(category));
    }

    @ApiOperation("删除分类")
    @PreAuthorize("@ss.hasPermi('biz:productCategory:remove')")
    @Log(title = "产品分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(categoryService.deleteCategoryByIds(categoryIds));
    }
}

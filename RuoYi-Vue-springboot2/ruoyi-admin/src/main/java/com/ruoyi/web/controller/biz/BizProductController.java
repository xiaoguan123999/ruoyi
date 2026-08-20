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
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.service.IBizProductService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-产品")
@RestController
@RequestMapping("/biz/product")
public class BizProductController extends BaseController
{
    @Autowired
    private IBizProductService productService;

    @ApiOperation("产品列表")
    @PreAuthorize("@ss.hasPermi('biz:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizProduct product)
    {
        startPage();
        List<BizProduct> list = productService.selectProductList(product);
        return getDataTable(list);
    }

    @ApiOperation("产品详情")
    @PreAuthorize("@ss.hasPermi('biz:product:query')")
    @GetMapping("/{productId}")
    public AjaxResult getInfo(@PathVariable Long productId)
    {
        return success(productService.selectProductById(productId));
    }

    @ApiOperation("新增产品")
    @PreAuthorize("@ss.hasPermi('biz:product:add')")
    @Log(title = "认购产品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizProduct product)
    {
        product.setCreateBy(getUsername());
        return toAjax(productService.insertProduct(product));
    }

    @ApiOperation("修改产品")
    @PreAuthorize("@ss.hasPermi('biz:product:edit')")
    @Log(title = "认购产品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizProduct product)
    {
        product.setUpdateBy(getUsername());
        return toAjax(productService.updateProduct(product));
    }

    @ApiOperation("删除产品")
    @PreAuthorize("@ss.hasPermi('biz:product:remove')")
    @Log(title = "认购产品", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds)
    {
        return toAjax(productService.deleteProductByIds(productIds));
    }
}

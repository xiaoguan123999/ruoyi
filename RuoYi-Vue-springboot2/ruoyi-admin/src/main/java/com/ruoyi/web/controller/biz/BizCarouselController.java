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
import com.ruoyi.biz.domain.BizCarousel;
import com.ruoyi.biz.service.IBizCarouselService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-视频轮播")
@RestController
@RequestMapping("/biz/carousel")
public class BizCarouselController extends BaseController
{
    @Autowired
    private IBizCarouselService carouselService;

    @ApiOperation("轮播列表")
    @PreAuthorize("@ss.hasPermi('biz:carousel:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCarousel carousel)
    {
        startPage();
        List<BizCarousel> list = carouselService.selectCarouselList(carousel);
        return getDataTable(list);
    }

    @ApiOperation("轮播详情")
    @PreAuthorize("@ss.hasPermi('biz:carousel:query')")
    @GetMapping("/{carouselId}")
    public AjaxResult getInfo(@PathVariable Long carouselId)
    {
        return success(carouselService.selectCarouselById(carouselId));
    }

    @ApiOperation("新增轮播")
    @PreAuthorize("@ss.hasPermi('biz:carousel:add')")
    @Log(title = "视频轮播", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizCarousel carousel)
    {
        carousel.setCreateBy(getUsername());
        return toAjax(carouselService.insertCarousel(carousel));
    }

    @ApiOperation("修改轮播")
    @PreAuthorize("@ss.hasPermi('biz:carousel:edit')")
    @Log(title = "视频轮播", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizCarousel carousel)
    {
        carousel.setUpdateBy(getUsername());
        return toAjax(carouselService.updateCarousel(carousel));
    }

    @ApiOperation("删除轮播")
    @PreAuthorize("@ss.hasPermi('biz:carousel:remove')")
    @Log(title = "视频轮播", businessType = BusinessType.DELETE)
    @DeleteMapping("/{carouselIds}")
    public AjaxResult remove(@PathVariable Long[] carouselIds)
    {
        return toAjax(carouselService.deleteCarouselByIds(carouselIds));
    }
}

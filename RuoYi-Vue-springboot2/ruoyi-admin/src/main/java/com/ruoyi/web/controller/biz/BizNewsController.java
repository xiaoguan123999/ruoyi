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
import com.ruoyi.biz.domain.BizNews;
import com.ruoyi.biz.service.IBizNewsService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-新闻资讯")
@RestController
@RequestMapping("/biz/news")
public class BizNewsController extends BaseController
{
    @Autowired
    private IBizNewsService newsService;

    @ApiOperation("新闻列表")
    @PreAuthorize("@ss.hasPermi('biz:news:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizNews news)
    {
        startPage();
        List<BizNews> list = newsService.selectNewsList(news);
        return getDataTable(list);
    }

    @ApiOperation("新闻详情")
    @PreAuthorize("@ss.hasPermi('biz:news:query')")
    @GetMapping("/{newsId}")
    public AjaxResult getInfo(@PathVariable Long newsId)
    {
        return success(newsService.selectNewsById(newsId));
    }

    @ApiOperation("新增新闻")
    @PreAuthorize("@ss.hasPermi('biz:news:add')")
    @Log(title = "新闻资讯", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizNews news)
    {
        news.setCreateBy(getUsername());
        return toAjax(newsService.insertNews(news));
    }

    @ApiOperation("修改新闻")
    @PreAuthorize("@ss.hasPermi('biz:news:edit')")
    @Log(title = "新闻资讯", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizNews news)
    {
        news.setUpdateBy(getUsername());
        return toAjax(newsService.updateNews(news));
    }

    @ApiOperation("删除新闻")
    @PreAuthorize("@ss.hasPermi('biz:news:remove')")
    @Log(title = "新闻资讯", businessType = BusinessType.DELETE)
    @DeleteMapping("/{newsIds}")
    public AjaxResult remove(@PathVariable Long[] newsIds)
    {
        return toAjax(newsService.deleteNewsByIds(newsIds));
    }
}

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
import com.ruoyi.biz.domain.BizCsChannel;
import com.ruoyi.biz.domain.BizCsConfig;
import com.ruoyi.biz.service.IBizCsService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-客服中心")
@RestController
@RequestMapping("/biz/service")
public class BizCsController extends BaseController
{
    @Autowired
    private IBizCsService csService;

    @ApiOperation("客服文案配置")
    @PreAuthorize("@ss.hasPermi('biz:service:query')")
    @GetMapping("/config")
    public AjaxResult config()
    {
        return success(csService.getConfig());
    }

    @ApiOperation("保存客服文案")
    @PreAuthorize("@ss.hasPermi('biz:service:edit')")
    @Log(title = "客服中心", businessType = BusinessType.UPDATE)
    @PutMapping("/config")
    public AjaxResult saveConfig(@RequestBody BizCsConfig config)
    {
        csService.saveConfig(config);
        return success();
    }

    @ApiOperation("客服渠道列表")
    @PreAuthorize("@ss.hasPermi('biz:service:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCsChannel query)
    {
        startPage();
        List<BizCsChannel> list = csService.selectChannelList(query);
        return getDataTable(list);
    }

    @ApiOperation("客服渠道详情")
    @PreAuthorize("@ss.hasPermi('biz:service:query')")
    @GetMapping("/{channelId}")
    public AjaxResult getInfo(@PathVariable Long channelId)
    {
        return success(csService.selectChannelById(channelId));
    }

    @ApiOperation("新增客服渠道")
    @PreAuthorize("@ss.hasPermi('biz:service:add')")
    @Log(title = "客服渠道", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizCsChannel channel)
    {
        channel.setCreateBy(getUsername());
        return toAjax(csService.insertChannel(channel));
    }

    @ApiOperation("修改客服渠道")
    @PreAuthorize("@ss.hasPermi('biz:service:edit')")
    @Log(title = "客服渠道", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizCsChannel channel)
    {
        channel.setUpdateBy(getUsername());
        return toAjax(csService.updateChannel(channel));
    }

    @ApiOperation("删除客服渠道")
    @PreAuthorize("@ss.hasPermi('biz:service:remove')")
    @Log(title = "客服渠道", businessType = BusinessType.DELETE)
    @DeleteMapping("/{channelIds}")
    public AjaxResult remove(@PathVariable Long[] channelIds)
    {
        return toAjax(csService.deleteChannelByIds(channelIds));
    }
}

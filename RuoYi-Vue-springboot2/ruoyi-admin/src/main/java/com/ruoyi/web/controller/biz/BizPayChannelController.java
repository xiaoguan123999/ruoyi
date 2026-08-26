package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizPayChannel;
import com.ruoyi.biz.domain.BizPayProvider;
import com.ruoyi.biz.service.IBizOnlinePayService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-支付通道")
@RestController
@RequestMapping("/biz/payChannel")
public class BizPayChannelController extends BaseController
{
    @Autowired
    private IBizOnlinePayService onlinePayService;

    @ApiOperation("通道列表")
    @PreAuthorize("@ss.hasPermi('biz:payChannel:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizPayChannel query)
    {
        startPage();
        List<BizPayChannel> list = onlinePayService.selectChannelList(query);
        return getDataTable(list);
    }

    @ApiOperation("通道详情")
    @PreAuthorize("@ss.hasPermi('biz:payChannel:query')")
    @GetMapping("/{channelId}")
    public AjaxResult getInfo(@PathVariable Long channelId)
    {
        return success(onlinePayService.selectChannelById(channelId));
    }

    @ApiOperation("修改通道")
    @PreAuthorize("@ss.hasPermi('biz:payChannel:edit')")
    @Log(title = "支付通道", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizPayChannel row)
    {
        return toAjax(onlinePayService.updateChannel(row));
    }

    @ApiOperation("服务商列表")
    @PreAuthorize("@ss.hasPermi('biz:payProvider:list')")
    @GetMapping("/providers")
    public AjaxResult providers(BizPayProvider query)
    {
        return success(onlinePayService.selectProviderList(query));
    }

    @ApiOperation("修改服务商")
    @PreAuthorize("@ss.hasPermi('biz:payProvider:edit')")
    @Log(title = "支付服务商", businessType = BusinessType.UPDATE)
    @PutMapping("/provider")
    public AjaxResult editProvider(@RequestBody BizPayProvider row)
    {
        return toAjax(onlinePayService.updateProvider(row));
    }
}

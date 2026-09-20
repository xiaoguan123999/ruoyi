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
import java.util.HashMap;
import java.util.Map;
import com.ruoyi.biz.domain.BizPayOrder;
import com.ruoyi.biz.service.IBizOnlinePayService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-支付订单")
@RestController
@RequestMapping("/biz/payOrder")
public class BizPayOrderController extends BaseController
{
    @Autowired
    private IBizOnlinePayService onlinePayService;

    @ApiOperation("支付单列表")
    @PreAuthorize("@ss.hasPermi('biz:payOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizPayOrder query)
    {
        startPage();
        List<BizPayOrder> list = onlinePayService.selectPayOrderList(query);
        return getDataTable(list);
    }

    @ApiOperation("支付单详情")
    @PreAuthorize("@ss.hasPermi('biz:payOrder:query')")
    @GetMapping("/{outTradeNo}")
    public AjaxResult getInfo(@PathVariable String outTradeNo)
    {
        return success(onlinePayService.selectPayOrderByOutTradeNo(outTradeNo));
    }

    @ApiOperation("向三方查单并补单")
    @PreAuthorize("@ss.hasPermi('biz:payOrder:query')")
    @Log(title = "支付查单补单", businessType = BusinessType.UPDATE)
    @PutMapping("/sync/{outTradeNo}")
    public AjaxResult sync(@PathVariable String outTradeNo)
    {
        return success(onlinePayService.syncFromProvider(outTradeNo));
    }

    @ApiOperation("模拟到账")
    @PreAuthorize("@ss.hasPermi('biz:payOrder:simulate')")
    @Log(title = "模拟支付到账", businessType = BusinessType.UPDATE)
    @PutMapping("/simulate/{outTradeNo}")
    public AjaxResult simulate(@PathVariable String outTradeNo)
    {
        onlinePayService.simulatePaid(outTradeNo, getUsername());
        return success();
    }

    @ApiOperation("待付超时分钟")
    @PreAuthorize("@ss.hasPermi('biz:payOrder:list')")
    @GetMapping("/expireMinutes")
    public AjaxResult getExpireMinutes()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("expireMinutes", Integer.valueOf(onlinePayService.getOrderExpireMinutes()));
        return success(data);
    }

    @ApiOperation("保存待付超时分钟")
    @PreAuthorize("@ss.hasPermi('biz:payOrder:list')")
    @Log(title = "支付超时分钟", businessType = BusinessType.UPDATE)
    @PutMapping("/expireMinutes")
    public AjaxResult saveExpireMinutes(@RequestBody Map<String, Object> body)
    {
        Object raw = body == null ? null : body.get("expireMinutes");
        int minutes = raw == null ? 0 : Integer.parseInt(String.valueOf(raw));
        onlinePayService.saveOrderExpireMinutes(minutes);
        return success();
    }
}

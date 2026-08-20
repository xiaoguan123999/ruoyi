package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizOrder;
import com.ruoyi.biz.service.IBizOrderService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-认购订单")
@RestController
@RequestMapping("/biz/order")
public class BizOrderController extends BaseController
{
    @Autowired
    private IBizOrderService orderService;

    @ApiOperation("订单列表")
    @PreAuthorize("@ss.hasPermi('biz:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizOrder order)
    {
        startPage();
        List<BizOrder> list = orderService.selectOrderList(order);
        return getDataTable(list);
    }

    @ApiOperation("订单详情")
    @PreAuthorize("@ss.hasPermi('biz:order:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId)
    {
        return success(orderService.selectOrderById(orderId));
    }
}

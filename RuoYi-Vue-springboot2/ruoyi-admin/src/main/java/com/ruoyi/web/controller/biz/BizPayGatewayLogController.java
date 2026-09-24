package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizPayGatewayLog;
import com.ruoyi.biz.service.IBizPayGatewayLogService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-支付日志")
@RestController
@RequestMapping("/biz/payGatewayLog")
public class BizPayGatewayLogController extends BaseController
{
    @Autowired
    private IBizPayGatewayLogService payGatewayLogService;

    @ApiOperation("支付网关日志列表")
    @PreAuthorize("@ss.hasPermi('biz:payGatewayLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizPayGatewayLog query)
    {
        startPage();
        List<BizPayGatewayLog> list = payGatewayLogService.selectList(query);
        return getDataTable(list);
    }

    @ApiOperation("支付网关日志详情")
    @PreAuthorize("@ss.hasPermi('biz:payGatewayLog:query')")
    @GetMapping("/{logId}")
    public AjaxResult getInfo(@PathVariable Long logId)
    {
        return success(payGatewayLogService.selectById(logId));
    }
}

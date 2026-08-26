package com.ruoyi.web.controller.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.service.IBizDashboardService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-经营日报")
@RestController
@RequestMapping("/biz/dashboard")
public class BizDashboardController extends BaseController
{
    @Autowired
    private IBizDashboardService dashboardService;

    @ApiOperation("经营日报统计")
    @GetMapping("/stats")
    public AjaxResult stats(@RequestParam(value = "date", required = false) String date)
    {
        return success(dashboardService.selectStats(date));
    }

    @ApiOperation("近7日趋势")
    @GetMapping("/trend")
    public AjaxResult trend(@RequestParam(value = "date", required = false) String date)
    {
        return success(dashboardService.selectTrend(date));
    }
}

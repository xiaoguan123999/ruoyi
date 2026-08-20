package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizCommissionLog;
import com.ruoyi.biz.service.IBizCommissionService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-分佣记录")
@RestController
@RequestMapping("/biz/commission")
public class BizCommissionController extends BaseController
{
    @Autowired
    private IBizCommissionService commissionService;

    @ApiOperation("分佣记录")
    @PreAuthorize("@ss.hasPermi('biz:commission:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCommissionLog log)
    {
        startPage();
        List<BizCommissionLog> list = commissionService.selectCommissionList(log);
        return getDataTable(list);
    }
}

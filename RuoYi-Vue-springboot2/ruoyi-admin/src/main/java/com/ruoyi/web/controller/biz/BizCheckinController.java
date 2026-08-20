package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizCheckin;
import com.ruoyi.biz.service.IBizCheckinService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-签到")
@RestController
@RequestMapping("/biz/checkin")
public class BizCheckinController extends BaseController
{
    @Autowired
    private IBizCheckinService checkinService;

    @ApiOperation("签到记录")
    @PreAuthorize("@ss.hasPermi('biz:checkin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCheckin checkin)
    {
        startPage();
        List<BizCheckin> list = checkinService.selectCheckinList(checkin);
        return getDataTable(list);
    }
}

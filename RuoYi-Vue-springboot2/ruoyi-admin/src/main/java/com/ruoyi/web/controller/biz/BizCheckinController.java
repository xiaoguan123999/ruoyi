package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizCheckin;
import com.ruoyi.biz.domain.BizCheckinPrize;
import com.ruoyi.biz.domain.CheckinRule;
import com.ruoyi.biz.service.IBizCheckinService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
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

    @ApiOperation("签到规则")
    @PreAuthorize("@ss.hasPermi('biz:checkin:rule')")
    @GetMapping("/rule")
    public AjaxResult rule()
    {
        return success(checkinService.getCheckinRule());
    }

    @ApiOperation("保存签到规则")
    @PreAuthorize("@ss.hasPermi('biz:checkin:rule')")
    @Log(title = "签到规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rule")
    public AjaxResult saveRule(@RequestBody CheckinRule rule)
    {
        checkinService.saveCheckinRule(rule);
        return success();
    }

    @ApiOperation("签到中奖记录")
    @PreAuthorize("@ss.hasPermi('biz:checkin:prize')")
    @GetMapping("/prize/list")
    public TableDataInfo prizeList(BizCheckinPrize prize)
    {
        startPage();
        List<BizCheckinPrize> list = checkinService.selectPrizeList(prize);
        return getDataTable(list);
    }
}

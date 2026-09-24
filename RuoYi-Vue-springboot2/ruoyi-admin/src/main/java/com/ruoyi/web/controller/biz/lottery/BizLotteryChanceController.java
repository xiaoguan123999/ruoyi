package com.ruoyi.web.controller.biz.lottery;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizLotteryChance;
import com.ruoyi.biz.domain.BizLotteryChanceLog;
import com.ruoyi.biz.service.IBizLotteryChanceService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-用户抽奖次数")
@RestController
@RequestMapping("/biz/lotteryChance")
public class BizLotteryChanceController extends BaseController
{
    @Autowired
    private IBizLotteryChanceService lotteryChanceService;

    @ApiOperation("用户次数列表")
    @PreAuthorize("@ss.hasPermi('biz:lotteryChance:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLotteryChance query)
    {
        startPage();
        List<BizLotteryChance> list = lotteryChanceService.selectChanceList(query);
        return getDataTable(list);
    }

    @ApiOperation("次数变动流水")
    @PreAuthorize("@ss.hasPermi('biz:lotteryChance:query')")
    @GetMapping("/log/list")
    public TableDataInfo logList(BizLotteryChanceLog query)
    {
        startPage();
        List<BizLotteryChanceLog> list = lotteryChanceService.selectChanceLogList(query);
        return getDataTable(list);
    }

    @ApiOperation("后台调整次数")
    @PreAuthorize("@ss.hasPermi('biz:lotteryChance:adjust')")
    @Log(title = "抽奖次数调整", businessType = BusinessType.UPDATE)
    @PostMapping("/adjust")
    public AjaxResult adjust(@RequestBody Map<String, Object> body)
    {
        Long activityId = body.get("activityId") == null ? null : Long.valueOf(String.valueOf(body.get("activityId")));
        Long memberId = body.get("memberId") == null ? null : Long.valueOf(String.valueOf(body.get("memberId")));
        if (body.get("amount") == null)
        {
            throw new ServiceException("请填写调整次数");
        }
        int amount = Integer.parseInt(String.valueOf(body.get("amount")));
        String remark = body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        lotteryChanceService.adminAdjust(activityId, memberId, amount, remark, getUsername());
        return success();
    }
}

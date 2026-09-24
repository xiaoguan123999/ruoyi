package com.ruoyi.web.controller.biz.lottery;

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
import com.ruoyi.biz.domain.BizLotteryActivity;
import com.ruoyi.biz.domain.BizLotteryPrize;
import com.ruoyi.biz.domain.BizLotteryWinStrategy;
import com.ruoyi.biz.service.IBizLotteryActivityService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-抽奖活动")
@RestController
@RequestMapping("/biz/lotteryActivity")
public class BizLotteryActivityController extends BaseController
{
    @Autowired
    private IBizLotteryActivityService lotteryActivityService;

    @ApiOperation("活动列表")
    @PreAuthorize("@ss.hasPermi('biz:lotteryActivity:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLotteryActivity query)
    {
        startPage();
        List<BizLotteryActivity> list = lotteryActivityService.selectLotteryActivityList(query);
        return getDataTable(list);
    }

    @ApiOperation("系统唯一抽奖活动（可为空）")
    @PreAuthorize("@ss.hasPermi('biz:lotteryActivity:query')")
    @GetMapping("/sole")
    public AjaxResult sole()
    {
        return success(lotteryActivityService.selectSoleLotteryActivity());
    }

    @ApiOperation("活动详情")
    @PreAuthorize("@ss.hasPermi('biz:lotteryActivity:query')")
    @GetMapping("/{activityId}")
    public AjaxResult getInfo(@PathVariable Long activityId)
    {
        return success(lotteryActivityService.selectLotteryActivityById(activityId));
    }

    @ApiOperation("新增活动")
    @PreAuthorize("@ss.hasPermi('biz:lotteryActivity:add')")
    @Log(title = "抽奖活动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLotteryActivity activity)
    {
        activity.setCreateBy(getUsername());
        return toAjax(lotteryActivityService.insertLotteryActivity(activity));
    }

    @ApiOperation("修改活动")
    @PreAuthorize("@ss.hasPermi('biz:lotteryActivity:edit')")
    @Log(title = "抽奖活动", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLotteryActivity activity)
    {
        activity.setUpdateBy(getUsername());
        return toAjax(lotteryActivityService.updateLotteryActivity(activity));
    }

    @ApiOperation("删除活动")
    @PreAuthorize("@ss.hasPermi('biz:lotteryActivity:remove')")
    @Log(title = "抽奖活动", businessType = BusinessType.DELETE)
    @DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds)
    {
        return toAjax(lotteryActivityService.deleteLotteryActivityByIds(activityIds));
    }

    @ApiOperation("保存奖品配置")
    @PreAuthorize("@ss.hasPermi('biz:lotteryPrize:edit')")
    @Log(title = "大转盘奖品", businessType = BusinessType.UPDATE)
    @PutMapping("/{activityId}/prizes")
    public AjaxResult savePrizes(@PathVariable Long activityId, @RequestBody List<BizLotteryPrize> prizes)
    {
        return toAjax(lotteryActivityService.savePrizes(activityId, prizes, getUsername()));
    }

    @ApiOperation("保存必中策略")
    @PreAuthorize("@ss.hasPermi('biz:lotteryStrategy:edit')")
    @Log(title = "大转盘必中策略", businessType = BusinessType.UPDATE)
    @PutMapping("/{activityId}/strategies")
    public AjaxResult saveStrategies(@PathVariable Long activityId, @RequestBody List<BizLotteryWinStrategy> strategies)
    {
        return toAjax(lotteryActivityService.saveStrategies(activityId, strategies, getUsername()));
    }
}

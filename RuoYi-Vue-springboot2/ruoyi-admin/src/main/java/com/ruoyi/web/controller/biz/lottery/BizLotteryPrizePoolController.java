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
import com.ruoyi.biz.domain.BizLotteryPrizePool;
import com.ruoyi.biz.service.IBizLotteryPrizePoolService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-抽奖奖品池")
@RestController
@RequestMapping("/biz/lotteryPrizePool")
public class BizLotteryPrizePoolController extends BaseController
{
    @Autowired
    private IBizLotteryPrizePoolService prizePoolService;

    @ApiOperation("奖品池列表")
    @PreAuthorize("@ss.hasPermi('biz:lotteryPrize:edit')")
    @GetMapping("/list")
    public TableDataInfo list(BizLotteryPrizePool query)
    {
        startPage();
        List<BizLotteryPrizePool> list = prizePoolService.selectPrizePoolList(query);
        return getDataTable(list);
    }

    @ApiOperation("启用中的奖品下拉")
    @PreAuthorize("@ss.hasPermi('biz:lotteryPrize:edit')")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(prizePoolService.selectEnabledPrizePoolOptions());
    }

    @ApiOperation("奖品池详情")
    @PreAuthorize("@ss.hasPermi('biz:lotteryPrize:edit')")
    @GetMapping("/{poolId}")
    public AjaxResult getInfo(@PathVariable Long poolId)
    {
        return success(prizePoolService.selectPrizePoolById(poolId));
    }

    @ApiOperation("新增奖品")
    @PreAuthorize("@ss.hasPermi('biz:lotteryPrize:edit')")
    @Log(title = "抽奖奖品池", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLotteryPrizePool pool)
    {
        pool.setCreateBy(getUsername());
        return toAjax(prizePoolService.insertPrizePool(pool));
    }

    @ApiOperation("修改奖品")
    @PreAuthorize("@ss.hasPermi('biz:lotteryPrize:edit')")
    @Log(title = "抽奖奖品池", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLotteryPrizePool pool)
    {
        pool.setUpdateBy(getUsername());
        return toAjax(prizePoolService.updatePrizePool(pool));
    }

    @ApiOperation("删除奖品")
    @PreAuthorize("@ss.hasPermi('biz:lotteryPrize:edit')")
    @Log(title = "抽奖奖品池", businessType = BusinessType.DELETE)
    @DeleteMapping("/{poolIds}")
    public AjaxResult remove(@PathVariable Long[] poolIds)
    {
        return toAjax(prizePoolService.deletePrizePoolByIds(poolIds));
    }
}

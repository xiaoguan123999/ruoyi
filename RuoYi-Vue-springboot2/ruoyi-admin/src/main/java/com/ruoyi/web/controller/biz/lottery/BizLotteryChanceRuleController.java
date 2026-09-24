package com.ruoyi.web.controller.biz.lottery;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.biz.domain.BizLotteryChanceRule;
import com.ruoyi.biz.service.IBizLotteryChanceService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-抽奖获次规则")
@RestController
@RequestMapping("/biz/lotteryChanceRule")
public class BizLotteryChanceRuleController extends BaseController
{
    @Autowired
    private IBizLotteryChanceService lotteryChanceService;

    @ApiOperation("获次规则列表")
    @PreAuthorize("@ss.hasPermi('biz:lotteryChanceRule:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLotteryChanceRule query)
    {
        startPage();
        List<BizLotteryChanceRule> list = lotteryChanceService.selectChanceRuleList(query);
        return getDataTable(list);
    }

    @ApiOperation("获次规则详情")
    @PreAuthorize("@ss.hasPermi('biz:lotteryChanceRule:query')")
    @GetMapping("/{ruleId}")
    public AjaxResult getInfo(@PathVariable Long ruleId)
    {
        return success(lotteryChanceService.selectChanceRuleById(ruleId));
    }

    @ApiOperation("新增获次规则")
    @PreAuthorize("@ss.hasPermi('biz:lotteryChanceRule:add')")
    @Log(title = "抽奖获次规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLotteryChanceRule rule)
    {
        rule.setCreateBy(getUsername());
        return toAjax(lotteryChanceService.insertChanceRule(rule));
    }

    @ApiOperation("修改获次规则")
    @PreAuthorize("@ss.hasPermi('biz:lotteryChanceRule:edit')")
    @Log(title = "抽奖获次规则", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLotteryChanceRule rule)
    {
        rule.setUpdateBy(getUsername());
        return toAjax(lotteryChanceService.updateChanceRule(rule));
    }

    @ApiOperation("删除获次规则")
    @PreAuthorize("@ss.hasPermi('biz:lotteryChanceRule:remove')")
    @Log(title = "抽奖获次规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return toAjax(lotteryChanceService.deleteChanceRuleByIds(ruleIds));
    }
}

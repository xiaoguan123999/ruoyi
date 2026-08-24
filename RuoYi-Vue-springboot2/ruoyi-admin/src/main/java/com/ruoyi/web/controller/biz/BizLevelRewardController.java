package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.domain.BizLevelRewardGrant;
import com.ruoyi.biz.domain.BizLevelRewardPayBody;
import com.ruoyi.biz.domain.BizLevelRewardRule;
import com.ruoyi.biz.service.IBizLevelRewardService;
import com.ruoyi.biz.service.IBizLevelService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-等级奖励")
@RestController
@RequestMapping("/biz/levelReward")
public class BizLevelRewardController extends BaseController
{
    @Autowired
    private IBizLevelRewardService levelRewardService;

    @Autowired
    private IBizLevelService levelService;

    @Autowired
    private IBizMemberService memberService;

    @ApiOperation("全局规则")
    @PreAuthorize("@ss.hasPermi('biz:levelReward:query')")
    @GetMapping("/rule")
    public AjaxResult rule()
    {
        return success(levelRewardService.getRule());
    }

    @ApiOperation("保存全局规则")
    @PreAuthorize("@ss.hasPermi('biz:levelReward:edit')")
    @Log(title = "等级奖励规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rule")
    public AjaxResult saveRule(@RequestBody BizLevelRewardRule rule)
    {
        levelRewardService.saveRule(rule);
        return success();
    }

    @ApiOperation("等级奖励配置列表")
    @PreAuthorize("@ss.hasPermi('biz:levelReward:list')")
    @GetMapping("/level/list")
    public TableDataInfo levelList(BizLevel level)
    {
        startPage();
        List<BizLevel> list = levelService.selectLevelList(level);
        return getDataTable(list);
    }

    @ApiOperation("保存某等级奖励")
    @PreAuthorize("@ss.hasPermi('biz:levelReward:edit')")
    @Log(title = "等级奖励配置", businessType = BusinessType.UPDATE)
    @PutMapping("/level")
    public AjaxResult updateLevel(@RequestBody BizLevel level)
    {
        level.setUpdateBy(getUsername());
        return toAjax(levelRewardService.updateLevelReward(level));
    }

    @ApiOperation("立即核算全部会员")
    @PreAuthorize("@ss.hasPermi('biz:levelReward:edit')")
    @Log(title = "等级奖励核算", businessType = BusinessType.UPDATE)
    @PostMapping("/evaluate")
    public AjaxResult evaluate()
    {
        int count = memberService.refreshAllLevels();
        return success("已核算" + count + "名会员");
    }

    @ApiOperation("发放记录")
    @PreAuthorize("@ss.hasPermi('biz:levelReward:grant')")
    @GetMapping("/grant/list")
    public TableDataInfo grantList(BizLevelRewardGrant grant)
    {
        startPage();
        List<BizLevelRewardGrant> list = levelRewardService.selectGrantList(grant);
        return getDataTable(list);
    }

    @ApiOperation("确认发放")
    @PreAuthorize("@ss.hasPermi('biz:levelReward:pay')")
    @Log(title = "等级奖励发放", businessType = BusinessType.UPDATE)
    @PutMapping("/grant/pay/{grantId}")
    public AjaxResult pay(@PathVariable Long grantId, @RequestBody(required = false) BizLevelRewardPayBody body)
    {
        String remark = body == null ? null : body.getRemark();
        levelRewardService.payGrant(grantId, getUsername(), remark);
        return success();
    }

    @ApiOperation("拒绝发放")
    @PreAuthorize("@ss.hasPermi('biz:levelReward:reject')")
    @Log(title = "等级奖励拒绝", businessType = BusinessType.UPDATE)
    @PutMapping("/grant/reject/{grantId}")
    public AjaxResult reject(@PathVariable Long grantId, @RequestBody(required = false) BizLevelRewardPayBody body)
    {
        String remark = body == null ? null : body.getRemark();
        levelRewardService.rejectGrant(grantId, getUsername(), remark);
        return success();
    }

    @ApiOperation("永久档额外发放")
    @PreAuthorize("@ss.hasPermi('biz:levelReward:pay')")
    @Log(title = "等级奖励额外发放", businessType = BusinessType.INSERT)
    @PostMapping("/grant/extraPay")
    public AjaxResult extraPay(@RequestBody BizLevelRewardPayBody body)
    {
        levelRewardService.extraPay(body, getUsername());
        return success();
    }
}

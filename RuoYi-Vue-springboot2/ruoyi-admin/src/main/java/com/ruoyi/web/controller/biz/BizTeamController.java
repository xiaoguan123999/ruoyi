package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppTeamData;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-团队")
@RestController
@RequestMapping("/biz/team")
public class BizTeamController extends BaseController
{
    @Autowired
    private IBizMemberService memberService;

    @ApiOperation("团队成员列表")
    @PreAuthorize("@ss.hasPermi('biz:team:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizMember member)
    {
        startPage();
        List<BizMember> list = memberService.selectMemberList(member);
        return getDataTable(list);
    }

    @ApiOperation("团队汇总")
    @PreAuthorize("@ss.hasPermi('biz:team:list')")
    @GetMapping("/summary/{memberId}")
    public AjaxResult summary(@PathVariable Long memberId)
    {
        BizMember member = memberService.selectMemberById(memberId);
        if (member == null)
        {
            return error("会员不存在");
        }
        AppTeamData team = memberService.getAppTeam(memberId);
        AjaxResult ajax = success();
        ajax.put("member", member);
        ajax.put("summary", team == null ? null : team.getSummary());
        return ajax;
    }
}

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
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-会员")
@RestController
@RequestMapping("/biz/member")
public class BizMemberController extends BaseController
{
    @Autowired
    private IBizMemberService memberService;

    @ApiOperation("会员列表")
    @PreAuthorize("@ss.hasPermi('biz:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizMember member)
    {
        startPage();
        List<BizMember> list = memberService.selectMemberList(member);
        return getDataTable(list);
    }

    @ApiOperation("会员详情")
    @PreAuthorize("@ss.hasPermi('biz:member:query')")
    @GetMapping("/{memberId}")
    public AjaxResult getInfo(@PathVariable Long memberId)
    {
        return success(memberService.selectMemberById(memberId));
    }

    @ApiOperation("新增顶级会员")
    @PreAuthorize("@ss.hasPermi('biz:member:add')")
    @Log(title = "会员管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizMember member)
    {
        BizMember created = memberService.createRootMember(member.getPhone(), member.getPassword());
        AjaxResult ajax = success(created);
        ajax.put("memberId", created.getMemberId());
        ajax.put("inviteCode", created.getInviteCode());
        return ajax;
    }

    @ApiOperation("修改会员")
    @PreAuthorize("@ss.hasPermi('biz:member:edit')")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizMember member)
    {
        member.setUpdateBy(getUsername());
        memberService.updateMember(member);
        return success();
    }

    @ApiOperation("会员团队")
    @PreAuthorize("@ss.hasPermi('biz:team:list')")
    @GetMapping("/team/{memberId}")
    public AjaxResult team(@PathVariable Long memberId, Integer teamLevel)
    {
        return success(memberService.selectTeamMembers(memberId, teamLevel));
    }
}

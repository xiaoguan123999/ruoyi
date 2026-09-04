package com.ruoyi.web.controller.biz;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizGoogleConfig;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.service.IBizGoogleAuthService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-会员")
@RestController
@RequestMapping("/biz/member")
public class BizMemberController extends BaseController
{
    @Autowired
    private IBizMemberService memberService;

    @Autowired
    private IBizGoogleAuthService googleAuthService;

    @ApiOperation("会员列表，testFlag=1 仅测试账号，testFlag=0 仅正式账号")
    @PreAuthorize("@ss.hasPermi('biz:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizMember member)
    {
        startPage();
        List<BizMember> list = memberService.selectMemberList(member);
        return getDataTable(list);
    }

    @ApiOperation("导出会员，按当前筛选条件；传 memberIds 则只导出勾选行")
    @PreAuthorize("@ss.hasPermi('biz:member:list')")
    @Log(title = "会员导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizMember member)
    {
        List<BizMember> list = memberService.selectMemberList(member);
        for (int i = 0; i < list.size(); i++)
        {
            list.get(i).setApplyTime(list.get(i).getCreateTime());
        }
        ExcelUtil<BizMember> util = new ExcelUtil<BizMember>(BizMember.class);
        util.exportExcel(response, list, "会员");
    }

    @ApiOperation("App谷歌验证配置")
    @PreAuthorize("@ss.hasPermi('biz:member:list')")
    @GetMapping("/googleConfig")
    public AjaxResult googleConfig()
    {
        return success(googleAuthService.getAdminConfig());
    }

    @ApiOperation("保存App谷歌验证配置")
    @PreAuthorize("@ss.hasPermi('biz:member:edit')")
    @Log(title = "App谷歌验证", businessType = BusinessType.UPDATE)
    @PutMapping("/googleConfig")
    public AjaxResult saveGoogleConfig(@RequestBody BizGoogleConfig config)
    {
        googleAuthService.saveAdminConfig(config);
        return success();
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

    @ApiOperation("修改会员，可改手机号（不可与其他会员重复）、status、testFlag（0正式 1测试）等")
    @PreAuthorize("@ss.hasPermi('biz:member:edit')")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizMember member)
    {
        member.setUpdateBy(getUsername());
        memberService.updateMember(member);
        return success();
    }

    @ApiOperation("重置登录密码")
    @PreAuthorize("@ss.hasPermi('biz:member:resetPwd')")
    @Log(title = "重置登录密码", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody BizMember member)
    {
        memberService.resetLoginPassword(member.getMemberId(), member.getPassword());
        return success();
    }

    @ApiOperation("重置交易密码")
    @PreAuthorize("@ss.hasPermi('biz:member:resetPayPwd')")
    @Log(title = "重置交易密码", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPayPwd")
    public AjaxResult resetPayPwd(@RequestBody BizMember member)
    {
        memberService.resetPayPassword(member.getMemberId(), member.getPayPassword());
        return success();
    }

    @ApiOperation("重置谷歌验证")
    @PreAuthorize("@ss.hasPermi('biz:member:edit')")
    @Log(title = "重置谷歌验证", businessType = BusinessType.UPDATE)
    @PutMapping("/{memberId}/google/reset")
    public AjaxResult resetGoogle(@PathVariable Long memberId)
    {
        googleAuthService.reset(memberId);
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

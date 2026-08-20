package com.ruoyi.web.controller.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.AppKycBody;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.service.IBizLevelService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.AppSecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-会员")
@RestController
@RequestMapping("/app")
public class AppMemberController extends BaseController
{
    @Autowired
    private IBizMemberService memberService;

    @Autowired
    private IBizLevelService levelService;

    @Autowired
    private BizMemberMapper memberMapper;

    @ApiOperation("我的资料")
    @GetMapping("/profile")
    public AjaxResult profile()
    {
        return success(memberService.selectMemberById(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation("提交实名")
    @PostMapping("/kyc")
    public AjaxResult kyc(@RequestBody AppKycBody body)
    {
        memberService.submitKyc(AppSecurityUtils.getMemberId(), body);
        return success();
    }

    @ApiOperation("邀请信息")
    @GetMapping("/invite")
    public AjaxResult invite()
    {
        Long memberId = AppSecurityUtils.getMemberId();
        BizMember member = memberService.selectMemberById(memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", member.getInviteCode());
        data.put("inviteCount", memberMapper.countDirectMembers(memberId));
        data.put("reward", 0);
        return success(data);
    }

    @ApiOperation("我的团队")
    @GetMapping("/team")
    public AjaxResult team()
    {
        Long memberId = AppSecurityUtils.getMemberId();
        Map<String, Object> data = new HashMap<>();
        data.put("level1", memberService.selectTeamMembers(memberId, 1));
        data.put("level2", memberService.selectTeamMembers(memberId, 2));
        data.put("level3", memberService.selectTeamMembers(memberId, 3));
        return success(data);
    }

    @ApiOperation("会员等级")
    @GetMapping("/levels")
    public AjaxResult levels()
    {
        List<BizLevel> list = levelService.selectLevelList(new BizLevel());
        Map<String, Object> data = new HashMap<>();
        data.put("current", memberService.selectMemberById(AppSecurityUtils.getMemberId()));
        data.put("levels", list);
        return success(data);
    }
}

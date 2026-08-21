package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppInviteData;
import com.ruoyi.biz.api.AppInviteResult;
import com.ruoyi.biz.api.AppLevelsData;
import com.ruoyi.biz.api.AppLevelsResult;
import com.ruoyi.biz.api.AppOkResult;
import com.ruoyi.biz.api.AppProfileResult;
import com.ruoyi.biz.api.AppTeamData;
import com.ruoyi.biz.api.AppTeamResult;
import com.ruoyi.biz.domain.AppKycBody;
import com.ruoyi.biz.domain.AppPasswordBody;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.service.IBizLevelService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.common.core.controller.BaseController;
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

    @ApiOperation(value = "我的资料", notes = "返回 data 为会员资料。资产卡看 cnyAvailable / usdtAvailable / productIncome 等字段。")
    @GetMapping("/profile")
    public AppProfileResult profile()
    {
        return AppProfileResult.ok(memberService.selectMemberById(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation(value = "提交实名", notes = "校验姓名和18位身份证。App 侧身份证号不能与其他会员重复。校验通过即已实名，没有审核中。后台改资料允许身份证重复。")
    @PostMapping("/kyc")
    public AppOkResult kyc(@RequestBody AppKycBody body)
    {
        memberService.submitKyc(AppSecurityUtils.getMemberId(), body);
        return AppOkResult.ok();
    }

    @ApiOperation(value = "修改密码", notes = "需登录。校验原密码后写入新密码。成功没有 data。登录态仍有效，不必重新登录。")
    @PostMapping("/password")
    public AppOkResult changePassword(@RequestBody AppPasswordBody body)
    {
        if (body == null)
        {
            return AppOkResult.fail("请填写原密码和新密码");
        }
        memberService.changePassword(AppSecurityUtils.getMemberId(), body.getOldPassword(),
                body.getNewPassword(), body.getConfirmPassword());
        return AppOkResult.ok();
    }

    @ApiOperation(value = "修改密码", notes = "同 POST /app/password", hidden = true)
    @PutMapping("/password")
    public AppOkResult changePasswordPut(@RequestBody AppPasswordBody body)
    {
        return changePassword(body);
    }

    @ApiOperation(value = "邀请信息", notes = "inviteCode 给别人填；inviteCount 是直推人数；reward 当前固定 0。")
    @GetMapping("/invite")
    public AppInviteResult invite()
    {
        Long memberId = AppSecurityUtils.getMemberId();
        BizMember member = memberService.selectMemberById(memberId);
        AppInviteData data = new AppInviteData();
        data.setInviteCode(member.getInviteCode());
        data.setInviteCount(Integer.valueOf(memberMapper.countDirectMembers(memberId)));
        data.setReward(Integer.valueOf(0));
        return AppInviteResult.ok(data);
    }

    @ApiOperation(value = "我的团队", notes = "level1/2/3 为三级下线列表，元素同会员资料（不含密码）。")
    @GetMapping("/team")
    public AppTeamResult team()
    {
        Long memberId = AppSecurityUtils.getMemberId();
        AppTeamData data = new AppTeamData();
        data.setLevel1(memberService.selectTeamMembers(memberId, 1));
        data.setLevel2(memberService.selectTeamMembers(memberId, 2));
        data.setLevel3(memberService.selectTeamMembers(memberId, 3));
        return AppTeamResult.ok(data);
    }

    @ApiOperation(value = "会员等级", notes = "current 是我；levels 是全部等级配置。")
    @GetMapping("/levels")
    public AppLevelsResult levels()
    {
        List<BizLevel> list = levelService.selectLevelList(new BizLevel());
        AppLevelsData data = new AppLevelsData();
        data.setCurrent(memberService.selectMemberById(AppSecurityUtils.getMemberId()));
        data.setLevels(list);
        return AppLevelsResult.ok(data);
    }
}

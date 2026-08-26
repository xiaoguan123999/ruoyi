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
import com.ruoyi.biz.api.AppTeamResult;
import com.ruoyi.biz.domain.AppKycBody;
import com.ruoyi.biz.domain.AppPasswordBody;
import com.ruoyi.biz.domain.AppPayPasswordBody;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.service.IBizLevelRewardService;
import com.ruoyi.biz.service.IBizLevelService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.biz.service.IBizPromoService;
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
    private IBizLevelRewardService levelRewardService;

    @Autowired
    private IBizPromoService promoService;

    @Autowired
    private BizMemberMapper memberMapper;

    @ApiOperation(value = "我的资料", notes = "返回 data 为会员资料，含 cnyAvailable / usdtAvailable / productIncome 等资产字段")
    @GetMapping("/profile")
    public AppProfileResult profile()
    {
        return AppProfileResult.ok(memberService.selectMemberById(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation(value = "提交实名", notes = "校验姓名和18位身份证。App 侧身份证号不能与其他会员重复。校验通过即已实名")
    @PostMapping("/kyc")
    public AppOkResult kyc(@RequestBody AppKycBody body)
    {
        memberService.submitKyc(AppSecurityUtils.getMemberId(), body);
        return AppOkResult.ok();
    }

    @ApiOperation(value = "修改密码", notes = "需登录。校验原密码后写入新密码。成功没有 data。登录态仍有效")
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

    @ApiOperation(value = "设置或修改支付密码", notes = "未设置过只需 newPassword 或 payPassword；已设置需带 oldPassword。别名 /app/tradePassword")
    @PostMapping({"/payPassword", "/tradePassword"})
    public AppOkResult savePayPassword(@RequestBody AppPayPasswordBody body)
    {
        if (body == null)
        {
            return AppOkResult.fail("请设置支付密码");
        }
        memberService.savePayPassword(AppSecurityUtils.getMemberId(), body.getOldPassword(),
                body.getNewPassword(), body.getConfirmPassword());
        return AppOkResult.ok();
    }

    @ApiOperation(value = "设置或修改支付密码", notes = "同 POST /app/payPassword", hidden = true)
    @PutMapping({"/payPassword", "/tradePassword"})
    public AppOkResult savePayPasswordPut(@RequestBody AppPayPasswordBody body)
    {
        return savePayPassword(body);
    }

    @ApiOperation(value = "邀请信息", notes = "inviteCode 给别人填；inviteCount 为直推人数")
    @GetMapping("/invite")
    public AppInviteResult invite()
    {
        Long memberId = AppSecurityUtils.getMemberId();
        BizMember member = memberService.selectMemberById(memberId);
        AppInviteData data = new AppInviteData();
        data.setInviteCode(member.getInviteCode());
        data.setInviteCount(Integer.valueOf(memberMapper.countDirectMembers(memberId)));
        promoService.fillInvite(data);
        return AppInviteResult.ok(data);
    }

    @ApiOperation(value = "我的团队", notes = "summary 为 1-7 级汇总；members 的 key 为 1-7；同时返回 level1-level7 数组")
    @GetMapping("/team")
    public AppTeamResult team()
    {
        Long memberId = AppSecurityUtils.getMemberId();
        return AppTeamResult.ok(memberService.getAppTeam(memberId));
    }

    @ApiOperation(value = "会员等级", notes = "current 为我；levels 为全部等级配置")
    @GetMapping("/levels")
    public AppLevelsResult levels()
    {
        List<BizLevel> list = levelService.selectLevelList(new BizLevel());
        AppLevelsData data = new AppLevelsData();
        data.setCurrent(memberService.selectMemberById(AppSecurityUtils.getMemberId()));
        data.setLevels(list);
        com.ruoyi.biz.domain.BizLevelRewardRule rule = levelRewardService.getRule();
        data.setRuleText(rule.getRuleText());
        data.setHint(rule.getHint());
        data.setNote(rule.getHint());
        return AppLevelsResult.ok(data);
    }
}

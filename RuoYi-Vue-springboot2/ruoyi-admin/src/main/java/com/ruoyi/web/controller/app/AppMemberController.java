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

@Api(tags = "App-???")
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

    @ApiOperation(value = "???????", notes = "???? data ???????????????? cnyAvailable / usdtAvailable / productIncome ????????")
    @GetMapping("/profile")
    public AppProfileResult profile()
    {
        return AppProfileResult.ok(memberService.selectMemberById(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation(value = "?????", notes = "§µ????????18¦Ë???????App ????????????????????????????§µ??????????????")
    @PostMapping("/kyc")
    public AppOkResult kyc(@RequestBody AppKycBody body)
    {
        memberService.submitKyc(AppSecurityUtils.getMemberId(), body);
        return AppOkResult.ok();
    }

    @ApiOperation(value = "????????", notes = "??????§µ????????§Õ?????????????? data??????????§¹??")
    @PostMapping("/password")
    public AppOkResult changePassword(@RequestBody AppPasswordBody body)
    {
        if (body == null)
        {
            return AppOkResult.fail("????§Õ????????????");
        }
        memberService.changePassword(AppSecurityUtils.getMemberId(), body.getOldPassword(),
                body.getNewPassword(), body.getConfirmPassword());
        return AppOkResult.ok();
    }

    @ApiOperation(value = "????????", notes = "?? POST /app/password", hidden = true)
    @PutMapping("/password")
    public AppOkResult changePasswordPut(@RequestBody AppPasswordBody body)
    {
        return changePassword(body);
    }

    @ApiOperation(value = "???????????????", notes = "?????¨´???? newPassword ?? payPassword???????????? oldPassword?????? /app/tradePassword??")
    @PostMapping({"/payPassword", "/tradePassword"})
    public AppOkResult savePayPassword(@RequestBody AppPayPasswordBody body)
    {
        if (body == null)
        {
            return AppOkResult.fail("???????????????");
        }
        memberService.savePayPassword(AppSecurityUtils.getMemberId(), body.getOldPassword(),
                body.getNewPassword(), body.getConfirmPassword());
        return AppOkResult.ok();
    }

    @ApiOperation(value = "???????????????", notes = "?? POST /app/payPassword", hidden = true)
    @PutMapping({"/payPassword", "/tradePassword"})
    public AppOkResult savePayPasswordPut(@RequestBody AppPayPasswordBody body)
    {
        return savePayPassword(body);
    }

    @ApiOperation(value = "????????", notes = "inviteCode ????????inviteCount ???????????reward ?????? 0??")
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

    @ApiOperation(value = "??????", notes = "summary ?? 1-7 ???????members ?? key ?? 1-7???????? level1-level7 ??????")
    @GetMapping("/team")
    public AppTeamResult team()
    {
        Long memberId = AppSecurityUtils.getMemberId();
        return AppTeamResult.ok(memberService.getAppTeam(memberId));
    }

    @ApiOperation(value = "??????", notes = "current ?????levels ????????????????????????? App ?????????")
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

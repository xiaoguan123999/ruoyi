package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.api.AppTeamData;
import com.ruoyi.biz.api.AppTeamLevelStats;
import com.ruoyi.biz.api.AppTeamMemberItem;
import com.ruoyi.biz.api.AppTeamSummary;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.AppKycBody;
import com.ruoyi.biz.domain.AppRegisterBody;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.service.IBizLevelRewardService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.biz.util.KycUtils;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizMemberServiceImpl implements IBizMemberService
{
    @Autowired
    private BizMemberMapper memberMapper;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizLevelRewardService levelRewardService;

    @Override
    public BizMember selectMemberById(Long memberId)
    {
        BizMember member = memberMapper.selectMemberById(memberId);
        walletService.fillAssetSummary(member);
        return member;
    }

    @Override
    public BizMember selectMemberByPhone(String phone)
    {
        BizMember member = memberMapper.selectMemberByPhone(phone);
        walletService.fillAssetSummary(member);
        return member;
    }

    @Override
    public List<BizMember> selectMemberList(BizMember member)
    {
        return memberMapper.selectMemberList(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizMember register(AppRegisterBody body)
    {
        if (body == null || StringUtils.isEmpty(body.getPhone()) || StringUtils.isEmpty(body.getPassword()))
        {
            throw new ServiceException("手机号和密码不能为空");
        }
        if (memberMapper.selectMemberByPhone(body.getPhone()) != null)
        {
            throw new ServiceException("手机号已注册");
        }
        BizMember parent = null;
        if (StringUtils.isNotEmpty(body.getInviteCode()))
        {
            parent = memberMapper.selectMemberByInviteCode(body.getInviteCode());
            if (parent == null)
            {
                parent = memberMapper.selectMemberById(parseLong(body.getInviteCode()));
            }
            if (parent == null)
            {
                throw new ServiceException("邀请码无效");
            }
            if (BizConstants.STATUS_DISABLE.equals(parent.getStatus()))
            {
                throw new ServiceException("邀请人已停用");
            }
        }
        BizMember member = new BizMember();
        member.setPhone(body.getPhone());
        member.setPassword(SecurityUtils.encryptPassword(body.getPassword()));
        if (StringUtils.isNotEmpty(body.getPayPassword()))
        {
            validatePayPasswordPlain(body.getPayPassword());
            member.setPayPassword(SecurityUtils.encryptPassword(body.getPayPassword()));
        }
        else
        {
            member.setPayPassword("");
        }
        member.setInviteCode(nextInviteCode());
        member.setKycStatus(BizConstants.KYC_NONE);
        member.setLevelId(1L);
        member.setStatus(BizConstants.STATUS_OK);
        member.setRealName("");
        member.setIdCard("");
        if (parent != null)
        {
            member.setParentId(parent.getMemberId());
            member.setAncestors(parent.getAncestors() + "," + parent.getMemberId());
        }
        else
        {
            member.setAncestors("0");
        }
        memberMapper.insertMember(member);
        walletService.initWallets(member.getMemberId());
        return selectMemberById(member.getMemberId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizMember createRootMember(String phone, String password)
    {
        AppRegisterBody body = new AppRegisterBody();
        body.setPhone(phone);
        body.setPassword(password);
        return register(body);
    }

    @Override
    public void updateMember(BizMember member)
    {
        if (member.getMemberId() == null)
        {
            throw new ServiceException("会员ID不能为空");
        }
        if (StringUtils.isNotEmpty(member.getPassword()))
        {
            member.setPassword(SecurityUtils.encryptPassword(member.getPassword()));
        }
        memberMapper.updateMember(member);
        refreshLevel(member.getMemberId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitKyc(Long memberId, AppKycBody body)
    {
        if (body == null)
        {
            throw new ServiceException("请填写姓名和身份证号");
        }
        String realName = KycUtils.normalizeName(body.getRealName());
        String idCard = KycUtils.normalizeIdCard(body.getIdCard());
        if (StringUtils.isEmpty(realName) || StringUtils.isEmpty(idCard))
        {
            throw new ServiceException("请填写姓名和身份证号");
        }
        if (!KycUtils.isValidName(realName))
        {
            throw new ServiceException("姓名须为2-20个中文，可含间隔号·");
        }
        if (!KycUtils.isValidIdCard(idCard))
        {
            throw new ServiceException("身份证号不正确");
        }
        BizMember exist = memberMapper.selectMemberById(memberId);
        if (exist == null)
        {
            throw new ServiceException("会员不存在");
        }
        if (BizConstants.KYC_DONE.equals(exist.getKycStatus()))
        {
            throw new ServiceException("已实名，不能重复提交");
        }
        if (memberMapper.countByIdCard(idCard, memberId) > 0)
        {
            throw new ServiceException("该身份证号已被使用");
        }
        BizMember update = new BizMember();
        update.setMemberId(memberId);
        update.setRealName(realName);
        update.setIdCard(idCard);
        update.setKycStatus(BizConstants.KYC_DONE);
        memberMapper.updateMember(update);
        refreshLevel(memberId);
        if (exist.getParentId() != null)
        {
            refreshLevel(exist.getParentId());
        }
    }

    @Override
    public void changePassword(Long memberId, String oldPassword, String newPassword, String confirmPassword)
    {
        if (StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(newPassword))
        {
            throw new ServiceException("请填写原密码和新密码");
        }
        if (StringUtils.isEmpty(confirmPassword) || !newPassword.equals(confirmPassword))
        {
            throw new ServiceException("两次密码不一致");
        }
        if (newPassword.length() < UserConstants.PASSWORD_MIN_LENGTH
                || newPassword.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("新密码长度必须为" + UserConstants.PASSWORD_MIN_LENGTH + "-"
                    + UserConstants.PASSWORD_MAX_LENGTH + "位");
        }
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("会员不存在");
        }
        if (!SecurityUtils.matchesPassword(oldPassword, member.getPassword()))
        {
            throw new ServiceException("原密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, member.getPassword()))
        {
            throw new ServiceException("新密码不能与原密码相同");
        }
        BizMember update = new BizMember();
        update.setMemberId(memberId);
        update.setPassword(SecurityUtils.encryptPassword(newPassword));
        memberMapper.updateMember(update);
    }

    @Override
    public void savePayPassword(Long memberId, String oldPayPassword, String newPayPassword, String confirmPassword)
    {
        validatePayPasswordPlain(newPayPassword);
        if (StringUtils.isNotEmpty(confirmPassword) && !newPayPassword.equals(confirmPassword))
        {
            throw new ServiceException("两次密码不一致");
        }
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("会员不存在");
        }
        boolean alreadySet = StringUtils.isNotEmpty(member.getPayPassword());
        if (alreadySet)
        {
            if (StringUtils.isEmpty(oldPayPassword))
            {
                throw new ServiceException("请输入原支付密码");
            }
            if (!SecurityUtils.matchesPassword(oldPayPassword, member.getPayPassword()))
            {
                throw new ServiceException("原支付密码错误");
            }
            if (SecurityUtils.matchesPassword(newPayPassword, member.getPayPassword()))
            {
                throw new ServiceException("新支付密码不能与原密码相同");
            }
        }
        memberMapper.updatePayPassword(memberId, SecurityUtils.encryptPassword(newPayPassword));
    }

    @Override
    public void assertPayPassword(Long memberId, String payPassword)
    {
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("会员不存在");
        }
        if (StringUtils.isEmpty(member.getPayPassword()))
        {
            throw new ServiceException("请先设置支付密码");
        }
        if (StringUtils.isEmpty(payPassword))
        {
            throw new ServiceException("请输入支付密码");
        }
        if (!SecurityUtils.matchesPassword(payPassword, member.getPayPassword()))
        {
            throw new ServiceException("支付密码错误");
        }
    }

    private void validatePayPasswordPlain(String payPassword)
    {
        if (StringUtils.isEmpty(payPassword))
        {
            throw new ServiceException("请设置支付密码");
        }
        if (payPassword.length() < BizConstants.PAY_PASSWORD_MIN_LENGTH
                || payPassword.length() > BizConstants.PAY_PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("支付密码长度必须为" + BizConstants.PAY_PASSWORD_MIN_LENGTH + "-"
                    + BizConstants.PAY_PASSWORD_MAX_LENGTH + "位");
        }
    }

    @Override
    public List<BizMember> selectTeamMembers(Long memberId, Integer teamLevel)
    {
        return memberMapper.selectTeamMembers(memberId, teamLevel, Integer.valueOf(viewerDepth(memberId)));
    }

    @Override
    public AppTeamData getAppTeam(Long memberId)
    {
        int depth = viewerDepth(memberId);
        Map<Integer, List<AppTeamMemberItem>> byLevel = new HashMap<Integer, List<AppTeamMemberItem>>();
        for (int i = 1; i <= BizConstants.TEAM_MAX_LEVEL; i++)
        {
            byLevel.put(Integer.valueOf(i), new ArrayList<AppTeamMemberItem>());
        }
        List<AppTeamMemberItem> all = memberMapper.selectAppTeamMembers(memberId, null, depth);
        for (int i = 0; i < all.size(); i++)
        {
            AppTeamMemberItem item = all.get(i);
            Integer lv = item.getTeamLevel();
            if (lv != null && byLevel.containsKey(lv))
            {
                if (StringUtils.isEmpty(item.getName()))
                {
                    item.setName(item.getRealName());
                }
                if (StringUtils.isEmpty(item.getName()))
                {
                    item.setName(item.getPhone());
                }
                byLevel.get(lv).add(item);
            }
        }
        Map<Integer, AppTeamLevelStats> stats = emptyStats();
        mergeRegister(stats, memberMapper.selectAppTeamRegisterStats(memberId, depth));
        mergeOrder(stats, memberMapper.selectAppTeamOrderStats(memberId, depth));
        mergeRecharge(stats, memberMapper.selectAppTeamRechargeStats(memberId, depth));

        AppTeamSummary summary = new AppTeamSummary();
        summary.setLevel1(stats.get(Integer.valueOf(1)));
        summary.setLevel2(stats.get(Integer.valueOf(2)));
        summary.setLevel3(stats.get(Integer.valueOf(3)));
        summary.setLevel4(stats.get(Integer.valueOf(4)));
        summary.setLevel5(stats.get(Integer.valueOf(5)));
        summary.setLevel6(stats.get(Integer.valueOf(6)));
        summary.setLevel7(stats.get(Integer.valueOf(7)));

        Map<String, List<AppTeamMemberItem>> members = new HashMap<String, List<AppTeamMemberItem>>();
        for (int i = 1; i <= BizConstants.TEAM_MAX_LEVEL; i++)
        {
            members.put(String.valueOf(i), byLevel.get(Integer.valueOf(i)));
        }
        AppTeamData data = new AppTeamData();
        data.setSummary(summary);
        data.setMembers(members);
        data.setLevel1(byLevel.get(Integer.valueOf(1)));
        data.setLevel2(byLevel.get(Integer.valueOf(2)));
        data.setLevel3(byLevel.get(Integer.valueOf(3)));
        data.setLevel4(byLevel.get(Integer.valueOf(4)));
        data.setLevel5(byLevel.get(Integer.valueOf(5)));
        data.setLevel6(byLevel.get(Integer.valueOf(6)));
        data.setLevel7(byLevel.get(Integer.valueOf(7)));
        data.setLevel1Members(byLevel.get(Integer.valueOf(1)));
        data.setMembers1(byLevel.get(Integer.valueOf(1)));
        return data;
    }

    private int viewerDepth(Long memberId)
    {
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null)
        {
            return 0;
        }
        return commaCount(member.getAncestors());
    }

    private int commaCount(String ancestors)
    {
        if (StringUtils.isEmpty(ancestors) || "0".equals(ancestors))
        {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < ancestors.length(); i++)
        {
            if (ancestors.charAt(i) == ',')
            {
                n++;
            }
        }
        return n;
    }

    private Map<Integer, AppTeamLevelStats> emptyStats()
    {
        Map<Integer, AppTeamLevelStats> map = new HashMap<Integer, AppTeamLevelStats>();
        for (int i = 1; i <= BizConstants.TEAM_MAX_LEVEL; i++)
        {
            AppTeamLevelStats s = new AppTeamLevelStats();
            s.setTeamLevel(Integer.valueOf(i));
            s.setRegister(Integer.valueOf(0));
            s.setActive(Integer.valueOf(0));
            s.setSubscribeUsd(BigDecimal.ZERO);
            s.setSubscribeUsdt(BigDecimal.ZERO);
            s.setSubscribeCny(BigDecimal.ZERO);
            s.setRechargeUsd(BigDecimal.ZERO);
            s.setRechargeUsdt(BigDecimal.ZERO);
            s.setRechargeCny(BigDecimal.ZERO);
            map.put(Integer.valueOf(i), s);
        }
        return map;
    }

    private void mergeRegister(Map<Integer, AppTeamLevelStats> target, List<AppTeamLevelStats> rows)
    {
        if (rows == null)
        {
            return;
        }
        for (int i = 0; i < rows.size(); i++)
        {
            AppTeamLevelStats row = rows.get(i);
            AppTeamLevelStats dest = target.get(row.getTeamLevel());
            if (dest == null)
            {
                continue;
            }
            dest.setRegister(nvl(row.getRegister()));
            dest.setActive(nvl(row.getActive()));
        }
    }

    private void mergeOrder(Map<Integer, AppTeamLevelStats> target, List<AppTeamLevelStats> rows)
    {
        if (rows == null)
        {
            return;
        }
        for (int i = 0; i < rows.size(); i++)
        {
            AppTeamLevelStats row = rows.get(i);
            AppTeamLevelStats dest = target.get(row.getTeamLevel());
            if (dest == null)
            {
                continue;
            }
            dest.setSubscribeUsd(nvl(row.getSubscribeUsd()));
            dest.setSubscribeUsdt(nvl(row.getSubscribeUsdt()));
            dest.setSubscribeCny(nvl(row.getSubscribeCny()));
        }
    }

    private void mergeRecharge(Map<Integer, AppTeamLevelStats> target, List<AppTeamLevelStats> rows)
    {
        if (rows == null)
        {
            return;
        }
        for (int i = 0; i < rows.size(); i++)
        {
            AppTeamLevelStats row = rows.get(i);
            AppTeamLevelStats dest = target.get(row.getTeamLevel());
            if (dest == null)
            {
                continue;
            }
            dest.setRechargeUsd(nvl(row.getRechargeUsd()));
            dest.setRechargeUsdt(nvl(row.getRechargeUsdt()));
            dest.setRechargeCny(nvl(row.getRechargeCny()));
        }
    }

    private int nvl(Integer v)
    {
        return v == null ? 0 : v.intValue();
    }

    private BigDecimal nvl(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }

    @Override
    public void refreshLevel(Long memberId)
    {
        if (memberId == null)
        {
            return;
        }
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null)
        {
            return;
        }
        BizLevel matched = levelRewardService.matchLevel(memberId);
        if (matched != null && (member.getLevelId() == null || !matched.getLevelId().equals(member.getLevelId())))
        {
            BizMember update = new BizMember();
            update.setMemberId(memberId);
            update.setLevelId(matched.getLevelId());
            memberMapper.updateMember(update);
        }
        levelRewardService.evaluate(memberId);
    }

    @Override
    public int refreshAllLevels()
    {
        List<BizMember> members = memberMapper.selectMemberList(new BizMember());
        int count = 0;
        for (int i = 0; i < members.size(); i++)
        {
            refreshLevel(members.get(i).getMemberId());
            count++;
        }
        return count;
    }

    private String nextInviteCode()
    {
        for (int i = 0; i < 30; i++)
        {
            String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000000, 10000000));
            if (memberMapper.selectMemberByInviteCode(code) == null)
            {
                return code;
            }
        }
        throw new ServiceException("邀请码生成失败，请重试");
    }

    private Long parseLong(String value)
    {
        try
        {
            return Long.valueOf(value);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}

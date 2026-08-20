package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.AppKycBody;
import com.ruoyi.biz.domain.AppRegisterBody;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.mapper.BizLevelMapper;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.mapper.BizRechargeMapper;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizMemberServiceImpl implements IBizMemberService
{
    @Autowired
    private BizMemberMapper memberMapper;

    @Autowired
    private BizLevelMapper levelMapper;

    @Autowired
    private BizRechargeMapper rechargeMapper;

    @Autowired
    private IBizWalletService walletService;

    @Override
    public BizMember selectMemberById(Long memberId)
    {
        return memberMapper.selectMemberById(memberId);
    }

    @Override
    public BizMember selectMemberByPhone(String phone)
    {
        return memberMapper.selectMemberByPhone(phone);
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
        return memberMapper.selectMemberById(member.getMemberId());
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
        if (body == null || StringUtils.isEmpty(body.getRealName()) || StringUtils.isEmpty(body.getIdCard()))
        {
            throw new ServiceException("姓名和身份证号不能为空");
        }
        BizMember exist = memberMapper.selectMemberById(memberId);
        if (exist == null)
        {
            throw new ServiceException("会员不存在");
        }
        BizMember update = new BizMember();
        update.setMemberId(memberId);
        update.setRealName(body.getRealName());
        update.setIdCard(body.getIdCard());
        update.setKycStatus(BizConstants.KYC_DONE);
        memberMapper.updateMember(update);
        refreshLevel(memberId);
        if (exist.getParentId() != null)
        {
            refreshLevel(exist.getParentId());
        }
    }

    @Override
    public List<BizMember> selectTeamMembers(Long memberId, Integer teamLevel)
    {
        return memberMapper.selectTeamMembers(memberId, teamLevel);
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
        int validMembers = memberMapper.countValidTeamMembers(memberId);
        BigDecimal rechargeCny = rechargeMapper.sumPassedRecharge(memberId, BizConstants.CURRENCY_CNY);
        if (rechargeCny == null)
        {
            rechargeCny = BigDecimal.ZERO;
        }
        BizLevel query = new BizLevel();
        query.setStatus(BizConstants.STATUS_OK);
        List<BizLevel> levels = levelMapper.selectLevelList(query);
        BizLevel matched = null;
        for (BizLevel level : levels)
        {
            int minMembers = level.getMinValidMembers() == null ? 0 : level.getMinValidMembers();
            BigDecimal minRecharge = level.getMinRechargeCny() == null ? BigDecimal.ZERO : level.getMinRechargeCny();
            if (validMembers >= minMembers && rechargeCny.compareTo(minRecharge) >= 0)
            {
                if (matched == null || (level.getSort() != null && matched.getSort() != null && level.getSort() > matched.getSort()))
                {
                    matched = level;
                }
            }
        }
        if (matched != null && (member.getLevelId() == null || !matched.getLevelId().equals(member.getLevelId())))
        {
            BizMember update = new BizMember();
            update.setMemberId(memberId);
            update.setLevelId(matched.getLevelId());
            memberMapper.updateMember(update);
        }
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

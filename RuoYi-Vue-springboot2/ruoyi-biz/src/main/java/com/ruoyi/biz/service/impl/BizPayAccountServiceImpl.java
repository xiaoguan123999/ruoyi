package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizPayAccount;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.mapper.BizPayAccountMapper;
import com.ruoyi.biz.service.IBizBlacklistService;
import com.ruoyi.biz.service.IBizPayAccountService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizPayAccountServiceImpl implements IBizPayAccountService
{
    private static final int MAX_PER_TYPE = 5;

    @Autowired
    private BizPayAccountMapper payAccountMapper;

    @Autowired
    private BizMemberMapper memberMapper;

    @Autowired
    private IBizBlacklistService blacklistService;

    @Override
    public BizPayAccount selectPayAccountById(Long accountId)
    {
        return payAccountMapper.selectPayAccountById(accountId);
    }

    @Override
    public List<BizPayAccount> selectPayAccountList(BizPayAccount query)
    {
        return payAccountMapper.selectPayAccountList(query);
    }

    @Override
    public List<BizPayAccount> selectMyAccounts(Long memberId, String accountType)
    {
        BizPayAccount query = new BizPayAccount();
        query.setMemberId(memberId);
        query.setAccountType(StringUtils.isEmpty(accountType) ? null : accountType.toUpperCase());
        query.setStatus(BizConstants.STATUS_OK);
        return payAccountMapper.selectPayAccountList(query);
    }

    @Override
    public int insertPayAccount(BizPayAccount account)
    {
        resolveMember(account);
        fillDefaults(account);
        checkRequired(account);
        if (payAccountMapper.countByMemberType(account.getMemberId(), account.getAccountType()) == 0)
        {
            account.setIsDefault("1");
        }
        if ("1".equals(account.getIsDefault()))
        {
            payAccountMapper.clearDefault(account.getMemberId(), account.getAccountType());
        }
        return payAccountMapper.insertPayAccount(account);
    }

    @Override
    public int updatePayAccount(BizPayAccount account)
    {
        if (account.getAccountId() == null)
        {
            throw new ServiceException("请选择账户");
        }
        BizPayAccount db = payAccountMapper.selectPayAccountById(account.getAccountId());
        if (db == null)
        {
            throw new ServiceException("账户不存在");
        }
        if (StringUtils.isEmpty(account.getAccountType()))
        {
            account.setAccountType(db.getAccountType());
        }
        else
        {
            account.setAccountType(account.getAccountType().toUpperCase());
        }
        if (account.getMemberId() == null)
        {
            account.setMemberId(db.getMemberId());
        }
        if ("1".equals(account.getIsDefault()))
        {
            payAccountMapper.clearDefault(account.getMemberId(), account.getAccountType());
        }
        return payAccountMapper.updatePayAccount(account);
    }

    @Override
    public int deletePayAccountByIds(Long[] accountIds)
    {
        return payAccountMapper.deletePayAccountByIds(accountIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizPayAccount saveMine(Long memberId, BizPayAccount account)
    {
        if (memberId == null)
        {
            throw new ServiceException("请先登录");
        }
        account.setMemberId(memberId);
        account.setStatus(BizConstants.STATUS_OK);
        fillDefaults(account);
        checkRequired(account);
        int exists = payAccountMapper.countByMemberType(memberId, account.getAccountType());
        if (account.getAccountId() == null && exists >= MAX_PER_TYPE)
        {
            throw new ServiceException("该类型最多保存" + MAX_PER_TYPE + "个账户");
        }
        if (exists == 0)
        {
            account.setIsDefault("1");
        }
        if ("1".equals(account.getIsDefault()))
        {
            payAccountMapper.clearDefault(memberId, account.getAccountType());
        }
        if (account.getAccountId() == null)
        {
            payAccountMapper.insertPayAccount(account);
        }
        else
        {
            BizPayAccount db = payAccountMapper.selectPayAccountById(account.getAccountId());
            if (db == null || !memberId.equals(db.getMemberId()))
            {
                throw new ServiceException("账户不存在");
            }
            payAccountMapper.updatePayAccount(account);
        }
        return payAccountMapper.selectPayAccountById(account.getAccountId());
    }

    @Override
    public void deleteMine(Long memberId, Long accountId)
    {
        BizPayAccount db = payAccountMapper.selectPayAccountById(accountId);
        if (db == null || !memberId.equals(db.getMemberId()))
        {
            throw new ServiceException("账户不存在");
        }
        payAccountMapper.deletePayAccountByIds(new Long[] { accountId });
    }

    private void fillDefaults(BizPayAccount account)
    {
        if (account.getAccountType() != null)
        {
            account.setAccountType(account.getAccountType().toUpperCase());
        }
        if (account.getAccountName() == null)
        {
            account.setAccountName("");
        }
        if (account.getBankName() == null)
        {
            account.setBankName("");
        }
        if (account.getNetwork() == null)
        {
            account.setNetwork("");
        }
        if (StringUtils.isEmpty(account.getIsDefault()))
        {
            account.setIsDefault("0");
        }
        if (StringUtils.isEmpty(account.getStatus()))
        {
            account.setStatus(BizConstants.STATUS_OK);
        }
        if (BizConstants.CURRENCY_USDT.equals(account.getAccountType()) && StringUtils.isEmpty(account.getNetwork()))
        {
            account.setNetwork("TRC20");
        }
    }

    private void checkRequired(BizPayAccount account)
    {
        String type = account.getAccountType();
        if (!BizConstants.CURRENCY_USDT.equals(type) && !BizConstants.PAY_BANK.equals(type)
                && !BizConstants.PAY_ALIPAY.equals(type))
        {
            throw new ServiceException("账户类型只能是 USDT、BANK 或 ALIPAY");
        }
        if (StringUtils.isEmpty(account.getAccountNo()))
        {
            if (BizConstants.CURRENCY_USDT.equals(type))
            {
                throw new ServiceException("请填写USDT地址");
            }
            if (BizConstants.PAY_BANK.equals(type))
            {
                throw new ServiceException("请填写银行卡号");
            }
            throw new ServiceException("请填写支付宝账号");
        }
        if (BizConstants.PAY_BANK.equals(type) && StringUtils.isEmpty(account.getBankName()))
        {
            throw new ServiceException("请填写银行名称");
        }
        if (BizConstants.PAY_BANK.equals(type))
        {
            String phone = account.getPhone();
            if (StringUtils.isEmpty(phone) && account.getMemberId() != null)
            {
                BizMember member = memberMapper.selectMemberById(account.getMemberId());
                if (member != null)
                {
                    phone = member.getPhone();
                }
            }
            blacklistService.assertBankCard(account.getAccountNo(), account.getMemberId(), phone);
        }
    }

    private void resolveMember(BizPayAccount account)
    {
        if (account.getMemberId() == null && StringUtils.isNotEmpty(account.getPhone()))
        {
            BizMember member = memberMapper.selectMemberByPhone(account.getPhone());
            if (member == null)
            {
                throw new ServiceException("会员不存在");
            }
            account.setMemberId(member.getMemberId());
        }
        if (account.getMemberId() == null)
        {
            throw new ServiceException("请填写会员ID或手机号");
        }
    }
}

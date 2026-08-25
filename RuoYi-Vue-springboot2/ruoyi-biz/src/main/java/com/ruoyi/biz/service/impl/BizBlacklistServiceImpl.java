package com.ruoyi.biz.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizBlacklist;
import com.ruoyi.biz.domain.BizBlacklistLog;
import com.ruoyi.biz.mapper.BizBlacklistLogMapper;
import com.ruoyi.biz.mapper.BizBlacklistMapper;
import com.ruoyi.biz.service.IBizBlacklistService;
import com.ruoyi.biz.util.KycUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizBlacklistServiceImpl implements IBizBlacklistService
{
    private static final Logger log = LoggerFactory.getLogger(BizBlacklistServiceImpl.class);

    @Autowired
    private BizBlacklistMapper blacklistMapper;

    @Autowired
    private BizBlacklistLogMapper logMapper;

    @Autowired
    private BizBlacklistLogRecorder logRecorder;

    @Override
    public BizBlacklist selectBlacklistById(Long blacklistId)
    {
        return blacklistMapper.selectBlacklistById(blacklistId);
    }

    @Override
    public List<BizBlacklist> selectBlacklistList(BizBlacklist query)
    {
        return blacklistMapper.selectBlacklistList(query);
    }

    @Override
    public int insertBlacklist(BizBlacklist row)
    {
        normalize(row);
        checkRequired(row);
        return blacklistMapper.insertBlacklist(row);
    }

    @Override
    public int updateBlacklist(BizBlacklist row)
    {
        normalize(row);
        checkRequired(row);
        return blacklistMapper.updateBlacklist(row);
    }

    @Override
    public int deleteBlacklistByIds(Long[] blacklistIds)
    {
        return blacklistMapper.deleteBlacklistByIds(blacklistIds);
    }

    @Override
    public List<BizBlacklistLog> selectLogList(BizBlacklistLog query)
    {
        return logMapper.selectLogList(query);
    }

    @Override
    public int deleteLogByIds(Long[] logIds)
    {
        return logMapper.deleteLogByIds(logIds);
    }

    @Override
    public void assertPhone(String phone, String action, Long memberId)
    {
        String value = normalizePhone(phone);
        if (StringUtils.isEmpty(value))
        {
            return;
        }
        BizBlacklist hit = blacklistMapper.selectEnabledByPhone(value);
        if (hit == null)
        {
            return;
        }
        reject(hit, action, BizConstants.BLACKLIST_HIT_PHONE, value, memberId, value, nvl(hit.getRealName()),
                messageFor(action));
    }

    @Override
    public void assertIdCard(String idCard, Long memberId, String phone, String realName)
    {
        String value = KycUtils.normalizeIdCard(idCard);
        if (StringUtils.isEmpty(value))
        {
            return;
        }
        BizBlacklist hit = blacklistMapper.selectEnabledByIdCard(value);
        if (hit == null)
        {
            return;
        }
        reject(hit, BizConstants.BLACKLIST_KYC, BizConstants.BLACKLIST_HIT_ID_CARD, value, memberId,
                normalizePhone(phone), nvl(realName), messageFor(BizConstants.BLACKLIST_KYC));
    }

    @Override
    public void assertBankCard(String bankCard, Long memberId, String phone)
    {
        String value = normalizeBankCard(bankCard);
        if (StringUtils.isEmpty(value))
        {
            return;
        }
        BizBlacklist hit = blacklistMapper.selectEnabledByBankCard(value);
        if (hit == null)
        {
            return;
        }
        reject(hit, BizConstants.BLACKLIST_BANK, BizConstants.BLACKLIST_HIT_BANK_CARD, value, memberId,
                normalizePhone(phone), nvl(hit.getRealName()), messageFor(BizConstants.BLACKLIST_BANK));
    }

    private void reject(BizBlacklist hit, String action, String hitType, String hitValue, Long memberId,
            String phone, String realName, String message)
    {
        try
        {
            BizBlacklistLog row = new BizBlacklistLog();
            row.setBlacklistId(hit.getBlacklistId());
            row.setAction(action);
            row.setHitType(hitType);
            row.setHitValue(hitValue);
            row.setMemberId(memberId);
            row.setPhone(nvl(phone));
            row.setRealName(nvl(realName));
            row.setRemark(message);
            logRecorder.record(row);
        }
        catch (Exception e)
        {
            log.warn("save blacklist log failed, action={}, hit={}", action, hitValue, e);
        }
        throw new ServiceException(message);
    }

    private void normalize(BizBlacklist row)
    {
        row.setRealName(nvl(row.getRealName()).trim());
        row.setPhone(normalizePhone(row.getPhone()));
        row.setIdCard(KycUtils.normalizeIdCard(row.getIdCard()));
        row.setBankCard(normalizeBankCard(row.getBankCard()));
        if (StringUtils.isEmpty(row.getStatus()))
        {
            row.setStatus(BizConstants.STATUS_OK);
        }
        if (row.getRemark() == null)
        {
            row.setRemark("");
        }
    }

    private void checkRequired(BizBlacklist row)
    {
        if (StringUtils.isEmpty(row.getPhone()) && StringUtils.isEmpty(row.getIdCard())
                && StringUtils.isEmpty(row.getBankCard()))
        {
            throw new ServiceException("请至少填写手机号、身份证号或银行卡号");
        }
    }

    private String messageFor(String action)
    {
        if (BizConstants.BLACKLIST_LOGIN.equals(action))
        {
            return "该手机号已被限制登录";
        }
        if (BizConstants.BLACKLIST_REGISTER.equals(action))
        {
            return "该手机号无法注册";
        }
        if (BizConstants.BLACKLIST_KYC.equals(action))
        {
            return "该身份证号无法完成实名认证";
        }
        if (BizConstants.BLACKLIST_BANK.equals(action))
        {
            return "该银行卡无法绑定";
        }
        return "操作已被限制";
    }

    private String normalizePhone(String phone)
    {
        return phone == null ? "" : phone.trim();
    }

    private String normalizeBankCard(String bankCard)
    {
        if (bankCard == null)
        {
            return "";
        }
        return bankCard.trim().replace(" ", "").replace("-", "");
    }

    private String nvl(String value)
    {
        return value == null ? "" : value;
    }
}

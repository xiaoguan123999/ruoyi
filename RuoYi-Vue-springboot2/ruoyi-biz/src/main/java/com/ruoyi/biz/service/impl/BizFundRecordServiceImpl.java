package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.api.AppFundRecordItem;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.mapper.BizFundRecordMapper;
import com.ruoyi.biz.service.IBizFundRecordService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizFundRecordServiceImpl implements IBizFundRecordService
{
    @Autowired
    private BizFundRecordMapper fundRecordMapper;

    @Override
    public List<AppFundRecordItem> selectAppFundRecords(Long memberId, String currency, String bizType, String status)
    {
        boolean includeRecharge = true;
        boolean includeWithdraw = true;
        String type = bizType == null ? "" : bizType.trim();
        if (StringUtils.isNotEmpty(type))
        {
            String key = type.toUpperCase();
            if ("RECHARGE".equals(key) || "CZ".equals(key) || "充值".equals(type))
            {
                includeRecharge = true;
                includeWithdraw = false;
            }
            else if ("WITHDRAW".equals(key) || "WD".equals(key) || "提现".equals(type))
            {
                includeRecharge = false;
                includeWithdraw = true;
            }
            else if ("ALL".equals(key))
            {
                includeRecharge = true;
                includeWithdraw = true;
            }
            else
            {
                throw new ServiceException("类型只能是 RECHARGE 或 WITHDRAW");
            }
        }
        String statusCode = normalizeStatus(status);
        List<AppFundRecordItem> rows = fundRecordMapper.selectAppFundRecords(memberId, currency, statusCode,
                includeRecharge, includeWithdraw);
        for (int i = 0; i < rows.size(); i++)
        {
            fillLabels(rows.get(i));
        }
        return rows;
    }

    private String normalizeStatus(String status)
    {
        if (StringUtils.isEmpty(status))
        {
            return null;
        }
        String key = status.trim().toUpperCase();
        if ("0".equals(key) || "PENDING".equals(key) || "WAIT".equals(key) || "待审".equals(status.trim())
                || "待打款".equals(status.trim()) || "待处理".equals(status.trim()))
        {
            return BizConstants.AUDIT_PENDING;
        }
        if ("1".equals(key) || "PASS".equals(key) || "SUCCESS".equals(key) || "通过".equals(status.trim())
                || "已通过".equals(status.trim()) || "已打款".equals(status.trim()) || "成功".equals(status.trim()))
        {
            return BizConstants.AUDIT_PASS;
        }
        if ("2".equals(key) || "REJECT".equals(key) || "FAIL".equals(key) || "拒绝".equals(status.trim())
                || "已拒绝".equals(status.trim()))
        {
            return BizConstants.AUDIT_REJECT;
        }
        throw new ServiceException("状态只能是 0待处理、1成功、2拒绝");
    }

    private void fillLabels(AppFundRecordItem item)
    {
        boolean withdraw = "WITHDRAW".equals(item.getBizType());
        String typeLabel = withdraw ? "提现" : "充值";
        item.setBizType(withdraw ? "WITHDRAW" : "RECHARGE");
        item.setBizTypeLabel(typeLabel);
        item.setTypeLabel(typeLabel);
        String status = item.getStatus();
        String statusLabel;
        if (BizConstants.AUDIT_PENDING.equals(status))
        {
            statusLabel = withdraw ? "待打款" : "待审";
        }
        else if (BizConstants.AUDIT_PASS.equals(status))
        {
            statusLabel = withdraw ? "已打款" : "已通过";
        }
        else if (BizConstants.AUDIT_REJECT.equals(status))
        {
            statusLabel = "已拒绝";
        }
        else
        {
            statusLabel = status == null ? "" : status;
        }
        item.setStatusLabel(statusLabel);
        String title = typeLabel + statusLabel;
        item.setTitle(title);
        item.setName(title);
        String payMethod = item.getPayMethod();
        if ("ALIPAY".equals(payMethod))
        {
            item.setPayMethodLabel("支付宝");
        }
        else if ("USDT".equals(payMethod))
        {
            item.setPayMethodLabel("USDT");
        }
        else if ("BANK".equals(payMethod))
        {
            item.setPayMethodLabel("银行卡");
        }
        else
        {
            item.setPayMethodLabel(payMethod);
        }
    }
}

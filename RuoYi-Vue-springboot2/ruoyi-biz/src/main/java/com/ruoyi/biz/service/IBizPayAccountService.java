package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizPayAccount;

public interface IBizPayAccountService
{
    BizPayAccount selectPayAccountById(Long accountId);

    List<BizPayAccount> selectPayAccountList(BizPayAccount query);

    List<BizPayAccount> selectMyAccounts(Long memberId, String accountType);

    int insertPayAccount(BizPayAccount account);

    int updatePayAccount(BizPayAccount account);

    int deletePayAccountByIds(Long[] accountIds);

    BizPayAccount saveMine(Long memberId, BizPayAccount account);

    void deleteMine(Long memberId, Long accountId);
}

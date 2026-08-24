package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizPayAccount;

public interface BizPayAccountMapper
{
    BizPayAccount selectPayAccountById(Long accountId);

    List<BizPayAccount> selectPayAccountList(BizPayAccount query);

    int countByMemberType(@Param("memberId") Long memberId, @Param("accountType") String accountType);

    int insertPayAccount(BizPayAccount account);

    int updatePayAccount(BizPayAccount account);

    int clearDefault(@Param("memberId") Long memberId, @Param("accountType") String accountType);

    int deletePayAccountByIds(Long[] accountIds);
}

package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizBlacklist;
import com.ruoyi.biz.domain.BizBlacklistLog;

public interface IBizBlacklistService
{
    BizBlacklist selectBlacklistById(Long blacklistId);

    List<BizBlacklist> selectBlacklistList(BizBlacklist query);

    int insertBlacklist(BizBlacklist row);

    int updateBlacklist(BizBlacklist row);

    int deleteBlacklistByIds(Long[] blacklistIds);

    List<BizBlacklistLog> selectLogList(BizBlacklistLog query);

    int deleteLogByIds(Long[] logIds);

    void assertPhone(String phone, String action, Long memberId);

    void assertIdCard(String idCard, Long memberId, String phone, String realName);

    void assertBankCard(String bankCard, Long memberId, String phone);
}

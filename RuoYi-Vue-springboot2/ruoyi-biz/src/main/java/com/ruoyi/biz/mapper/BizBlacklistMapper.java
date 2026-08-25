package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizBlacklist;

public interface BizBlacklistMapper
{
    BizBlacklist selectBlacklistById(Long blacklistId);

    List<BizBlacklist> selectBlacklistList(BizBlacklist query);

    BizBlacklist selectEnabledByPhone(@Param("phone") String phone);

    BizBlacklist selectEnabledByIdCard(@Param("idCard") String idCard);

    BizBlacklist selectEnabledByBankCard(@Param("bankCard") String bankCard);

    int insertBlacklist(BizBlacklist row);

    int updateBlacklist(BizBlacklist row);

    int deleteBlacklistByIds(Long[] blacklistIds);
}

package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizMemberLogininfor;

public interface IBizMemberLogininforService
{
    void record(String phone, Long memberId, String status, String message);

    List<BizMemberLogininfor> selectLogininforList(BizMemberLogininfor query);

    int deleteLogininforByIds(Long[] infoIds);

    void cleanLogininfor();
}

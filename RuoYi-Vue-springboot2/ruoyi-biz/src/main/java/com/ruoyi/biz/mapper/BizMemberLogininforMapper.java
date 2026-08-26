package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizMemberLogininfor;

public interface BizMemberLogininforMapper
{
    int insertLogininfor(BizMemberLogininfor row);

    List<BizMemberLogininfor> selectLogininforList(BizMemberLogininfor query);

    int deleteLogininforByIds(Long[] infoIds);

    int cleanLogininfor();
}

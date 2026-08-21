package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizAbout;

public interface BizAboutMapper
{
    BizAbout selectAboutById(Long aboutId);

    List<BizAbout> selectAboutList(BizAbout about);

    int insertAbout(BizAbout about);

    int updateAbout(BizAbout about);

    int deleteAboutByIds(Long[] aboutIds);
}

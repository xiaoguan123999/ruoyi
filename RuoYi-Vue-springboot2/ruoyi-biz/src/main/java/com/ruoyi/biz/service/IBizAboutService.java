package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizAbout;

public interface IBizAboutService
{
    BizAbout selectAboutById(Long aboutId);

    List<BizAbout> selectAboutList(BizAbout about);

    List<BizAbout> selectAppAboutList();

    int insertAbout(BizAbout about);

    int updateAbout(BizAbout about);

    int deleteAboutByIds(Long[] aboutIds);
}

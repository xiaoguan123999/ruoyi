package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizLevel;

public interface IBizLevelService
{
    BizLevel selectLevelById(Long levelId);

    List<BizLevel> selectLevelList(BizLevel level);

    int insertLevel(BizLevel level);

    int updateLevel(BizLevel level);

    int deleteLevelByIds(Long[] levelIds);
}

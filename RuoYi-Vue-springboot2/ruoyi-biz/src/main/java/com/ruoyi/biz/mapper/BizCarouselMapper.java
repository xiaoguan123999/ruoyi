package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizCarousel;

public interface BizCarouselMapper
{
    BizCarousel selectCarouselById(Long carouselId);

    List<BizCarousel> selectCarouselList(BizCarousel carousel);

    int insertCarousel(BizCarousel carousel);

    int updateCarousel(BizCarousel carousel);

    int deleteCarouselByIds(Long[] carouselIds);
}

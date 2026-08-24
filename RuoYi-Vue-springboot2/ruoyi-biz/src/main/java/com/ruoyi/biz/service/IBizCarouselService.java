package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizCarousel;

public interface IBizCarouselService
{
    BizCarousel selectCarouselById(Long carouselId);

    List<BizCarousel> selectCarouselList(BizCarousel carousel);

    List<BizCarousel> selectAppCarouselList();

    int insertCarousel(BizCarousel carousel);

    int updateCarousel(BizCarousel carousel);

    int deleteCarouselByIds(Long[] carouselIds);
}

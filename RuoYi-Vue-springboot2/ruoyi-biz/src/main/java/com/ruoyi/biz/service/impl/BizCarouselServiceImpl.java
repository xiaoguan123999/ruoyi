package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizCarousel;
import com.ruoyi.biz.mapper.BizCarouselMapper;
import com.ruoyi.biz.service.IBizCarouselService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizCarouselServiceImpl implements IBizCarouselService
{
    @Autowired
    private BizCarouselMapper carouselMapper;

    @Override
    public BizCarousel selectCarouselById(Long carouselId)
    {
        return carouselMapper.selectCarouselById(carouselId);
    }

    @Override
    public List<BizCarousel> selectCarouselList(BizCarousel carousel)
    {
        return carouselMapper.selectCarouselList(carousel);
    }

    @Override
    public List<BizCarousel> selectAppCarouselList()
    {
        BizCarousel query = new BizCarousel();
        query.setStatus(BizConstants.STATUS_OK);
        return carouselMapper.selectCarouselList(query);
    }

    @Override
    public int insertCarousel(BizCarousel carousel)
    {
        fillDefaults(carousel);
        checkRequired(carousel);
        return carouselMapper.insertCarousel(carousel);
    }

    @Override
    public int updateCarousel(BizCarousel carousel)
    {
        fillDefaults(carousel);
        checkRequired(carousel);
        return carouselMapper.updateCarousel(carousel);
    }

    @Override
    public int deleteCarouselByIds(Long[] carouselIds)
    {
        return carouselMapper.deleteCarouselByIds(carouselIds);
    }

    private void fillDefaults(BizCarousel carousel)
    {
        if (StringUtils.isEmpty(carousel.getStatus()))
        {
            carousel.setStatus(BizConstants.STATUS_OK);
        }
        if (carousel.getTitle() == null)
        {
            carousel.setTitle("");
        }
        else
        {
            carousel.setTitle(carousel.getTitle().trim());
        }
        if (carousel.getCoverUrl() == null)
        {
            carousel.setCoverUrl("");
        }
        if (carousel.getSort() == null)
        {
            carousel.setSort(Integer.valueOf(0));
        }
        if (carousel.getVideoUrl() != null)
        {
            carousel.setVideoUrl(carousel.getVideoUrl().trim());
        }
    }

    private void checkRequired(BizCarousel carousel)
    {
        if (StringUtils.isEmpty(carousel.getVideoUrl()))
        {
            throw new ServiceException("请上传或填写视频地址");
        }
    }
}

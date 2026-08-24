package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("首页视频轮播列表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppCarouselListResult extends AppDataResult<java.util.List<AppCarouselItem>>
{
    public static AppCarouselListResult ok(java.util.List<AppCarouselItem> data)
    {
        return fillOk(new AppCarouselListResult(), data);
    }

    public static AppCarouselListResult fail(String message)
    {
        return fillFail(new AppCarouselListResult(), message);
    }
}

package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppLotteryInfoResult;
import com.ruoyi.biz.api.AppLotteryResult;
import com.ruoyi.biz.domain.BizLotteryRecord;
import com.ruoyi.biz.service.IBizLotteryDrawService;
import com.ruoyi.biz.service.IBizLotteryRecordService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.AppSecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-大转盘")
@RestController
@RequestMapping("/app/lottery")
public class AppLotteryController extends BaseController
{
    @Autowired
    private IBizLotteryDrawService lotteryDrawService;

    @Autowired
    private IBizLotteryRecordService lotteryRecordService;

    @ApiOperation("当前抽奖活动与可抽状态")
    @GetMapping("/current")
    public AppLotteryInfoResult current()
    {
        try
        {
            return AppLotteryInfoResult.ok(lotteryDrawService.getCurrentInfo(AppSecurityUtils.getMemberId()));
        }
        catch (ServiceException e)
        {
            return AppLotteryInfoResult.fail(e.getMessage());
        }
    }

    @ApiOperation("抽奖（频控/必中/双池库存/熔断）")
    @PostMapping("/draw")
    public AppLotteryResult draw()
    {
        try
        {
            return AppLotteryResult.ok(lotteryDrawService.draw(AppSecurityUtils.getMemberId()));
        }
        catch (ServiceException e)
        {
            return AppLotteryResult.fail(e.getMessage());
        }
    }

    @ApiOperation("我的抽奖记录")
    @GetMapping("/records")
    public TableDataInfo records()
    {
        startPage();
        BizLotteryRecord query = new BizLotteryRecord();
        query.setMemberId(AppSecurityUtils.getMemberId());
        return getDataTable(lotteryRecordService.selectRecordList(query));
    }
}

package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppCheckinResult;
import com.ruoyi.biz.api.AppOrderResult;
import com.ruoyi.biz.api.AppRechargeResult;
import com.ruoyi.biz.api.AppWalletResult;
import com.ruoyi.biz.api.AppWithdrawResult;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.AppAmountBody;
import com.ruoyi.biz.domain.BizCheckin;
import com.ruoyi.biz.domain.BizOrder;
import com.ruoyi.biz.domain.BizRecharge;
import com.ruoyi.biz.domain.BizWithdraw;
import com.ruoyi.biz.service.IBizCheckinService;
import com.ruoyi.biz.service.IBizOrderService;
import com.ruoyi.biz.service.IBizRechargeService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.biz.service.IBizWithdrawService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.AppSecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-交易")
@RestController
@RequestMapping("/app")
public class AppBizController extends BaseController
{
    @Autowired
    private IBizCheckinService checkinService;

    @Autowired
    private IBizOrderService orderService;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizRechargeService rechargeService;

    @Autowired
    private IBizWithdrawService withdrawService;

    @Autowired
    private ServerConfig serverConfig;

    @ApiOperation(value = "每日签到", notes = "成功入账 CNY。data 含 amount、streakDays、是否抽奖中奖。")
    @PostMapping("/checkin")
    public AppCheckinResult checkin()
    {
        return AppCheckinResult.ok(checkinService.checkin(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation(value = "签到记录", notes = "分页。rows 元素字段：checkinId、checkinDate、amount、currency。")
    @GetMapping("/checkin/list")
    public TableDataInfo checkinList()
    {
        startPage();
        BizCheckin query = new BizCheckin();
        query.setMemberId(AppSecurityUtils.getMemberId());
        return getDataTable(checkinService.selectCheckinList(query));
    }

    @ApiOperation(value = "签到状态与规则", notes = "checkedToday 表示今天是否已签。rule 是后台配置的金额和抽奖条件。")
    @GetMapping("/checkin/info")
    public AppCheckinResult checkinInfo()
    {
        return AppCheckinResult.ok(checkinService.getCheckinInfo(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation(value = "认购产品", notes = "body 必填 productId，currency 选 CNY 或 USDT。金额以产品配置为准，不信任客户端 amount。扣对应钱包，日返同币种。返回订单含所属系列。")
    @PostMapping("/orders")
    public AppOrderResult subscribe(@RequestBody AppAmountBody body)
    {
        if (body == null || body.getProductId() == null)
        {
            return AppOrderResult.fail("请选择产品");
        }
        return AppOrderResult.ok(fillSeriesCover(orderService.subscribe(AppSecurityUtils.getMemberId(),
                body.getProductId(), body.getCurrency())));
    }

    @ApiOperation(value = "我的认购订单", notes = "分页。status：0持仓中 1已完成。每条带所属产品系列 seriesId/seriesName/seriesCoverUrl。")
    @GetMapping("/orders")
    public TableDataInfo orders()
    {
        startPage();
        BizOrder query = new BizOrder();
        query.setMemberId(AppSecurityUtils.getMemberId());
        TableDataInfo table = getDataTable(orderService.selectOrderList(query));
        List<?> rows = table.getRows();
        if (rows != null)
        {
            for (int i = 0; i < rows.size(); i++)
            {
                Object row = rows.get(i);
                if (row instanceof BizOrder)
                {
                    fillSeriesCover((BizOrder) row);
                }
            }
        }
        return table;
    }

    @ApiOperation(value = "我的钱包/资产卡", notes = "data 含 CNY/USDT 余额、冻结、产品收益。助力值固定 0。")
    @GetMapping("/wallet")
    public AppWalletResult wallet()
    {
        return AppWalletResult.ok(walletService.selectAppWalletCard(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation(value = "申请充值", notes = "只提交申请，后台审核通过才入账。")
    @PostMapping("/recharge")
    public AppRechargeResult recharge(@RequestBody AppAmountBody body)
    {
        String currency = body.getCurrency() == null ? BizConstants.CURRENCY_CNY : body.getCurrency();
        return AppRechargeResult.ok(rechargeService.apply(AppSecurityUtils.getMemberId(), currency, body.getAmount(), body.getRemark()));
    }

    @ApiOperation(value = "充值记录", notes = "分页。status：0待审 1通过 2拒绝。")
    @GetMapping("/recharge")
    public TableDataInfo rechargeList()
    {
        startPage();
        BizRecharge query = new BizRecharge();
        query.setMemberId(AppSecurityUtils.getMemberId());
        return getDataTable(rechargeService.selectRechargeList(query));
    }

    @ApiOperation(value = "申请提现", notes = "提交后冻结余额，后台确认打款才扣掉。accountInfo 必填。status 0待打款 1已打款 2已拒绝。")
    @PostMapping("/withdraw")
    public AppWithdrawResult withdraw(@RequestBody AppAmountBody body)
    {
        String currency = StringUtils.isEmpty(body.getCurrency()) ? BizConstants.CURRENCY_CNY : body.getCurrency();
        return AppWithdrawResult.ok(withdrawService.apply(AppSecurityUtils.getMemberId(), currency, body.getAmount(),
                body.getAccountInfo(), body.getRemark(), body.getGoogleCode()));
    }

    @ApiOperation(value = "提现记录", notes = "分页。rows 元素含 amount、accountInfo、status、statusLabel、payMethodLabel。")
    @GetMapping("/withdraw")
    public TableDataInfo withdrawList()
    {
        startPage();
        BizWithdraw query = new BizWithdraw();
        query.setMemberId(AppSecurityUtils.getMemberId());
        return getDataTable(withdrawService.selectWithdrawList(query));
    }

    private BizOrder fillSeriesCover(BizOrder order)
    {
        if (order != null)
        {
            order.setSeriesCoverUrl(toPublicUrl(order.getSeriesCoverUrl()));
        }
        return order;
    }

    private String toPublicUrl(String stored)
    {
        if (StringUtils.isEmpty(stored))
        {
            return "";
        }
        if (stored.startsWith("http://") || stored.startsWith("https://"))
        {
            return stored;
        }
        String domain = serverConfig.getUrl();
        if (stored.startsWith("/"))
        {
            return domain + stored;
        }
        return domain + "/" + stored;
    }
}

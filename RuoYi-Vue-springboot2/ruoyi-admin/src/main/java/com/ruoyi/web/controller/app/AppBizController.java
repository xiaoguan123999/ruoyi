package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.AppAmountBody;
import com.ruoyi.biz.domain.BizCheckin;
import com.ruoyi.biz.domain.BizOrder;
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.domain.BizRecharge;
import com.ruoyi.biz.domain.BizWithdraw;
import com.ruoyi.biz.service.IBizCheckinService;
import com.ruoyi.biz.service.IBizOrderService;
import com.ruoyi.biz.service.IBizProductService;
import com.ruoyi.biz.service.IBizRechargeService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.biz.service.IBizWithdrawService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.AppSecurityUtils;
import com.ruoyi.common.utils.StringUtils;
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
    private IBizProductService productService;

    @Autowired
    private IBizOrderService orderService;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizRechargeService rechargeService;

    @Autowired
    private IBizWithdrawService withdrawService;

    @ApiOperation("每日签到")
    @PostMapping("/checkin")
    public AjaxResult checkin()
    {
        return success(checkinService.checkin(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation("签到记录")
    @GetMapping("/checkin/list")
    public TableDataInfo checkinList()
    {
        startPage();
        BizCheckin query = new BizCheckin();
        query.setMemberId(AppSecurityUtils.getMemberId());
        return getDataTable(checkinService.selectCheckinList(query));
    }

    @ApiOperation("产品列表")
    @GetMapping("/products")
    public AjaxResult products()
    {
        BizProduct query = new BizProduct();
        query.setStatus(BizConstants.STATUS_OK);
        return success(productService.selectProductList(query));
    }

    @ApiOperation("认购产品")
    @PostMapping("/orders")
    public AjaxResult subscribe(@RequestBody AppAmountBody body)
    {
        if (body == null || body.getProductId() == null)
        {
            return error("请选择产品");
        }
        return success(orderService.subscribe(AppSecurityUtils.getMemberId(), body.getProductId()));
    }

    @ApiOperation("我的认购订单")
    @GetMapping("/orders")
    public TableDataInfo orders()
    {
        startPage();
        BizOrder query = new BizOrder();
        query.setMemberId(AppSecurityUtils.getMemberId());
        return getDataTable(orderService.selectOrderList(query));
    }

    @ApiOperation("我的钱包")
    @GetMapping("/wallet")
    public AjaxResult wallet()
    {
        return success(walletService.selectWalletsByMemberId(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation("申请充值")
    @PostMapping("/recharge")
    public AjaxResult recharge(@RequestBody AppAmountBody body)
    {
        String currency = body.getCurrency() == null ? BizConstants.CURRENCY_CNY : body.getCurrency();
        return success(rechargeService.apply(AppSecurityUtils.getMemberId(), currency, body.getAmount(), body.getRemark()));
    }

    @ApiOperation("充值记录")
    @GetMapping("/recharge")
    public TableDataInfo rechargeList()
    {
        startPage();
        BizRecharge query = new BizRecharge();
        query.setMemberId(AppSecurityUtils.getMemberId());
        return getDataTable(rechargeService.selectRechargeList(query));
    }

    @ApiOperation("申请提现")
    @PostMapping("/withdraw")
    public AjaxResult withdraw(@RequestBody AppAmountBody body)
    {
        String currency = StringUtils.isEmpty(body.getCurrency()) ? BizConstants.CURRENCY_CNY : body.getCurrency();
        return success(withdrawService.apply(AppSecurityUtils.getMemberId(), currency, body.getAmount(),
                body.getAccountInfo(), body.getRemark()));
    }

    @ApiOperation("提现记录")
    @GetMapping("/withdraw")
    public TableDataInfo withdrawList()
    {
        startPage();
        BizWithdraw query = new BizWithdraw();
        query.setMemberId(AppSecurityUtils.getMemberId());
        return getDataTable(withdrawService.selectWithdrawList(query));
    }
}

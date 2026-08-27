package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizPromoGrant;
import com.ruoyi.biz.domain.BizPromoRule;
import com.ruoyi.biz.service.IBizPromoService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-实名认证奖励")
@RestController
@RequestMapping("/biz/kycReward")
public class BizKycRewardController extends BaseController
{
    @Autowired
    private IBizPromoService promoService;

    @ApiOperation("实名认证奖励配置")
    @PreAuthorize("@ss.hasPermi('biz:kycReward:query')")
    @GetMapping
    public AjaxResult getConfig()
    {
        return success(promoService.getRule());
    }

    @ApiOperation("保存实名认证奖励")
    @PreAuthorize("@ss.hasPermi('biz:kycReward:edit')")
    @Log(title = "实名认证奖励", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult save(@RequestBody BizPromoRule rule)
    {
        promoService.saveKycSelfReward(rule);
        return success();
    }

    @ApiOperation("实名奖励领取记录")
    @PreAuthorize("@ss.hasPermi('biz:kycReward:query')")
    @GetMapping("/grant/list")
    public TableDataInfo grantList(BizPromoGrant grant)
    {
        if (grant == null)
        {
            grant = new BizPromoGrant();
        }
        if (StringUtils.isEmpty(grant.getGrantType()))
        {
            grant.setGrantType(BizConstants.PROMO_KYC_SELF);
        }
        startPage();
        List<BizPromoGrant> list = promoService.selectGrantList(grant);
        return getDataTable(list);
    }
}

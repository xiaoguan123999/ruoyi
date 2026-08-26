package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizPayProvider;
import com.ruoyi.biz.service.IBizOnlinePayService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-支付供应商")
@RestController
@RequestMapping("/biz/payProvider")
public class BizPayProviderController extends BaseController
{
    @Autowired
    private IBizOnlinePayService onlinePayService;

    @ApiOperation("供应商列表")
    @PreAuthorize("@ss.hasPermi('biz:payProvider:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizPayProvider query)
    {
        startPage();
        List<BizPayProvider> list = onlinePayService.selectProviderList(query);
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                hideSecret(list.get(i));
            }
        }
        return getDataTable(list);
    }

    @ApiOperation("供应商详情")
    @PreAuthorize("@ss.hasPermi('biz:payProvider:query')")
    @GetMapping("/{providerId}")
    public AjaxResult getInfo(@PathVariable Long providerId)
    {
        BizPayProvider row = onlinePayService.selectProviderById(providerId);
        hideSecret(row);
        return success(row);
    }

    @ApiOperation("修改供应商")
    @PreAuthorize("@ss.hasPermi('biz:payProvider:edit')")
    @Log(title = "支付供应商", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizPayProvider row)
    {
        return toAjax(onlinePayService.updateProvider(row));
    }

    private static void hideSecret(BizPayProvider row)
    {
        if (row == null)
        {
            return;
        }
        if (StringUtils.isNotEmpty(row.getSecretKey()))
        {
            row.setSecretKey("******");
        }
    }
}

package com.ruoyi.web.controller.biz;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizAuditBody;
import com.ruoyi.biz.domain.BizWithdraw;
import com.ruoyi.biz.domain.BizWithdrawBatchBody;
import com.ruoyi.biz.domain.BizWithdrawRule;
import com.ruoyi.biz.service.IBizWithdrawService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-提现审核")
@RestController
@RequestMapping("/biz/withdraw")
public class BizWithdrawController extends BaseController
{
    @Autowired
    private IBizWithdrawService withdrawService;

    @ApiOperation("提现规则")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:list')")
    @GetMapping("/config")
    public AjaxResult config()
    {
        return success(withdrawService.getRule());
    }

    @ApiOperation("保存提现规则")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:audit')")
    @Log(title = "提现规则", businessType = BusinessType.UPDATE)
    @PutMapping("/config")
    public AjaxResult saveConfig(@RequestBody BizWithdrawRule rule)
    {
        withdrawService.saveRule(rule);
        return success();
    }

    @ApiOperation("提现列表")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizWithdraw withdraw)
    {
        startPage();
        List<BizWithdraw> list = withdrawService.selectWithdrawList(withdraw);
        fillPayMethodLabel(list);
        return getDataTable(list);
    }

    @ApiOperation("导出提现，按当前筛选条件；传 withdrawIds 则只导出勾选行")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:list')")
    @Log(title = "提现导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizWithdraw withdraw, String withdrawIds)
    {
        if ((withdraw.getWithdrawIds() == null || withdraw.getWithdrawIds().length == 0)
                && withdrawIds != null && withdrawIds.length() > 0)
        {
            String[] parts = withdrawIds.split(",");
            java.util.List<Long> idList = new java.util.ArrayList<Long>();
            for (int i = 0; i < parts.length; i++)
            {
                String part = parts[i].trim();
                if (part.length() > 0)
                {
                    idList.add(Long.valueOf(part));
                }
            }
            if (!idList.isEmpty())
            {
                withdraw.setWithdrawIds(idList.toArray(new Long[0]));
            }
        }
        List<BizWithdraw> list = withdrawService.selectWithdrawList(withdraw);
        fillPayMethodLabel(list);
        for (int i = 0; i < list.size(); i++)
        {
            list.get(i).setApplyTime(list.get(i).getCreateTime());
        }
        ExcelUtil<BizWithdraw> util = new ExcelUtil<BizWithdraw>(BizWithdraw.class);
        util.exportExcel(response, list, "提现");
    }

    @ApiOperation("提现详情")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:query')")
    @GetMapping("/{withdrawId}")
    public AjaxResult getInfo(@PathVariable Long withdrawId)
    {
        BizWithdraw row = withdrawService.selectWithdrawById(withdrawId);
        if (row != null)
        {
            row.fillPayMethodLabel();
        }
        return success(row);
    }

    @ApiOperation("审核提现：0审核中 → 3待打款（不扣冻结）→ 1提现成功（扣冻结）；审核中/待打款均可标记失败")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:audit')")
    @Log(title = "提现审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody BizAuditBody body)
    {
        withdrawService.audit(body.getId(), body.getStatus(), getUsername(), body.getAuditRemark(), body.getPayProofUrl());
        return success();
    }

    @ApiOperation("批量审核：传 ids 处理勾选；不传 ids 则按当前筛选条件处理全部")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:audit')")
    @Log(title = "提现批量审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/batch")
    public AjaxResult auditBatch(@RequestBody BizWithdrawBatchBody body)
    {
        Long[] ids = body.getIds();
        if (ids == null || ids.length == 0)
        {
            BizWithdraw query = new BizWithdraw();
            query.setWithdrawId(body.getWithdrawId());
            query.setMemberId(body.getMemberId());
            query.setPhone(body.getPhone());
            query.setCurrency(body.getCurrency());
            query.setStatus(body.getFilterStatus());
            java.util.Map<String, Object> params = query.getParams();
            if (body.getBeginTime() != null && body.getBeginTime().length() > 0)
            {
                params.put("beginTime", body.getBeginTime());
            }
            if (body.getEndTime() != null && body.getEndTime().length() > 0)
            {
                params.put("endTime", body.getEndTime());
            }
            List<BizWithdraw> list = withdrawService.selectWithdrawList(query);
            ids = new Long[list.size()];
            for (int i = 0; i < list.size(); i++)
            {
                ids[i] = list.get(i).getWithdrawId();
            }
        }
        return success(withdrawService.auditBatch(ids, body.getStatus(), getUsername(), body.getAuditRemark(), body.getPayProofUrl()));
    }

    private void fillPayMethodLabel(List<BizWithdraw> list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            list.get(i).fillPayMethodLabel();
        }
    }
}

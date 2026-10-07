package com.ruoyi.biz.domain;

import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("同步持仓快照结果")
public class BizOrderSnapshotSyncResult
{
    @ApiModelProperty("产品ID")
    private Long productId;

    @ApiModelProperty("产品名称")
    private String productName;

    @ApiModelProperty("只同步持仓中，固定 0")
    private String statusFilter;

    @ApiModelProperty("该产品持仓单总数（含助力）")
    private Integer totalMatched;

    @ApiModelProperty("有差异、会同步的日返单")
    private Integer wouldSync;

    @ApiModelProperty("已与产品一致，跳过")
    private Integer skipSame;

    @ApiModelProperty("助力单，首期不同步")
    private Integer skipAssist;

    @ApiModelProperty("本次实际更新笔数，仅执行接口有")
    private Integer synced;

    @ApiModelProperty("preview / synced / skip_same")
    private String action;

    @ApiModelProperty("抽样条数")
    private Integer sampleLimit;

    @ApiModelProperty("抽样 diff，执行接口不返回全量")
    private List<BizOrderSnapshotPreview> sample = new ArrayList<BizOrderSnapshotPreview>();

    @ApiModelProperty("产品级提示")
    private List<String> warnings = new ArrayList<String>();

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getStatusFilter() { return statusFilter; }
    public void setStatusFilter(String statusFilter) { this.statusFilter = statusFilter; }
    public Integer getTotalMatched() { return totalMatched; }
    public void setTotalMatched(Integer totalMatched) { this.totalMatched = totalMatched; }
    public Integer getWouldSync() { return wouldSync; }
    public void setWouldSync(Integer wouldSync) { this.wouldSync = wouldSync; }
    public Integer getSkipSame() { return skipSame; }
    public void setSkipSame(Integer skipSame) { this.skipSame = skipSame; }
    public Integer getSkipAssist() { return skipAssist; }
    public void setSkipAssist(Integer skipAssist) { this.skipAssist = skipAssist; }
    public Integer getSynced() { return synced; }
    public void setSynced(Integer synced) { this.synced = synced; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Integer getSampleLimit() { return sampleLimit; }
    public void setSampleLimit(Integer sampleLimit) { this.sampleLimit = sampleLimit; }
    public List<BizOrderSnapshotPreview> getSample() { return sample; }
    public void setSample(List<BizOrderSnapshotPreview> sample) { this.sample = sample; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    @ApiModel("单笔快照对比")
    public static class BizOrderSnapshotPreview
    {
        private Long orderId;
        private String orderNo;
        private Long memberId;
        private String phone;
        private Boolean changed;
        @ApiModelProperty("would_sync / skip_same / skip_assist / synced")
        private String action;
        private List<BizOrderSnapshotFieldDiff> fieldDiffs = new ArrayList<BizOrderSnapshotFieldDiff>();
        private List<String> warnings = new ArrayList<String>();

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Boolean getChanged() { return changed; }
        public void setChanged(Boolean changed) { this.changed = changed; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public List<BizOrderSnapshotFieldDiff> getFieldDiffs() { return fieldDiffs; }
        public void setFieldDiffs(List<BizOrderSnapshotFieldDiff> fieldDiffs) { this.fieldDiffs = fieldDiffs; }
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }

    @ApiModel("字段前后值")
    public static class BizOrderSnapshotFieldDiff
    {
        private String field;
        private String before;
        private String after;

        public BizOrderSnapshotFieldDiff() {}

        public BizOrderSnapshotFieldDiff(String field, String before, String after)
        {
            this.field = field;
            this.before = before;
            this.after = after;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getBefore() { return before; }
        public void setBefore(String before) { this.before = before; }
        public String getAfter() { return after; }
        public void setAfter(String after) { this.after = after; }
    }
}

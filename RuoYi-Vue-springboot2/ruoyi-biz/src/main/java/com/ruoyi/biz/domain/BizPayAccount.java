package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("会员收款账户")
public class BizPayAccount extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("账户ID")
    private Long accountId;
    @ApiModelProperty("会员ID")
    private Long memberId;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("USDT/BANK/ALIPAY")
    private String accountType;
    @ApiModelProperty("户名")
    private String accountName;
    @ApiModelProperty("卡号/支付宝账号/USDT地址")
    private String accountNo;
    @ApiModelProperty("银行名称")
    private String bankName;
    @ApiModelProperty("USDT网络")
    private String network;
    @ApiModelProperty("是否默认 1是")
    private String isDefault;
    @ApiModelProperty("0正常 1停用")
    private String status;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    public String getIsDefault() { return isDefault; }
    public void setIsDefault(String isDefault) { this.isDefault = isDefault; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

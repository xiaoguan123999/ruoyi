package com.ruoyi.biz.domain;

import java.util.ArrayList;
import java.util.List;

public class BizDashboardStats
{
    private String date;
    private BizDashboardCount register;
    private BizDashboardCount kyc;
    private BizDashboardCount checkin;
    private BizDashboardMoney checkinReward;
    private BizDashboardMoney recharge;
    private BizDashboardCount rechargeUsers;
    private BizDashboardCount rechargeOrders;
    private BizDashboardCount subscribeUsers;
    private BizDashboardCount subscribeNewUsers;
    private BizDashboardCount pullCount;
    private BizDashboardMoney pullAmount;
    private BizDashboardMoney withdrawProduct;
    private BizDashboardMoney withdrawPromo;
    private BizDashboardMoney withdrawAssist;
    private BizDashboardMoney withdrawTotal;
    private BizDashboardCount withdrawCount;
    private BizDashboardMoney withdrawApply;
    private BizDashboardMoney rebate;
    private BizDashboardMoney commission;
    private BizDashboardMoney invite;
    private BizDashboardCount holdingOrders;
    private BizDashboardCount holdingUsers;
    private BizDashboardMoney walletAvailable;
    private BizDashboardMoney walletFrozen;
    private long pendingRecharge;
    private long pendingWithdraw;
    private long pendingLevelReward;
    private List<BizDashboardActivity> recent = new ArrayList<BizDashboardActivity>();

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public BizDashboardCount getRegister() { return register; }
    public void setRegister(BizDashboardCount register) { this.register = register; }
    public BizDashboardCount getKyc() { return kyc; }
    public void setKyc(BizDashboardCount kyc) { this.kyc = kyc; }
    public BizDashboardCount getCheckin() { return checkin; }
    public void setCheckin(BizDashboardCount checkin) { this.checkin = checkin; }
    public BizDashboardMoney getCheckinReward() { return checkinReward; }
    public void setCheckinReward(BizDashboardMoney checkinReward) { this.checkinReward = checkinReward; }
    public BizDashboardMoney getRecharge() { return recharge; }
    public void setRecharge(BizDashboardMoney recharge) { this.recharge = recharge; }
    public BizDashboardCount getRechargeUsers() { return rechargeUsers; }
    public void setRechargeUsers(BizDashboardCount rechargeUsers) { this.rechargeUsers = rechargeUsers; }
    public BizDashboardCount getRechargeOrders() { return rechargeOrders; }
    public void setRechargeOrders(BizDashboardCount rechargeOrders) { this.rechargeOrders = rechargeOrders; }
    public BizDashboardCount getSubscribeUsers() { return subscribeUsers; }
    public void setSubscribeUsers(BizDashboardCount subscribeUsers) { this.subscribeUsers = subscribeUsers; }
    public BizDashboardCount getSubscribeNewUsers() { return subscribeNewUsers; }
    public void setSubscribeNewUsers(BizDashboardCount subscribeNewUsers) { this.subscribeNewUsers = subscribeNewUsers; }
    public BizDashboardCount getPullCount() { return pullCount; }
    public void setPullCount(BizDashboardCount pullCount) { this.pullCount = pullCount; }
    public BizDashboardMoney getPullAmount() { return pullAmount; }
    public void setPullAmount(BizDashboardMoney pullAmount) { this.pullAmount = pullAmount; }
    public BizDashboardMoney getWithdrawProduct() { return withdrawProduct; }
    public void setWithdrawProduct(BizDashboardMoney withdrawProduct) { this.withdrawProduct = withdrawProduct; }
    public BizDashboardMoney getWithdrawPromo() { return withdrawPromo; }
    public void setWithdrawPromo(BizDashboardMoney withdrawPromo) { this.withdrawPromo = withdrawPromo; }
    public BizDashboardMoney getWithdrawAssist() { return withdrawAssist; }
    public void setWithdrawAssist(BizDashboardMoney withdrawAssist) { this.withdrawAssist = withdrawAssist; }
    public BizDashboardMoney getWithdrawTotal() { return withdrawTotal; }
    public void setWithdrawTotal(BizDashboardMoney withdrawTotal) { this.withdrawTotal = withdrawTotal; }
    public BizDashboardCount getWithdrawCount() { return withdrawCount; }
    public void setWithdrawCount(BizDashboardCount withdrawCount) { this.withdrawCount = withdrawCount; }
    public BizDashboardMoney getWithdrawApply() { return withdrawApply; }
    public void setWithdrawApply(BizDashboardMoney withdrawApply) { this.withdrawApply = withdrawApply; }
    public BizDashboardMoney getRebate() { return rebate; }
    public void setRebate(BizDashboardMoney rebate) { this.rebate = rebate; }
    public BizDashboardMoney getCommission() { return commission; }
    public void setCommission(BizDashboardMoney commission) { this.commission = commission; }
    public BizDashboardMoney getInvite() { return invite; }
    public void setInvite(BizDashboardMoney invite) { this.invite = invite; }
    public BizDashboardCount getHoldingOrders() { return holdingOrders; }
    public void setHoldingOrders(BizDashboardCount holdingOrders) { this.holdingOrders = holdingOrders; }
    public BizDashboardCount getHoldingUsers() { return holdingUsers; }
    public void setHoldingUsers(BizDashboardCount holdingUsers) { this.holdingUsers = holdingUsers; }
    public BizDashboardMoney getWalletAvailable() { return walletAvailable; }
    public void setWalletAvailable(BizDashboardMoney walletAvailable) { this.walletAvailable = walletAvailable; }
    public BizDashboardMoney getWalletFrozen() { return walletFrozen; }
    public void setWalletFrozen(BizDashboardMoney walletFrozen) { this.walletFrozen = walletFrozen; }
    public long getPendingRecharge() { return pendingRecharge; }
    public void setPendingRecharge(long pendingRecharge) { this.pendingRecharge = pendingRecharge; }
    public long getPendingWithdraw() { return pendingWithdraw; }
    public void setPendingWithdraw(long pendingWithdraw) { this.pendingWithdraw = pendingWithdraw; }
    public long getPendingLevelReward() { return pendingLevelReward; }
    public void setPendingLevelReward(long pendingLevelReward) { this.pendingLevelReward = pendingLevelReward; }
    public List<BizDashboardActivity> getRecent() { return recent; }
    public void setRecent(List<BizDashboardActivity> recent) { this.recent = recent; }
}

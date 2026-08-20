package com.ruoyi.biz.constant;

/**
 * 业务常量
 */
public class BizConstants
{
    public static final String CURRENCY_CNY = "CNY";

    public static final String CURRENCY_USDT = "USDT";

    /** 未实名 */
    public static final String KYC_NONE = "0";

    /** 已实名 */
    public static final String KYC_DONE = "1";

    public static final String GA_NONE = "0";

    public static final String GA_BOUND = "1";

    /** 正常 */
    public static final String STATUS_OK = "0";

    /** 停用/下架 */
    public static final String STATUS_DISABLE = "1";

    /** 待审 */
    public static final String AUDIT_PENDING = "0";

    /** 通过 */
    public static final String AUDIT_PASS = "1";

    /** 拒绝 */
    public static final String AUDIT_REJECT = "2";

    /** 持仓中 */
    public static final String ORDER_HOLDING = "0";

    /** 已完成 */
    public static final String ORDER_FINISHED = "1";

    public static final String BIZ_CHECKIN = "CHECKIN";

    public static final String BIZ_SUBSCRIBE = "SUBSCRIBE";

    public static final String BIZ_REBATE = "REBATE";

    public static final String BIZ_RECHARGE = "RECHARGE";

    public static final String BIZ_WITHDRAW_FREEZE = "WITHDRAW_FREEZE";

    public static final String BIZ_WITHDRAW_SUCCESS = "WITHDRAW_SUCCESS";

    public static final String BIZ_WITHDRAW_REJECT = "WITHDRAW_REJECT";

    public static final String BIZ_COMMISSION = "COMMISSION";

    public static final String CONFIG_CHECKIN_AMOUNT = "biz.checkin.amount";

    public static final String CONFIG_WITHDRAW_MIN = "biz.withdraw.minAmount";

    public static final String CONFIG_WITHDRAW_MIN_USDT = "biz.withdraw.minAmount.usdt";

    public static final String CONFIG_RATE_L1 = "biz.team.rate.l1";

    public static final String CONFIG_RATE_L2 = "biz.team.rate.l2";

    public static final String CONFIG_RATE_L3 = "biz.team.rate.l3";

    public static final String CONFIG_INVITE_REWARD = "biz.invite.reward";

    public static final String CONFIG_USDT_ENABLED = "biz.usdt.enabled";

    public static final String CONFIG_CHECKIN_PRIZE1_DAYS = "biz.checkin.prize1.days";
    public static final String CONFIG_CHECKIN_PRIZE1_NAME = "biz.checkin.prize1.name";
    public static final String CONFIG_CHECKIN_PRIZE1_RATE = "biz.checkin.prize1.rate";
    public static final String CONFIG_CHECKIN_PRIZE1_ENABLED = "biz.checkin.prize1.enabled";
    public static final String CONFIG_CHECKIN_PRIZE2_DAYS = "biz.checkin.prize2.days";
    public static final String CONFIG_CHECKIN_PRIZE2_NAME = "biz.checkin.prize2.name";
    public static final String CONFIG_CHECKIN_PRIZE2_RATE = "biz.checkin.prize2.rate";
    public static final String CONFIG_CHECKIN_PRIZE2_ENABLED = "biz.checkin.prize2.enabled";

    public static final String CONFIG_GOOGLE_ENABLED = "biz.google.enabled";
    public static final String CONFIG_GOOGLE_REQUIRE_WITHDRAW = "biz.google.requireWithdraw";
    public static final String CONFIG_GOOGLE_ISSUER = "biz.google.issuer";
}



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

    public static final int PAY_PASSWORD_MIN_LENGTH = 4;

    public static final int PAY_PASSWORD_MAX_LENGTH = 20;

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

    public static final String BIZ_WITHDRAW_PRODUCT = "WITHDRAW_PRODUCT";

    public static final String BIZ_WITHDRAW_PROMO = "WITHDRAW_PROMO";

    public static final String PAY_ALIPAY = "ALIPAY";

    public static final String PAY_USDT = "USDT";

    public static final String BIZ_COMMISSION = "COMMISSION";

    public static final String BIZ_LEVEL_REWARD = "LEVEL_REWARD";

    public static final String BIZ_KYC_REWARD = "KYC_REWARD";

    public static final String BIZ_INVITE = "INVITE";

    public static final String BIZ_ADJUST = "ADJUST";

    public static final String WALLET_BALANCE = "BALANCE";

    public static final String WALLET_PRODUCT = "PRODUCT";

    public static final String WALLET_PROMO = "PROMO";

    public static final String WALLET_ASSIST = "ASSIST";

    public static final String WALLET_WITHDRAW_NONE = "NONE";

    public static final String WALLET_WITHDRAW_OPEN = "OPEN";

    public static final String WALLET_WITHDRAW_ANY_ORDER = "ANY_ORDER";

    public static final String WALLET_WITHDRAW_PRODUCT_REQUIRED = "PRODUCT_REQUIRED";

    /** 推广收益：签到、实名奖励、邀请、分佣、等级奖励 */
    public static boolean isPromoIncome(String bizType)
    {
        return BIZ_CHECKIN.equals(bizType)
                || BIZ_KYC_REWARD.equals(bizType)
                || BIZ_INVITE.equals(bizType)
                || BIZ_COMMISSION.equals(bizType)
                || BIZ_LEVEL_REWARD.equals(bizType);
    }

    public static void addPromoIncomeTypes(java.util.Set<String> types)
    {
        types.add(BIZ_CHECKIN);
        types.add(BIZ_KYC_REWARD);
        types.add(BIZ_INVITE);
        types.add(BIZ_COMMISSION);
        types.add(BIZ_LEVEL_REWARD);
    }

    public static final String PROMO_KYC_SELF = "KYC_SELF";

    public static final String PROMO_INVITE = "INVITE";

    public static final String CONFIG_CHECKIN_AMOUNT = "biz.checkin.amount";

    public static final String CONFIG_WITHDRAW_MIN = "biz.withdraw.minAmount";

    public static final String CONFIG_WITHDRAW_MIN_USDT = "biz.withdraw.minAmount.usdt";

    public static final String CONFIG_WITHDRAW_MAX = "biz.withdraw.maxAmount";

    public static final String CONFIG_WITHDRAW_MAX_USDT = "biz.withdraw.maxAmount.usdt";

    public static final String CONFIG_WITHDRAW_FEE_RATE = "biz.withdraw.feeRate";

    /** 提现是否必须已实名 */
    public static final String CONFIG_WITHDRAW_NEED_KYC = "biz.withdraw.needKyc";

    public static final String CONFIG_RATE_L1 = "biz.team.rate.l1";

    public static final String CONFIG_RATE_L2 = "biz.team.rate.l2";

    public static final String CONFIG_RATE_L3 = "biz.team.rate.l3";

    public static final String CONFIG_INVITE_REWARD = "biz.invite.reward";

    public static final String CONFIG_PROMO_ENABLED = "biz.promo.enabled";

    public static final String CONFIG_PROMO_KYC_SELF_ENABLED = "biz.promo.kycSelf.enabled";

    public static final String CONFIG_PROMO_KYC_SELF_CNY = "biz.promo.kycSelf.cny";

    public static final String CONFIG_PROMO_KYC_SELF_USDT = "biz.promo.kycSelf.usdt";

    public static final String CONFIG_PROMO_INVITE_ENABLED = "biz.promo.invite.enabled";

    public static final String CONFIG_PROMO_INVITE_AMOUNT = "biz.promo.invite.amount";

    public static final String CONFIG_PROMO_INVITE_CURRENCY = "biz.promo.invite.currency";

    public static final String CONFIG_PROMO_LOCK_PARENT = "biz.promo.invite.lockParent";

    public static final String CONFIG_TEAM_ENABLED = "biz.team.enabled";

    public static final String CONFIG_PROMO_RULE_TEXT = "biz.promo.ruleText";

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

    public static final String CONFIG_LEVEL_REWARD_ENABLED = "biz.levelReward.enabled";
    public static final String CONFIG_LEVEL_REWARD_MIXED_PAY = "biz.levelReward.mixedPayCurrency";
    public static final String CONFIG_LEVEL_REWARD_PERF_SOURCE = "biz.levelReward.performanceSource";
    public static final String CONFIG_LEVEL_REWARD_INCLUDE_SELF = "biz.levelReward.includeSelf";
    public static final String CONFIG_LEVEL_REWARD_NEED_KYC = "biz.levelReward.validNeedKyc";
    public static final String CONFIG_LEVEL_REWARD_NEED_ORDER = "biz.levelReward.validNeedOrder";
    public static final String CONFIG_LEVEL_REWARD_TEXT = "biz.levelReward.ruleText";
    public static final String CONFIG_LEVEL_REWARD_HINT = "biz.levelReward.hint";
    /** 1 USDT 折合多少人民币，等级折合门槛用 */
    public static final String CONFIG_FX_USDT_TO_CNY = "biz.fx.usdtToCny";
    public static final String FX_USDT_TO_CNY_DEFAULT = "6.25";
    /** 分币种同时达标（默认，与现网一致） */
    public static final String THRESHOLD_SPLIT = "SPLIT";
    /** 折合后或过一项：CNY门槛、USDT门槛填了的项，满足其中一项即可 */
    public static final String THRESHOLD_EQUIV = "EQUIV";
    public static final String REWARD_MODE_AUTO = "AUTO";
    public static final String REWARD_MODE_MANUAL = "MANUAL";
    /** 用户在 App 领取 */
    public static final String REWARD_MODE_CLAIM = "CLAIM";
    /** 用户领取：本周期二选一 */
    public static final String CLAIM_POLICY_ONE = "ONE";
    /** 用户领取：两种币都可各领一次 */
    public static final String CLAIM_POLICY_ALL = "ALL";
    public static final String DICT_TEAM_DEPTH = "biz_team_depth";
    public static final int TEAM_MAX_LEVEL = 7;

    public static final String PAY_BANK = "BANK";

    public static final String BLACKLIST_LOGIN = "LOGIN";

    public static final String BLACKLIST_REGISTER = "REGISTER";

    public static final String BLACKLIST_KYC = "KYC";

    public static final String BLACKLIST_BANK = "BANK";

    public static final String BLACKLIST_HIT_PHONE = "PHONE";

    public static final String BLACKLIST_HIT_ID_CARD = "ID_CARD";

    public static final String BLACKLIST_HIT_BANK_CARD = "BANK_CARD";

    public static final String PAY_MOCK_YES = "1";
    public static final String PAY_MOCK_NO = "0";
    public static final String PAY_MODE_MANUAL = "0";
    public static final String PAY_MODE_ONLINE = "1";
    public static final String PAY_ORDER_WAIT = "0";
    public static final String PAY_ORDER_SUCCESS = "1";
    public static final String PAY_ORDER_FAIL = "2";
    public static final String PAY_ORDER_CLOSED = "3";
    public static final String PAY_TRADE_SUCCESS = "1";
    public static final String PAY_SCENE_ALIPAY = "alipay";
    public static final String PAY_SCENE_WECHAT = "wechat";
    public static final String PAY_SCENE_UNION = "union";
    public static final String PAY_SCENE_USDT = "usdt";

    public static final String ABOUT_MODE_TEXT = "TEXT";

    public static final String ABOUT_MODE_PDF = "PDF";

    public static final String CONFIG_SERVICE_TITLE = "biz.service.title";
    public static final String CONFIG_SERVICE_WORK_TIME = "biz.service.workTime";
    public static final String CONFIG_SERVICE_HINT = "biz.service.hint";
}



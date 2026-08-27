SET NAMES utf8mb4;
-- 业务参数改到对应功能页：补最高提现、把签到规则/注册推广挂回可见目录（可重复执行）

delete from sys_config where config_id in (71, 72) or config_key in ('biz.withdraw.maxAmount', 'biz.withdraw.maxAmount.usdt');
insert into sys_config values(71, '提现最高金额', 'biz.withdraw.maxAmount', '0', 'N', 'admin', sysdate(), '', null, '人民币最高提现，0表示不限');
insert into sys_config values(72, 'USDT最高提现', 'biz.withdraw.maxAmount.usdt', '0', 'N', 'admin', sysdate(), '', null, 'USDT最高提现，0表示不限');

-- 签到规则、签到中奖：产品交易
update sys_menu set parent_id = 2025, order_num = 5 where menu_id = 2011;
update sys_menu set parent_id = 2025, order_num = 6 where menu_id = 2012;

-- 注册推广规则：会员中心
update sys_menu set parent_id = 2297, order_num = 11 where menu_id = 2023;

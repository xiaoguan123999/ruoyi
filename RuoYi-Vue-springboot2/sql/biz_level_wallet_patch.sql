SET NAMES utf8mb4;
-- 到账钱包、发放币种、有效成员规则下放到每个等级（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'wallet_type_code'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column wallet_type_code varchar(32) not null default ''PROMO'' comment ''到账钱包类型'' after reward_usdt',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'mixed_pay_currency'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column mixed_pay_currency varchar(16) not null default ''USDT'' comment ''发放币种 USDT/CNY/BOTH'' after wallet_type_code',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'valid_need_kyc'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column valid_need_kyc char(1) not null default ''1'' comment ''有效成员需实名 1是 0否'' after mixed_pay_currency',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'valid_need_order'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column valid_need_order char(1) not null default ''1'' comment ''有效成员需认购 1是 0否'' after valid_need_kyc',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @wallet := 'PROMO';
set @has_credit := (
  select count(*) from information_schema.tables
  where table_schema = database() and table_name = 'biz_wallet_credit_rule'
);
set @sql := if(@has_credit > 0,
  'select ifnull((select type_code from biz_wallet_credit_rule where biz_type = ''LEVEL_REWARD'' limit 1), ''PROMO'') into @wallet',
  'select ''PROMO'' into @wallet');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_level
set wallet_type_code = ifnull(@wallet, 'PROMO')
where wallet_type_code = 'PROMO';

set @mixed := (
  select config_value from sys_config where config_key = 'biz.levelReward.mixedPayCurrency' limit 1
);
update biz_level
set mixed_pay_currency = ifnull(@mixed, 'USDT')
where mixed_pay_currency = 'USDT';

set @kyc := (
  select config_value from sys_config where config_key = 'biz.levelReward.validNeedKyc' limit 1
);
update biz_level
set valid_need_kyc = if(@kyc in ('false', '0', 'n', 'N'), '0', '1')
where valid_need_kyc = '1';

set @ord := (
  select config_value from sys_config where config_key = 'biz.levelReward.validNeedOrder' limit 1
);
update biz_level
set valid_need_order = if(@ord in ('false', '0', 'n', 'N'), '0', '1')
where valid_need_order = '1';

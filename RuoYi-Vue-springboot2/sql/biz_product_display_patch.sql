SET NAMES utf8mb4;
-- 产品展示字段：收益发放方式、风险等级（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_product'
    and column_name = 'payout_method'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column payout_method varchar(100) default '''' comment ''收益发放方式，App展示'' after buy_limit',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_product'
    and column_name = 'risk_level'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column risk_level varchar(64) default '''' comment ''风险等级，App展示'' after payout_method',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

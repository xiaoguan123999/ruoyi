SET NAMES utf8mb4;
-- 等级门槛：下级累计充值（按团队要求层数统计，不含本人）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_recharge_cny'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column min_team_recharge_cny decimal(18,4) not null default 0 comment ''下级累计充值CNY，0不限制'' after min_recharge_usdt',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_recharge_usdt'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column min_team_recharge_usdt decimal(18,4) not null default 0 comment ''下级累计充值USDT，0不限制'' after min_team_recharge_cny',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

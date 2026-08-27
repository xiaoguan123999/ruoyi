SET NAMES utf8mb4;
-- 产品一拖二：直属下级认购同一产品达到份数后，再等 N 小时才开始日返（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_product' and column_name = 'unlock_direct_qty'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column unlock_direct_qty int(11) not null default 0 comment ''直属下级需认购同一产品份数，0关闭'' after buy_limit',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_product' and column_name = 'unlock_delay_hours'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column unlock_delay_hours int(11) not null default 0 comment ''条件达成后等待小时数再开始收益'' after unlock_direct_qty',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_order' and column_name = 'unlock_direct_qty'
);
set @sql := if(@exist = 0,
  'alter table biz_order add column unlock_direct_qty int(11) not null default 0 comment ''下单时一拖二份数快照'' after withdraw_required',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_order' and column_name = 'unlock_delay_hours'
);
set @sql := if(@exist = 0,
  'alter table biz_order add column unlock_delay_hours int(11) not null default 0 comment ''下单时等待小时快照'' after unlock_direct_qty',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_order' and column_name = 'income_start_time'
);
set @sql := if(@exist = 0,
  'alter table biz_order add column income_start_time datetime default null comment ''收益开始时间'' after unlock_delay_hours',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

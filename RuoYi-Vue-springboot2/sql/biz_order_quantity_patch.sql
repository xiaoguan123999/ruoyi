SET NAMES utf8mb4;
-- 认购订单支持数量（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_order'
    and column_name = 'quantity'
);
set @sql := if(@exist = 0,
  'alter table biz_order add column quantity int(11) not null default 1 comment ''认购份数'' after price',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

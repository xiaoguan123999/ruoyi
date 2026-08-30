SET NAMES utf8mb4;
-- 产品是否开售。可重复执行。现有产品默认开售。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_product'
    and column_name = 'on_sale'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column on_sale char(1) not null default ''1'' comment ''1开售 0未开售'' after risk_level',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

SET NAMES utf8mb4;
-- 产品限购：每人限购份数，0 表示不限制。（可重复执行）

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'buy_limit');
set @sql := if(@exist = 0, 'alter table biz_product add column buy_limit int(11) default 0 comment ''每人限购份数，0不限制'' after withdraw_required', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product set buy_limit = 0 where buy_limit is null;

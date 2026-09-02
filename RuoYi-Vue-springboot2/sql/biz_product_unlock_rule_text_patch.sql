SET NAMES utf8mb4;
-- 产品激活条件文案。后台填写，App 原样展示。可重复执行。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_product'
    and column_name = 'unlock_rule_text'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column unlock_rule_text varchar(500) default '''' comment ''激活条件文案，App原样展示'' after unlock_delay_hours',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

SET NAMES utf8mb4;
-- 同一产品同时配人民币/USDT 价格和日返。认购按所选币种扣对应钱包。（可重复执行）

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'price_cny');
set @sql := if(@exist = 0, 'alter table biz_product add column price_cny decimal(18,4) default null comment ''人民币认购价'' after price', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'price_usdt');
set @sql := if(@exist = 0, 'alter table biz_product add column price_usdt decimal(18,4) default null comment ''USDT认购价'' after price_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'daily_rebate_cny');
set @sql := if(@exist = 0, 'alter table biz_product add column daily_rebate_cny decimal(18,4) default null comment ''人民币每日返利'' after daily_rebate', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'daily_rebate_usdt');
set @sql := if(@exist = 0, 'alter table biz_product add column daily_rebate_usdt decimal(18,4) default null comment ''USDT每日返利'' after daily_rebate_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product
   set price_cny = price, daily_rebate_cny = daily_rebate
 where price_cny is null
   and (currency is null or currency = '' or upper(currency) = 'CNY');

update biz_product
   set price_usdt = price, daily_rebate_usdt = daily_rebate
 where price_usdt is null
   and upper(currency) = 'USDT';

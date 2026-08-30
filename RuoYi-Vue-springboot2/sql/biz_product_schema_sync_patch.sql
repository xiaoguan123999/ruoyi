SET NAMES utf8mb4;
-- 补齐 biz_product 缺列，避免 /biz/product/list 报 Unknown column 'unlock_direct_qty'
-- 可重复执行；在业务库执行后刷新产品列表即可，然后重启 Java

-- 产品系列表，列表 left join 用：
create table if not exists biz_product_category (
  category_id       bigint(20)      not null auto_increment    comment '分类/系列ID',
  category_name     varchar(100)    not null                   comment '系列名称',
  cover_url         varchar(500)    default ''                 comment '封面图',
  status            char(1)         default '0'                comment '0显示 1隐藏',
  sort              int(4)          default 0                  comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (category_id)
) engine=innodb comment = '产品分类/系列';

-- biz_product 列表查询用到的列
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'name_en');
set @sql := if(@exist = 0, 'alter table biz_product add column name_en varchar(100) default '''' comment ''英文名'' after product_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'category_id');
set @sql := if(@exist = 0, 'alter table biz_product add column category_id bigint(20) default null comment ''所属分类/系列ID'' after name_en', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

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

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'buy_limit');
set @sql := if(@exist = 0, 'alter table biz_product add column buy_limit int(11) default 0 comment ''每人限购数量，0不限制'' after withdraw_required', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'unlock_direct_qty');
set @sql := if(@exist = 0, 'alter table biz_product add column unlock_direct_qty int(11) not null default 0 comment ''直推下级认购同一产品数量，0关闭'' after buy_limit', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'unlock_delay_hours');
set @sql := if(@exist = 0, 'alter table biz_product add column unlock_delay_hours int(11) not null default 0 comment ''认购完成后等待小时数再开始返利'' after unlock_direct_qty', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'payout_method');
set @sql := if(@exist = 0, 'alter table biz_product add column payout_method varchar(100) default '''' comment ''收益发放方式'' after unlock_delay_hours', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'risk_level');
set @sql := if(@exist = 0, 'alter table biz_product add column risk_level varchar(64) default '''' comment ''风险等级'' after payout_method', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'cover_url');
set @sql := if(@exist = 0, 'alter table biz_product add column cover_url varchar(500) default '''' comment ''产品封面图'' after sort', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product set buy_limit = 0 where buy_limit is null;
update biz_product set price_cny = price, daily_rebate_cny = daily_rebate
 where price_cny is null and (currency is null or currency = '' or upper(currency) = 'CNY');
update biz_product set price_usdt = price, daily_rebate_usdt = daily_rebate
 where price_usdt is null and upper(ifnull(currency,'')) = 'USDT';

-- 订单一次购买份数，避免认购/返利用到列缺失
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'quantity');
set @sql := if(@exist = 0, 'alter table biz_order add column quantity int(11) not null default 1 comment ''认购份数'' after price', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'unlock_direct_qty');
set @sql := if(@exist = 0, 'alter table biz_order add column unlock_direct_qty int(11) not null default 0 comment ''下单时一次购买的份数'' after withdraw_required', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'unlock_delay_hours');
set @sql := if(@exist = 0, 'alter table biz_order add column unlock_delay_hours int(11) not null default 0 comment ''下单时等待小时数'' after unlock_direct_qty', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'income_start_time');
set @sql := if(@exist = 0, 'alter table biz_order add column income_start_time datetime default null comment ''收益开始时间'' after unlock_delay_hours', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

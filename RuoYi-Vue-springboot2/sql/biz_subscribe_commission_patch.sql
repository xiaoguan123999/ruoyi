SET NAMES utf8mb4;
-- 团队返佣改为认购触发（可重复执行，不删菜单）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_commission_log'
    and column_name = 'order_id'
);
set @sql := if(@exist = 0,
  'alter table biz_commission_log add column order_id bigint(20) default null comment ''认购订单ID'' after recharge_id',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update sys_config set remark = 'false关闭认购三级返佣' where config_key = 'biz.team.enabled';
update sys_config set remark = '认购一级分佣百分比' where config_key = 'biz.team.rate.l1';
update sys_config set remark = '认购二级分佣百分比' where config_key = 'biz.team.rate.l2';
update sys_config set remark = '认购三级分佣百分比' where config_key = 'biz.team.rate.l3';

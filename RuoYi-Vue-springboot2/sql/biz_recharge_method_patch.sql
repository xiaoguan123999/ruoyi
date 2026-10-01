-- 充值方式主数据 + 通道履约类型
-- 通道 scene = 支付方式 method_code；USDT 链上走独立链上充值，不造系统通道
-- 可重复执行

-- 1) 通道履约类型 ONLINE/CHAIN（三方线上仍可用；链上入口不依赖通道行）
SET @col := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_pay_channel' AND column_name = 'fulfill_type'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE biz_pay_channel ADD COLUMN fulfill_type varchar(16) NOT NULL DEFAULT ''ONLINE'' COMMENT ''ONLINE收银台 CHAIN链上'' AFTER scene',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
UPDATE biz_pay_channel SET fulfill_type = 'ONLINE' WHERE fulfill_type IS NULL OR fulfill_type = '';

-- 若仍残留 method_id（旧中间稿），先回填 scene 再删列
SET @col := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_pay_channel' AND column_name = 'method_id'
);
SET @sql := IF(@col > 0,
  'UPDATE biz_pay_channel c INNER JOIN biz_recharge_method m ON m.method_id = c.method_id SET c.scene = LOWER(m.method_code) WHERE c.method_id IS NOT NULL',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF(@col > 0,
  'ALTER TABLE biz_pay_channel DROP COLUMN method_id',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 清掉中间稿造的「系统内置」链上占位通道
DELETE FROM biz_pay_channel WHERE channel_code = 'SYSTEM_CHAIN_USDT' OR provider_code = 'system';
DELETE FROM biz_pay_provider WHERE provider_code = 'system';

-- 3) 充值方式表（最终字段）
create table if not exists biz_recharge_method (
  method_id    bigint       not null auto_increment comment '方式ID',
  method_code  varchar(32)  not null comment '编码 wechat/alipay/usdt/bank_cs',
  label        varchar(64)  not null comment '展示名',
  icon_url     varchar(500) default '' comment '图标URL',
  is_cs        char(1)      not null default '0' comment '是否客服 0否 1是',
  status       char(1)      default '0' comment '0启用 1停用',
  sort_order   int          default 0 comment '排序升序',
  remark       varchar(500) default null comment '备注',
  create_by    varchar(64)  default '' comment '创建者',
  create_time  datetime     comment '创建时间',
  update_by    varchar(64)  default '' comment '更新者',
  update_time  datetime     comment '更新时间',
  primary key (method_id),
  unique key uk_recharge_method_code (method_code)
) engine=innodb auto_increment=1 comment = 'App充值方式';

-- 旧库补 icon_url
SET @col := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_recharge_method' AND column_name = 'icon_url'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE biz_recharge_method ADD COLUMN icon_url varchar(500) DEFAULT '''' COMMENT ''图标URL'' AFTER label',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

insert into biz_recharge_method
  (method_code, label, icon_url, is_cs, status, sort_order, create_by, create_time, remark)
select * from (
  select 'wechat' as method_code, '微信' as label, '' as icon_url, '0' as is_cs, '0' as status, 10 as sort_order,
         'admin' as create_by, sysdate() as create_time, '线上微信，通道 scene=wechat' as remark
  union all
  select 'alipay', '支付宝', '', '0', '0', 20, 'admin', sysdate(), '线上支付宝，通道 scene=alipay'
  union all
  select 'usdt', 'USDT', '', '0', '0', 30, 'admin', sysdate(), '链上USDT，走独立链上充值入口'
  union all
  select 'bank_cs', '银行卡（客服）', '', '1', '0', 40, 'admin', sysdate(), '跳转在线客服'
) t
where not exists (select 1 from biz_recharge_method x where x.method_code = t.method_code);

-- 4) 菜单：支付中心下「充值方式」
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 2470, '充值方式', 2358, 4, 'rechargeMethod', 'biz/rechargeMethod/index', '', '', 1, 0, 'C', '0', '0', 'biz:rechargeMethod:list', 'money', 'admin', sysdate(), '', null, 'App充值页顶部方式'
from dual where not exists (select 1 from sys_menu where menu_id = 2470 or perms = 'biz:rechargeMethod:list');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 2471, '方式查询', 2470, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:rechargeMethod:query', '#', 'admin', sysdate(), '', null, ''
from dual where not exists (select 1 from sys_menu where menu_id = 2471 or perms = 'biz:rechargeMethod:query')
  and exists (select 1 from sys_menu where menu_id = 2470);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 2472, '方式新增', 2470, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:rechargeMethod:add', '#', 'admin', sysdate(), '', null, ''
from dual where not exists (select 1 from sys_menu where menu_id = 2472 or perms = 'biz:rechargeMethod:add')
  and exists (select 1 from sys_menu where menu_id = 2470);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 2473, '方式修改', 2470, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:rechargeMethod:edit', '#', 'admin', sysdate(), '', null, ''
from dual where not exists (select 1 from sys_menu where menu_id = 2473 or perms = 'biz:rechargeMethod:edit')
  and exists (select 1 from sys_menu where menu_id = 2470);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 2474, '方式删除', 2470, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:rechargeMethod:remove', '#', 'admin', sysdate(), '', null, ''
from dual where not exists (select 1 from sys_menu where menu_id = 2474 or perms = 'biz:rechargeMethod:remove')
  and exists (select 1 from sys_menu where menu_id = 2470);

UPDATE sys_menu SET parent_id = 2358, order_num = 4
WHERE menu_id = 2470 AND (parent_id <> 2358 OR order_num <> 4);

insert into sys_role_menu (role_id, menu_id)
select 1, 2470 from dual where not exists (select 1 from sys_role_menu where role_id = 1 and menu_id = 2470);
insert into sys_role_menu (role_id, menu_id)
select 1, 2471 from dual where not exists (select 1 from sys_role_menu where role_id = 1 and menu_id = 2471);
insert into sys_role_menu (role_id, menu_id)
select 1, 2472 from dual where not exists (select 1 from sys_role_menu where role_id = 1 and menu_id = 2472);
insert into sys_role_menu (role_id, menu_id)
select 1, 2473 from dual where not exists (select 1 from sys_role_menu where role_id = 1 and menu_id = 2473);
insert into sys_role_menu (role_id, menu_id)
select 1, 2474 from dual where not exists (select 1 from sys_role_menu where role_id = 1 and menu_id = 2474);

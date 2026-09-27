SET NAMES utf8mb4;
-- ============================================================
-- 2026-09-27 增量脚本（可重复执行）
-- A. 百乐支付供应商 + 通道
-- B. 支付通道「新增」菜单权限
-- C. 累计结算对档激活份数字段
-- ============================================================

-- ------------------------------------------------------------
-- A. 百乐支付供应商 + 通道
-- 通道编码需向客服确认后改 biz_pay_channel.product_id；文档示例 wayCode=901
-- ------------------------------------------------------------
insert into biz_pay_provider
  (provider_code, provider_name, adapter_family, gateway_url, app_id, secret_key, callback_ips, mock_mode, status, sort_order, remark, create_time)
select * from (
  select
    'baile' as provider_code,
    '百乐支付' as provider_name,
    'baile' as adapter_family,
    'https://fuckbaile.jkosiuwn.xyz' as gateway_url,
    '54593239253254917' as app_id,
    'ec94999cf1034dc398cea90a5897ba3f' as secret_key,
    '43.165.198.183' as callback_ips,
    '0' as mock_mode,
    '0' as status,
    13 as sort_order,
    '百乐：wayCode 填通道 product_id；回调成功 state=2；应答 SUCCESS/OK' as remark,
    sysdate() as create_time
) t
where not exists (select 1 from biz_pay_provider p where p.provider_code = t.provider_code);

insert into biz_pay_channel
  (provider_code, channel_code, channel_name, display_name, scene, product_id, currency, min_amount, max_amount, weight, status, sort_order, remark, create_time)
select * from (
  select
    'baile' as provider_code,
    'BAILE_ALIPAY' as channel_code,
    '支付宝' as channel_name,
    '百乐支付宝' as display_name,
    'alipay' as scene,
    '901' as product_id,
    'CNY' as currency,
    10 as min_amount,
    50000 as max_amount,
    170 as weight,
    '0' as status,
    1 as sort_order,
    '文档示例 wayCode=901，请按客服实际编码修改 product_id' as remark,
    sysdate() as create_time
) t
where not exists (select 1 from biz_pay_channel c where c.channel_code = t.channel_code);

-- ------------------------------------------------------------
-- B. 支付通道「新增」权限
-- ------------------------------------------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 2370, '通道新增', 2028, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payChannel:add', '#', 'admin', sysdate(), '', null, '支付通道新增'
from dual
where not exists (select 1 from sys_menu where menu_id = 2370 or perms = 'biz:payChannel:add');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, 2370
from sys_role_menu rm
where rm.menu_id = 2308
  and exists (select 1 from sys_menu m where m.menu_id = 2370)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 2370);

-- ------------------------------------------------------------
-- C. 累计结算：对档激活份数消耗字段
-- ------------------------------------------------------------
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'related_slots_used');
set @sql := if(@exist = 0, 'alter table biz_order add column related_slots_used int(11) not null default 0 comment ''累计结算已消耗的对档激活份数'' after last_accumulate_date', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'accumulate_settled_shares');
set @sql := if(@exist = 0, 'alter table biz_order add column accumulate_settled_shares int(11) not null default 0 comment ''当前累计周期内已结算份数'' after related_slots_used', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_order set related_slots_used = 0 where related_slots_used is null;
update biz_order set accumulate_settled_shares = 0 where accumulate_settled_shares is null;

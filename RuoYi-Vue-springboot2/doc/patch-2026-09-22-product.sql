SET NAMES utf8mb4;
-- ============================================================
-- 2026-09-22 产品相关增量脚本（可重复执行）
-- 章节：A 卡片模板 / B ASSIST / C skip_detail / D 发放模式
--       E ACCUMULATE / F 会员钱包菜单
-- 用法：目标业务库执行本文件即可。
-- 不含链上充值（sql/biz_chain_deposit*.sql；菜单 2039、job 102 冲突）。
-- ============================================================

-- ------------------------------------------------------------
-- A. 产品卡片模板（表/字段/预置模板/菜单）
-- ------------------------------------------------------------

-- ---------- 1. 模板表 ----------
create table if not exists biz_product_card_template (
  template_id        bigint(20)      not null auto_increment    comment '模板ID',
  template_code      varchar(32)     not null                   comment 'App编码，契约字段',
  template_name      varchar(64)     not null                   comment '运营名称',
  preview_url        varchar(500)    default ''                 comment '预览图',
  metric_slot_count  int(4)          not null default 2         comment '指标坑数量',
  has_main_amount    char(1)         default '0'                comment '是否有大号主金额区（1是 0否）',
  default_cta_text   varchar(32)     default '立即参与'          comment '默认按钮文案',
  sort               int(4)          default 0                  comment '排序',
  status             char(1)         default '0'                comment '0启用 1停用',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '适用场景说明',
  primary key (template_id),
  unique key uk_template_code (template_code)
) engine=innodb comment = '产品卡片布局模板';

-- ---------- 2. 指标槽子表 ----------
create table if not exists biz_product_card_metric (
  id                 bigint(20)      not null auto_increment    comment '主键',
  product_id         bigint(20)      not null                   comment '产品ID',
  slot_index         int(4)          not null                   comment '坑位，从1开始',
  label              varchar(32)     not null default ''        comment '展示标签',
  source             varchar(32)     not null default 'CUSTOM'  comment '数据来源',
  custom_text        varchar(64)     default ''                 comment 'CUSTOM时文案',
  primary key (id),
  unique key uk_product_slot (product_id, slot_index),
  key idx_metric_product (product_id)
) engine=innodb comment = '产品卡片指标槽';

-- ---------- 3. 产品表加列 ----------
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'template_id');
set @sql := if(@exist = 0, 'alter table biz_product add column template_id bigint(20) default null comment ''卡片模板ID'' after cover_url', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'theme');
set @sql := if(@exist = 0, 'alter table biz_product add column theme varchar(16) default ''blue'' comment ''卡片主题色'' after template_id', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'badge_text');
set @sql := if(@exist = 0, 'alter table biz_product add column badge_text varchar(64) default '''' comment ''角标覆盖，空则用系列名'' after theme', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'card_no');
set @sql := if(@exist = 0, 'alter table biz_product add column card_no varchar(8) default '''' comment ''卡片序号（NUMBERED等）'' after badge_text', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'cta_text');
set @sql := if(@exist = 0, 'alter table biz_product add column cta_text varchar(32) default '''' comment ''主按钮文案覆盖'' after card_no', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 4. 系列表加列 ----------
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product_category' and column_name = 'default_template_id');
set @sql := if(@exist = 0, 'alter table biz_product_category add column default_template_id bigint(20) default null comment ''新建产品默认模板'' after cover_url', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 5. 预置模板（固定 ID 1~9；预留停用） ----------
insert into biz_product_card_template
  (template_id, template_code, template_name, preview_url, metric_slot_count, has_main_amount, default_cta_text, sort, status, create_by, create_time, remark)
select 1, 'CLASSIC', '经典详情卡', '', 2, '1', '立即参与', 10, '0', 'admin', sysdate(), '默认兜底，接近现网'
from dual where not exists (select 1 from biz_product_card_template where template_code = 'CLASSIC');

insert into biz_product_card_template
  (template_id, template_code, template_name, preview_url, metric_slot_count, has_main_amount, default_cta_text, sort, status, create_by, create_time, remark)
select 2, 'HERO', '天启大图卡', '', 2, '1', '立即参与', 20, '0', 'admin', sysdate(), '旗舰/故事感系列'
from dual where not exists (select 1 from biz_product_card_template where template_code = 'HERO');

insert into biz_product_card_template
  (template_id, template_code, template_name, preview_url, metric_slot_count, has_main_amount, default_cta_text, sort, status, create_by, create_time, remark)
select 3, 'SPLIT', '助力左右卡', '', 4, '0', '立即参与', 30, '0', 'admin', sysdate(), '双币种强对比、助力向'
from dual where not exists (select 1 from biz_product_card_template where template_code = 'SPLIT');

insert into biz_product_card_template
  (template_id, template_code, template_name, preview_url, metric_slot_count, has_main_amount, default_cta_text, sort, status, create_by, create_time, remark)
select 4, 'NUMBERED', '深空序号卡', '', 4, '0', '立即认购', 40, '0', 'admin', sysdate(), '序列编号、多档并列'
from dual where not exists (select 1 from biz_product_card_template where template_code = 'NUMBERED');

insert into biz_product_card_template
  (template_id, template_code, template_name, preview_url, metric_slot_count, has_main_amount, default_cta_text, sort, status, create_by, create_time, remark)
select 5, 'ROW', '简洁列表行', '', 2, '0', '立即参与', 50, '0', 'admin', sysdate(), '产品多、快速扫价'
from dual where not exists (select 1 from biz_product_card_template where template_code = 'ROW');

insert into biz_product_card_template
  (template_id, template_code, template_name, preview_url, metric_slot_count, has_main_amount, default_cta_text, sort, status, create_by, create_time, remark)
select 6, 'COMPACT', '紧凑双列指标卡', '', 4, '0', '立即参与', 60, '0', 'admin', sysdate(), '紧凑展示多指标'
from dual where not exists (select 1 from biz_product_card_template where template_code = 'COMPACT');

insert into biz_product_card_template
  (template_id, template_code, template_name, preview_url, metric_slot_count, has_main_amount, default_cta_text, sort, status, create_by, create_time, remark)
select 7, 'BANNER', '横幅主推卡', '', 2, '0', '立即参与', 70, '1', 'admin', sysdate(), '二期预留：首页主推'
from dual where not exists (select 1 from biz_product_card_template where template_code = 'BANNER');

insert into biz_product_card_template
  (template_id, template_code, template_name, preview_url, metric_slot_count, has_main_amount, default_cta_text, sort, status, create_by, create_time, remark)
select 8, 'PRICE_FOCUS', '价格突出卡', '', 2, '1', '立即参与', 80, '1', 'admin', sysdate(), '二期预留：强调门槛金额'
from dual where not exists (select 1 from biz_product_card_template where template_code = 'PRICE_FOCUS');

insert into biz_product_card_template
  (template_id, template_code, template_name, preview_url, metric_slot_count, has_main_amount, default_cta_text, sort, status, create_by, create_time, remark)
select 9, 'MEDIA_LEFT', '左图右文标准卡', '', 3, '0', '立即参与', 90, '1', 'admin', sysdate(), '二期预留：资讯/电商风'
from dual where not exists (select 1 from biz_product_card_template where template_code = 'MEDIA_LEFT');

-- ---------- 6. 老产品默认 CLASSIC + 默认指标（仅无模板时） ----------
update biz_product set template_id = 1 where template_id is null;
update biz_product set theme = 'blue' where theme is null or theme = '';

insert into biz_product_card_metric (product_id, slot_index, label, source, custom_text)
select p.product_id, 1, '每日收益', 'DAILY_REBATE', ''
from biz_product p
where not exists (
  select 1 from biz_product_card_metric m where m.product_id = p.product_id and m.slot_index = 1
);

insert into biz_product_card_metric (product_id, slot_index, label, source, custom_text)
select p.product_id, 2, '收益周期', 'DURATION', ''
from biz_product p
where not exists (
  select 1 from biz_product_card_metric m where m.product_id = p.product_id and m.slot_index = 2
);

update biz_product_category set default_template_id = 1 where default_template_id is null;

-- ---------- 7. 菜单（挂在「产品交易」2025 下） ----------
delete from sys_role_menu where menu_id in (2039, 2360, 2361, 2362, 2363, 2364);
delete from sys_menu where menu_id in (2039, 2360, 2361, 2362, 2363, 2364);

insert into sys_menu values('2039', '卡片模板', '2025', '3', 'productCardTemplate', 'biz/productCardTemplate/index', '', '', 1, 0, 'C', '0', '0', 'biz:productCardTemplate:list', 'form', 'admin', sysdate(), '', null, 'App产品卡片布局模板');
insert into sys_menu values('2360', '模板查询', '2039', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCardTemplate:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2361', '模板新增', '2039', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCardTemplate:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2362', '模板修改', '2039', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCardTemplate:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2363', '模板删除', '2039', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCardTemplate:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2364', '模板下拉', '2039', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCardTemplate:query', '#', 'admin', sysdate(), '', null, '');

-- 给已拥有「产品管理」权限的角色同步授权卡片模板
insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
cross join (select 2039 as menu_id union all select 2360 union all select 2361 union all select 2362 union all select 2363 union all select 2364) m
where rm.menu_id = 2002
  and not exists (
    select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id
  );


-- ------------------------------------------------------------
-- B. ASSIST 业务模式（助力退本/钱包规则/退本任务）
-- ------------------------------------------------------------

-- ---------- 1. 产品表字段 ----------
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'biz_mode');
set @sql := if(@exist = 0, 'alter table biz_product add column biz_mode varchar(16) default ''REBATE'' comment ''业务模式 REBATE日返 / ASSIST助力退本'' after on_sale', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'assist_value_cny');
set @sql := if(@exist = 0, 'alter table biz_product add column assist_value_cny decimal(18,4) default 0 comment ''助力值(CNY侧，认购成功发放)'' after biz_mode', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'assist_value_usdt');
set @sql := if(@exist = 0, 'alter table biz_product add column assist_value_usdt decimal(18,4) default 0 comment ''助力值(USDT侧，认购成功发放)'' after assist_value_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'principal_return_days');
set @sql := if(@exist = 0, 'alter table biz_product add column principal_return_days int(4) default 0 comment ''本金返还天数，ASSIST 用'' after assist_value_usdt', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product set biz_mode = 'REBATE' where biz_mode is null or biz_mode = '';

-- ---------- 2. 订单表快照字段 ----------
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'biz_mode');
set @sql := if(@exist = 0, 'alter table biz_order add column biz_mode varchar(16) default ''REBATE'' comment ''业务模式快照'' after withdraw_required', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'assist_value');
set @sql := if(@exist = 0, 'alter table biz_order add column assist_value decimal(18,4) default 0 comment ''本次发放助力值快照'' after biz_mode', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'principal_return_days');
set @sql := if(@exist = 0, 'alter table biz_order add column principal_return_days int(4) default 0 comment ''本金返还天数快照'' after assist_value', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'principal_return_at');
set @sql := if(@exist = 0, 'alter table biz_order add column principal_return_at datetime default null comment ''预计退本时间'' after principal_return_days', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'principal_returned');
set @sql := if(@exist = 0, 'alter table biz_order add column principal_returned char(1) default ''0'' comment ''是否已退本 0否 1是'' after principal_return_at', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_order set biz_mode = 'REBATE' where biz_mode is null or biz_mode = '';
update biz_order set principal_returned = '0' where principal_returned is null or principal_returned = '';

-- ---------- 3. 确保 ASSIST 钱包类型存在（不可提现） ----------
insert into biz_wallet_type (type_code, type_name, withdraw_mode, status, sort, builtin, create_by, create_time, remark)
select 'ASSIST', '助力值', 'NONE', '0', 4, '1', 'admin', sysdate(), '星航助力等发放，不可提现'
from dual where not exists (select 1 from biz_wallet_type where type_code = 'ASSIST');

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time, remark)
select 'ASSIST_GRANT', '助力值发放', 'ASSIST', '1', 20, 'admin', sysdate(), 'ASSIST 产品认购发放'
from dual where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'ASSIST_GRANT');

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time, remark)
select 'PRINCIPAL_RETURN', '助力产品退本', 'BALANCE', '1', 21, 'admin', sysdate(), 'ASSIST 到期本金退回余额（不可提现）'
from dual where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'PRINCIPAL_RETURN');

-- ---------- 4. 定时任务：ASSIST 到期退本（每日 00:10） ----------
delete from sys_job where job_id = 102;
insert into sys_job values(102, '助力产品退本', 'DEFAULT', 'assistPrincipalReturnTask.execute()', '0 10 0 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, 'ASSIST 订单到期本金退回 BALANCE（不可提现）');


-- ------------------------------------------------------------
-- C. 跳过二级页 skip_detail
-- ------------------------------------------------------------

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'skip_detail');
set @sql := if(@exist = 0, 'alter table biz_product add column skip_detail char(1) default ''0'' comment ''1列表直购无二级页 0进认购页'' after on_sale', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product set skip_detail = '0' where skip_detail is null or skip_detail = '';


-- ------------------------------------------------------------
-- D. 助力发放模式 assist_grant_mode
-- ------------------------------------------------------------

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'assist_grant_mode');
set @sql := if(@exist = 0,
  'alter table biz_product add column assist_grant_mode varchar(16) default ''CNY'' comment ''助力发放 MATCH跟认购币 / CNY固定 / USDT固定 / BOTH双送'' after biz_mode',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 历史 ASSIST 产品默认固定送 CNY（与人民币 1:1）
update biz_product
set assist_grant_mode = 'CNY'
where (assist_grant_mode is null or assist_grant_mode = '')
  and upper(ifnull(biz_mode, '')) = 'ASSIST';

update biz_product
set assist_grant_mode = 'CNY'
where assist_grant_mode is null or assist_grant_mode = '';


-- ------------------------------------------------------------
-- E. 日返累计入账 ACCUMULATE
-- ------------------------------------------------------------

-- ---------- 1. 产品表 ----------
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'income_mode');
set @sql := if(@exist = 0, 'alter table biz_product add column income_mode varchar(16) default ''CREDIT'' comment ''日返入账 CREDIT进钱包 / ACCUMULATE订单累计'' after principal_return_days', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'accumulate_cycle_days');
set @sql := if(@exist = 0, 'alter table biz_product add column accumulate_cycle_days int(4) default 0 comment ''累计周期天数，如60；0表示不用'' after income_mode', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'related_product_id');
set @sql := if(@exist = 0, 'alter table biz_product add column related_product_id bigint(20) default null comment ''对档产品ID，结算累计前须持有'' after accumulate_cycle_days', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product set income_mode = 'CREDIT' where income_mode is null or income_mode = '';
update biz_product set accumulate_cycle_days = 0 where accumulate_cycle_days is null;

-- ---------- 2. 订单表快照 + 运行字段 ----------
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'income_mode');
set @sql := if(@exist = 0, 'alter table biz_order add column income_mode varchar(16) default ''CREDIT'' comment ''入账方式快照'' after principal_returned', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'accumulate_cycle_days');
set @sql := if(@exist = 0, 'alter table biz_order add column accumulate_cycle_days int(4) default 0 comment ''累计周期天数快照'' after income_mode', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'related_product_id');
set @sql := if(@exist = 0, 'alter table biz_order add column related_product_id bigint(20) default null comment ''对档产品快照'' after accumulate_cycle_days', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'accumulated_amount');
set @sql := if(@exist = 0, 'alter table biz_order add column accumulated_amount decimal(18,4) default 0 comment ''当前周期累计金额'' after related_product_id', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'accumulate_days');
set @sql := if(@exist = 0, 'alter table biz_order add column accumulate_days int(4) default 0 comment ''当前周期已累计天数'' after accumulated_amount', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'accumulate_paused');
set @sql := if(@exist = 0, 'alter table biz_order add column accumulate_paused char(1) default ''0'' comment ''满周期无对档产品暂停 0否1是'' after accumulate_days', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'accumulate_cycle_start_at');
set @sql := if(@exist = 0, 'alter table biz_order add column accumulate_cycle_start_at datetime default null comment ''当前累计周期开始时间'' after accumulate_paused', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'last_accumulate_date');
set @sql := if(@exist = 0, 'alter table biz_order add column last_accumulate_date date default null comment ''上次累计日期'' after accumulate_cycle_start_at', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_order set income_mode = 'CREDIT' where income_mode is null or income_mode = '';
update biz_order set accumulate_cycle_days = 0 where accumulate_cycle_days is null;
update biz_order set accumulated_amount = 0 where accumulated_amount is null;
update biz_order set accumulate_days = 0 where accumulate_days is null;
update biz_order set accumulate_paused = '0' where accumulate_paused is null or accumulate_paused = '';

-- ---------- 3. 结算入账规则 → 产品收益钱包 ----------
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time, remark)
select 'ACCUMULATE_SETTLE', '订单累计结算', 'PRODUCT', '1', 22, 'admin', sysdate(), 'ACCUMULATE 订单满周期结算进产品收益'
from dual where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'ACCUMULATE_SETTLE');


-- ------------------------------------------------------------
-- F. 后台菜单：会员钱包
-- ------------------------------------------------------------

-- 菜单：会员钱包
delete from sys_role_menu where menu_id in (2400, 2401);
delete from sys_menu where menu_id in (2400, 2401);

insert into sys_menu values(2400, '会员钱包', 2032, 3, 'wallet', 'biz/wallet/index', '', '', 1, 0, 'C', '0', '0', 'biz:wallet:list', 'wallet', 'admin', sysdate(), '', null, '按会员查看余额/产品/推广/助力等各钱包可用与冻结');
insert into sys_menu values(2401, '钱包查询', 2400, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:wallet:list', '#', 'admin', sysdate(), '', null, '');

-- 资金流水顺序后移，避免与会员钱包撞序
update sys_menu set order_num = 4 where menu_id = 2007 and parent_id = 2032;

-- 授权给拥有「资金流水」权限的角色
insert into sys_role_menu (role_id, menu_id)
select rm.role_id, 2400
from sys_role_menu rm
where rm.menu_id = 2007
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 2400);

insert into sys_role_menu (role_id, menu_id)
select rm.role_id, 2401
from sys_role_menu rm
where rm.menu_id = 2007
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 2401);

-- 超级管理员兜底
insert into sys_role_menu (role_id, menu_id)
select 1, 2400 from dual where not exists (select 1 from sys_role_menu where role_id = 1 and menu_id = 2400);
insert into sys_role_menu (role_id, menu_id)
select 1, 2401 from dual where not exists (select 1 from sys_role_menu where role_id = 1 and menu_id = 2401);

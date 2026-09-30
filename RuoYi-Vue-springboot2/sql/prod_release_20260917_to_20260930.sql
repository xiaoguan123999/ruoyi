SET NAMES utf8mb4;
-- =============================================================================
-- 生产增量（相对 2026-09-17 22:29 的 ruoyi-admin.jar）
-- 覆盖 2026-09-17 之后到 2026-09-30 的库表/菜单/任务
-- 可重复执行。执行前请备份 ry-vue。
--
-- mysql -uroot -p --default-character-set=utf8mb4 生产库名 < sql/prod_release_20260917_to_20260930.sql
--
-- 原补丁冲突已在本文件内处理：
--   job 102 支付超时关单 / 103 链上扫链 / 104 助力退本（不再互相覆盖）
--   菜单 2039 卡片模板，按钮 2460~2464（生产 2360/2361 已占用，禁止覆盖）
--   链上充值菜单 2450~2454
-- 执行后：后台填链上收款地址和扫链 Key；定时任务确认 102/103/104 为「正常」
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 福旺 / 无忧 / 非凡 通道（结构 + 资料）
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS patch_biz_pay_real_channels;
DELIMITER $$
CREATE PROCEDURE patch_biz_pay_real_channels()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_pay_provider' AND COLUMN_NAME = 'callback_ips'
  ) THEN
    ALTER TABLE biz_pay_provider ADD COLUMN callback_ips varchar(255) default '' comment 'callback source IPs' AFTER secret_key;
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_pay_provider' AND COLUMN_NAME = 'secret_key'
      AND CHARACTER_MAXIMUM_LENGTH < 256
  ) THEN
    ALTER TABLE biz_pay_provider MODIFY COLUMN secret_key varchar(256) default '' comment 'sign key';
  END IF;
END$$
DELIMITER ;
CALL patch_biz_pay_real_channels();
DROP PROCEDURE IF EXISTS patch_biz_pay_real_channels;

INSERT INTO biz_pay_provider
  (provider_code, provider_name, adapter_family, gateway_url, app_id, secret_key,
   callback_ips, mock_mode, status, sort_order, remark, create_time)
SELECT * FROM (
  SELECT 'fuwang' AS provider_code, 'FuWang' AS provider_name, 'jeepay' AS adapter_family,
         'https://fuwang-pay.aaagood.xyz' AS gateway_url,
         'M2100946162876579840' AS app_id, '63d41a6ab1204846aa15cab2d423cf7b' AS secret_key,
         '' AS callback_ips, '0' AS mock_mode, '0' AS status, 10 AS sort_order,
         'FuWang: app_id=mchId; product_id=numeric wayCode' AS remark, NOW() AS create_time
  UNION ALL
  SELECT 'wuyou', 'WuYou', 'wuyou', 'http://pay.wyoukj.click',
         '10109', 'RGYNFXPAI7WX44GFFOALG4EY4U1WVQKHHSLOMWF17VHDOSZ7EYTIVEXSVUCVSBVCVRX4SOZ3HGSA9B4RTCSKNDMWVTXHIZPUYHINUZAECF8XAQPORGTYNIWB0PUS7KPM',
         '18.163.116.198', '0', '0', 11, 'product 8000 alipay 8001 wechat', NOW()
  UNION ALL
  SELECT 'feifan', 'FeiFan', 'monpay', 'https://nicepaymon-api.pangukaitianpidi.xyz',
         '494b481b65cb95f6c1233133', '0E7020b1143951764DA751CFbb7706F39e302Bed',
         '43.199.5.177', '0', '0', 12, 'monpay product 13 alipay 15 wechat', NOW()
) t
WHERE NOT EXISTS (SELECT 1 FROM biz_pay_provider x WHERE x.provider_code = t.provider_code);

INSERT INTO biz_pay_channel
  (provider_code, channel_code, channel_name, display_name, scene, product_id,
   currency, min_amount, max_amount, weight, status, sort_order, remark, create_time)
SELECT * FROM (
  SELECT 'fuwang' AS provider_code, 'FUWANG_ALIPAY' AS channel_code, '支付宝' AS channel_name, '支付宝' AS display_name,
         'alipay' AS scene, '901' AS product_id, 'CNY' AS currency, 10 AS min_amount, 50000 AS max_amount,
         200 AS weight, '0' AS status, 1 AS sort_order, 'FuWang wayCode=901' AS remark, NOW() AS create_time
  UNION ALL SELECT 'fuwang','FUWANG_WECHAT','微信','微信','wechat','901','CNY',10,50000,200,'0',2,'FuWang wayCode=901',NOW()
  UNION ALL SELECT 'wuyou','WUYOU_ALIPAY','支付宝','支付宝','alipay','8000','CNY',100,20000,190,'0',1,'wuyou 8000',NOW()
  UNION ALL SELECT 'wuyou','WUYOU_WECHAT','微信','微信','wechat','8001','CNY',100,3000,190,'0',2,'wuyou 8001',NOW()
  UNION ALL SELECT 'feifan','FEIFAN_ALIPAY','支付宝','支付宝','alipay','13','CNY',100,50000,180,'0',1,'feifan 13',NOW()
  UNION ALL SELECT 'feifan','FEIFAN_WECHAT','微信','微信','wechat','15','CNY',100,50000,180,'0',2,'feifan 15',NOW()
) t
WHERE NOT EXISTS (SELECT 1 FROM biz_pay_channel x WHERE x.channel_code = t.channel_code);

UPDATE biz_pay_channel SET channel_name='支付宝', display_name='支付宝', product_id='901'
WHERE channel_code='FUWANG_ALIPAY';
UPDATE biz_pay_channel SET channel_name='微信', display_name='微信', product_id='901'
WHERE channel_code='FUWANG_WECHAT';
UPDATE biz_pay_channel SET channel_name='支付宝', display_name='支付宝'
WHERE channel_code IN ('WUYOU_ALIPAY','FEIFAN_ALIPAY');
UPDATE biz_pay_channel SET channel_name='微信', display_name='微信'
WHERE channel_code IN ('WUYOU_WECHAT','FEIFAN_WECHAT');


-- -----------------------------------------------------------------------------
-- 2. 支付网关日志
-- -----------------------------------------------------------------------------

-- 支付网关调用 / 回调日志（拉单失败也会留下，独立落库）
-- 排序规则必须与库内其他表一致：utf8mb4_general_ci
-- 文件请保持 UTF-8 保存/导入

CREATE TABLE IF NOT EXISTS biz_pay_gateway_log (
  log_id            bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '日志ID',
  log_type          varchar(16)     NOT NULL                   COMMENT 'CALL调用 CALLBACK回调',
  action            varchar(32)     NOT NULL DEFAULT ''        COMMENT 'create/query/notify',
  provider_code     varchar(32)     DEFAULT ''                 COMMENT '服务商',
  channel_code      varchar(64)     DEFAULT ''                 COMMENT '通道',
  out_trade_no      varchar(64)     DEFAULT ''                 COMMENT '商户单号',
  member_id         bigint(20)      DEFAULT NULL               COMMENT '会员ID',
  request_url       varchar(500)    DEFAULT ''                 COMMENT '请求URL',
  request_body      varchar(4000)   DEFAULT ''                 COMMENT '请求体',
  response_body     varchar(4000)   DEFAULT ''                 COMMENT '响应/回调原文',
  http_status       int(11)         DEFAULT NULL               COMMENT 'HTTP状态',
  success           char(1)         DEFAULT '0'                COMMENT '1成功 0失败',
  error_msg         varchar(500)    DEFAULT ''                 COMMENT '错误信息',
  client_ip         varchar(64)     DEFAULT ''                 COMMENT '回调来源IP',
  cost_ms           bigint(20)      DEFAULT NULL               COMMENT '耗时毫秒',
  create_time       datetime                                   COMMENT '创建时间',
  PRIMARY KEY (log_id),
  KEY idx_pay_gw_log_out (out_trade_no),
  KEY idx_pay_gw_log_provider (provider_code, create_time),
  KEY idx_pay_gw_log_type (log_type, create_time)
) ENGINE=innodb DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付网关调用与回调日志';

-- 若表已按 MySQL8 默认 utf8mb4_0900_ai_ci 建过，执行下面转换（可重复）
ALTER TABLE biz_pay_gateway_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 菜单挂在「支付订单」同级（取其 parent_id）
INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT
  2380, '支付日志',
  (SELECT parent_id FROM sys_menu WHERE menu_id = 2029),
  17, 'payGatewayLog', 'biz/payGatewayLog/index', '', '',
  1, 0, 'C', '0', '0', 'biz:payGatewayLog:list', 'log', 'admin', SYSDATE(),
  '三方拉单/查单/回调原文'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2380)
  AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2029);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT
  2381, '日志查询', 2380, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'biz:payGatewayLog:query', '#', 'admin', SYSDATE(), ''
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2381)
  AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2380);

UPDATE sys_menu SET menu_name='支付日志', remark='三方拉单/查单/回调原文' WHERE menu_id=2380;
UPDATE sys_menu SET menu_name='日志查询' WHERE menu_id=2381;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, m.menu_id
FROM sys_role_menu rm
JOIN (SELECT 2380 AS menu_id UNION ALL SELECT 2381) m
WHERE rm.menu_id = 2029
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = m.menu_id);


-- -----------------------------------------------------------------------------
-- 3. 支付超时分钟参数（任务见文末 job 102）
-- -----------------------------------------------------------------------------

INSERT INTO sys_config
  (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT
  '线上支付单超时分钟',
  'biz.pay.orderExpireMinutes',
  '30',
  'N',
  'admin',
  SYSDATE(),
  '拉起收银台后未支付，超过该分钟数自动关闭待付单'
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_config WHERE config_key = 'biz.pay.orderExpireMinutes'
);


-- -----------------------------------------------------------------------------
-- 4. 链上充值表结构 + 参数（菜单/任务见文末，避免覆盖卡片模板）
-- -----------------------------------------------------------------------------

-- TRON USDT-TRC20 shared address + amount fingerprint auto-credit. Re-runnable.

create table if not exists biz_chain_deposit (
  deposit_id        bigint(20)      not null auto_increment    comment 'id',
  out_trade_no      varchar(64)     not null                   comment 'biz order no',
  recharge_id       bigint(20)      default null               comment 'biz_recharge id',
  member_id         bigint(20)      not null                   comment 'member id',
  asset             varchar(32)     not null default 'USDT-TRC20' comment 'USDT-TRC20',
  currency          varchar(16)     not null default 'USDT'    comment 'wallet currency',
  amount            decimal(18,6)   not null                   comment 'requested 2-decimal amount',
  pay_amount        decimal(18,6)   not null                   comment 'fingerprint amount, 6 decimals',
  address           varchar(128)    not null                   comment 'collection address',
  status            char(1)         not null default '0'       comment '0 wait 1 success 2 expired',
  tx_hash           varchar(128)    default null               comment 'on-chain tx',
  from_address      varchar(128)    default ''                 comment 'payer address',
  expire_time       datetime        not null                   comment 'expire at',
  paid_time         datetime        default null               comment 'credited at',
  create_time       datetime                                   comment 'create time',
  update_time       datetime                                   comment 'update time',
  remark            varchar(500)    default ''                 comment 'remark',
  primary key (deposit_id),
  unique key uk_biz_chain_deposit_no (out_trade_no),
  unique key uk_biz_chain_deposit_tx (tx_hash),
  key idx_biz_chain_deposit_pay (status, pay_amount, expire_time),
  key idx_biz_chain_deposit_member (member_id, create_time)
) engine=innodb comment = 'TRON USDT fingerprint deposit';

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select * from (
  select 'TRON chain deposit switch' as config_name, 'biz.chain.tron.enabled' as config_key,
         'true' as config_value, 'N' as config_type, 'admin' as create_by, sysdate() as create_time,
         'false to hide App chain deposit' as remark
  union all
  select 'TRON collection address', 'biz.chain.tron.address', '', 'N', 'admin', sysdate(),
         'shared USDT-TRC20 receive address, fill in admin config'
  union all
  select 'TronGrid API key', 'biz.chain.tron.apiKey', '', 'N', 'admin', sysdate(),
         'TRON-PRO-API-KEY header, from trongrid.io'
  union all
  select 'Chain deposit expire minutes', 'biz.chain.tron.expireMinutes', '30', 'N', 'admin', sysdate(),
         'pending fingerprint TTL minutes'
  union all
  select 'Chain deposit min USDT', 'biz.chain.tron.minAmount', '10', 'N', 'admin', sysdate(),
         'min requested amount'
  union all
  select 'Chain deposit max USDT', 'biz.chain.tron.maxAmount', '100000', 'N', 'admin', sysdate(),
         'max requested amount, 0 unlimited'
  union all
  select 'Chain deposit mock', 'biz.chain.tron.mock', '0', 'N', 'admin', sysdate(),
         '1 skip TronGrid, admin simulate credit'
  union all
  select 'Chain deposit hint', 'biz.chain.tron.hint',
         'Send USDT-TRC20 to this address. Copy the 6-decimal amount exactly. Do not send TRX or other tokens. Expired orders will not auto-credit.',
         'N', 'admin', sysdate(), 'shown on App pay screen'
) t
where not exists (select 1 from sys_config c where c.config_key = t.config_key);


-- Team-leader USDT-TRC20 address. Re-runnable.

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_member'
    and column_name = 'chain_address'
);
set @sql := if(@exist = 0,
  'alter table biz_member add column chain_address varchar(128) default '''' comment ''USDT-TRC20 collect address for downline'' after remark',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_chain_deposit'
    and column_name = 'address_source'
);
set @sql := if(@exist = 0,
  'alter table biz_chain_deposit add column address_source varchar(16) not null default ''SYSTEM'' comment ''SYSTEM/TEAM'' after address',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_chain_deposit'
    and column_name = 'collect_member_id'
);
set @sql := if(@exist = 0,
  'alter table biz_chain_deposit add column collect_member_id bigint(20) default null comment ''team leader who owns address'' after address_source',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;


-- USDT-BEP20 (BSC) alongside USDT-TRC20. Re-runnable.

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_member'
    and column_name = 'chain_address_bep20'
);
set @sql := if(@exist = 0,
  'alter table biz_member add column chain_address_bep20 varchar(128) default '''' comment ''USDT-BEP20 collect address for downline'' after chain_address',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_chain_deposit'
    and column_name = 'network'
);
set @sql := if(@exist = 0,
  'alter table biz_chain_deposit add column network varchar(16) not null default ''TRC20'' comment ''TRC20 or BEP20'' after asset',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update biz_chain_deposit set network = 'TRC20' where ifnull(network, '') = '';

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select * from (
  select 'BSC chain deposit switch' as config_name, 'biz.chain.bsc.enabled' as config_key,
         'true' as config_value, 'N' as config_type, 'admin' as create_by, sysdate() as create_time,
         'false to hide BEP20 on App; TRC20 uses biz.chain.tron.enabled' as remark
  union all
  select 'BSC collection address', 'biz.chain.bsc.address', '', 'N', 'admin', sysdate(),
         'shared USDT-BEP20 receive address, fill in admin config'
  union all
  select 'BscScan API key', 'biz.chain.bsc.apiKey', '', 'N', 'admin', sysdate(),
         'BscScan / Etherscan API key for tokentx'
  union all
  select 'BscScan API URL', 'biz.chain.bsc.apiUrl', '', 'N', 'admin', sysdate(),
         'empty = https://api.bscscan.com/api ; Etherscan v2 = https://api.etherscan.io/v2/api'
) t
where not exists (select 1 from sys_config c where c.config_key = t.config_key);

-- 链上扫链任务改到 job_id=103，见文末统一任务段


-- -----------------------------------------------------------------------------
-- 5. 产品卡片模板 / 助力退本 / 日返累计 / 会员钱包菜单
-- -----------------------------------------------------------------------------

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
-- 生产已占用 2360 推广奖励发放、2361 汇率配置，按钮改用 2460~2464，禁止 delete 覆盖
INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2039, '卡片模板', 2025, 3, 'productCardTemplate', 'biz/productCardTemplate/index', '', '',
       1, 0, 'C', '0', '0', 'biz:productCardTemplate:list', 'form', 'admin', SYSDATE(), 'App产品卡片布局模板'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2039);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2460, '模板查询', 2039, 1, '', '', '', '',
       1, 0, 'F', '0', '0', 'biz:productCardTemplate:query', '#', 'admin', SYSDATE(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2460);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2461, '模板新增', 2039, 2, '', '', '', '',
       1, 0, 'F', '0', '0', 'biz:productCardTemplate:add', '#', 'admin', SYSDATE(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2461);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2462, '模板修改', 2039, 3, '', '', '', '',
       1, 0, 'F', '0', '0', 'biz:productCardTemplate:edit', '#', 'admin', SYSDATE(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2462);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2463, '模板删除', 2039, 4, '', '', '', '',
       1, 0, 'F', '0', '0', 'biz:productCardTemplate:remove', '#', 'admin', SYSDATE(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2463);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2464, '模板下拉', 2039, 5, '', '', '', '',
       1, 0, 'F', '0', '0', 'biz:productCardTemplate:query', '#', 'admin', SYSDATE(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2464);

-- 给已拥有「产品管理」权限的角色同步授权卡片模板
insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
cross join (select 2039 as menu_id union all select 2460 union all select 2461 union all select 2462 union all select 2463 union all select 2464) m
where rm.menu_id = 2002
  and exists (select 1 from sys_menu x where x.menu_id = m.menu_id)
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

-- 助力退本任务改到 job_id=104，见文末统一任务段


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


-- -----------------------------------------------------------------------------
-- 6. 产品卡片颜色
-- -----------------------------------------------------------------------------

-- 产品卡片颜色覆盖（可空，空则 App 走 theme 推导）可重复执行

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'accent_color');
set @sql := if(@exist = 0, 'alter table biz_product add column accent_color varchar(16) default '''' comment ''序号/英文名强调色，空则跟theme'' after cta_text', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'title_color');
set @sql := if(@exist = 0, 'alter table biz_product add column title_color varchar(16) default '''' comment ''产品名颜色，空则跟theme'' after accent_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'remark_color');
set @sql := if(@exist = 0, 'alter table biz_product add column remark_color varchar(16) default '''' comment ''备注/口号颜色，空则跟theme'' after title_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'label_color');
set @sql := if(@exist = 0, 'alter table biz_product add column label_color varchar(16) default '''' comment ''内容区标题颜色，空则跟theme'' after remark_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'value_color');
set @sql := if(@exist = 0, 'alter table biz_product add column value_color varchar(16) default '''' comment ''数值颜色，空则跟theme'' after label_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'unit_color');
set @sql := if(@exist = 0, 'alter table biz_product add column unit_color varchar(16) default '''' comment ''单位颜色，空则跟theme'' after value_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'btn_color');
set @sql := if(@exist = 0, 'alter table biz_product add column btn_color varchar(16) default '''' comment ''按钮背景色，空则跟theme'' after unit_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'btn_text_color');
set @sql := if(@exist = 0, 'alter table biz_product add column btn_text_color varchar(16) default '''' comment ''按钮文字色，空则白'' after btn_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;


-- -----------------------------------------------------------------------------
-- 7. 大转盘抽奖
-- -----------------------------------------------------------------------------

-- ============================================================
-- 大转盘配置化抽奖 增量脚本（可重复执行）
-- 合并自：lottery-wheel-patch.sql + lottery-chance-rule-max-repeat.sql
-- 对齐本仓库：biz_* 命名、BaseEntity 审计字段、status 0正常/1停用
-- 含：人群包、活动/奖池/必中、获次规则(仅一次/按倍数不限或限档)、次数余额流水、菜单
-- 用法：目标业务库执行本文件即可。
-- ============================================================

-- ------------------------------------------------------------
-- A. 营销标签人群（资产化，多行条件，禁止 JSON 落库）
-- ------------------------------------------------------------

create table if not exists biz_crowd_package (
  package_id        bigint(20)      not null auto_increment    comment '人群包ID',
  package_name      varchar(64)     not null                   comment '人群包名称',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (package_id),
  unique key uk_biz_crowd_package_name (package_name)
) engine=innodb comment = '营销标签人群包';

create table if not exists biz_crowd_condition (
  condition_id      bigint(20)      not null auto_increment    comment '条件ID',
  package_id        bigint(20)      not null                   comment '所属人群包ID',
  label_type        varchar(32)     not null                   comment '标签维度 IMAGE画像 TRANSACTION资产 ACTION行为',
  label_field       varchar(64)     not null                   comment '标签字段 user_type/charge_amount/invite_count',
  operator_type     varchar(16)     not null                   comment '运算符 EQUALS/GREATER_THAN/LESS_THAN',
  rule_value        varchar(128)    not null                   comment '限制值',
  sort              int(4)          default 0                  comment '排序',
  primary key (condition_id),
  key idx_crowd_condition_package (package_id)
) engine=innodb comment = '营销人群包原子条件';

-- ------------------------------------------------------------
-- B. 抽奖活动 / 奖品双池 / 步进必中策略
-- ------------------------------------------------------------

create table if not exists biz_lottery_activity (
  activity_id       bigint(20)      not null auto_increment    comment '活动ID',
  title             varchar(128)    not null                   comment '活动名称',
  start_time        datetime        default null               comment '开始时间，空=立即生效',
  end_time          datetime        default null               comment '结束时间，空=长期有效',
  interval_hours    int(11)         not null default 72        comment '抽奖频控间隔(小时)',
  rule_text         text            default null               comment 'App展示抽奖规则（多行文本）',
  status            char(1)         default '1'                comment '0正常(启用) 1停用；新建默认停用防误开',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (activity_id),
  key idx_lottery_act_duration (start_time, end_time, status)
) engine=innodb comment = '大转盘抽奖活动';


-- 兼容已建表：App 抽奖规则文案
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_activity' and column_name = 'rule_text');
set @sql := if(@exist = 0, 'alter table biz_lottery_activity add column rule_text text default null comment ''App展示抽奖规则（多行文本）'' after interval_hours', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 兼容已建表：时间可空（长期活动）
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_activity' and column_name = 'start_time' and is_nullable = 'NO');
set @sql := if(@exist > 0, 'alter table biz_lottery_activity modify column start_time datetime default null comment ''开始时间，空=立即生效''', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_activity' and column_name = 'end_time' and is_nullable = 'NO');
set @sql := if(@exist > 0, 'alter table biz_lottery_activity modify column end_time datetime default null comment ''结束时间，空=长期有效''', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

create table if not exists biz_lottery_prize_pool (
  pool_id           bigint(20)      not null auto_increment    comment '奖品池ID',
  prize_name        varchar(64)     not null                   comment '奖品名称',
  prize_desc        varchar(128)    default null               comment '奖品说明',
  image_url         varchar(512)    default null               comment '奖品图片',
  sector_bg_color   varchar(32)     default '#FFF8EC'          comment '转盘扇区背景色',
  name_color        varchar(32)     default '#111827'          comment '名称字体颜色',
  desc_color        varchar(32)     default '#374151'          comment '说明字体颜色',
  prize_type        tinyint(4)      not null                   comment '1实物 2现金 3虚拟资产',
  assist_amount     decimal(18,4)   default null               comment '虚拟助力值额度(prize_type=3)',
  currency          varchar(16)     default 'CNY'              comment '虚拟入账币种',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (pool_id)
) engine=innodb comment = '抽奖奖品池（独立管理）';

create table if not exists biz_lottery_prize (
  prize_id          bigint(20)      not null auto_increment    comment '活动奖项ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  pool_id           bigint(20)      default null               comment '奖品池ID',
  prize_name        varchar(64)     not null                   comment '奖品名称快照',
  prize_desc        varchar(128)    default null               comment '奖品说明快照',
  image_url         varchar(512)    default null               comment '奖品图片快照',
  sector_bg_color   varchar(32)     default null               comment '扇区背景色快照',
  name_color        varchar(32)     default null               comment '名称颜色快照',
  desc_color        varchar(32)     default null               comment '说明颜色快照',
  prize_type        tinyint(4)      not null                   comment '1实物 2现金 3虚拟资产',
  public_stock      int(11)         not null default 0         comment '公海库存 -1无限(仅虚拟允许)',
  exclusive_stock   int(11)         not null default 0         comment '专属库存 -1无限(仅虚拟允许)',
  probability       int(11)         not null default 0         comment '公海中奖概率(万分制)',
  position          tinyint(4)      not null                   comment '转盘排序/扇区序号，从1起可扩展',
  is_fallback       char(1)         default '0'                comment '是否兜底奖 0否 1是',
  assist_amount     decimal(18,4)   default null               comment '虚拟助力值额度(prize_type=3)',
  currency          varchar(16)     default 'CNY'              comment '虚拟入账币种',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (prize_id),
  key idx_lottery_prize_act (activity_id),
  key idx_lottery_prize_pool (pool_id),
  unique key uk_lottery_prize_position (activity_id, position)
) engine=innodb comment = '活动转盘奖项（引用奖品池+双库存）';

-- 兼容已建表：奖品池 + 活动奖项 pool_id
set @exist := (select count(*) from information_schema.tables where table_schema = database() and table_name = 'biz_lottery_prize_pool');
set @sql := if(@exist = 0, 'create table biz_lottery_prize_pool (
  pool_id bigint(20) not null auto_increment comment ''奖品池ID'',
  prize_name varchar(64) not null comment ''奖品名称'',
  prize_type tinyint(4) not null comment ''1实物 2现金 3虚拟资产'',
  assist_amount decimal(18,4) default null comment ''虚拟助力值额度'',
  currency varchar(16) default ''CNY'' comment ''虚拟入账币种'',
  sort int(4) default 0 comment ''排序'',
  status char(1) default ''0'' comment ''0正常 1停用'',
  create_by varchar(64) default '''' comment ''创建者'',
  create_time datetime comment ''创建时间'',
  update_by varchar(64) default '''' comment ''更新者'',
  update_time datetime comment ''更新时间'',
  remark varchar(500) default null comment ''备注'',
  primary key (pool_id)
) engine=innodb comment=''抽奖奖品池''', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'prize_desc');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column prize_desc varchar(128) default null comment ''奖品说明'' after prize_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'image_url');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column image_url varchar(512) default null comment ''奖品图片'' after prize_desc', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'sector_bg_color');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column sector_bg_color varchar(32) default ''#FFF8EC'' comment ''转盘扇区背景色'' after image_url', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'name_color');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column name_color varchar(32) default ''#111827'' comment ''名称字体颜色'' after sector_bg_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'desc_color');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column desc_color varchar(32) default ''#374151'' comment ''说明字体颜色'' after name_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize' and column_name = 'pool_id');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize add column pool_id bigint(20) default null comment ''奖品池ID'' after activity_id, add key idx_lottery_prize_pool (pool_id)', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

create table if not exists biz_lottery_win_strategy (
  strategy_id       bigint(20)      not null auto_increment    comment '策略ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  target_type       tinyint(4)      not null                   comment '1全员 2指定用户 3人群包',
  user_ids          text            default null               comment '指定会员ID逗号分隔(target_type=2)',
  package_id        bigint(20)      default null               comment '人群包ID(target_type=3)',
  target_prize_id   bigint(20)      not null                   comment '必中奖品ID',
  trigger_count     int(11)         not null                   comment '第X次抽奖必中',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (strategy_id),
  key idx_lottery_strategy_act (activity_id, trigger_count, status),
  key idx_lottery_strategy_pkg (package_id)
) engine=innodb comment = '大转盘步进必中策略';

-- 兼容已建表：必中策略库存模式
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_win_strategy' and column_name = 'stock_mode');
set @sql := if(@exist = 0, 'alter table biz_lottery_win_strategy add column stock_mode varchar(16) not null default ''AUTO'' comment ''必中扣库存 EXCLUSIVE/PUBLIC/AUTO'' after trigger_count', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;


-- ------------------------------------------------------------
-- C. 用户抽奖概况 + 流水（强幂等）
-- ------------------------------------------------------------

create table if not exists biz_lottery_member (
  id                bigint(20)      not null auto_increment    comment '主键',
  activity_id       bigint(20)      not null                   comment '活动ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  draw_count        int(11)         not null default 0         comment '累计抽奖次数(落库备份)',
  last_draw_time    datetime        default null               comment '最近抽奖时间',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_lottery_member (activity_id, member_id)
) engine=innodb comment = '会员抽奖资格概况';

create table if not exists biz_lottery_record (
  record_id         bigint(20)      not null auto_increment    comment '流水ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  draw_count        int(11)         not null                   comment '本次为第几次',
  prize_id          bigint(20)      default null               comment '奖品ID(熔断时可空)',
  prize_name        varchar(64)     default ''                 comment '奖品名称快照',
  prize_type        tinyint(4)      default null               comment '奖品类型快照',
  pool_type         varchar(16)     default null               comment 'PUBLIC公海 EXCLUSIVE专属',
  is_strategy       char(1)         default '0'                comment '是否步进必中 0否 1是',
  is_meltdown       char(1)         default '0'                comment '是否库存熔断 0否 1是',
  grant_status      char(1)         default '0'                comment '0待处理 1已入账 2待领取 3已关闭',
  biz_no            varchar(64)     default null               comment '幂等业务号',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (record_id),
  unique key uk_lottery_record_draw (activity_id, member_id, draw_count),
  unique key uk_lottery_record_biz (biz_no),
  key idx_lottery_record_member (member_id, create_time),
  key idx_lottery_record_act (activity_id, create_time)
) engine=innodb comment = '大转盘抽奖流水';

-- ------------------------------------------------------------
-- D. 入账规则（虚拟助力值走 ASSIST 钱包）
-- ------------------------------------------------------------

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time, remark)
select 'LOTTERY_ASSIST', '大转盘助力值', 'ASSIST', '1', 30, 'admin', sysdate(), '大转盘虚拟奖入账助力钱包'
from dual where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'LOTTERY_ASSIST');

-- ------------------------------------------------------------
-- E. 后台菜单（顶级「抽奖系统」，与运营中心同级）
-- ------------------------------------------------------------

delete from sys_role_menu where menu_id between 2405 and 2445;
delete from sys_menu where menu_id between 2405 and 2445;

-- 为插入 order_num=5 腾位（可重复执行：仅当 2405 不存在时移位，简化为固定重排业务菜单较危险，此处仅插入顶级）
-- 顶级目录
insert into sys_menu values(2405, '抽奖系统', 0, 5, 'lottery', null, '', '', 1, 0, 'M', '0', '0', '', 'guide', 'admin', sysdate(), '', null, '抽奖系统：人群、奖品、活动、记录');

insert into sys_menu values(2410, '标签人群', 2405, 1, 'lotteryCrowd', 'biz/lottery/crowdPackage/index', '', '', 1, 0, 'C', '0', '0', 'biz:crowdPackage:list', 'peoples', 'admin', sysdate(), '', null, '营销标签人群管理中心，全站复用');
insert into sys_menu values(2411, '人群查询', 2410, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:crowdPackage:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2412, '人群新增', 2410, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:crowdPackage:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2413, '人群修改', 2410, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:crowdPackage:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2414, '人群删除', 2410, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:crowdPackage:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2415, '奖品管理', 2405, 2, 'lotteryPrize', 'biz/lottery/prize/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryPrize:edit', 'shopping', 'admin', sysdate(), '', null, '独立奖品池：名称/类型/虚拟配置');
insert into sys_menu values(2416, '奖品增删改', 2415, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryPrize:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2420, '抽奖活动', 2405, 3, 'lotteryActivity', 'biz/lottery/activity/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryActivity:list', 'guide', 'admin', sysdate(), '', null, '抽奖活动/必中策略配置');
insert into sys_menu values(2421, '活动查询', 2420, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryActivity:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2422, '活动新增', 2420, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryActivity:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2423, '活动修改', 2420, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryActivity:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2424, '活动删除', 2420, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryActivity:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2425, '奖品配置', 2420, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryPrize:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2426, '必中策略', 2420, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryStrategy:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2430, '抽奖记录', 2405, 4, 'lotteryRecord', 'biz/lottery/record/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryRecord:list', 'log', 'admin', sysdate(), '', null, '大转盘抽奖流水查询');
insert into sys_menu values(2431, '记录查询', 2430, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryRecord:query', '#', 'admin', sysdate(), '', null, '');

-- 授权：已有运营中心或其子菜单权限的角色 + 超管
insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
cross join sys_menu m
where rm.menu_id in (select menu_id from sys_menu where menu_id = 2024 or parent_id = 2024 or menu_id = 2405 or parent_id = 2405)
  and m.menu_id between 2405 and 2442
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

insert into sys_role_menu (role_id, menu_id)
select 1, m.menu_id from sys_menu m
where m.menu_id between 2405 and 2442
  and not exists (select 1 from sys_role_menu x where x.role_id = 1 and x.menu_id = m.menu_id);


-- ------------------------------------------------------------
-- F. 获次规则 + 次数余额/流水
-- ------------------------------------------------------------

create table if not exists biz_lottery_chance_rule (
  rule_id           bigint(20)      not null auto_increment    comment '规则ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  rule_name         varchar(64)     not null                   comment '规则名称',
  checkin_days      int(11)         not null default 0         comment '需累计签到天数，0=不限制',
  streak_days int(11) NOT NULL DEFAULT 0 COMMENT '连续签到天数，0=不限制',
  invite_kyc_count  int(11)         not null default 0         comment '需直推实名人数，0=不限制',
  grant_amount      int(11)         not null default 1         comment '每档达标发放次数',
  once_only         char(1)         default '1'                comment '1仅发放一次 0按倍数可重复',
  max_repeat        int(11)         not null default 0         comment '重复最多档次：0=不限按倍数全发；>0=封顶；仅一次时为1',
  status            char(1)         default '0'                comment '0启用 1停用',
  sort              int(4)          default 0                  comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (rule_id),
  key idx_lottery_chance_rule_act (activity_id, status)
) engine=innodb comment='抽奖获次规则';

create table if not exists biz_lottery_chance (
  id                bigint(20)      not null auto_increment    comment '主键',
  activity_id       bigint(20)      not null                   comment '活动ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  balance           int(11)         not null default 0         comment '可用抽奖次数',
  total_granted     int(11)         not null default 0         comment '累计发放',
  total_consumed    int(11)         not null default 0         comment '累计消耗',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_lottery_chance (activity_id, member_id),
  key idx_lottery_chance_member (member_id)
) engine=innodb comment='会员抽奖次数余额';

create table if not exists biz_lottery_chance_log (
  log_id            bigint(20)      not null auto_increment    comment '流水ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  change_type       varchar(32)     not null                   comment 'RULE_GRANT/ADMIN_GRANT/ADMIN_DEDUCT/DRAW_CONSUME',
  change_amount     int(11)         not null                   comment '变动值，正增负减',
  balance_after     int(11)         not null                   comment '变动后余额',
  biz_no            varchar(64)     not null                   comment '幂等业务号',
  rule_id           bigint(20)      default null               comment '关联获次规则',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '操作者',
  create_time       datetime                                   comment '创建时间',
  primary key (log_id),
  unique key uk_lottery_chance_biz (biz_no),
  key idx_lottery_chance_log_m (member_id, create_time),
  key idx_lottery_chance_log_a (activity_id, create_time)
) engine=innodb comment='抽奖次数变动流水';

insert into sys_menu values(2432, '获次规则', 2405, 5, 'lotteryChanceRule', 'biz/lottery/chanceRule/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryChanceRule:list', 'edit', 'admin', sysdate(), '', null, '抽奖次数获得规则');
insert into sys_menu values(2433, '规则查询', 2432, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChanceRule:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2434, '规则新增', 2432, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChanceRule:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2435, '规则修改', 2432, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChanceRule:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2436, '规则删除', 2432, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChanceRule:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2440, '用户次数', 2405, 6, 'lotteryChance', 'biz/lottery/chance/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryChance:list', 'number', 'admin', sysdate(), '', null, '用户抽奖次数与流水');
insert into sys_menu values(2441, '次数查询', 2440, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChance:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2442, '次数调整', 2440, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChance:adjust', '#', 'admin', sysdate(), '', null, '');

-- 必中触发模式：ONCE仅第N次 / LOOP每N次循环
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_win_strategy' and column_name = 'loop_mode');
set @sql := if(@exist = 0, 'alter table biz_lottery_win_strategy add column loop_mode varchar(16) not null default ''ONCE'' comment ''触发模式 ONCE仅一次 LOOP每N次循环'' after trigger_count', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 奖品说明加长并支持多行
alter table biz_lottery_prize_pool modify column prize_desc varchar(500) default null comment '奖品说明，支持换行';
alter table biz_lottery_prize modify column prize_desc varchar(500) default null comment '奖品说明快照，支持换行';

-- 获次规则兼容：max_repeat（0=按倍数不限档；>0=最多 N 档；once_only=1 仅一次）
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_chance_rule' and column_name = 'max_repeat');
set @sql := if(@exist = 0, 'alter table biz_lottery_chance_rule add column max_repeat int(11) not null default 0 comment ''重复最多档次：0=不限按倍数全发；>0=封顶；仅一次时为1'' after once_only', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
alter table biz_lottery_chance_rule modify column once_only char(1) default '1' comment '1仅发放一次 0按倍数可重复';


-- 获次规则增加连续签到天数：0=不限制该项
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_chance_rule' and column_name = 'streak_days');
set @sql := if(@exist = 0, 'alter table biz_lottery_chance_rule add column streak_days int(11) not null default 0 comment ''连续签到天数，0=不限制'' after checkin_days', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;


-- 获次规则/用户次数菜单在建表之后才插入，这里补授权（含 2436、2440~2442）
insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
cross join sys_menu m
where rm.menu_id in (select menu_id from sys_menu where menu_id = 2024 or parent_id = 2024 or menu_id = 2405 or parent_id = 2405)
  and m.menu_id between 2405 and 2442
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

insert into sys_role_menu (role_id, menu_id)
select 1, m.menu_id from sys_menu m
where m.menu_id between 2405 and 2442
  and not exists (select 1 from sys_role_menu x where x.role_id = 1 and x.menu_id = m.menu_id);


-- -----------------------------------------------------------------------------
-- 8. 百乐支付 + 通道新增权限 + 累计结算份数字段
-- -----------------------------------------------------------------------------

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


-- -----------------------------------------------------------------------------
-- 9. 定时任务（三套并存，不再抢 102）
--     102 支付超时关单   payOrderExpireTask.execute()     每5分钟
--     103 链上充值扫链   chainDepositTask.scan()          每30秒
--     104 助力产品退本   assistPrincipalReturnTask.execute() 每天 00:10
-- -----------------------------------------------------------------------------
INSERT INTO sys_job
  (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
   create_by, create_time, remark)
SELECT CASE
         WHEN (SELECT COUNT(*) FROM sys_job WHERE job_id = 102) = 0 THEN 102
         WHEN (SELECT COUNT(*) FROM sys_job WHERE job_id = 105) = 0 THEN 105
         ELSE 107
       END,
       '支付超时关单', 'DEFAULT', 'payOrderExpireTask.execute()', '0 */5 * * * ?', '3', '1', '0',
       'admin', SYSDATE(), '待付超过 expire_time 自动关闭，并拒绝关联线上充值'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'payOrderExpireTask.execute()');

INSERT INTO sys_job
  (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
   create_by, create_time, remark)
SELECT CASE
         WHEN (SELECT COUNT(*) FROM sys_job WHERE job_id = 103) = 0 THEN 103
         WHEN (SELECT COUNT(*) FROM sys_job WHERE job_id = 106) = 0 THEN 106
         ELSE 108
       END,
       'USDT链上充值扫链', 'DEFAULT', 'chainDepositTask.scan()', '0/30 * * * * ?', '3', '1', '0',
       'admin', SYSDATE(), 'poll TronGrid USDT-TRC20 and BscScan USDT-BEP20 by amount fingerprint'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'chainDepositTask.scan()');

INSERT INTO sys_job
  (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
   create_by, create_time, remark)
SELECT CASE
         WHEN (SELECT COUNT(*) FROM sys_job WHERE job_id = 104) = 0 THEN 104
         WHEN (SELECT COUNT(*) FROM sys_job WHERE job_id = 109) = 0 THEN 109
         ELSE 110
       END,
       '助力产品退本', 'DEFAULT', 'assistPrincipalReturnTask.execute()', '0 10 0 * * ?', '3', '1', '0',
       'admin', SYSDATE(), 'ASSIST 订单到期本金退回 BALANCE（不可提现）'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'assistPrincipalReturnTask.execute()');

UPDATE sys_job
SET job_name = '支付超时关单',
    cron_expression = '0 */5 * * * ?',
    misfire_policy = '3',
    concurrent = '1',
    remark = '待付超过 expire_time 自动关闭，并拒绝关联线上充值'
WHERE invoke_target = 'payOrderExpireTask.execute()';

UPDATE sys_job
SET job_name = 'USDT链上充值扫链',
    cron_expression = '0/30 * * * * ?',
    misfire_policy = '3',
    concurrent = '1',
    remark = 'poll TronGrid USDT-TRC20 and BscScan USDT-BEP20 by amount fingerprint'
WHERE invoke_target = 'chainDepositTask.scan()';

UPDATE sys_job
SET job_name = '助力产品退本',
    cron_expression = '0 10 0 * * ?',
    misfire_policy = '3',
    concurrent = '1',
    remark = 'ASSIST 订单到期本金退回 BALANCE（不可提现）'
WHERE invoke_target = 'assistPrincipalReturnTask.execute()';

-- -----------------------------------------------------------------------------
-- 10. 链上充值菜单（2450~2454，避开卡片模板 2039）
-- -----------------------------------------------------------------------------
INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2450, '链上充值', 2032, 15, 'chainDeposit', 'biz/chainDeposit/index', '', '',
       1, 0, 'C', '0', '0', 'biz:chainDeposit:list', 'list', 'admin', SYSDATE(), 'USDT 指纹充值订单'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2450)
  AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2032);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2451, '链上充值查询', 2450, 1, '', '', '', '',
       1, 0, 'F', '0', '0', 'biz:chainDeposit:query', '#', 'admin', SYSDATE(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2451)
  AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2450);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2452, '链上充值列表', 2450, 2, '', '', '', '',
       1, 0, 'F', '0', '0', 'biz:chainDeposit:list', '#', 'admin', SYSDATE(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2452)
  AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2450);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2453, '链上模拟到账', 2450, 3, '', '', '', '',
       1, 0, 'F', '0', '0', 'biz:chainDeposit:simulate', '#', 'admin', SYSDATE(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2453)
  AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2450);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2454, '链上扫链', 2450, 4, '', '', '', '',
       1, 0, 'F', '0', '0', 'biz:chainDeposit:query', '#', 'admin', SYSDATE(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2454)
  AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2450);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, m.menu_id
FROM sys_role_menu rm
JOIN (SELECT 2450 AS menu_id UNION ALL SELECT 2451 UNION ALL SELECT 2452 UNION ALL SELECT 2453 UNION ALL SELECT 2454) m
WHERE rm.menu_id IN (2005, 2029)
  AND EXISTS (SELECT 1 FROM sys_menu x WHERE x.menu_id = m.menu_id)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = m.menu_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM (
  SELECT 2450 AS menu_id UNION ALL SELECT 2451 UNION ALL SELECT 2452 UNION ALL SELECT 2453 UNION ALL SELECT 2454
) m
WHERE EXISTS (SELECT 1 FROM sys_menu x WHERE x.menu_id = m.menu_id)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = 1 AND x.menu_id = m.menu_id);

-- -----------------------------------------------------------------------------
-- 11. 执行后核对
-- -----------------------------------------------------------------------------
SELECT job_id, job_name, invoke_target, cron_expression, status
FROM sys_job
WHERE invoke_target IN (
  'payOrderExpireTask.execute()',
  'chainDepositTask.scan()',
  'assistPrincipalReturnTask.execute()'
)
ORDER BY job_id;

SELECT menu_id, menu_name, parent_id, path, perms
FROM sys_menu
WHERE menu_id IN (2039, 2360, 2361, 2370, 2380, 2400, 2405, 2450, 2454, 2460, 2464)
ORDER BY menu_id;

SELECT config_key, LEFT(config_value, 40) AS config_value
FROM sys_config
WHERE config_key IN (
  'biz.pay.orderExpireMinutes',
  'biz.chain.tron.enabled', 'biz.chain.tron.apiKey',
  'biz.chain.bsc.enabled', 'biz.chain.bsc.apiKey', 'biz.chain.bsc.apiUrl'
)
ORDER BY config_key;

SELECT provider_code, provider_name, status FROM biz_pay_provider
WHERE provider_code IN ('fuwang','wuyou','feifan','baile')
ORDER BY provider_code;

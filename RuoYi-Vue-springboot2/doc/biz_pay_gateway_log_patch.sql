SET NAMES utf8mb4;

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

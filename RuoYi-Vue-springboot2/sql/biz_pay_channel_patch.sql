SET NAMES utf8mb4;
-- 线上代收：服务商 / 通道 / 支付单。当前全部 mock_mode=1，用模拟收银台跑通下单和回调入账。
-- 以后把 mock_mode 改成 0 并填真实网关，即可换成宝利/百付/牛付/沙付。

create table if not exists biz_pay_provider (
  provider_id       bigint(20)      not null auto_increment    comment '服务商ID',
  provider_code     varchar(32)     not null                   comment 'baifu/baoli/niupay/shapay/baoli_u',
  provider_name     varchar(64)     not null                   comment '显示名',
  adapter_family    varchar(32)     not null default 'monpay'  comment '适配器族 mock/monpay',
  gateway_url       varchar(255)    default ''                 comment '网关地址，模拟态可空',
  app_id            varchar(64)     default ''                 comment '商户号',
  secret_key        varchar(128)    default ''                 comment '签名密钥，模拟密钥不是生产密钥',
  mock_mode         char(1)         default '1'                comment '1模拟 0真实',
  status            char(1)         default '0'                comment '0正常 1停用',
  sort_order        int(4)          default 0                  comment '排序',
  remark            varchar(500)    default ''                 comment '备注',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (provider_id),
  unique key uk_biz_pay_provider_code (provider_code)
) engine=innodb comment = '支付服务商';

create table if not exists biz_pay_channel (
  channel_id        bigint(20)      not null auto_increment    comment '通道ID',
  provider_code     varchar(32)     not null                   comment '服务商编码',
  channel_code      varchar(64)     not null                   comment '业务通道编码',
  channel_name      varchar(64)     not null                   comment '通道名',
  display_name      varchar(64)     default ''                 comment 'App展示名',
  scene             varchar(16)     not null                   comment 'alipay/wechat/union/usdt',
  product_id        varchar(32)     default ''                 comment '三方产品ID',
  currency          varchar(16)     not null default 'CNY'     comment '入账币种',
  min_amount        decimal(18,4)   not null default 10        comment '最小金额',
  max_amount        decimal(18,4)   default null               comment '最大金额，空不限',
  weight            int(4)          default 100                comment '同场景权重',
  status            char(1)         default '0'                comment '0正常 1停用',
  sort_order        int(4)          default 0                  comment '排序',
  remark            varchar(500)    default ''                 comment '备注',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (channel_id),
  unique key uk_biz_pay_channel_code (channel_code),
  key idx_biz_pay_channel_scene (scene, status)
) engine=innodb comment = '支付通道';

create table if not exists biz_pay_order (
  pay_order_id      bigint(20)      not null auto_increment    comment '支付单ID',
  out_trade_no      varchar(64)     not null                   comment '商户订单号',
  recharge_id       bigint(20)      not null                   comment '充值单ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  provider_code     varchar(32)     not null                   comment '服务商',
  channel_code      varchar(64)     not null                   comment '通道',
  product_id        varchar(32)     default ''                 comment '三方产品ID',
  currency          varchar(16)     not null                   comment '币种',
  amount            decimal(18,4)   not null                   comment '入账金额',
  provider_amount   decimal(18,4)   not null                   comment '向三方下单金额',
  status            char(1)         default '0'                comment '0待付 1成功 2失败 3关闭',
  pay_type          varchar(16)     default 'url'              comment 'url/qr',
  pay_url           varchar(500)    default ''                 comment '支付跳转地址',
  provider_trade_no varchar(64)     default ''                 comment '三方单号',
  notify_payload    varchar(2000)   default ''                 comment '最近一次回调原文',
  expire_time       datetime                                   comment '过期时间',
  paid_time         datetime                                   comment '支付成功时间',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (pay_order_id),
  unique key uk_biz_pay_order_out (out_trade_no),
  key idx_biz_pay_order_member (member_id, status),
  key idx_biz_pay_order_recharge (recharge_id)
) engine=innodb comment = '线上支付单';

-- 充值单补线上字段（可重复执行）
DROP PROCEDURE IF EXISTS patch_biz_recharge_pay;
DELIMITER $$
CREATE PROCEDURE patch_biz_recharge_pay()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_recharge' AND COLUMN_NAME = 'pay_mode'
  ) THEN
    ALTER TABLE biz_recharge ADD COLUMN pay_mode char(1) default '0' comment '0人工 1线上' AFTER remark;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_recharge' AND COLUMN_NAME = 'channel_code'
  ) THEN
    ALTER TABLE biz_recharge ADD COLUMN channel_code varchar(64) default '' comment '支付通道' AFTER pay_mode;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_recharge' AND COLUMN_NAME = 'out_trade_no'
  ) THEN
    ALTER TABLE biz_recharge ADD COLUMN out_trade_no varchar(64) default '' comment '线上商户单号' AFTER channel_code;
  END IF;
END$$
DELIMITER ;
CALL patch_biz_recharge_pay();
DROP PROCEDURE IF EXISTS patch_biz_recharge_pay;

insert into biz_pay_provider (provider_code, provider_name, adapter_family, gateway_url, app_id, secret_key, mock_mode, status, sort_order, remark, create_time)
select * from (
  select 'baifu' as provider_code, '百付' as provider_name, 'monpay' as adapter_family, 'https://mock.pay.local/baifu' as gateway_url, 'mock_baifu_app' as app_id, 'mock_baifu_secret' as secret_key, '1' as mock_mode, '0' as status, 1 as sort_order, '模拟：支付宝/微信/银联' as remark, sysdate() as create_time
  union all select 'baoli', '宝利', 'monpay', 'https://mock.pay.local/baoli', 'mock_baoli_app', 'mock_baoli_secret', '1', '0', 2, '模拟：支付宝/微信', sysdate()
  union all select 'niupay', '牛付', 'monpay', 'https://mock.pay.local/niupay', 'mock_niupay_app', 'mock_niupay_secret', '1', '0', 3, '模拟：支付宝/微信', sysdate()
  union all select 'shapay', '沙付', 'monpay', 'https://mock.pay.local/shapay', 'mock_shapay_app', 'mock_shapay_secret', '1', '0', 4, '模拟：支付宝/微信', sysdate()
  union all select 'baoli_u', '宝利U', 'monpay', 'https://mock.pay.local/baoli-u', 'mock_baoli_u_app', 'mock_baoli_u_secret', '1', '0', 5, '模拟：USDT代收', sysdate()
) t
where not exists (select 1 from biz_pay_provider p where p.provider_code = t.provider_code);

insert into biz_pay_channel (provider_code, channel_code, channel_name, display_name, scene, product_id, currency, min_amount, max_amount, weight, status, sort_order, create_time)
select * from (
  select 'baifu' as provider_code,'BAIFU_ALIPAY' as channel_code,'支付宝' as channel_name,'百付支付宝' as display_name,'alipay' as scene,'8801' as product_id,'CNY' as currency,10 as min_amount,50000 as max_amount,100 as weight,'0' as status,1 as sort_order,sysdate() as create_time
  union all select 'baifu','BAIFU_WECHAT','微信','百付微信','wechat','8802','CNY',10,50000,100,'0',2,sysdate()
  union all select 'baifu','BAIFU_UNION','银联快捷','百付银联','union','8808','CNY',10,50000,80,'0',3,sysdate()
  union all select 'baoli','BAOLI_ALIPAY','支付宝','宝利支付宝','alipay','8801','CNY',10,50000,90,'0',1,sysdate()
  union all select 'baoli','BAOLI_WECHAT','微信','宝利微信','wechat','8802','CNY',10,50000,90,'0',2,sysdate()
  union all select 'niupay','NIUPAY_ALIPAY','支付宝','牛付支付宝','alipay','8801','CNY',10,50000,80,'0',1,sysdate()
  union all select 'niupay','NIUPAY_WECHAT','微信','牛付微信','wechat','8802','CNY',10,50000,80,'0',2,sysdate()
  union all select 'shapay','SHAPAY_ALIPAY','支付宝','沙付支付宝','alipay','8801','CNY',10,50000,70,'0',1,sysdate()
  union all select 'shapay','SHAPAY_WECHAT','微信','沙付微信','wechat','8802','CNY',10,50000,70,'0',2,sysdate()
  union all select 'baoli_u','BAOLI_U_DEPOSIT','USDT代收','宝利U充值','usdt','30','USDT',10,20000,100,'0',1,sysdate()
) t
where not exists (select 1 from biz_pay_channel c where c.channel_code = t.channel_code);

delete from sys_role_menu where menu_id in (2028, 2029, 2307, 2308, 2309, 2310, 2311, 2312);
delete from sys_menu where menu_id in (2028, 2029, 2307, 2308, 2309, 2310, 2311, 2312);

insert into sys_menu values('2028', '支付通道', '2000', '15', 'payChannel', 'biz/payChannel/index', '', '', 1, 0, 'C', '0', '0', 'biz:payChannel:list', 'server', 'admin', sysdate(), '', null, '百付/宝利/牛付/沙付通道，当前为模拟');
insert into sys_menu values('2307', '通道查询', '2028', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payChannel:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2308', '通道修改', '2028', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payChannel:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2309', '服务商修改', '2028', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2029', '支付订单', '2000', '16', 'payOrder', 'biz/payOrder/index', '', '', 1, 0, 'C', '0', '0', 'biz:payOrder:list', 'list', 'admin', sysdate(), '', null, '线上代收单，模拟可点到账');
insert into sys_menu values('2310', '订单查询', '2029', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payOrder:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2311', '模拟到账', '2029', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payOrder:simulate', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2312', '服务商查询', '2028', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:list', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2028 as menu_id union all select 2029 union all select 2307 union all select 2308
  union all select 2309 union all select 2310 union all select 2311 union all select 2312
) m
where rm.menu_id = 2005
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

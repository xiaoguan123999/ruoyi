SET NAMES utf8mb4;
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

delete from sys_job where job_id = 102;
insert into sys_job values(102, 'USDT chain deposit scan', 'DEFAULT', 'chainDepositTask.scan()',
  '0/30 * * * * ?', '3', '1', '0', 'admin', sysdate(), '', null, 'poll TronGrid USDT-TRC20 by amount fingerprint');

delete from sys_role_menu where menu_id in (2039, 2360, 2361, 2362, 2363);
delete from sys_menu where menu_id in (2039, 2360, 2361, 2362, 2363);

insert into sys_menu values('2039', '链上充值', '2032', '15', 'chainDeposit', 'biz/chainDeposit/index', '', '', 1, 0, 'C', '0', '0', 'biz:chainDeposit:list', 'list', 'admin', sysdate(), '', null, 'TRON USDT fingerprint orders');
insert into sys_menu values('2360', '链上充值查询', '2039', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:chainDeposit:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2361', '链上充值列表', '2039', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:chainDeposit:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2362', '链上模拟到账', '2039', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:chainDeposit:simulate', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2363', '链上扫链', '2039', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:chainDeposit:query', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2039 as menu_id union all select 2360 union all select 2361 union all select 2362 union all select 2363
) m
where rm.menu_id in (2005, 2029)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

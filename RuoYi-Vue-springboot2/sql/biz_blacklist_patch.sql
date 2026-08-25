SET NAMES utf8mb4;
-- 黑名单：姓名/手机/身份证/银行卡，拦截登录、注册、实名、绑卡，并记拦截记录

create table if not exists biz_blacklist (
  blacklist_id      bigint(20)      not null auto_increment    comment '黑名单ID',
  real_name         varchar(50)     default ''                 comment '姓名',
  phone             varchar(20)     default ''                 comment '手机号',
  id_card           varchar(32)     default ''                 comment '身份证号',
  bank_card         varchar(64)     default ''                 comment '银行卡号',
  status            char(1)         default '0'                comment '状态（0启用 1停用）',
  remark            varchar(500)    default ''                 comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (blacklist_id),
  key idx_biz_blacklist_phone (phone),
  key idx_biz_blacklist_id_card (id_card),
  key idx_biz_blacklist_bank (bank_card)
) engine=innodb comment = '黑名单';

create table if not exists biz_blacklist_log (
  log_id            bigint(20)      not null auto_increment    comment '记录ID',
  blacklist_id      bigint(20)      default null               comment '命中的黑名单ID',
  action            varchar(16)     not null                   comment 'LOGIN/REGISTER/KYC/BANK',
  hit_type          varchar(16)     not null                   comment 'PHONE/ID_CARD/BANK_CARD',
  hit_value         varchar(64)     default ''                 comment '命中值',
  member_id         bigint(20)      default null               comment '会员ID',
  phone             varchar(20)     default ''                 comment '当时手机号',
  real_name         varchar(50)     default ''                 comment '当时姓名',
  remark            varchar(500)    default ''                 comment '备注',
  create_time       datetime                                   comment '拦截时间',
  primary key (log_id),
  key idx_biz_blacklist_log_action (action),
  key idx_biz_blacklist_log_phone (phone),
  key idx_biz_blacklist_log_time (create_time)
) engine=innodb comment = '黑名单拦截记录';

delete from sys_role_menu where menu_id in (2026, 2027, 2301, 2302, 2303, 2304, 2305, 2306);
delete from sys_menu where menu_id in (2026, 2027, 2301, 2302, 2303, 2304, 2305, 2306);

insert into sys_menu values('2026', '黑名单', '2000', '13', 'blacklist', 'biz/blacklist/index', '', '', 1, 0, 'C', '0', '0', 'biz:blacklist:list', 'lock', 'admin', sysdate(), '', null, '拦截登录、注册、实名、绑卡');
insert into sys_menu values('2301', '黑名单查询', '2026', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklist:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2302', '黑名单新增', '2026', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklist:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2303', '黑名单修改', '2026', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklist:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2304', '黑名单删除', '2026', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklist:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2027', '黑名单记录', '2000', '14', 'blacklistLog', 'biz/blacklist/log', '', '', 1, 0, 'C', '0', '0', 'biz:blacklistLog:list', 'log', 'admin', sysdate(), '', null, '黑名单拦截记录');
insert into sys_menu values('2305', '记录查询', '2027', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklistLog:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2306', '记录删除', '2027', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklistLog:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2026 as menu_id union all select 2027 union all select 2301 union all select 2302
  union all select 2303 union all select 2304 union all select 2305 union all select 2306
) m
where rm.menu_id = 2001
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

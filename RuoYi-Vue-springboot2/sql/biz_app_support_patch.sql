-- App 收款账户、客服中心、七级团队（可重复执行时加表用 if not exists）

create table if not exists biz_pay_account (
  account_id        bigint(20)      not null auto_increment    comment '账户ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  account_type      varchar(20)     not null                   comment 'USDT/BANK/ALIPAY',
  account_name      varchar(100)    default ''                 comment '户名/实名',
  account_no        varchar(255)    not null                   comment '卡号/支付宝账号/USDT地址',
  bank_name         varchar(100)    default ''                 comment '银行名称',
  network           varchar(30)     default ''                 comment 'USDT网络 TRC20/ERC20',
  is_default        char(1)         default '0'                comment '是否默认 1是 0否',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (account_id),
  key idx_pay_account_member (member_id, account_type)
) engine=innodb comment = '会员收款账户';

create table if not exists biz_cs_channel (
  channel_id        bigint(20)      not null auto_increment    comment '渠道ID',
  name              varchar(50)     not null                   comment '名称',
  channel_type      varchar(20)     default 'WECHAT'           comment 'PHONE/WECHAT/TELEGRAM/QQ/LINK/QR',
  value             varchar(255)    default ''                 comment '手机号/微信号/链接',
  qr_url            varchar(500)    default ''                 comment '二维码图片',
  link_url          varchar(500)    default ''                 comment '点击跳转',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '0显示 1隐藏',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (channel_id)
) engine=innodb comment = '客服渠道';

delete from sys_config where config_id between 48 and 50;
insert into sys_config values(48, '客服中心标题', 'biz.service.title', '客服中心', 'N', 'admin', sysdate(), '', null, 'App客服中心标题');
insert into sys_config values(49, '客服工作时间', 'biz.service.workTime', '09:00 - 21:00', 'N', 'admin', sysdate(), '', null, 'App客服工作时间');
insert into sys_config values(50, '客服提示文案', 'biz.service.hint', '通道拥堵可联系在线客服', 'N', 'admin', sysdate(), '', null, 'App客服说明');

insert into biz_cs_channel (name, channel_type, value, qr_url, link_url, sort, status, create_by, create_time, remark)
select '微信客服', 'WECHAT', '', '', '', 1, '0', 'admin', sysdate(), '请上传客服二维码或填写微信号'
from dual where not exists (select 1 from biz_cs_channel limit 1);

delete from sys_menu where menu_id in (2021, 2022, 2281, 2282, 2283, 2284, 2291, 2292, 2293, 2294);
insert into sys_menu values('2021', '收款账户', '2000', '11', 'payAccount', 'biz/payAccount/index', '', '', 1, 0, 'C', '0', '0', 'biz:payAccount:list', 'wallet', 'admin', sysdate(), '', null, '会员USDT/银行卡/支付宝收款账户');
insert into sys_menu values('2022', '客服中心', '2000', '0', 'service', 'biz/service/index', '', '', 1, 0, 'C', '0', '0', 'biz:service:list', 'service', 'admin', sysdate(), '', null, 'App联系客服渠道');
insert into sys_menu values('2281', '账户查询', '2021', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payAccount:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2282', '账户新增', '2021', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payAccount:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2283', '账户修改', '2021', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payAccount:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2284', '账户删除', '2021', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payAccount:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2291', '客服查询', '2022', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:service:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2292', '客服新增', '2022', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:service:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2293', '客服修改', '2022', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:service:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2294', '客服删除', '2022', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:service:remove', '#', 'admin', sysdate(), '', null, '');

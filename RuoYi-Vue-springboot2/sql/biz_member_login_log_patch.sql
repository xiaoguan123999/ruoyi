SET NAMES utf8mb4;
-- 会员登录日志（可重复执行）

create table if not exists biz_member_logininfor (
  info_id        bigint(20)     not null auto_increment   comment '访问ID',
  member_id      bigint(20)     default null              comment '会员ID',
  phone          varchar(20)    default ''                comment '手机号',
  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
  ipaddr         varchar(128)   default ''                comment '登录IP地址',
  login_location varchar(255)   default ''                comment '登录地点',
  browser        varchar(50)    default ''                comment '浏览器类型',
  os             varchar(50)    default ''                comment '操作系统',
  msg            varchar(255)   default ''                comment '提示消息',
  login_time     datetime                                 comment '访问时间',
  primary key (info_id),
  key idx_biz_member_login_phone (phone),
  key idx_biz_member_login_member (member_id),
  key idx_biz_member_login_s (status),
  key idx_biz_member_login_lt (login_time)
) engine=innodb comment = '会员登录日志';

delete from sys_role_menu where menu_id in (2034, 2330, 2331, 2332);
delete from sys_menu where menu_id in (2034, 2330, 2331, 2332);

insert into sys_menu values('2034', '会员登录日志', '2000', '2', 'memberLogin', 'biz/memberLogin/index', '', '', 1, 0, 'C', '0', '0', 'biz:memberLogin:list', 'logininfor', 'admin', sysdate(), '', null, 'App会员登录、注册、退出记录');
insert into sys_menu values('2330', '登录查询', '2034', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:memberLogin:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2331', '登录删除', '2034', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:memberLogin:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2332', '登录导出', '2034', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:memberLogin:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2034 as menu_id union all select 2330 union all select 2331 union all select 2332
) m
where rm.menu_id = 2001
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

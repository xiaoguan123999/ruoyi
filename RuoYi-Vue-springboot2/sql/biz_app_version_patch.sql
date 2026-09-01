SET NAMES utf8mb4;
-- App 版本管理。可重复执行。

create table if not exists biz_app_version (
  version_id        bigint(20)      not null auto_increment    comment '版本ID',
  platform          varchar(16)     not null                   comment 'android/ios',
  version           varchar(32)     not null                   comment '版本号，如 1.0.11',
  download_url      varchar(500)    not null                   comment '下载链接',
  description       varchar(1000)   default ''                 comment '版本说明',
  force_update      char(1)         not null default '0'       comment '强制更新 1是 0否',
  is_latest         char(1)         not null default '0'       comment '最新版本 1是 0否，同平台最多一条',
  is_enabled        char(1)         not null default '1'       comment '启用 1是 0否',
  sort_order        int(11)         not null default 0         comment '排序，越大越靠前',
  del_flag          char(1)         not null default '0'       comment '删除 0存在 1删除',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (version_id),
  key idx_biz_app_version_platform (platform, is_latest, is_enabled, del_flag)
) engine=innodb comment = 'App版本';

delete from sys_role_menu where menu_id in (2038, 2350, 2351, 2352, 2353, 2354);
delete from sys_menu where menu_id in (2038, 2350, 2351, 2352, 2353, 2354);

insert into sys_menu values('2038', '版本管理', '2024', '8', 'appVersion', 'biz/appVersion/index', '', '', 1, 0, 'C', '0', '0', 'biz:appVersion:list', 'guide', 'admin', sysdate(), '', null, 'App下载链接、强制更新、最新版本');
insert into sys_menu values('2350', '版本查询', '2038', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2351', '版本新增', '2038', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2352', '版本修改', '2038', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2353', '版本删除', '2038', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2354', '版本开关', '2038', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2038 as menu_id union all select 2350 union all select 2351 union all select 2352 union all select 2353 union all select 2354
) m
where rm.menu_id = 2016
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

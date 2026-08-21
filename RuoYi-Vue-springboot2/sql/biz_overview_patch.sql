-- App 首页运行概览（展示用，后台手改，可重复执行）
create table if not exists biz_overview (
  item_id           bigint(20)      not null auto_increment    comment '卡片ID',
  item_key          varchar(32)     not null                   comment '卡片标识，App用它匹配本地图',
  title             varchar(64)     not null                   comment '标题',
  display_value     varchar(64)     not null                   comment '展示数值，含单位',
  status_text       varchar(64)     default ''                 comment '状态文案',
  status_color      varchar(16)     default '#4DA3FF'          comment '状态点颜色',
  image_url         varchar(500)    default ''                 comment '可选配图，空则App用本地图',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (item_id),
  unique key uk_biz_overview_key (item_key)
) engine=innodb comment = 'App运行概览';

insert into biz_overview (item_key, title, display_value, status_text, status_color, image_url, sort, status, create_by, create_time)
select 'satellite', '在轨卫星', '320 颗', '正常运行', '#3DDC84', '', 1, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_overview where item_key = 'satellite');

insert into biz_overview (item_key, title, display_value, status_text, status_color, image_url, sort, status, create_by, create_time)
select 'coverage', '覆盖国家/地区', '150+', '正常运行', '#4DA3FF', '', 2, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_overview where item_key = 'coverage');

insert into biz_overview (item_key, title, display_value, status_text, status_color, image_url, sort, status, create_by, create_time)
select 'terminal', '在线终端', '1256000+', '稳定连接', '#4DA3FF', '', 3, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_overview where item_key = 'terminal');

delete from sys_menu where menu_id in (2013, 2201, 2202, 2203, 2204);
insert into sys_menu values('2013', '运行概览', '2000', '0', 'overview', 'biz/overview/index', '', '', 1, 0, 'C', '0', '0', 'biz:overview:list', 'dashboard', 'admin', sysdate(), '', null, 'App首页展示数字，后台手改');
insert into sys_menu values('2201', '概览查询', '2013', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2202', '概览新增', '2013', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2203', '概览修改', '2013', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2204', '概览删除', '2013', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:remove', '#', 'admin', sysdate(), '', null, '');

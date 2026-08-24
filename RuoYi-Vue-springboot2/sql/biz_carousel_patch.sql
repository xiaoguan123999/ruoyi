-- App 首页视频轮播（后台手改，可重复执行）
create table if not exists biz_carousel (
  carousel_id       bigint(20)      not null auto_increment    comment '轮播ID',
  title             varchar(100)    default ''                 comment '后台备注标题',
  video_url         varchar(500)    not null                   comment '视频地址',
  cover_url         varchar(500)    default ''                 comment '封面图，未播前展示',
  sort              int(4)          default 0                  comment '排序，越小越靠前',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (carousel_id)
) engine=innodb comment = 'App首页视频轮播';

delete from sys_menu where menu_id in (2018, 2251, 2252, 2253, 2254);
insert into sys_menu values('2018', '视频轮播', '2024', '2', 'carousel', 'biz/carousel/index', '', '', 1, 0, 'C', '0', '0', 'biz:carousel:list', 'example', 'admin', sysdate(), '', null, 'App首页视频轮播');
insert into sys_menu values('2251', '轮播查询', '2018', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2252', '轮播新增', '2018', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2253', '轮播修改', '2018', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2254', '轮播删除', '2018', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:remove', '#', 'admin', sysdate(), '', null, '');

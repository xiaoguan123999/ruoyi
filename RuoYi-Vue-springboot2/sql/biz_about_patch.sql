-- App 关于我们（展示用，后台手改，可重复执行）
create table if not exists biz_about (
  about_id          bigint(20)      not null auto_increment    comment '内容ID',
  title             varchar(100)    not null                   comment '标题',
  subtitle          varchar(200)    default ''                 comment '副标题',
  content           mediumtext                                 comment '正文，后台富文本',
  image_url         varchar(500)    default ''                 comment '可选配图',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (about_id)
) engine=innodb comment = 'App关于我们';

insert into biz_about (title, subtitle, content, image_url, sort, status, create_by, create_time)
select '星帆智联', '连接星空 · 智联未来', '<p>星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。</p>', '', 1, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_about limit 1);

delete from sys_menu where menu_id in (2014, 2211, 2212, 2213, 2214);
insert into sys_menu values('2014', '关于我们', '2000', '0', 'about', 'biz/about/index', '', '', 1, 0, 'C', '0', '0', 'biz:about:list', 'guide', 'admin', sysdate(), '', null, 'App关于我们，后台手改');
insert into sys_menu values('2211', '关于查询', '2014', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2212', '关于新增', '2014', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2213', '关于修改', '2014', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2214', '关于删除', '2014', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:remove', '#', 'admin', sysdate(), '', null, '');

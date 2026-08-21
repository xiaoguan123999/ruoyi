-- App 新闻资讯（展示用，后台手改，可重复执行）
create table if not exists biz_news (
  news_id           bigint(20)      not null auto_increment    comment '新闻ID',
  title             varchar(200)    not null                   comment '标题',
  summary           varchar(500)    default ''                 comment '摘要',
  cover_url         varchar(500)    default ''                 comment '封面图',
  content           mediumtext                                 comment '正文，后台富文本',
  publish_time      datetime                                   comment '发布日期',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (news_id)
) engine=innodb comment = 'App新闻资讯';

insert into biz_news (title, summary, cover_url, content, publish_time, sort, status, create_by, create_time)
select '俄罗斯近24小时遥感卫星观测任务与行业应用动态',
       '俄罗斯近24小时遥感卫星观测任务与行业应用动态',
       '',
       '<p>一、在轨遥感星座整体运行工况平稳</p><p>（一）高分辨率光学卫星完成农情、地质重点区域成像。</p><p>（二）雷达卫星持续开展全天候云雨覆盖区域观测。</p><p>二、行业应用动态</p><p>面向应急、农业、交通等场景的数据产品按计划分发，支撑多地业务系统稳定运行。</p>',
       '2026-08-18 00:00:00', 1, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_news where title = '俄罗斯近24小时遥感卫星观测任务与行业应用动态');

insert into biz_news (title, summary, cover_url, content, publish_time, sort, status, create_by, create_time)
select '商业航天星座组网加速，行业应用场景持续拓展',
       '商业航天星座组网加速，行业应用场景持续拓展',
       '',
       '<p>商业航天正从单星验证走向规模组网。星帆智联持续推进星座部署与地面终端协同，为行业用户提供稳定连接能力。</p>',
       '2026-08-12 00:00:00', 2, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_news where title = '商业航天星座组网加速，行业应用场景持续拓展');

delete from sys_menu where menu_id in (2016, 2231, 2232, 2233, 2234);
insert into sys_menu values('2016', '新闻资讯', '2000', '0', 'news', 'biz/news/index', '', '', 1, 0, 'C', '0', '0', 'biz:news:list', 'documentation', 'admin', sysdate(), '', null, 'App新闻资讯，后台手改');
insert into sys_menu values('2231', '新闻查询', '2016', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2232', '新闻新增', '2016', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2233', '新闻修改', '2016', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2234', '新闻删除', '2016', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:remove', '#', 'admin', sysdate(), '', null, '');

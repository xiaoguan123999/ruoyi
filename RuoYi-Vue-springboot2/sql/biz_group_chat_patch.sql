-- App 官方群聊（展示用，后台上传二维码，可重复执行）
create table if not exists biz_group_chat (
  group_id          bigint(20)      not null auto_increment    comment '群聊ID',
  title             varchar(100)    not null                   comment '标题',
  hint              varchar(100)    default '扫码进群'          comment '二维码下方提示',
  qr_url            varchar(500)    default ''                 comment '群聊二维码图片地址',
  remark            varchar(500)    default ''                 comment '补充说明',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (group_id)
) engine=innodb comment = 'App官方群聊';

insert into biz_group_chat (title, hint, qr_url, remark, sort, status, create_by, create_time)
select '官方群聊', '扫码进群', '', '', 1, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_group_chat limit 1);

delete from sys_menu where menu_id in (2015, 2221, 2222, 2223, 2224);
insert into sys_menu values('2015', '官方群聊', '2024', '4', 'groupChat', 'biz/groupChat/index', '', '', 1, 0, 'C', '0', '0', 'biz:group:list', 'message', 'admin', sysdate(), '', null, 'App官方群聊二维码，后台手改');
insert into sys_menu values('2221', '群聊查询', '2015', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2222', '群聊新增', '2015', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2223', '群聊修改', '2015', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2224', '群聊删除', '2015', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:remove', '#', 'admin', sysdate(), '', null, '');

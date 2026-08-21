-- App 产品系列（后台叫产品分类）。Tab 渲染系列，点进去查该系列下产品。
-- 可重复执行：建表/菜单用 not exists；加列若已存在会报错可忽略。

create table if not exists biz_product_category (
  category_id       bigint(20)      not null auto_increment    comment '分类/系列ID',
  category_name     varchar(100)    not null                   comment '系列名称',
  cover_url         varchar(500)    default ''                 comment '封面图，Tab 卡片用',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  sort              int(4)          default 0                  comment '排序，越小越靠前',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (category_id)
) engine=innodb comment = '产品分类/系列';

insert into biz_product_category (category_id, category_name, cover_url, status, sort, create_by, create_time, remark)
select 1, '「星帆·天启计划」', '', '0', 1, 'admin', sysdate(), 'App产品Tab系列'
from dual where not exists (select 1 from biz_product_category where category_id = 1);

insert into biz_product_category (category_id, category_name, cover_url, status, sort, create_by, create_time, remark)
select 2, '「星帆·远征计划」', '', '0', 2, 'admin', sysdate(), 'App产品Tab系列'
from dual where not exists (select 1 from biz_product_category where category_id = 2);

alter table biz_product add column category_id bigint(20) default null comment '所属分类/系列ID' after product_name;
alter table biz_product add column name_en varchar(100) default '' comment '英文名' after product_name;
alter table biz_product add column cover_url varchar(500) default '' comment '产品封面图' after sort;

update biz_product set category_id = 1 where category_id is null;

delete from sys_menu where menu_id in (2017, 2241, 2242, 2243, 2244);
insert into sys_menu values('2017', '产品分类', '2000', '2', 'productCategory', 'biz/productCategory/index', '', '', 1, 0, 'C', '0', '0', 'biz:productCategory:list', 'cascader', 'admin', sysdate(), '', null, 'App产品Tab上的系列');
insert into sys_menu values('2241', '分类查询', '2017', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCategory:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2242', '分类新增', '2017', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCategory:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2243', '分类修改', '2017', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCategory:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2244', '分类删除', '2017', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCategory:remove', '#', 'admin', sysdate(), '', null, '');

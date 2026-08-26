SET NAMES utf8mb4;
-- 团队查询 / 会员结构图 / 推荐关系图（可重复执行）
-- 结构图：从某会员往下的直推树。关系图：从网体顶点到该会员的路径，列表为同级直推并标出路径上的人。

update sys_menu set menu_name = '团队查询', remark = '按会员查看1-7级下线与汇总' where menu_id = 2008;

delete from sys_role_menu where menu_id in (2030, 2031, 2313, 2314, 2315);
delete from sys_menu where menu_id in (2030, 2031, 2313, 2314, 2315);

insert into sys_menu values('2030', '会员结构图', '2000', '8', 'teamTree', 'biz/teamTree/index', '', '', 1, 0, 'C', '0', '0', 'biz:team:tree', 'tree-table', 'admin', sysdate(), '', null, '按手机号查看该会员下级树');
insert into sys_menu values('2313', '结构图查询', '2030', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:team:tree', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2031', '推荐关系图', '2000', '8', 'teamRelation', 'biz/teamRelation/index', '', '', 1, 0, 'C', '0', '0', 'biz:team:relation', 'nested', 'admin', sysdate(), '', null, '从顶点到该会员的推荐路径');
insert into sys_menu values('2314', '关系图查询', '2031', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:team:relation', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2315', '关系图导出', '2031', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:team:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2030 as menu_id union all select 2031 union all select 2313 union all select 2314 union all select 2315
) m
where rm.menu_id = 2008
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

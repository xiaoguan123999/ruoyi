SET NAMES utf8mb4;
-- 资金中心目录 + 支付供应商菜单（可重复执行）
-- 把充值/提现/流水/结构图/关系图/等级奖励/分佣/收款账户/通道/订单归到资金中心。

delete from sys_role_menu where menu_id in (2032, 2033, 2316, 2317, 2318);
delete from sys_menu where menu_id in (2032, 2033, 2316, 2317, 2318);

insert into sys_menu values('2032', '资金中心', '0', '6', 'fund', null, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', null, '充值提现、支付通道与资金记录');
update sys_menu set order_num = 7 where menu_id = 2025;
update sys_menu set order_num = 8 where menu_id = 2024;

update sys_menu set parent_id = 2032, order_num = 1 where menu_id = 2005;
update sys_menu set parent_id = 2032, order_num = 2 where menu_id = 2006;
update sys_menu set parent_id = 2032, order_num = 3 where menu_id = 2007;
update sys_menu set parent_id = 2032, order_num = 4 where menu_id = 2031;
update sys_menu set parent_id = 2032, order_num = 5 where menu_id = 2030;
update sys_menu set parent_id = 2032, order_num = 6 where menu_id = 2020;
update sys_menu set parent_id = 2032, order_num = 7 where menu_id = 2019;
update sys_menu set parent_id = 2032, order_num = 8 where menu_id = 2010;
update sys_menu set parent_id = 2032, order_num = 9 where menu_id = 2021;
insert into sys_menu values('2033', '供应商', '2032', '10', 'payProvider', 'biz/payProvider/index', '', '', 1, 0, 'C', '0', '0', 'biz:payProvider:list', 'server', 'admin', sysdate(), '', null, '代收服务商：百付/宝利/牛付/沙付，当前模拟');
insert into sys_menu values('2316', '供应商查询', '2033', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2317', '供应商修改', '2033', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2318', '供应商列表权限', '2033', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:list', '#', 'admin', sysdate(), '', null, '');
update sys_menu set parent_id = 2032, order_num = 11 where menu_id = 2028;
update sys_menu set parent_id = 2032, order_num = 12 where menu_id = 2029;

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2032 as menu_id union all select 2033 union all select 2316 union all select 2317 union all select 2318
) m
where rm.menu_id in (2005, 2028)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

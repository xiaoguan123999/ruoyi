SET NAMES utf8mb4;
-- 产品交易目录：产品分类、产品管理、认购订单、签到记录（可重复执行）
delete from sys_role_menu where menu_id = 2025;
delete from sys_menu where menu_id = 2025;
insert into sys_menu values('2025', '产品交易', '0', '6', 'trade', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, '产品、认购与签到目录');
update sys_menu set order_num = 7 where menu_id = 2024;

update sys_menu set parent_id = 2025, order_num = 1 where menu_id = 2017;
update sys_menu set parent_id = 2025, order_num = 2 where menu_id = 2002;
update sys_menu set parent_id = 2025, order_num = 3 where menu_id = 2003;
update sys_menu set parent_id = 2025, order_num = 4 where menu_id = 2004;

insert into sys_role_menu (role_id, menu_id)
select distinct role_id, 2025 from sys_role_menu
where menu_id in (2017, 2002, 2003, 2004);

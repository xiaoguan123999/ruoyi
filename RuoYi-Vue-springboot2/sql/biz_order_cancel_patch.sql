SET NAMES utf8mb4;
-- Admin cancel subscribe. Repeatable. menu_id 2122 under »œπ∫∂©µ• 2003.

delete from sys_role_menu where menu_id = 2122;
delete from sys_menu where menu_id = 2122;

insert into sys_menu values('2122', 'Cancel order', '2003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:order:cancel', '#', 'admin', sysdate(), '', null, 'refund principal and claw rebate/assist/commission');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, 2122
from sys_role_menu rm
where rm.menu_id = 2121
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 2122);

insert into sys_role_menu (role_id, menu_id)
select 1, 2122 from dual
where not exists (select 1 from sys_role_menu x where x.role_id = 1 and x.menu_id = 2122);

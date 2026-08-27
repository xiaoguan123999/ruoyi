SET NAMES utf8mb4;
-- 去掉「等级奖励」配置页菜单；查询/修改按钮挂到「会员等级」，「等级奖励发放」保留

delete from sys_role_menu where menu_id = 2019;
delete from sys_menu where menu_id = 2019;

update sys_menu set parent_id = 2009, order_num = 5 where menu_id = 2261;
update sys_menu set parent_id = 2009, order_num = 6 where menu_id = 2262;

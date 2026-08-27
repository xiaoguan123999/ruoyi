SET NAMES utf8mb4;
-- 团队要求改为字典 biz_team_depth：标签展示，键值是层数。可重复执行。
-- 新增例如：标签「八级内」，键值 8（表示第1到第8层都算）

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '团队要求层数', 'biz_team_depth', '0', 'admin', sysdate(), '一级内=直属，二级内=第1+2层。键值填层数'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'biz_team_depth');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '一级内', '1', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '只算直属下级'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '1');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '二级内', '2', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第2层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '2');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '三级内', '3', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第3层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '3');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 4, '四级内', '4', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第4层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '4');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 5, '五级内', '5', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第5层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '5');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 6, '六级内', '6', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第6层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '6');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 7, '七级内', '7', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第7层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '7');

update biz_level l
inner join sys_dict_data d on d.dict_type = 'biz_team_depth' and d.dict_label = l.team_depth
set l.team_depth = d.dict_value
where l.team_depth is not null and l.team_depth <> '';

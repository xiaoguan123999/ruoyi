SET NAMES utf8mb4;
-- App 会员等级表「团队要求」展示文案，接口字段 teamDepth（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'team_depth'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column team_depth varchar(50) default '''' comment ''团队要求，App等级表展示'' after min_valid_members',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_level set team_depth = '一级内' where level_name = '启航' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '二级内' where level_name = '探索' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '三级内' where level_name = '开拓' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '四级内' where level_name = '星耀' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '五级内' where level_name = '领航' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '六级内' where level_name = '星域' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '七级内' where level_name = '星链' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '' where team_depth is null;

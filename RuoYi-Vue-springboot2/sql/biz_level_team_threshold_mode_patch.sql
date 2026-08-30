SET NAMES utf8mb4;
-- Personal vs team threshold mode. Safe to re-run.
-- threshold_mode = personal; team_threshold_mode = team
-- First run copies current threshold_mode onto team so live levels stay the same.

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_level'
    and column_name = 'team_threshold_mode'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column team_threshold_mode varchar(16) not null default ''SPLIT'' comment ''team SPLIT/EQUIV'' after threshold_mode',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @sql := if(@exist = 0,
  'update biz_level set team_threshold_mode = ifnull(nullif(threshold_mode, ''''), ''SPLIT'')',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

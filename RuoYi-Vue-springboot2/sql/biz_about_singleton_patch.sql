SET NAMES utf8mb4;
-- 关于我们改为全局一条：文本/PDF 二选一（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_about'
    and column_name = 'display_mode'
);
set @sql := if(@exist = 0,
  'alter table biz_about add column display_mode varchar(16) not null default ''TEXT'' comment ''展示模式 TEXT/PDF'' after image_url',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_about'
    and column_name = 'pdf_url'
);
set @sql := if(@exist = 0,
  'alter table biz_about add column pdf_url varchar(500) default '''' comment ''PDF文件地址'' after display_mode',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update biz_about set display_mode = 'TEXT' where display_mode is null or display_mode = '';
update biz_about set pdf_url = ifnull(pdf_url, '');

delete from biz_about
 where about_id not in (select id from (select min(about_id) as id from biz_about) t);

insert into biz_about (title, subtitle, content, image_url, display_mode, pdf_url, sort, status, create_by, create_time)
select '星帆智联', '连接星空 · 智联未来',
       '<p>星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。</p>',
       '', 'TEXT', '', 1, '0', 'admin', sysdate()
  from dual
 where not exists (select 1 from biz_about limit 1);

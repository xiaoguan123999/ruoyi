SET NAMES utf8mb4;
-- 产品卡片颜色覆盖（可空，空则 App 走 theme 推导）可重复执行

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'accent_color');
set @sql := if(@exist = 0, 'alter table biz_product add column accent_color varchar(16) default '''' comment ''序号/英文名强调色，空则跟theme'' after cta_text', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'title_color');
set @sql := if(@exist = 0, 'alter table biz_product add column title_color varchar(16) default '''' comment ''产品名颜色，空则跟theme'' after accent_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'remark_color');
set @sql := if(@exist = 0, 'alter table biz_product add column remark_color varchar(16) default '''' comment ''备注/口号颜色，空则跟theme'' after title_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'label_color');
set @sql := if(@exist = 0, 'alter table biz_product add column label_color varchar(16) default '''' comment ''内容区标题颜色，空则跟theme'' after remark_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'value_color');
set @sql := if(@exist = 0, 'alter table biz_product add column value_color varchar(16) default '''' comment ''数值颜色，空则跟theme'' after label_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'unit_color');
set @sql := if(@exist = 0, 'alter table biz_product add column unit_color varchar(16) default '''' comment ''单位颜色，空则跟theme'' after value_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'btn_color');
set @sql := if(@exist = 0, 'alter table biz_product add column btn_color varchar(16) default '''' comment ''按钮背景色，空则跟theme'' after unit_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'btn_text_color');
set @sql := if(@exist = 0, 'alter table biz_product add column btn_text_color varchar(16) default '''' comment ''按钮文字色，空则白'' after btn_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

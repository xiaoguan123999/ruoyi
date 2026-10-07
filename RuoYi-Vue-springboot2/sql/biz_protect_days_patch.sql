SET NAMES utf8mb4;
-- Protect days N (product income first N days). Not the old accumulate_cycle_days.

SET @exist := (SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_product' AND column_name = 'protect_days');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_product ADD COLUMN protect_days int(4) default 0 comment ''protect days N, product income first N days'' AFTER accumulate_cycle_days',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_order' AND column_name = 'protect_days');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_order ADD COLUMN protect_days int(4) default 0 comment ''protect days snapshot'' AFTER accumulate_cycle_days',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE biz_product SET protect_days = 0 WHERE protect_days IS NULL;
UPDATE biz_order SET protect_days = 0 WHERE protect_days IS NULL;

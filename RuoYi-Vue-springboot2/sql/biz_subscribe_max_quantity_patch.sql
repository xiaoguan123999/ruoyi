SET NAMES utf8mb4;
-- 单次认购上限不再走系统参数，改由产品「每人限购」控制；0 或不填不限制（可重复执行）

delete from sys_config where config_key = 'biz.subscribe.maxQuantity';

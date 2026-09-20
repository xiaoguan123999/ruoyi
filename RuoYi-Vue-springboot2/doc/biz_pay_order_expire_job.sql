SET NAMES utf8mb4;

-- 每 5 分钟关闭已过期的待付支付单（拉起收银台未付）
-- 文件请保持 UTF-8 保存/导入

INSERT INTO sys_job
  (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
   create_by, create_time, remark)
SELECT
  102,
  '支付超时关单',
  'DEFAULT',
  'payOrderExpireTask.execute()',
  '0 */5 * * * ?',
  '3', '1', '0',
  'admin',
  SYSDATE(),
  '待付超过 expire_time（分钟数见参数 biz.pay.orderExpireMinutes）自动关闭，并拒绝关联线上充值'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE job_id = 102);

-- 若曾用乱码脚本插入过，纠正名称与备注
UPDATE sys_job
SET job_name = '支付超时关单',
    remark = '待付超过 expire_time（分钟数见参数 biz.pay.orderExpireMinutes）自动关闭，并拒绝关联线上充值',
    invoke_target = 'payOrderExpireTask.execute()',
    cron_expression = '0 */5 * * * ?'
WHERE job_id = 102;

-- 默认超时分钟（支付订单页可改；参数键 biz.pay.orderExpireMinutes）
INSERT INTO sys_config
  (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT
  '线上支付单超时分钟',
  'biz.pay.orderExpireMinutes',
  '30',
  'N',
  'admin',
  SYSDATE(),
  '拉起收银台后未支付，超过该分钟数自动关闭待付单'
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_config WHERE config_key = 'biz.pay.orderExpireMinutes'
);

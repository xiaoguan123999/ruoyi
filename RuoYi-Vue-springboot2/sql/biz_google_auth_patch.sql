-- 谷歌验证器（可重复执行）
DROP PROCEDURE IF EXISTS patch_biz_google_auth;
DELIMITER $$
CREATE PROCEDURE patch_biz_google_auth()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_member' AND COLUMN_NAME = 'ga_status'
  ) THEN
    ALTER TABLE biz_member
      ADD COLUMN ga_secret varchar(64) default '' comment '谷歌验证密钥' AFTER status,
      ADD COLUMN ga_status char(1) default '0' comment '谷歌验证（0未绑定 1已绑定）' AFTER ga_secret;
  END IF;
END$$
DELIMITER ;
CALL patch_biz_google_auth();
DROP PROCEDURE IF EXISTS patch_biz_google_auth;

delete from sys_config where config_id between 36 and 38;
insert into sys_config values(36, '谷歌验证开关', 'biz.google.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭谷歌验证');
insert into sys_config values(37, '提现必须谷歌验证', 'biz.google.requireWithdraw', 'true', 'N', 'admin', sysdate(), '', null, 'true表示未绑定不能提现');
insert into sys_config values(38, '谷歌验证器名称', 'biz.google.issuer', 'App', 'N', 'admin', sysdate(), '', null, '显示在谷歌验证器中的名称');

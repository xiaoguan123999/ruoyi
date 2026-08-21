-- 后台账号谷歌验证器（可重复执行）
DROP PROCEDURE IF EXISTS patch_sys_google_auth;
DELIMITER $$
CREATE PROCEDURE patch_sys_google_auth()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'ga_status'
  ) THEN
    ALTER TABLE sys_user
      ADD COLUMN ga_secret varchar(64) default '' comment '谷歌验证密钥' AFTER status,
      ADD COLUMN ga_status char(1) default '0' comment '谷歌验证（0未绑定 1已绑定）' AFTER ga_secret;
  END IF;
END$$
DELIMITER ;
CALL patch_sys_google_auth();
DROP PROCEDURE IF EXISTS patch_sys_google_auth;

delete from sys_config where config_id in (39, 40);
insert into sys_config values(39, '后台谷歌验证开关', 'sys.google.enabled', 'true', 'Y', 'admin', sysdate(), '', null, 'false表示后台登录不校验谷歌验证码');
insert into sys_config values(40, '后台谷歌验证器名称', 'sys.google.issuer', '后台管理', 'Y', 'admin', sysdate(), '', null, '显示在谷歌验证器中的名称');

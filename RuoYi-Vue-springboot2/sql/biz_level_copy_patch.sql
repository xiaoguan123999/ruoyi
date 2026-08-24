-- App 会员等级页：规则说明 + 表格上方注释（可重复执行）

delete from sys_config where config_id = 51;
insert into sys_config values(51, '等级页注释', 'biz.levelReward.hint', '注：成员个人累计认购金额达到 ¥10,000 或 1,429 USDT 后，方可计入团队等级考核。请遵循平台规则，严禁作弊行为，一经发现将取消奖励资格。', 'N', 'admin', sysdate(), '', null, 'App会员等级页表格上方的注');

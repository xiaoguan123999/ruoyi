-- 成长激励金：前六档一次自动发放，仅星链找客服、后台手动发放（可重复执行）

update biz_level
   set reward_cycle = 'ONCE',
       reward_mode = 'AUTO',
       reward_repeat = 'NONE',
       remark = '成长激励金：一次自动发放'
 where level_name in ('启航', '探索', '开拓', '星耀', '领航', '星域');

update biz_level
   set reward_cycle = 'PERMANENT',
       reward_mode = 'MANUAL',
       reward_repeat = 'UNLIMITED',
       remark = '成长激励金：达标后联系客服，后台手动发放'
 where level_name = '星链';

update sys_config
   set config_value = '启航、探索、开拓、星耀、领航、星域：达成条件后系统自动发放1次成长激励金。星链：达成条件后联系客服领取，由后台手动发放。团队同时有人民币和USDT业绩时发放USDT。最终以系统核算为准。'
 where config_key = 'biz.levelReward.ruleText';

update sys_job
   set remark = '每日核算成长激励金：前六档一次自动，星链生成待发放'
 where job_name = '等级奖励核算';

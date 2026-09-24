package com.ruoyi.biz.lottery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * 大转盘 Redis Lua 脚本 Bean
 */
@Configuration
public class LotteryRedisConfig
{
    @Bean
    public DefaultRedisScript<String> lotteryDrawScript()
    {
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(String.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/draw_processor.lua")));
        return redisScript;
    }
}

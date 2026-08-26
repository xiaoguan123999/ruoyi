package com.ruoyi.framework.config;

import java.nio.charset.Charset;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.ruoyi.common.core.domain.model.AppLoginMember;
import com.ruoyi.common.core.domain.model.LoginUser;

/**
 * Redis JSON serializer without Fastjson autoType.
 * WriteClassName + autoTypeFilter treats nested {@code false} as a type name and
 * breaks admin login (getInfo 401). Sessions are written as plain JSON and read
 * into explicit Java types.
 */
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T>
{
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private Class<T> clazz;

    public FastJson2JsonRedisSerializer(Class<T> clazz)
    {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException
    {
        if (t == null)
        {
            return new byte[0];
        }
        return JSON.toJSONString(t, JSONWriter.Feature.FieldBased).getBytes(DEFAULT_CHARSET);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(byte[] bytes) throws SerializationException
    {
        if (bytes == null || bytes.length <= 0)
        {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET).trim();
        if (str.isEmpty())
        {
            return null;
        }
        try
        {
            if (LoginUser.class == clazz)
            {
                return (T) parseLoginUser(str);
            }
            if (AppLoginMember.class == clazz)
            {
                return (T) parseAppMember(str);
            }
            Object parsed = JSON.parse(str);
            if (parsed instanceof JSONObject)
            {
                JSONObject obj = (JSONObject) parsed;
                if (isLoginUser(obj))
                {
                    return (T) parseLoginUser(str);
                }
                if (isAppMember(obj))
                {
                    return (T) parseAppMember(str);
                }
            }
            return (T) parsed;
        }
        catch (SerializationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new SerializationException("Redis JSON deserialize failed: " + e.getClass().getSimpleName(), e);
        }
    }

    private static boolean isLoginUser(JSONObject obj)
    {
        return obj.get("user") instanceof JSONObject && obj.containsKey("permissions");
    }

    private static boolean isAppMember(JSONObject obj)
    {
        return obj.get("memberId") != null && obj.get("phone") != null && obj.containsKey("token")
                && !(obj.get("user") instanceof JSONObject);
    }

    private static LoginUser parseLoginUser(String str)
    {
        return JSON.parseObject(str, LoginUser.class, JSONReader.Feature.FieldBased);
    }

    private static AppLoginMember parseAppMember(String str)
    {
        return JSON.parseObject(str, AppLoginMember.class, JSONReader.Feature.FieldBased);
    }
}

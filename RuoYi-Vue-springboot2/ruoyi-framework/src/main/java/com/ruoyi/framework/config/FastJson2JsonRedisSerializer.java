package com.ruoyi.framework.config;

import java.nio.charset.Charset;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ruoyi.common.core.domain.model.AppLoginMember;
import com.ruoyi.common.core.domain.model.LoginUser;

/**
 * LoginUser / AppLoginMember use Jackson. Fastjson2 breaks Chrome login sessions
 * (getInfo 401) on OS/browser strings and nested SysUser getters.
 */
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T>
{
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private static final ObjectMapper SESSION_MAPPER = buildSessionMapper();

    private Class<T> clazz;

    public FastJson2JsonRedisSerializer(Class<T> clazz)
    {
        super();
        this.clazz = clazz;
    }

    private static ObjectMapper buildSessionMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException
    {
        if (t == null)
        {
            return new byte[0];
        }
        try
        {
            if (t instanceof LoginUser || t instanceof AppLoginMember)
            {
                return SESSION_MAPPER.writeValueAsBytes(t);
            }
            return JSON.toJSONString(t, JSONWriter.Feature.FieldBased).getBytes(DEFAULT_CHARSET);
        }
        catch (Exception e)
        {
            throw new SerializationException("Redis JSON serialize failed: " + e.getClass().getSimpleName(), e);
        }
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
            if (LoginUser.class == clazz || looksLikeLoginUser(str))
            {
                return (T) SESSION_MAPPER.readValue(str, LoginUser.class);
            }
            if (AppLoginMember.class == clazz || looksLikeAppMember(str))
            {
                return (T) SESSION_MAPPER.readValue(str, AppLoginMember.class);
            }
            return (T) JSON.parse(str);
        }
        catch (SerializationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new SerializationException("Redis JSON deserialize failed: " + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : (": " + e.getMessage())), e);
        }
    }

    private static boolean looksLikeLoginUser(String str)
    {
        return str.contains("\"permissions\"") && str.contains("\"user\"");
    }

    private static boolean looksLikeAppMember(String str)
    {
        return str.contains("\"memberId\"") && str.contains("\"phone\"") && str.contains("\"token\"")
                && !str.contains("\"permissions\"");
    }
}

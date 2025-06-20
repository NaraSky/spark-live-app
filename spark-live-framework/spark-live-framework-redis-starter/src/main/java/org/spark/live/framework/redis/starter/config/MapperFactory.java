package org.spark.live.framework.redis.starter.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.cache.support.NullValue;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * ObjectMapper 工厂类
 * 提供预配置的 ObjectMapper 实例，用于 Redis 序列化
 */
public class MapperFactory {

    /**
     * 创建新的 ObjectMapper 实例
     *
     * @return 配置好的 ObjectMapper 实例
     */
    public static ObjectMapper newInstance() {
        return initMapper(new ObjectMapper(), (String) null);
    }

    /**
     * 初始化 ObjectMapper 配置
     *
     * @param mapper                ObjectMapper 实例
     * @param classPropertyTypeName 类型属性名称
     * @return 配置后的 ObjectMapper
     */
    private static ObjectMapper initMapper(ObjectMapper mapper, String classPropertyTypeName) {
        // 注册自定义的 NullValue 序列化器
        mapper.registerModule(new SimpleModule().addSerializer(new MapperNullValueSerializer(classPropertyTypeName)));

        // 配置类型信息的序列化方式
        if (StringUtils.hasText(classPropertyTypeName)) {
            // 使用自定义属性名存储类型信息
            mapper.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, classPropertyTypeName);
        } else {
            // 使用默认的 @class 属性存储类型信息
            mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
        }
        // 忽略未知属性，避免反序列化时出错
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }

    /**
     * 自定义的 NullValue 序列化器
     * 用于正确处理 Spring Cache 的 NullValue 对象
     *
     * @author Christoph Strobl
     * @since 1.8
     */
    private static class MapperNullValueSerializer extends StdSerializer<NullValue> {
        private static final long serialVersionUID = 1999052150548658808L;
        private final String classIdentifier;

        /**
         * @param classIdentifier 类标识符，如果为空则默认为 "@class"
         */
        MapperNullValueSerializer(String classIdentifier) {

            super(NullValue.class);
            this.classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
        }

        /**
         * 序列化 NullValue 对象
         * 生成包含类型信息的 JSON 对象
         */
        @Override
        public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {

            jgen.writeStartObject();
            jgen.writeStringField(classIdentifier, NullValue.class.getName());
            jgen.writeEndObject();
        }
    }
}
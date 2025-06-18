package org.spark.live.framework.redis.starter.config.key;

import org.spark.live.framework.redis.starter.config.MapperFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * 自定义的 Redis JSON 序列化器
 * 继承自 Spring 的 GenericJackson2JsonRedisSerializer
 * 对字符串和字符类型进行特殊处理，避免不必要的 JSON 序列化
 */
public class IGenericJackson2JsonRedisSerializer extends GenericJackson2JsonRedisSerializer {

    /**
     * 构造函数，使用自定义的 ObjectMapper 工厂
     */
    public IGenericJackson2JsonRedisSerializer() {
        super(MapperFactory.newInstance());
    }

    /**
     * 重写序列化方法
     * 对字符串和字符类型直接转换为字节数组，避免 JSON 包装
     *
     * @param source 待序列化的对象
     * @return 序列化后的字节数组
     * @throws SerializationException 序列化异常
     */
    @Override
    public byte[] serialize(Object source) throws SerializationException {
        // 如果对象不为空且是字符串或字符类型，直接转换为字节数组
        if (source != null && ((source instanceof String) || (source instanceof Character))) {
            return source.toString().getBytes();
        }
        // 其他类型使用父类的 JSON 序列化
        return super.serialize(source);
    }
}
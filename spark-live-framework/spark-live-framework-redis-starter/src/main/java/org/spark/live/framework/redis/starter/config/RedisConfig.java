package org.spark.live.framework.redis.starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 自动配置类
 * 提供 Redis 模板的统一配置，包括序列化策略
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisConfig {

    /**
     * 配置 Redis 模板 Bean
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return 配置好的 RedisTemplate 实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 创建 Redis 模板实例
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 创建自定义的 JSON 序列化器
        IGenericJackson2JsonRedisSerializer valueSerializer = new IGenericJackson2JsonRedisSerializer();
        // 创建字符串序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 设置 key 的序列化方式为字符串
        redisTemplate.setKeySerializer(stringRedisSerializer);
        // 设置 value 的序列化方式为 JSON
        redisTemplate.setValueSerializer(valueSerializer);
        // 设置 hash key 的序列化方式为字符串
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // 设置 hash value 的序列化方式为 JSON
        redisTemplate.setHashValueSerializer(valueSerializer);

        // 初始化 RedisTemplate 的配置
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}

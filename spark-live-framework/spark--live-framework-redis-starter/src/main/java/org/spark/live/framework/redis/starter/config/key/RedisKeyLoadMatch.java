package org.spark.live.framework.redis.starter.config.key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Redis 键构建器条件加载匹配器
 * 实现 Spring 的 Condition 接口，用于控制 KeyBuilder 的条件加载
 * 根据应用名称决定是否加载特定的 KeyBuilder
 */
public class RedisKeyLoadMatch implements Condition {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedisKeyLoadMatch.class);

    /**
     * 统一的类名前缀，用于匹配规则
     */
    private static final String PREFIX = "spark";

    /**
     * 条件匹配逻辑
     * 判断当前的 KeyBuilder 类是否应该被加载
     *
     * @param context  Spring 上下文
     * @param metadata 注解元数据
     * @return true 表示条件匹配，应该加载该 Bean
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String appName = context.getEnvironment().getProperty("spring.application.name");
        if (appName == null) {
            LOGGER.error("没有匹配到应用名称，所以无法加载任何RedisKeyBuilder对象");
            return false;
        }
        try {
            // 通过反射获取当前正在检查的类名
            Field classNameField = metadata.getClass().getDeclaredField("className");
            classNameField.setAccessible(true);
            String keyBuilderName = (String) classNameField.get(metadata);

            // 解析类的简单名称
            List<String> splitList = Arrays.asList(keyBuilderName.split("\\."));

            // 构造匹配用的类名：spark + 类简单名称（小写）
            String classSimplyName = PREFIX + splitList.get(splitList.size() - 1).toLowerCase();

            // 判断是否匹配：类名是否包含应用名（去掉横线）
            boolean matchStatus = classSimplyName.contains(appName.replaceAll("-", ""));
            LOGGER.info("keyBuilderClass is {},matchStatus is {}", keyBuilderName, matchStatus);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("无法获取类名字段", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法访问类名字段", e);
        }
        return true;
    }
}
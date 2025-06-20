package org.spark.live.framework.redis.starter.config.key;

import org.springframework.beans.factory.annotation.Value;

/**
 * Redis 键构建器基类
 * 提供统一的键命名规范：应用名:业务标识:具体值
 */
public class RedisKeyBuilder {

    @Value("${spring.application.name}")
    private String applicationName;

    private static final String SPLIT_ITEM = ":";

    /**
     * 获取分隔符
     *
     * @return 键分隔符
     */
    public String getSplitItem() {
        return SPLIT_ITEM;
    }

    /**
     * 获取键前缀
     * 格式：应用名:
     *
     * @return 键前缀
     */
    public String getPrefix() {
        return applicationName + SPLIT_ITEM;
    }
}
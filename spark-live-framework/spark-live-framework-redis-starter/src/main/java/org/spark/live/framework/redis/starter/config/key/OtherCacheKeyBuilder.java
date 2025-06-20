package org.spark.live.framework.redis.starter.config.key;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Conditional;

/**
 * 其他业务缓存键构建器
 * 用于构建其他业务相关的 Redis 键
 */
@Configurable
@Conditional(RedisKeyLoadMatch.class)   // 条件加载：只有匹配条件的应用才会加载此类
public class OtherCacheKeyBuilder extends RedisKeyBuilder {

    private static String USER_INFO_KEY = "other";

    /**
     * 构建其他业务的 Redis 键
     * 键格式：应用名:other:用户ID
     *
     * @param userId 用户ID
     * @return 完整的业务键
     */
    public String buildUserInfoKey(Long userId) {
        return super.getPrefix() + USER_INFO_KEY + super.getSplitItem() + userId;
    }

}
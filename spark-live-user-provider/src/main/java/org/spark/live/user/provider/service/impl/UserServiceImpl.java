package org.spark.live.user.provider.service.impl;

import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.spark.live.common.interfaces.ConvertBeanUtils;
import org.spark.live.framework.redis.starter.config.key.UserProviderCacheKeyBuilder;
import org.spark.live.user.dto.UserDTO;
import org.spark.live.user.provider.dao.mapper.IUserMapper;
import org.spark.live.user.provider.dao.po.UserPO;
import org.spark.live.user.provider.service.IUserService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private IUserMapper userMapper;
    @Resource
    private RedisTemplate<String, UserDTO> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Override
    public UserDTO getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userId);
        UserDTO userDTO = redisTemplate.opsForValue().get(key);
        if (userDTO != null) {
            return userDTO;
        }
        userDTO = ConvertBeanUtils.convert(userMapper.selectById(userId), UserDTO.class);
        if (userDTO != null) {
            redisTemplate.opsForValue().set(key, userDTO, 30, TimeUnit.MINUTES);
        }
        return userDTO;
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        userMapper.updateById(ConvertBeanUtils.convert(userDTO, UserPO.class));
        return true;
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        userMapper.insert(ConvertBeanUtils.convert(userDTO, UserPO.class));
        return true;
    }

    /**
     * 批量查询用户信息
     * 采用缓存优先策略，先从Redis查询，缓存未命中再查询数据库
     *
     * @param userIdList 用户ID列表
     * @return 用户ID与用户信息的映射Map
     */
    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        // 检查输入参数是否为空，为空则返回空Map
        if (CollectionUtils.isEmpty(userIdList)) {
            return Maps.newHashMap();
        }

        // 过滤无效的用户ID（小于等于10000的ID被认为是无效的）
        userIdList = userIdList.stream().filter(id -> id > 10000).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIdList)) {
            return Maps.newHashMap();
        }

        // 第一步：从Redis缓存中批量查询用户信息
        List<String> keyList = new ArrayList<>();
        // 为每个用户ID构建对应的Redis缓存key
        userIdList.forEach(userId -> {
            keyList.add(userProviderCacheKeyBuilder.buildUserInfoKey(userId));
        });

        // 批量从Redis获取用户信息，过滤掉null值
        List<UserDTO> userDTOList = redisTemplate.opsForValue().multiGet(keyList).stream()
                .filter(x -> x != null)
                .collect(Collectors.toList());

        // 如果缓存中查到了所有用户信息，直接返回结果
        if (!CollectionUtils.isEmpty(userDTOList) && userDTOList.size() == userIdList.size()) {
            return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x -> x));
        }

        // 第二步：找出缓存中不存在的用户ID，需要从数据库查询
        List<Long> userIdInCacheList = userDTOList.stream()
                .map(UserDTO::getUserId)
                .collect(Collectors.toList());

        // 计算缓存中不存在的用户ID列表
        List<Long> userIdNotInCacheList = userIdList.stream()
                .filter(x -> !userIdInCacheList.contains(x))
                .collect(Collectors.toList());

        // 第三步：多线程分片查询数据库（替换了union all方式）
        // 按用户ID对100取模进行分组，实现分片查询
        Map<Long, List<Long>> userIdMap = userIdNotInCacheList.stream()
                .collect(Collectors.groupingBy(userId -> userId % 100));

        // 使用线程安全的CopyOnWriteArrayList存储查询结果
        List<UserDTO> dbQueryResult = new CopyOnWriteArrayList<>();

        // 并行查询各个分片的数据
        userIdMap.values().parallelStream().forEach(queryUserIdList -> {
            // 查询数据库并转换为DTO对象
            dbQueryResult.addAll(ConvertBeanUtils.convertList(userMapper.selectBatchIds(queryUserIdList), UserDTO.class));
        });

        // 第四步：将数据库查询结果保存到Redis缓存
        if (!CollectionUtils.isEmpty(dbQueryResult)) {
            // 构建缓存key与用户信息的映射Map
            Map<String, UserDTO> saveCacheMap = dbQueryResult.stream()
                    .collect(Collectors.toMap(
                            userDto -> userProviderCacheKeyBuilder.buildUserInfoKey(userDto.getUserId()),
                            x -> x));

            // 批量保存到Redis
            redisTemplate.opsForValue().multiSet(saveCacheMap);

            // 使用Redis管道批量设置过期时间，提高性能
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    // 为每个缓存key设置随机过期时间，避免缓存雪崩
                    for (String redisKey : saveCacheMap.keySet()) {
                        operations.expire((K) redisKey, createRandomTime(), TimeUnit.SECONDS);
                    }
                    return null;
                }
            });

            // 将数据库查询结果合并到缓存查询结果中
            userDTOList.addAll(dbQueryResult);
        }

        // 返回最终的用户ID与用户信息映射Map
        return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x -> x));
    }

    /**
     * 生成随机过期时间，用户平均停留直播间时间？基于业务思考
     * 基础时间30分钟（1800秒）+ 随机时间（0-10000秒）
     * 避免大量缓存同时过期造成缓存雪崩
     *
     * @return 随机过期时间（秒）
     */
    private int createRandomTime() {
        // 生成0-10000之间的随机秒数
        int randomNumSecond = ThreadLocalRandom.current().nextInt(10000);
        // 基础过期时间30分钟（1800秒）+ 随机秒数
        return randomNumSecond + 30 * 60;
    }
}

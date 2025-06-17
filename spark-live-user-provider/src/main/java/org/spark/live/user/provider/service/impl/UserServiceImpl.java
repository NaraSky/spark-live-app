package org.spark.live.user.provider.service.impl;

import jakarta.annotation.Resource;
import org.spark.live.common.interfaces.ConvertBeanUtils;
import org.spark.live.user.dto.UserDTO;
import org.spark.live.user.provider.dao.mapper.IUserMapper;
import org.spark.live.user.provider.dao.po.UserPO;
import org.spark.live.user.provider.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private IUserMapper userMapper;

    @Override
    public UserDTO getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return ConvertBeanUtils.convert(userMapper.selectById(userId), UserDTO.class);
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

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        return Map.of();
    }
}

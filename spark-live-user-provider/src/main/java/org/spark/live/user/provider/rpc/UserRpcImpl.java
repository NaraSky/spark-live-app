package org.spark.live.user.provider.rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.spark.live.user.dto.UserDTO;
import org.spark.live.user.interfaces.IUserRpc;
import org.spark.live.user.provider.service.IUserService;

@DubboService
public class UserRpcImpl implements IUserRpc {

    @Resource
    private IUserService userService;

    @Override
    public UserDTO getByUserId(Long userId) {
        return userService.getByUserId(userId);
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        return userService.updateUserInfo(userDTO);
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        return userService.insertOne(userDTO);
    }
}

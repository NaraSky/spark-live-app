package org.spark.live.user.interfaces;

import org.spark.live.user.dto.UserDTO;

public interface IUserRpc {

    UserDTO getByUserId(Long userId);

    boolean updateUserInfo(UserDTO userDTO);

    boolean insertOne(UserDTO userDTO);
}

package org.spark.live.api.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.spark.live.user.dto.UserDTO;
import org.spark.live.user.interfaces.IUserRpc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class ApiController {

    @DubboReference
    private IUserRpc userRpc;

    @GetMapping("/getUserInfo")
    public UserDTO getUserInfo(@RequestParam Long userId) {
        return userRpc.getByUserId(userId);
    }

    @GetMapping("/updateUserInfo")
    public UserDTO updateUserInfo(@RequestParam Long userId) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName("updateNickName");
        return userRpc.updateUserInfo(userDTO) ? userDTO : null;
    }

    @GetMapping("/insertOne")
    public UserDTO insertOne(@RequestParam Long userId) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName("insertNickName");
        return userRpc.insertOne(userDTO) ? userDTO : null;
    }

    @GetMapping("/batchQueryUserInfo")
    public Map<Long, UserDTO> batchQueryUserInfo(@RequestParam List<Long> userIdList) {
        return userRpc.batchQueryUserInfo(userIdList);
    }

}
package com.cardmanager.service;

import com.cardmanager.common.PageResult;
import com.cardmanager.dto.UserDTO;
import com.cardmanager.dto.UserQueryDTO;
import com.cardmanager.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 查询用户列表
     */
    PageResult<UserVO> listUsers(UserQueryDTO dto);

    /**
     * 添加用户
     */
    void addUser(UserDTO dto);

    /**
     * 修改用户
     */
    void updateUser(UserDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 根据ID查询用户
     */
    UserVO getUserById(Long id);
}

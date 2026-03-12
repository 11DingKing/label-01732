package com.cardmanager.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.dto.UserDTO;
import com.cardmanager.dto.UserQueryDTO;
import com.cardmanager.entity.SysUser;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.SysUserMapper;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.UserService;
import com.cardmanager.util.BusinessLogger;
import com.cardmanager.util.PasswordEncoder;
import com.cardmanager.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public PageResult<UserVO> listUsers(UserQueryDTO dto) {
        Page<SysUser> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(dto.getUsername()), SysUser::getUsername, dto.getUsername())
               .like(StrUtil.isNotBlank(dto.getRealName()), SysUser::getRealName, dto.getRealName())
               .eq(StrUtil.isNotBlank(dto.getRole()), SysUser::getRole, dto.getRole())
               .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);

        List<UserVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    @Override
    public void addUser(UserDTO dto) {
        // 检查用户名是否已存在
        SysUser existUser = sysUserMapper.selectByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 密码为必填项（新增时）
        if (StrUtil.isBlank(dto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : Constants.UserStatus.ENABLED);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);

        sysUserMapper.insert(user);
        
        // 记录业务日志
        BusinessLogger.logUserOperation("新增", user.getId(), dto.getUsername(), 
            UserContext.getUserId(), UserContext.getRealName());
        log.info("添加用户成功: username={}, operator={}", dto.getUsername(), UserContext.getRealName());
    }

    @Override
    public void updateUser(UserDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 查询用户
        SysUser user = sysUserMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果修改了用户名，检查新用户名是否已存在
        if (StrUtil.isNotBlank(dto.getUsername()) && !dto.getUsername().equals(user.getUsername())) {
            SysUser existUser = sysUserMapper.selectByUsername(dto.getUsername());
            if (existUser != null) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(dto.getUsername());
        }

        // 更新密码（如果提供了新密码）
        if (StrUtil.isNotBlank(dto.getPassword())) {
            user.setPassword(PasswordEncoder.encode(dto.getPassword()));
        }

        // 更新其他字段
        if (StrUtil.isNotBlank(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (StrUtil.isNotBlank(dto.getRole())) {
            user.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 记录业务日志
        BusinessLogger.logUserOperation("修改", dto.getId(), user.getUsername(), 
            UserContext.getUserId(), UserContext.getRealName());
        log.info("修改用户成功: userId={}, operator={}", dto.getId(), UserContext.getRealName());
    }

    @Override
    public void deleteUser(Long id) {
        // 查询用户
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 不能删除自己
        if (id.equals(UserContext.getUserId())) {
            throw new BusinessException("不能删除自己");
        }

        // 逻辑删除
        sysUserMapper.deleteById(id);

        // 记录业务日志
        BusinessLogger.logUserOperation("删除", id, user.getUsername(), 
            UserContext.getUserId(), UserContext.getRealName());
        log.info("删除用户成功: userId={}, operator={}", id, UserContext.getRealName());
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    /**
     * 转换为 UserVO
     */
    private UserVO convertToVO(SysUser entity) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setRoleName(Constants.UserRole.getRoleName(entity.getRole()));
        vo.setStatusName(Constants.UserStatus.getStatusName(entity.getStatus()));
        return vo;
    }
}

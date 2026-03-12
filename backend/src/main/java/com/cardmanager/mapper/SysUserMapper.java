package com.cardmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardmanager.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 系统用户Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0")
    SysUser selectByUsername(String username);

    /**
     * 统计用户总数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0")
    Long countTotal();
}

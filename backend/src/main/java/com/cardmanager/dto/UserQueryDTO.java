package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String username;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 角色
     */
    private String role;

    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小最小为1")
    private Integer pageSize = 10;
}

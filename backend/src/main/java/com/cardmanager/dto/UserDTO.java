package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户DTO
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（修改时使用）
     */
    private Long id;

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 20, message = "账号长度为3-20个字符")
    private String username;

    /**
     * 密码
     */
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String realName;

    /**
     * 角色：admin/operator
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "admin|operator", message = "角色只能是admin或operator")
    private String role;

    /**
     * 状态：0-启用 1-禁用
     */
    private Integer status;
}

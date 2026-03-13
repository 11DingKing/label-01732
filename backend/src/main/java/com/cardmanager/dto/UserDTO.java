package com.cardmanager.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户DTO
 */
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

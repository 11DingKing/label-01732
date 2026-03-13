package com.cardmanager.dto;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求DTO
 */
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

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
}

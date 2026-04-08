package com.dashboard.app.user.dto;

import lombok.Data;

@Data
public class UserUpdateReqDto {
    private String password;
    private String email;
    private String phone;
    private String profileImage;
}

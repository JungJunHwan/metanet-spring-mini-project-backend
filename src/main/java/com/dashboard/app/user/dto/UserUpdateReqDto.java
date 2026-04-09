package com.dashboard.app.user.dto;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class UserUpdateReqDto {
    private String password;
    private String email;
    private String phone;
    private MultipartFile profileImage;
}

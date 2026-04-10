package com.dashboard.app.user.dto;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

import java.util.Date;

@Data
public class UserUpdateReqDto {
    private String name;
    private String password;
    private String email;
    private String phone;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birth;
    private String gender;
    private MultipartFile profileImage;
}

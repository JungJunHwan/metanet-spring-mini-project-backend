package com.dashboard.app.user.dto;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class UserCreateReqDto {
    private String name;
    private String loginId;
    private String password;
    private String email;
    private String phone;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private Date birth;
    private char gender;
    private MultipartFile profileImage;

}
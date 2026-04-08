package com.dashboard.app.user.dto;

import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserDto {
    private String name;
    private String loginId;
    private String password;
    private String email;
    private String phone;
    private Date birth;
    private char gender;
    private String profileImage;

}
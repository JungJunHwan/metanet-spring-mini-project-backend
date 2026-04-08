package com.dashboard.app.user.dto;

import com.dashboard.app.user.domain.UserDomain;
import lombok.Data;

import java.util.Date;

@Data
public class UserResDto {
    private Long userId;
    private String name;
    private String loginId;
    private String email;
    private String phone;
    private Date birth;
    private char gender;
    private String profileImage;
    private Date signDate;

    public UserResDto(UserDomain user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.loginId = user.getLoginId();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.birth = user.getBirth();
        this.gender = user.getGender();
        this.profileImage = user.getProfileImage();
        this.signDate = user.getSignDate();
    }
}

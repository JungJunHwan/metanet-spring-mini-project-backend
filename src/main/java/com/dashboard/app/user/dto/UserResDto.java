package com.dashboard.app.user.dto;

import com.dashboard.app.user.domain.User;
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

    public UserResDto(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.loginId = user.getLoginId();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.birth = user.getBirth();
        this.gender = user.getGender();
        if (user.getProfileImage() != null && user.getProfileImage().length > 0) {
            this.profileImage = "/bike/users/" + user.getUserId() + "/profile-image";
        }
        this.signDate = user.getSignDate();
    }
}

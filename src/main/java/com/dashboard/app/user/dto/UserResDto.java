package com.dashboard.app.user.dto;

import com.dashboard.app.user.domain.User;
import lombok.Data;

import java.util.Base64;
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
    private Date joinDate;   // DB 컬럼: signDate → 프론트 노출명: joinDate

    public UserResDto(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.loginId = user.getLoginId();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.birth = user.getBirth();
        this.gender = user.getGender();
        // byte[] → Base64 Data URI 변환: <img src="data:image/jpeg;base64,..."> 형태로
        // JWT 인증 없이 브라우저가 직접 렌더링 가능
        if (user.getProfileImage() != null && user.getProfileImage().length > 0) {
            this.profileImage = "data:image/jpeg;base64,"
                    + Base64.getEncoder().encodeToString(user.getProfileImage());
        }
        this.joinDate = user.getSignDate();
    }
}

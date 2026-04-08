package com.dashboard.app.user.domain;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class UserDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;
    private String loginId;
    private String password;
    private String email;
    private String phone;
    private Date birth;
    private char gender;
    private String profileImage;
    private Date signDate;
    private char status; // 탈퇴 여부

}
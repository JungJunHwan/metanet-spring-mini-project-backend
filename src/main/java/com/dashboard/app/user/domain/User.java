package com.dashboard.app.user.domain;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;
    private String loginId;
    private String password;
    private String email;
    private String phone;
    private Date birth;
    @Column(length = 1)
    private char gender;

    @jakarta.persistence.Lob
    private byte[] profileImage;

    private Date signDate;

    @Column(length = 1)
    private char status; // 탈퇴 여부

}
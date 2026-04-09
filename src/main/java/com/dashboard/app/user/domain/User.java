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

    // ★ 아이디는 시스템 전체에서 유일해야 하므로 UNIQUE + NOT NULL 제약
    @Column(unique = true, nullable = false)
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
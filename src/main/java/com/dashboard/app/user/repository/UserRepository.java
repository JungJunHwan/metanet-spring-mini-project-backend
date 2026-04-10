package com.dashboard.app.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dashboard.app.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId); // 로그인용

    Optional<User> findByEmail(String email); // 중복 체크
}
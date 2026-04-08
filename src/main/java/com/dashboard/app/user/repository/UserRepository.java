package com.dashboard.app.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dashboard.app.user.domain.UserDomain;

public interface UserRepository extends JpaRepository<UserDomain, Long> {

    Optional<UserDomain> findByLoginId(String loginId); // 로그인용

    Optional<UserDomain> findByEmail(String email); // 중복 체크
}
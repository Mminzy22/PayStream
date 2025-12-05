package com.paystream.user.repository;

import com.paystream.user.entity.AuthProvider;
import com.paystream.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    // 소셜 로그인용 (나중에 사용)
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    Optional<User> findByEmailAndProvider(String email, AuthProvider provider);
}

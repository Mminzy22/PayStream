package com.paystream.user.repository;

import com.paystream.user.entity.AuthProvider;
import com.paystream.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** User 엔티티를 위한 JPA Repository */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    Optional<User> findByEmailAndProvider(String email, AuthProvider provider);
}

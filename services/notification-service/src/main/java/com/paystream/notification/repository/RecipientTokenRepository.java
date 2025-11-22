package com.paystream.notification.repository;

import com.paystream.notification.domain.RecipientToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** RecipientToken 저장소 */
public interface RecipientTokenRepository extends JpaRepository<RecipientToken, Long> {
    List<RecipientToken> findByUserIdAndActiveTrue(Long userId);

    Optional<RecipientToken> findByDeviceToken(String deviceToken);
}

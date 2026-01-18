package com.paystream.payment.repository;

import com.paystream.payment.entity.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Payment 엔티티를 위한 JPA Repository */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByMerchantUid(String merchantUid);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findAllByOrderId(Long orderId);

    List<Payment> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}

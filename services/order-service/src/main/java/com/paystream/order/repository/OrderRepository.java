package com.paystream.order.repository;

import com.paystream.order.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Order 엔티티를 위한 JPA Repository */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByMerchantUid(String merchantUid);

    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}

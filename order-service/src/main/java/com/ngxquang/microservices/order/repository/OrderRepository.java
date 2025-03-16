package com.ngxquang.microservices.order.repository;

import com.ngxquang.microservices.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}

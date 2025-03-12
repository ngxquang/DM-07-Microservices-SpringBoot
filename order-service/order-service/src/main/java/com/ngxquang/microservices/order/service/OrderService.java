package com.ngxquang.microservices.order.service;

import com.ngxquang.microservices.order.InventoryClient;
import com.ngxquang.microservices.order.dto.OrderRequest;
import com.ngxquang.microservices.order.model.Order;
import com.ngxquang.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public void placeOrder(OrderRequest orderRequest) {

        var isProductInStock

        // Map Order Request to Order obj
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setPrice(orderRequest.price());
        order.setSkuCode(orderRequest.skuCode());
        order.setQuantity(orderRequest.quantity());
        // Save order to OrderRepository
        orderRepository.save(order);
    }

}

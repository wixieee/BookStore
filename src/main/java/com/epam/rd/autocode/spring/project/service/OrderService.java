package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<OrderDTO> getOrdersByClient(String clientEmail, String search, Pageable pageable);

    Page<OrderDTO> getUnconfirmedOrders(String search, Pageable pageable);

    void addOrder(OrderDTO order);

    void confirmOrder(Long orderId, String employeeEmail);

    void declineOrder(Long orderId, String employeeEmail);
}
package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.EmptyCartException;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.CartRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<OrderDTO> getOrdersByClient(String clientEmail, String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return orderRepository.searchByClient(clientEmail, search, pageable)
                    .map(order -> modelMapper.map(order, OrderDTO.class));
        }
        return orderRepository.findAllByClient_Email(clientEmail, pageable)
                .map(order -> modelMapper.map(order, OrderDTO.class));
    }

    @Override
    public Page<OrderDTO> getUnconfirmedOrders(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return orderRepository.searchByStatus(OrderStatus.PROCESSING, search, pageable)
                    .map(order -> modelMapper.map(order, OrderDTO.class));
        }
        return orderRepository.findAllByStatus(OrderStatus.PROCESSING, pageable)
                .map(order -> modelMapper.map(order, OrderDTO.class));
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, String employeeEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found"));

        order.setEmployee(employee);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void declineOrder(Long orderId, String employeeEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));


        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found"));

        Client client = order.getClient();
        client.setBalance(client.getBalance().add(order.getPrice()));
        clientRepository.save(client);

        order.setEmployee(employee);
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void addOrder(OrderDTO orderDto) {
        String email = orderDto.getClientEmail();
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client with email " + email + " not found"));
        Cart cart = cartRepository.findByClient_Email(email)
                .orElseThrow(() -> new NotFoundException("Cart associated with email " + email + " not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new EmptyCartException("Cannot create order from empty cart");
        }

        Order newOrder = new Order();
        List<BookItem> bookItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem item : cart.getCartItems()) {
            BookItem bookItem = new BookItem();
            bookItem.setBook(item.getBook());
            bookItem.setQuantity(item.getQuantity());
            bookItem.setOrder(newOrder);

            bookItems.add(bookItem);

            totalPrice = totalPrice.add(item.getBook().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        if (totalPrice.compareTo(client.getBalance()) > 0) {
            throw new InsufficientFundsException("Not enough balance for client " + email);
        }
        client.setBalance(client.getBalance().subtract(totalPrice));
        clientRepository.save(client);

        newOrder.setClient(client);
        newOrder.setBookItems(bookItems);
        newOrder.setPrice(totalPrice);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setEmployee(null);
        newOrder.setStatus(OrderStatus.PROCESSING);

        Order savedOrder = orderRepository.save(newOrder);
        cart.clear();
        cartRepository.save(cart);

        modelMapper.map(savedOrder, OrderDTO.class);
    }
}
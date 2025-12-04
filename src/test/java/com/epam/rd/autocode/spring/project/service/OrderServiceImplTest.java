package com.epam.rd.autocode.spring.project.service;

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
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Client client;
    private Employee employee;
    private Order order;
    private OrderDTO orderDTO;
    private Cart cart;
    private Book book;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setEmail("client@test.com");
        client.setBalance(new BigDecimal("1000.00"));

        employee = new Employee();
        employee.setId(2L);
        employee.setEmail("emp@test.com");

        book = new Book();
        book.setId(10L);
        book.setPrice(new BigDecimal("100.00"));
        book.setName("Test Book");

        cart = new Cart();
        cart.setId(5L);
        cart.setClient(client);
        cart.setCartItems(new ArrayList<>());

        order = new Order();
        order.setId(100L);
        order.setClient(client);
        order.setPrice(new BigDecimal("200.00"));
        order.setStatus(OrderStatus.PROCESSING);
        order.setOrderDate(LocalDateTime.now());

        orderDTO = new OrderDTO();
        orderDTO.setClientEmail("client@test.com");
        orderDTO.setPrice(new BigDecimal("200.00"));

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getOrdersByClient_withSearch_shouldReturnFilteredPage() {
        String search = "100";
        Page<Order> orderPage = new PageImpl<>(List.of(order));

        when(orderRepository.searchByClient(client.getEmail(), search, pageable)).thenReturn(orderPage);
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

        Page<OrderDTO> result = orderService.getOrdersByClient(client.getEmail(), search, pageable);

        assertEquals(1, result.getTotalElements());
        verify(orderRepository).searchByClient(client.getEmail(), search, pageable);
        verify(orderRepository, never()).findAllByClient_Email(any(), any());
    }

    @Test
    void getOrdersByClient_withoutSearch_shouldReturnAllPage() {
        Page<Order> orderPage = new PageImpl<>(List.of(order));

        when(orderRepository.findAllByClient_Email(client.getEmail(), pageable)).thenReturn(orderPage);
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

        Page<OrderDTO> result = orderService.getOrdersByClient(client.getEmail(), null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findAllByClient_Email(client.getEmail(), pageable);
    }

    @Test
    void getUnconfirmedOrders_withSearch_shouldReturnFilteredPage() {
        String search = "client";
        Page<Order> orderPage = new PageImpl<>(List.of(order));

        when(orderRepository.searchByStatus(OrderStatus.PROCESSING, search, pageable)).thenReturn(orderPage);
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

        Page<OrderDTO> result = orderService.getUnconfirmedOrders(search, pageable);

        assertEquals(1, result.getTotalElements());
        verify(orderRepository).searchByStatus(OrderStatus.PROCESSING, search, pageable);
    }

    @Test
    void getUnconfirmedOrders_withoutSearch_shouldReturnAllProcessingPage() {
        Page<Order> orderPage = new PageImpl<>(List.of(order));

        when(orderRepository.findAllByStatus(OrderStatus.PROCESSING, pageable)).thenReturn(orderPage);
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

        Page<OrderDTO> result = orderService.getUnconfirmedOrders("", pageable);

        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findAllByStatus(OrderStatus.PROCESSING, pageable);
    }

    @Test
    void confirmOrder_shouldUpdateStatusAndSetEmployee() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        orderService.confirmOrder(order.getId(), employee.getEmail());

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(employee, order.getEmployee());
        verify(orderRepository).save(order);
    }

    @Test
    void confirmOrder_shouldThrowNotFound_whenOrderMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                orderService.confirmOrder(99L, "emp@test.com")
        );
        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmOrder_shouldThrowNotFound_whenEmployeeMissing() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                orderService.confirmOrder(order.getId(), "unknown@test.com")
        );
        verify(orderRepository, never()).save(any());
    }

    @Test
    void declineOrder_shouldRefundClientAndCancelOrder() {
        BigDecimal initialBalance = client.getBalance();
        BigDecimal orderPrice = order.getPrice();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        orderService.declineOrder(order.getId(), employee.getEmail());

        assertEquals(initialBalance.add(orderPrice), client.getBalance());
        verify(clientRepository).save(client);

        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertEquals(employee, order.getEmployee());
        verify(orderRepository).save(order);
    }

    @Test
    void declineOrder_shouldThrowNotFound_whenOrderMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.declineOrder(99L, "emp@test.com"));
    }


    @Test
    void addOrder_shouldCreateOrder_subtractBalance_clearCart() {
        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(2);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.addOrder(orderDTO);

        assertEquals(new BigDecimal("800.00"), client.getBalance());
        verify(clientRepository).save(client);

        verify(orderRepository).save(argThat(savedOrder ->
                savedOrder.getClient().equals(client) &&
                        savedOrder.getPrice().compareTo(new BigDecimal("200.00")) == 0 &&
                        savedOrder.getStatus() == OrderStatus.PROCESSING &&
                        savedOrder.getBookItems().size() == 1
        ));

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void addOrder_shouldThrowEmptyCartException_whenCartIsEmpty() {
        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));

        assertThrows(EmptyCartException.class, () -> orderService.addOrder(orderDTO));

        verify(orderRepository, never()).save(any());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void addOrder_shouldThrowInsufficientFundsException_whenBalanceLow() {
        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(20);
        cart.getCartItems().add(cartItem);

        client.setBalance(new BigDecimal("500.00"));

        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));

        assertThrows(InsufficientFundsException.class, () -> orderService.addOrder(orderDTO));

        verify(orderRepository, never()).save(any());
        verify(clientRepository, never()).save(any());
        assertEquals(new BigDecimal("500.00"), client.getBalance());
    }

    @Test
    void addOrder_shouldThrowNotFound_whenClientMissing() {
        when(clientRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        OrderDTO badDto = new OrderDTO();
        badDto.setClientEmail("unknown");

        assertThrows(NotFoundException.class, () -> orderService.addOrder(badDto));
    }

    @Test
    void addOrder_shouldThrowNotFound_whenCartMissing() {
        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));
    }
}
package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private Page<OrderDTO> orderPage;

    @BeforeEach
    void setUp() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setStatus(OrderStatus.PROCESSING);
        orderDTO.setPrice(BigDecimal.TEN);
        orderDTO.setOrderDate(LocalDateTime.now());

        orderPage = new PageImpl<>(Collections.singletonList(orderDTO), PageRequest.of(0, 5), 1);
    }

    @Test
    void createOrder_shouldCreateAndRedirectRoot_whenSuccessful() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");
        when(messageSource.getMessage(eq("checkout.success"), isNull(), any(Locale.class)))
                .thenReturn("Order Created");

        mockMvc.perform(post("/orders/create")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("successMessage", "Order Created"));

        verify(orderService).addOrder(any(OrderDTO.class));
    }

    @Test
    void createOrder_shouldRedirectCart_whenInsufficientFunds() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        doThrow(new InsufficientFundsException("No money"))
                .when(orderService).addOrder(any(OrderDTO.class));

        when(messageSource.getMessage(eq("checkout.error.insufficientFunds"), isNull(), any(Locale.class)))
                .thenReturn("Not enough funds");

        mockMvc.perform(post("/orders/create")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attribute("errorMessage", "Not enough funds"));

        verify(orderService).addOrder(any(OrderDTO.class));
    }

    @Test
    void createOrder_shouldRedirectLogin_whenPrincipalNull() throws Exception {
        mockMvc.perform(post("/orders/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(orderService, never()).addOrder(any());
    }

    @Test
    void listClientOrders_shouldReturnView_whenAuthenticated() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        when(orderService.getOrdersByClient(eq("user@email.com"), any(), any(Pageable.class)))
                .thenReturn(orderPage);

        mockMvc.perform(get("/orders/client")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("client/orders"))
                .andExpect(model().attributeExists("orders", "currentPage"));

        verify(orderService).getOrdersByClient(eq("user@email.com"), eq(null), pageableCaptor.capture());

        Sort expectedSort = Sort.by("orderDate").descending();
        assertEquals(expectedSort, pageableCaptor.getValue().getSort());
    }

    @Test
    void listClientOrders_shouldSortByPriceAsc() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        when(orderService.getOrdersByClient(anyString(), any(), any(Pageable.class)))
                .thenReturn(orderPage);

        mockMvc.perform(get("/orders/client")
                        .param("sort", "price-asc")
                        .principal(principal))
                .andExpect(status().isOk());

        verify(orderService).getOrdersByClient(anyString(), any(), pageableCaptor.capture());

        Sort expectedSort = Sort.by("price").ascending();
        assertEquals(expectedSort, pageableCaptor.getValue().getSort());
    }

    @Test
    void listClientOrders_shouldRedirectLogin_whenPrincipalNull() throws Exception {
        mockMvc.perform(get("/orders/client"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(orderService, never()).getOrdersByClient(anyString(), any(), any());
    }

    @Test
    void listEmployeeOrders_shouldReturnView() throws Exception {
        when(orderService.getUnconfirmedOrders(any(), any(Pageable.class)))
                .thenReturn(orderPage);

        mockMvc.perform(get("/orders/employee"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/orders"))
                .andExpect(model().attributeExists("orders"));

        verify(orderService).getUnconfirmedOrders(eq(null), pageableCaptor.capture());

        Sort expectedSort = Sort.by("orderDate").ascending();
        assertEquals(expectedSort, pageableCaptor.getValue().getSort());
    }

    @Test
    void listEmployeeOrders_shouldSortByPriceDesc() throws Exception {
        when(orderService.getUnconfirmedOrders(any(), any(Pageable.class)))
                .thenReturn(orderPage);

        mockMvc.perform(get("/orders/employee")
                        .param("sort", "price-desc"))
                .andExpect(status().isOk());

        verify(orderService).getUnconfirmedOrders(any(), pageableCaptor.capture());

        Sort expectedSort = Sort.by("price").descending();
        assertEquals(expectedSort, pageableCaptor.getValue().getSort());
    }

    @Test
    void confirmOrder_shouldCallServiceAndRedirect() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("admin@email.com");

        mockMvc.perform(post("/orders/1/confirm")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/employee"));

        verify(orderService).confirmOrder(1L, "admin@email.com");
    }

    @Test
    void declineOrder_shouldCallServiceAndRedirect() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("admin@email.com");

        mockMvc.perform(post("/orders/1/decline")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/employee"));

        verify(orderService).declineOrder(1L, "admin@email.com");
    }
}
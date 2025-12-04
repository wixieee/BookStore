package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.JwtUtils;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.model.CartItem;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @Test
    void viewCart_shouldReturnCartPage_whenAuthenticated() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        Cart cart = new Cart();
        CartItem cartItem = Mockito.mock(CartItem.class);
        Book book = Mockito.mock(Book.class);

        when(book.getPrice()).thenReturn(BigDecimal.TEN);
        when(cartItem.getBook()).thenReturn(book);
        when(cartItem.getQuantity()).thenReturn(2);

        cart.setCartItems(List.of(cartItem));

        when(cartService.getCartByClientEmail("user@email.com")).thenReturn(cart);

        mockMvc.perform(get("/cart").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("cart", cart))
                .andExpect(model().attribute("totalPrice", BigDecimal.valueOf(20)));

        verify(cartService).getCartByClientEmail("user@email.com");
    }

    @Test
    void viewCart_shouldReturnCartPage_whenAuthenticatedAndCartEmpty() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        Cart cart = new Cart();
        cart.setCartItems(Collections.emptyList());

        when(cartService.getCartByClientEmail("user@email.com")).thenReturn(cart);

        mockMvc.perform(get("/cart").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("cart", cart))
                .andExpect(model().attribute("totalPrice", BigDecimal.ZERO));
    }

    @Test
    void viewCart_shouldRedirectToLogin_whenPrincipalNull() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(cartService, never()).getCartByClientEmail(anyString());
    }

    @Test
    void addToCart_shouldAddAndRedirectToReferer_whenAuthenticated() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        String bookName = "Book Name";
        String bookAuthor = "Author";
        String successMsg = "Added successfully";
        String referer = "http://localhost:8080/books";

        when(messageSource.getMessage(eq("cart.add.success"), isNull(), any(Locale.class)))
                .thenReturn(successMsg);

        mockMvc.perform(post("/cart/add")
                        .param("bookName", bookName)
                        .param("bookAuthor", bookAuthor)
                        .header("Referer", referer)
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(referer))
                .andExpect(flash().attribute("successMessage", successMsg));

        verify(cartService).addBookToCart("user@email.com", bookName, bookAuthor);
    }

    @Test
    void addToCart_shouldAddAndRedirectRoot_whenRefererMissing() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        when(messageSource.getMessage(anyString(), isNull(), any(Locale.class)))
                .thenReturn("Msg");

        mockMvc.perform(post("/cart/add")
                        .param("bookName", "Name")
                        .param("bookAuthor", "Author")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(cartService).addBookToCart(anyString(), anyString(), anyString());
    }

    @Test
    void addToCart_shouldRedirectToLogin_whenPrincipalNull() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .param("bookName", "Name")
                        .param("bookAuthor", "Author"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(cartService, never()).addBookToCart(anyString(), anyString(), anyString());
    }

    @Test
    void updateQuantity_shouldUpdateAndRedirectCart_whenAuthenticated() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        mockMvc.perform(post("/cart/update")
                        .param("cartItemId", "10")
                        .param("quantity", "5")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).updateQuantity("user@email.com", 10L, 5);
    }

    @Test
    void updateQuantity_shouldRedirectToLogin_whenPrincipalNull() throws Exception {
        mockMvc.perform(post("/cart/update")
                        .param("cartItemId", "1")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(cartService, never()).updateQuantity(anyString(), anyLong(), anyInt());
    }

    @Test
    void removeFromCart_shouldRemoveAndRedirectCart_whenAuthenticated() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        mockMvc.perform(post("/cart/remove")
                        .param("cartItemId", "5")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).removeBookFromCart("user@email.com", 5L);
    }

    @Test
    void removeFromCart_shouldRedirectToLogin_whenPrincipalNull() throws Exception {
        mockMvc.perform(post("/cart/remove")
                        .param("cartItemId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(cartService, never()).removeBookFromCart(anyString(), anyLong());
    }

    @Test
    void clearCart_shouldClearAndRedirectCart_whenAuthenticated() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        mockMvc.perform(post("/cart/clear")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).clearCart("user@email.com");
    }

    @Test
    void clearCart_shouldRedirectToLogin_whenPrincipalNull() throws Exception {
        mockMvc.perform(post("/cart/clear"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(cartService, never()).clearCart(anyString());
    }
}
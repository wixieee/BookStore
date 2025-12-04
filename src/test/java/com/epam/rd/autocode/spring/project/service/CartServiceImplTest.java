package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.model.CartItem;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.CartRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Client client;
    private Book book;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setEmail("client@test.com");

        book = new Book();
        book.setId(10L);
        book.setName("Test Book");
        book.setAuthor("Test Author");
        book.setPrice(new BigDecimal("100.00"));

        cart = new Cart();
        cart.setId(5L);
        cart.setClient(client);
        cart.setCartItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setBook(book);
        cartItem.setCart(cart);
        cartItem.setQuantity(1);
    }

    @Test
    void getCartByClientEmail_shouldReturnExistingCart() {
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartByClientEmail(client.getEmail());

        assertEquals(cart, result);
        verify(cartRepository).findByClient_Email(client.getEmail());
        verify(clientRepository, never()).findByEmail(any());
    }

    @Test
    void getCartByClientEmail_shouldCreateNewCart_whenNoneExists() {
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.empty());
        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.getCartByClientEmail(client.getEmail());

        assertEquals(cart, result);
        verify(clientRepository).findByEmail(client.getEmail());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCartByClientEmail_shouldThrowNotFound_whenClientNotExist() {
        when(cartRepository.findByClient_Email("unknown")).thenReturn(Optional.empty());
        when(clientRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartService.getCartByClientEmail("unknown"));
    }

    @Test
    void addBookToCart_shouldAddNewItem_whenBookNotInCart() {
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));
        when(bookRepository.findByNameAndAuthor(book.getName(), book.getAuthor()))
                .thenReturn(Optional.of(book));

        cartService.addBookToCart(client.getEmail(), book.getName(), book.getAuthor());

        assertEquals(1, cart.getCartItems().size());
        assertEquals(book, cart.getCartItems().get(0).getBook());
        assertEquals(1, cart.getCartItems().get(0).getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void addBookToCart_shouldIncrementQuantity_whenBookAlreadyInCart() {
        cart.getCartItems().add(cartItem);

        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));
        when(bookRepository.findByNameAndAuthor(book.getName(), book.getAuthor()))
                .thenReturn(Optional.of(book));

        cartService.addBookToCart(client.getEmail(), book.getName(), book.getAuthor());

        assertEquals(1, cart.getCartItems().size());
        assertEquals(2, cart.getCartItems().get(0).getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void addBookToCart_shouldThrowNotFound_whenBookDoesNotExist() {
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));
        when(bookRepository.findByNameAndAuthor("Unknown", "Unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                cartService.addBookToCart(client.getEmail(), "Unknown", "Unknown"));

        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeBookFromCart_shouldRemoveItem() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));

        cartService.removeBookFromCart(client.getEmail(), cartItem.getId());

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void clearCart_shouldRemoveAllItems() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));

        cartService.clearCart(client.getEmail());

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void updateQuantity_shouldUpdate_whenQuantityPositive() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));

        cartService.updateQuantity(client.getEmail(), cartItem.getId(), 5);

        assertEquals(5, cartItem.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void updateQuantity_shouldRemove_whenQuantityZero() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));

        cartService.updateQuantity(client.getEmail(), cartItem.getId(), 0);

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void updateQuantity_shouldRemove_whenQuantityNegative() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByClient_Email(client.getEmail())).thenReturn(Optional.of(cart));

        cartService.updateQuantity(client.getEmail(), cartItem.getId(), -1);

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository).save(cart);
    }
}
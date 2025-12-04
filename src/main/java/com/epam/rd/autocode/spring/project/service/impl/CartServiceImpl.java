package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final ClientRepository clientRepository;

    @Override
    public Cart getCartByClientEmail(String email) {
        return cartRepository.findByClient_Email(email)
                .orElseGet(() -> createCartForClient(email));
    }

    private Cart createCartForClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client with email " + email + " not found"));

        Cart cart = new Cart();
        cart.setClient(client);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void addBookToCart(String email, String bookName, String bookAuthor) {
        Cart cart = getCartByClientEmail(email);

        Book book = bookRepository.findByNameAndAuthor(bookName, bookAuthor)
                .orElseThrow(() -> new NotFoundException("Book with name " + bookName + " and author " + bookAuthor + " not found"));

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(book.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + 1);
        } else {
            CartItem newItem = new CartItem();
            newItem.setBook(book);
            newItem.setCart(cart);
            newItem.setQuantity(1);
            cart.getCartItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeBookFromCart(String email, Long cartItemId) {
        Cart cart = getCartByClientEmail(email);

        cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(String email) {
        Cart cart = getCartByClientEmail(email);
        cart.clear();
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateQuantity(String email, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            removeBookFromCart(email, cartItemId);
            return;
        }

        Cart cart = getCartByClientEmail(email);

        cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        cartRepository.save(cart);
    }
}
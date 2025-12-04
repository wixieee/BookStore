package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.Cart;

public interface CartService {
    Cart getCartByClientEmail(String email);

    void addBookToCart(String clientEmail, String bookName, String bookAuthor);

    void removeBookFromCart(String clientEmail, Long cartItemId);

    void clearCart(String clientEmail);

    void updateQuantity(String clientEmail, Long cartItemId, int quantity);
}
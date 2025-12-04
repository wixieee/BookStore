package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    Optional<Cart> findByClient_Email(String clientEmail);
}

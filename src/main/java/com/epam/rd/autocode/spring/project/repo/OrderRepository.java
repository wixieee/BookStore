package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT DISTINCT o FROM Order o JOIN o.bookItems bi " +
            "WHERE o.client.email = :email AND " +
            "(CAST(o.id AS string) LIKE %:search% OR LOWER(bi.book.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Order> searchByClient(@Param("email") String email, @Param("search") String search, Pageable pageable);

    Page<Order> findAllByClient_Email(String clientEmail, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.bookItems bi " +
            "WHERE o.status = :status AND " +
            "(CAST(o.id AS string) LIKE %:search% " +
            "OR LOWER(o.client.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(bi.book.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Order> searchByStatus(@Param("status") OrderStatus status, @Param("search") String search, Pageable pageable);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);
}
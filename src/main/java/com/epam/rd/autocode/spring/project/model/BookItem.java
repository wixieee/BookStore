package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "book_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;

    private Integer quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BookItem bookItem))
            return false;
        return getId() != null && getId().equals(bookItem.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

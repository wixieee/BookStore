package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    Page<BookDTO> getBooksPage(Pageable pageable);

    Page<BookDTO> searchBooks(String searchTerm, Pageable pageable);

    BookDTO getBookByNameAndAuthor(String name, String author);

    void addBook(BookDTO book);

    BookDTO getBookById(Long id);

    void updateBook(Long id, BookDTO bookDTO);

    void deleteBook(Long id);
}
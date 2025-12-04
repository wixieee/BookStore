package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<BookDTO> getBooksPage(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(book -> modelMapper.map(book, BookDTO.class));
    }

    @Override
    public Page<BookDTO> searchBooks(String searchTerm, Pageable pageable) {
        return bookRepository.searchBooks(searchTerm, pageable)
                .map(book -> modelMapper.map(book, BookDTO.class));
    }

    @Override
    public BookDTO getBookByNameAndAuthor(String name, String author) {
        Book book = bookRepository.findByNameAndAuthor(name, author)
                .orElseThrow(() -> new NotFoundException("Book with name " + name + " and author " + author + " not found"));
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Transactional
    public void addBook(BookDTO book) {
        bookExists(book.getName(), book.getAuthor());

        Book newBook = modelMapper.map(book, Book.class);
        if (newBook.getImagePath().isBlank()) {
            newBook.setImagePath(null);
        }
        Book savedBook = bookRepository.save(newBook);
        modelMapper.map(savedBook, BookDTO.class);
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id: " + id));
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Transactional
    public void updateBook(Long id, BookDTO bookDTO) {
        Book current = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        if (!current.getName().equals(bookDTO.getName()) || !current.getAuthor().equals(bookDTO.getAuthor())) {
            bookExists(bookDTO.getName(), bookDTO.getAuthor());
        }

        modelMapper.map(bookDTO, current);
        current.setId(id);

        Book saved = bookRepository.save(current);
        modelMapper.map(saved, BookDTO.class);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    private void bookExists(String name, String author) {
        if (bookRepository.existsByNameAndAuthor(name, author)) {
            throw new AlreadyExistException("Book with name " + name + " and author " + author + " already exists");
        }
    }
}

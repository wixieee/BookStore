package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        book = new Book(
                1L,
                "The Great Gatsby",
                "Classic",
                AgeGroup.ADULT,
                new BigDecimal("15.99"),
                LocalDate.of(1925, 4, 10),
                "F. Scott Fitzgerald",
                218,
                "Hardcover",
                "A story of decadence and excess.",
                Language.ENGLISH,
                "/img/gatsby.jpg"
        );

        bookDTO = new BookDTO(
                1L,
                "The Great Gatsby",
                "Classic",
                AgeGroup.ADULT,
                new BigDecimal("15.99"),
                LocalDate.of(1925, 4, 10),
                "F. Scott Fitzgerald",
                218,
                "Hardcover",
                "A story of decadence and excess.",
                Language.ENGLISH,
                "/img/gatsby.jpg"
        );
    }

    @Test
    void getBooksPage_shouldReturnPageOfDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.getBooksPage(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(bookDTO, result.getContent().get(0));
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void searchBooks_shouldReturnFilteredPage() {
        String searchTerm = "Gatsby";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.searchBooks(searchTerm, pageable)).thenReturn(bookPage);
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.searchBooks(searchTerm, pageable);

        assertEquals(1, result.getTotalElements());
        verify(bookRepository).searchBooks(searchTerm, pageable);
    }

    @Test
    void getBookByNameAndAuthor_shouldReturnDTO() {
        when(bookRepository.findByNameAndAuthor(book.getName(), book.getAuthor()))
                .thenReturn(Optional.of(book));
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookByNameAndAuthor(book.getName(), book.getAuthor());

        assertEquals(bookDTO, result);
        verify(bookRepository).findByNameAndAuthor(book.getName(), book.getAuthor());
    }

    @Test
    void getBookByNameAndAuthor_shouldThrowNotFound() {
        when(bookRepository.findByNameAndAuthor("Unknown", "Unknown"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookService.getBookByNameAndAuthor("Unknown", "Unknown"));
    }

    @Test
    void addBook_shouldSaveNewBook() {
        when(bookRepository.existsByNameAndAuthor(bookDTO.getName(), bookDTO.getAuthor()))
                .thenReturn(false);
        when(modelMapper.map(bookDTO, Book.class)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);

        bookService.addBook(bookDTO);

        verify(bookRepository).save(book);
        verify(bookRepository).existsByNameAndAuthor(bookDTO.getName(), bookDTO.getAuthor());
    }

    @Test
    void addBook_shouldSetImageToNullIfBlank() {
        bookDTO.setImagePath("   ");
        book.setImagePath("   ");

        when(bookRepository.existsByNameAndAuthor(any(), any())).thenReturn(false);
        when(modelMapper.map(bookDTO, Book.class)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);

        bookService.addBook(bookDTO);

        assertNull(book.getImagePath());
        verify(bookRepository).save(book);
    }

    @Test
    void addBook_shouldThrowAlreadyExist() {
        when(bookRepository.existsByNameAndAuthor(bookDTO.getName(), bookDTO.getAuthor()))
                .thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> bookService.addBook(bookDTO));

        verify(bookRepository, never()).save(any());
    }

    @Test
    void getBookById_shouldReturnDTO() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookById(1L);

        assertEquals(bookDTO, result);
    }

    @Test
    void getBookById_shouldThrowNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getBookById(99L));
    }

    @Test
    void updateBook_shouldUpdateWhenNoConflict() {
        Long id = 1L;
        bookDTO.setPrice(new BigDecimal("20.00"));

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        bookService.updateBook(id, bookDTO);

        verify(modelMapper).map(bookDTO, book);
        verify(bookRepository).save(book);
        assertEquals(id, book.getId());
    }

    @Test
    void updateBook_shouldCheckConflictWhenNameChanged() {
        Long id = 1L;
        String newName = "New Title";
        bookDTO.setName(newName);

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.existsByNameAndAuthor(newName, book.getAuthor())).thenReturn(false);

        bookService.updateBook(id, bookDTO);

        verify(bookRepository).existsByNameAndAuthor(newName, book.getAuthor());
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_shouldThrowAlreadyExistOnConflict() {
        Long id = 1L;
        String newName = "Existing Book";
        bookDTO.setName(newName);

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.existsByNameAndAuthor(newName, book.getAuthor())).thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> bookService.updateBook(id, bookDTO));

        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateBook_shouldThrowNotFoundIfIdInvalid() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.updateBook(99L, bookDTO));

        verify(bookRepository, never()).save(any());
    }

    @Test
    void deleteBook_shouldCallRepository() {
        bookService.deleteBook(1L);
        verify(bookRepository).deleteById(1L);
    }
}
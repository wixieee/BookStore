package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeBookController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private BookDTO bookDTO;
    private Page<BookDTO> bookPage;

    @BeforeEach
    void setUp() {
        bookDTO = new BookDTO(
                1L,
                "Test Book",
                "Genre",
                AgeGroup.ADULT,
                BigDecimal.TEN,
                LocalDate.now(),
                "Author",
                100,
                "Characteristics",
                "Description",
                Language.ENGLISH,
                "image.jpg"
        );
        bookPage = new PageImpl<>(Collections.singletonList(bookDTO), PageRequest.of(0, 9), 1);
    }

    @Test
    void showBooksPage_shouldReturnBooksPage_whenNoParams() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/employee/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/books"))
                .andExpect(model().attributeExists("books", "bookForm", "ageGroups", "languages"))
                .andExpect(model().attribute("isEditMode", false));

        verify(bookService).getBooksPage(pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(9, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void showBooksPage_shouldReturnBooksPage_withSearch() throws Exception {
        String searchTerm = "Harry";
        when(bookService.searchBooks(eq(searchTerm), any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/employee/books")
                        .param("search", searchTerm))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/books"))
                .andExpect(model().attribute("searchTerm", searchTerm));

        verify(bookService).searchBooks(eq(searchTerm), any(Pageable.class));
    }

    @Test
    void showBooksPage_shouldPrepareEditForm_whenEditIdProvided() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);
        when(bookService.getBookById(1L)).thenReturn(bookDTO);

        mockMvc.perform(get("/employee/books")
                        .param("editId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/books"))
                .andExpect(model().attribute("bookForm", bookDTO))
                .andExpect(model().attribute("isEditMode", true));

        verify(bookService).getBookById(1L);
    }

    @Test
    void saveBook_shouldCreateBook_whenIdIsNullAndValid() throws Exception {
        when(messageSource.getMessage(eq("book.create.success"), isNull(), any(Locale.class)))
                .thenReturn("Created successfully");

        mockMvc.perform(post("/employee/books/save")
                        .param("name", "New Book")
                        .param("genre", "Drama")
                        .param("ageGroup", "ADULT")
                        .param("price", "20.00")
                        .param("publicationDate", "2023-01-01")
                        .param("author", "John Doe")
                        .param("pages", "150")
                        .param("characteristics", "Hardcover")
                        .param("description", "A nice book")
                        .param("language", "ENGLISH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employee/books"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(bookService).addBook(any(BookDTO.class));
    }

    @Test
    void saveBook_shouldUpdateBook_whenIdIsNotNullAndValid() throws Exception {
        when(messageSource.getMessage(eq("book.update.success"), isNull(), any(Locale.class)))
                .thenReturn("Updated successfully");

        mockMvc.perform(post("/employee/books/save")
                        .param("id", "1")
                        .param("name", "Updated Book")
                        .param("genre", "Drama")
                        .param("ageGroup", "ADULT")
                        .param("price", "25.00")
                        .param("publicationDate", "2023-01-01")
                        .param("author", "John Doe")
                        .param("pages", "150")
                        .param("characteristics", "Hardcover")
                        .param("description", "Updated description")
                        .param("language", "ENGLISH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employee/books"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(bookService).updateBook(eq(1L), any(BookDTO.class));
    }

    @Test
    void saveBook_shouldReturnViewWithErrors_whenValidationFails() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(post("/employee/books/save")
                        .param("name", "")
                        .param("price", "-10"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/books"))
                .andExpect(model().attributeHasFieldErrors("bookForm", "name", "price"))
                .andExpect(model().attributeExists("books", "ageGroups", "languages"));

        verify(bookService, never()).addBook(any());
        verify(bookService, never()).updateBook(any(), any());
        verify(bookService).getBooksPage(any(Pageable.class));
    }

    @Test
    void saveBook_shouldReturnViewWithError_whenAlreadyExistExceptionThrown() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);
        doThrow(new AlreadyExistException("Exists")).when(bookService).addBook(any(BookDTO.class));

        mockMvc.perform(post("/employee/books/save")
                        .param("name", "Existing Book")
                        .param("genre", "Drama")
                        .param("ageGroup", "ADULT")
                        .param("price", "20.00")
                        .param("publicationDate", "2023-01-01")
                        .param("author", "John Doe")
                        .param("pages", "150")
                        .param("characteristics", "Hardcover")
                        .param("description", "A nice book")
                        .param("language", "ENGLISH"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/books"))
                .andExpect(model().attributeHasFieldErrors("bookForm", "name"));

        verify(bookService).addBook(any(BookDTO.class));
        verify(bookService).getBooksPage(any(Pageable.class));
    }

    @Test
    void deleteBook_shouldDeleteAndRedirect() throws Exception {
        when(messageSource.getMessage(eq("book.delete.success"), isNull(), any(Locale.class)))
                .thenReturn("Deleted successfully");

        mockMvc.perform(post("/employee/books/delete/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employee/books"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(bookService).deleteBook(1L);
    }

    @Test
    void cancelEdit_shouldRedirectToBooks() throws Exception {
        mockMvc.perform(get("/employee/books/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employee/books"));
    }
}
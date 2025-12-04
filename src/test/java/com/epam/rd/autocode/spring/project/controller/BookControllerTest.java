package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.JwtUtils; // Перевір пакет, якщо він відрізняється
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        bookDTO = new BookDTO(
                1L,
                "The Great Gatsby",
                "Classic",
                AgeGroup.ADULT,
                BigDecimal.valueOf(15.99),
                LocalDate.of(1925, 4, 10),
                "F. Scott Fitzgerald",
                218,
                "Hardcover",
                "A novel about the American dream.",
                Language.ENGLISH,
                "gatsby.jpg"
        );
    }

    @Test
    void getBook_shouldReturnBookDetailsPage_whenBookExists() throws Exception {
        String bookName = "The Great Gatsby";
        String authorName = "F. Scott Fitzgerald";

        when(bookService.getBookByNameAndAuthor(bookName, authorName)).thenReturn(bookDTO);

        mockMvc.perform(get("/book/{name}/{author}", bookName, authorName))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attribute("book", bookDTO));

        verify(bookService).getBookByNameAndAuthor(bookName, authorName);
    }
}
package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
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
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private Page<BookDTO> bookPage;
    private BookDTO bookDTO;

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
                "Chars",
                "Desc",
                Language.ENGLISH,
                "img.jpg"
        );
        List<BookDTO> books = Collections.singletonList(bookDTO);
        bookPage = new PageImpl<>(books, PageRequest.of(0, 8), 1);
    }

    @Test
    void index_shouldReturnDefaultPage() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("books", "currentPage", "totalPages", "pageSize", "sortParam", "searchTerm"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("sortParam", "name"))
                .andExpect(model().attribute("searchTerm", ""));

        verify(bookService).getBooksPage(pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();
        assertEquals(0, captured.getPageNumber());
        assertEquals(8, captured.getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "name"), captured.getSort());
    }

    @Test
    void index_withSearchParam_shouldCallSearchService() throws Exception {
        String searchTerm = "Harry";
        when(bookService.searchBooks(eq(searchTerm), any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/")
                        .param("search", searchTerm))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("searchTerm", searchTerm));

        verify(bookService).searchBooks(eq(searchTerm), pageableCaptor.capture());
        verify(bookService, never()).getBooksPage(any());
    }

    @Test
    void index_withEmptySearchParam_shouldCallGetBooksPage() throws Exception {
        String emptySearch = "   ";
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/")
                        .param("search", emptySearch))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("searchTerm", emptySearch));

        verify(bookService).getBooksPage(any(Pageable.class));
        verify(bookService, never()).searchBooks(anyString(), any());
    }

    @Test
    void index_withSortPriceAsc_shouldSortByPriceAsc() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/")
                        .param("sort", "price-asc"))
                .andExpect(status().isOk());

        verify(bookService).getBooksPage(pageableCaptor.capture());

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "price");
        assertEquals(expectedSort, pageableCaptor.getValue().getSort());
    }

    @Test
    void index_withSortPriceDesc_shouldSortByPriceDesc() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/")
                        .param("sort", "price-desc"))
                .andExpect(status().isOk());

        verify(bookService).getBooksPage(pageableCaptor.capture());

        Sort expectedSort = Sort.by(Sort.Direction.DESC, "price");
        assertEquals(expectedSort, pageableCaptor.getValue().getSort());
    }

    @Test
    void index_withSortAuthor_shouldSortByAuthor() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/")
                        .param("sort", "author"))
                .andExpect(status().isOk());

        verify(bookService).getBooksPage(pageableCaptor.capture());

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "author");
        assertEquals(expectedSort, pageableCaptor.getValue().getSort());
    }

    @Test
    void index_withUnknownSort_shouldFallbackToName() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/")
                        .param("sort", "unknown-param"))
                .andExpect(status().isOk());

        verify(bookService).getBooksPage(pageableCaptor.capture());

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "name");
        assertEquals(expectedSort, pageableCaptor.getValue().getSort());
    }

    @Test
    void index_withPaginationParams_shouldUseProvidedValues() throws Exception {
        when(bookService.getBooksPage(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/")
                        .param("page", "2")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 2))
                .andExpect(model().attribute("pageSize", 12));

        verify(bookService).getBooksPage(pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();
        assertEquals(2, captured.getPageNumber());
        assertEquals(12, captured.getPageSize());
    }
}
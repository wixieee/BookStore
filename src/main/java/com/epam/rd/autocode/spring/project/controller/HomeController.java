package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final BookService bookService;

    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(required = false) String search,
            Model model) {

        Sort sortObj = createSort(sort);

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<BookDTO> booksPage;
        if (search != null && !search.trim().isEmpty()) {
            booksPage = bookService.searchBooks(search, pageable);
        } else {
            booksPage = bookService.getBooksPage(pageable);
        }

        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortParam", sort);
        model.addAttribute("searchTerm", search != null ? search : "");

        return "index";
    }

    private Sort createSort(String sortParam) {
        return switch (sortParam) {
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "author" -> Sort.by(Sort.Direction.ASC, "author");
            default -> Sort.by(Sort.Direction.ASC, "name");
        };
    }
}

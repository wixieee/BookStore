package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/{name}/{author}")
    public String getBook(@PathVariable(name = "name") String name,
                          @PathVariable(name = "author") String author,
                          Model model) {

        BookDTO book = bookService.getBookByNameAndAuthor(name, author);
        model.addAttribute("book", book);
        return "book-details";
    }
}

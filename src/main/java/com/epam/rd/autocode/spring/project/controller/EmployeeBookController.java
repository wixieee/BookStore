package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/employee/books")
@RequiredArgsConstructor
public class EmployeeBookController {

    private final BookService bookService;
    private final MessageSource messageSource;

    @GetMapping
    public String showBooksPage(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "9") int size,
                                @RequestParam(required = false) String search,
                                @RequestParam(defaultValue = "id") String sort,
                                @RequestParam(required = false) Long editId) {

        prepareBooksList(model, page, size, search, sort);

        if (editId != null) {
            BookDTO bookToEdit = bookService.getBookById(editId);
            model.addAttribute("bookForm", bookToEdit);
            model.addAttribute("isEditMode", true);
        } else {
            if (!model.containsAttribute("bookForm")) {
                model.addAttribute("bookForm", new BookDTO());
            }
            model.addAttribute("isEditMode", false);
        }

        model.addAttribute("ageGroups", AgeGroup.values());
        model.addAttribute("languages", Language.values());

        return "employee/books";
    }

    @PostMapping("/save")
    public String saveBook(@Valid @ModelAttribute("bookForm") BookDTO bookDTO,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           Locale locale,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "id") String sort) {

        if (bindingResult.hasErrors()) {
            prepareBooksList(model, page, 10, search, sort);
            model.addAttribute("ageGroups", AgeGroup.values());
            model.addAttribute("languages", Language.values());
            model.addAttribute("isEditMode", bookDTO.getId() != null);
            return "employee/books";
        }

        String message;

        try {
            if (bookDTO.getId() != null) {
                bookService.updateBook(bookDTO.getId(), bookDTO);
                message = messageSource.getMessage("book.update.success", null, locale);
                redirectAttributes.addFlashAttribute("successMessage", message);
            } else {
                bookService.addBook(bookDTO);
                message = messageSource.getMessage("book.create.success", null, locale);
                redirectAttributes.addFlashAttribute("successMessage", message);
            }
        } catch (AlreadyExistException e) {
            bindingResult.rejectValue("name", "book.update.exists.error");
            prepareBooksList(model, page, 10, search, sort);
            model.addAttribute("ageGroups", AgeGroup.values());
            model.addAttribute("languages", Language.values());
            model.addAttribute("isEditMode", bookDTO.getId() != null);
            return "employee/books";
        }

        return "redirect:/employee/books";
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes, Locale locale) {
        bookService.deleteBook(id);
        String message = messageSource.getMessage("book.delete.success", null, locale);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/employee/books";
    }

    @GetMapping("/cancel")
    public String cancelEdit() {
        return "redirect:/employee/books";
    }

    private void prepareBooksList(Model model, int page, int size, String search, String sortParam) {
        Sort sort = Sort.by(sortParam);
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<BookDTO> books;
        if (search != null && !search.isBlank()) {
            books = bookService.searchBooks(search, pageable);
        } else {
            books = bookService.getBooksPage(pageable);
        }

        model.addAttribute("books", books.getContent());
        model.addAttribute("currentPage", books.getNumber());
        model.addAttribute("totalPages", books.getTotalPages());
        model.addAttribute("searchTerm", search);
        model.addAttribute("sortParam", sortParam);
    }
}
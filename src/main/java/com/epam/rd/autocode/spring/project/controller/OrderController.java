package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Locale;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MessageSource messageSource;

    @PostMapping("/create")
    public String createOrder(Principal principal,
                              RedirectAttributes redirectAttributes,
                              Locale locale) {
        if (principal == null) {
            return "redirect:/login";
        }

        String message;

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail(principal.getName());

        try{
            orderService.addOrder(orderDTO);

            message = messageSource.getMessage(
                    "checkout.success",
                    null,
                    locale);

            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (InsufficientFundsException e){
            message = messageSource.getMessage(
                    "checkout.error.insufficientFunds",
                    null,
                    locale);

            redirectAttributes.addFlashAttribute("errorMessage", message);
            return "redirect:/cart";
        }

        return "redirect:/";
    }

    @GetMapping("/client")
    public String listClientOrders(Model model,
                                   Principal principal,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "5") int size,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(defaultValue = "date-desc") String sort) {
        if (principal == null) return "redirect:/login";

        PageRequest pageable = createPageRequest(page, size, sort);
        Page<OrderDTO> ordersPage = orderService.getOrdersByClient(principal.getName(), search, pageable);

        addAttributes(model, ordersPage, search, sort);
        return "client/orders";
    }

    @GetMapping("/employee")
    public String listEmployeeOrders(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "5") int size,
                                     @RequestParam(required = false) String search,
                                     @RequestParam(defaultValue = "date-asc") String sort) {

        PageRequest pageable = createPageRequest(page, size, sort);
        Page<OrderDTO> ordersPage = orderService.getUnconfirmedOrders(search, pageable);

        addAttributes(model, ordersPage, search, sort);
        return "employee/orders";
    }

    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable Long id, Principal principal) {
        orderService.confirmOrder(id, principal.getName());
        return "redirect:/orders/employee";
    }

    @PostMapping("/{id}/decline")
    public String declineOrder(@PathVariable Long id, Principal principal) {
        orderService.declineOrder(id, principal.getName());
        return "redirect:/orders/employee";
    }

    private PageRequest createPageRequest(int page, int size, String sortParam) {
        Sort sort;

        switch (sortParam) {
            case "date-asc" -> sort = Sort.by("orderDate").ascending();
            case "price-desc" -> sort = Sort.by("price").descending();
            case "price-asc" -> sort = Sort.by("price").ascending();
            default -> sort = Sort.by("orderDate").descending();
        }

        return PageRequest.of(page, size, sort);
    }

    private void addAttributes(Model model, Page<OrderDTO> page, String search, String sort) {
        model.addAttribute("orders", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("searchTerm", search);
        model.addAttribute("sortParam", sort);
        model.addAttribute("pageSize", page.getSize());
    }
}

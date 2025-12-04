package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Locale;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final MessageSource messageSource;

    @GetMapping
    public String viewCart(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Cart cart = cartService.getCartByClientEmail(principal.getName());

        BigDecimal totalPrice = cart.getCartItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam String bookName,
                            @RequestParam String bookAuthor,
                            Principal principal,
                            RedirectAttributes redirectAttributes,
                            Locale locale,
                            HttpServletRequest request) {
        if (principal == null) return "redirect:/login";

        String message;

        cartService.addBookToCart(principal.getName(), bookName, bookAuthor);
        message = messageSource.getMessage(
                    "cart.add.success",
                    null,
                    locale
        );
        redirectAttributes.addFlashAttribute("successMessage", message);

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }

        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long cartItemId,
                                 @RequestParam int quantity,
                                 Principal principal) {
        if (principal == null) return "redirect:/login";

        cartService.updateQuantity(principal.getName(), cartItemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long cartItemId, Principal principal) {
        if (principal == null) return "redirect:/login";

        cartService.removeBookFromCart(principal.getName(), cartItemId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(Principal principal) {
        if (principal == null) return "redirect:/login";
        cartService.clearCart(principal.getName());
        return "redirect:/cart";
    }
}
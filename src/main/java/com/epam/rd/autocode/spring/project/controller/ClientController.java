package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientRegisterDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientController {

    private final ClientService clientService;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("clientRegisterDTO", new ClientRegisterDTO());
        return "client/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute @Valid ClientRegisterDTO clientRegisterDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        if (bindingResult.hasErrors()) {
            return "client/register";
        }

        if (!clientRegisterDTO.getPassword().equals(clientRegisterDTO.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "registration.password.mismatch");
            return "client/register";
        }

        String message = messageSource.getMessage(
                "registration.success",
                null,
                locale);

        try {
            clientService.addClient(modelMapper.map(clientRegisterDTO, ClientDTO.class));
        } catch (AlreadyExistException e) {
            bindingResult.rejectValue("email", "registration.email.taken");
            return "client/register";
        }

        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/login";
    }
}
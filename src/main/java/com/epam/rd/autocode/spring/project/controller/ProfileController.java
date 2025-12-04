package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.PasswordTooShortException;
import com.epam.rd.autocode.spring.project.exception.PasswordWhitespaceException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Locale;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final MessageSource messageSource;

    @GetMapping("/client")
    public String showClientProfile(Model model, Principal principal) {
        String email = principal.getName();
        ClientDTO clientDTO = clientService.getClientByEmail(email);
        clientDTO.setPassword("");

        model.addAttribute("client", clientDTO);
        return "client/profile";
    }

    @PostMapping("/client/update")
    public String updateClientProfile(@Valid @ModelAttribute("client") ClientDTO clientDTO,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      HttpServletResponse response,
                                      RedirectAttributes redirectAttributes,
                                      Locale locale) {

        if (bindingResult.hasErrors()) {
            restoreBalance(clientDTO, principal.getName());
            return "client/profile";
        }

        try {
            String oldEmail = principal.getName();
            ClientDTO updatedClient = clientService.updateClientByEmail(oldEmail, clientDTO);

            if (!oldEmail.equals(updatedClient.getEmail())) {
                updateSessionCookie(updatedClient.getEmail(), response);
            }

            String message = messageSource.getMessage("profile.update.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", message);

            return "redirect:/profile/client";

        } catch (PasswordTooShortException e) {
            bindingResult.rejectValue("password", "validation.password.size");
            restoreBalance(clientDTO, principal.getName());
            return "client/profile";

        } catch (PasswordWhitespaceException e) {
            bindingResult.rejectValue("password", "validation.password.whitespace");
            restoreBalance(clientDTO, principal.getName());
            return "client/profile";

        } catch (AlreadyExistException e) {
            bindingResult.rejectValue("email", "profile.update.email.taken");
            restoreBalance(clientDTO, principal.getName());
            return "client/profile";
        }
    }

    @PostMapping("/client/delete")
    public String deleteClientAccount(Principal principal,
                                      HttpServletResponse response,
                                      RedirectAttributes redirectAttributes,
                                      Locale locale) {
        clientService.deleteClientByEmail(principal.getName());
        clearSessionCookie(response);

        String message = messageSource.getMessage(
                "profile.delete.success",
                null,
                locale);

        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/";
    }

    @GetMapping("/employee")
    public String showEmployeeProfile(Model model, Principal principal) {
        String email = principal.getName();
        EmployeeDTO employeeDTO = employeeService.getEmployeeByEmail(email);
        employeeDTO.setPassword("");

        model.addAttribute("employee", employeeDTO);
        return "employee/profile";
    }

    @PostMapping("/employee/update")
    public String updateEmployeeProfile(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                                        BindingResult bindingResult,
                                        Principal principal,
                                        HttpServletResponse response,
                                        RedirectAttributes redirectAttributes,
                                        Locale locale) {

        if (bindingResult.hasErrors()) {
            return "employee/profile";
        }

        try {
            String oldEmail = principal.getName();
            EmployeeDTO updatedEmployee = employeeService.updateEmployeeByEmail(oldEmail, employeeDTO);

            if (!oldEmail.equals(updatedEmployee.getEmail())) {
                updateSessionCookie(updatedEmployee.getEmail(), response);
            }

            String message = messageSource.getMessage("profile.update.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", message);

            return "redirect:/profile/employee";

        } catch (PasswordTooShortException e) {
            bindingResult.rejectValue("password", "validation.password.size");
            return "employee/profile";

        } catch (PasswordWhitespaceException e) {
            bindingResult.rejectValue("password", "validation.password.whitespace");
            return "employee/profile";

        } catch (AlreadyExistException e) {
            bindingResult.rejectValue("email", "profile.update.email.taken");
            return "employee/profile";
        }
    }

    @PostMapping("/employee/delete")
    public String deleteEmployeeAccount(Principal principal,
                                        HttpServletResponse response,
                                        RedirectAttributes redirectAttributes,
                                        Locale locale) {
        employeeService.deleteEmployeeByEmail(principal.getName());
        clearSessionCookie(response);

        String message = messageSource.getMessage("profile.delete.success", null, locale);
        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/";
    }

    private void restoreBalance(ClientDTO clientDTO, String email) {
        ClientDTO dbClient = clientService.getClientByEmail(email);
        clientDTO.setBalance(dbClient.getBalance());
    }

    private void updateSessionCookie(String newEmail, HttpServletResponse response) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(newEmail);

        String newJwt = jwtUtils.generateToken(userDetails);

        Cookie cookie = new Cookie("JWT_TOKEN", newJwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
    }

    private void clearSessionCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

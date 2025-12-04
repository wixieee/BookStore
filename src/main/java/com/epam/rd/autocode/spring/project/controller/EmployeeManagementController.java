package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeRegisterDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
@RequestMapping("/employee/users")
@RequiredArgsConstructor
public class EmployeeManagementController {

    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;

    @GetMapping
    public String showUsersPage(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(required = false) String search,
                                @RequestParam(defaultValue = "name") String sort) {

        prepareUsersList(model, page, size, search, sort);

        if (!model.containsAttribute("newEmployee")) {
            model.addAttribute("newEmployee", new EmployeeRegisterDTO());
        }

        return "employee/users";
    }

    @PostMapping("/add-employee")
    public String createEmployee(@Valid @ModelAttribute("newEmployee") EmployeeRegisterDTO registerDTO,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "") String search,
                                 @RequestParam(defaultValue = "name") String sort) {

        if (!registerDTO.getPassword().equals(registerDTO.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "registration.password.mismatch");
        }

        if (bindingResult.hasErrors()) {
            prepareUsersList(model, page, 5, search, sort);
            return "employee/users";
        }

        try {
            EmployeeDTO employeeDTO = modelMapper.map(registerDTO, EmployeeDTO.class);
            employeeService.addEmployee(employeeDTO);

            String message = messageSource.getMessage("registration.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", message);

            return "redirect:/employee/users";

        } catch (AlreadyExistException e) {
            bindingResult.rejectValue("email", "registration.email.taken");

            prepareUsersList(model, page, 5, search, sort);

            return "employee/users";
        }
    }

    @PostMapping("/client/status")
    public String toggleClientStatus(@RequestParam("email") String email,
                                     @RequestParam("blocked") boolean blocked,
                                     RedirectAttributes redirectAttributes,
                                     Locale locale) {

        clientService.updateClientStatus(email, blocked);

        String msgKey = blocked ? "user.blocked.success" : "user.unblocked.success";

        String message = messageSource.getMessage(msgKey, null, locale);
        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/employee/users";
    }


    private void prepareUsersList(Model model, int page, int size, String search, String sortParam) {
        Sort sort = Sort.by("name");
        if ("balance".equals(sortParam)) {
            sort = Sort.by("balance").descending();
        } else if ("email".equals(sortParam)) {
            sort = Sort.by("email");
        }

        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<ClientDTO> clients = clientService.getClients(search, pageable);

        model.addAttribute("clients", clients.getContent());
        model.addAttribute("currentPage", clients.getNumber());
        model.addAttribute("totalPages", clients.getTotalPages());
        model.addAttribute("searchTerm", search);
        model.addAttribute("sortParam", sortParam);
    }
}
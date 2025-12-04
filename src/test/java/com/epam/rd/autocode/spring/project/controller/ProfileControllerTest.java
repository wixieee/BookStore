package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.PasswordTooShortException;
import com.epam.rd.autocode.spring.project.exception.PasswordWhitespaceException;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private UserRepository userRepository;

    private ClientDTO clientDTO;
    private EmployeeDTO employeeDTO;
    private Principal principal;

    @BeforeEach
    void setUp() {
        principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("user@email.com");

        clientDTO = new ClientDTO();
        clientDTO.setEmail("user@email.com");
        clientDTO.setName("Client Name");
        clientDTO.setBalance(BigDecimal.valueOf(100));
        clientDTO.setBlocked(false);

        employeeDTO = new EmployeeDTO();
        employeeDTO.setEmail("emp@email.com");
        employeeDTO.setName("Emp Name");
        employeeDTO.setPhone("1234567890");
        employeeDTO.setBirthDate(LocalDate.of(1990, 1, 1));
    }

    @Test
    void showClientProfile_shouldReturnClientProfileView() throws Exception {
        when(clientService.getClientByEmail("user@email.com")).thenReturn(clientDTO);

        mockMvc.perform(get("/profile/client")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"))
                .andExpect(model().attributeExists("client"))
                .andExpect(model().attribute("client", clientDTO));

        verify(clientService).getClientByEmail("user@email.com");
    }

    @Test
    void updateClientProfile_shouldUpdateAndRedirect_whenValid() throws Exception {
        when(clientService.updateClientByEmail(eq("user@email.com"), any(ClientDTO.class)))
                .thenReturn(clientDTO);
        when(messageSource.getMessage(eq("profile.update.success"), isNull(), any(Locale.class)))
                .thenReturn("Updated successfully");

        mockMvc.perform(post("/profile/client/update")
                        .principal(principal)
                        .flashAttr("client", clientDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/client"))
                .andExpect(flash().attribute("successMessage", "Updated successfully"));

        verify(clientService).updateClientByEmail(eq("user@email.com"), any(ClientDTO.class));
        verify(jwtUtils, never()).generateToken(any());
    }

    @Test
    void updateClientProfile_shouldUpdateCookie_whenEmailChanged() throws Exception {
        ClientDTO updatedClient = new ClientDTO();
        updatedClient.setEmail("new@email.com");
        updatedClient.setName("Name");

        when(clientService.updateClientByEmail(eq("user@email.com"), any(ClientDTO.class)))
                .thenReturn(updatedClient);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("new@email.com")).thenReturn(userDetails);
        when(jwtUtils.generateToken(userDetails)).thenReturn("newToken");

        mockMvc.perform(post("/profile/client/update")
                        .principal(principal)
                        .flashAttr("client", updatedClient))
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().value("JWT_TOKEN", "newToken"));

        verify(jwtUtils).generateToken(userDetails);
    }

    @Test
    void updateClientProfile_shouldReturnView_whenValidationFails() throws Exception {
        clientDTO.setName("");

        when(clientService.getClientByEmail("user@email.com")).thenReturn(clientDTO);

        mockMvc.perform(post("/profile/client/update")
                        .principal(principal)
                        .flashAttr("client", clientDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"))
                .andExpect(model().attributeHasFieldErrors("client", "name"));

        verify(clientService, never()).updateClientByEmail(anyString(), any());
    }

    @Test
    void updateClientProfile_shouldHandlePasswordTooShortException() throws Exception {
        when(clientService.updateClientByEmail(anyString(), any()))
                .thenThrow(new PasswordTooShortException("Short"));
        when(clientService.getClientByEmail("user@email.com")).thenReturn(clientDTO);

        mockMvc.perform(post("/profile/client/update")
                        .principal(principal)
                        .flashAttr("client", clientDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"))
                .andExpect(model().attributeHasFieldErrors("client", "password"));
    }

    @Test
    void updateClientProfile_shouldHandlePasswordWhitespaceException() throws Exception {
        when(clientService.updateClientByEmail(anyString(), any()))
                .thenThrow(new PasswordWhitespaceException("Space"));
        when(clientService.getClientByEmail("user@email.com")).thenReturn(clientDTO);

        mockMvc.perform(post("/profile/client/update")
                        .principal(principal)
                        .flashAttr("client", clientDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"))
                .andExpect(model().attributeHasFieldErrors("client", "password"));
    }

    @Test
    void updateClientProfile_shouldHandleAlreadyExistException() throws Exception {
        when(clientService.updateClientByEmail(anyString(), any()))
                .thenThrow(new AlreadyExistException("Exists"));
        when(clientService.getClientByEmail("user@email.com")).thenReturn(clientDTO);

        mockMvc.perform(post("/profile/client/update")
                        .principal(principal)
                        .flashAttr("client", clientDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"))
                .andExpect(model().attributeHasFieldErrors("client", "email"));
    }

    @Test
    void deleteClientAccount_shouldDeleteAndClearCookie() throws Exception {
        when(messageSource.getMessage(eq("profile.delete.success"), isNull(), any(Locale.class)))
                .thenReturn("Deleted");

        mockMvc.perform(post("/profile/client/delete")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().maxAge("JWT_TOKEN", 0))
                .andExpect(flash().attribute("successMessage", "Deleted"));

        verify(clientService).deleteClientByEmail("user@email.com");
    }

    @Test
    void showEmployeeProfile_shouldReturnEmployeeProfileView() throws Exception {
        when(principal.getName()).thenReturn("emp@email.com");
        when(employeeService.getEmployeeByEmail("emp@email.com")).thenReturn(employeeDTO);

        mockMvc.perform(get("/profile/employee")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/profile"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attribute("employee", employeeDTO));
    }

    @Test
    void updateEmployeeProfile_shouldUpdateAndRedirect_whenValid() throws Exception {
        when(principal.getName()).thenReturn("emp@email.com");
        when(employeeService.updateEmployeeByEmail(eq("emp@email.com"), any(EmployeeDTO.class)))
                .thenReturn(employeeDTO);
        when(messageSource.getMessage(eq("profile.update.success"), isNull(), any(Locale.class)))
                .thenReturn("Updated successfully");

        mockMvc.perform(post("/profile/employee/update")
                        .principal(principal)
                        .flashAttr("employee", employeeDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/employee"))
                .andExpect(flash().attribute("successMessage", "Updated successfully"));

        verify(employeeService).updateEmployeeByEmail(eq("emp@email.com"), any(EmployeeDTO.class));
    }

    @Test
    void updateEmployeeProfile_shouldUpdateCookie_whenEmailChanged() throws Exception {
        when(principal.getName()).thenReturn("emp@email.com");
        EmployeeDTO updatedEmployee = new EmployeeDTO();
        updatedEmployee.setEmail("newemp@email.com");
        updatedEmployee.setName("Name");
        updatedEmployee.setPhone("1234567890");
        updatedEmployee.setBirthDate(LocalDate.of(1990, 1, 1));

        when(employeeService.updateEmployeeByEmail(eq("emp@email.com"), any(EmployeeDTO.class)))
                .thenReturn(updatedEmployee);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("newemp@email.com")).thenReturn(userDetails);
        when(jwtUtils.generateToken(userDetails)).thenReturn("newToken");

        mockMvc.perform(post("/profile/employee/update")
                        .principal(principal)
                        .flashAttr("employee", updatedEmployee))
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().value("JWT_TOKEN", "newToken"));
    }

    @Test
    void updateEmployeeProfile_shouldReturnView_whenValidationFails() throws Exception {
        employeeDTO.setName("");

        mockMvc.perform(post("/profile/employee/update")
                        .principal(principal)
                        .flashAttr("employee", employeeDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/profile"))
                .andExpect(model().attributeHasFieldErrors("employee", "name"));

        verify(employeeService, never()).updateEmployeeByEmail(anyString(), any());
    }

    @Test
    void updateEmployeeProfile_shouldHandlePasswordTooShortException() throws Exception {
        when(principal.getName()).thenReturn("emp@email.com");
        when(employeeService.updateEmployeeByEmail(anyString(), any()))
                .thenThrow(new PasswordTooShortException("Short"));

        mockMvc.perform(post("/profile/employee/update")
                        .principal(principal)
                        .flashAttr("employee", employeeDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/profile"))
                .andExpect(model().attributeHasFieldErrors("employee", "password"));
    }

    @Test
    void updateEmployeeProfile_shouldHandlePasswordWhitespaceException() throws Exception {
        when(principal.getName()).thenReturn("emp@email.com");
        when(employeeService.updateEmployeeByEmail(anyString(), any()))
                .thenThrow(new PasswordWhitespaceException("Space"));

        mockMvc.perform(post("/profile/employee/update")
                        .principal(principal)
                        .flashAttr("employee", employeeDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/profile"))
                .andExpect(model().attributeHasFieldErrors("employee", "password"));
    }

    @Test
    void updateEmployeeProfile_shouldHandleAlreadyExistException() throws Exception {
        when(principal.getName()).thenReturn("emp@email.com");
        when(employeeService.updateEmployeeByEmail(anyString(), any()))
                .thenThrow(new AlreadyExistException("Exists"));

        mockMvc.perform(post("/profile/employee/update")
                        .principal(principal)
                        .flashAttr("employee", employeeDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/profile"))
                .andExpect(model().attributeHasFieldErrors("employee", "email"));
    }

    @Test
    void deleteEmployeeAccount_shouldDeleteAndClearCookie() throws Exception {
        when(principal.getName()).thenReturn("emp@email.com");
        when(messageSource.getMessage(eq("profile.delete.success"), isNull(), any(Locale.class)))
                .thenReturn("Deleted");

        mockMvc.perform(post("/profile/employee/delete")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().maxAge("JWT_TOKEN", 0))
                .andExpect(flash().attribute("successMessage", "Deleted"));

        verify(employeeService).deleteEmployeeByEmail("emp@email.com");
    }
}
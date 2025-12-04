package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeRegisterDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ModelMapper modelMapper;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private Page<ClientDTO> clientPage;
    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        clientDTO = new ClientDTO();
        clientDTO.setEmail("client@email.com");
        clientDTO.setName("Client Name");
        clientDTO.setBalance(BigDecimal.valueOf(100));
        clientDTO.setBlocked(false);

        clientPage = new PageImpl<>(Collections.singletonList(clientDTO), PageRequest.of(0, 5), 1);
    }

    @Test
    void showUsersPage_shouldReturnUsersPage() throws Exception {
        when(clientService.getClients(any(), any(Pageable.class))).thenReturn(clientPage);

        mockMvc.perform(get("/employee/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/users"))
                .andExpect(model().attributeExists("clients", "newEmployee", "currentPage", "totalPages"));

        verify(clientService).getClients(eq(null), pageableCaptor.capture());
        assertEquals(Sort.by("name"), pageableCaptor.getValue().getSort());
    }

    @Test
    void showUsersPage_shouldSortByBalanceDesc() throws Exception {
        when(clientService.getClients(any(), any(Pageable.class))).thenReturn(clientPage);

        mockMvc.perform(get("/employee/users")
                        .param("sort", "balance"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/users"));

        verify(clientService).getClients(eq(null), pageableCaptor.capture());
        assertEquals(Sort.by("balance").descending(), pageableCaptor.getValue().getSort());
    }

    @Test
    void createEmployee_shouldAddEmployeeAndRedirect_whenValid() throws Exception {
        EmployeeRegisterDTO registerDTO = new EmployeeRegisterDTO();
        registerDTO.setEmail("emp@email.com");
        registerDTO.setPassword("password123");
        registerDTO.setPasswordConfirm("password123");
        registerDTO.setName("Emp Name");
        registerDTO.setPhone("1234567890");
        registerDTO.setBirthDate(LocalDate.of(1990, 1, 1));

        EmployeeDTO employeeDTO = new EmployeeDTO();
        when(modelMapper.map(any(), eq(EmployeeDTO.class))).thenReturn(employeeDTO);
        when(messageSource.getMessage(eq("registration.success"), isNull(), any(Locale.class)))
                .thenReturn("Success");

        mockMvc.perform(post("/employee/users/add-employee")
                        .flashAttr("newEmployee", registerDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employee/users"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(employeeService).addEmployee(any(EmployeeDTO.class));
    }

    @Test
    void createEmployee_shouldReturnViewWithErrors_whenPasswordsMismatch() throws Exception {
        when(clientService.getClients(any(), any(Pageable.class))).thenReturn(clientPage);

        EmployeeRegisterDTO registerDTO = new EmployeeRegisterDTO();
        registerDTO.setEmail("emp@email.com");
        registerDTO.setPassword("password123");
        registerDTO.setPasswordConfirm("password321");
        registerDTO.setName("Emp Name");
        registerDTO.setPhone("1234567890");
        registerDTO.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/employee/users/add-employee")
                        .flashAttr("newEmployee", registerDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/users"))
                .andExpect(model().attributeHasFieldErrors("newEmployee", "passwordConfirm"));

        verify(employeeService, never()).addEmployee(any());
    }

    @Test
    void createEmployee_shouldReturnViewWithErrors_whenValidationFails() throws Exception {
        when(clientService.getClients(any(), any(Pageable.class))).thenReturn(clientPage);

        EmployeeRegisterDTO registerDTO = new EmployeeRegisterDTO();
        registerDTO.setPassword("short");
        registerDTO.setPasswordConfirm("short");

        mockMvc.perform(post("/employee/users/add-employee")
                        .flashAttr("newEmployee", registerDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/users"))
                .andExpect(model().attributeHasErrors("newEmployee"));

        verify(employeeService, never()).addEmployee(any());
    }

    @Test
    void createEmployee_shouldReturnViewWithError_whenAlreadyExistException() throws Exception {
        when(clientService.getClients(any(), any(Pageable.class))).thenReturn(clientPage);
        when(modelMapper.map(any(), eq(EmployeeDTO.class))).thenReturn(new EmployeeDTO());

        doThrow(new AlreadyExistException("Exists")).when(employeeService).addEmployee(any());

        EmployeeRegisterDTO registerDTO = new EmployeeRegisterDTO();
        registerDTO.setEmail("exist@email.com");
        registerDTO.setPassword("password123");
        registerDTO.setPasswordConfirm("password123");
        registerDTO.setName("Name");
        registerDTO.setPhone("1234567890");
        registerDTO.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/employee/users/add-employee")
                        .flashAttr("newEmployee", registerDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/users"))
                .andExpect(model().attributeHasFieldErrors("newEmployee", "email"));

        verify(employeeService).addEmployee(any());
    }

    @Test
    void toggleClientStatus_shouldBlockAndRedirect() throws Exception {
        when(messageSource.getMessage(eq("user.blocked.success"), isNull(), any(Locale.class)))
                .thenReturn("Blocked");

        mockMvc.perform(post("/employee/users/client/status")
                        .param("email", "client@email.com")
                        .param("blocked", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employee/users"))
                .andExpect(flash().attribute("successMessage", "Blocked"));

        verify(clientService).updateClientStatus("client@email.com", true);
    }

    @Test
    void toggleClientStatus_shouldUnblockAndRedirect() throws Exception {
        when(messageSource.getMessage(eq("user.unblocked.success"), isNull(), any(Locale.class)))
                .thenReturn("Unblocked");

        mockMvc.perform(post("/employee/users/client/status")
                        .param("email", "client@email.com")
                        .param("blocked", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employee/users"))
                .andExpect(flash().attribute("successMessage", "Unblocked"));

        verify(clientService).updateClientStatus("client@email.com", false);
    }
}
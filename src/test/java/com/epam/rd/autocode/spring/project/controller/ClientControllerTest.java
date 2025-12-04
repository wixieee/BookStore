package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientRegisterDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private ModelMapper modelMapper;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    private ClientDTO mockClientDTO;

    @BeforeEach
    void setUp() {
        mockClientDTO = new ClientDTO(
                "example@example.com",
                "example_password",
                "example_name",
                BigDecimal.valueOf(100L),
                false);
    }

    @Test
    void register_shouldReturnModelAndView() throws Exception {
        mockMvc.perform(get("/client/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/register"))
                .andExpect(model().attributeExists("clientRegisterDTO"));
    }

    @Test
    void register_shouldPerformSuccessfully() throws Exception {
        String expectedMessage = "Registration successful! Please login.";

        when(modelMapper.map(any(ClientRegisterDTO.class), eq(ClientDTO.class))).thenReturn(mockClientDTO);
        when(messageSource.getMessage(eq("registration.success"), isNull(), any(Locale.class))).thenReturn(expectedMessage);

        mockMvc.perform(post("/client/register")
                        .param("name", "example_name")
                        .param("email", "example@example.com")
                        .param("password", "example_password")
                        .param("passwordConfirm", "example_password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("successMessage", expectedMessage))
                .andExpect(model().hasNoErrors());

        verify(clientService, times(1)).addClient(mockClientDTO);
    }

    @Test
    void register_shouldReturnValidationError() throws Exception {
        mockMvc.perform(post("/client/register")
                        .param("name", "")
                        .param("email", "wrong_email")
                        .param("password", "12")
                        .param("passwordConfirm", "12"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/register"))
                .andExpect(model().attributeHasFieldErrorCode("clientRegisterDTO", "name", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode("clientRegisterDTO", "email", "Email"))
                .andExpect(model().attributeHasFieldErrorCode("clientRegisterDTO", "password", "Size"));

        verifyNoInteractions(clientService);
    }

    @Test
    void register_shouldReturnErrorOnPasswordMismatch() throws Exception {
        mockMvc.perform(post("/client/register")
                        .param("name", "example_name")
                        .param("email", "example@example.com")
                        .param("password", "password123")
                        .param("passwordConfirm", "wrong_password"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/register"))
                .andExpect(model().attributeHasFieldErrorCode("clientRegisterDTO", "passwordConfirm",
                        "registration.password.mismatch"));

        verifyNoInteractions(clientService);
    }

    @Test
    void register_shouldThrowErrorOnTakenEmail() throws Exception {
        when(modelMapper.map(any(ClientRegisterDTO.class), eq(ClientDTO.class))).thenReturn(mockClientDTO);

        doThrow(new AlreadyExistException("Email taken"))
                .when(clientService).addClient(any(ClientDTO.class));

        when(messageSource.getMessage(eq("registration.success"), isNull(), any(Locale.class))).thenReturn("Success");

        mockMvc.perform(post("/client/register")
                        .param("name", "John")
                        .param("email", "example@example.com")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/register"))
                .andExpect(model().attributeHasFieldErrorCode("clientRegisterDTO", "email", "registration.email.taken"));

        verify(clientService, times(1)).addClient(mockClientDTO);
    }
}
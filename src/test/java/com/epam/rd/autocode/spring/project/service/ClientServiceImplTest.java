package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.PasswordTooShortException;
import com.epam.rd.autocode.spring.project.exception.PasswordWhitespaceException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client1;
    private Client client2;
    private ClientDTO dto1;
    private ClientDTO dto2;
    private List<Client> mockClients;

    @BeforeEach
    void setUp() {
        client1 = new Client(1L, "c1@test.com", "pass1", "Client A", new BigDecimal("100.00"), false);
        client2 = new Client(2L, "c2@test.com", "pass2", "Client B", new BigDecimal("200.00"), false);
        mockClients = List.of(client1, client2);

        dto1 = new ClientDTO("c1@test.com", "pass1", "Client A", new BigDecimal("100.00"), false);
        dto2 = new ClientDTO("c2@test.com", "pass2", "Client B", new BigDecimal("200.00"), false);
    }

    @Test
    void getAllClients_shouldReturnListOfClientDTOs() {
        when(clientRepository.findAll()).thenReturn(mockClients);
        when(modelMapper.map(client1, ClientDTO.class)).thenReturn(dto1);
        when(modelMapper.map(client2, ClientDTO.class)).thenReturn(dto2);

        List<ClientDTO> result = clientService.getAllClients();

        verify(clientRepository, times(1)).findAll();
        verify(modelMapper, times(2)).map(any(Client.class), eq(ClientDTO.class));
        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
    }

    @Test
    void getClientByEmail_shouldReturnClientDTO() {
        when(clientRepository.findByEmail("c1@test.com")).thenReturn(Optional.of(client1));
        when(modelMapper.map(client1, ClientDTO.class)).thenReturn(dto1);

        ClientDTO result = clientService.getClientByEmail("c1@test.com");

        verify(clientRepository, times(1)).findByEmail("c1@test.com");
        verify(modelMapper, times(1)).map(client1, ClientDTO.class);
        assertEquals(dto1, result);
    }

    @Test
    void getClientByEmail_shouldThrowNotFoundException() {
        when(clientRepository.findByEmail("test")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> clientService.getClientByEmail("test"));
        assertEquals("Client with email test not found", exception.getMessage());
        verify(clientRepository, times(1)).findByEmail("test");
    }

    @Test
    void updateClientByEmail_shouldUpdateAndProduceDTO() {
        String email = client1.getEmail();
        String newPasswordRaw = "new_secure_pass";
        String newPasswordEncoded = "encoded_new_pass";

        ClientDTO updateInput = new ClientDTO(
                email,
                newPasswordRaw,
                "New Client Name",
                new BigDecimal("500.00"),
                false);

        Client saved = new Client(
                1L,
                email,
                newPasswordEncoded,
                "New Client Name",
                new BigDecimal("500.00"),
                false);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client1));
        when(passwordEncoder.encode(newPasswordRaw)).thenReturn(newPasswordEncoded);

        when(clientRepository.save(client1)).thenReturn(saved);
        when(modelMapper.map(saved, ClientDTO.class)).thenReturn(updateInput);

        ClientDTO result = clientService.updateClientByEmail(email, updateInput);

        verify(clientRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).encode(newPasswordRaw);
        verify(clientRepository, times(1)).save(client1);
        verify(modelMapper, times(1)).map(saved, ClientDTO.class);

        assertEquals(updateInput, result);
    }

    @Test
    void updateClientByEmail_shouldThrowPasswordTooShortException() {
        String email = client1.getEmail();
        ClientDTO updateInput = new ClientDTO(email, "short", "Name", BigDecimal.ZERO, false);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client1));

        assertThrows(PasswordTooShortException.class, () ->
                clientService.updateClientByEmail(email, updateInput)
        );

        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClientByEmail_shouldThrowPasswordWhitespaceException() {
        String email = client1.getEmail();
        ClientDTO updateInput = new ClientDTO(email, "pass with space", "Name", BigDecimal.ZERO, false);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client1));

        assertThrows(PasswordWhitespaceException.class, () ->
                clientService.updateClientByEmail(email, updateInput)
        );

        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClientByEmail_shouldThrowNotFoundException() {
        when(clientRepository.findByEmail("test")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> clientService.updateClientByEmail("test", dto1));

        assertEquals("Client with email test not found", exception.getMessage());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClientByEmail_shouldThrowAlreadyExistsException() {
        when(clientRepository.findByEmail("c1@test.com")).thenReturn(Optional.of(client1));
        when(userRepository.existsByEmail("c2@test.com")).thenReturn(true);

        AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> clientService.updateClientByEmail("c1@test.com", dto2)
        );

        assertEquals("User with email c2@test.com already exists", exception.getMessage());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClientByEmail_shouldDelete() {
        when(clientRepository.findByEmail("c1@test.com")).thenReturn(Optional.of(client1));

        clientService.deleteClientByEmail("c1@test.com");

        verify(clientRepository, times(1)).findByEmail("c1@test.com");
        verify(clientRepository, times(1)).delete(client1);
    }

    @Test
    void deleteClientByEmail_shouldThrowNotFoundException() {
        when(clientRepository.findByEmail("test")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.deleteClientByEmail("test"));

        verify(clientRepository, never()).delete(any());
    }

    @Test
    void addClient_shouldAddClientAndReturnDTO() {
        String rawPassword = "secure123";
        String encodedPassword = "encoded_secure123";
        String email = "new@test.com";

        ClientDTO inputDTO = new ClientDTO(email, rawPassword, "New User", new BigDecimal("999.00"), false);
        Client mappedEntity = new Client();
        mappedEntity.setEmail(email);
        mappedEntity.setName("New User");

        Client savedEntity = new Client(3L, email, encodedPassword, "New User", BigDecimal.ZERO, false);
        ClientDTO expectedResultDTO = new ClientDTO(email, rawPassword, "New User", BigDecimal.ZERO, false);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(modelMapper.map(inputDTO, Client.class)).thenReturn(mappedEntity);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(clientRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(modelMapper.map(savedEntity, ClientDTO.class)).thenReturn(expectedResultDTO);

        ClientDTO result = clientService.addClient(inputDTO);

        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(clientRepository, times(1)).save(mappedEntity);

        assertEquals(BigDecimal.ZERO, mappedEntity.getBalance());
        assertEquals(encodedPassword, mappedEntity.getPassword());
        assertEquals(expectedResultDTO, result);
    }

    @Test
    void addClient_shouldThrowAlreadyExistException() {
        ClientDTO inputDTO = new ClientDTO("new@test.com", "pass", "User", BigDecimal.ZERO, false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(true);

        AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> clientService.addClient(inputDTO));

        assertEquals("User with email " + inputDTO.getEmail() + " already exists", exception.getMessage());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void getClients_withSearchTerm_shouldReturnFilteredPage() {
        String searchTerm = "query";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = new PageImpl<>(mockClients);

        when(clientRepository.searchClients(searchTerm, pageable)).thenReturn(clientPage);
        when(modelMapper.map(client1, ClientDTO.class)).thenReturn(dto1);
        when(modelMapper.map(client2, ClientDTO.class)).thenReturn(dto2);

        Page<ClientDTO> result = clientService.getClients(searchTerm, pageable);

        assertEquals(2, result.getTotalElements());
        verify(clientRepository).searchClients(searchTerm, pageable);
        verify(clientRepository, never()).findAll(pageable);
    }

    @Test
    void getClients_withoutSearchTerm_shouldReturnAllPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = new PageImpl<>(mockClients);

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);
        when(modelMapper.map(client1, ClientDTO.class)).thenReturn(dto1);
        when(modelMapper.map(client2, ClientDTO.class)).thenReturn(dto2);

        Page<ClientDTO> result = clientService.getClients(null, pageable);

        assertEquals(2, result.getTotalElements());
        verify(clientRepository).findAll(pageable);
        verify(clientRepository, never()).searchClients(anyString(), any(Pageable.class));
    }

    @Test
    void getClients_withEmptySearchTerm_shouldReturnAllPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = new PageImpl<>(Collections.emptyList());

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);

        Page<ClientDTO> result = clientService.getClients("", pageable);

        assertEquals(0, result.getTotalElements());
        verify(clientRepository).findAll(pageable);
        verify(clientRepository, never()).searchClients(anyString(), any(Pageable.class));
    }

    @Test
    void updateClientStatus_shouldUpdateBlockedStatus() {
        String email = "c1@test.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client1));

        clientService.updateClientStatus(email, true);

        assertTrue(client1.isBlocked());
        verify(clientRepository).save(client1);
    }

    @Test
    void updateClientStatus_shouldThrowNotFoundException() {
        String email = "unknown@test.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.updateClientStatus(email, true));
        verify(clientRepository, never()).save(any());
    }
}
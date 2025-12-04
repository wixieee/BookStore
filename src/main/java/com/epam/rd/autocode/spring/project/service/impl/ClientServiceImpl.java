package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.PasswordTooShortException;
import com.epam.rd.autocode.spring.project.exception.PasswordWhitespaceException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(c -> modelMapper.map(c, ClientDTO.class))
                .toList();
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        Client client = findClientByEmail(email);
        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    @Transactional
    public ClientDTO updateClientByEmail(String email, ClientDTO clientDto) {
        Client current = findClientByEmail(email);

        if (!current.getEmail().equals(clientDto.getEmail())) {
            userExists(clientDto.getEmail());
            current.setEmail(clientDto.getEmail());
        }

        current.setName(clientDto.getName());

        if (clientDto.getPassword() != null && !clientDto.getPassword().trim().isEmpty()) {
            validatePassword(clientDto.getPassword());
            current.setPassword(passwordEncoder.encode(clientDto.getPassword()));
        }

        Client updated = clientRepository.save(current);
        return modelMapper.map(updated, ClientDTO.class);
    }

    @Override
    @Transactional
    public void deleteClientByEmail(String email) {
        Client client = findClientByEmail(email);
        clientRepository.delete(client);
    }

    @Override
    @Transactional
    public ClientDTO addClient(ClientDTO client) {
        String email = client.getEmail();

        userExists(email);

        Client newClient = modelMapper.map(client, Client.class);
        newClient.setBalance(BigDecimal.ZERO);
        newClient.setPassword(passwordEncoder.encode(client.getPassword()));

        Client saved = clientRepository.save(newClient);
        return modelMapper.map(saved, ClientDTO.class);
    }

    @Override
    public Page<ClientDTO> getClients(String search, Pageable pageable) {
        Page<Client> page;
        if (search != null && !search.isBlank()) {
            page = clientRepository.searchClients(search, pageable);
        } else {
            page = clientRepository.findAll(pageable);
        }
        return page.map(c -> modelMapper.map(c, ClientDTO.class));
    }

    @Override
    @Transactional
    public void updateClientStatus(String email, boolean isBlocked) {
        Client client = findClientByEmail(email);
        client.setBlocked(isBlocked);
        clientRepository.save(client);
    }

    private Client findClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client with email " + email + " not found"));
    }

    private void userExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AlreadyExistException("User with email " + email + " already exists");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new PasswordTooShortException("Password must be at least 8 characters long");
        }
        if (password.contains(" ")) {
            throw new PasswordWhitespaceException("Password cannot contain whitespace");
        }
    }
}

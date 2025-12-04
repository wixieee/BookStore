package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.PasswordTooShortException;
import com.epam.rd.autocode.spring.project.exception.PasswordWhitespaceException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employee = new Employee(
                1L,
                "emp@test.com",
                "encoded_pass",
                "John Doe",
                "+380501234567",
                LocalDate.of(1990, 1, 1),
                false
        );

        employeeDTO = new EmployeeDTO(
                "emp@test.com",
                "password123",
                "John Doe",
                "+380501234567",
                LocalDate.of(1990, 1, 1)
        );
    }

    @Test
    void getEmployeeByEmail_shouldReturnDTO() {
        when(employeeRepository.findByEmail("emp@test.com")).thenReturn(Optional.of(employee));
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(employeeDTO);

        EmployeeDTO result = employeeService.getEmployeeByEmail("emp@test.com");

        verify(employeeRepository).findByEmail("emp@test.com");
        verify(modelMapper).map(employee, EmployeeDTO.class);
        assertEquals(employeeDTO, result);
    }

    @Test
    void getEmployeeByEmail_shouldThrowNotFoundException() {
        when(employeeRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.getEmployeeByEmail("unknown@test.com"));

        verify(employeeRepository).findByEmail("unknown@test.com");
    }

    @Test
    void updateEmployeeByEmail_shouldUpdateWithPassword() {
        String email = "emp@test.com";
        String newPassRaw = "new_secure_pass";
        String newPassEncoded = "encoded_new_pass";

        EmployeeDTO updateInput = new EmployeeDTO(
                email,
                newPassRaw,
                "Jane Doe",
                "+380991234567",
                LocalDate.of(1995, 5, 5)
        );

        Employee updatedEntity = new Employee(
                1L, email, newPassEncoded, "Jane Doe", "+380991234567", LocalDate.of(1995, 5, 5), false
        );

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(passwordEncoder.encode(newPassRaw)).thenReturn(newPassEncoded);
        when(employeeRepository.save(employee)).thenReturn(updatedEntity);
        when(modelMapper.map(updatedEntity, EmployeeDTO.class)).thenReturn(updateInput);

        EmployeeDTO result = employeeService.updateEmployeeByEmail(email, updateInput);

        verify(passwordEncoder).encode(newPassRaw);
        verify(employeeRepository).save(employee);
        assertEquals(updateInput, result);
    }

    @Test
    void updateEmployeeByEmail_shouldUpdateWithoutPassword() {
        String email = "emp@test.com";
        EmployeeDTO updateInput = new EmployeeDTO(
                email,
                null,
                "Jane Doe",
                "+380991234567",
                LocalDate.of(1995, 5, 5)
        );

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(updateInput);

        employeeService.updateEmployeeByEmail(email, updateInput);

        verify(passwordEncoder, never()).encode(any());
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployeeByEmail_shouldThrowAlreadyExistException() {
        String currentEmail = "emp@test.com";
        String newEmail = "taken@test.com";

        EmployeeDTO updateInput = new EmployeeDTO(newEmail, "pass", "Name", "123", LocalDate.now());

        when(employeeRepository.findByEmail(currentEmail)).thenReturn(Optional.of(employee));
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        assertThrows(AlreadyExistException.class, () ->
                employeeService.updateEmployeeByEmail(currentEmail, updateInput)
        );

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateEmployeeByEmail_shouldThrowPasswordTooShort() {
        EmployeeDTO updateInput = new EmployeeDTO("emp@test.com", "short", "Name", "123", LocalDate.now());

        when(employeeRepository.findByEmail("emp@test.com")).thenReturn(Optional.of(employee));

        assertThrows(PasswordTooShortException.class, () ->
                employeeService.updateEmployeeByEmail("emp@test.com", updateInput)
        );
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateEmployeeByEmail_shouldThrowPasswordWhitespace() {
        EmployeeDTO updateInput = new EmployeeDTO("emp@test.com", "pass word", "Name", "123", LocalDate.now());

        when(employeeRepository.findByEmail("emp@test.com")).thenReturn(Optional.of(employee));

        assertThrows(PasswordWhitespaceException.class, () ->
                employeeService.updateEmployeeByEmail("emp@test.com", updateInput)
        );
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateEmployeeByEmail_shouldThrowNotFound() {
        when(employeeRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                employeeService.updateEmployeeByEmail("unknown", employeeDTO)
        );
    }

    @Test
    void deleteEmployeeByEmail_shouldDelete() {
        when(employeeRepository.findByEmail("emp@test.com")).thenReturn(Optional.of(employee));

        employeeService.deleteEmployeeByEmail("emp@test.com");

        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployeeByEmail_shouldThrowNotFound() {
        when(employeeRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.deleteEmployeeByEmail("unknown"));

        verify(employeeRepository, never()).delete(any());
    }

    @Test
    void addEmployee_shouldSaveEmployee() {
        String rawPass = "password123";
        String encodedPass = "encoded_pass";

        Employee mappedEmployee = new Employee();
        mappedEmployee.setEmail("emp@test.com");

        when(userRepository.existsByEmail("emp@test.com")).thenReturn(false);
        when(modelMapper.map(employeeDTO, Employee.class)).thenReturn(mappedEmployee);
        when(passwordEncoder.encode(rawPass)).thenReturn(encodedPass);
        when(employeeRepository.save(mappedEmployee)).thenReturn(employee);

        employeeService.addEmployee(employeeDTO);

        assertEquals(encodedPass, mappedEmployee.getPassword());
        verify(userRepository).existsByEmail("emp@test.com");
        verify(passwordEncoder).encode(rawPass);
        verify(employeeRepository).save(mappedEmployee);
    }

    @Test
    void addEmployee_shouldThrowAlreadyExist() {
        when(userRepository.existsByEmail(employeeDTO.getEmail())).thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> employeeService.addEmployee(employeeDTO));

        verify(employeeRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
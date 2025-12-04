package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.PasswordTooShortException;
import com.epam.rd.autocode.spring.project.exception.PasswordWhitespaceException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        Employee employee = findEmployeeByEmail(email);
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employeeDto) {
        Employee current = findEmployeeByEmail(email);

        if (!current.getEmail().equals(employeeDto.getEmail())) {
            userExists(employeeDto.getEmail());
            current.setEmail(employeeDto.getEmail());
        }

        current.setName(employeeDto.getName());

        current.setPhone(employeeDto.getPhone());
        current.setBirthDate(employeeDto.getBirthDate());

        if (employeeDto.getPassword() != null && !employeeDto.getPassword().trim().isEmpty()) {
            validatePassword(employeeDto.getPassword());
            current.setPassword(passwordEncoder.encode(employeeDto.getPassword()));
        }

        Employee updated = employeeRepository.save(current);
        return modelMapper.map(updated, EmployeeDTO.class);
    }

    @Override
    @Transactional
    public void deleteEmployeeByEmail(String email) {
        Employee employee = findEmployeeByEmail(email);
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional
    public void addEmployee(EmployeeDTO employeeDto) {
        String email = employeeDto.getEmail();

        userExists(email);

        Employee newEmployee = modelMapper.map(employeeDto, Employee.class);

        newEmployee.setPassword(passwordEncoder.encode(employeeDto.getPassword()));

        Employee saved = employeeRepository.save(newEmployee);
        modelMapper.map(saved, EmployeeDTO.class);
    }

    private Employee findEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee with email " + email + " not found"));
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
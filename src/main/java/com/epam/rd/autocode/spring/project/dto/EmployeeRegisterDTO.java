package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRegisterDTO {
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.register.name.required}")
    private String name;

    @NotBlank(message = "{validation.register.phone.required}")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "{validation.register.phone.invalid}")
    private String phone;

    @NotNull(message = "{validation.register.date.required}")
    @Past(message = "{validation.register.date.invalid}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Size(min = 8, message = "{validation.password.size}")
    @Pattern(regexp = "^[^\\s]*$", message = "{validation.password.whitespace}")
    private String password;

    private String passwordConfirm;
}
package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisterDTO {
    @NotBlank(message = "{validation.register.name.required}")
    private String name;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @Size(min = 8, message = "{validation.password.size}")
    @Pattern(regexp = "^[^\\s]*$", message = "{validation.password.whitespace}")
    private String password;

    private String passwordConfirm;
}

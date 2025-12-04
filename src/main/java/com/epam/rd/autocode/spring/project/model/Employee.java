package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee extends User {
    private String phone;
    private LocalDate birthDate;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    public Employee(Long id, String email, String password, String name, String phone, LocalDate birthDate, boolean isBlocked) {
        super(id, email, password, name, isBlocked);
        this.phone = phone;
        this.birthDate = birthDate;
    }
}

package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {
    private Long id;

    @NotBlank(message = "{validation.book.name.required}")
    private String name;

    @NotBlank(message = "{validation.book.genre.required}")
    private String genre;

    @NotNull(message = "{validation.book.ageGroup.required}")
    private AgeGroup ageGroup;

    @NotNull(message = "{validation.book.price.required}")
    @Positive(message = "{validation.book.price.positive}")
    private BigDecimal price;

    @NotNull(message = "{validation.book.date.required}")
    @PastOrPresent(message = "{validation.book.date.past}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    @NotBlank(message = "{validation.book.author.required}")
    private String author;

    @NotNull(message = "{validation.book.pages.required}")
    @Min(value = 1, message = "{validation.book.pages.min}")
    private Integer pages;

    @NotBlank(message = "{validation.book.characteristics.required}")
    private String characteristics;

    @NotBlank(message = "{validation.book.description.required}")
    @Size(max = 2000, message = "{validation.book.description.size}")
    private String description;

    @NotNull(message = "{validation.book.language.required}")
    private Language language;

    private String imagePath;
}
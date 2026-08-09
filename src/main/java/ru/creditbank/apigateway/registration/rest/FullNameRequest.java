package ru.creditbank.apigateway.registration.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FullNameRequest(

        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[А-Яа-яЁёA-Za-z-]+$")
        String firstname,

        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[А-Яа-яЁёA-Za-z-]+$")
        String lastname,

        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[А-Яа-яЁёA-Za-z-]+$")
        String middlename
) {
}
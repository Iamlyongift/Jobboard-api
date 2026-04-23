package com.jobboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class JobRequest {
    @NotBlank
    private String title;
    private String email;
    @NotBlank
    private String description;
    @NotBlank
    private String location;
    @NotNull
    private Double salary;
}



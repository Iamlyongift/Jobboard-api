package com.jobboard.dto;

import com.jobboard.entity.User.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RegisterRequest {

    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotNull
    private Role role;

}

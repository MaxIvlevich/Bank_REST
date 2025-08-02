package com.example.bankcards.dto.request;

import com.example.bankcards.entity.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateUserRolesRequest (
        @NotNull
        @NotEmpty(message = "Roles set cannot be empty.")
        Set<Role> roles
){
}

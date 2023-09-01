package dev.marvin.crud.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class NewUser {
    @NotBlank
    private String name;
    @Email
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String username;
    private Set<String> roles = new HashSet<>();


}

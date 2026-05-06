package com.carmeet.ms_auth_user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {
    @NotBlank(message = "El RUT es obligatorio") 
    @Size(min = 9, max = 12) 
    String rut;

    @NotBlank(message = "El nombre es obligatorio") 
    String fullName;

    @NotBlank(message = "El email es obligatorio") 
    @Email(message = "Debe ser un email válido") 
    String email;

    @NotBlank(message = "El rol es obligatorio") 
    String role;
}

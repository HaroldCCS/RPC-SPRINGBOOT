package com.rpc.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormRequestDto {
    private String nombre;
    private String apellido;
    private Integer edad;
    private String email;
}


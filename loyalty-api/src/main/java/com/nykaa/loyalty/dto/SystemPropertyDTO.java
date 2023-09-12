package com.nykaa.loyalty.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class SystemPropertyDTO {

    private Long id;

    @NotBlank
    private String name;

    private String value;
}

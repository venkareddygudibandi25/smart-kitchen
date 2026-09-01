package com.smartkitchen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateChefRequest {

    @NotBlank(message = "Chef name is required")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String name;
}
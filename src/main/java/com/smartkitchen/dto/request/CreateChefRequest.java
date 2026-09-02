package com.smartkitchen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateChefRequest {

	@NotBlank(message = "Chef name is required")
	@Size(min = 2, max = 50, message = "Chef name must be between 2 and 50 characters")
	@Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Chef name must contain only letters and cannot be purely numeric")
	private String name;
}
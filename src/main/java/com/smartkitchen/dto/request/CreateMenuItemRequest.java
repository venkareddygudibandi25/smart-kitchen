package com.smartkitchen.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateMenuItemRequest {

	@NotBlank(message = "Item name is required")
	private String name;

	@Min(value = 1, message = "Cook time must be at least 1 second")
	@Max(value = 60, message = "Cook time cannot exceed 60 seconds")
	private Integer cookTime;

	@Min(value = 0, message = "Failure rate cannot be negative")
	@Max(value = 100, message = "Failure rate cannot exceed 100")
	private Integer failureRate;
}
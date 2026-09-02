package com.smartkitchen.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMenuItemRequest {

	@NotBlank(message = "Item name is required")
	@Size(min = 2, max = 100, message = "Item name must be between 2 and 100 characters")
	@Pattern(regexp = "^(?!\\d+$)[a-zA-Z0-9\\s'-]+$", message = "Item name cannot be purely numeric")
	private String name;

	@NotNull(message = "Cook time is required")
	@Min(value = 1, message = "Cook time must be at least 1 second")
	@Max(value = 60, message = "Cook time cannot exceed 60 seconds")
	private Integer cookTime;

	@NotNull(message = "Failure rate is required")
	@Min(value = 0, message = "Failure rate cannot be negative")
	@Max(value = 100, message = "Failure rate cannot exceed 100")
	private Integer failureRate;
}
package com.smartkitchen.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

	@NotBlank(message = "Customer name is required")
	@Size(min = 2, max = 50, message = "Customer name must be between 2 and 50 characters")
	@Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Customer name must contain only letters and cannot be numeric")
	private String customerName;

	private List<Long> itemIds;

	private List<OrderItemRequest> items;
}
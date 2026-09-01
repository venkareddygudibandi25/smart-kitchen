package com.smartkitchen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {

	@NotBlank(message = "Customer name is required")
	private String customerName;

	@NotEmpty(message = "Select at least one menu item")
	private List<Long> itemIds;
}
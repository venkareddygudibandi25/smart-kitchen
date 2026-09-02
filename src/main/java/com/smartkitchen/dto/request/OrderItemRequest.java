package com.smartkitchen.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

	@NotNull(message = "Menu item ID is required")
	private Long menuItemId;

	private Integer dependsOnIndex;
}

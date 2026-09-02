package com.smartkitchen.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {

	private Long itemId;
	private String itemName;
	private String chefName;
	private String status;
	private Integer attempts;
	private Long dependsOnItemId;
}

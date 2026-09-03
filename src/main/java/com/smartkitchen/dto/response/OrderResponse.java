package com.smartkitchen.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

	private Long orderId;
	private String customerName;
	private String status;
	private Integer estimatedCompletionSeconds;
	private List<OrderItemResponse> items;
}
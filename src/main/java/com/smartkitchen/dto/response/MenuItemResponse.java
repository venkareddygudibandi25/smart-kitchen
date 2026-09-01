package com.smartkitchen.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuItemResponse {

	private Long id;
	private String name;
	private Integer cookTime;
	private Integer failureRate;
}
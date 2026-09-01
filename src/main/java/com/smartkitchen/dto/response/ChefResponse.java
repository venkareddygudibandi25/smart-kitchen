package com.smartkitchen.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChefResponse {

	private Long id;
	private String name;
	private Boolean available;
}
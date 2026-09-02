package com.smartkitchen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse<T> {

	private Integer statusCode;

	private Boolean isError;

	private T result;

	public static <T> APIResponse<T> success(Integer statusCode, T result) {
		return APIResponse.<T>builder()
				.statusCode(statusCode)
				.isError(false)
				.result(result)
				.build();
	}

	public static <T> APIResponse<T> error(Integer statusCode, T result) {
		return APIResponse.<T>builder()
				.statusCode(statusCode)
				.isError(true)
				.result(result)
				.build();
	}
}

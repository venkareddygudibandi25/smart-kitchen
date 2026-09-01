package com.smartkitchen.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.smartkitchen.dto.response.ErrorResponse;

public class GlobalExceptionHandler {
	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {

		ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.NOT_FOUND.value()).error("Order Not Found").message(ex.getMessage()).build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

		List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage)
				.toList();

		ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(400)
				.error("Validation Failed").messages(errors).build();

		return ResponseEntity.badRequest().body(response);
	}

}

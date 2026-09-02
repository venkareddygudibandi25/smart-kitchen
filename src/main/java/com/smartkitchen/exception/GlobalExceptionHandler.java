package com.smartkitchen.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.smartkitchen.dto.response.APIResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<APIResponse<String>> handleOrderNotFound(OrderNotFoundException ex) {

		APIResponse<String> response = APIResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(MenuItemNotFoundException.class)
	public ResponseEntity<APIResponse<String>> handleMenuItemNotFound(MenuItemNotFoundException ex) {

		APIResponse<String> response = APIResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(InvalidOrderException.class)
	public ResponseEntity<APIResponse<String>> handleInvalidOrder(InvalidOrderException ex) {

		APIResponse<String> response = APIResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<APIResponse<List<String>>> handleValidation(MethodArgumentNotValidException ex) {

		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.toList();

		APIResponse<List<String>> response = APIResponse.error(HttpStatus.BAD_REQUEST.value(), errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<APIResponse<String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

		String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
		String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
				ex.getValue(), ex.getName(), requiredType);

		APIResponse<String> response = APIResponse.error(HttpStatus.BAD_REQUEST.value(), message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<APIResponse<String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

		APIResponse<String> response = APIResponse.error(HttpStatus.BAD_REQUEST.value(),
				"Invalid request body format or data type mismatch (e.g. invalid value for numeric ID field)");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<APIResponse<String>> handleGeneralException(Exception ex) {

		APIResponse<String> response = APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected error occurred: " + ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

}

package com.smartkitchen.controller.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartkitchen.dto.request.PlaceOrderRequest;
import com.smartkitchen.dto.response.APIResponse;
import com.smartkitchen.dto.response.OrderResponse;
import com.smartkitchen.service.order.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<APIResponse<OrderResponse>> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {

		OrderResponse data = orderService.placeOrder(request);

		APIResponse<OrderResponse> response = new APIResponse<>();
		response.setStatusCode(HttpStatus.CREATED.value());
		response.setIsError(false);
		response.setResult(data);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<APIResponse<OrderResponse>> getOrder(@PathVariable Long id) {

		OrderResponse data = orderService.getOrder(id);

		APIResponse<OrderResponse> response = new APIResponse<>();
		response.setStatusCode(HttpStatus.OK.value());
		response.setIsError(false);
		response.setResult(data);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<APIResponse<String>> cancel(@PathVariable Long id) {

		orderService.cancelOrder(id);

		APIResponse<String> response = new APIResponse<>();
		response.setStatusCode(HttpStatus.OK.value());
		response.setIsError(false);
		response.setResult("Order cancelled successfully");

		return ResponseEntity.ok(response);
	}

}
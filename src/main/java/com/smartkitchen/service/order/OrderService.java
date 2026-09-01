package com.smartkitchen.service.order;

import com.smartkitchen.dto.request.PlaceOrderRequest;
import com.smartkitchen.dto.response.OrderResponse;

public interface OrderService {

	OrderResponse placeOrder(PlaceOrderRequest request);

	OrderResponse getOrder(Long orderId);

	void cancelOrder(Long orderId);
}

package com.smartkitchen.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private Long orderId;
    private String customerName;
    private String status;
    private List<OrderItemResponse> items;
}
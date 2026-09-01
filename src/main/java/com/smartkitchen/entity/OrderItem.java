package com.smartkitchen.entity;

import com.smartkitchen.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @ManyToOne
    @JoinColumn(name = "chef_id")
    private Chef chef;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    @Builder.Default
    private Integer attempts = 0;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}
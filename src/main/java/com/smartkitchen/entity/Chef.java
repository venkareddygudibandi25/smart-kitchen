package com.smartkitchen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chefs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private Boolean available = true;
}
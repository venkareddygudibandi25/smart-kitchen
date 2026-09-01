package com.smartkitchen.repository;

import com.smartkitchen.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository
        extends JpaRepository<MenuItem, Long> {
}
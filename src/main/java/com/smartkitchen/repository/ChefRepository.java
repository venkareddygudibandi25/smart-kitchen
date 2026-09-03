package com.smartkitchen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartkitchen.entity.Chef;

public interface ChefRepository extends JpaRepository<Chef, Long> {

	List<Chef> findByAvailableTrue();

	long countByAvailable(Boolean available);
}
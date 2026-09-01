package com.smartkitchen.repository;

import com.smartkitchen.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChefRepository extends JpaRepository<Chef, Long> {

    List<Chef> findByAvailableTrue();

}
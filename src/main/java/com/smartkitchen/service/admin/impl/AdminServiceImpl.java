package com.smartkitchen.service.admin.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartkitchen.dto.request.CreateChefRequest;
import com.smartkitchen.dto.request.CreateMenuItemRequest;
import com.smartkitchen.dto.response.ChefResponse;
import com.smartkitchen.dto.response.MenuItemResponse;
import com.smartkitchen.entity.Chef;
import com.smartkitchen.entity.MenuItem;
import com.smartkitchen.repository.ChefRepository;
import com.smartkitchen.repository.MenuItemRepository;
import com.smartkitchen.service.admin.AdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
	private final ChefRepository chefRepository;
	private final MenuItemRepository menuRepository;

	@Override
	public ChefResponse createChef(CreateChefRequest request) {

		Chef chef = Chef.builder().name(request.getName()).available(true).build();

		Chef saved = chefRepository.save(chef);

		return ChefResponse.builder().id(saved.getId()).name(saved.getName()).available(saved.getAvailable()).build();
	}

	@Override
	public List<ChefResponse> getAllChefs() {

		return chefRepository.findAll().stream().map(chef -> ChefResponse.builder().id(chef.getId())
				.name(chef.getName()).available(chef.getAvailable()).build()).toList();
	}

	@Override
	public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {

		MenuItem item = MenuItem.builder().name(request.getName()).cookTime(request.getCookTime())
				.failureRate(request.getFailureRate()).build();

		MenuItem saved = menuRepository.save(item);

		return MenuItemResponse.builder().id(saved.getId()).name(saved.getName()).cookTime(saved.getCookTime())
				.failureRate(saved.getFailureRate()).build();
	}

	@Override
	public List<MenuItemResponse> getAllMenuItems() {

		return menuRepository.findAll().stream().map(item -> MenuItemResponse.builder().id(item.getId())
				.name(item.getName()).cookTime(item.getCookTime()).failureRate(item.getFailureRate()).build()).toList();
	}

}

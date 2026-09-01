package com.smartkitchen.service.admin;

import java.util.List;

import com.smartkitchen.dto.request.CreateChefRequest;
import com.smartkitchen.dto.request.CreateMenuItemRequest;
import com.smartkitchen.dto.response.ChefResponse;
import com.smartkitchen.dto.response.MenuItemResponse;

public interface AdminService {

	ChefResponse createChef(CreateChefRequest request);

	List<ChefResponse> getAllChefs();

	MenuItemResponse createMenuItem(CreateMenuItemRequest request);

	List<MenuItemResponse> getAllMenuItems();
}
package com.smartkitchen.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smartkitchen.dto.request.CreateChefRequest;
import com.smartkitchen.dto.request.CreateMenuItemRequest;
import com.smartkitchen.dto.response.ChefResponse;
import com.smartkitchen.dto.response.MenuItemResponse;
import com.smartkitchen.service.admin.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;

	@PostMapping("/chefs")
	@ResponseStatus(HttpStatus.CREATED)
	public ChefResponse createChef(@Valid @RequestBody CreateChefRequest request) {

		return adminService.createChef(request);
	}

	@GetMapping("/chefs")
	public List<ChefResponse> getChefs() {

		return adminService.getAllChefs();
	}

	@PostMapping("/menu")
	@ResponseStatus(HttpStatus.CREATED)
	public MenuItemResponse createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {

		return adminService.createMenuItem(request);
	}

	@GetMapping("/menu")
	public List<MenuItemResponse> getMenu() {

		return adminService.getAllMenuItems();
	}

}

package com.smartkitchen.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartkitchen.dto.request.CreateChefRequest;
import com.smartkitchen.dto.request.CreateMenuItemRequest;
import com.smartkitchen.dto.response.APIResponse;
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
	public ResponseEntity<APIResponse<ChefResponse>> createChef(@Valid @RequestBody CreateChefRequest request) {

		ChefResponse data = adminService.createChef(request);

		APIResponse<ChefResponse> response = new APIResponse<>();
		response.setStatusCode(HttpStatus.CREATED.value());
		response.setIsError(false);
		response.setResult(data);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/chefs")
	public ResponseEntity<APIResponse<List<ChefResponse>>> getChefs() {

		List<ChefResponse> data = adminService.getAllChefs();

		APIResponse<List<ChefResponse>> response = new APIResponse<>();
		response.setStatusCode(HttpStatus.OK.value());
		response.setIsError(false);
		response.setResult(data);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/menu")
	public ResponseEntity<APIResponse<MenuItemResponse>> createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {

		MenuItemResponse data = adminService.createMenuItem(request);

		APIResponse<MenuItemResponse> response = new APIResponse<>();
		response.setStatusCode(HttpStatus.CREATED.value());
		response.setIsError(false);
		response.setResult(data);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/menu")
	public ResponseEntity<APIResponse<List<MenuItemResponse>>> getMenu() {

		List<MenuItemResponse> data = adminService.getAllMenuItems();

		APIResponse<List<MenuItemResponse>> response = new APIResponse<>();
		response.setStatusCode(HttpStatus.OK.value());
		response.setIsError(false);
		response.setResult(data);

		return ResponseEntity.ok(response);
	}

}

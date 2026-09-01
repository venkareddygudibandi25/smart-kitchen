package com.smartkitchen.exception;

public class MenuItemNotFoundException extends RuntimeException {

	public MenuItemNotFoundException(Long id) {
		super("Menu item not found : " + id);
	}
}
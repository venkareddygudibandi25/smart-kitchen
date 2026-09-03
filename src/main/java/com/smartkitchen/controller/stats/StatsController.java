package com.smartkitchen.controller.stats;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartkitchen.dto.response.APIResponse;
import com.smartkitchen.dto.response.StatsResponse;
import com.smartkitchen.service.stats.StatsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

	private final StatsService statsService;

	@GetMapping
	public ResponseEntity<APIResponse<StatsResponse>> getStats() {

		StatsResponse data = statsService.getStats();

		APIResponse<StatsResponse> response = APIResponse.success(200, data);

		return ResponseEntity.ok(response);
	}
}

package com.smartkitchen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {

	private long runningTasks;
	private long waitingTasks;
	private long availableChefs;
	private long busyChefs;
}

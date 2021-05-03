package domain;

import java.time.LocalDateTime;

import kf5012darthmaulapplication.User;

public class Completion {
	private User caretaker;
	private LocalDateTime startTime;
	private LocalDateTime completionTime;
	private TaskCompletionQuality workQuality;
	private String notes;
	
	public Completion(
			User caretaker,
			LocalDateTime startTime,
			LocalDateTime completionTime,
			TaskCompletionQuality workQuality,
			String notes
	) {
		this.caretaker = caretaker;
		this.startTime = startTime;
		this.completionTime = completionTime;
		this.workQuality = workQuality;
		this.notes = notes;
	}
	
	public Completion() {
		this(
			null, // Not ideal: not a nullable field (will need to be validated)
			LocalDateTime.now(),
			LocalDateTime.now(),
			TaskCompletionQuality.GOOD,
			""
		);
	}

	public User getCaretaker() {
		return this.caretaker;
	}
	public LocalDateTime getStartTime() {
		return this.startTime;
	}
	public LocalDateTime getCompletionTime() {
		return this.completionTime;
	}
	public TaskCompletionQuality getWorkQuality() {
		return this.workQuality;
	}
	public String getNotes() {
		return this.notes;
	}
}

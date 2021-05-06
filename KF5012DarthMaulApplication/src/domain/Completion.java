package domain;

import java.time.LocalDateTime;

import kf5012darthmaulapplication.User;

public class Completion {
	private Integer id; // Nullable
	
	private User caretaker;
	private LocalDateTime startTime;
	private LocalDateTime completionTime;
	private TaskCompletionQuality workQuality;
	private String notes;
	
	public Completion(
			Integer id,
			
			User caretaker,
			LocalDateTime startTime,
			LocalDateTime completionTime,
			TaskCompletionQuality workQuality,
			String notes
	) {
		this.id = id;
		
		this.caretaker = caretaker;
		this.startTime = startTime;
		this.completionTime = completionTime;
		this.workQuality = workQuality;
		this.notes = notes;
	}
	
	public Completion() {
		this(
			null, // No ID assigned yet (ie. not in DB)
			null, // Not ideal: not a nullable field (will need to be validated)
			LocalDateTime.now(),
			LocalDateTime.now(),
			TaskCompletionQuality.GOOD,
			""
		);
	}

	public Integer getID() {
		return this.id;
	}
	public void setID(Integer id) {
		this.id = id;
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

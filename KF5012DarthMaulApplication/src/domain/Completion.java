package domain;

import java.time.LocalDateTime;

import kf5012darthmaulapplication.User;

/**
 * The information about completing an execution of a task or verification.
 * 
 * @author William Taylor
 */
public class Completion {
	private Integer id; // Nullable
	
	private User staff;
	private LocalDateTime startTime;
	private LocalDateTime completionTime;
	private TaskCompletionQuality workQuality;
	private String notes;
	
	/**
	 * Create a new Completion, or populate a Completion object from data
	 * storage.
	 * 
	 * @param id The ID of the completion in the database. Null if not in the
	 * DB.
	 * @param staff The caretaker or manager that completed the task or
	 * verification execution.
	 * @param startTime The time the task or verification execution was started.
	 * @param completionTime The time the task or verification execution was
	 * completed.
	 * @param workQuality The quality the task was completed to. Self-reported
	 * at the completion of a task, and externally verified at the completion of
	 * a verification.
	 * @param notes Any notes to make after a task or verification was
	 * completed, such as comments on what happened, any needed follow-up tasks
	 * that must be made, etc.
	 */
	public Completion(
			Integer id,
			
			User staff,
			LocalDateTime startTime,
			LocalDateTime completionTime,
			TaskCompletionQuality workQuality,
			String notes
	) {
		this.id = id;
		
		this.staff = staff;
		this.startTime = startTime;
		this.completionTime = completionTime;
		this.workQuality = workQuality;
		this.notes = notes;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param completion The completion to copy.
	 */
	public Completion(Completion c) {
		this.id = c.id;
		
		this.staff = c.staff;
		this.startTime = c.startTime;
		this.completionTime = c.completionTime;
		this.workQuality = c.workQuality;
		this.notes = c.notes;
	}

	/**
	 * Create a new completion with sensible default values. These should be
	 * filled out by the user who completed the associated task.
	 * 
	 * Note that the staff member who completed the task or verification is not
	 * set by default, which is a temporarily invalid state. It must be set
	 * before the completion is used (linked to the task or verification
	 * execution) or stored.
	 */
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

	// ID management
	public Integer getID() {
		return this.id;
	}
	public void setID(Integer id) {
		this.id = id;
	}

	// Getters
	public User getStaff() {
		return this.staff;
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

	// Setters
	public void setStaff(User staff) {
		this.staff = staff;
	}
	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}
	public void setCompletionTime(LocalDateTime completionTime) {
		this.completionTime = completionTime;
	}
	public void setWorkQuality(TaskCompletionQuality workQuality) {
		this.workQuality = workQuality;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
}

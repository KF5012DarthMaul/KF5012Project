package temporal;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A start time and possibly an end time that represents a period of time.
 * 
 * The interpretation of a period with no end time (where end is null), is up to
 * the client. Common interpretations include: no duration (end time == start
 * time), or as infinite duration (start time onwards). Making an explicit zero
 * duration (with no room for interpretation) is possible by setting the end
 * time equal to the start time. This is not possible for infinite duration.
 * 
 * @author William Taylor
 */
public class Period {
	private final LocalDateTime start;
	private final LocalDateTime end; // Nullable
	
	public Period(LocalDateTime start, LocalDateTime end) {
		this.start = start;
		if (this.start.isAfter(end)) {
			this.end = start;
		} else {
			this.end = end;
		}
	}
	public Period(LocalDateTime start, Duration length) {
		this.start = start;
		if (length == null) {
			this.end = null;
		} else if (length.isNegative()) {
			this.end = start;
		} else {
			this.end = start.plus(length);
		}
	}
	public Period(LocalDateTime start) {
		this.start = start;
		this.end = null;
	}
	
	public LocalDateTime start() {
		return this.start;
	}
	public LocalDateTime end() {
		return this.end;
	}
	public Duration duration() {
		if (this.end == null) {
			return null;
		}
		return Duration.between(this.start, this.end);
	}
}

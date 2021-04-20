package temporal;

import java.time.Duration;
import java.time.LocalDateTime;

public class Period {
	private final LocalDateTime start;
	private final LocalDateTime end; // Nullable
	
	public Period(LocalDateTime start, LocalDateTime end) {
		this.start = start;
		this.end = end;
	}
	public Period(LocalDateTime start, Duration length) {
		this.start = start;
		if (length.isNegative()) {
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
		return Duration.between(this.start, this.end);
	}
}

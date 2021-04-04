package temporal;

import java.time.Duration;

public class IntervaledPeriodSet {
	private final Period referencePeriod;
	private final Duration interval; // Nullable
	
	public IntervaledPeriodSet(Period referencePeriod, Duration interval) {
		this.referencePeriod = referencePeriod;
		this.interval = interval;
	}
	public IntervaledPeriodSet(Period referencePeriod) {
		this(referencePeriod, null);
	}
	
	public Period referencePeriod() {
		return this.referencePeriod;
	}
	public Duration interval() {
		return this.interval;
	}
}

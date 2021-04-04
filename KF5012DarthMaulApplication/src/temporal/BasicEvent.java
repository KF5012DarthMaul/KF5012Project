package temporal;

public final class BasicEvent implements Event {
	private Period period;
	
	public BasicEvent(Period period) {
		this.period = period;
	}
	
	@Override
	public Period getPeriod() {
		return this.period;
	}
};

package temporal;

/**
 * A basic implementation of the Event interface.
 * 
 * Useful to implement comparators, etc. that require an Event.
 * 
 * @author William Taylor
 */
public class BasicEvent implements Event {
	private Period period;
	
	public BasicEvent(Period period) {
		this.period = period;
	}
	
	@Override
	public Period getPeriod() {
		return this.period;
	}
};

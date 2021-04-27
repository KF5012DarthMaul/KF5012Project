package temporal;

/**
 * The description of a type of event. It describes how long the event usually
 * is, how often the event takes place, and over what (possibly recurring)
 * periods the event takes place.
 * 
 * @author William Taylor
 */
public interface EventDescriptor {
	public ConstrainedIntervaledPeriodSet getCIPC();
}

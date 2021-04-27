package temporal;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * An event (whether actual, expected, theoretical, etc.) executed over a period
 * of time.
 * 
 * @author William Taylor
 */
public interface Event extends Comparable<Event> {
	public Period getPeriod();

	/* Comparators
	 * -------------------------------------------------- */
	
	/**
	 * Defines the ordering of Events by start time only.
	 * 
	 * This comparison is commutative - if A == B and B == C, then A == C.
	 * 
	 * @param o The Event to compare to.
	 * @return
	 */
	@Override
	public default int compareTo(Event o) {
    	Period p1 = this.getPeriod();
    	Period p2 = o.getPeriod();
    	return p1.start().compareTo(p2.start());
	}
	
	/**
	 * A comparator that uses the default ordering (by start time).
	 */
	public static final Comparator<Event> byStartTime = (a,b) -> a.compareTo(b);

	/**
	 * A comparator that uses the end time for ordering.
	 */
	public static final Comparator<Event> byEndTime = new Comparator<Event>() {
		@Override
		public int compare(Event o1, Event o2) {
	    	Period p1 = o1.getPeriod();
	    	Period p2 = o2.getPeriod();
	    	return p1.end().compareTo(p2.end());
		}
	};
	
	/**
	 * A comparator that factors in the period's duration. If the periods
	 * overlap at any point, the comparator returns 0.
	 * 
	 * Null duration is interpreted as zero duration.
	 */
	public static final Comparator<Event> byPeriodDefaultZero =
			new Comparator<Event>()
	{
		@Override
		public int compare(Event o1, Event o2) {
	    	Period p1 = o1.getPeriod();
	    	Period p2 = o2.getPeriod();
	    	
	    	LocalDateTime p1s = p1.start();
	    	LocalDateTime p1e = p1.end();
	    	if (p1e == null) p1e = p1s; // Zero duration

	    	LocalDateTime p2s = p2.start();
	    	LocalDateTime p2e = p2.end();
	    	if (p2e == null) p2e = p2s; // Zero duration
	    	
	    	if ( // o1 is before the start of o2
					p1s.compareTo(p2s) < 0 &&
					p1e.compareTo(p2s) < 0
	    	) {
	    		return -1;
	    		
	    	} else if ( // o1 is after the end of o2
	    			p1s.compareTo(p2e) > 0 &&
	    			p1e.compareTo(p2e) > 0
	    	) {
	    		return 1;
	    		
	    	} else { // o1 and o2 overlap (even if only at one point)
	    		return 0;
	    	}
		}
	};

	/**
	 * A comparator that factors in the period's duration. If the periods
	 * overlap at any point, the comparator returns 0.
	 * 
	 * Null duration is interpreted as infinite duration.
	 */
	public static final Comparator<Event> byPeriodDefaultInf =
			new Comparator<Event>()
	{
		@Override
		public int compare(Event o1, Event o2) {
	    	Period p1 = o1.getPeriod();
	    	Period p2 = o2.getPeriod();
	    	
	    	LocalDateTime p1s = p1.start();
	    	LocalDateTime p1e = p1.end();

	    	LocalDateTime p2s = p2.start();
	    	LocalDateTime p2e = p2.end();
	    	
	    	if ( // o1 is before the start of o2
					p1s.compareTo(p2s) < 0 &&
					(p1e != null && p1e.compareTo(p2s) < 0)
	    	) {
	    		return -1;
	    		
	    	} else if ( // o1 is after the end of o2
	    			(p2e != null || p1s.compareTo(p2e) > 0) &&

	    			// (inf, inf) -> false - must overlap at some point
	    			// (fx, inf) -> false - nothing is > inf
	    			// (inf, fx) -> true - inf is > everything (except inf)
	    			// (fx, fx) -> compare
	    			(p2e != null || p1e == null || p1e.compareTo(p2e) > 0)
	    	) {
	    		return 1;
	    		
	    	} else { // o1 and o2 overlap (even if at only one point)
	    		return 0;
	    	}
		}
	};
}

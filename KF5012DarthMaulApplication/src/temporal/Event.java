package temporal;

public interface Event extends Comparable<Event> {
	public Period getPeriod();
	
    @Override
    public default int compareTo(Event event) {
    	Period p1 = this.getPeriod();
    	Period p2 = event.getPeriod();
    	return p1.start().compareTo(p2.start());
    }
}

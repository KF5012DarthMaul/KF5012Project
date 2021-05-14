package temporal;

import java.awt.Color;

/**
 * A basic implementation of the Event interface.
 * 
 * Useful to implement comparators, etc. that require an Event.
 * 
 * @author William Taylor
 */
public class BasicChartableEvent extends BasicEvent implements ChartableEvent {
	private final String name;
	private final Color color;
	
	public BasicChartableEvent(Period period, String name, Color color) {
		super(period);
		
		this.name = name;
		this.color = color;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Color getColor() {
		return color;
	}
};

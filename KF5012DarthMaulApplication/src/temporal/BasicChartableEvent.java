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
	private static final Color color = Color.GREEN;
	
	private String name;
	
	public BasicChartableEvent(Period period, String name) {
		super(period);
		this.name = name;
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

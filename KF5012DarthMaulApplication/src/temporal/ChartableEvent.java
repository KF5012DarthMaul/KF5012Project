package temporal;

import java.awt.Color;

/**
 * An Event that can be charted ('graphed').
 * 
 * @author William Taylor
 */
public interface ChartableEvent extends Event {
    public String getName();
    public Color getColor();
}

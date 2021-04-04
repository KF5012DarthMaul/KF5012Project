package temporal;

import java.awt.Color;

public interface ChartableEvent extends Event {
    public String getName();
    public Color getColor();
}

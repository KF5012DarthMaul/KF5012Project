package guicomponents.formatters;

import java.time.Duration;

/**
 * Formats Duration objects by showing how many hours and minutes they contain.
 * 
 * @author William Taylor
 */
public class DurationFormatter implements Formatter<Duration> {
	@Override
	public String apply(Duration duration) {
		// Based on https://stackoverflow.com/a/266970
		
		long totalM = duration.toMinutes();
		long h = (totalM / 60);
		long m = (totalM % 60);
		
		if (h == 0) {
			return String.format("%02dm", m);
		} else {
			return String.format("%dh, %02dm", h, m);
		}
	}
}

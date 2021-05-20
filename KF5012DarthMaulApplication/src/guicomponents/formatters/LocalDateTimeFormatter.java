package guicomponents.formatters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formatter for LocalDateTime objects. Must be given a pattern string.
 * 
 * @author William Taylor
 */
public class LocalDateTimeFormatter implements Formatter<LocalDateTime> {
	private DateTimeFormatter dateTimeFormatter;
	
	public LocalDateTimeFormatter(String format) {
		dateTimeFormatter = DateTimeFormatter.ofPattern(format);
	}

	@Override
	public String apply(LocalDateTime t) {
		return dateTimeFormatter.format(t);
	}
}

package guicomponents.formatters;

/**
 * A formatter that makes its contents read as HTML. Requires a formatter for
 * the type of objects to be read as HTML.
 * 
 * This is a required wrapper for some other formatters.
 * 
 * @author William Taylor
 *
 * @param <T> The type of objects to be read as HTML.
 */
public class HTMLFormatter<T> implements Formatter<T> {
	// Note: This class was made pretty much solely to reference it from other
	//       javadocs.
	
	private static final String HTML_FORMAT = "<html>%s</html>";

	private final Formatter<T> formatter;
	
	public HTMLFormatter(Formatter<T> formatter) {
		this.formatter = formatter;
	}
	
	@Override
	public String apply(T t) {
		return String.format(HTML_FORMAT, formatter.apply(t));
	}

}

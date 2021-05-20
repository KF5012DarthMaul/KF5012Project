package guicomponents.formatters;

/**
 * Formats objects it is applied to in a way that indicates they have been
 * deleted. Requires a formatter for the type of objects to be shown as deleted.
 * 
 * Currently shows text with a strike-through. Must be wrapped with a
 * HTMLFormatter.
 * 
 * @author William Taylor
 *
 * @param <T> The type of objects to be show as deleted.
 */
public class DeletionFormatter<T> implements Formatter<T> {
	// See https://medium.com/swlh/strikethrough-using-html5-26fea2020a72
	private static final String DEL_FORMAT = "<strike>%s</strike>";
	
	private final Formatter<T> formatter;
	
	public DeletionFormatter(Formatter<T> formatter) {
		this.formatter = formatter;
	}
	
	@Override
	public String apply(T t) {
		return String.format(DEL_FORMAT, formatter.apply(t));
	}
}

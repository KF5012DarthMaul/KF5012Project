package guicomponents.formatters;

/**
 * A formatter for strings that displays them in a specified colour.
 * 
 * Colours must be given as valid HTML colour strings, eg. "red" or
 * "rgb(200, 200, 0)". This formatter must be wrapped with a HTMLFormatter.
 * 
 * @author music
 */
public class ColorFormatter implements Formatter<String> {
	// Based on https://www.logicbig.com/tutorials/java-swing/jtree-renderer.html
	private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";
	
	private String htmlColor;
	
	public ColorFormatter(String htmlColor) {
		this.htmlColor = htmlColor;
	}
	
	@Override
	public String apply(String t) {
		return String.format(SPAN_FORMAT, htmlColor, t);
	}
}

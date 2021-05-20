package guicomponents.formatters;

/**
 * Identity formatter for strings - returns what it has been given.
 * 
 * This formatter is useful to semantically show 'current' or 'default' format
 * in some contexts, or using as a termination point in a formatter chain.
 * 
 * @author William Taylor
 */
public class IdentityFormatter implements Formatter<String> {
	@Override
	public String apply(String t) {
		return t;
	}
}

package guicomponents.formatters;

import java.util.function.Function;

/**
 * Defines a formatter for a type of object.
 * 
 * That type of object may be String, but is often other types.
 * 
 * @author William Taylor
 *
 * @param <T> The type of object that can be formatted by this formatter.
 */
public interface Formatter<T> extends Function<T, String> {}

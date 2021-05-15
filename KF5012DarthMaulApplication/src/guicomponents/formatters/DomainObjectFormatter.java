package guicomponents.formatters;

import java.util.function.Function;

public interface DomainObjectFormatter<T> extends Function<T, String> {}

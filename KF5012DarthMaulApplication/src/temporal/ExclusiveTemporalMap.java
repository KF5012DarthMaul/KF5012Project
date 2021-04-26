package temporal;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * A TemporalMap that can be verified to hold only non-overlapping events.
 * 
 * @author William Taylor
 *
 * @param <I> The type of the indexes of this map.
 * @param <T> The type of the events in this map.
 */
public class ExclusiveTemporalMap<I extends Comparable<I>, T extends Event>
		implements TemporalMap<I, T>
{
	private final TemporalMap<I, T> map;
	private final Comparator<Event> exclusivityMetric;
	
	public ExclusiveTemporalMap(
			TemporalMap<I, T> map, Comparator<Event> exclusivityMetric
	) {
		this.map = map;
		this.exclusivityMetric = exclusivityMetric;
	}

	// Validity checking
	
	public boolean isValid(I index) {
		Period p = this.get(index).getPeriod();
		List<T> items = this.getBetween(
			p.start(), p.end(), this.exclusivityMetric, false, false
		);
		return items.isEmpty();
	}

	// Delegate to map for everything

	@Override
	public I indexBefore(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		return this.map.indexBefore(time, eventOrder, inclusive);
	}

	@Override
	public List<I> indexesAt(LocalDateTime time, Comparator<Event> eventOrder) {
		return this.map.indexesAt(time, eventOrder);
	}

	@Override
	public I indexAfter(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		return this.map.indexAfter(time, eventOrder, inclusive);
	}

	@Override
	public T get(I index) {
		return this.map.get(index);
	}

	@Override
	public List<T> getBefore(I index, boolean inclusive) {
		return this.map.getBefore(index, inclusive);
	}

	@Override
	public List<T> getAfter(I index, boolean inclusive) {
		return this.map.getAfter(index, inclusive);
	}

	@Override
	public List<T> getBetween(
			I start, I end, boolean includeStart, boolean includeEnd
	) {
		return this.map.getBetween(start, end, includeStart, includeEnd);
	}

	@Override
	public List<T> getBefore(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		return this.map.getBefore(time, eventOrder, inclusive);
	}

	@Override
	public List<T> getAfter(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		return this.map.getAfter(time, eventOrder, inclusive);
	}

	@Override
	public List<T> getBetween(
			LocalDateTime start, LocalDateTime end,
			Comparator<Event> eventOrder,
			boolean includeStart, boolean includeEnd
	) {
		return this.map.getBetween(
			start, end, eventOrder, includeStart, includeEnd
		);
	}
}

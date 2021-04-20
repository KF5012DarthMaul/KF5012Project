package temporal;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * A TemporalMap that can only store chartable events, so can itself be charted.
 * 
 * Unofficially, see TimelinePanel.
 * 
 * @author William Taylor
 *
 * @param <T> The type of chartable event this map stores.
 */
public class ChartableTemporalMap
		<I extends Comparable<I>, T extends ChartableEvent>
		implements TemporalMap<I, T>
{
	private final TemporalMap<I, T> map;
	
	public ChartableTemporalMap(TemporalMap<I, T> map) {
		this.map = map;
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

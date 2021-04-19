package temporal;

import java.time.LocalDateTime;
import java.util.List;

public class ExclusiveTemporalMap<I extends Comparable<I>, T extends Event>
		implements TemporalMap<I, T>
{
	private TemporalMap<I, T> map;
	
	public ExclusiveTemporalMap(TemporalMap<I, T> map) {
		this.map = map;
	}
	
	// Delegate to map for everything

	@Override
	public I indexBefore(LocalDateTime time) {
		return this.map.indexBefore(time);
	}

	@Override
	public List<I> indexesAt(LocalDateTime time) {
		return this.map.indexesAt(time);
	}

	@Override
	public I indexAfter(LocalDateTime time) {
		return this.map.indexAfter(time);
	}

	@Override
	public T get(I index) {
		return this.map.get(index);
	}

	@Override
	public List<T> getBefore(I index) {
		return this.map.getBefore(index);
	}

	@Override
	public List<T> getAfter(I index) {
		return this.map.getAfter(index);
	}

	@Override
	public List<T> getBetween(I start, I end) {
		return this.map.getBetween(start, end);
	}

	@Override
	public List<T> getBefore(LocalDateTime time) {
		return this.map.getBefore(time);
	}

	@Override
	public List<T> getAfter(LocalDateTime time) {
		return this.map.getAfter(time);
	}

	@Override
	public List<T> getBetween(LocalDateTime start, LocalDateTime end) {
		return this.map.getBetween(start, end);
	}
}

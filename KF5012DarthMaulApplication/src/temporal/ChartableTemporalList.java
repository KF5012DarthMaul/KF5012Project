package temporal;

import java.time.LocalDateTime;
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
public class ChartableTemporalList<T extends ChartableEvent>
		implements TemporalMap<Integer, T>
{
	private TemporalMap<Integer, T> map;
	
	public ChartableTemporalList(TemporalMap<Integer, T> map) {
		this.map = map;
	}
	
	// Delegate to map for everything

	@Override
	public Integer indexBefore(LocalDateTime time) {
		return this.map.indexBefore(time);
	}

	@Override
	public List<Integer> indexesAt(LocalDateTime time) {
		return this.map.indexesAt(time);
	}

	@Override
	public Integer indexAfter(LocalDateTime time) {
		return this.map.indexAfter(time);
	}

	@Override
	public T get(Integer index) {
		return this.map.get(index);
	}

	@Override
	public List<T> getBefore(Integer index) {
		return this.map.getBefore(index);
	}

	@Override
	public List<T> getAfter(Integer index) {
		return this.map.getAfter(index);
	}

	@Override
	public List<T> getBetween(Integer start, Integer end) {
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

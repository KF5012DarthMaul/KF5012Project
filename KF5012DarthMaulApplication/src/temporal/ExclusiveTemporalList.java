package temporal;

import java.time.LocalDateTime;
import java.util.List;

public class ExclusiveTemporalList<T extends Event>
		implements TemporalMap<Integer, T>
{
	private TemporalMap<Integer, T> map;
	
	public ExclusiveTemporalList(TemporalMap<Integer, T> map) {
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

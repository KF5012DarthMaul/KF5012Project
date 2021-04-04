package temporal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TemporalList<T extends Event> implements TemporalMap<Integer, T> {
	private final List<T> events;

	public TemporalList(List<T> events) {
		this.events = events;
		Collections.sort(this.events);
	}

	@Override
	public Integer indexBefore(LocalDateTime time) {
		int index = Collections.binarySearch(
			this.events, new BasicEvent(new Period(time))
		);

		// In the event that 'now' is in the events ...
		Integer retIndex;
		if (index >= 0) {
			retIndex = index - 1; // Always the index before now
		} else {
			// Collections.binarySearch() returns (-<insertion point> - 1) if
			// not found; doing (-index - 1) will reverse that and produce the
			// index of where it would have been had it been found (ie. the next
			// index). The following results in (0 - 1) = -1 if the insertion
			// position would be 0 (which it would be for the empty list or if
			// there are no items that sort before it).
			retIndex = (-index - 1) - 1;
		}
		
		if (retIndex == -1) {
			return null;
		} else {
			return retIndex;
		}
	}

	/**
	 * Returns the indexes at a given time.
	 * 
	 * For this type, this list is guaranteed to contain 0 or 1 events.
	 */
	@Override
	public List<Integer> indexesAt(LocalDateTime time) {
		int index = Collections.binarySearch(
			this.events, new BasicEvent(new Period(time))
		);

		List<Integer> list = new ArrayList<>();
		if (index > 0) list.add(index);
		return list;
	}
	
	@Override
	public Integer indexAfter(LocalDateTime time) {
		int index = Collections.binarySearch(
			this.events, new BasicEvent(new Period(time))
		);
		
		Integer retIndex = null;

		// In the event that 'now' is in the events ...
		if (index >= 0) {
			if (index + 1 < this.size()) { // is there one after?
				retIndex = index + 1; // The index *after* now
			}
		} else {
			// Collections.binarySearch() returns (-<insertion point> - 1) if
			// not found; doing (-index - 1) will reverse that and produce the
			// index of where it would have been had it been found (ie. the next
			// index). This results in returning -1 if events is empty.
			int potentialRetIndex = -index - 1;
			if (potentialRetIndex < this.size()) {
				retIndex = potentialRetIndex;
			}
		}
		
		return retIndex;
	}

	@Override
	public T get(Integer index) {
		return this.events.get(index);
	}

	@Override
	public List<T> getBefore(Integer index) {
		return new ArrayList<>(this.events.subList(0, index));
	}

	@Override
	public List<T> getAfter(Integer index) {
		return new ArrayList<>(this.events.subList(index, this.size()));
	}
	
	@Override
	public List<T> getBetween(Integer start, Integer end) {
		return new ArrayList<>(this.events.subList(start + 1, end));
	}
	
	@Override
	public List<T> getBefore(LocalDateTime time) {
		RangeIndex startIndex = this.getStartRangeIndexExclusive(time);
		List<T> events = this.getBefore(startIndex.index);
		if (startIndex.inclusive) {
			events.add(this.get(startIndex.index));
		}
		return events;
	}

	@Override
	public List<T> getAfter(LocalDateTime time) {
		RangeIndex endIndex = this.getEndRangeIndexExclusive(time);
		List<T> events = this.getAfter(endIndex.index);
		if (endIndex.inclusive) {
			events.add(0, this.get(endIndex.index));
		}
		return events;
	}

	@Override
	public List<T> getBetween(LocalDateTime start, LocalDateTime end) {
		RangeIndex startIndex = this.getStartRangeIndexExclusive(start);
		RangeIndex endIndex = this.getEndRangeIndexExclusive(end);
		
		T startEvent = this.get(startIndex.index);
		T endEvent = this.get(endIndex.index);
		
		if (
				// If the map is empty
				// Note: Neither or both of these will be null, as the map
				// cannot be both empty and non-empty, but I'm checking
				// anyway.
				startIndex == null || endIndex == null ||

				// If the found start index is >= end
				!startEvent.getPeriod().start().isBefore(end)

				// If the found end index is <= start
				|| !endEvent.getPeriod().start().isAfter(start)
		) {
			return new ArrayList<>();
			
		} else {
			List<T> events = this.getBetween(startIndex.index, endIndex.index);
			if (startIndex.inclusive) {
				events.add(0, startEvent);
			}
			if (endIndex.inclusive) {
				events.add(endEvent);
			}
			return events;
		}
	}

	/* RangeIndex and utilities that use it
	 * -------------------------------------------------- */
	
	private class RangeIndex {
		public int index;
		public boolean inclusive;
		
		public RangeIndex(Integer index, boolean inclusive) {
			this.index = index;
			this.inclusive = inclusive;
		}
	}
	
	private RangeIndex getStartRangeIndexExclusive(LocalDateTime time) {
		List<Integer> indexesAt = this.indexesAt(time);
		if (indexesAt.size() > 0) {
			return new RangeIndex(indexesAt.get(indexesAt.size() - 1), false);
		}
		
		Integer indexBefore = this.indexBefore(time);
		if (indexBefore != null) {
			return new RangeIndex(indexBefore, false);
		}
		
		Integer indexAfter = this.indexAfter(time);
		if (indexAfter != null) {
			return new RangeIndex(indexAfter, true);
		}
		
		return null; // empty map
	}

	private RangeIndex getEndRangeIndexExclusive(LocalDateTime time) {
		List<Integer> indexesAt = this.indexesAt(time);
		if (indexesAt.size() > 0) {
			return new RangeIndex(indexesAt.get(0), false);
		}

		Integer indexAfter = this.indexAfter(time);
		if (indexAfter != null) {
			return new RangeIndex(indexAfter, false);
		}
		
		Integer indexBefore = this.indexBefore(time);
		if (indexBefore != null) {
			return new RangeIndex(indexBefore, true);
		}
		
		return null; // empty map
	}

	/* Other utilities for a fixed-length temporal map
	 * -------------------------------------------------- */
	
	public int size() {
		return this.events.size();
	}

	public LocalDateTime getEarliestStartDate() {
		return this.events.get(0).getPeriod().start();
	}

	public LocalDateTime getLatestEndDate() {
		return this.events.get(this.size() - 1).getPeriod().end();
	}
}

package temporal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public class Timeline<I extends Comparable<? super I>, T extends Event>
		implements TemporalMap<Timeline.Index, T>
{
	private List<TemporalMap<I, T>> maps;

	public Timeline(List<TemporalMap<I, T>> maps) {
		this.maps = maps;
	}

	/* Implement TemporalMap<Timeline.Index, T>
	 * -------------------------------------------------- */

	@Override
	public Index indexBefore(LocalDateTime time) {
		I latestIndex = null;
		Index latestTimelineIndex = null;
		for (int mapIndex = 0; mapIndex < this.maps.size(); mapIndex++) {
			TemporalMap<I, T> map = this.maps.get(mapIndex);
			
			I indexBefore = map.indexBefore(time);
			if (latestIndex == null || indexBefore.compareTo(latestIndex) > 0) {
				latestIndex = indexBefore;
				latestTimelineIndex = new IndexImpl(mapIndex, latestIndex);
			}
		}
		return latestTimelineIndex;
	}

	@Override
	public List<Index> indexesAt(LocalDateTime time) {
		List<List<Index>> indexLists = new ArrayList<>();
		for (int mapIndex = 0; mapIndex < this.maps.size(); mapIndex++) {
			TemporalMap<I, T> map = this.maps.get(mapIndex);
			
			List<I> indexesInMap = map.indexesAt(time); // Sorted
			List<Index> indexesInTimeline = new ArrayList<>();
			for (I indexInMap : indexesInMap) {
				indexesInTimeline.add(new IndexImpl(mapIndex, indexInMap));
			}
			indexLists.add(indexesInTimeline);
		}
		return mergeSortedLists(indexLists);
	}
	
	@Override
	public Index indexAfter(LocalDateTime time) {
		I earliestIndex = null;
		Index earliestTimelineIndex = null;
		for (int mapIndex = 0; mapIndex < this.maps.size(); mapIndex++) {
			TemporalMap<I, T> map = this.maps.get(mapIndex);
			
			I indexAfter = map.indexAfter(time);
			if (
					earliestIndex == null ||
					indexAfter.compareTo(earliestIndex) < 0
			) {
				earliestIndex = indexAfter;
				earliestTimelineIndex = new IndexImpl(mapIndex, earliestIndex);
			}
		}
		return earliestTimelineIndex;
	}
	
	@Override
	public T get(Index index) {
		IndexImpl indexImpl = (IndexImpl) index; // See NOTE 1
		return maps.get(indexImpl.mapIndex).get(indexImpl.eventIndexInMap);
	}

	@Override
	public List<T> getBefore(Index index) {
		// I'd rather not have to hard-code start() as *the* time of the event,
		// but I couldn't think of any way around this.
		LocalDateTime time = this.get(index).getPeriod().start();
		return this.getBefore(time);
	}
	
	@Override
	public List<T> getAfter(Index index) {
		LocalDateTime time = this.get(index).getPeriod().start();
		return this.getAfter(time);
	}

	@Override
	public List<T> getBetween(Index start, Index end) {
		// if start and end are the same, then skip the rest
		if (start.equals(end)) {
			return new ArrayList<>();
		}

		List<List<T>> eventLists = new ArrayList<>();
		for (int mapIndex = 0; mapIndex < this.maps.size(); mapIndex++) {
			TemporalMap<I, T> map = this.maps.get(mapIndex);
			
			RangeIndex mapStart = getStartRangeIndexExclusive(mapIndex, start);
			RangeIndex mapEnd = getEndRangeIndexExclusive(mapIndex, end);

			if (
					// If the map is empty
					// Note: Neither or both of these will be null, as the map
					// cannot be both empty and non-empty, but I'm checking
					// anyway.
					mapStart == null || mapEnd == null ||
					
					// If the found start index is >= end
					(new IndexImpl(mapIndex, mapStart.index))
						.compareTo(end) >= 0 ||
					
					// If the found end index is <= start
					(new IndexImpl(mapIndex, mapEnd.index))
						.compareTo(start) <= 0
			) {
				eventLists.add(new ArrayList<>());
				
			} else {
				// Add events between (adding start and end as needed)
				List<T> events = map.getBetween(mapStart.index, mapEnd.index);
				if (mapStart.inclusive) {
					events.add(0, map.get(mapStart.index));
				}
				if (mapEnd.inclusive) {
					events.add(map.get(mapEnd.index));
				}
				eventLists.add(events);
			}
		}
		
		return mergeSortedLists(eventLists);
	}

	/* Retrieve items by time
	 * -------------------- */

	public List<T> getBefore(LocalDateTime time) {
		List<List<T>> eventsBefore = new ArrayList<>();
		for (int mapIndex = 0; mapIndex < this.maps.size(); mapIndex++) {
			TemporalMap<I, T> map = this.maps.get(mapIndex);
			
			RangeIndex end = this.getEndRangeIndexExclusive(mapIndex, time);
			if (end == null) { // Map empty
				eventsBefore.add(new ArrayList<>());
			} else {
				List<T> eventsBeforeInMap = map.getBefore(end.index);
				if (end.inclusive) {
					eventsBeforeInMap.add(map.get(end.index));
				}
				eventsBefore.add(eventsBeforeInMap);
			}
		}
		return mergeSortedLists(eventsBefore);
	}

	public List<T> getAfter(LocalDateTime time) {
		List<List<T>> eventsAfter = new ArrayList<>();
		for (int mapIndex = 0; mapIndex < this.maps.size(); mapIndex++) {
			TemporalMap<I, T> map = this.maps.get(mapIndex);
			
			RangeIndex start = this.getStartRangeIndexExclusive(mapIndex, time);
			if (start == null) { // Map empty
				eventsAfter.add(new ArrayList<>());
			} else {
				List<T> eventsAfterInMap = map.getAfter(start.index);
				if (start.inclusive) {
					eventsAfterInMap.add(0, map.get(start.index));
				}
				eventsAfter.add(eventsAfterInMap);
			}
		}
		return mergeSortedLists(eventsAfter);
	}

	public List<T> getBetween(LocalDateTime start, LocalDateTime end) {
		// if start and end are the same, then skip the rest
		if (start.equals(end)) {
			return new ArrayList<>();
		}
		
		List<List<T>> eventLists = new ArrayList<>();
		for (int mapIndex = 0; mapIndex < this.maps.size(); mapIndex++) {
			TemporalMap<I, T> map = this.maps.get(mapIndex);
			
			RangeIndex mapStart = this.getStartRangeIndexExclusive(
				mapIndex, start);
			RangeIndex mapEnd = this.getEndRangeIndexExclusive(mapIndex, end);
			
			if (
					// If the map is empty
					// Note: Neither or both of these will be null, as the map
					// cannot be both empty and non-empty, but I'm checking
					// anyway.
					mapStart == null || mapEnd == null ||

					// If the found start index is >= end
					!map.get(mapStart.index).getPeriod().start().isBefore(end)

					// If the found end index is <= start
					|| !map.get(mapEnd.index).getPeriod().start().isAfter(start)
			) {
				eventLists.add(new ArrayList<>());
				
			} else {
				// Add events between (adding start and end as needed)
				List<T> events = map.getBetween(mapStart.index, mapEnd.index);
				if (mapStart.inclusive) {
					events.add(0, map.get(mapStart.index));
				}
				if (mapEnd.inclusive) {
					events.add(map.get(mapEnd.index));
				}
				eventLists.add(events);
			}
		}
		return mergeSortedLists(eventLists);
	}

	/* Other Public Components
	 * -------------------------------------------------- */

	public TemporalMap<I, T> getMap(int mapIndex) {
		return this.maps.get(mapIndex);
	}
	public List<TemporalMap<I, T>> getAllMaps() {
		return this.maps;
	}
	
	/* Own Components (mainly utilities)
	 * -------------------------------------------------- */

	/* Index, IndexImpl
	 * -------------------- */
	 
	// Public interface, private implementation idea from:
	//   https://stackoverflow.com/a/59525639
	public interface Index extends Comparable<Index> {}
	
	@SuppressWarnings("rawtypes")
	private final class IndexImpl implements Index {
		public int mapIndex;
		public I eventIndexInMap;
		
		public IndexImpl(int mapIndex, I eventIndexInMap) {
			this.mapIndex = mapIndex;
			this.eventIndexInMap = eventIndexInMap;
		}
		@SuppressWarnings("unused")
		public IndexImpl() {
			this.mapIndex = -1;
			this.eventIndexInMap = null;
		}

		@Override
		public int compareTo(Index index) {
			// NOTE 1: If it is an Index, then it must be an IndexImpl because
			// you can't implement Index without access to private fields of the
			// outer class (Timeline).
			IndexImpl indexImpl = (IndexImpl) index;
			
			// Use Event type rather than T to protect against
			// ClassCastException if used on temporal maps of different T types.
			// This can't be checked at runtime due to erasure.
			Event event1 = maps.get(this.mapIndex).get(this.eventIndexInMap);
			Event event2 = maps.get(indexImpl.mapIndex)
				.get(indexImpl.eventIndexInMap);
			
			// First natural ordering of indexes matches the natural ordering of
			// the events they identify.
			int comparison = event1.compareTo(event2);
			
			// Second natural ordering is by map index.
			if (comparison == 0) {
				comparison = ((Integer) this.mapIndex)
					.compareTo(indexImpl.mapIndex);
			}
			
			return comparison;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Timeline.IndexImpl)) return false;
			Timeline.IndexImpl index = (Timeline.IndexImpl) o;
			
			// Also check that the types of the eventIndexInMap's are *equal*.
			// It may be that they are comparable, but that cannot be checked at
			// runtime due to erasure. Besides, if they're not equal, then you
			// are probably doing something wrong anyway, like comparing indices
			// of two different Timelines that store temporal maps with
			// different indexing schemes, which is likely to be a bug on your
			// end.
			if (
					this.eventIndexInMap.getClass() !=
					index.eventIndexInMap.getClass()
			) {
				return false;
			}
			
			return (
				this.mapIndex == index.mapIndex &&
				this.eventIndexInMap == index.eventIndexInMap
			);
		}
	}

	/* RangeIndex and methods that use it
	 * -------------------- */
	
	// NOTE: The class and the first two methods have quite a bit in common with
	//       the equivalent class/methods in BasicTemporalMap due to the nature
	//       of TemporalMap. It may be possible to extract the first four of
	//       these into a separate utils class.
	 
	private class RangeIndex {
		public I index;
		public boolean inclusive;
		
		public RangeIndex(I index, boolean inclusive) {
			this.index = index;
			this.inclusive = inclusive;
		}
	}
	
	private RangeIndex getStartRangeIndexExclusive(
			int mapIndex, LocalDateTime time
	) {
		TemporalMap<I, T> map = this.maps.get(mapIndex);
		
		List<I> indexesAt = map.indexesAt(time);
		if (indexesAt.size() > 0) {
			return new RangeIndex(indexesAt.get(indexesAt.size() - 1), false);
		}
		
		I indexBefore = map.indexBefore(time);
		if (indexBefore != null) {
			return new RangeIndex(indexBefore, false);
		}
		
		I indexAfter = map.indexAfter(time);
		if (indexAfter != null) {
			return new RangeIndex(indexAfter, true);
		}
		
		return null; // empty map
	}

	private RangeIndex getEndRangeIndexExclusive(
			int mapIndex, LocalDateTime time
	) {
		TemporalMap<I, T> map = this.maps.get(mapIndex);
		
		List<I> indexesAt = map.indexesAt(time);
		if (indexesAt.size() > 0) {
			return new RangeIndex(indexesAt.get(0), false);
		}

		I indexAfter = map.indexAfter(time);
		if (indexAfter != null) {
			return new RangeIndex(indexAfter, false);
		}
		
		I indexBefore = map.indexBefore(time);
		if (indexBefore != null) {
			return new RangeIndex(indexBefore, true);
		}
		
		return null; // empty map
	}

	private RangeIndex getStartRangeIndexInclusive(
			int mapIndex, LocalDateTime time
	) {
		TemporalMap<I, T> map = this.maps.get(mapIndex);

		I indexBefore = map.indexBefore(time);
		if (indexBefore != null) {
			return new RangeIndex(indexBefore, false);
		}
		
		List<I> indexesAt = map.indexesAt(time);
		if (indexesAt.size() > 0) {
			return new RangeIndex(indexesAt.get(0), true);
		}
		
		I indexAfter = map.indexAfter(time);
		if (indexAfter != null) {
			return new RangeIndex(indexAfter, true);
		}
		
		return null; // empty map
	}

	private RangeIndex getEndRangeIndexInclusive(
			int mapIndex, LocalDateTime time
	) {
		TemporalMap<I, T> map = this.maps.get(mapIndex);

		I indexAfter = map.indexAfter(time);
		if (indexAfter != null) {
			return new RangeIndex(indexAfter, false);
		}
		
		List<I> indexesAt = map.indexesAt(time);
		if (indexesAt.size() > 0) {
			return new RangeIndex(indexesAt.get(indexesAt.size() - 1), true);
		}

		I indexBefore = map.indexBefore(time);
		if (indexBefore != null) {
			return new RangeIndex(indexBefore, true);
		}
		
		return null; // empty map
	}

	// may return null (ie. map with given index is empty)
	private RangeIndex getStartRangeIndexExclusive(int mapIndex, Index index) {
		IndexImpl indexImpl = (IndexImpl) index; // See NOTE 1
		LocalDateTime time = this.get(indexImpl).getPeriod().start();
		
		// If mapIndex is before the map of the given index, exclude all events
		// at the time of that index (times after the time of the given index
		// would still sort after all maps at the time of the given index).
		if (mapIndex < indexImpl.mapIndex) {
			return this.getStartRangeIndexExclusive(mapIndex, time);
			
		// If mapIndex is at the given map index, include what was requested
		} else if (mapIndex == indexImpl.mapIndex) {
			return new RangeIndex(indexImpl.eventIndexInMap, false);

		// If mapIndex is after the start, include all events at the start
		} else {
			return this.getStartRangeIndexInclusive(mapIndex, time);
		}
	}

	private RangeIndex getEndRangeIndexExclusive(int mapIndex, Index index) {
		IndexImpl indexImpl = (IndexImpl) index; // See NOTE 1
		LocalDateTime time = this.get(indexImpl).getPeriod().start();
		
		// If mapIndex is before the map of the given index, include all events
		// at the time of that index (times before the time of the given index
		// would still sort before all maps at the time of the given index).
		if (mapIndex < indexImpl.mapIndex) {
			return this.getEndRangeIndexInclusive(mapIndex, time);
			
		// If mapIndex is at the given map index, include what was requested
		} else if (mapIndex == indexImpl.mapIndex) {
			return new RangeIndex(indexImpl.eventIndexInMap, false);

		// If mapIndex is after the end, exclude all events at the end
		} else {
			return this.getEndRangeIndexExclusive(mapIndex, time);
		}
	}
	
	/* Generic merge sorted lists algorithm
	 * -------------------- */
	 
	// This is efficient for List<ArrayList<C>>, but will work for any List type
	// WARNING: This is a mutating operation (of the List<C>s in lists)!!!
	private static <C extends Comparable<? super C>>
			List<C> mergeSortedLists(List<List<C>> lists)
	{
		List<C> retList = new LinkedList<>();
		
		while (getTotalSize(lists) != 0) {
			// Find latest item
			C latestC = null;
			List<C> listWithLatestC = null;
			for (int i = 0; i < lists.size(); i++) {
				List<C> list = lists.get(i);
				C lastC = list.get(list.size() - 1);
				
				if (
						latestC == null || // if first found
						// or if 'after', by time or list index
						lastC.compareTo(latestC) >= 0
				) {
					latestC = lastC;
					listWithLatestC = list;
				}
			}
			
			// pop/push latest item
			assert listWithLatestC != null;
			listWithLatestC.remove(listWithLatestC.size() - 1);
			retList.add(0, latestC);
		}
		
		return retList;
	}

	private static <M> int getTotalSize(List<List<M>> lists) {
		return lists.stream().mapToInt(l -> l.size()).sum();
	}
}

package temporal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple TemporalMap by Integer index.
 * 
 * It cannot contain overlapping events.
 * 
 * @author music
 *
 * @param <T> The Event type this list stores.
 */
public class TemporalList<T extends Event> implements TemporalMap<Integer, T> {
	private final List<T> events;

	public TemporalList(List<T> events) {
		this.events = events;
		Collections.sort(this.events, Event.byStartTime);
	}

	/**
	 * Returns the indexes 'before' a given time.
	 * 
	 * Because this type requires unique start times and sorts by start time,
	 * Events at indexes before the returned index are guaranteed to all be
	 * 'before' the given time when using Event.byStartTime. Other comparators
	 * have no such guarantee (ie. a non-contiguous set of indexes before the
	 * returned index may be 'before' the given time).
	 */
	@Override
	public Integer indexBefore(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		Event timeEvent = new BasicEvent(new Period(time));
		
		// NOTE 1: Search by start time - events 'at' and/or 'after' this point
		// can then be excluded, as Event doesn't allow a negative duration.
		int index = Collections.binarySearch(this.events, timeEvent);

		// Based on start time:
		// (found -> event 'at'; not found -> event 'after', if any)
		if (index < 0) index = (-index - 1);
		// (inclusive and exists -> index 'at'; exclusive or doesn't exist ->
		//  index 'before', if any)
		int retIndex;
		if (inclusive && index < this.size()) {
			retIndex = index;
		} else {
			retIndex = index - 1;
		}
		
		// Partial linear search. Search backward (down to 0, inclusive) to find
		// the greatest index 'before'.
		// Note: not all indices before the found index are necessarily 'before'
		// (based on eventOrder), eg. if the event before the found index has a
		// very long duration and Event.byPeriodDefaultZero or
		// Event.byPeriodDefaultInf is used then it may not be 'before'.
		for (; retIndex >= 0; retIndex--) {
			Event candidate = this.get(retIndex);
			int comp = eventOrder.compare(candidate, timeEvent);
			if (comp < 0 || (inclusive && comp == 0)) {
				break; // 'before', or possibly 'at', timeEvent
			}
		}
		
		// Return the contracted value
		if (retIndex == -1) {
			return null;
		} else {
			return retIndex;
		}
	}

	/**
	 * Returns the indexes at a given time.
	 * 
	 * Because this type requires unique start times, then the returned list is
	 * guaranteed to contain 0 or 1 events if using the Event.byStartTime
	 * comparator. Other comparators may yield multiple events.
	 */
	@Override
	public List<Integer> indexesAt(
			LocalDateTime time, Comparator<Event> eventOrder
	) {
		Event timeEvent = new BasicEvent(new Period(time, time));
		int index = Collections.binarySearch(this.events, timeEvent); // NOTE 1

		// Based on start time:
		// (found -> event 'at'; not found -> event 'after', if any)
		if (index < 0) index = (-index - 1);
		// (exists -> index 'at'; doesn't exist -> index 'before', if any)
		int realIndex;
		if (index < this.size()) {
			realIndex = index;
		} else {
			realIndex = index - 1;
		}
		
		// Partial linear search. As not all indices before the first one found
		// are necessarily before, this is also O(n) best-case.
		List<Integer> list = new ArrayList<>();
		for (; realIndex >= 0; realIndex--) {
			Event candidate = this.get(realIndex);
			if (eventOrder.compare(candidate, timeEvent) == 0) {
				list.add(realIndex); // 'at' timeEvent
			}
		}
		
		return list;
	}
	
	/**
	 * Returns the indexes 'after' a given time.
	 * 
	 * Because this type requires unique start times and sorts by start time,
	 * Events at indexes after the returned index are guaranteed to all be
	 * 'after' the given time when using Event.byStartTime. Other comparators
	 * have no such guarantee (ie. a non-contiguous set of indexes after the
	 * returned index may be 'after' the given time).
	 */
	@Override
	public Integer indexAfter(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		Event timeEvent = new BasicEvent(new Period(time, time));
		
		// Full linear search. As any Events before or after the found binary
		// search-ed time could be 'after' (eg. if using Event.byEndTime), then
		// we have to search all elements 'before' and 'at'. The first element
		// 'after' by start time will definitely be 'after' by eventOrder, but
		// there's no point in special-casing it, as that just increases code
		// surface for bugs.
		Integer retIndex = null;
		for (int i = 0; i < this.size(); i++) {
			Event candidate = this.get(i);
			int comp = eventOrder.compare(candidate, timeEvent);
			if (comp > 0 || (inclusive && comp == 0)) {
				retIndex = i; // 'after' timeEvent
				break;
			}
		}
		
		return retIndex;
	}

	@Override
	public T get(Integer index) {
		return this.events.get(index);
	}

	@Override
	public List<T> getBefore(Integer index, boolean inclusive) {
		int realIndex = index;
		if (inclusive) index++;
		return new ArrayList<>(this.events.subList(0, realIndex));
	}

	@Override
	public List<T> getAfter(Integer index, boolean inclusive) {
		int realIndex = index;
		if (!inclusive) index++;
		return new ArrayList<>(this.events.subList(realIndex, this.size()));
	}
	
	@Override
	public List<T> getBetween(
			Integer start, Integer end, boolean includeStart, boolean includeEnd
	) {
		int realStart = start;
		int realEnd = end;
		if (!includeStart) realStart++;
		if (includeEnd) realEnd++;
		return new ArrayList<>(this.events.subList(realStart, realEnd));
	}
	
	@Override
	public List<T> getBefore(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		Event timeEvent = new BasicEvent(new Period(time, time));
		int index = Collections.binarySearch(this.events, timeEvent); // NOTE 1

		// Based on start time:
		// (found -> event 'at'; not found -> event 'after', if any)
		if (index < 0) index = (-index - 1);
		// (inclusive and exists -> index 'at'; exclusive or doesn't exist ->
		//  index 'before', if any)
		int realIndex;
		if (inclusive && index < this.size()) {
			realIndex = index;
		} else {
			realIndex = index - 1;
		}

		List<T> events = new ArrayList<>();
		for (int i = 0; i <= realIndex; i++) {
			T candidate = this.get(i);
			int comp = eventOrder.compare(candidate, timeEvent);
			if (comp < 0 || (inclusive && comp == 0)) {
				events.add(candidate); // 'before' timeEvent
			}
		}
		return events;
	}

	@Override
	public List<T> getAfter(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		Event timeEvent = new BasicEvent(new Period(time, time));

		// Full linear search. As any Events before or after the found binary
		// search-ed time could be 'after' (eg. if using Event.byEndTime), then
		// we have to search all elements 'before' and 'at'. The first element
		// 'after' by start time will definitely be 'after' by eventOrder, but
		// there's no point in special-casing it, as that just increases code
		// surface for bugs.
		List<T> list = new ArrayList<>();
		for (int i = 0; i < this.size(); i++) {
			T candidate = this.get(i);
			int comp = eventOrder.compare(candidate, timeEvent);
			if (comp > 0 || (inclusive && comp == 0)) {
				list.add(candidate);
			}
		}
		
		return list;
	}

	@Override
	public List<T> getBetween(
			LocalDateTime start, LocalDateTime end,
			Comparator<Event> eventOrder,
			boolean includeStart, boolean includeEnd
	) {
		Event startEvent = new BasicEvent(new Period(start, start));
		Event endEvent = new BasicEvent(new Period(end, end));
		
		List<T> events = new ArrayList<>();
		for (int i = 0; i < this.size(); i++) {
			T event = this.get(i);
			int compStart = eventOrder.compare(event, startEvent);
			int compEnd = eventOrder.compare(event, endEvent);
			if (
					(compStart > 0 && compEnd < 0) ||
					(includeStart && compStart == 0) ||
					(includeEnd && compEnd == 0)
			) {
				events.add(event);
			}
		}
		return events;
	}

	/* Other utilities for a fixed-length temporal map
	 * -------------------------------------------------- */
	
	public int size() {
		return this.events.size();
	}
}

package temporal;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * An temporal list-like temporal map that generates requested events
 * incrementally according to the given ConstrainedIntervaledPeriodSet, and
 * tracks them in a (mutable) list given to it.
 * 
 * @author music
 */
public class GenerativeTemporalMap<T extends Event>
		implements TemporalMap<Integer, T>
{
	private enum Direction {
		AT_OR_AFTER,
		AT_OR_BEFORE
	}

	private final List<T> events;
	private final TemporalList<T> map;
	private final ConstrainedIntervaledPeriodSet cips;
	private final Function<Period, T> gen;
	
	public GenerativeTemporalMap(
			List<T> events,
			ConstrainedIntervaledPeriodSet cips,
			Function<Period, T> gen
	) {
		this.events = events;
		this.events.sort(Event.byStartTime);
		this.map = new TemporalList<>(this.events);
		this.cips = cips;
		this.gen = gen;
	}
	
	/* Generation
	 * -------------------------------------------------- */
	
	// For the time being, these assume Event.byStartTime, inclusive
	
	/**
	 * Generate all events up to time based on the constrained intervaled period
	 * set of this map.
	 * 
	 * Equivalent to generateBetween(null, time).
	 * 
	 * @param time The time to generate events up to.
	 */
	public void generateBefore(LocalDateTime time) {
		this.generateBetween(null, time);
	}
	
	/**
	 * Generate all events up to the next event after time based on the
	 * constrained intervaled period set of this map.
	 * 
	 * If time is before the start time of the last-stored event, then do not
	 * generate anything.
	 * 
	 * @param time The time to generate the next event after (and all
	 * intervening events).
	 */
	public void generateNextAfter(LocalDateTime time) {
		Integer eventAfter = this.map.indexAfter(time, Event.byStartTime, true);
		if (eventAfter != null) return;
		
		// If there isn't an event after time
		IntervaledPeriodSet setRef = this.cips.periodSet();
		LocalDateTime setRefStart = setRef.referencePeriod().start();
		Duration setInterval = setRef.interval();
		
		LocalDateTime nextStart;
		if (setInterval == null) {
			if (!setRefStart.isBefore(time)) { // time <= setRefStart
				nextStart = setRefStart;
			} else {
				return; // Nothing to generate
			}
		} else {
			nextStart = this.first(
				Direction.AT_OR_AFTER, time, setRefStart, setInterval
			);
		}
		
		this.generateBefore(nextStart); // Before or including
	}
	
	/**
	 * Generate all events between start or the end of the last event, whichever
	 * is earlier, and end based on the constrained intervaled period set of
	 * this map.
	 * 
	 * Equivalent to generateBetween(start, end, false).
	 * 
	 * @param start The start of the range to generate within, or null to
	 * generate as far back as is needed (like a 'generateBefore()' would be).
	 * @param end The end of the range to generate within.
	 */
	public void generateBetween(LocalDateTime start, LocalDateTime end) {
		this.generateBetween(start, end, false);
	}
	
	/**
	 * Generate all events between start and end based on the constrained
	 * intervaled period set of this map.
	 * 
	 * If allowNonContiguous is false, then also generate events between the end
	 * of the last event and start (if last event end < start).
	 * 
	 * @param start The start of the range to generate within, or null to
	 * generate as far back as is needed (like a 'generateBefore()' would be).
	 * @param end The end of the range to generate within.
	 * @param allowNonContiguous Whether to allow non-contiguous generation, ie.
	 * some events may be missed that will not be generated by generateBetween()
	 * if called again.
	 */
	public void generateBetween(
			LocalDateTime start, LocalDateTime end, boolean allowNonContiguous
	) {
		this.generateBetween(start, end, allowNonContiguous, false);
	}
	
	/**
	 * Private version of generateBetween() that allows for recursive behaviour.
	 * 
	 * @param start The start of the range to generate within, or null to
	 * generate as far back as is needed (like a 'generateBefore()' would be).
	 * @param end The end of the range to generate within.
	 * @param allowNonContiguous Whether to allow non-contiguous generation, ie.
	 * some events may be missed that will not be generated by generateBetween()
	 * if called again.
	 * @param ignoreConstraint Whether to ignore the constraint part of this
	 * map's constrained intervaled period set. This being 'true' is used as the
	 * base-case of recursion.
	 */
	private void generateBetween(
			LocalDateTime start, LocalDateTime end, boolean allowNonContiguous,
			boolean ignoreConstraint
	) {
		// Destructure info
		IntervaledPeriodSet set = cips.periodSet();
		IntervaledPeriodSet cSet = cips.periodSetConstraint();
		
		Period setRef = set.referencePeriod();
		LocalDateTime setRefStart = setRef.start();
		Duration setRefDuration = setRef.duration();
		Duration setInterval = set.interval();

		Period cSetRef = cSet.referencePeriod();
		LocalDateTime cSetRefStart = cSetRef.start();
		LocalDateTime cSetRefEnd = cSetRef.end();
		Duration cSetRefDuration = cSetRef.duration();
		Duration cSetInterval = cSet.interval();
		
		// Initialise vars
		LocalDateTime genStart = start;
		
		/* Push forward (always) or back (if allowed) to last event, if any
		 * -------------------- */
		
		int size = this.map.size();
		if (size != 0) {
			T lastEvent = this.map.get(size - 1);
			LocalDateTime endOfLastEvent = lastEvent.getPeriod().end();
			
			// If genStart < endOfLastEvent, then push forward to endOfLastEvent
			if (genStart == null || genStart.isBefore(endOfLastEvent)) {
				genStart = endOfLastEvent;
			}
			
			// If genStart > endOfLastEvent, then push back to endOfLastEvent,
			// unless non-contiguity is allowed.
			if (genStart.isAfter(endOfLastEvent) && !allowNonContiguous) {
				genStart = endOfLastEvent;
			}
		}
		
		// If genStart > end, there is nothing to do
		if (genStart != null && genStart.isAfter(end)) {
			return;
		}
		
		/* Push forward to the start of the period set, if needed
		 * -------------------- */
		
		// If genStart < setRefStart, then push forward to setRefStart
		if (genStart == null || genStart.isBefore(setRefStart)) {
			genStart = setRefStart;
		}
		
		// If genStart > end, there is nothing to do
		if (genStart.isAfter(end)) {
			return;
		}

		/* Generating based on the period set
		 * -------------------- */
		
		// Note that genStart is now guaranteed to be after the last event (if
		// any) and the period set start.

		// ignoreConstraint is the recursive base-case - see the `else`.
		if (cSet == null || ignoreConstraint) {
			// If there is only one period in the set and
			// (genStart <= setRefStart <= end), then generate an event for that
			// period.
			if (setInterval == null) {
				if (!genStart.isAfter(setRefStart) && !setRefStart.isAfter(end)) {
					this.events.add(this.gen.apply(setRef));
				}
				
			// If there are multiple periods in the set, find the next one, and
			// start generating from there until the end. If the start point of
			// the next period is after the end, this will generate 0 events.
			} else {
				LocalDateTime nextPeriodStart = first(
					Direction.AT_OR_AFTER, genStart, setRefStart, setInterval
				);
				
				Duration d = setRefDuration; // period duration
				for (
						LocalDateTime s = nextPeriodStart; // s = 0
						s.isBefore(end); // s < end
						s = s.plus(setInterval) // s += setInterval
				) {
					/* Note that IntervaledPeriodSet(Period(a, null), b) doesn't
					 * always make sense - an infinite period every b duration,
					 * starting from a. It would mean, eg. a series of tasks
					 * that each must be started after each a (b apart), but may
					 * be done at any point after that, meaning you could leave
					 * them all until next year, then do 100 of them in a day?
					 * There may be a use-case for that in some domains.
					 * Regardless, it is representable, so must be accounted for.
					 */
					this.events.add(this.gen.apply(
						new Period(s, (d == null ? null : s.plus(d)))
					));
				}
			}
			
			return;
		}

		/* Generating for each period in the constraint period set
		 * -------------------- */

		/* For one constraining period
		 * ---------- */
		
		// This is recursive - use each period in the constraint as an input
		// to generateBetween(), passing ignoreConstraint.
		
		// If there is only one constraining period, then generate between:
		// - its start or genStart, whichever is later
		// - its end or this end, whichever is earlier
		if (cSetInterval == null) {
			this.generateBetween(
				genStart.isAfter(cSetRefStart) ? genStart : cSetRefStart,
				end.isBefore(cSetRefEnd) ? end : cSetRefEnd,
				true, // It is deliberately non-contiguous
				true // Recursive base-case
			);
			return;
		}
		
		/* For multiple constraining periods that are effectively one
		 * ---------- */
		
		// Are they really constraining? If the constraint period is
		// infinite, or the the periods otherwise overlap, then it's
		// not. In that case, do the same as above, but only constrain
		// the start (as the end of the constraint period set is
		// infinite).
		if (
				cSetRefEnd == null ||
				cSetRefDuration.compareTo(cSetInterval) >= 0
		) {
			this.generateBetween(
				genStart.isAfter(cSetRefStart) ? genStart : cSetRefStart,
				end,
				true, // It is deliberately non-contiguous
				true // Recursive base-case
			);
			return;
		}

		/* For multiple constraining periods that are truly constraining
		 * ---------- */
		
		// If there is actually a constraint, ie. gaps in the period set
		// to be generated, then iterate through constraint periods and
		// generate between the start/end of those (bounded by genStart
		// and the end).
	
		// Get the earliest time that the periods could start
		LocalDateTime startCPeriodStart = first(
			Direction.AT_OR_BEFORE, genStart, setRefStart, cSetInterval
		);
		if (startCPeriodStart == null) {
			// Cannot return null
			startCPeriodStart = first(
				Direction.AT_OR_AFTER, genStart, setRefStart, cSetInterval
			);
		}

		// For each constraining period, recurse between:
		// - its start or genStart, whichever is later
		// - its end or this end, whichever is earlier
		for (
				LocalDateTime s = startCPeriodStart; // s = 0
				s.isBefore(end); // s < end
				s = s.plus(cSetInterval) // s += cSetInterval
		) {
			LocalDateTime e = cSetRefStart.plus(cSetInterval);
			this.generateBetween(
				genStart.isAfter(s) ? genStart : s, // max(genStart, s)
				end.isBefore(e) ? end : e, // min(end, e)
				true, // It is deliberately non-contiguous
				true // Recursive base-case
			);
		}
	}
	
	/**
	 * Returns the first date/time either AT_OR_BEFORE point or AT_OR_AFTER
	 * point that lies on a snapInterval after start (or start itself).
	 * 
	 * If dir is AT_OR_BEFORE, returns the first date/time at or before point,
	 * or null (no such date/time) if point < start.
	 * 
	 * If dir is AT_OR_AFTER, returns the first date/time at or after point
	 * (will never return null).
	 * 
	 * @param dir The direction in which to find the first point. Either
	 * Direction.AT_OR_BEFORE, or Direction.AT_OR_AFTER
	 * @param point The point to search from.
	 * @param start The start of the interval set.
	 * @param snapInterval The interval after start that produces an infinite
	 * set of periods. Must not be null.
	 * @return The first date/time either AT_OR_BEFORE point or AT_OR_AFTER
	 * point that lies on a snapInterval after start (or start itself).
	 */
	private LocalDateTime first(
			Direction dir,
			LocalDateTime point, LocalDateTime start, Duration snapInterval
	) {
		Duration diff = Duration.between(start, point);
		if (dir == Direction.AT_OR_BEFORE && diff.isNegative()) {
			return null; // Nothing before
		}
		
		long diffMins = diff.toMinutes();
		long intervalMins = snapInterval.toMinutes();
		long intervalsInDiff = diffMins / intervalMins;
		long minsAfterLastInterval = diffMins % intervalMins;

		Duration toInterval;
		if (minsAfterLastInterval == 0) {
			toInterval = diff;
		} else {
			long periodIndex = intervalsInDiff;
			if (dir == Direction.AT_OR_AFTER) {
				periodIndex++;
			}
			
			long minsToPeriod = periodIndex * intervalMins;
			toInterval = Duration.ofMinutes(minsToPeriod);
		}

		return start.plus(toInterval);
	}

	/* Implement TemporalMap<>
	 * -------------------------------------------------- */

	// TODO: The generation methods and these methods don't match on their
	// assumptions!!! They need to incorporate eventOrder and inclusive
	// parameters to be accurate.
	
	@Override
	public Integer indexBefore(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		this.generateBefore(time);
		return this.map.indexBefore(time, eventOrder, inclusive);
	}

	@Override
	public List<Integer> indexesAt(LocalDateTime time, Comparator<Event> eventOrder) {
		this.generateBefore(time);
		return this.map.indexesAt(time, eventOrder);
	}

	@Override
	public Integer indexAfter(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		this.generateNextAfter(time);
		return this.map.indexAfter(time, eventOrder, inclusive);
	}

	@Override
	public T get(Integer index) {
		// who knows ...
		return this.map.get(index);
	}

	@Override
	public List<T> getBefore(Integer index, boolean inclusive) {
		// who knows ...
		return this.map.getBefore(index, inclusive);
	}

	@Override
	public List<T> getAfter(Integer index, boolean inclusive) {
		// who knows ...
		return this.map.getAfter(index, inclusive);
	}

	@Override
	public List<T> getBetween(
			Integer start, Integer end, boolean includeStart, boolean includeEnd
	) {
		// who knows ...
		return this.map.getBetween(start, end, includeStart, includeEnd);
	}

	@Override
	public List<T> getBefore(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		this.generateBefore(time);
		return this.map.getBefore(time, eventOrder, inclusive);
	}

	@Override
	public List<T> getAfter(
			LocalDateTime time, Comparator<Event> eventOrder, boolean inclusive
	) {
		this.generateNextAfter(time);
		return this.map.getAfter(time, eventOrder, inclusive);
	}

	@Override
	public List<T> getBetween(
			LocalDateTime start, LocalDateTime end,
			Comparator<Event> eventOrder,
			boolean includeStart, boolean includeEnd
	) {
		this.generateBetween(start, end);
		return this.map.getBetween(
			start, end, eventOrder, includeStart, includeEnd
		);
	}
}

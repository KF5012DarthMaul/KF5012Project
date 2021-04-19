package temporal;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A mapping from an index type to an event type in a temporal space.
 * 
 * In this temporal space, a single event will always have a single index and a
 * single index will always represent a single event. This interface does not
 * constrain how many events may be stored at a single time, though subtypes may
 * impose such constraints.
 * 
 * @author William Taylor
 *
 * @param <I> The type of the indexes of this map.
 * @param <T> The type of the events in this map.
 */
public interface TemporalMap<I extends Comparable<? super I>, T extends Event> {
	/* Snap dates and times to indexes
	 * -------------------------------------------------- */

	/**
	 * Retrieve the index of the event closest to a given time that is before
	 * that time.
	 * 
	 * If two or more events have the same time, then the greatest index among
	 * them is returned. Subtypes may decide what minor orderings are present
	 * (if any are needed), though these orderings must produce consistent
	 * results for any call of indexAfter() with the same state of the map. For
	 * example, where the horizontal axis represents time and the <code>V</code>
	 * represents the LocalDateTime argument, the following would return the
	 * index of event 11 (where the downward-increasing vertical axis is used as
	 * a minor ordering):
	 * <pre>
	 * ----------------------------------------
	 *                     V
	 * 0  2  3  5  7  9  10 12 14 16
	 *   1   4   6   8   11  13  15  17  18  19
	 * ----------------------------------------
	 * </pre>
	 *
	 * If there is an index at the given time, returns the index *before* the
	 * given time. For example, the following would return the index of event 0:
	 * <pre>
	 * ----------------------------------------
	 *   V
	 * 0
	 *   1 2
	 * ----------------------------------------
	 * </pre>
	 * 
	 * <p><strong>Note</strong>: Presuming that there exists an index both
	 * before and after the given time, the indexes returned by indexAfter(time)
	 * and indexBefore(time) may be non-contiguous. To get indexes at a given
	 * time, use indexesAt().</p>
	 * 
	 * If there are no indexes before the given time, returns null. For example,
	 * the following would return null:
	 * <pre>
	 * ----------------------------------------
	 * V
	 *  1
	 *    2
	 * ----------------------------------------
	 * </pre>
	 *
	 * This also applies if all maps are empty (ie. there are no indexes, so no
	 * indexes before).
	 * 
	 * @param time The time to get the index of the first event before.
	 * @return The index of the first event before the given time, or null.
	 */
	public I indexBefore(LocalDateTime time);
	
	/**
	 * Retrieve a list of indexes of all events at the given time, sorted by any
	 * minor ordering in place.
	 * 
	 * @param time Time at which to select indexes.
	 * @return A list of indexes of events at the given time.
	 */
	public List<I> indexesAt(LocalDateTime time);
	
	/**
	 * Retrieve the index of the event closest to a given time that is after
	 * that time.
	 * 
	 * If two or more events have the same time, then the least index among them
	 * is returned. Subtypes may decide what minor orderings are present (if any
	 * are needed), though these orderings must produce consistent results for
	 * any call of indexAfter() with the same state of the map. For example,
	 * where the horizontal axis represents time and the <code>V</code>
	 * represents the LocalDateTime argument, the following would return the
	 * index of event 12 (where the downward-increasing vertical axis is used as
	 * a minor ordering):
	 * <pre>
	 * ----------------------------------------
	 *                        V
	 * 0: 0  2  3  5  7  9  10 12 14 16
	 * 1:  1   4   6   8   11  13  15  17  18  19
	 * ----------------------------------------
	 * </pre>
	 *
	 * If there is an index at the given time, returns the index *after* the
	 * given time. For example, the following would return the index of event 2:
	 * <pre>
	 * ----------------------------------------
	 *   V
	 * 0
	 *   1 2
	 * ----------------------------------------
	 * </pre>
	 *
	 * <p><strong>Note</strong>: Presuming that there exists an index both
	 * before and after the given time, the indexes returned by indexAfter(time)
	 * and indexBefore(time) may be non-contiguous. To get indexes at a given
	 * time, use indexesAt().</p>
	 * 
	 * If there are no indexes after the given time, returns null. For example,
	 * the following would return null:
	 * <pre>
	 * ----------------------------------------
	 *    V
	 * 1
	 *   2
	 * ----------------------------------------
	 * </pre>
	 *
	 * This also applies if all maps are empty (ie. there are no indexes, so no
	 * indexes after).
	 * 
	 * @param time The time to get the index of the first event after.
	 * @return The index of the first event after the given time, or null.
	 */
	public I indexAfter(LocalDateTime time);

	/* Retrieve items by index
	 * -------------------------------------------------- */

	/**
	 * Retrieve the event at the given index.
	 * 
	 * @param index The non-null index at which to retrieve the event.
	 * @return The event at the given index.
	 */
	public T get(I index);

	/**
	 * Retrieve all events before the given index.
	 * 
	 * Applies the same logic as indexBefore() regarding what counts as
	 * 'before'. Returns an empty list if no events are before the given index.
	 * 
	 * @param index The non-null index from which to retrieve events, exclusive.
	 * @param index A non-null index.
	 * @return A list of all events before the given index.
	 */
	public List<T> getBefore(I index);

	/**
	 * Retrieve all events after the given index.
	 * 
	 * Applies the same logic as indexAfter() regarding what counts as 'after'.
	 * Returns an empty list if no events are after the given index.
	 * 
	 * @param index The non-null index up to which to retrieve events,
	 * exclusive.
	 * @return A list of all events after the given index.
	 */
	public List<T> getAfter(I index);

	/**
	 * Retrieve a list of all events between the two given indexes.
	 * 
	 * Returns an empty list if the start index equals the end index or the end
	 * index is before the start index.
	 * 
	 * @param start The non-null index from which to retrieve events,
	 * exclusive.
	 * @param end The non-null index up to which to retrieve events, exclusive.
	 * @return A list of all events between the start and end index.
	 */
	public List<T> getBetween(I start, I end);

	/* Retrieve items by time
	 * -------------------------------------------------- */

	/**
	 * Retrieve all events before the given time.
	 * 
	 * Applies the same logic as indexBefore() regarding what counts as
	 * 'before'. Returns an empty list if no events are before the given time.
	 * 
	 * @param time The time up to which to retrieve events, exclusive.
	 * @return A list of all events before the given time.
	 */
	public List<T> getBefore(LocalDateTime time);

	/**
	 * Retrieve all events after the given time.
	 * 
	 * Applies the same logic as indexAfter() regarding what counts as 'after'.
	 * Returns an empty list if no events are after the given time.
	 * 
	 * @param time The time from which to retrieve events, exclusive.
	 * @return A list of all events after the given time.
	 */
	public List<T> getAfter(LocalDateTime time);

	/**
	 * Retrieve a list of all events between the two given times.
	 * 
	 * Returns an empty list if the start time equals the end time or the end
	 * time is before the start time.
	 * 
	 * Assumes start and end times are not null.
	 * 
	 * @param start The time from which to retrieve events, exclusive.
	 * @param end The time up to which to retrieve events, exclusive.
	 * @return
	 */
	public List<T> getBetween(LocalDateTime start, LocalDateTime end);
}

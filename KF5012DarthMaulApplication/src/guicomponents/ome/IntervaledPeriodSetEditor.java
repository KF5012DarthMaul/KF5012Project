package guicomponents.ome;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import guicomponents.utils.ObjectEditor;
import guicomponents.utils.ObjectManager;
import temporal.IntervaledPeriodSet;
import temporal.Period;

// Extending JComponent is just evil here, but hell ...
/**
 * An editor of IntervaledPeriodSets that does not dictate component positions.
 * 
 * Is a JComponent purely to conform to the expected interface of
 * getComponent(), and overrides some methods to make things that expect a
 * JComponent function correctly.
 * 
 * @author William Taylor
 */
@SuppressWarnings("serial")
public class IntervaledPeriodSetEditor
		implements ObjectEditor<IntervaledPeriodSet>
{
	private LocalDateTimeEditor ldteRefStart;
	private LocalDateTimeEditor ldteRefEnd;
	private DurationEditor dureInterval;
	private ObjectManager<LocalDateTime> refEndManager;
	private ObjectManager<Duration> intervalManager;
	
	public IntervaledPeriodSetEditor(
		LocalDateTimeEditor ldteRefStart,
		LocalDateTimeEditor ldteRefEnd,
		DurationEditor dureInterval,
		
		ObjectManager<LocalDateTime> refEndManager,
		ObjectManager<Duration> intervalManager
	) {
		this.ldteRefStart = ldteRefStart;
		this.ldteRefEnd = ldteRefEnd;
		this.dureInterval = dureInterval;
		
		this.refEndManager = refEndManager;
		this.intervalManager = intervalManager;
		
		this.ldteRefEnd.setValidator((ldt) -> {
			// Verify is either null, or is not before the start
			LocalDateTime setRefEnd = this.refEndManager.getObject();
			return (
				setRefEnd == null ||
				!setRefEnd.isBefore(this.ldteRefStart.getObject())
			);
		});
	}
	
	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(ldteRefStart);
		arr.add(ldteRefEnd);
		arr.add(dureInterval);
		return arr;
	}

	@Override
	public void setObject(IntervaledPeriodSet obj) {
		ldteRefStart.setObject(obj.referencePeriod().start());
		refEndManager.setObject(obj.referencePeriod().end());
		intervalManager.setObject(obj.interval());
	}

	@Override
	public boolean validateFields() {
		return (
			ldteRefStart.validateFields() &&
			refEndManager.getEditor().validateFields() &&
			intervalManager.getEditor().validateFields()
		);
	}

	@Override
	public IntervaledPeriodSet getObject() {
		LocalDateTime refStart = ldteRefStart.getObject();
		LocalDateTime refEnd = refEndManager.getObject();
		Duration interval = intervalManager.getObject();

		// Constructing a new ConstrainedIntervaledPeriodSet isn't that
		// problematic memory-wise, and is less complicated than checking to see
		// if it's changed.
		return new IntervaledPeriodSet(new Period(refStart, refEnd), interval);
	}
}

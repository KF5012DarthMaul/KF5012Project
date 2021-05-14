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
public class IntervaledPeriodSetEditor
		implements ObjectEditor<IntervaledPeriodSet>
{
	private LocalDateTimeEditor ldteRefStart;
	private ObjectManager<LocalDateTime> refEndManager;
	private ObjectManager<Duration> intervalManager;
	
	public IntervaledPeriodSetEditor(
		LocalDateTimeEditor ldteRefStart,
		ObjectManager<LocalDateTime> refEndManager,
		ObjectManager<Duration> intervalManager,
		
		LocalDateTimeEditor ldteRefEnd
	) {
		this.ldteRefStart = ldteRefStart;
		this.refEndManager = refEndManager;
		this.intervalManager = intervalManager;

		// Requires ldteRefEnd to set the validator, but don't need to store it
		// Verify is not before the start (use refEndManager.validateFields())
		ldteRefEnd.setValidator((ldt) -> {
			return !refEndManager.getObject().isBefore(ldteRefStart.getObject());
		});
	}
	
	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		
		arr.add(ldteRefStart);
		arr.addAll(refEndManager.getEditorComponents());
		arr.addAll(intervalManager.getEditorComponents());
		
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
			refEndManager.validateFields() &&
			intervalManager.validateFields()
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

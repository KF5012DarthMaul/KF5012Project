package temporal;

public class ConstrainedIntervaledPeriodSet {
	private final IntervaledPeriodSet periodSet;
	private final IntervaledPeriodSet periodSetConstraint; // Nullable
	
	public ConstrainedIntervaledPeriodSet(
			IntervaledPeriodSet periodSet,
			IntervaledPeriodSet periodSetConstraint
	) {
		this.periodSet = periodSet;
		this.periodSetConstraint = periodSetConstraint;
	}
	public ConstrainedIntervaledPeriodSet(IntervaledPeriodSet periodSet) {
		this(periodSet, null);
	}
	
	public IntervaledPeriodSet periodSet() {
		return this.periodSet;
	}
	public IntervaledPeriodSet periodSetConstraint() {
		return this.periodSetConstraint;
	}
}

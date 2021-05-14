package guicomponents.ome;

import domain.TaskPriority;
import domain.Verification;
import kf5012darthmaulapplication.User;

import java.awt.Insets;
import java.time.Duration;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import guicomponents.utils.ObjectEditor;
import guicomponents.utils.ObjectManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class VerificationEditor
	extends JPanel
	implements ObjectEditor<Verification>
{
	private Verification active;
	
	// Basic fields
	private LongTextEditor txteNotes;
	private ListSelectionEditor<TaskPriority> lsteStandardPriority;
	private ListSelectionEditor<User> lsteAllocationConstraint;
	private ObjectManager<Duration> durationManager;

	private JCheckBox chkStandardDeadline;
	
	/**
	 * Create the panel.
	 */
	public VerificationEditor() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		// Notes
		
		JLabel lblNotes = new JLabel("General Verification Notes");
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.anchor = GridBagConstraints.EAST;
		gbc_lblNotes.insets = new Insets(0, 5, 5, 5);
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 0;
		add(lblNotes, gbc_lblNotes);
		
		txteNotes = new LongTextEditor();
		GridBagConstraints gbc_txteNotes = new GridBagConstraints();
		gbc_txteNotes.insets = new Insets(0, 0, 5, 0);
		gbc_txteNotes.fill = GridBagConstraints.HORIZONTAL;
		gbc_txteNotes.gridwidth = 2;
		gbc_txteNotes.gridx = 1;
		gbc_txteNotes.gridy = 0;
		add(txteNotes, gbc_txteNotes);
		
		// Standard Priority
		
		JLabel lblStandardPriority = new JLabel("Standard Verification Priority");
		GridBagConstraints gbc_lblStandardPriority = new GridBagConstraints();
		gbc_lblStandardPriority.anchor = GridBagConstraints.EAST;
		gbc_lblStandardPriority.insets = new Insets(0, 5, 5, 5);
		gbc_lblStandardPriority.gridx = 0;
		gbc_lblStandardPriority.gridy = 1;
		add(lblStandardPriority, gbc_lblStandardPriority);
		
		lsteStandardPriority = new ListSelectionEditor<>(
			(taskPriority) -> taskPriority.toString()
		);
		lsteStandardPriority.populate(Arrays.asList(TaskPriority.values()));
		GridBagConstraints gbc_lsteStandardPriority = new GridBagConstraints();
		gbc_lsteStandardPriority.insets = new Insets(0, 0, 5, 0);
		gbc_lsteStandardPriority.anchor = GridBagConstraints.WEST;
		gbc_lsteStandardPriority.gridwidth = 2;
		gbc_lsteStandardPriority.gridx = 1;
		gbc_lsteStandardPriority.gridy = 1;
		add(lsteStandardPriority, gbc_lsteStandardPriority);
		
		// Allocation constraint

		JLabel lblAllocationConstraint = new JLabel("Allocation Constraint");
		GridBagConstraints gbc_lblAllocationConstraint = new GridBagConstraints();
		gbc_lblAllocationConstraint.anchor = GridBagConstraints.EAST;
		gbc_lblAllocationConstraint.insets = new Insets(0, 5, 5, 5);
		gbc_lblAllocationConstraint.gridx = 0;
		gbc_lblAllocationConstraint.gridy = 2;
		add(lblAllocationConstraint, gbc_lblAllocationConstraint);

		lsteAllocationConstraint = new ListSelectionEditor<>(
			(user) -> {
				if (user == null) {
					return "No Allocation Constraint";
				} else {
					return user.getUsername();
				}
			}
		);
		GridBagConstraints gbc_lsteAllocationConstraint = new GridBagConstraints();
		gbc_lsteAllocationConstraint.insets = new Insets(0, 0, 5, 0);
		gbc_lsteAllocationConstraint.anchor = GridBagConstraints.WEST;
		gbc_lsteAllocationConstraint.gridwidth = 2;
		gbc_lsteAllocationConstraint.gridx = 1;
		gbc_lsteAllocationConstraint.gridy = 2;
		add(lsteAllocationConstraint, gbc_lsteAllocationConstraint);
		
		// Standard deadline

		JLabel lblStandardDeadline = new JLabel("Standard Verification Deadline");
		GridBagConstraints gbc_lblStandardDeadline = new GridBagConstraints();
		gbc_lblStandardDeadline.insets = new Insets(0, 5, 5, 5);
		gbc_lblStandardDeadline.anchor = GridBagConstraints.EAST;
		gbc_lblStandardDeadline.gridx = 0;
		gbc_lblStandardDeadline.gridy = 3;
		add(lblStandardDeadline, gbc_lblStandardDeadline);

		chkStandardDeadline = new JCheckBox("");
		GridBagConstraints gbc_chkStandardDeadline = new GridBagConstraints();
		gbc_chkStandardDeadline.insets = new Insets(0, 0, 5, 5);
		gbc_chkStandardDeadline.anchor = GridBagConstraints.WEST;
		gbc_chkStandardDeadline.gridx = 1;
		gbc_chkStandardDeadline.gridy = 3;
		add(chkStandardDeadline, gbc_chkStandardDeadline);
		
		DurationEditor dureStandardDeadline = new DurationEditor();
		GridBagConstraints gbc_dureStandardDeadline = new GridBagConstraints();
		gbc_dureStandardDeadline.insets = new Insets(0, 0, 5, 5);
		gbc_dureStandardDeadline.anchor = GridBagConstraints.WEST;
		gbc_dureStandardDeadline.gridx = 2;
		gbc_dureStandardDeadline.gridy = 3;
		add(dureStandardDeadline, gbc_dureStandardDeadline);
		
		durationManager = new ObjectManager<>(
			chkStandardDeadline, dureStandardDeadline,
			() -> dureStandardDeadline.getObject()
		);
	}

	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(this);
		return arr;
	}
	
	public void setUsers(List<User> users) {
		lsteAllocationConstraint.populate(users);
	}

	@Override
	public void setObject(Verification obj) {
		active = obj;
		
		// Basic Fields
		txteNotes.setObject(obj.getNotes());
		lsteStandardPriority.setObject(obj.getStandardPriority());
		lsteAllocationConstraint.setObject(obj.getAllocationConstraint());
		durationManager.setObject(obj.getStandardDeadline());
	}

	@Override
	public boolean validateFields() {
		boolean valid = true;
		
		if (!txteNotes.validateFields()) valid = false;
		if (!lsteStandardPriority.validateFields()) valid = false;
		if (!lsteAllocationConstraint.validateFields()) valid = false;
		if (!durationManager.getEditor().validateFields()) valid = false;
		
		return valid;
	}

	@Override
	public Verification getObject() {
		active.setNotes(txteNotes.getObject());
		active.setStandardPriority(lsteStandardPriority.getObject());
		active.setAllocationConstraint(lsteAllocationConstraint.getObject());
		active.setStandardDeadline(durationManager.getObject());
		
		return active;
	}
}

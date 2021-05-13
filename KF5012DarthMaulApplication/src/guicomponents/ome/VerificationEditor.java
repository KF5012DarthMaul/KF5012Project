package guicomponents.ome;

import domain.TaskPriority;
import domain.Verification;
import kf5012darthmaulapplication.User;

import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import guicomponents.utils.NullableComboBox;
import guicomponents.utils.ObjectEditor;
import lib.DurationField;

import java.time.Duration;
import java.util.List;

@SuppressWarnings("serial")
public class VerificationEditor
	extends JPanel
	implements ObjectEditor<Verification>
{
	private JTextArea txtNotes;
	private JComboBox<Object> cmbStandardPriority;
	private NullableComboBox<User> cmbAllocationConstraint;
	private DurationField durStandardDeadline;

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
		
		txtNotes = new JTextArea();
		GridBagConstraints gbc_txtNotes = new GridBagConstraints();
		gbc_txtNotes.insets = new Insets(0, 0, 5, 0);
		gbc_txtNotes.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtNotes.gridwidth = 2;
		gbc_txtNotes.gridx = 1;
		gbc_txtNotes.gridy = 0;
		add(txtNotes, gbc_txtNotes);
		
		// Standard Priority
		
		JLabel lblStandardPriority = new JLabel("Standard Verification Priority");
		GridBagConstraints gbc_lblStandardPriority = new GridBagConstraints();
		gbc_lblStandardPriority.anchor = GridBagConstraints.EAST;
		gbc_lblStandardPriority.insets = new Insets(0, 5, 5, 5);
		gbc_lblStandardPriority.gridx = 0;
		gbc_lblStandardPriority.gridy = 1;
		add(lblStandardPriority, gbc_lblStandardPriority);
		
		cmbStandardPriority = new JComboBox<>(TaskPriority.values());
		GridBagConstraints gbc_cmbStandardPriority = new GridBagConstraints();
		gbc_cmbStandardPriority.insets = new Insets(0, 0, 5, 0);
		gbc_cmbStandardPriority.anchor = GridBagConstraints.WEST;
		gbc_cmbStandardPriority.gridwidth = 2;
		gbc_cmbStandardPriority.gridx = 1;
		gbc_cmbStandardPriority.gridy = 1;
		add(cmbStandardPriority, gbc_cmbStandardPriority);
		
		// Allocation constraint

		JLabel lblAllocationConstraint = new JLabel("Allocation Constraint");
		GridBagConstraints gbc_lblAllocationConstraint = new GridBagConstraints();
		gbc_lblAllocationConstraint.anchor = GridBagConstraints.EAST;
		gbc_lblAllocationConstraint.insets = new Insets(0, 5, 5, 5);
		gbc_lblAllocationConstraint.gridx = 0;
		gbc_lblAllocationConstraint.gridy = 2;
		add(lblAllocationConstraint, gbc_lblAllocationConstraint);

		cmbAllocationConstraint = new NullableComboBox<>((user) -> {
			return user == null ? "No Allocation Constraint" : user.getUsername();
		});
		GridBagConstraints gbc_cmbAllocationConstraint = new GridBagConstraints();
		gbc_cmbAllocationConstraint.insets = new Insets(0, 0, 5, 0);
		gbc_cmbAllocationConstraint.anchor = GridBagConstraints.WEST;
		gbc_cmbAllocationConstraint.gridwidth = 2;
		gbc_cmbAllocationConstraint.gridx = 1;
		gbc_cmbAllocationConstraint.gridy = 2;
		add(cmbAllocationConstraint, gbc_cmbAllocationConstraint);
		
		// Standard deadline

		JLabel lblStandardDeadline = new JLabel("Standard Verification Deadline");
		GridBagConstraints gbc_lblStandardDeadline = new GridBagConstraints();
		gbc_lblStandardDeadline.insets = new Insets(0, 5, 5, 5);
		gbc_lblStandardDeadline.anchor = GridBagConstraints.EAST;
		gbc_lblStandardDeadline.gridx = 0;
		gbc_lblStandardDeadline.gridy = 3;
		add(lblStandardDeadline, gbc_lblStandardDeadline);

		chkStandardDeadline = new JCheckBox("");
		chkStandardDeadline.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				standardDeadlineEnabled(true, false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				standardDeadlineEnabled(false, false);
			}
		});
		GridBagConstraints gbc_chkStandardDeadline = new GridBagConstraints();
		gbc_chkStandardDeadline.insets = new Insets(0, 0, 5, 5);
		gbc_chkStandardDeadline.anchor = GridBagConstraints.WEST;
		gbc_chkStandardDeadline.gridx = 1;
		gbc_chkStandardDeadline.gridy = 3;
		add(chkStandardDeadline, gbc_chkStandardDeadline);
		
		durStandardDeadline = new DurationField();
		GridBagConstraints gbc_durStandardDeadline = new GridBagConstraints();
		gbc_durStandardDeadline.insets = new Insets(0, 0, 5, 5);
		gbc_durStandardDeadline.anchor = GridBagConstraints.WEST;
		gbc_durStandardDeadline.gridx = 2;
		gbc_durStandardDeadline.gridy = 3;
		add(durStandardDeadline, gbc_durStandardDeadline);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}
	
	public void setUsers(List<User> users) {
		cmbAllocationConstraint.populate(users);
	}

	private void standardDeadlineEnabled(boolean enabled, boolean force) {
		if (force) {
			chkStandardDeadline.setSelected(enabled);
		}
		durStandardDeadline.setVisible(enabled);
	}
	
	@Override
	public void showObject(Verification obj) {
		txtNotes.setText(obj.getNotes());
		cmbStandardPriority.setSelectedItem(obj.getStandardPriority());
		cmbAllocationConstraint.setSelection(obj.getAllocationConstraint());

		Duration standardDeadline = obj.getStandardDeadline();
		if (standardDeadline == null) {
			standardDeadlineEnabled(false, true);
		} else {
			standardDeadlineEnabled(true, true);
			durStandardDeadline.setHour((int) standardDeadline.getSeconds() / 3600);
			durStandardDeadline.setMinute((int) standardDeadline.getSeconds() % 3600 / 60);
		}
	}

	@Override
	public boolean validateFields() {
		boolean valid = true;
		
		// Notes - no validation required
		// Standard Priority - combo box does validation
		// Allocation Constraint - combo box does validation
		// Standard Deadline - duration field does validation
		
		return valid;
	}

	@Override
	public void updateObject(Verification obj) {
		obj.setNotes(txtNotes.getText());
		obj.setStandardPriority((TaskPriority) cmbStandardPriority.getSelectedItem());
		obj.setAllocationConstraint(cmbAllocationConstraint.getSelection());
		obj.setStandardDeadline(Duration.ofSeconds(durStandardDeadline.getDuration()));
	}
}

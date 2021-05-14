package guicomponents.ome;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import domain.VerificationExecution;
import guicomponents.utils.ObjectEditor;

@SuppressWarnings("serial")
public class VerificationExecutionEditor
		extends JPanel
		implements ObjectEditor<VerificationExecution>
{
	private VerificationExecution active;
	
	// Basic fields
	private LongTextEditor txteNotes;
	private DurationEditor dureStandardDeadline;
	
	/**
	 * Create the panel.
	 */
	public VerificationExecutionEditor() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		// Notes
		
		JLabel lblNotes = new JLabel("Verification Notes");
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.anchor = GridBagConstraints.EAST;
		gbc_lblNotes.insets = new Insets(0, 5, 5, 5);
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 0;
		add(lblNotes, gbc_lblNotes);
		
		txteNotes = new LongTextEditor();
		GridBagConstraints gbc_txteNotes = new GridBagConstraints();
		gbc_txteNotes.insets = new Insets(0, 5, 5, 5);
		gbc_txteNotes.anchor = GridBagConstraints.WEST;
		gbc_txteNotes.gridx = 1;
		gbc_txteNotes.gridy = 0;
		add(txteNotes, gbc_txteNotes);
		txteNotes.setColumns(40);
		txteNotes.setRows(6);

		// Standard deadline

		JLabel lblStandardDeadline = new JLabel("Verification Deadline");
		GridBagConstraints gbc_lblStandardDeadline = new GridBagConstraints();
		gbc_lblStandardDeadline.insets = new Insets(0, 5, 5, 5);
		gbc_lblStandardDeadline.anchor = GridBagConstraints.EAST;
		gbc_lblStandardDeadline.gridx = 0;
		gbc_lblStandardDeadline.gridy = 1;
		add(lblStandardDeadline, gbc_lblStandardDeadline);

		dureStandardDeadline = new DurationEditor();
		GridBagConstraints gbc_dureStandardDeadline = new GridBagConstraints();
		gbc_dureStandardDeadline.insets = new Insets(0, 5, 5, 5);
		gbc_dureStandardDeadline.anchor = GridBagConstraints.WEST;
		gbc_dureStandardDeadline.gridx = 1;
		gbc_dureStandardDeadline.gridy = 1;
		add(dureStandardDeadline, gbc_dureStandardDeadline);
	}
	
	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(this);
		return arr;
	}
	
	@Override
	public void setObject(VerificationExecution obj) {
		active = obj;
		
		txteNotes.setObject(obj.getNotes());
		dureStandardDeadline.setObject(obj.getDeadline());
	}

	@Override
	public boolean validateFields() {
		boolean valid = true;
		
		if (!txteNotes.validateFields()) valid = false;
		if (!dureStandardDeadline.validateFields()) valid = false;
		
		return valid;
	}

	@Override
	public VerificationExecution getObject() {
		active.setNotes(txteNotes.getObject());
		active.setDeadline(dureStandardDeadline.getObject());
		
		return active;
	}
}

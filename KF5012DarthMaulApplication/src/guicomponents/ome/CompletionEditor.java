package guicomponents.ome;

import domain.Completion;
import domain.TaskCompletionQuality;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import guicomponents.utils.ObjectEditor;
import java.util.Arrays;
import kf5012darthmaulapplication.User;

@SuppressWarnings("serial")
public class CompletionEditor
		extends JPanel
		implements ObjectEditor<Completion>
{
	private Completion active;
	
	// Basic fields
        private final ListSelectionEditor<User> lsteUser;
	private final LocalDateTimeEditor ldteStartTime;
        private final LocalDateTimeEditor ldteCompletionTime;
        private final ListSelectionEditor<TaskCompletionQuality> lsteQuality;
        private final LongTextEditor txteNotes;
	
	/**
	 * Create the panel.
	 */
	public CompletionEditor() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

                // User List Selection
                
                JLabel lblUserSelection = new JLabel("Caretaker that completed task");
		GridBagConstraints gbc_lblUserSelection = new GridBagConstraints();
		gbc_lblUserSelection.anchor = GridBagConstraints.EAST;
		gbc_lblUserSelection.insets = new Insets(5, 5, 5, 5);
		gbc_lblUserSelection.gridx = 0;
		gbc_lblUserSelection.gridy = 0;
		add(lblUserSelection, gbc_lblUserSelection);

                lsteUser = new ListSelectionEditor<>((user) -> { return user.getDisplayName();});
                
		GridBagConstraints gbc_lsteUserSelection = new GridBagConstraints();
		gbc_lsteUserSelection.insets = new Insets(5, 5, 5, 5);
		gbc_lsteUserSelection.anchor = GridBagConstraints.WEST;
		gbc_lsteUserSelection.gridwidth = 2;
		gbc_lsteUserSelection.gridx = 1;
		gbc_lsteUserSelection.gridy = 0;
		add(lsteUser, gbc_lsteUserSelection);

		// Start time of task/verification execution

		JLabel lblStartTime = new JLabel("Start time of task");
		GridBagConstraints gbc_lblStartTime = new GridBagConstraints();
		gbc_lblStartTime.insets = new Insets(0, 5, 5, 5);
		gbc_lblStartTime.anchor = GridBagConstraints.EAST;
		gbc_lblStartTime.gridx = 0;
		gbc_lblStartTime.gridy = 1;
		add(lblStartTime, gbc_lblStartTime);

		ldteStartTime = new LocalDateTimeEditor();
		GridBagConstraints gbc_ldteStartTime = new GridBagConstraints();
		gbc_ldteStartTime.insets = new Insets(0, 5, 5, 5);
		gbc_ldteStartTime.anchor = GridBagConstraints.WEST;
		gbc_ldteStartTime.gridx = 1;
		gbc_ldteStartTime.gridy = 1;
		add(ldteStartTime, gbc_ldteStartTime);
                
                // Completion time of task/verification execution

		JLabel lblCompletionTime = new JLabel("Completion time of task");
		GridBagConstraints gbc_lblCompletionTime = new GridBagConstraints();
		gbc_lblCompletionTime.insets = new Insets(0, 5, 5, 5);
		gbc_lblCompletionTime.anchor = GridBagConstraints.EAST;
		gbc_lblCompletionTime.gridx = 0;
		gbc_lblCompletionTime.gridy = 2;
		add(lblCompletionTime, gbc_lblCompletionTime);

		ldteCompletionTime = new LocalDateTimeEditor();
		GridBagConstraints gbc_ldteCompletion = new GridBagConstraints();
		gbc_ldteCompletion.insets = new Insets(0, 5, 5, 5);
		gbc_ldteCompletion.anchor = GridBagConstraints.WEST;
		gbc_ldteCompletion.gridx = 1;
		gbc_ldteCompletion.gridy = 2;
		add(ldteCompletionTime, gbc_ldteCompletion);
                
                // Quality of work
                
                JLabel lblQuality = new JLabel("Quality of work");
		GridBagConstraints gbc_lblQuality = new GridBagConstraints();
		gbc_lblQuality.anchor = GridBagConstraints.EAST;
		gbc_lblQuality.insets = new Insets(0, 5, 5, 5);
		gbc_lblQuality.gridx = 0;
		gbc_lblQuality.gridy = 3;
		add(lblQuality, gbc_lblQuality);
		
		lsteQuality = new ListSelectionEditor<>(
			(taskPriority) -> taskPriority.toString()
		);
		lsteQuality.populate(Arrays.asList(TaskCompletionQuality.values()));
		GridBagConstraints gbc_lsteQuality = new GridBagConstraints();
		gbc_lsteQuality.anchor = GridBagConstraints.WEST;
		gbc_lsteQuality.insets = new Insets(0, 5, 5, 5);
		gbc_lsteQuality.gridwidth = 2;
		gbc_lsteQuality.gridx = 1;
		gbc_lsteQuality.gridy = 3;
		add(lsteQuality, gbc_lsteQuality);
                
                // Notes
		
		JLabel lblNotes = new JLabel("Completion Notes");
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.anchor = GridBagConstraints.EAST;
		gbc_lblNotes.insets = new Insets(0, 5, 5, 5);
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 4;
		add(lblNotes, gbc_lblNotes);
		
		txteNotes = new LongTextEditor();
		GridBagConstraints gbc_txteNotes = new GridBagConstraints();
		gbc_txteNotes.insets = new Insets(0, 5, 5, 5);
		gbc_txteNotes.anchor = GridBagConstraints.WEST;
		gbc_txteNotes.gridx = 1;
		gbc_txteNotes.gridy = 4;
		add(txteNotes, gbc_txteNotes);
		txteNotes.setColumns(40);
		txteNotes.setRows(6);
	}
	
	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(this);
		return arr;
	}
	
        public void setUsers(List<User> users) {
            
		lsteUser.populate(users);
	}

        
	@Override
	public void setObject(Completion obj) {
            active = obj;
            
            lsteUser.setObject(obj.getStaff());
            ldteStartTime.setObject(obj.getStartTime());
            ldteCompletionTime.setObject(obj.getCompletionTime());
            lsteQuality.setObject(obj.getWorkQuality());
            txteNotes.setObject(obj.getNotes());
	}

	@Override
	public boolean validateFields() {
		boolean valid = true;
                
		if (!lsteUser.validateFields()) valid = false;
		if (!ldteStartTime.validateFields()) valid = false;
		if (!ldteCompletionTime.validateFields()) valid = false;
                if (!lsteQuality.validateFields()) valid = false;
                if (!txteNotes.validateFields()) valid = false;
                
		return valid;
	}

	@Override
	public Completion getObject() {
		active.setNotes(txteNotes.getObject());
		active.setStartTime(ldteStartTime.getObject());
                active.setCompletionTime(ldteCompletionTime.getObject());
                active.setStaff(lsteUser.getObject());
                active.setWorkQuality(lsteQuality.getObject());
		
		return active;
	}
}

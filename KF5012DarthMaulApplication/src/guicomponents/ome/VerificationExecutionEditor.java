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

import domain.VerificationExecution;
import guicomponents.utils.ObjectEditor;
import guicomponents.utils.ObjectManager;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import kf5012darthmaulapplication.User;

@SuppressWarnings("serial")
public class VerificationExecutionEditor
		extends JPanel
		implements ObjectEditor<VerificationExecution>
{
	private VerificationExecution active;
	
	// Basic fields
	private LongTextEditor txteNotes;
	private DurationEditor dureStandardDeadline;
	
        //Completion editor
        private CompletionEditor edtCompletion;
        private DomainObjectManager<Completion> omgCompletion;
        private ObjectManager<Duration> standardDeadlineManager;
	/**
	 * Create the panel.
	 */
	public VerificationExecutionEditor() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
                gbc_txteNotes.gridwidth = 2;
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
                
                JCheckBox chkStandardDeadline = new JCheckBox("");
		GridBagConstraints gbc_chkStandardDeadline = new GridBagConstraints();
		gbc_chkStandardDeadline.insets = new Insets(0, 0, 5, 5);
		gbc_chkStandardDeadline.anchor = GridBagConstraints.WEST;
		gbc_chkStandardDeadline.gridx = 1;
		gbc_chkStandardDeadline.gridy = 1;
		add(chkStandardDeadline, gbc_chkStandardDeadline);
                
		dureStandardDeadline = new DurationEditor();
		GridBagConstraints gbc_dureStandardDeadline = new GridBagConstraints();
		gbc_dureStandardDeadline.insets = new Insets(0, 0, 5, 5);
		gbc_dureStandardDeadline.anchor = GridBagConstraints.WEST;
		gbc_dureStandardDeadline.gridx = 2;
		gbc_dureStandardDeadline.gridy = 1;
		add(dureStandardDeadline, gbc_dureStandardDeadline);
                
                /* Completion Editor
		 * -------------------- */
                JSeparator sep2 = new JSeparator();
                GridBagConstraints gbc_sep2 = new GridBagConstraints();
                gbc_sep2.fill = GridBagConstraints.HORIZONTAL;
                gbc_sep2.insets = new Insets(0, 5, 5, 5);
                gbc_sep2.gridwidth = 3;
                gbc_sep2.gridx = 0;
                gbc_sep2.gridy = 2;
                add(sep2, gbc_sep2);

                edtCompletion = new CompletionEditor();

                omgCompletion = new DomainObjectManager<>(
                        "Has been verified", edtCompletion,

                        // Create a new one each time the checkbox is re-ticked
                        () -> new Completion(
                                null, // No ID
                                active.getAllocation(),
                                active.getPeriod().start(),
                                LocalDateTime.now(),
                                TaskCompletionQuality.GOOD,
                                "" // No notes
                        )
                );

                GridBagConstraints gbc_compPanel = new GridBagConstraints();
                gbc_compPanel.insets = new Insets(5, 5, 5, 5);
                gbc_compPanel.anchor = GridBagConstraints.WEST;
                gbc_compPanel.gridwidth = 3;
                gbc_compPanel.gridx = 0;
                gbc_compPanel.gridy = 3;
                add(omgCompletion, gbc_compPanel);
                
                standardDeadlineManager = new ObjectManager<>(
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
	
	@Override
	public void setObject(VerificationExecution obj) {
		active = obj;
		
		txteNotes.setObject(obj.getNotes());
		standardDeadlineManager.setObject(obj.getDeadline());
                omgCompletion.getObjectManager().setObject(obj.getCompletion());
	}

	@Override
	public boolean validateFields() {
		boolean valid = true;
		
		if (!txteNotes.validateFields()) valid = false;
		if (!dureStandardDeadline.validateFields()) valid = false;
		if (!omgCompletion.getObjectManager().validateFields()) valid = false;
                
		return valid;
	}

	@Override
	public VerificationExecution getObject() {
		active.setNotes(txteNotes.getObject());
		active.setDeadline(dureStandardDeadline.getObject());
		active.setCompletion(omgCompletion.getObjectManager().getObject());
                
		return active;
	}
        
                /* Allocation combo box management
	 * -------------------------------------------------- */
	// Loading of users for various components
	private boolean usersLoaded = false;
	/**
	 * Load users into the allocation constraint combo box and all other
	 * components that require them.
	 * 
	 * @param reload If users are cached, whether to re-fetch users regardless.
	 */
	public void loadUsers(List<User> users) {
            edtCompletion.setUsers(users);
	}
        
}

package guicomponents;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions;
import domain.TaskPriority;
import domain.Task;
import domain.TaskExecution;
import domain.Verification;
import domain.VerificationExecution;

import temporal.Event;
import temporal.GenerativeTemporalMap;
import temporal.TemporalMap;
import temporal.Timeline;
import guicomponents.formatters.Formatter;
import guicomponents.formatters.HTMLFormatter;
import guicomponents.formatters.NamedTaskExecutionFormatter;
import guicomponents.ome.LocalDateTimeEditor;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSplitPane;

@SuppressWarnings("serial")
public class ManageAllocation extends JPanel {
	private static final Formatter<TaskExecution> TASK_EXEC_FORMATTER =
			new HTMLFormatter<>(new NamedTaskExecutionFormatter());

	// Generation
	private LocalDateTimeEditor ldteGenEndTime;
	private JButton btnConfirm;
	private JList<Object> previewList;
	
	// Allocation
	private LocalDateTimeEditor ldteAllocEndTime;
	private JList<Object> allocatedList;
	private JList<Object> unallocatedList;
	
	// DB
	private DBAbstraction db;
	
	/**
	 * Create the panel.
	 */
	public ManageAllocation() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		JPanel generationPanel = new JPanel();
		tabbedPane.addTab("Task Generation", null, generationPanel, null);
		GridBagLayout gbl_generationPanel = new GridBagLayout();
		gbl_generationPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_generationPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_generationPanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_generationPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		generationPanel.setLayout(gbl_generationPanel);
		
		JLabel lblGenUntil = new JLabel("Generate from now until:");
		GridBagConstraints gbc_lblGenUntil = new GridBagConstraints();
		gbc_lblGenUntil.anchor = GridBagConstraints.WEST;
		gbc_lblGenUntil.insets = new Insets(5, 5, 5, 5);
		gbc_lblGenUntil.gridx = 0;
		gbc_lblGenUntil.gridy = 0;
		generationPanel.add(lblGenUntil, gbc_lblGenUntil);
		
		ldteGenEndTime = new LocalDateTimeEditor();
		GridBagConstraints gbc_ldteEndTime = new GridBagConstraints();
		gbc_ldteEndTime.anchor = GridBagConstraints.WEST;
		gbc_ldteEndTime.insets = new Insets(5, 5, 5, 5);
		gbc_ldteEndTime.gridx = 1;
		gbc_ldteEndTime.gridy = 0;
		generationPanel.add(ldteGenEndTime, gbc_ldteEndTime);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 2;
		gbc_panel.gridy = 0;
		generationPanel.add(panel, gbc_panel);
		
		JButton btnGenerate = new JButton("Preview");
		btnGenerate.addActionListener((e) -> this.preview());
		panel.add(btnGenerate);
		
		btnConfirm = new JButton("Confirm and Add");
		btnConfirm.addActionListener((e) -> this.confirm());
		btnConfirm.setEnabled(false);
		panel.add(btnConfirm);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(5, 5, 5, 0);
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.gridwidth = 3;
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 1;
		generationPanel.add(separator, gbc_separator);
		
		JScrollPane listScrollPane = new JScrollPane();
		GridBagConstraints gbc_listScrollPane = new GridBagConstraints();
		gbc_listScrollPane.gridwidth = 3;
		gbc_listScrollPane.insets = new Insets(5, 5, 5, 0);
		gbc_listScrollPane.fill = GridBagConstraints.BOTH;
		gbc_listScrollPane.gridx = 0;
		gbc_listScrollPane.gridy = 2;
		generationPanel.add(listScrollPane, gbc_listScrollPane);
		
		previewList = new JList<>(new DefaultListModel<>());
		previewList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(
					JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus
			) {
				// Set stuff related to isSelected and cellHasFocus
				super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
				
				setText(TASK_EXEC_FORMATTER.apply((TaskExecution) value));
				return this;
			}
		});
		listScrollPane.setViewportView(previewList);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 3;
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		generationPanel.add(panel_1, gbc_panel_1);
		
		JButton btnRemove = new JButton("Remove Selected Task Instances");
		btnRemove.addActionListener((e) -> this.removeTaskExec());
		panel_1.add(btnRemove);
		
		JPanel allocationPanel = new JPanel();
		tabbedPane.addTab("Task Allocation", null, allocationPanel, null);
		allocationPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_allocationNorth = new JPanel();
		allocationPanel.add(panel_allocationNorth, BorderLayout.NORTH);
		panel_allocationNorth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lblAllocUntil = new JLabel("Generate from now until:");
		panel_allocationNorth.add(lblAllocUntil);
		
		ldteAllocEndTime = new LocalDateTimeEditor();
		panel_allocationNorth.add(ldteAllocEndTime);
		
		JButton btnShowExecutions = new JButton("Load Tasks");
		panel_allocationNorth.add(btnShowExecutions);
		
		JButton btnSwapAllocations = new JButton("Swap");
		panel_allocationNorth.add(btnSwapAllocations);
		
		JPanel panel_allcoationCentre = new JPanel();
		allocationPanel.add(panel_allcoationCentre, BorderLayout.CENTER);
		GridBagLayout gbl_panel_allcoationCentre = new GridBagLayout();
		gbl_panel_allcoationCentre.columnWidths = new int[]{0, 0, 0};
		gbl_panel_allcoationCentre.rowHeights = new int[]{0, 0};
		gbl_panel_allcoationCentre.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_allcoationCentre.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_allcoationCentre.setLayout(gbl_panel_allcoationCentre);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		panel_allcoationCentre.add(scrollPane_1, gbc_scrollPane_1);
		
		allocatedList = new JList<>();
		scrollPane_1.setViewportView(allocatedList);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 0;
		panel_allcoationCentre.add(scrollPane, gbc_scrollPane);
		
		unallocatedList = new JList<>();
		scrollPane.setViewportView(unallocatedList);
	}

	private void preview() {
		if (db == null) {
		    try{
		        db = DBAbstraction.getInstance();
		    } catch (DBExceptions.FailedToConnectException ex) {
		        Logger.getLogger(ManageAllocation.class.getName()).log(Level.SEVERE, null, ex);
		        return;
		    }
		}

		// Get all tasks (maybe filtered on the DB end?)
		List<Task> allTasks = db.getTaskList();

		// create generative temporal maps for all tasks and verifications
		List<TemporalMap<Integer, TaskExecution>> taskMaps = new ArrayList<>();

		for (Task task : allTasks) {
			taskMaps.add(new GenerativeTemporalMap<>(
				new ArrayList<>(), // <all task executions for that task>,
				task,
				(p) -> {
					TaskExecution taskExec = new TaskExecution(
						null, task, "", TaskPriority.NORMAL, p, null, null, null
					);

					Verification ver = task.getVerification();
					if (ver != null) {
						// Don't auto-allocate to the allocation constraint
						// because there are other constraints on allocation
						// (eg. time).
						VerificationExecution verExec = new VerificationExecution(
							null, ver, taskExec, "", ver.getStandardDeadline(), null, null
						);
						taskExec.setVerification(verExec);
					}
					
					return taskExec;
				}
			));
		}
		
		// Generate the task/verification executions
		Timeline<Integer, TaskExecution> tasksTimeline = new Timeline<>(taskMaps);
		List<TaskExecution> genTaskExecs = tasksTimeline.getBetween(
			LocalDateTime.now(), ldteGenEndTime.getObject(), Event.byStartTime, true, true
		);
		
		// Update the model
		DefaultListModel<Object> model = (DefaultListModel<Object>) (previewList.getModel());
		model.removeAllElements();
		model.addAll(genTaskExecs);
		
		// Finally, enable the 'confirm' button
		btnConfirm.setEnabled(true);
	}

	private void removeTaskExec() {
		DefaultListModel<Object> model = (DefaultListModel<Object>) previewList.getModel();
		int[] selectedIndexes = previewList.getSelectedIndices();
		for (int i = selectedIndexes.length - 1; i >= 0; i--) {
			model.remove(selectedIndexes[i]);
		}
	}
	
	private void confirm() {
		DefaultListModel<Object> model = (DefaultListModel<Object>) previewList.getModel();
		
		// Get List from JList
		List<TaskExecution> genTaskExecs = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			genTaskExecs.add((TaskExecution) model.get(i));
		}
		
		// Submit to DB
		db.submitTaskExecutions(genTaskExecs);
		
		// Remove graphically
		model.removeAllElements();
	}
}

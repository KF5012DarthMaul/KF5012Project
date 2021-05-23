package guicomponents;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions;
import domain.Task;
import domain.TaskExecution;
import domain.TaskPriority;
import domain.Verification;
import domain.VerificationExecution;
import guicomponents.formatters.Formatter;
import guicomponents.formatters.HTMLFormatter;
import guicomponents.formatters.NamedTaskExecutionFormatter;
import guicomponents.ome.LocalDateTimeEditor;
import temporal.Event;
import temporal.GenerativeTemporalMap;
import temporal.Period;
import temporal.TemporalMap;
import temporal.Timeline;

@SuppressWarnings("serial")
public class GenerateTasks extends JPanel {
	private static final Font LIST_FONT = new Font("Arial", Font.PLAIN, 12);
	
	private static final Formatter<TaskExecution> TASK_EXEC_FORMATTER =
			new HTMLFormatter<>(new NamedTaskExecutionFormatter());

	private LocalDateTimeEditor lsteEndTime;
	private JButton btnConfirm;
	private JList<Object> previewList;

	// DB
	private DBAbstraction db;
	
	public GenerateTasks() {
		GridBagLayout gbl_generateTasks = new GridBagLayout();
		gbl_generateTasks.columnWidths = new int[]{0, 0, 0, 0};
		gbl_generateTasks.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_generateTasks.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_generateTasks.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gbl_generateTasks);
		
		JLabel lblGenUntil = new JLabel("Generate from now until:");
		GridBagConstraints gbc_lblGenUntil = new GridBagConstraints();
		gbc_lblGenUntil.anchor = GridBagConstraints.WEST;
		gbc_lblGenUntil.insets = new Insets(5, 5, 5, 5);
		gbc_lblGenUntil.gridx = 0;
		gbc_lblGenUntil.gridy = 0;
		add(lblGenUntil, gbc_lblGenUntil);
		
		lsteEndTime = new LocalDateTimeEditor();
		GridBagConstraints gbc_lsteEndTime = new GridBagConstraints();
		gbc_lsteEndTime.anchor = GridBagConstraints.WEST;
		gbc_lsteEndTime.insets = new Insets(5, 5, 5, 5);
		gbc_lsteEndTime.gridx = 1;
		gbc_lsteEndTime.gridy = 0;
		add(lsteEndTime, gbc_lsteEndTime);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 2;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		
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
		add(separator, gbc_separator);
		
		JScrollPane listScrollPane = new JScrollPane();
		GridBagConstraints gbc_listScrollPane = new GridBagConstraints();
		gbc_listScrollPane.gridwidth = 3;
		gbc_listScrollPane.insets = new Insets(5, 5, 5, 0);
		gbc_listScrollPane.fill = GridBagConstraints.BOTH;
		gbc_listScrollPane.gridx = 0;
		gbc_listScrollPane.gridy = 2;
		add(listScrollPane, gbc_listScrollPane);
		
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

				this.setFont(LIST_FONT);
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
		add(panel_1, gbc_panel_1);
		
		JButton btnRemove = new JButton("Remove Selected Task Instances");
		btnRemove.addActionListener((e) -> this.removeTaskExec());
		panel_1.add(btnRemove);
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
						null, task, "", task.getStandardPriority(),
						p, new Period(p.start(), p.start()),
						null, null, null
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
			LocalDateTime.now(), lsteEndTime.getObject(), Event.byStartTime, true, true
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

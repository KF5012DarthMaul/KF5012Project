package guicomponents;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import domain.Completion;
import domain.Task;
import domain.TaskExecution;
import domain.TaskPriority;
import guicomponents.formatters.ColorFormatter;
import guicomponents.formatters.Formatter;
import guicomponents.formatters.HTMLFormatter;
import guicomponents.formatters.NamedTaskExecutionFormatter;
import guicomponents.ome.LocalDateTimeEditor;
import guicomponents.utils.TimelinePanel;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;
import lib.ScrollablePanel;
import temporal.ChartableEvent;
import temporal.Event;
import temporal.ExclusiveTemporalMap;
import temporal.Period;
import temporal.TemporalList;
import temporal.TemporalMap;
import temporal.Timeline;

@SuppressWarnings("serial")
public class AllocateTasks extends JScrollPane {
	private static final Font LIST_FONT = new Font("Arial", Font.PLAIN, 12);
	
	private static final Formatter<TaskExecution> REAL_TASK_EXEC_FORMATTER =
			new NamedTaskExecutionFormatter();
	
	private static final Formatter<TaskExecution> TASK_EXEC_FORMATTER =
			new HTMLFormatter<>(REAL_TASK_EXEC_FORMATTER);
	private static final Formatter<Candidate> CANDIDATE_FORMATTER =
			new HTMLFormatter<>(new CandidateFormatter(REAL_TASK_EXEC_FORMATTER));

	// GUI components
	private LocalDateTimeEditor lsteEndTime;
	private JList<Object> allocatedList;
	private JList<Object> unallocatedList;

	// Retained and split data
	private List<User> allUsers;
	private List<User> allCaretakers;
	
	private List<TaskExecution> allTaskExecutions;
	private List<TaskExecution> complAllocList = new ArrayList<>();
	private List<TaskExecution> uncomplAllocList = new ArrayList<>();
	private List<TaskExecution> uncomplUnallocList = new ArrayList<>();
	
	private Map<User, List<ChartableEvent>> allCaretakerAllocs;
	private Map<User, TemporalList<ChartableEvent>> allCaretakerAllocsTmprl;
	
	// Start and end times of a few things
	private LocalDateTime allocStartTime;
	private LocalDateTime allocEndTime;
	
	// Timeline
	private TimelinePanel timelinePanel;
	
	// State for allocation
	private List<Candidate> allAllocCandidates = new ArrayList<>();
	
	// DB
	private DBAbstraction db;
	
	public AllocateTasks() {
		ScrollablePanel content = new ScrollablePanel();
		content.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
		setViewportView(content);
		GridBagLayout gbl_allocateTasks = new GridBagLayout();
		gbl_allocateTasks.columnWidths = new int[]{0, 0};
		gbl_allocateTasks.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_allocateTasks.columnWeights = new double[]{0.0, 1.0};
		gbl_allocateTasks.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		content.setLayout(gbl_allocateTasks);

		JLabel lblGenUntil = new JLabel("Allocate from now until:");
		GridBagConstraints gbc_lblGenUntil = new GridBagConstraints();
		gbc_lblGenUntil.anchor = GridBagConstraints.EAST;
		gbc_lblGenUntil.insets = new Insets(5, 5, 5, 5);
		gbc_lblGenUntil.gridx = 0;
		gbc_lblGenUntil.gridy = 0;
		content.add(lblGenUntil, gbc_lblGenUntil);
		
		lsteEndTime = new LocalDateTimeEditor();
		lsteEndTime.addChangeListener((e) -> this.refreshTimeline());
		GridBagConstraints gbc_lsteEndTime = new GridBagConstraints();
		gbc_lsteEndTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_lsteEndTime.insets = new Insets(5, 5, 5, 5);
		gbc_lsteEndTime.gridx = 1;
		gbc_lsteEndTime.gridy = 0;
		content.add(lsteEndTime, gbc_lsteEndTime);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.gridwidth = 2;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		content.add(panel, gbc_panel);
		
		JButton btnPreviewAllocations = new JButton("Auto-Allocate (Preview)");
		btnPreviewAllocations.addActionListener((e) -> this.previewAllocations());
		panel.add(btnPreviewAllocations);
		
		JButton btnConfirmAllocations = new JButton("Confirm");
		btnConfirmAllocations.addActionListener((e) -> this.confirmAllocations());
		panel.add(btnConfirmAllocations);
		
		JButton btnRemoveAllocations = new JButton("Remove All Candidates");
		btnRemoveAllocations.addActionListener((e) -> this.removeAllAllocations());
		panel.add(btnRemoveAllocations);
		
		JButton btnRemoveSelected = new JButton("Remove Selected Candidates");
		btnRemoveSelected.addActionListener((e) -> this.removeSelectedAllocationCandidates());
		panel.add(btnRemoveSelected);

		JSeparator sep1 = new JSeparator();
		GridBagConstraints gbc_sep1 = new GridBagConstraints();
		gbc_sep1.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep1.insets = new Insets(5, 5, 5, 5);
		gbc_sep1.gridwidth = 2;
		gbc_sep1.gridx = 0;
		gbc_sep1.gridy = 2;
		content.add(sep1, gbc_sep1);

		timelinePanel = new TimelinePanel();
		GridBagConstraints gbc_timelinePanel = new GridBagConstraints();
		gbc_timelinePanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_timelinePanel.insets = new Insets(0, 5, 5, 0);
		gbc_timelinePanel.gridwidth = 2;
		gbc_timelinePanel.gridx = 0;
		gbc_timelinePanel.gridy = 3;
		content.add(timelinePanel, gbc_timelinePanel);

		JSeparator sep2 = new JSeparator();
		GridBagConstraints gbc_sep2 = new GridBagConstraints();
		gbc_sep2.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep2.insets = new Insets(5, 5, 5, 5);
		gbc_sep2.gridwidth = 2;
		gbc_sep2.gridx = 0;
		gbc_sep2.gridy = 4;
		content.add(sep2, gbc_sep2);

		JPanel listsPanel = new JPanel();
		GridBagConstraints gbc_listsPanel = new GridBagConstraints();
		gbc_listsPanel.fill = GridBagConstraints.BOTH;
		gbc_listsPanel.insets = new Insets(5, 5, 5, 5);
		gbc_listsPanel.gridwidth = 2;
		gbc_listsPanel.gridx = 0;
		gbc_listsPanel.gridy = 5;
		content.add(listsPanel, gbc_listsPanel);
		GridBagLayout gbl_listsPanel = new GridBagLayout();
		gbl_listsPanel.columnWidths = new int[] {0, 0};
		gbl_listsPanel.rowHeights = new int[] {0, 0, 0, 0};
		gbl_listsPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_listsPanel.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		listsPanel.setLayout(gbl_listsPanel);
		
		JScrollPane allocatedScrollPane = new JScrollPane();
		GridBagConstraints gbc_allocatedScrollPane = new GridBagConstraints();
		gbc_allocatedScrollPane.fill = GridBagConstraints.BOTH;
		gbc_allocatedScrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_allocatedScrollPane.gridx = 0;
		gbc_allocatedScrollPane.gridy = 0;
		listsPanel.add(allocatedScrollPane, gbc_allocatedScrollPane);
		
		allocatedList = new JList<>(new DefaultListModel<>());
		allocatedList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(
					JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus
			) {
				// Set stuff related to isSelected and cellHasFocus
				super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
				
				this.setFont(LIST_FONT);
				if (value instanceof TaskExecution) {
					setText(TASK_EXEC_FORMATTER.apply((TaskExecution) value));
				} else if (value instanceof Candidate) {
					setText(CANDIDATE_FORMATTER.apply((Candidate) value));
				}
				return this;
			}
		});
		allocatedScrollPane.setViewportView(allocatedList);

		JPanel buttonPanel = new JPanel();
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.anchor = GridBagConstraints.CENTER;
		gbc_buttonPanel.insets = new Insets(5, 5, 5, 5);
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 1;
		listsPanel.add(buttonPanel, gbc_buttonPanel);
		
		JButton btnAllocate = new JButton("↑ Allocate ↑");
		btnAllocate.addActionListener((e) -> this.allocateSelected());
		buttonPanel.add(btnAllocate);

		JButton btnSwap = new JButton("⟳ Swap ⟳");
		btnSwap.addActionListener((e) -> this.swapAllocations());
		buttonPanel.add(btnSwap);

		JButton btnDeallocate = new JButton("↓  Deallocate  ↓");
		btnDeallocate.addActionListener((e) -> this.deallocateSelected());
		buttonPanel.add(btnDeallocate);

		JScrollPane unallocatedScrollPane = new JScrollPane();
		GridBagConstraints gbc_unallocatedScrollPane = new GridBagConstraints();
		gbc_unallocatedScrollPane.fill = GridBagConstraints.BOTH;
		gbc_unallocatedScrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_unallocatedScrollPane.gridx = 0;
		gbc_unallocatedScrollPane.gridy = 2;
		listsPanel.add(unallocatedScrollPane, gbc_unallocatedScrollPane);
		
		unallocatedList = new JList<>(new DefaultListModel<>());
		unallocatedList.setCellRenderer(new DefaultListCellRenderer() {
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
		unallocatedScrollPane.setViewportView(unallocatedList);

		initialise();
	}
	
	/* Data Lifecycle
	 * -------------------------------------------------- */
	
	/* Groups of methods
	 * -------------------- */

	/**
	 * Fully (re-)initialise the allocation data model and GUI.
	 */
	private void initialise() {
		fetch();
		initUserEfficiencies(); // Only do this on init, not reinit
		reinitialise();
	}
	
	/**
	 * Reinitialise data model based on current DB cache.
	 */
	private void reinitialise() {
		initTaskExecSplit();
		initCaretakerAllocCandidates();
		initAllCaretakerAllocs();
		
		fetchGUI();
		refreshGUI();
	}

	/* Initialisations
	 * -------------------- */

	/**
	 * Fetch data from DB and cache it.
	 */
	private void fetch() {
		if (db == null) {
			try{
				db = DBAbstraction.getInstance();
			} catch (DBExceptions.FailedToConnectException ex) {
				Logger.getLogger(ManageAllocation.class.getName()).log(Level.SEVERE, null, ex);
				return;
			}
		}

		// Get all users
		allUsers = db.getAllUsers();
		allUsers.sort((u1, u2) -> u1.getDisplayName().compareTo(u2.getDisplayName()));
		allCaretakers = allUsers.stream()
			.filter(u -> u.getAccountType() == PermissionManager.AccountType.CARETAKER)
			.collect(Collectors.toList());
		
		// Get all task executions split by allocation
		allTaskExecutions = db.getTaskExecutionList();
	}

	/**
	 * Calculate and update the efficiencies (average time taken per user) for
	 * all users for all tasks, then flush the updates to the DB.
	 * 
	 * Note: This is a computationally expensive operation - O(n^2) with a
	 *       potentially large n - so try to avoid doing it often.
	 */
	private void initUserEfficiencies() {
		// Get all task executions by task
		Map<Task, List<TaskExecution>> taskExecsByTask = new HashMap<>();
		for (TaskExecution taskExec : allTaskExecutions) {
			// Include deleted tasks as well - makes no difference here
			Task task = taskExec.getTask();
			
			if (!taskExecsByTask.containsKey(task)) {
				taskExecsByTask.put(task, new ArrayList<>());
			}
			taskExecsByTask.get(task).add(taskExec);
		}

		for (Task task : taskExecsByTask.keySet()) {
			// Get sums of time taken for completed execs of this task for each
			// user.
			Map<User, Duration> sum = new HashMap<>();
			Map<User, Integer> count = new HashMap<>();
			for (TaskExecution taskExec : taskExecsByTask.get(task)) {
				if (taskExec.getCompletion() != null) {
					Completion compl = taskExec.getCompletion();
					User completedBy = compl.getStaff();
					Duration taskDur = Duration.between(compl.getStartTime(), compl.getCompletionTime());
					
					// Init / add sum
					Duration curSum = sum.get(completedBy);
					if (curSum == null) {
						curSum = Duration.ofMinutes(0);
					}
					sum.put(completedBy, curSum.plus(taskDur));
					
					// Init / increment count
					Integer curCount = count.get(completedBy);
					if (curCount == null) {
						curCount = Integer.valueOf(0);
					}
					count.put(completedBy, curCount + 1);
				}
			}
			
			// Set the efficiency values for each user (average time taken)
			for (User user : allCaretakers) {
				if (sum.containsKey(user)) {
					task.setEfficiency(user, sum.get(user).dividedBy(count.get(user)));
				} else {
					Duration dur = task.getSchedule().periodSet().referencePeriod().duration();
					if (dur != null) {
						task.setEfficiency(user, dur);
					} else {
						// FIXME: This is silly - you can't do a task in
						//        literally no time. It also means it can be
						//        allocated anywhere.
						task.setEfficiency(user, Duration.ofSeconds(1));
					}
				}
			}
			
			// Flush to DB
			db.submitTask(task);
		}
	}

	/**
	 * Split task executions into lists based on their allocation and completion
	 * status.
	 */
	private void initTaskExecSplit() {
		// All allocated + complete tasks
		// All unallocated + incomplete tasks
		
		complAllocList = new ArrayList<>();
		uncomplAllocList = new ArrayList<>();
		uncomplUnallocList = new ArrayList<>();
		for (TaskExecution taskExec : allTaskExecutions) {
			if (taskExec.getAllocation() != null) {
				if (taskExec.getCompletion() != null) {
					complAllocList.add(taskExec);
				} else {
					uncomplAllocList.add(taskExec);
				}
			} else {
				if (taskExec.getCompletion() == null) {
					uncomplUnallocList.add(taskExec);
				}
			}
		}
	}

	/**
	 * Initialise the list of candidate caretaker allocations.
	 */
	private void initCaretakerAllocCandidates() {
		allAllocCandidates = new ArrayList<>();
	}

	/**
	 * Initialise each user's list of current and candidate caretaker
	 * allocations.
	 */
	private void initAllCaretakerAllocs() {
		// Split allocated task executions by caretaker
		allCaretakerAllocs = new HashMap<>();
		for (User user : allCaretakers) {
			allCaretakerAllocs.put(user, new ArrayList<>());
		}
		for (TaskExecution taskExec : uncomplAllocList) {
			allCaretakerAllocs.get(taskExec.getAllocation()).add(taskExec);
		}
		for (Candidate candidate : allAllocCandidates) {
			allCaretakerAllocs.get(candidate.caretaker()).add(candidate);
		}

		// Make temporal lists from these (will sort them by start date)
		allCaretakerAllocsTmprl = new HashMap<>();
		for (User user : allCaretakers) {
			allCaretakerAllocsTmprl.put(user, new TemporalList<ChartableEvent>(allCaretakerAllocs.get(user)));
		}
	}

	/**
	 * Retrieve current allocation start and end dates and cache them.
	 */
	private void fetchGUI() {
		// The start and end times for the allocation
		allocStartTime = LocalDateTime.now();
		allocEndTime = lsteEndTime.getObject();
	}

	/* Data Mutation
	 * -------------------- */

	// Allocation uses genAllocCandidates()
	
	private void deallocate(List<Event> events) {
		for (Event event : events)
		if (event instanceof TaskExecution) {
			TaskExecution taskExec = (TaskExecution) event;
			
			// 'Deallocate' = set allocated user to null and period to zero duration
			taskExec.setAllocation(null);
			taskExec.setPeriod(new Period(
				taskExec.getPeriodConstraint().start(),
				Duration.ofMinutes(0)
			));
			
			db.submitTaskExecution(taskExec);
		}
		
		initTaskExecSplit();
		initAllCaretakerAllocs();
		refreshGUI();
	}
	
	/**
	 * Remove from the list of candidate carataker allocations - use this to
	 * ensure all other model data and GUI elements are updated appropriately
	 * after the model update.
	 * 
	 * @param allocCandidates The list of candidates to remove.
	 */
	private void removeAllocCandidates(List<Candidate> delAllocCandidates) {
		// Remove all and refresh
		allAllocCandidates.removeAll(delAllocCandidates);
		initAllCaretakerAllocs();
		refreshGUI();
	}

	/* Updating the GUI
	 * -------------------- */

	/**
	 * Refresh the entire GUI with the current data in the data model.
	 */
	private void refreshGUI() {
		DefaultListModel<Object> model;

		// Update the models
		model = (DefaultListModel<Object>) (allocatedList.getModel());
		model.removeAllElements();
		model.addAll(uncomplAllocList);
		model.addAll(allAllocCandidates);

		model = (DefaultListModel<Object>) (unallocatedList.getModel());
		model.removeAllElements();
		model.addAll(uncomplUnallocList);

		// Update the timeline - set both preferred and min to ensure it
		// displays the right size
		Dimension dim = new Dimension(
			this.getPreferredSize().width,
			80 + allCaretakers.size() * 20
		);
		timelinePanel.setMinimumSize(dim);
		timelinePanel.setPreferredSize(dim);
		
		this.refreshTimeline();
	}
	
	/**
	 * Refresh just the graphical timeline with cached data.
	 */
	private void refreshTimeline() {
		// Make a list of maps in order of display name
		List<TemporalMap<Integer, ChartableEvent>> maps = new ArrayList<>();
		for (User caretaker : allCaretakers) {
			maps.add(allCaretakerAllocsTmprl.get(caretaker));
		}
		timelinePanel.setTimeline(new Timeline<>(maps));
		timelinePanel.showBetween(LocalDateTime.now(), lsteEndTime.getObject());
	}

	/* Main Lifecycle
	 * -------------------------------------------------- */

	/**
	 * Attempt to allocate all unallocated tasks to caretakers in the specified
	 * date range (now to when the user selects).
	 */
	private void previewAllocations() {
		fetchGUI();
		
		/* Split by priority and do allocation
		 * -------------------- */

		// Split unallocated tasks by priority
		Map<TaskPriority, List<TaskExecution>> unallocPriorityTasks = new HashMap<>();
		for (TaskPriority tp : TaskPriority.values()) {
			unallocPriorityTasks.put(tp, new ArrayList<>());
		}
		for(TaskExecution taskExec : uncomplUnallocList) {
			unallocPriorityTasks.get(taskExec.getPriority()).add(taskExec);
		}

		// Allocate for each task priority, in order
		genAllocCandidates(unallocPriorityTasks.get(TaskPriority.HIGH));
		genAllocCandidates(unallocPriorityTasks.get(TaskPriority.NORMAL));
		genAllocCandidates(unallocPriorityTasks.get(TaskPriority.LOW));
	}
	
	/**
	 * Remove selected allocation candidates and refresh relevant parts of the
	 * data model and GUI.
	 */
	private void removeSelectedAllocationCandidates() {
		List<Candidate> delAllocCandidates = new ArrayList<>();

		// Build the list to remove
		DefaultListModel<Object> model = (DefaultListModel<Object>) allocatedList.getModel();
		int[] selectedIndexes = allocatedList.getSelectedIndices();
		for (int i = selectedIndexes.length - 1; i >= 0; i--) {
			if (model.get(selectedIndexes[i]) instanceof Candidate) { // Can only remove candidates
				delAllocCandidates.add((Candidate) model.get(selectedIndexes[i]));
			}
		}
		
		// Remove and refresh data/GUI
		removeAllocCandidates(delAllocCandidates);
	}

	/**
	 * Remove all allocation candidates and refresh relevant parts of the data
	 * model and GUI.
	 */
	private void removeAllAllocations() {
		removeAllocCandidates(allAllocCandidates);
	}

	/**
	 * Confirm all allocation candidates - perform update to event objects and
	 * flush changes to the DB.
	 */
	private void confirmAllocations() {
		// Store alloc candidates, then wipe the alloc candidate list
		List<Candidate> newAllAllocCandidates = allAllocCandidates;
		initCaretakerAllocCandidates();

		// Update all task executions of these candidate allocations
		List<TaskExecution> allAllocTasks = new ArrayList<>();
		for (Candidate candidate : newAllAllocCandidates) {
			TaskExecution taskExec = candidate.taskExecution();
			
			taskExec.setAllocation(candidate.caretaker());
			taskExec.setPeriod(new Period(candidate.startTime(), candidate.endTime()));
			
			allAllocTasks.add(taskExec);
		}

		// Flush to DB
		db.submitTaskExecutions(allAllocTasks);

		// Reinitialise
		// Note: Don't need to fetch, as the above will have mutated the cached
		//       task executions.
		reinitialise();
	}

	/**
	 * Allocate selected unallocated task(s).
	 * 
	 * Uses the same mechanism as automatic allocation, but for only the
	 * selected task(s).
	 */
	private void allocateSelected() {
		fetchGUI();
		
		List<TaskExecution> allocList = new ArrayList<>();
		
		DefaultListModel<Object> unallocModel = (DefaultListModel<Object>) unallocatedList.getModel();
		int[] unallocSelIndexes = unallocatedList.getSelectedIndices();
		for (int selIndex : unallocSelIndexes) {
			Event event = (Event) unallocModel.get(selIndex);
			if (event instanceof TaskExecution) {
				allocList.add((TaskExecution) event);
			}
		}
		
		genAllocCandidates(allocList);
	}

	/**
	 * Deallocate selected allocated task(s).
	 */
	private void deallocateSelected() {
		fetchGUI();
		
		List<Event> deallocList = new ArrayList<>();
		
		DefaultListModel<Object> allocModel = (DefaultListModel<Object>) allocatedList.getModel();
		int[] allocSelIndexes = allocatedList.getSelectedIndices();
		for (int selIndex : allocSelIndexes) {
			deallocList.add((Event) allocModel.get(selIndex));
		}
		
		deallocate(deallocList);
	}

	/**
	 * Swap either two allocated tasks if two are selected, or the selected
	 * allocated task and the selected unallocated task otherwise.
	 * 
	 * Before carrying out the swaps, check if the swap is viable. All newly
	 * allocated tasks or reallocated tasks must be checked - ie. both tasks if
	 * swapping two allocated tasks, or the unallocated task to be allocated if
	 * swapping an unallocated task and an allocated task. For each newly
	 * allocated/reallocated task, check that allocating the task to the current
	 * allocated user of the task it is being swapped with would result in
	 * overlapping with any other task that user has allocated. Note that each
	 * user may take a different amount of time to complete the task, which is
	 * factored in to the check.
	 * 
	 * If the check(s) are successful, then swap the task executions and flush
	 * the changes to the DB.
	 */
	private void swapAllocations() {
		DefaultListModel<Object> allocModel = (DefaultListModel<Object>) allocatedList.getModel();
		int[] allocSelIndexes = allocatedList.getSelectedIndices();
		int[] unallocSelIndexes = unallocatedList.getSelectedIndices();
		
		// Swap two allocated tasks
		if (allocSelIndexes.length == 2 && unallocSelIndexes.length == 0) {
			Event event1 = (Event) allocModel.get(allocSelIndexes[0]);
			Event event2 = (Event) allocModel.get(allocSelIndexes[1]);
			
			if (event1 instanceof TaskExecution && event2 instanceof TaskExecution) {
				// Get some values
				TaskExecution taskExec1 = (TaskExecution) event1;
				TaskExecution taskExec2 = (TaskExecution) event2;

				LocalDateTime taskExec1Start = taskExec1.getPeriod().start();
				LocalDateTime taskExec2Start = taskExec2.getPeriod().start();
				
				User taskExec1Alloc = taskExec1.getAllocation();
				User taskExec2Alloc = taskExec2.getAllocation();
				
				Duration taskExec1DurWith2Alloc = taskExec1.getTask().getEfficiencyMap().get(taskExec2Alloc);
				Duration taskExec2DurWith1Alloc = taskExec2.getTask().getEfficiencyMap().get(taskExec1Alloc);

				// Make the exclusive temporal maps
				List<Event> te1UncomplAllocTasks =
					new ArrayList<>(allCaretakerAllocs.get(taskExec1Alloc));
				te1UncomplAllocTasks.remove(taskExec1);
				ExclusiveTemporalMap<Integer, Event> te1UncomplAllocTasksExcl =
					new ExclusiveTemporalMap<>(
						new TemporalList<>(te1UncomplAllocTasks),
						Event.byPeriodDefaultZero // Default shouldn't make a difference
					);

				List<Event> te2UncomplAllocTasks =
					new ArrayList<>(allCaretakerAllocs.get(taskExec2Alloc));
				te2UncomplAllocTasks.remove(taskExec2);
				ExclusiveTemporalMap<Integer, Event> te2UncomplAllocTasksExcl =
					new ExclusiveTemporalMap<>(
						new TemporalList<>(te2UncomplAllocTasks),
						Event.byPeriodDefaultZero // Default shouldn't make a difference
					);

				// Make the candidates for the swap
				Candidate te1ToTe2Candidate = new Candidate(
					// Task exec 1, but allocated to who + at when task exec 2 is allocated
					taskExec1, taskExec2Alloc, taskExec2Start, taskExec2Start.plus(taskExec2DurWith1Alloc)
				);
				Candidate te2ToTe1Candidate = new Candidate(
					// Task exec 2, but allocated to who + at when task exec 1 is allocated
					taskExec2, taskExec1Alloc, taskExec1Start, taskExec1Start.plus(taskExec1DurWith2Alloc)
				);
				
				// Check if the candidates are a valid swap
				if (
						// Is the taskExec2 valid when allocated to the user/at the time of taskExec1?
						!te1UncomplAllocTasksExcl.isValid(te2ToTe1Candidate) ||

						// Is the taskExec1 valid when allocated to the user/at the time of taskExec2?
						!te2UncomplAllocTasksExcl.isValid(te1ToTe2Candidate)
				) {
					new ExceptionDialog("Cannot swap tasks, as swapped task allocations would overlap with other allocated tasks");
					return;
				}
				
				// If so, do swap
				taskExec1.setAllocation(te1ToTe2Candidate.caretaker());
				taskExec1.setPeriod(te1ToTe2Candidate.getPeriod());

				taskExec2.setAllocation(te2ToTe1Candidate.caretaker());
				taskExec2.setPeriod(te2ToTe1Candidate.getPeriod());
				
				// Flush both to DB
				db.submitTaskExecution(taskExec1);
				db.submitTaskExecution(taskExec2);
			}
		}

		initAllCaretakerAllocs();
		refreshGUI();
	}
	
	/* Allocation Algorithm
	 * -------------------------------------------------- */

	private void genAllocCandidates(List<TaskExecution> unallocUncomplTaskExecs) {
		// Filter ...
		
		// ... to only those that do not already have allocation candidates
		// (you would have to deallocate them first)
		unallocUncomplTaskExecs = unallocUncomplTaskExecs.stream()
			.filter((taskExec) -> (
				allAllocCandidates.stream().allMatch((candidate) -> (
					candidate.taskExecution() != taskExec
				))
			))
			.collect(Collectors.toList());
		
		// ... to only those in the current date/time range
		TemporalList<TaskExecution> taskExecListTmprl = new TemporalList<>(unallocUncomplTaskExecs);
		List<TaskExecution> unallocUncomplTaskExecsBefore = taskExecListTmprl.getBefore(
			allocEndTime, Event.byStartTime, true
		);

		// Try allocating each task execution
		for (TaskExecution unallocUncomplTaskExec : unallocUncomplTaskExecsBefore) {
			/* Check constraints
			 * -------------------- */
			
			// If there is a user constraint, use it, otherwise check all caretakers
			User allocConst = unallocUncomplTaskExec.getTask().getAllocationConstraint();
			List<User> candidates;
			if (allocConst != null) {
				candidates = listOfSingleItem(allocConst);
			} else {
				candidates = allCaretakers;
			}

			/* Define what the 'best' candidate means
			 * -------------------- */
			
			CandidateContainer bestCandidate = new CandidateContainer();

			/**
			 * A function that checks if a particular allocation candidate
			 * slot (ie. user + temporal gap) is the best candidate slot
			 * across all users (so far), and returns the Candidate that
			 * represents the actual allocation if it is. Otherwise returns
			 * Candidate.NO_CANDIDATE.
			 */
			Function<Candidate, Candidate> getIfBestCandidate = (c) -> {
				Duration gapLen = Duration.between(c.startTime(), c.endTime());
				Duration taskExecLength =
					c.taskExecution().getTask().getEfficiencyMap().get(c.caretaker());
				if (
						// Is the gap big enough?
						gapLen.compareTo(taskExecLength) >= 0 &&
						
						// Found a slot! Is it the best slot so far?
						(
						bestCandidate.candidate == Candidate.NO_CANDIDATE ||
						c.startTime().isBefore(bestCandidate.candidate.startTime())
						)
				) {
					return new Candidate(
						c.taskExecution(),
						c.caretaker(),
						c.startTime(),
						c.startTime().plus(taskExecLength)
					);
				}
				return Candidate.NO_CANDIDATE;
			};

			/* Find bounds for this task exec's allocation
			 * -------------------- */
			 
			LocalDateTime thisAllocStartTime = allocStartTime;
			LocalDateTime thisAllocEndTime = allocEndTime;

			Period pc = unallocUncomplTaskExec.getPeriodConstraint();
			if (pc.start().isAfter(thisAllocStartTime)) {
				thisAllocStartTime = pc.start();
			}
			if (pc.end() != null && pc.end().isBefore(thisAllocEndTime)) {
				thisAllocEndTime = pc.end();
			}
			if (thisAllocEndTime.isBefore(thisAllocStartTime)) {
				// If the times end up out of order, then the event is outside
				// of the allocation range.
				continue; // Next task exec
			}
			
			/* Find the best candidate user + period to allocate
			 * -------------------- */

			for (User candidateUser : candidates) {
				// Just declare this here
				Candidate possiblyBestCandidate;
				
				// Filter the list to get all allocations between the
				// allocation start and end times (ie. now and the time the
				// user selected).
				List<? extends Event> allocToUserBetween =
					allCaretakerAllocsTmprl.get(candidateUser).getBetween(
						thisAllocStartTime, thisAllocEndTime,
						
						// Include any that start before now but continue to
						// at least now, and any that end after the date
						// selected, but that start before it.
						Event.byPeriodDefaultInf, true, true
					);
				
				// Each end time is a potential point that another task
				// could be slotted in, so go through them in order of end
				// time (ie. potential start time).
				Collections.sort(allocToUserBetween, Event.byEndTime);

				// If this user has nothing on their schedule, then check
				// the entire requested allocation period.
				if (allocToUserBetween.size() == 0) {
					// If this user has nothing to do, how about now?
					possiblyBestCandidate = getIfBestCandidate.apply(
						new Candidate(
							unallocUncomplTaskExec,
							candidateUser,
							thisAllocStartTime, thisAllocEndTime
						)
					);
					if (possiblyBestCandidate != Candidate.NO_CANDIDATE) {
						bestCandidate.candidate = possiblyBestCandidate;
					}
					continue; // Next candidate user
				}
				
				// Search this user's schedule - are there any gaps large
				// enough for this task?
				// Note 1: This algorithm doesn't do any shifting of tasks
				//         to make space for other tasks.

				int i = 0;
				
				// First, check between the allocation start date and the
				// first event found (by end time).
				possiblyBestCandidate = getIfBestCandidate.apply(
					new Candidate(
						unallocUncomplTaskExec,
						candidateUser,
						thisAllocStartTime,
						allocToUserBetween.get(i).getPeriod().start()
					)
				);
				if (possiblyBestCandidate != Candidate.NO_CANDIDATE) {
					bestCandidate.candidate = possiblyBestCandidate;
				}
				
				// Second, check between the gaps of all other events.
				// Note: allocToUserBetween is guaranteed to be an exclusive
				//       list, by the definition of 'exclusive' in
				//       ExclusiveTemporalMap (ie. periods don't overlap,
				//       except possibly at a single point in time). Eg.
				//               Couldn't have been allocated to this user
				// t1:           |2|    |4|    |X| <-'
				// t2:   | 1 |      | 3 |      | 5 |
				for (i++; i < allocToUserBetween.size(); i++) {
					// FIXME[?]: prevEnd shouldn't be null for allocated tasks
					possiblyBestCandidate = getIfBestCandidate.apply(
						new Candidate(
							unallocUncomplTaskExec,
							candidateUser,
							allocToUserBetween.get(i-1).getPeriod().end(),
							allocToUserBetween.get(i).getPeriod().start()
						)
					);
					if (possiblyBestCandidate != Candidate.NO_CANDIDATE) {
						bestCandidate.candidate = possiblyBestCandidate;
						break; // Next candidate
					}
				}
				
				// Finally, check the gap between the last candidate and
				// the end of the allocation period.
				// FIXME[?]: prevEnd shouldn't be null for allocated tasks
				possiblyBestCandidate = getIfBestCandidate.apply(
					new Candidate(
						unallocUncomplTaskExec,
						candidateUser,
						allocToUserBetween.get(i-1).getPeriod().end(),
						thisAllocEndTime
					)
				);
				if (possiblyBestCandidate != Candidate.NO_CANDIDATE) {
					bestCandidate.candidate = possiblyBestCandidate;
				}
			}

			/* If there is a best candidate, allocate it
			 * -------------------- */
			
			Candidate candidate = bestCandidate.candidate;
			if (candidate != Candidate.NO_CANDIDATE) {
				allAllocCandidates.add(candidate);
				
				// MUTATE allCaretakerAllocs during algo ...
				// Add it to the list of allocated tasks to use it as a time
				// constraint. ENSURE it maintains start time sort order,
				// because this is the list underlying the TemporalList.
				List<ChartableEvent> allocTasks = allCaretakerAllocs.get(candidate.caretaker());
				allocTasks.add(candidate); // Add the candidate, not the task exec
				allocTasks.sort(Event.byStartTime);
			}
			// else, the task can't be allocated (using this algo)
		}

		// Then update the GUI
		refreshGUI();
	}

	/**
	 * Class for allocation candidates.
	 * 
	 * @author William Taylor
	 */
	private static class Candidate implements ChartableEvent {
		private static final Color COLOR = Color.CYAN;
		public static final Candidate NO_CANDIDATE = new Candidate(null, null, null, null);
		
		private TaskExecution taskExec;
		private User user;
		private LocalDateTime startTime;
		private LocalDateTime endTime;

		private Period period;
		
		/**
		 * Create a candidate
		 * 
		 * @param user The candidate user for allocation.
		 * @param startTime The candidate user's earliest start time for
		 * allocation.
		 * @param endTime The end time for allocation.
		 */
		public Candidate(
				TaskExecution taskExec,
				User user,
				LocalDateTime startTime,
				LocalDateTime endTime
		) {
			assert taskExec != null;
			assert user != null;
			assert startTime != null;
			assert endTime != null;

			this.taskExec = taskExec;
			this.user = user;
			this.startTime = startTime;
			this.endTime = endTime;
			
			this.period = new Period(startTime, endTime);
		}

		public TaskExecution taskExecution() { return taskExec; }
		public User caretaker() { return user; }
		public LocalDateTime startTime() { return startTime; }
		public LocalDateTime endTime() { return endTime; }

		@Override
		public Period getPeriod() {
			return this.period;
		}

		@Override
		public String getName() {
			return this.taskExecution().getName();
		}

		@Override
		public Color getColor() {
			return COLOR;
		}
	}
	
	private static class CandidateFormatter implements Formatter<Candidate> {
		private static final ColorFormatter FORMATTER = new ColorFormatter("green");
		
		private Formatter<TaskExecution> taskExecFormatter;
		
		public CandidateFormatter(Formatter<TaskExecution> taskExecFormatter) {
			this.taskExecFormatter = taskExecFormatter;
		}
		
		@Override
		public String apply(Candidate c) {
			TaskExecution t = c.taskExecution();
			TaskExecution displayTaskExec = new TaskExecution(
					t.getID(), t.getTask(),
					t.getNotes(), t.getPriority(),
					t.getPeriodConstraint(), new Period(c.startTime(), c.endTime()),
					c.caretaker(), t.getCompletion(), t.getVerification()
			);
			return FORMATTER.apply(taskExecFormatter.apply(displayTaskExec));
		}
	}

	/**
	 * Wrapper class to allow changeable candidate to be used in lambda functions.
	 * 
	 * @author William Taylor
	 */
	private static class CandidateContainer {
		public Candidate candidate = Candidate.NO_CANDIDATE;
	}
	
	/**
	 * Make a list of a single item.
	 * 
	 * @author Emanuel Oliveira W19029581
	 * 
	 * @param <T> Type of the item.
	 * @param item The item to make a list of.
	 * @return A list containing only item.
	 */
	private static <T> List<T> listOfSingleItem(T item)
	{
		List<T> l = new ArrayList<>();
		l.add(item);
		return l;
	}
}

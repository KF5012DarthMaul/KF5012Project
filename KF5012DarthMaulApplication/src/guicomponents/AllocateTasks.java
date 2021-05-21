package guicomponents;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import dbmgr.DBAbstraction;
import domain.TaskExecution;
import domain.TaskPriority;
import guicomponents.ome.LocalDateTimeEditor;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;
import temporal.Event;
import temporal.Period;
import temporal.TemporalList;

@SuppressWarnings("serial")
public class AllocateTasks extends JPanel {
	private LocalDateTimeEditor lsteEndTime;
	private JList<Object> allocatedList;
	private JList<Object> unallocatedList;

	// DB
	private DBAbstraction db;
	
	public AllocateTasks() {
		GridBagLayout gbl_allocateTasks = new GridBagLayout();
		gbl_allocateTasks.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_allocateTasks.rowHeights = new int[]{0, 0, 0};
		gbl_allocateTasks.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_allocateTasks.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gbl_allocateTasks);

		JLabel lblGenUntil = new JLabel("Allocate from now until:");
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
		
		JButton btnShowExecutions = new JButton("Load Tasks");
		GridBagConstraints gbc_btnShowExecutions = new GridBagConstraints();
		gbc_btnShowExecutions.anchor = GridBagConstraints.WEST;
		gbc_btnShowExecutions.insets = new Insets(5, 5, 5, 5);
		gbc_btnShowExecutions.gridx = 2;
		gbc_btnShowExecutions.gridy = 0;
		add(btnShowExecutions, gbc_btnShowExecutions);
		
		JButton btnSwapAllocations = new JButton("Swap");
		GridBagConstraints gbc_btnSwapAllocations = new GridBagConstraints();
		gbc_btnSwapAllocations.anchor = GridBagConstraints.WEST;
		gbc_btnSwapAllocations.insets = new Insets(5, 5, 5, 5);
		gbc_btnSwapAllocations.gridx = 3;
		gbc_btnSwapAllocations.gridy = 0;
		add(btnSwapAllocations, gbc_btnSwapAllocations);
		
		JPanel listsPanel = new JPanel();
		GridBagConstraints gbc_listsPanel = new GridBagConstraints();
		gbc_listsPanel.anchor = GridBagConstraints.WEST;
		gbc_listsPanel.insets = new Insets(5, 5, 5, 5);
		gbc_listsPanel.gridwidth = 4;
		gbc_listsPanel.gridx = 0;
		gbc_listsPanel.gridy = 1;
		add(listsPanel, gbc_listsPanel);
		listsPanel.setLayout(new BoxLayout(listsPanel, BoxLayout.X_AXIS));
		
		JScrollPane allocatedScrollPane = new JScrollPane();
		listsPanel.add(allocatedScrollPane);
		
		allocatedList = new JList<>();
		allocatedScrollPane.setViewportView(allocatedList);
		
		JScrollPane unallocatedScrollPane = new JScrollPane();
		listsPanel.add(unallocatedScrollPane);
		
		unallocatedList = new JList<>();
		unallocatedScrollPane.setViewportView(unallocatedList);
	}
	
	private void allocateAll() {
		/* Get from DB & GUI, and split (immutable)
		 * -------------------------------------------------- */
		
		// Get all users
		List<User> allUsers = db.getAllUsers();
		List<User> allCaretakers = allUsers.stream()
			.filter(u -> u.getAccountType() == PermissionManager.AccountType.CARETAKER)
			.collect(Collectors.toList());
		
		// Get all task executions split by allocation
		List<TaskExecution> allTaskExecutions = db.getTaskExecutionList();

		List<TaskExecution> allocList = new ArrayList<>();
		List<TaskExecution> unallocList = new ArrayList<>();
		for (TaskExecution taskExec : allTaskExecutions) {
			if (taskExec.getAllocation() != null) {
				allocList.add(taskExec);
			} else {
				unallocList.add(taskExec);
			}
		}
		
		LocalDateTime endTime = lsteEndTime.getObject();

		/* Initialise key variables (mutated over time)
		 * -------------------------------------------------- */
		
		// Get a map of all allocated task executions by caretaker.
		Map<User, List<TaskExecution>> allocCaretakerTasks = new HashMap<>();
		for (User user : allCaretakers) {
			allocCaretakerTasks.put(user, new ArrayList<>());
		}
		for (TaskExecution taskExec : allocList) {
			allocCaretakerTasks.get(taskExec.getAllocation()).add(taskExec);
		}

		// Make TemporalLists from these - used in various places
		Map<User, TemporalList<TaskExecution>> allocCaretakerTasksTmprl = new HashMap<>();
		for (User user : allCaretakers) {
			allocCaretakerTasksTmprl.put(user, new TemporalList<>(allocCaretakerTasks.get(user)));
		}

		// Make the list of tasks to allocate at the end
		List<TaskExecution> allNewExecs = new ArrayList<>();
		
		/* Define allocator
		 * -------------------------------------------------- */
		
		// Define the allocation function
		Consumer<List<TaskExecution>> allocateTasks = (unallocTaskExecs) -> {
			// Filter the list of unallocated task executions to only those in
			// the current range.
			TemporalList<TaskExecution> taskExecListTmprl = new TemporalList<>(unallocTaskExecs);
			List<TaskExecution> unallocTaskExecsBefore = taskExecListTmprl.getBefore(
				endTime, Event.byStartTime, true
			);

			// Try allocating each task execution
			for (TaskExecution unallocTaskExec : unallocTaskExecsBefore) {
				// If there is a constraint, use it, otherwise use all caretakers
				User allocConst = unallocTaskExec.getTask().getAllocationConstraint();
				List<User> candidates;
				if (allocConst != null) {
					candidates = listOfSingleItem(allocConst);
				} else {
					candidates = allCaretakers;
				}
				
				// Find the earliest viable time between the candidate users
				// (earliest start time, first user available/in the list).
				Duration unallocTaskExecLength = unallocTaskExec.getPeriod().duration();
				
				User bestCandidate = null; // No candidate slots
				LocalDateTime bestCandidateStart = null;
				for (User candidate : candidates) {
					// Note: Allocated tasks MUST have an end time - it's only
					//       optional for unallocated tasks. [FIXME?]
					List<TaskExecution> allocToUserBefore =
						allocCaretakerTasksTmprl.get(candidate).getBefore(
							endTime, Event.byEndTime, true
						);
					
					// Filtered the list by end time != sorting it by end time
					Collections.sort(allocToUserBefore, Event.byEndTime);
					
					// Search this user's schedule - are there any gaps large
					// enough for this task?
					// Note 1: Each end time is a potential point that another
					//         task could be slotted in, so go through them in
					//         order (of end time).
					// Note 2: This algorithm doesn't do any shifting of tasks
					//         to make space for other tasks.
					// Note 3: allocTaskExec is guaranteed to be an exclusive
					//         list, by the definition of 'exclusive' in
					//         ExclusiveTemporalMap (ie. periods don't overlap).

					for (int i = 1; i < allocToUserBefore.size()-1; i++) {
						LocalDateTime prevEnd = allocToUserBefore.get(i-1).getPeriod().end();
						LocalDateTime nextStart = allocToUserBefore.get(i).getPeriod().start();
						Duration gap = Duration.between(prevEnd, nextStart);
						
						// If this throws NullPointerException, I will be :(
						if (
								// Is the gap big enough?
								// TODO: The task exec length should be determined
								//       by the duration it would take *this*
								//       caretaker - not the period constraint.
								gap.compareTo(unallocTaskExecLength) >= 0 &&
								
								// Found a slot! Is it the best slot so far?
								bestCandidateStart.isAfter(prevEnd)
						) {
							bestCandidate = candidate;
							bestCandidateStart = prevEnd;
							break; // Next candidate
						}
					}
				}
				
				if (bestCandidate != null) {
					// If we've found a best candidate, then allocate it to that
					// user and set its period.
					unallocTaskExec.setAllocation(bestCandidate);
					unallocTaskExec.setPeriod(
						new Period(bestCandidateStart, unallocTaskExecLength)
					);

					// Add to the list to flush to DB
	        		allNewExecs.add(unallocTaskExec);
				}
				// else, the task can't be allocated (using this algo)
			}
        };

		/* Split by priority and do allocation
		 * -------------------------------------------------- */

		// Split unallocated tasks by priority
        Map<TaskPriority, List<TaskExecution>> splitMap = new HashMap<>();
        for (TaskPriority tp : TaskPriority.values()) {
            splitMap.put(tp, new ArrayList<>());
        }
        for(TaskExecution taskExec : unallocList) {
    		splitMap.get(taskExec.getPriority()).add(taskExec);
        }

        // Allocate for each task priority, in order
        allocateTasks.accept(splitMap.get(TaskPriority.HIGH));
        allocateTasks.accept(splitMap.get(TaskPriority.NORMAL));
        allocateTasks.accept(splitMap.get(TaskPriority.LOW));

		// Flush to DB
		db.submitTaskExecutions(allNewExecs);
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

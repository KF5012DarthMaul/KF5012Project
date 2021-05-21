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
import java.util.function.Function;
import java.util.function.Predicate;
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
				// If it's already been completed, we don't need to avoid it
				// when allocating
				if (taskExec.getCompletion() != null) {
					allocList.add(taskExec);
				}
			} else {
				// If it's already been completed, we don't need to allocate it
				if (taskExec.getCompletion() == null) {
					unallocList.add(taskExec);
				}
			}
		}
		
		LocalDateTime allocStartTime = LocalDateTime.now();
		LocalDateTime allocEndTime = lsteEndTime.getObject();

		/* Initialise key variables (mutated over time)
		 * -------------------------------------------------- */
		
		// Split allocated task executions by caretaker
		Map<User, List<TaskExecution>> allocCaretakerTasks = new HashMap<>();
		for (User user : allCaretakers) {
			allocCaretakerTasks.put(user, new ArrayList<>());
		}
		for (TaskExecution taskExec : allocList) {
			allocCaretakerTasks.get(taskExec.getAllocation()).add(taskExec);
		}

		// Make temporal lists from these (will sort them by start date)
		Map<User, TemporalList<TaskExecution>> allocCaretakerTasksTmprl = new HashMap<>();
		for (User user : allCaretakers) {
			allocCaretakerTasksTmprl.put(user, new TemporalList<>(allocCaretakerTasks.get(user)));
		}

		// Build the list of tasks to allocate (then allocate them at the end)
		List<TaskExecution> allNewAllocs = new ArrayList<>();
		
		/* Define allocator
		 * -------------------------------------------------- */
		
		// Define the allocation function (encloses variables above)
		Consumer<List<TaskExecution>> allocateTasks = (unallocTaskExecs) -> {
			// Filter the list of uncompleted/unallocated task executions to
			// only those in the current range.
			TemporalList<TaskExecution> taskExecListTmprl = new TemporalList<>(unallocTaskExecs);
			List<TaskExecution> unallocTaskExecsBefore = taskExecListTmprl.getBefore(
				allocEndTime, Event.byStartTime, true
			);

			// Try allocating each task execution
			for (TaskExecution unallocTaskExec : unallocTaskExecsBefore) {
				/* Check constraints
				 * -------------------- */
				
				// If there is a constraint, use it, otherwise check all caretakers
				User allocConst = unallocTaskExec.getTask().getAllocationConstraint();
				List<User> candidates;
				if (allocConst != null) {
					candidates = listOfSingleItem(allocConst);
				} else {
					candidates = allCaretakers;
				}

				// Get the duration of this task exec; constant for all users
				// for now ...
				// TODO: The task exec length should be determined by the
				//       duration it would take a particular caretaker - not the
				//       time allocation constraint of the task. Anywhere this
				//       variable is used may need fixing if this todo is ever
				//       done.
				// WARNING: MAY BE NULL
				Duration unallocTaskExecLength = unallocTaskExec.getPeriod().duration();

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
					if (
							// Is the gap big enough?
							gapLen.compareTo(unallocTaskExecLength) >= 0 &&
							
							// Found a slot! Is it the best slot so far?
							(
							bestCandidate.candidate == Candidate.NO_CANDIDATE ||
							c.startTime().isBefore(bestCandidate.candidate.startTime())
							)
					) {
						return new Candidate(
							c.user(),
							c.startTime(),
							c.startTime().plus(unallocTaskExecLength)
						);
					}
					return Candidate.NO_CANDIDATE;
				};

				/* Find the best candidate user + period to allocate
				 * -------------------- */

				for (User candidateUser : candidates) {
					// Just declare this here
					Candidate possiblyBestCandidate;
					
					// Filter the list to get all allocations between the
					// allocation start and end times (ie. now and the time the
					// user selected).
					List<TaskExecution> allocToUserBetween =
						allocCaretakerTasksTmprl.get(candidateUser).getBetween(
							allocStartTime, allocEndTime,
							
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
							new Candidate(candidateUser, allocStartTime, allocEndTime)
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
							candidateUser,
							allocStartTime,
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
							candidateUser,
							allocToUserBetween.get(i).getPeriod().end(),
							allocEndTime
						)
					);
					if (possiblyBestCandidate != Candidate.NO_CANDIDATE) {
						bestCandidate.candidate = possiblyBestCandidate;
					}
				}

				/* If there is a best candidate, allocate it
				 * -------------------- */
				
				if (bestCandidate.candidate != Candidate.NO_CANDIDATE) {
					// If we've found a best candidate, then allocate it to that
					// user and set its period.
					// NOTE: MUTABLE OPERATION.
					unallocTaskExec.setAllocation(bestCandidate.candidate.user());
					unallocTaskExec.setPeriod(
						new Period(bestCandidate.candidate.startTime(), unallocTaskExecLength)
					);

					// Add to the list of task execs to flush to DB
	        		allNewAllocs.add(unallocTaskExec);
				}
				// else, the task can't be allocated (using this algo)
			}
        };

		/* Split by priority and do allocation
		 * -------------------------------------------------- */

		// Split unallocated tasks by priority
        Map<TaskPriority, List<TaskExecution>> unallocPriorityTasks = new HashMap<>();
        for (TaskPriority tp : TaskPriority.values()) {
            unallocPriorityTasks.put(tp, new ArrayList<>());
        }
        for(TaskExecution taskExec : unallocList) {
    		unallocPriorityTasks.get(taskExec.getPriority()).add(taskExec);
        }

        // Allocate for each task priority, in order
        allocateTasks.accept(unallocPriorityTasks.get(TaskPriority.HIGH));
        allocateTasks.accept(unallocPriorityTasks.get(TaskPriority.NORMAL));
        allocateTasks.accept(unallocPriorityTasks.get(TaskPriority.LOW));

		// Flush to DB
		db.submitTaskExecutions(allNewAllocs);
	}
	
	/**
	 * Class for allocation candidates.
	 * 
	 * @author William Taylor
	 */
	private static class Candidate {
		public static final Candidate NO_CANDIDATE = new Candidate(null, null, null);
		
		private User user;
		private LocalDateTime startTime;
		private LocalDateTime endTime;
		
		/**
		 * Create a candidate
		 * 
		 * @param user The candidate user for allocation.
		 * @param startTime The candidate user's earliest start time for
		 * allocation.
		 * @param endTime The end time for allocation.
		 */
		public Candidate(
				User user,
				LocalDateTime startTime,
				LocalDateTime endTime
		) {
			assert user != null;
			assert startTime != null;
			assert endTime != null;
			
			this.user = user;
			this.startTime = startTime;
			this.endTime = endTime;
		}
		
		public User user() { return user; }
		public LocalDateTime startTime() { return startTime; }
		public LocalDateTime endTime() { return endTime; }
	}

	/**
	 * Wrapper class to allow changeable candidate to be used in lambda functions.
	 * 
	 * @author William Taylor
	 */
	public static class CandidateContainer {
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

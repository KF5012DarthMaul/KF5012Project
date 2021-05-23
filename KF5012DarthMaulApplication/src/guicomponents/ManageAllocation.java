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
	/**
	 * Create the panel.
	 */
	public ManageAllocation() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		GenerateTasks generationPanel = new GenerateTasks();
		tabbedPane.addTab("Task Generation", null, generationPanel, null);
		
		AllocateTasks allocationPanel = new AllocateTasks();
		tabbedPane.addTab("Task Allocation", null, allocationPanel, null);
	}
}

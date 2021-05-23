package guicomponents;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

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

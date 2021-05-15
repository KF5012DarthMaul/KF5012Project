package guicomponents;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;

@SuppressWarnings("serial")
public class ManageAllocation extends JPanel {
	/**
	 * Create the panel.
	 */
	public ManageAllocation() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		JPanel generationPanel = new JPanel();
		tabbedPane.addTab("Task Generation", null, generationPanel, null);
		generationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel allocationPanel = new JPanel();
		tabbedPane.addTab("Task Allocation", null, allocationPanel, null);
	}
}

package guicomponents;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import guicomponents.ome.LocalDateTimeEditor;

@SuppressWarnings("serial")
public class AllocateTasks extends JPanel {
	private LocalDateTimeEditor ldteEndTime;
	private JList<Object> allocatedList;
	private JList<Object> unallocatedList;
	
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
		
		ldteEndTime = new LocalDateTimeEditor();
		GridBagConstraints gbc_ldteEndTime = new GridBagConstraints();
		gbc_ldteEndTime.anchor = GridBagConstraints.WEST;
		gbc_ldteEndTime.insets = new Insets(5, 5, 5, 5);
		gbc_ldteEndTime.gridx = 1;
		gbc_ldteEndTime.gridy = 0;
		add(ldteEndTime, gbc_ldteEndTime);
		
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
}

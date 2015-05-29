package gui;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import master.Worker;
import master.Master;

public class WorkerTable extends JPanel {

	private static final long serialVersionUID = 3524291560512364688L;
	ArrayList<Worker> workers;
	String[] columnNames = {"Address", "Port", "Running?"};
	Object[][] data = new Object[50][3];
	JTable table;
	
	public WorkerTable(Master master) {
		workers = master.getWorkers();
		
		data[0] = new Object[]{"No workers", "N/A", "N/A"};
		table = new JTable(data, columnNames);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
        
		JFrame frame = new JFrame("WorkersTable");
		
        //Create and set up the content pane.
        WorkerTable newContentPane = this;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}

	public void updateTable() {
		int count = 0;
		for (Worker w : workers) {
			String tableRunning = "";
			if (w.isRunning() == true) {
				tableRunning = "Yes";
			} else {
				tableRunning = "No";
			}
			data[count] = new Object[]{w.getAddress(), w.getPort(), tableRunning};
			count++;
		}
		table.repaint();
	}
}

package gui;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import master.Job;
import master.Master;

public class JobTable extends JPanel {

	private static final long serialVersionUID = 5099368265902004685L;
	ArrayList<Job> jobs;
	String[] columnNames = {"Job Name", "Job ID", "Status"};
	String[][] data = {{"No jobs", "N/A", "N/A"}};
	
	public JobTable(Master master) {
		jobs = master.getJobs();
		
		final JTable table = new JTable(data, columnNames);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setEnabled(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
        
		JFrame frame = new JFrame("JobsTable");
		
		int count = 0;
		String tableStatus = "";
		for (Job j : jobs) {
			int status = j.getStatus();
			if (status == 0) {
				tableStatus = "Disconnected";
			} else if (status == 1) {
				tableStatus = "Running";
			} else if (status == 2) {
				tableStatus = "Finished";
			} else if (status == 3) {
				tableStatus = "Failed";
			}
			data[count] = new String[]{j.getJobName(), j.getId(), tableStatus};
			count++;
		}
		
        //Create and set up the content pane.
        JobTable newContentPane = this;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
}

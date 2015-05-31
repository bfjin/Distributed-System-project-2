/***
 * Subject                      Distributed System
 * Author: 						Bofan Jin, Fei Tang, Kimple Ke, Roger Li
 * Date of last modification: 	31/05/2015
 ***/

package gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import master.Job;
import master.Master;

/**
 * Table listing all jobs. 
 * Based on tutorial provided at 
 * https://docs.oracle.com/javase/tutorial/uiswing/components/table.html
 */
public class JobTable extends JPanel {

	private static final long serialVersionUID = 5099368265902004685L;
	ArrayList<Job> jobs;
	String[] tableColumnNames = {"Job Name", "Job ID", "Status", 
			"Output File"};
	Object[][] tableData = new Object[50][4];
	JTable table;
	
	/**
	 * Constructor for JobTable. 
	 * @param master Contains data about jobs. 
	 */
	public JobTable(Master master) {
		jobs = master.getJobs();
		
		tableData[0] = new Object[]{"No jobs", "N/A", "N/A", "N/A"};
		table = new JTable(new JobTableModel());
		
		// An action which opens the relevant output file.
		Action openOutputFile = new AbstractAction() {

			private static final long serialVersionUID = 8317427055188057245L;

			@Override
			public void actionPerformed(ActionEvent event) {
				if (Integer.parseInt(event.getActionCommand()) < jobs.size()) {
					Job j = jobs.get(Integer.parseInt(event.getActionCommand()));
					if (j.getStatus() == 2) {
						try {
							File f = j.getResultFile();
							java.awt.Desktop.getDesktop().edit(f);
						} catch (IOException e) {
							System.err.println("Failed to open result file");
							e.printStackTrace();
						}
					}
				}
			}
		};
		
		// Creates buttons on the fourth column for opening output file.
		@SuppressWarnings("unused")
		ButtonColumn buttonColumn = new ButtonColumn(table, openOutputFile, 3);
		
		table.setPreferredScrollableViewportSize(new Dimension(600, 150));
        table.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
        
		JFrame frame = new JFrame("JobsTable");
		
        // Create and set up the content pane.
        JobTable newContentPane = this;
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	// Model for the job table.
	class JobTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 172087816491090660L;
		private String[] columnNames = tableColumnNames;
		private Object[][] data = tableData;
		
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

	    public String getColumnName(int col) {
	        return columnNames[col];
	    }
		
	    // Can only interact with the button column.
	    public boolean isCellEditable(int row, int col) {
	        if (col == 3) {
	            return true;
	        } else {
	            return false;
	        }
	    }
	    
		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}
		
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
	}
	
	// Updates table data and redraw the table.
	public void updateTable() {
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
			tableData[count] = new String[]{j.getJobName(), j.getId(), 
					tableStatus, "Open"};
			count++;
		}
		table.repaint();
	}
}

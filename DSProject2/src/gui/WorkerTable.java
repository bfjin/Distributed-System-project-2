/***
 * Subject                      Distributed System
 * Author: 						Bofan Jin, Fei Tang, Kimple Ke, Roger Li
 * Date of last modification: 	31/05/2015
 ***/

package gui;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import master.Master;
import master.Worker;

/**
 * Table listing all workers. 
 * Based on tutorial provided at 
 * https://docs.oracle.com/javase/tutorial/uiswing/components/table.html
 */
public class WorkerTable extends JPanel {

	private static final long serialVersionUID = 3524291560512364688L;
	ArrayList<Worker> workers;
	String[] tableColumnNames = {"Address", "Port", "Running?"};
	Object[][] tableData = new Object[50][3];
	JTable table;
	
	/**
	 * Constructor for WorkerTable.
	 * @param master Contains data about workers. 
	 */
	public WorkerTable(Master master) {
		workers = master.getWorkers();
		
		tableData[0] = new Object[]{"No workers", "N/A", "N/A"};
		table = new JTable(new WorkerTableModel());
		table.setPreferredScrollableViewportSize(new Dimension(600, 150));
        table.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
        
		JFrame frame = new JFrame("WorkersTable");
		
        // Create and set up the content pane.
        WorkerTable newContentPane = this;
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
	}

	// Model for worker table.
	class WorkerTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -6383618404857911463L;
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

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}
		
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
	}
	
	// Update the table data and redraw the table.
	public void updateTable() {
		int count = 0;
		for (Worker w : workers) {
			String tableRunning = "";
			if (w.isRunning() == true) {
				tableRunning = "Yes";
			} else {
				tableRunning = "No";
			}
			tableData[count] = new Object[]{w.getAddress(), w.getPort(), 
					tableRunning};
			count++;
		}
		table.repaint();
	}
}

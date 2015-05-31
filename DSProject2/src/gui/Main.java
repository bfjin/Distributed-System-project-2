/***
 * Subject                      Distributed System
 * Author: 						Bofan Jin, Fei Tang, Kimple Ke, Roger Li
 * Date of last modification: 	31/05/2015
 ***/

package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import master.Master;

/**
 * Main starts a gui to begin
 * */
public class Main {

	private JFrame frmMasterGui;
	private Master master;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmMasterGui.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		master = new Master();

		frmMasterGui = new JFrame();
		frmMasterGui.setTitle("Master GUI");
		frmMasterGui.setBounds(100, 100, 500, 200);
		frmMasterGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton btnAddAJob = new JButton("Add a Job");
		btnAddAJob.setPreferredSize(new Dimension(180, 25));
		btnAddAJob.setMinimumSize(new Dimension(180, 25));
		btnAddAJob.setMaximumSize(new Dimension(180, 25));
		btnAddAJob.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddAJob.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddJobDialog dialog = new AddJobDialog(master);
				dialog.pack();
				dialog.setLocationRelativeTo(frmMasterGui);
				dialog.setVisible(true);
			}
		});
		frmMasterGui.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));
		frmMasterGui.getContentPane().add(btnAddAJob);

		JButton btnAddWorker = new JButton("Add a Worker");
		btnAddWorker.setPreferredSize(new Dimension(180, 25));
		btnAddWorker.setMinimumSize(new Dimension(180, 25));
		btnAddWorker.setMaximumSize(new Dimension(180, 25));
		btnAddWorker.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddWorker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddWorkerDialog dialog = new AddWorkerDialog(master);
				dialog.pack();
				dialog.setLocationRelativeTo(frmMasterGui);
				dialog.setVisible(true);
			}
		});
		frmMasterGui.getContentPane().add(btnAddWorker);

		JButton btnShowJobsTable = new JButton("Show Job Statuses");
		btnShowJobsTable.setPreferredSize(new Dimension(180, 25));
		btnShowJobsTable.setMinimumSize(new Dimension(180, 25));
		btnShowJobsTable.setMaximumSize(new Dimension(180, 25));
		btnShowJobsTable.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnShowJobsTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JobTable jobs = new JobTable(master);
				jobs.updateTable();
				master.setJobTable(jobs);
			}
		});
		frmMasterGui.getContentPane().add(btnShowJobsTable);

		JButton btnShowWorkersTable = new JButton("Show Worker Statuses");
		btnShowWorkersTable.setPreferredSize(new Dimension(180, 25));
		btnShowWorkersTable.setMinimumSize(new Dimension(180, 25));
		btnShowWorkersTable.setMaximumSize(new Dimension(180, 25));
		btnShowWorkersTable.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnShowWorkersTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WorkerTable workers = new WorkerTable(master);
				workers.updateTable();
				master.setWorkerTable(workers);
			}
		});
		frmMasterGui.getContentPane().add(btnShowWorkersTable);
	}
}

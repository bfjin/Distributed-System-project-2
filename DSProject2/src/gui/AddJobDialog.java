package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import master.Master;

public class AddJobDialog extends JDialog {

	private static final long serialVersionUID = -1382469568373074545L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtRunable;
	private JTextField txtInput;
	
	private File runnableFile;
	private File inputFile;
	
	/**
	 * Create the dialog.
	 * @param master 
	 */
	public AddJobDialog(Master master) {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			{
				JLabel lblRunnable = new JLabel("Runnable File: ");
				panel.add(lblRunnable);
			}
			{
				txtRunable = new JTextField();
				panel.add(txtRunable);
				txtRunable.setColumns(30);
			}
			{
				JButton btnRunnable = new JButton("Browse");
				btnRunnable.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						fc.setDialogTitle("Select Runnable File For Job");
						fc.setFileFilter(new FileNameExtensionFilter("Java Runnable file (.jar)", "jar"));								
						int returnVal = fc.showOpenDialog(AddJobDialog.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							runnableFile = fc.getSelectedFile();
			                txtRunable.setText(runnableFile.getPath());
						}
					}
				});
				panel.add(btnRunnable);
			}
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			{
				JLabel lblInput = new JLabel("Input File: ");
				panel.add(lblInput);
			}
			{
				txtInput = new JTextField();
				txtInput.setColumns(30);
				panel.add(txtInput);
			}
			{
				JButton btnInput = new JButton("Browse");
				panel.add(btnInput);
				btnInput.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						fc.setDialogTitle("Select Input File For Job");
						int returnVal = fc.showOpenDialog(AddJobDialog.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							inputFile = fc.getSelectedFile();
			                txtInput.setText(inputFile.getPath());
						}
					}
				});
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						master.addJob(runnableFile, inputFile);
						setVisible(false);
						dispose();
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}

}

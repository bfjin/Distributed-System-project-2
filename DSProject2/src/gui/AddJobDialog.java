package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	 * 
	 * @param master
	 */
	public AddJobDialog(Master master) {
		setTitle("Add a Job");
		setBounds(100, 100, 395, 169);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
			{
				JLabel lblRunnable = new JLabel("Runnable File: ");
				lblRunnable.setPreferredSize(new Dimension(85, 15));
				panel.add(lblRunnable);
			}
			{
				txtRunable = new JTextField();
				txtRunable.setColumns(30);
				panel.add(txtRunable);
				txtRunable.setText("wordcount.jar");
			}
			{
				JButton btnRunnable = new JButton("Browse");
				btnRunnable.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						fc.setDialogTitle("Select Runnable File For Job");
						fc.setFileFilter(new FileNameExtensionFilter(
								"Java Runnable file (.jar)", "jar"));
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
			panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
			{
				JLabel lblInput = new JLabel("Input File: ");
				lblInput.setPreferredSize(new Dimension(85, 15));
				panel.add(lblInput);
			}
			{
				txtInput = new JTextField();
				txtInput.setColumns(30);
				panel.add(txtInput);
				txtInput.setText("sample-input.txt");

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
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (runnableFile == null)
							runnableFile = new File(txtRunable.getText());
						if (inputFile == null)
							inputFile = new File(txtInput.getText());
						if (!runnableFile.isFile()
								|| !runnableFile.getName().endsWith(".jar"))
							JOptionPane.showMessageDialog(AddJobDialog.this,
									"A java runnable file is required.",
									"Error", JOptionPane.ERROR_MESSAGE);
						else if (!inputFile.isFile())
							JOptionPane.showMessageDialog(AddJobDialog.this,
									"A input file is required.", "Error",
									JOptionPane.ERROR_MESSAGE);
						else {
							// TODO Job Name
							master.addJob("TODO", runnableFile, inputFile);
							setVisible(false);
							dispose();
						}
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

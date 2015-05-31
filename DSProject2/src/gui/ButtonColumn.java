package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Class for the fourth column of the jobs table, which contains buttons.
 * Based on an example at 
 * https://tips4java.wordpress.com/2009/07/12/table-button-column/
 */
public class ButtonColumn extends AbstractCellEditor implements 
TableCellRenderer, TableCellEditor, ActionListener, MouseListener {

	private static final long serialVersionUID = 6881816954415557080L;
	private JTable table;
	private Action action;
	private Border originalBorder;
	private Border focusBorder;

	private JButton renderButton;
	private JButton editButton;
	private Object editorValue;
	private boolean isButtonColumnEditor;
	
	/**
	 * Constructor for button column.
	 * @param table JTable which the column will be added to
	 * @param action Action to do when button is pressed
	 * @param column Column to add the button to
	 */
	public ButtonColumn(JTable table, Action action, int column) {
		this.table = table;
		this.action = action;
		renderButton = new JButton();
		editButton = new JButton();
		editButton.setFocusPainted(false);
		editButton.addActionListener(this);
		originalBorder = editButton.getBorder();
		setFocusBorder(new LineBorder(Color.BLUE));

		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer(this);
		columnModel.getColumn(column).setCellEditor(this);
		table.addMouseListener(this);
	}

	/**
	 *  Get foreground color of the button when the cell has focus
	 *  @return the foreground color
	 */
	public Border getFocusBorder() {
		return focusBorder;
	}

	/**
	 *  The foreground color of the button when the cell has focus
	 *  @param focusBorder the foreground color
	 */
	public void setFocusBorder(Border focusBorder) {
		this.focusBorder = focusBorder;
		editButton.setBorder(focusBorder);
	}
	
	 // Gets the table cell editor.
	@Override
	public Component getTableCellEditorComponent(
		JTable table, Object value, boolean isSelected, int row, int column) {
		if (value == null) {
			editButton.setText("");
			editButton.setIcon(null);
		} else if (value instanceof Icon) {
			editButton.setText("");
			editButton.setIcon((Icon)value);
		} else {
			editButton.setText(value.toString());
			editButton.setIcon(null);
		}
		this.editorValue = value;
		return editButton;
	}

	// Gets the table cell editor value.
	@Override
	public Object getCellEditorValue() {
		return editorValue;
	}
	
	// Gets the table cell renderer. 
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, 
			boolean hasFocus, int row, int column) {
		if (isSelected) {
			renderButton.setForeground(table.getSelectionForeground());
		 	renderButton.setBackground(table.getSelectionBackground());
		} else {
			renderButton.setForeground(table.getForeground());
			renderButton.setBackground(UIManager.getColor
					("Button.background"));
		}

		if (hasFocus) {
			renderButton.setBorder(focusBorder);
		} else {
			renderButton.setBorder(originalBorder);
		}

		if (value == null) {
			renderButton.setText("");
			renderButton.setIcon(null);
		} else if (value instanceof Icon) {
			renderButton.setText("");
			renderButton.setIcon((Icon)value);
		} else {
			renderButton.setText(value.toString());
			renderButton.setIcon(null);
		}
		return renderButton;
	}

	// Perform action when button is pressed and editing is on.
	public void actionPerformed(ActionEvent e) {
		int row = table.convertRowIndexToModel(table.getEditingRow());
		fireEditingStopped();

		// Call the action when button is pressed. 
		ActionEvent event = new ActionEvent(
			table,
			ActionEvent.ACTION_PERFORMED,
			"" + row);
		action.actionPerformed(event);
	}
	
	// Set editing on when mouse is pressed. 
    public void mousePressed(MouseEvent e) {
    	if (table.isEditing() && table.getCellEditor() == this) {
			isButtonColumnEditor = true;
    	}
    }
    
    // Set editing off when mouse is released. 
    public void mouseReleased(MouseEvent e) {
    	if (isButtonColumnEditor &&  table.isEditing()) {
    		table.getCellEditor().stopCellEditing();
    	}
		isButtonColumnEditor = false;
    }

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}

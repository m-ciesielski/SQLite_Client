package sqliteDB;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

class FileChooser implements Loggable {
	private JFileChooser fileChooser;
	private JDialog fileChooserDialog;
	private JPanel fileChooserPanel;
	private DatabaseOverviewController controller;

	public FileChooser(DatabaseOverviewController controller) {
		if (controller != null)
			this.controller = controller;
		else
			throw new IllegalArgumentException();
	}
	//TODO: update table edition model on database change
	void popupFileChooser() {
		fileChooserPanel = new JPanel();
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter(null, "db"));
		File currentDir = new File(System.getProperty("user.dir"));
		fileChooser.setCurrentDirectory(currentDir);
		fileChooserPanel.add(fileChooser);

		int retVal = fileChooser.showOpenDialog(fileChooserPanel);
		if (retVal == JFileChooser.APPROVE_OPTION
				&& fileChooser.accept(fileChooser.getSelectedFile()) == true) {
			File f = fileChooser.getSelectedFile();
			LOGGER.info("Database connected: "
					+ fileChooser.getSelectedFile().getName());
			try {
				if (dbase.conn != null && dbase.conn.isClosed() == false)
					dbase.conn.close();
				System.out.println(f.getName());
				dbase.conn = dbase.getConnection(f.getName());
				dbase.stat = dbase.conn.createStatement();
				dbase.connected = true;
				AbstractModel.conn = dbase.conn;
				AbstractModel.stat = dbase.stat;
				controller.initialize();
				
			} catch (SQLException e) {
				LOGGER.info("SQL Exception encountered, database connection failed.");
				e.printStackTrace();
			} catch (IOException e) {
				LOGGER.info("IO Exception encountered, database connection failed.");
				e.printStackTrace();
			}

		}
	}
}

class TopMenu {
	private JMenu menu;
	private JMenuBar menuBar;
	private JMenuItem menuItem;

	TopMenu(JFrame f, final DatabaseOverviewController controller) {
		menu = new JMenu("menu");
		menuBar = new JMenuBar();
		menuItem = new JMenuItem("Polacz z baza danych");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new FileChooser(controller).popupFileChooser();
			}
		});
		menu.add(menuItem);
		menuBar.add(menu);
		f.setJMenuBar(menuBar);

	}

}

class SidePanel extends JTabbedPane {
	private JList<String> list;
	private DefaultListModel<String> listModel;
	private JScrollPane listScroll;
	private JPanel tablePanel;
	private JPanel viewPanel;

	public SidePanel(JFrame frame) {
		listModel = new DefaultListModel<String>();
		listScroll = new JScrollPane();
		tablePanel = new JPanel();
		viewPanel = new JPanel();
		this.addTab("Tabele", tablePanel);
		this.addTab("Widoki", viewPanel);
		// this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		frame.add(this, BorderLayout.WEST);
		frame.pack();
	}

	public void addListElement(String arg) {
		listModel.addElement(arg);
	}

	public void createList(String dest) {
		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		listScroll.setMaximumSize(new Dimension(125, 200));
		listScroll.setMinimumSize(new Dimension(125, 200));
		listScroll.setPreferredSize(listScroll.getMaximumSize());
		listScroll.setViewportView(list);
		if (dest == "Table")
			tablePanel.add(listScroll);
		else
			viewPanel.add(listScroll);
		listScroll.revalidate();
	}

	public JList<String> getList() {
		return list;
	}

	public void clearListModel() {
		listModel.clear();
	}

	public void removeListListener() {
		if (list != null) {
			for (int i = 0; i < list.getListSelectionListeners().length; ++i)
				list.removeListSelectionListener(list
						.getListSelectionListeners()[i]);
		}
	}

}

// TODO: create distinc classes for insert/update views
class TablePanel extends JPanel {
	private JTable table;
	private JScrollPane tableScroll;
	private JPanel buttonContainer;
	private JButton newRecordButton;
	private JDialog newRecordDialog;
	private JButton commitButton;
	private JButton deleteButton;
	private JButton filterButton;
	private JDialog filterDialog;
	private JButton databaseConnectionButton;
	private JDialog deleteDialog;
	private JOptionPane deleteOptionPane;
	private JFormattedTextField textFields[];
	private JComboBox comboFields[];
	private TableRowSorter<TableModel> sorter;
	private ArrayList<String> filterStrings;

	public TablePanel(JFrame frame) {
		filterStrings = new ArrayList<String>();
		newRecordButton = new JButton("Dodaj nowy rekord");
		deleteButton = new JButton("Usun zaznaczone rekordy");
		filterButton = new JButton("Filtry");
		this.setLayout(new BorderLayout());
		buttonContainer = new JPanel(new GridLayout());
		buttonContainer.add(filterButton);
		buttonContainer.add(newRecordButton);
		buttonContainer.add(deleteButton);
		this.add(buttonContainer, BorderLayout.NORTH);
		databaseConnectionButton = new JButton("Polacz z baza danych");
		// this.add(databaseConnectionButton, BorderLayout.CENTER);
		frame.add(this);
		frame.pack();
	}

	// getters
	public JButton getNewRecordButton() {
		return newRecordButton;
	}

	public JButton getCommitButton() {
		return commitButton;
	}

	public JButton getDeleteButton() {
		return deleteButton;
	}

	public JButton getFilterButton() {
		return filterButton;
	}

	public JDialog getFilterDialog() {
		return filterDialog;
	}

	public JTable getTable() {
		return table;
	}

	public JScrollPane getTableScroll() {
		return tableScroll;
	}

	public JOptionPane getDeleteOptionPane() {
		return deleteOptionPane;
	}

	public JDialog getDeleteDialog() {
		return deleteDialog;
	}
	
	public JDialog getNewRecordDialog() {
		return newRecordDialog;
	}

	public void removeDatabaseConnectionButton() {
		this.remove(databaseConnectionButton);
	}

	/**
	 * Gets validated values inserted in input text boxes. If inserted value is
	 * invalid, default value for data type is returned.
	 * 
	 * @return values inserted in text fields
	 */
	public ArrayList<Object> getInsertedValues() {
		validateTextFields();
		ArrayList<Object> values = new ArrayList<Object>();
		for (int i = 0; i < textFields.length; ++i) {
			values.add(textFields[i].getValue());
		}
		return values;
	}

	// TODO:remove?
	public boolean insertedValuesAreValid() {
		boolean valid = true;

		for (int i = 0; i < textFields.length; ++i) {
			textFields[i].validate();
			if (!textFields[i].isValid()) {
				valid = false;
				textFields[i].setBackground(Color.RED);
			}

		}

		return valid;

	}

	// TODO: show message box if any vield is unvalid?
	private void validateTextFields() {
		for (int i = 0; i < textFields.length; ++i) {
			textFields[i].validate();
		}
	}

	private String createDeleteConfirmationInfo() {
		StringBuffer buff = new StringBuffer();
		int selectedRows[] = table.getSelectedRows();
		for (int i = 0; i < selectedRows.length; ++i) {
			buff.append((selectedRows[i] + 1) + ", ");
		}
		return buff.toString();
	}

	private void saveFilterStrings(JTextField filterTextFields[]) {
		filterStrings.clear();
		for (int i = 0; i < filterTextFields.length; ++i)
			filterStrings.add(filterTextFields[i].getText());
	}

	private void loadFilterStrings(JTextField filterTextFields[]) {
		if (filterStrings.isEmpty() == true)
			return;
		for (int i = 0; i < filterTextFields.length; ++i)
			filterTextFields[i].setText(filterStrings.get(i));
	}

	private String getDigitsFromFilterString(String arg) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < arg.length(); ++i) {
			char c = arg.charAt(i);
			if (Character.isDigit(c)) {
				buff.append(c);
			}
		}
		return buff.toString();
	}
	//TODO: check if filter dialog is already rendered
	public void popupFilterDialog(final ArrayList<String> columnTypes) {
		filterDialog = new JDialog();

		JPanel textFieldPanel;
		JPanel labelPanel;
		JPanel buttonPanel;
		JButton applyButton;
		JButton cancelButton;
		JButton clearButton;

		labelPanel = new JPanel(new GridLayout(0, 1));
		textFieldPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel = new JPanel(new GridLayout(1, 0));
		applyButton = new JButton("Zatwierdz");
		cancelButton = new JButton("Anuluj");
		clearButton = new JButton("Usun Filtry");

		buttonPanel.add(applyButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(cancelButton);

		final JTextField filterTextFields[];
		JLabel labels[];

		labels = new JLabel[table.getColumnCount()];
		filterTextFields = new JTextField[table.getColumnCount()];

		for (int i = 0; i < table.getColumnCount(); ++i) {
			filterTextFields[i] = new JTextField();
			filterTextFields[i].setPreferredSize(new Dimension(100, 25));
			textFieldPanel.add(filterTextFields[i]);
			labels[i] = new JLabel(table.getColumnName(i) + " :");
			labels[i].setLabelFor(filterTextFields[i]);
			labelPanel.add(labels[i]);
		}
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				filterDialog.setVisible(false);
				filterDialog.dispose();
				filterDialog.dispatchEvent(new WindowEvent(filterDialog,
						WindowEvent.WINDOW_CLOSING));
			}
		});
		clearButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sorter.setRowFilter(null);
				filterStrings.clear();
				for (int i = 0; i < filterTextFields.length; ++i)
					filterTextFields[i].setText(null);
			}
		});
		// TODO: move to distinct method
		applyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < filterTextFields.length; ++i) {
					if (filterTextFields[i].getText().length() > 0) {
						Object type = Query.parseColumnType(columnTypes.get(i));

						// TODO: join string regex filters
						if (type.getClass() == String.class) {
							System.out.println(filterTextFields[i].getText());

							sorter.setRowFilter(RowFilter.regexFilter(
									filterTextFields[i].getText(), i));

						} else if (type.getClass() == Integer.class) {
							System.out.println("int i: " + i);
							if (filterTextFields[i].getText().length() > 0) {
								Integer constraint = Integer
										.valueOf(getDigitsFromFilterString(filterTextFields[i]
												.getText()));

								if (filterTextFields[i].getText().contains(">"))
									sorter.setRowFilter(RowFilter.numberFilter(
											ComparisonType.AFTER,
											constraint.intValue(), i));
								else if (filterTextFields[i].getText()
										.contains("<"))
									sorter.setRowFilter(RowFilter.numberFilter(
											ComparisonType.BEFORE,
											constraint.intValue(), i));
								else if (filterTextFields[i].getText()
										.contains("="))
									sorter.setRowFilter(RowFilter.numberFilter(
											ComparisonType.EQUAL,
											constraint.intValue(), i));
							}

						} else if (type.getClass() == Double.class) {
							System.out.println("double i: " + i);
							if (filterTextFields[i].getText().length() > 0) {
								// System.out.println("bb");
								Double constraint = Double
										.valueOf(getDigitsFromFilterString(filterTextFields[i]
												.getText()));
								// System.out.println("constraint double value "+constraint.doubleValue());
								if (filterTextFields[i].getText().contains(">"))
									sorter.setRowFilter(RowFilter.numberFilter(
											ComparisonType.AFTER,
											constraint.doubleValue(), i));
								else if (filterTextFields[i].getText()
										.contains("<"))
									sorter.setRowFilter(RowFilter.numberFilter(
											ComparisonType.BEFORE,
											constraint.doubleValue(), i));
								else if (filterTextFields[i].getText()
										.contains("="))
									sorter.setRowFilter(RowFilter.numberFilter(
											ComparisonType.EQUAL,
											constraint.doubleValue(), i));
							}

						} else if (type.getClass() == Date.class) {
							String dateString = filterTextFields[i].getText()
									.substring(1);
							Date constraint = Date.valueOf(dateString);
							// System.out.println("constraint double value "+constraint.doubleValue());
							if (filterTextFields[i].getText().contains(">"))
								sorter.setRowFilter(RowFilter.dateFilter(
										ComparisonType.AFTER, constraint, i));
							else if (filterTextFields[i].getText()
									.contains("<"))
								sorter.setRowFilter(RowFilter.dateFilter(
										ComparisonType.BEFORE, constraint, i));
							else if (filterTextFields[i].getText()
									.contains("="))
								sorter.setRowFilter(RowFilter.dateFilter(
										ComparisonType.EQUAL, constraint, i));
						}
					}

				}
				saveFilterStrings(filterTextFields);
				table.setRowSorter(sorter);
				sorter.setSortKeys(null);
				filterDialog.setVisible(false);
				filterDialog.dispose();
				filterDialog.dispatchEvent(new WindowEvent(filterDialog,
						WindowEvent.WINDOW_CLOSING));
			}
		});
		loadFilterStrings(filterTextFields);
		filterDialog.add(textFieldPanel, BorderLayout.EAST);
		filterDialog.add(labelPanel, BorderLayout.WEST);
		filterDialog.add(buttonPanel, BorderLayout.SOUTH);
		filterDialog.setSize(new Dimension(200, 300));
		filterDialog.setLocation(new Point(100, 100));
		filterDialog.pack();
		filterDialog.setVisible(true);
		// this.setEnabled(false);
	}

	public void popupDeleteConfirmationDialog() {
		deleteDialog = new JDialog();
		deleteOptionPane = new JOptionPane(
				"Zostana usuniête rekordy o numerach: \n"
						+ createDeleteConfirmationInfo(),
				JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		deleteDialog.add(deleteOptionPane);
		deleteDialog.setSize(new Dimension(200, 300));
		deleteDialog.setLocation(new Point(100, 100));
		deleteDialog.pack();
		deleteDialog.setVisible(true);
	}

	public void popupNewRecordDialog(ArrayList<String> columnNames,
			ArrayList<String> columnTypes, ArrayList<String> pkColumns) {
		newRecordDialog = new JDialog();
		JPanel textFieldPanel;
		JPanel labelPanel;
		JPanel buttonPanel;
		textFieldPanel = new JPanel(new GridLayout(0, 1));
		labelPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel = new JPanel(new GridLayout(1, 0));
		// JPanel buttonPanel = new JPanel();
		//TODO: change textFields array size and init loop
		textFields = new JFormattedTextField[columnNames.size()];
		//comboFields = new JComboBox[fkColumns.size()];
		JLabel labels[];
		labels = new JLabel[columnNames.size()];

		commitButton = new JButton("Dodaj");
		JButton cancelButton = new JButton("Anuluj");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newRecordDialog.setVisible(false);
				newRecordDialog.dispose();
			}
		});
		// buttonPanel.add(commitButton);
		for (int i = 0; i < columnNames.size(); ++i) {
			
			textFields[i] = new JFormattedTextField();

			// TODO: set pk text field default value as next possible ID
			for (int j = 0; j < pkColumns.size(); ++j)
				if (pkColumns.get(j) == columnNames.get(i))
					textFields[i].setValue(Query.parseColumnType(columnTypes
							.get(i)));

			textFields[i].setValue(Query.parseColumnType(columnTypes.get(i)));
			textFields[i].setPreferredSize(new Dimension(100, 25));
			labels[i] = new JLabel(columnNames.get(i) + " ("
					+ columnTypes.get(i) + " ) " + " :");
			labels[i].setLabelFor(textFields[i]);
			labelPanel.add(labels[i]);
			textFieldPanel.add(textFields[i]);
		}
		buttonPanel.add(commitButton);
		buttonPanel.add(cancelButton);
		newRecordDialog.add(textFieldPanel, BorderLayout.EAST);
		newRecordDialog.add(labelPanel, BorderLayout.WEST);
		newRecordDialog.add(buttonPanel, BorderLayout.SOUTH);
		newRecordDialog.setSize(new Dimension(200, 300));
		newRecordDialog.setLocation(new Point(100, 100));
		newRecordDialog.pack();
		newRecordDialog.setVisible(true);
		// newRecordDialog.setPreferredSize(new Dimension(100,200));
	}
	
	public void popupNewRecordDialog(ArrayList<String> columnNames,
			ArrayList<String> columnTypes, ArrayList<String> pkColumns,
			ArrayList<String> fkColumns) {
		newRecordDialog = new JDialog();
		JPanel textFieldPanel;
		JPanel labelPanel;
		JPanel buttonPanel;
		textFieldPanel = new JPanel(new GridLayout(0, 1));
		labelPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel = new JPanel(new GridLayout(1, 0));
		// JPanel buttonPanel = new JPanel();
		//TODO: change textFields array size and init loop
		textFields = new JFormattedTextField[columnNames.size()];
		comboFields = new JComboBox[fkColumns.size()];
		JLabel labels[];
		labels = new JLabel[columnNames.size()];

		commitButton = new JButton("Dodaj");
		JButton cancelButton = new JButton("Anuluj");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newRecordDialog.setVisible(false);
				newRecordDialog.dispose();
			}
		});
		// buttonPanel.add(commitButton);
		for (int i = 0; i < columnNames.size(); ++i) {
			// TODO: create comboBox for fk fields
			boolean fk=false;
			for (int j = 0; j < fkColumns.size(); ++j)
				if (fkColumns.get(j) == columnNames.get(i)) {
					comboFields[i]=new JComboBox<String>();
					
					labels[i] = new JLabel(columnNames.get(i) + " ("
							+ columnTypes.get(i) + " ) " + " :");
					labels[i].setLabelFor(textFields[i]);
					labelPanel.add(labels[i]);
					
					fk=true;
				}
			
			if(fk==true)
				continue;
			
			textFields[i] = new JFormattedTextField();

			// TODO: set pk text field default value as next possible ID
			for (int j = 0; j < pkColumns.size(); ++j)
				if (pkColumns.get(j) == columnNames.get(i))
					textFields[i].setValue(Query.parseColumnType(columnTypes
							.get(i)));

			textFields[i].setValue(Query.parseColumnType(columnTypes.get(i)));
			textFields[i].setPreferredSize(new Dimension(100, 25));
			labels[i] = new JLabel(columnNames.get(i) + " ("
					+ columnTypes.get(i) + " ) " + " :");
			labels[i].setLabelFor(textFields[i]);
			labelPanel.add(labels[i]);
			textFieldPanel.add(textFields[i]);
		}
		buttonPanel.add(commitButton);
		buttonPanel.add(cancelButton);
		newRecordDialog.add(textFieldPanel, BorderLayout.EAST);
		newRecordDialog.add(labelPanel, BorderLayout.WEST);
		newRecordDialog.add(buttonPanel, BorderLayout.SOUTH);
		newRecordDialog.setSize(new Dimension(200, 300));
		newRecordDialog.setLocation(new Point(100, 100));
		newRecordDialog.pack();
		newRecordDialog.setVisible(true);
		// newRecordDialog.setPreferredSize(new Dimension(100,200));
	}

	/**
	 * This method creates new table view based on array of table data and
	 * column names
	 * 
	 * @param data
	 *            table data in object arrays
	 * @param columnNames
	 *            table column names
	 * @deprecated
	 */

	public void createTable(Object[][] data, Object[] columnNames) {
		table = new JTable(data, columnNames);
		table.setPreferredScrollableViewportSize(new Dimension(500, 200));
		table.setFillsViewportHeight(true);
		tableScroll = new JScrollPane(table);
		this.add(tableScroll);
		tableScroll.repaint();
		revalidate();
	}

	/**
	 * This method creates new table view with scroll pane and row sorter
	 * 
	 * @param editionModel
	 *            model of table
	 */
	public void createTable(TableEditionModel editionModel) {
		filterStrings.clear();
		//delete old table?
		
		table = new JTable(editionModel);
		sorter = new TableRowSorter<TableModel>(editionModel);
		table.setRowSorter(sorter);
		table.setPreferredScrollableViewportSize(new Dimension(500, 500));
		table.setFillsViewportHeight(true);
		//this.remove(tableScroll);
		tableScroll = new JScrollPane(table);
		this.add(tableScroll);
		tableScroll.repaint();
		revalidate();
	}

	public int getColumnIndex(String columnName) {
		for (int i = 0; i < table.getModel().getColumnCount(); ++i) {
			System.out.println(table.getModel().getColumnName(i));
			if (table.getModel().getColumnName(i).toLowerCase()
					.equals(columnName)) // conversion to lower case
				return i;
		}

		return -1;
	}

}

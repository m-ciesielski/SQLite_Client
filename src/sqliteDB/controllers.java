package sqliteDB;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

/**
 * Interface of controller responsible for updating names of tables and views.
 * 
 * @author Owen
 *
 */
interface DatabaseOverviewController {
	void updateTableNames();

	void updateViewNames();

	void initialize();
}

class SideController implements Loggable, DatabaseOverviewController {
	private SidePanelModel model;
	private SidePanel view;
	private TableController tableController;

	/**
	 * This constructor creates side panel controller and links it with side
	 * panel model, side panel view and table controller
	 * 
	 * @param model
	 *            Model of side panel
	 * @param view
	 *            view of side panel
	 * @param tableController
	 *            controller of table
	 * 
	 */
	public SideController(SidePanelModel model, SidePanel view,
			TableController tableController) {
		this.model = model;
		this.view = view;
		this.tableController = tableController;
	}

	public void initialize() {
		updateTableNames();
		tableController.clearView();
		tableController.setModelTableName(model.getTableName(0));
		tableController.createTable();
		addSidePanelTabListener();
	}

	/**
	 * This method updates names of database tables in side panel model and view
	 */
	public void updateTableNames() {
		view.removeListListener();
		model.clearTableNames();
		view.clearListModel();
		model.fetchTableNames();
		ArrayList<String> tableNames = model.getTableNames();
		for (int i = 0; i < tableNames.size(); ++i)
			view.addListElement(tableNames.get(i));

		view.createList("Table");
		addSidePanelListListener();
	}

	/**
	 * This method updates names of database views in side panel model and view
	 */
	public void updateViewNames() {
		view.removeListListener(); // remove side panel view listener
		model.clearViewNames();
		view.clearListModel();
		model.fetchViewNames();
		ArrayList<String> viewNames = model.getViewNames();
		for (int i = 0; i < viewNames.size(); ++i)
			view.addListElement(viewNames.get(i));
		view.createList("View");
		addSidePanelListListener();
	}

	private void addSidePanelListListener() {
		view.getList().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) // Sprawdzenie w celu pominiecia
												// duplikowanych wydarzen
				{
					ListSelectionModel lsm = view.getList().getSelectionModel();
					if (view.getSelectedIndex() == 0) {
						//TODO: close new record dialog on table change
						
						tableController.clearView();
						tableController.setModelTableName(model
								.getTableName(lsm.getMinSelectionIndex()));
						tableController.createTable();
					} else {
						tableController.clearView();
						tableController.setModelTableName(model.getViewName(lsm
								.getMinSelectionIndex()));
						tableController.createTable();
					}
				}
			}
		});
	}

	void addSidePanelTabListener() {
		view.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println(view.getSelectedIndex());
				if (view.getSelectedIndex() == 0) {
					updateTableNames();
					tableController.setMode(0);
				} else if (view.getSelectedIndex() == 1) {
					updateViewNames();
					tableController.setMode(1);
				}
			}
		});
	}
}

interface TableController {
	void createTable();

	void setMode(int i);

	void setModelTableName(String name);

	void clearView();
}

class DefaultTableController implements Loggable, TableController {
	private DefaultTableModel model;
	private TablePanel view;
	private TableEditionModel editionModel;

	private int mode; // 0=tabela, 1=widok

	public DefaultTableController(DefaultTableModel model, TablePanel view) {
		this.model = model;
		this.view = view;
		mode = 0;
		initializeViewListeners();
	}

	public void setMode(int mode) {
		if (mode == 0 || mode == 1)
			this.mode = mode;
	}

	private void initializeViewListeners() {
		addNewRecordButtonListener(view.getNewRecordButton());
		addDeleteButtonListener(view.getDeleteButton());
		addFilterButtonListener();
	}

	// TODO: complete rework of controller INSERT logic ()
	// TODO: pass referenced tables from model to view new record method
	private void addNewRecordButtonListener(JButton insertButton) {
		insertButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (dbase.connected
						&& mode == 0

						&& (view.getNewRecordDialog() == null || view
								.getNewRecordDialog().isVisible() == false)) {
					view.popupNewRecordDialog(model.getColumnNames(),
							model.getColumnClasses(),
							model.getPrimaryKeyColumns());
					addCommitButtonListener(view.getCommitButton());
				}

			}
		});
	}

	private void addCommitButtonListener(JButton commitButton) {
		commitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: check if inserted values are valid before constructing
				// query
				// TODO:remove?
				if (view.insertedValuesAreValid()) {
					Query insertQuery = new Query(Query.MainStatement.INSERT,
							model.getTableName(), model.getColumnNames(), view
									.getInsertedValues());
					try {
						model.executeQuery(insertQuery.createQueryString());
					} catch (SQLException e1) {
						JOptionPane.showMessageDialog(null,
								"Wartoœci dodawanego rekordu s¹ niepoprawne.");
						e1.printStackTrace();
					}
					clearView();
					createTable();
					view.revalidate();
					view.repaint();
				} else {

				}

			}
		});
	}

	private int[] getCompositePrimaryKeys() {
		int PKCount = model.getPrimaryKeyColumnsCount(); // primary key columns
															// count

		int[] PKColumnIndex; // indices of primary key columns
		PKColumnIndex = new int[PKCount];
		for (int i = 0; i < PKCount; ++i) {
			PKColumnIndex[i] = view.getColumnIndex(model.getPrimaryKeyColumns()
					.get(i));
			if (PKColumnIndex[i] == -1) {
				LOGGER.info("Error: unable to assign index of: "
						+ model.getPrimaryKeyColumns().get(i)
						+ " primary key column.");
				return null;
			}
		}

		int[] compositePK; // array of composite primary key values
		compositePK = new int[view.getTable().getSelectedRowCount() * PKCount];

		TableModel tabModel = view.getTable().getModel(); // reference to
															// tableModel of
															// table
		int[] selectedRows;
		selectedRows = view.getTable().getSelectedRows(); // selected rows array

		int k = 0;
		for (int i = 0; i < selectedRows.length; ++i) {
			for (int j = 0; j < PKCount; ++j) {
				// System.out.println("selectedRows[i]="+selectedRows[i]);
				// System.out.println("PKColumnIndex[j]="+PKColumnIndex[j]);
				compositePK[k] = (int) tabModel.getValueAt(selectedRows[i],
						PKColumnIndex[j]);
				++k;
			}
		}

		return compositePK;
	}

	private void addDeleteOptionPaneListener(final JOptionPane optionPane) {
		optionPane.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				int value = ((Integer) optionPane.getValue()).intValue();
				if (value == JOptionPane.OK_OPTION) {

					Query deleteQuery = new Query(Query.MainStatement.DELETE,
							model.getTableName(), model.getPrimaryKeyColumns(),
							getCompositePrimaryKeys());
					if (deleteQuery != null)
						try {
							model.executeQuery(deleteQuery.createQueryString());
						} catch (SQLException e) {
							JOptionPane.showMessageDialog(null,
									"Usuwanie nie powiod³o siê.");
							e.printStackTrace();
						}
					clearView();
					createTable();
					view.revalidate();
					view.repaint();
					view.getDeleteDialog().dispatchEvent(
							new WindowEvent(view.getDeleteDialog(),
									WindowEvent.WINDOW_CLOSING));

				} else if (value == JOptionPane.CANCEL_OPTION) {
					// view.getDeleteConfirmationDialog().setVisible(false);
					view.getDeleteDialog().dispatchEvent(
							new WindowEvent(view.getDeleteDialog(),
									WindowEvent.WINDOW_CLOSING));
				}

			}
		});
	}

	private void addDeleteButtonListener(JButton deleteButton) {
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (dbase.connected
						&& view.getTable().getSelectedRows().length > 0) {
					if (mode == 0) {
						view.popupDeleteConfirmationDialog();
						addDeleteOptionPaneListener(view.getDeleteOptionPane());
					}

				}

			}
		});
	}

	private Object[][] tableDataToArray(ArrayList<ArrayList<Object>> data) {
		Object array[][];
		array = new Object[data.size()][];

		for (int i = 0; i < data.size(); ++i)
			array[i] = data.get(i).toArray();

		return array;
	}

	private String[] columnNamesToArray(ArrayList<String> columns) {
		String array[];
		array = new String[columns.size()];
		array = columns.toArray(array);
		return array;
	}

	public void clearView() {
		if(view.getTableScroll()!=null)
			view.remove(view.getTableScroll());
		view.revalidate();
	}

	public void createTable() {
		editionModel = new TableEditionModel(
				tableDataToArray(model.getTableData()),
				columnNamesToArray(model.getColumnNames()), model);
		view.createTable(editionModel);
		LOGGER.info("Created view of table: " + model.getTableName());
	}

	public void addFilterButtonListener() {
		view.getFilterButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (dbase.connected && (view.getFilterDialog() == null || view
						.getFilterDialog().isVisible() == false))
					view.popupFilterDialog(model.getColumnClasses());
			}
		});
	}

	public void setModelTableName(String tableName) {
		// delete unclosed filter view
		if (view.getFilterDialog() != null)
			view.getFilterDialog().dispatchEvent(
					new WindowEvent(view.getFilterDialog(),
							WindowEvent.WINDOW_CLOSING));

		model.setTableName(tableName);
		LOGGER.info("Model table name set to: " + model.getTableName());
	}
}

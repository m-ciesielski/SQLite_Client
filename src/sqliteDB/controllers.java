package sqliteDB;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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


interface DatabaseOverviewController{
	void updateTableNames();
	void updateViewNames();
	void initialize();
}
class SideController implements Loggable, DatabaseOverviewController{
	private SidePanelModel model;
	private SidePanel view;
	private TableController tableController;
	/**
	 * This constructor creates side panel controller and links it with side panel model, side panel view
	 * and table controller
	 * @param model Model of side panel
	 * @param view view of side panel
	 * @param tableController controller of table
	 * 
	 */
	public SideController(SidePanelModel model, SidePanel view,TableController tableController) {
		this.model=model;
		this.view=view;
		this.tableController=tableController;
	}
	
	public void initialize(){
		updateTableNames();
		tableController.setModelTableName(model.getTableName(0));
		tableController.createTable();
		addSidePanelTabListener();
	}
	
	
	/**
	 * This method updates names of database tables in side panel model and view
	 */
	public void updateTableNames(){
		view.removeListListener();
		model.clearTableNames();
			view.clearListModel();
			model.fetchTableNames();
			ArrayList<String> tableNames=model.getTableNames();
			for(int i=0;i<tableNames.size();++i)
				view.addListElement(tableNames.get(i));
				
		view.createList("Table");
		addSidePanelListListener();
	}
	/**
	 * This method updates names of database views in side panel model and view
	 */
	public void updateViewNames(){
		view.removeListListener(); //remove side panel view listener
		model.clearViewNames();
		view.clearListModel();
		model.fetchViewNames();
		ArrayList<String> viewNames=model.getViewNames();
		for(int i=0;i<viewNames.size();++i)
			view.addListElement(viewNames.get(i));
		view.createList("View");
		addSidePanelListListener();
	}
	
	private void addSidePanelListListener(){
		view.getList().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) // Sprawdzenie w celu pominiecia duplikowanych wydarzen
				{
					ListSelectionModel lsm = view.getList().getSelectionModel();
					if(view.getSelectedIndex()==0){
							
							tableController.clearView();
							tableController.setModelTableName(model.getTableName(lsm.getMinSelectionIndex()));
							tableController.createTable();		
					}
					else{
						tableController.clearView();
						tableController.setModelTableName(model.getViewName(lsm.getMinSelectionIndex()));
						tableController.createTable();	
					}
				}
			}
		});
	}
	void addSidePanelTabListener(){
		view.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println(view.getSelectedIndex());
				if(view.getSelectedIndex()==0)
				{
					updateTableNames();
					tableController.setMode(0);
				}
				else if(view.getSelectedIndex()==1){
					updateViewNames();
					tableController.setMode(1);
				}
			}
		});
	}
}

interface TableController{
	void createTable();
	void setMode(int i);
	void setModelTableName(String name);
	void clearView();
}

class DefaultTableController implements Loggable, TableController{
	private DefaultTableModel model;
	private TablePanel view;
	private TableEditionModel editionModel;
	private int mode; //0=tabela, 1=widok
	public DefaultTableController(DefaultTableModel model, TablePanel view) {
		this.model=model;
		this.view=view;
		mode=0;
		initializeViewListeners();
	}
	
	public void setMode(int mode){this.mode=mode;}
	
	private void initializeViewListeners(){
		addNewRecordButtonListener(view.getNewRecordButton());
		addDeleteButtonListener(view.getDeleteButton());
		addFilterButtonListener();
	}
	
	private void addNewRecordButtonListener(JButton insertButton){
		insertButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mode==0)
				{
					view.createNewRecordDialog(model.getColumnNames(), model.getColumnClasses());
					addCommitButtonListener(view.getCommitButton());	
				}
				
			}
		});
	}
	
	private void addCommitButtonListener(JButton commitButton){
		commitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Query insertQuery=new Query(Query.MainStatement.INSERT, model.getTableName(),
						model.getColumnNames(), null, view.getInsertedValues());
				model.executeQuery(insertQuery.createQueryString());
				clearView();
				createTable();
				view.revalidate();
				view.repaint();
			}
		});
	}
	
	private int[] getCompositePrimaryKeys(){
		int PKCount=model.getPrimaryKeyColumnsCount(); //primary key columns count
		
		int [] PKColumnIndex; //indices of primary key columns
		PKColumnIndex= new int [PKCount]; 
		for(int i=0;i<PKCount;++i)
		{
			PKColumnIndex[i]=view.getColumnIndex(model.getPrimaryKeyColumns().get(i));
			if(PKColumnIndex[i]==-1)
			{
				LOGGER.info("Error: unable to assign index of: "+model.getPrimaryKeyColumns().get(i)+" primary key column.");
				return null;
			}
		}
			
		
		int[] compositePK; //array of composite primary key values
		compositePK= new int [view.getTable().getSelectedRowCount()*PKCount];

		TableModel tabModel=view.getTable().getModel(); //reference to tableModel of table
		int[] selectedRows;
		selectedRows=view.getTable().getSelectedRows(); // selected rows array
		
		int k=0;
		for(int i=0;i<selectedRows.length;++i)
		{
			for(int j=0;j<PKCount;++j)
			{
				System.out.println("selectedRows[i]="+selectedRows[i]);
				System.out.println("PKColumnIndex[j]="+PKColumnIndex[j]);
				compositePK[k]=(int) tabModel.getValueAt(selectedRows[i], PKColumnIndex[j]);
				++k;
			}
		}
		
		return compositePK;
	}
	
	private void addDeleteConfirmationOptionPaneListener(final JOptionPane optionPane){
		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				int value = ((Integer)optionPane.getValue()).intValue();
				if (value == JOptionPane.OK_OPTION) {
					Query deleteQuery=null;
					//if(model.getPrimaryKeyColumnsCount()<2) //standard delete when primary key is singular
				  //  deleteQuery=new Query(Query.MainStatement.DELETE, model.getTableName(),
				  //  		model.getPrimaryKeyColumns(), view.getTable().getSelectedRows());
					//else //composite primary keys deletion
						deleteQuery=new Query(Query.MainStatement.DELETE, model.getTableName(),
					    		model.getPrimaryKeyColumns(), getCompositePrimaryKeys());
						if(deleteQuery!=null)
				    model.executeQuery(deleteQuery.createQueryString());
				    clearView();
					createTable();
					view.revalidate();
					view.repaint();
					view.getDeleteConfirmationDialog().dispatchEvent(new WindowEvent(
		                    view.getDeleteConfirmationDialog(), WindowEvent.WINDOW_CLOSING));
				    
				} else if (value == JOptionPane.CANCEL_OPTION) {
				   // view.getDeleteConfirmationDialog().setVisible(false);
				    view.getDeleteConfirmationDialog().dispatchEvent(new WindowEvent(
		                    view.getDeleteConfirmationDialog(), WindowEvent.WINDOW_CLOSING));
				}
				
			}
		});
	}
	
	private void addDeleteButtonListener(JButton deleteButton){
		deleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(view.getTable().getSelectedRows().length>0)
				{
					if(mode==0)
					{
						view.popupDeleteConfirmationDialog();
						addDeleteConfirmationOptionPaneListener(view.getDeleteConfirmationOptionPane());		
					}
					
				}
				
			}
		});
	}
	
	
	private Object[][] tableDataToArray(ArrayList<ArrayList<Object>> arg){
		Object array [][];
		array=new Object[arg.size()][];
		
		for(int i=0;i<arg.size();++i)
			array[i]=arg.get(i).toArray();
		
		return array;
	}
	
	private String [] columnNamesToArray(ArrayList<String> arg){
		String array [];
		array=new String[arg.size()];
		array=arg.toArray(array);
		return array;
	}
	public void clearView(){
		view.remove(view.getTableScroll());
		view.revalidate();
	}
	public void createTable(){
		editionModel=new TableEditionModel(tableDataToArray(model.getTableData()),
				columnNamesToArray(model.getColumnNames()), model);
		view.createTable(editionModel) ;
		LOGGER.info("Created view of table: "+model.getTableName());
	}
	
	public void addFilterButtonListener()
	{
		view.getFilterButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				view.popupFilterDialog(model.getColumnClasses());
			}
		});
	}
	
	public void setModelTableName(String tableName){
		model.setTableName(tableName);
		LOGGER.info("Model table name set to: "+model.getTableName());
	}
}


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

abstract class Controller{
	static Logger logger;
}

class SideController extends Controller{
	private SidePanelModel model;
	private SidePanel view;
	private TableController tableController;
	
	public SideController(SidePanelModel model, SidePanel view,TableController tableController) {
		this.model=model;
		this.view=view;
		this.tableController=tableController;
	}
	
	void insertTableNames(){
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
	
	void insertViewNames(){
		view.removeListListener();
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
					insertTableNames();
					tableController.setMode(0);
				}
				else if(view.getSelectedIndex()==1){
					insertViewNames();
					tableController.setMode(1);
				}
			}
		});
	}
}

class TableController extends Controller{
	private DatabaseTableModel model;
	private TablePanel view;
	private TableEditionModel editionModel;
	private int mode; //0=tabela, 1=widok
	public TableController(DatabaseTableModel model, TablePanel view) {
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
				// TODO Auto-generated method stub
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
	
	private void addDeleteConfirmationOptionPaneListener(final JOptionPane optionPane){
		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				int value = ((Integer)optionPane.getValue()).intValue();
				if (value == JOptionPane.OK_OPTION) {
				    Query deleteQuery=new Query(Query.MainStatement.DELETE, model.getTableName(),
				    		model.getColumnNames(), view.getTable().getSelectedRows());
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
		logger.info("Created view of table: "+model.getTableName());
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
		logger.info("Model table name set to: "+model.getTableName());
	}
}


package sqliteDB;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

abstract class Controller{
	ListPanel view;
	static Statement stat;
	static Logger logger;
}

class SideController extends Controller{
	SidePanelModel model;
	SidePanel view;
	TableController tableController;
	
	public SideController(SidePanelModel model, SidePanel view,TableController tableController) {
		this.model=model;
		this.view=view;
		this.tableController=tableController;
	}
	
	void insertTableColumns()
	{
			model.fetchTableNames();
			ArrayList<String> tableNames=model.getTableNames();
			for(int i=0;i<tableNames.size();++i)
				view.addListElement(tableNames.get(i));
		view.createList();
	}
	
	void addSidePanelListener(){
		view.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				tableController.clearView();
				tableController.setModelTableName(model.getTableName(lsm.getMinSelectionIndex()));
				tableController.createTable();
			}
		});
	}
}

class TableController extends Controller{
	DatabaseTableModel model;
	TablePanel view;
	TableEditionModel editionModel;
	public TableController(DatabaseTableModel model, TablePanel view) {
		this.model=model;
		this.view=view;
	}
	
	void insertTableColumns()
	{
		ArrayList<String> columnNames=model.getColumnNames();
		
		for(int i=0;i<columnNames.size();++i)
			view.addListElement(columnNames.get(i));
		
		view.createList();
	}
	
	void addNewRecordButtonListener(JButton insertButton, final JFrame frame){
		insertButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				view.createNewRecordFrame(frame);
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
		view.remove(1);
	}
	public void createTable(){
		editionModel=new TableEditionModel(tableDataToArray(model.getTableData()),
				columnNamesToArray(model.getColumnNames()), model);
		view.createTable(editionModel) ;
		logger.info("Created view of table: "+model.getTableName());
	}
	
	public void updateModel(){
		
	}
	
	public void setModelTableName(String tableName){
		model.setTableName(tableName);
		logger.info("Model table name set to: "+model.getTableName());
	}
}


package sqliteDB;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

class Query{
	public enum MainStatement{
		UPDATE("UPDATE"),
		INSERT("INSERT INTO"),
		DELETE("DELETE FROM"),
		SELECT("SELECT")
		;
		private final String string;
		
		private MainStatement(String string){
			this.string=string;
		}
		public String toString(){return string;}
	};
	private MainStatement mainStat;
	private String tableName;
	private String constraint;
	private String column;
	private Object value;
	
	public Query(MainStatement mainStat, String tableName,String column, String constraint, Object value){
		this.mainStat=mainStat;
		this.tableName=tableName;
		this.column=column;
		this.constraint=constraint;
		this.value=value;
	}
	
	public String createQueryString(){
		String queryString = null;
		switch(mainStat){
		case DELETE:
		{
			break;
		}
		case INSERT:
		{
			break;
		}
		case SELECT:
		{
			break;
		}
		case UPDATE:
		{
			queryString=mainStat.toString()+" "+tableName+" SET " +column+"=" +value+ " WHERE "+constraint;
			break;
		}
		default:
			break;
		
		}
		return queryString;
	}
}


abstract class AbstractModel{
	static Statement stat;
	static Connection conn;
	static Logger logger;
	public void connectionReanimation() throws SQLException, IOException{

			conn=dbase.getConnection();
			if(conn.isClosed())
				System.out.println("Ponowne polaczenie nieudane.");

	}
}


 class DatabaseTableModel extends AbstractModel{
	private String tableName;
	
	public DatabaseTableModel(String tableName){this.tableName=tableName;}
	
	public String getTableName(){return this.tableName;};
	
	public void setTableName(String tableName){this.tableName=tableName;}
	
	public static int getTableSize(Statement stat, String name)
	{
		ResultSet result;
		int size=0;
		try {
			result = stat.executeQuery("SELECT COUNT(*) FROM "+name);
			result.next();
			size=(result.getInt(1));
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return size;
	}
	public ArrayList<String> getColumnNames(){
		ResultSet result;
		ArrayList<String> list = new ArrayList<String>();
		try {
			result = stat.executeQuery("SELECT * FROM "+tableName);
			ResultSetMetaData meta=result.getMetaData();
			result.next();

			System.out.print(" ");
			for(int i=1;i<=meta.getColumnCount();++i)
			{
				//System.out.format("%1s%15s%1s","|",meta.getColumnLabel(i),"|");
				list.add(meta.getColumnLabel(i));
			}
			result.close();
		} catch (SQLException e) {
			//System.out.println("Nie mozna wyswietlic zawartosci tablicy o nazwie: "+model.tableName);
			e.printStackTrace();
		}
		
		return list;
	}
	
	public ArrayList<ArrayList<Object>> getTableData(){
		ResultSet result;
		ArrayList<ArrayList<Object>> data= new ArrayList<ArrayList<Object>>();
		ArrayList<Object> temp= new ArrayList<Object>();
		
		try {
			if(conn.isClosed())
			{
				 super.connectionReanimation();
			}
			   
			result = stat.executeQuery("SELECT * FROM "+tableName);
			ResultSetMetaData meta=result.getMetaData();
			result.next();
			System.out.println(result.getString(1));
			while(result.isAfterLast()==false)
			{
				//TODO 
				//Wczytywanie odpowiednich obiektow zamiast stringow
				for(int i=1;i<=meta.getColumnCount();++i)
				temp.add(result.getString(i));
				
				
				data.add(temp);
				temp= new ArrayList<Object>();
				
				result.next();
			}
			result.close();
		} catch (SQLException e) {
			System.out.println("Nie mozna odczytac zawartosci tablicy o nazwie: "+tableName);
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	public void executeQuery(String query){
		try {
			stat.execute(query);
			logger.info("Query Executed: "+query);
		} catch (SQLException e) {
			//System.out.println("Nie mozna wyswietlic zawartosci tablicy o nazwie: "+model.tableName);
			logger.warning("Following query cannot be executed: "+query);
			e.printStackTrace();
		}
	}
	
 }

class SidePanelModel extends AbstractModel{
	ArrayList<String> tableNames;
	
	public SidePanelModel(){
		tableNames= new ArrayList<String>();
	}
	
	public void fetchTableNames(){
		try {
			ResultSet result= stat.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table';");
			result.next();
			int tableCount=(result.getInt(1));
			
			for(int i=0;i<tableCount;++i)
			{
				result = stat.executeQuery("SELECT * FROM sqlite_master WHERE type='table';");
				for(int j=0;j<=i;++j)
				result.next();
		
			
				tableNames.add(result.getString("name"));
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public ArrayList<String> getTableNames(){return tableNames;}
	public String getTableName(int index){return tableNames.get(index);}
} 

class TableEditionModel extends AbstractTableModel{
	private String[] columnNames;
	private Object [][] data;
	DatabaseTableModel dbTableModel;
	public TableEditionModel(Object [][] data, String[] columnNames, DatabaseTableModel dbTableModel) {
		this.data=data;
		this.columnNames=columnNames;
		this.dbTableModel=dbTableModel;
	}
	
	@Override
	public int getColumnCount() {return columnNames.length;}

	@Override
	public int getRowCount() {return data.length;}

	@Override
	public Object getValueAt(int row, int col) {return data[row][col];}
	
	public String getColumnName(int col) {return columnNames[col];}
	
	public boolean isCellEditable(int row, int col) {
            return true;
    }
	public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
        //System.out.println("Cell ["+row+"]["+col+"] value changed to "+value.toString());
        Query query= new Query(Query.MainStatement.UPDATE,
        		dbTableModel.getTableName(),getColumnName(col), columnNames[0].toString()+"="+(row+1),value);
      //  System.out.println(query.createQueryString());
        dbTableModel.executeQuery(query.createQueryString());
    }
}
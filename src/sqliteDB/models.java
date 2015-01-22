package sqliteDB;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.logging.Logger;

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
	private ArrayList<Object> values;
	private ArrayList<String> columns;
	static Logger logger;
	
	public Query(MainStatement mainStat, String tableName,String column, String constraint, Object value){
		this.mainStat=mainStat;
		this.tableName=tableName;
		columns = new ArrayList<String>();
		columns.add(column);
		this.constraint=constraint;
		values= new ArrayList<Object>();
		values.add(value);
	}
	
	public Query(MainStatement mainStat, String tableName, ArrayList<String> columns, int [] constraint)
	{
		this.mainStat=mainStat;
		this.tableName=tableName;
		this.columns=columns;
		this.constraint=deleteConstraintToString(constraint);
	}
	
	public Query(MainStatement mainStat, String tableName,ArrayList<String> columns, String constraint, ArrayList<Object> values){
		this.mainStat=mainStat;
		this.tableName=tableName;
		this.columns=columns;
		this.constraint=constraint;
		this.values=values;
	}
	
	private String deleteConstraintToString(int [] constraint){
		StringBuffer buff = new StringBuffer();
		for(int i=0;i<constraint.length;++i)
			buff.append(constraint[i]+ " OR ");
		

		return buff.substring(0, buff.length()-3);
	}
	
	private String columnArrayListToString(){
		StringBuffer buff;
		buff=new StringBuffer();
		for(int i=0;i<columns.size();++i)
			buff.append(columns.get(i)+", ");

		
		return buff.substring(0, buff.length()-2);
		
	}
	
	private String valuesArrayListToString()
	{
		StringBuffer buff;
		buff=new StringBuffer();
		for(int i=0;i<values.size();++i)
		{
			if(values.get(i).getClass().equals(Boolean.class))
			{
				if((Boolean)values.get(i)==true)
					buff.append(1+", ");
				else
					buff.append(0+", ");
			}
			else if(values.get(i).getClass().equals(String.class))
				buff.append("\""+values.get(i)+"\""+", ");
			else
			buff.append(values.get(i)+", ");
		}
			

		
		return buff.substring(0, buff.length()-2);
	}
	public String createQueryString(){
		String queryString = null;
		switch(mainStat){
		case DELETE:
		{
			queryString=mainStat.toString()+" "+tableName+" WHERE "+columns.get(0)+" = " +constraint;
			break;
		}
		case INSERT:
		{
			queryString=mainStat.toString()+" "+tableName+" ("+columnArrayListToString()+")"+ " VALUES " + " ("+
					valuesArrayListToString()+")";
			break;
		}
		case SELECT:
		{
			break;
		}
		case UPDATE:
		{
			queryString=mainStat.toString()+" "+tableName+" SET " +columnArrayListToString()+"=" +valuesArrayListToString()+ " WHERE "+constraint;
			break;
		}
		default:
			break;
		
		}
		return queryString;
	}
	
	public static Object parseColumnType(String columnTypeName){
		
		switch(columnTypeName)
		{
		case "INTEGER":
		{
			return new Integer(0);
		}
		case "DECIMAL":
		{
			return new Double(0);
		}
		case "CHAR":
		{
			return new String();
		}
		case "VARCHAR":
		{
			return new String();
		}
		case "DATE":
		{
			return new Date(System.currentTimeMillis());
		}
		case "TIME":
		{
			return new Time(System.currentTimeMillis());
		}
		case "BIT":
		{
			return new Boolean(false);
		}
		default:
		{
			logger.info("Column type: "+columnTypeName+" unrecognizable, parsing to String.");
			return new String();
		}
		}
	}
}


abstract class AbstractModel{
	static Statement stat;
	static Connection conn;
	static Logger logger;
}

 class DatabaseTableModel extends AbstractModel{
	private String tableName;
	private ArrayList<Integer> primaryKeyColumns;
	public DatabaseTableModel(String tableName){
		this.tableName=tableName;
		primaryKeyColumns=new ArrayList<Integer>();
		//primaryKeyColumns=getPrimaryKeyColumns();
		for(int i=0;i<primaryKeyColumns.size();++i)
			System.out.println(primaryKeyColumns.get(i));
	}
	
	public String getTableName(){return this.tableName;};
	
	public void setTableName(String tableName){
		primaryKeyColumns.clear();
		this.tableName=tableName;}
	/*
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
	*/
	private ArrayList<Integer> getPrimaryKeyColumns(){
		ResultSet result;
		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			DatabaseMetaData meta = conn.getMetaData();
			result=meta.getPrimaryKeys(null, null, tableName);
			result.next();
			int i=0;
			while(result.isAfterLast()==false)
			{
				System.out.println(result.getInt(i));
				list.add(result.getInt(i));
				result.next();
				++i;
			}
		} catch (SQLException e) {
			//System.out.println("Nie mozna wyswietlic zawartosci tablicy o nazwie: "+model.tableName);
			e.printStackTrace();
		}
		
		return list;
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
	
	public ArrayList<String> getColumnClasses(){
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
				list.add(meta.getColumnTypeName(i));
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
			result = stat.executeQuery("SELECT * FROM "+tableName);
			ResultSetMetaData meta=result.getMetaData();
			result.next();
			while(result.isAfterLast()==false)
			{
				for(int i=1;i<=meta.getColumnCount();++i)
				temp.add(result.getObject(i));
				
				
				data.add(temp);
				temp= new ArrayList<Object>();
				
				result.next();
			}
			result.close();
		} catch (SQLException e) {
			System.out.println("Nie mozna odczytac zawartosci tablicy o nazwie: "+tableName);
			System.out.println(e.getMessage());
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
	private ArrayList<String> tableNames;
	private ArrayList<String> viewNames;
	public SidePanelModel(){
		tableNames= new ArrayList<String>();
		viewNames=new ArrayList<String>();
	}
	
	public void fetchTableNames(){
		try {
			ResultSet result= stat.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table';");
			result.next();
			int tableCount=(result.getInt(1));
			result = stat.executeQuery("SELECT * FROM sqlite_master WHERE type='table';");
			for(int i=0;i<tableCount;++i)
			{
				result.next();
				tableNames.add(result.getString("name"));
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public void fetchViewNames(){
		try {
			ResultSet result= stat.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='view';");
			result.next();
			int tableCount=(result.getInt(1));
			result = stat.executeQuery("SELECT * FROM sqlite_master WHERE type='view';");
			for(int i=0;i<tableCount;++i)
			{
				result.next();
				viewNames.add(result.getString("name"));
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	public void clearTableNames(){tableNames.clear();}
	public void clearViewNames(){viewNames.clear();}
	public boolean tableNamesCleared(){return tableNames.isEmpty();}
	public ArrayList<String> getTableNames(){return tableNames;}
	public String getTableName(int index){return tableNames.get(index);}
	public ArrayList<String> getViewNames(){return viewNames;}
	public String getViewName(int index){return viewNames.get(index);}
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
	
	public Class getColumnClass(int column){
		try{
			getValueAt(0, column).getClass();
		}
		catch(NullPointerException e)
		{
			return String.class;
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			return String.class;
		}
		return getValueAt(0, column).getClass();
	}
	public void setValueAt(Object value, int row, int col) {
		if(value.equals(data[row][col])==false)
		{
			data[row][col] = value;
	        fireTableCellUpdated(row, col);
	        //System.out.println("Cell ["+row+"]["+col+"] value changed to "+value.toString());
	        Query query= new Query(Query.MainStatement.UPDATE,
	        		dbTableModel.getTableName(),getColumnName(col), columnNames[0].toString()+"="+(row+1),value);
	      //  System.out.println(query.createQueryString());
	        dbTableModel.executeQuery(query.createQueryString());
		}
        
    }
}
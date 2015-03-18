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
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;
/**
 * Class Query creates SQL Insert, Update and Delete statements
 * as strings ready to execute by Statement.execute() method. 
 * @author Owen
 * TODO: constructors unification
 *
 */
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
	//private ArrayList<String> constraints;
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
	/**
	 * 
	 * @param mainStat
	 * @param tableName
	 * @param columns
	 * @param constraint
	 */
	
	public Query(MainStatement mainStat, String tableName, ArrayList<String> columns, int [] constraint)
	{
		if(constraint==null)
		{
			logger.info("Unable to create delete statement without constraint.");
		}
		this.mainStat=mainStat;
		this.tableName=tableName;
		this.columns=columns;
		if(columns.size()==1)
		this.constraint=deleteConstraintToString(constraint);
		else
			this.constraint=compositeDeleteConstraintToString(constraint);
	}
	
	public Query(MainStatement mainStat, String tableName,ArrayList<String> columns, String constraint, ArrayList<Object> values){
		this.mainStat=mainStat;
		this.tableName=tableName;
		this.columns=columns;
		this.constraint=constraint;
		this.values=values;
	}
	/**
	 * Creates a condition of Delete SQL statement by joining given primary keys
	 * with OR operator.  It can be used only with tables that utilize singular primary key.
	 * 
	 * @param constraint an array consisting of primary keys of rows that should be deleted
	 * @return string that describes deletion condition 
	 * 
	 */
	private String deleteConstraintToString(int [] constraint){
		StringBuffer buff = new StringBuffer();
		buff.append(columns.get(0)+ " = ");
		for(int i=0;i<constraint.length;++i)
			buff.append(constraint[i]+ " OR ");
		

		return buff.substring(0, buff.length()-3);
	}
	/**
	 * Creates a condition of Delete SQL statement by 
	 * .  It can be used only with tables that utilize singular primary key.
	 * 
	 * @param constraint an array consisting of primary keys of rows that should be deleted
	 * @return string that describes deletion condition 
	 * 
	 */
	private String compositeDeleteConstraintToString(int[] constraint)
	{
		System.out.println("constraint array length" +constraint.length);
		System.out.println("columns list size" +columns.size());
		int rowsToDelete=constraint.length/columns.size();
		
		StringBuffer buff = new StringBuffer();
		
		for(int i=0;i<rowsToDelete;++i)
		{
			buff.append("( ");
			for(int j=0;j<columns.size();++j)
			{
				buff.append(columns.get(j)+" = "+constraint[j+i*columns.size()]+" AND " );
			}
			buff.setLength(buff.length()-4);
			buff.append(") OR ");
		}
		
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
			queryString=mainStat.toString()+" "+tableName+" WHERE "+constraint;
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
	//TODO: create distinct abstract class for parsing
	public static Object parseColumnType(String columnTypeName){
		
		switch(columnTypeName)
		{
		case "INTEGER":
		{
			return new Integer(0);
		}
		case "INT":
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
			//TODO: check of database date format
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
}

interface TableModel{
	String getTableName();
	void setTableName(String name);
	Collection<? extends Object> getTableData();
	Collection<String> getPrimaryKeyColumns();
	int getPrimaryKeyColumnsCount();
	Collection<String> getColumnNames();
	Collection<String> getColumnClasses();
	void executeQuery(String query);
}

 class DefaultTableModel extends AbstractModel implements Loggable, TableModel{
	private String tableName;
	private ArrayList<String> primaryKeyColumns=new ArrayList<String>();
	private ArrayList<String> foreignKeyColumns= new ArrayList<String>();

	public DefaultTableModel(String tableName){
		this.tableName=tableName;
		fetchPrimaryKeyColumns();
		fetchForeignKeyColumns();
	}
	
	public DefaultTableModel(){
		
	}
	
	public String getTableName(){return this.tableName;};
	
	public void setTableName(String tableName){
		this.tableName=tableName;
		fetchPrimaryKeyColumns();
		fetchForeignKeyColumns();}
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
	
	
	/**
	 * This method retrieves primary key columns from database meta data
	 * and stores their names in primaryKeyColumns field.
	 * GetPrimaryKeys function seems to return primary key column names WITHOUT capital letters, so
	 * comparison with any column name present in this application usually requires proper conversion. 
	 */
	private void fetchPrimaryKeyColumns(){
		ResultSet result;
		primaryKeyColumns.clear();
		try {
			DatabaseMetaData meta = conn.getMetaData();
			result=meta.getPrimaryKeys(null, null, tableName);
			result.next();
			while(result.isAfterLast()==false)
			{
				System.out.println("primary key column: "+result.getString(4));
				primaryKeyColumns.add(result.getString(4));
				result.next();
			}
		} catch (SQLException e) {
			LOGGER.info("SQL Error encountered when fetching Primary Keys of "+tableName);
			e.printStackTrace();
		}
		
		return;
	}
	
	public ArrayList<String> getPrimaryKeyColumns(){return primaryKeyColumns;}
	public int getPrimaryKeyColumnsCount(){return primaryKeyColumns.size();}
	/**
	 * This method retrieves foreign key columns from database meta data
	 * and stores their names in foreignKeyColumns field.
	 */
	
	private void fetchForeignKeyColumns(){
		ResultSet result;
		foreignKeyColumns.clear();
		try {
			DatabaseMetaData meta = conn.getMetaData();
			result=meta.getImportedKeys(null, null, tableName);
			result.next();
			while(result.isAfterLast()==false)
			{
				System.out.println("foreign key column: "+result.getString(8));
				foreignKeyColumns.add(result.getString(8));
				result.next();
			}
		} catch (SQLException e) {
			LOGGER.info("SQL Error encountered when fetching Foreign Keys of "+tableName);
			e.printStackTrace();
		}
		
		return;
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
	/**
	 * This method retrieves data of table defined by tableName field of DatabaseTableModel
	 * @return table data as ArrayList of ArrayLists
	 */
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
	/**
	 * This method executes statement generated by Query class
	 */
	public void executeQuery(String query){
		try {
			stat.execute(query);
			LOGGER.info("Query Executed: "+query);
		} catch (SQLException e) {
			LOGGER.warning("Query: "+query+" cannot be executed.");
			e.printStackTrace();
		}
	}
	
 }
/**
 * Class SidePanelModel is responsible for fetching and storing information about 
 * tables and views in database 
 * TODO: security restrictions, stored procedures and functions
 * @author Owen
 *
 */
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
/**
 * Class TableEditionModel is customised AbstracTableModel class of JTable view (table in TablePanel class)
 * linked DatabaseTableModel allows performing Update statements on table by overrided setValueAt method
 * @author Owen
 *
 */
class TableEditionModel extends AbstractTableModel{
	private String[] columnNames;
	private Object [][] data;
	DefaultTableModel dbTableModel;
	public TableEditionModel(Object [][] data, String[] columnNames, DefaultTableModel dbTableModel) {
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
	/**
	 * This method is called whenever any field of table is updated. It creates and executes
	 *  appropriate update statement.
	 *  TODO: security checks, composite primary keys handling
	 */
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
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
import java.util.List;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import sqliteDB.Query.MainStatement;
/**
 * Class Query creates SQL Insert, Update and Delete statements
 * as strings ready to execute by Statement.execute() method. 
 * @author Owen
 * TODO: constructors unification
 */
class Query implements Loggable{
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
	protected MainStatement mainStat;
	protected String tableName;
	protected String condition;
	/**
	 * Defines which column should be updated on Update command.
	 */
	protected String updateColumn;
	//private ArrayList<String> constraints;
	protected ArrayList<Object> values;
	protected ArrayList<String> columns;
	/**
	 * 
	 * @param mainStat
	 * @param tableName
	 * @param columns
	 * @param constraint
	 */

	
	//TODO: constructor renaming?
	
	//DELETE
	public Query(MainStatement mainStat, String tableName, ArrayList<String> columns, int [] condition)
	{
		if(mainStat==null || tableName==null || columns==null || condition ==null)
			throw new IllegalArgumentException("None of Query constructor arguments can be null.");
		if(columns.size()>condition.length)
			throw new IllegalArgumentException("Number of composite primary key columns cannot exceed the number of provided primary keys.");
		if(columns.size()==0 ||condition.length==0)
			throw new IllegalArgumentException("At least one column name and condition value must be provided.");
		this.mainStat=mainStat;
		this.tableName=tableName;
		this.columns=columns;
		if(columns.size()==1)
			this.condition=singularKeyConditionToString(condition);
		else
			this.condition=compositeKeyConditionToString(condition);
	}
	
	//INSERT
	
	/**
	 * Creates singular insert command. Count of columns must be equal to the count of values.
	 * @param mainStat
	 * @param tableName
	 * @param columns
	 * @param values
	 */
	public Query(MainStatement mainStat, String tableName,ArrayList<String> columns,ArrayList<Object> values){
		if(mainStat==null || tableName==null || columns==null || values==null)
			throw new IllegalArgumentException("None of Query constructor arguments can be null.");
		if(columns.size()!=values.size())
			throw new IllegalArgumentException("Count of columns must be equal to the count of values.");
		if(columns.size()==0 && values.size()==0)
			throw new IllegalArgumentException("Count of columns and values must be greater than zero.");
		this.mainStat=mainStat;
		this.tableName=tableName;
		this.columns=columns;
		this.condition="";
		this.values=values;
	}
	
	//UPDATE
	public Query(MainStatement mainStat, String tableName, ArrayList<String> columns,
			int [] condition, Object value, String updateColumn) {
		if(mainStat==null || tableName==null || columns==null || condition==null || value==null || updateColumn==null)
			throw new IllegalArgumentException("None of Query constructor arguments can be null.");
		//TODO:length check
		if(condition.length==0)
			throw new IllegalArgumentException("Condition primary keys must be provided.");
		this.mainStat=mainStat;
		this.tableName=tableName;
		this.columns=columns;
		this.updateColumn=updateColumn;
		if(columns.size()==1)
			this.condition=singularKeyConditionToString(condition);
		else
			this.condition=compositeKeyConditionToString(condition);
		values=new ArrayList<Object>();
		this.values.add(value);
	}
	
	

	/**
	 * Creates a condition of Delete/Update SQL statement by joining given primary keys
	 * with OR operator.  It can be used only with tables that utilize singular primary key.
	 * 
	 * @param condition an array consisting of primary keys of rows that should be deleted
	 * @return string that describes deletion condition 
	 * 
	 */
	protected String singularKeyConditionToString(int [] condition){
		StringBuffer buff = new StringBuffer();
		buff.append(columns.get(0)+ " = ");
		for(int i=0;i<condition.length;++i)
			buff.append(condition[i]+ " OR ");
		

		return buff.substring(0, buff.length()-3);
	}
	/**
	 * Creates a condition of Delete/Update SQL statement. Primary keys in constraint array
	 * must be organized in the following schema: indices of keys of 
	 * first composite primary key column must satisfy equation index % constraint.length == 0,
	 * indices of second column must satisfy equation index % constraint.length == 1, and so on.
	 * 
	 *  It can be used only with tables that utilize composite primary key.
	 * 
	 * @param condition an array consisting of primary keys of rows that should be deleted
	 * @return string that describes deletion condition 
	 * 
	 */
	protected String compositeKeyConditionToString(int[] condition)
	{
		System.out.println("constraint array length" +condition.length);
		System.out.println("columns list size" +columns.size());
		int rowsToDelete=condition.length/columns.size();
		
		StringBuffer buff = new StringBuffer();
		
		for(int i=0;i<rowsToDelete;++i)
		{
			buff.append("( ");
			for(int j=0;j<columns.size();++j)
			{
				buff.append(columns.get(j)+" = "+condition[j+i*columns.size()]+" AND " );
			}
			buff.setLength(buff.length()-4);
			buff.append(") OR ");
		}
		
		return buff.substring(0, buff.length()-3);
	}
	
	protected String columnArrayListToString(){
		StringBuffer buff;
		buff=new StringBuffer();
		for(int i=0;i<columns.size();++i)
			buff.append(columns.get(i)+", ");

		
		return buff.substring(0, buff.length()-2);
		
	}
	
	protected String valuesArrayListToString()
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
				
				queryString=mainStat.toString()+" "+tableName+" WHERE "+condition;
				break;
			}
			case INSERT:
			{
				queryString=mainStat.toString()+" "+tableName+" ("+columnArrayListToString()+")"+ " VALUES " + " ("+
						valuesArrayListToString()+")";
				break;
			}
			case UPDATE:
			{
				queryString=mainStat.toString()+" "+tableName+" SET " +updateColumn+"=" +valuesArrayListToString()+ " WHERE "+condition;
				break;
			}
			default:
				break;
			
		}
		return queryString;
	}
	//TODO: create distinct abstract class for parsing?
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
				LOGGER.info("Column type: "+columnTypeName+" unrecognizable, parsing to String.");
				return new String();
			}
		}
	}
}


abstract class AbstractModel{
	static Statement stat;
	static Connection conn;
}
/**
 * TableModel interface prototype. TableModel is responsible for fetching table data from database.
 * @author Owen
 *
 */
interface TableModel{
	String getTableName();
	void setTableName(String name);
	Collection<? extends Object> getTableData();
	
	/**
	 * @return Collection of names of primary key columns.
	 */
	Collection<String> getPrimaryKeyColumns();
	int getPrimaryKeyColumnsCount();
	List<String> getColumnNames();
	
	/**
	 * @return Collection of names of columns classes.
	 */
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
		fetchForeignKeyColumns();
	}
	
	
	public ArrayList<String> getPrimaryKeyColumns(){return primaryKeyColumns;}
	public int getPrimaryKeyColumnsCount(){return primaryKeyColumns.size();}
	
	
	public ArrayList<String> getColumnNames(){
		ResultSet result;
		ArrayList<String> list = new ArrayList<String>();
		try {
			result = stat.executeQuery("SELECT * FROM "+tableName);
			ResultSetMetaData meta=result.getMetaData();
			result.next();

			for(int i=1;i<=meta.getColumnCount();++i)
			{
				list.add(meta.getColumnLabel(i));
			}
			result.close();
		} catch (SQLException e) {
			LOGGER.warning("SQL Error encountered when fetching "+tableName+" column names.");
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
			for(int i=1;i<=meta.getColumnCount();++i){
				list.add(meta.getColumnTypeName(i));
			}
			result.close();
		} catch (SQLException e) {
			LOGGER.warning("SQL Error encountered when fetching "+tableName+" column classes.");
			e.printStackTrace();
		}
		
		return list;

	}
	/**
	 * This method retrieves data of table defined by tableName field of DefaultTableModel
	 * @return table data as ArrayList of ArrayLists of Objects
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
			LOGGER.warning("SQL Error encountered when fetching "+tableName+" data.");
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
			LOGGER.warning("SQL Error encountered when fetching Foreign Keys of "+tableName);
			e.printStackTrace();
		}
		
		return;
	}
	
	/**
	 * This method retrieves primary key columns from database meta data
	 * and stores their names in primaryKeyColumns field.
	 * JDBC GetPrimaryKeys function seems to return primary key column names WITHOUT capital letters, so
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
				primaryKeyColumns.add(result.getString(4));
				result.next();
			}
		} catch (SQLException e) {
			LOGGER.warning("SQL Error encountered when fetching Primary Keys of "+tableName);
			e.printStackTrace();
		}
		
		return;
	}
	
 }
/**
 * Class SidePanelModel is responsible for fetching and storing information about 
 * tables and views in database 
 * @author Owen
 *
 */
class SidePanelModel extends AbstractModel implements Loggable{
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
			LOGGER.warning("SQL Error encountered when fetching table names.");
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
			LOGGER.warning("SQL Error encountered when fetching view names.");
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
 * Class TableEditionModel is customized AbstracTableModel class of JTable view (table in TablePanel class)
 * linked DatabaseTableModel allows performing Update statements on table by overridden setValueAt method
 * @author Owen
 *
 */
class TableEditionModel extends AbstractTableModel{
	private String[] columnNames;
	private Object [][] data;
	private DefaultTableModel dbTableModel;
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
	
	
	private int[] getPrimaryKeysValues(int row)
	{
		ArrayList<String> primaryColumns=dbTableModel.getPrimaryKeyColumns();

		
		int [] pKeys= new int [primaryColumns.size()];
		
		for(int i=0; i<data[0].length;++i)
			for(int j=0; j< primaryColumns.size(); ++j)
			{
				//Lower case conversion needed
				if(getColumnName(i).toLowerCase().equals(primaryColumns.get(j)))
					pKeys[i]=(int) getValueAt(row, i);
			}
				
		return pKeys;
			
	}
	
	/**
	 * This method is called whenever any field of table is updated. It creates and executes
	 *  appropriate update statement.
	 *  
	 */
	public void setValueAt(Object value, int row, int col) {
		if(value.equals(data[row][col])==false)
		{
			getPrimaryKeysValues(row);
			Query query= new Query(Query.MainStatement.UPDATE, dbTableModel.getTableName() ,
	        		dbTableModel.getPrimaryKeyColumns(), getPrimaryKeysValues(row),value, getColumnName(col));
			dbTableModel.executeQuery(query.createQueryString());
			data[row][col] = value;
	        fireTableCellUpdated(row, col);
		}
        
    }
}
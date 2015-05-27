package sqliteDB;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

class Query implements Loggable {
	public enum MainStatement {
		UPDATE("UPDATE"), INSERT("INSERT INTO"), DELETE("DELETE FROM"), SELECT(
				"SELECT");
		private final String string;

		private MainStatement(String string) {
			this.string = string;
		}

		public String toString() {
			return string;
		}
	};

	protected MainStatement mainStat;
	protected String tableName;
	protected String condition;
	/**
	 * Defines which column should be updated on Update command.
	 */
	protected String updateColumn;
	// private ArrayList<String> constraints;
	protected ArrayList<Object> values;
	protected ArrayList<String> columns;

	/**
	 * 
	 * @param mainStat
	 * @param tableName
	 * @param columns
	 * @param constraint
	 */

	// TODO: constructor renaming?

	// DELETE
	public Query(MainStatement mainStat, String tableName,
			ArrayList<String> columns, int[] condition) {
		if (mainStat == null || tableName == null || columns == null
				|| condition == null)
			throw new IllegalArgumentException(
					"None of Query constructor arguments can be null.");
		if (columns.size() > condition.length)
			throw new IllegalArgumentException(
					"Number of composite primary key columns cannot exceed the number of provided primary keys.");
		if (columns.size() == 0 || condition.length == 0)
			throw new IllegalArgumentException(
					"At least one column name and condition value must be provided.");
		this.mainStat = mainStat;
		this.tableName = tableName;
		this.columns = columns;
		if (columns.size() == 1)
			this.condition = singularKeyConditionToString(condition);
		else
			this.condition = compositeKeyConditionToString(condition);
	}

	// INSERT

	/**
	 * Creates singular insert command. Count of columns must be equal to the
	 * count of values.
	 * 
	 * @param mainStat
	 * @param tableName
	 * @param columns
	 * @param values
	 */
	public Query(MainStatement mainStat, String tableName,
			ArrayList<String> columns, ArrayList<Object> values) {
		if (mainStat == null || tableName == null || columns == null
				|| values == null)
			throw new IllegalArgumentException(
					"None of Query constructor arguments can be null.");
		if (columns.size() != values.size())
			throw new IllegalArgumentException(
					"Count of columns must be equal to the count of values.");
		if (columns.size() == 0 && values.size() == 0)
			throw new IllegalArgumentException(
					"Count of columns and values must be greater than zero.");
		this.mainStat = mainStat;
		this.tableName = tableName;
		this.columns = columns;
		this.condition = "";
		this.values = values;
	}

	// UPDATE
	public Query(MainStatement mainStat, String tableName,
			ArrayList<String> columns, int[] condition, Object value,
			String updateColumn) {
		if (mainStat == null || tableName == null || columns == null
				|| condition == null || value == null || updateColumn == null)
			throw new IllegalArgumentException(
					"None of Query constructor arguments can be null.");
		// TODO:length check
		if (condition.length == 0)
			throw new IllegalArgumentException(
					"Condition primary keys must be provided.");
		this.mainStat = mainStat;
		this.tableName = tableName;
		this.columns = columns;
		this.updateColumn = updateColumn;
		if (columns.size() == 1)
			this.condition = singularKeyConditionToString(condition);
		else
			this.condition = compositeKeyConditionToString(condition);
		values = new ArrayList<Object>();
		this.values.add(value);
	}

	/**
	 * Creates a condition of Delete/Update SQL statement by joining given
	 * primary keys with OR operator. It can be used only with tables that
	 * utilize singular primary key.
	 * 
	 * @param condition
	 *            an array consisting of primary keys of rows that should be
	 *            deleted
	 * @return string that describes deletion condition
	 * 
	 */
	protected String singularKeyConditionToString(int[] condition) {
		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < condition.length; ++i) {
			buff.append(columns.get(0) + " = ");
			buff.append(condition[i] + " OR ");
		}

		return buff.substring(0, buff.length() - 3);
	}

	/**
	 * Creates a condition of Delete/Update SQL statement. Primary keys in
	 * constraint array must be organized in the following schema: indices of
	 * keys of first composite primary key column must satisfy equation index %
	 * constraint.length == 0, indices of second column must satisfy equation
	 * index % constraint.length == 1, and so on.
	 * 
	 * It can be used only with tables that utilize composite primary key.
	 * 
	 * @param condition
	 *            an array consisting of primary keys of rows that should be
	 *            deleted
	 * @return string that describes deletion condition
	 * 
	 */
	protected String compositeKeyConditionToString(int[] condition) {
		System.out.println("constraint array length" + condition.length);
		System.out.println("columns list size" + columns.size());
		int rowsToDelete = condition.length / columns.size();

		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < rowsToDelete; ++i) {
			buff.append("( ");
			for (int j = 0; j < columns.size(); ++j) {
				buff.append(columns.get(j) + " = "
						+ condition[j + i * columns.size()] + " AND ");
			}
			buff.setLength(buff.length() - 4);
			buff.append(") OR ");
		}

		return buff.substring(0, buff.length() - 3);
	}

	protected String columnArrayListToString() {
		StringBuffer buff;
		buff = new StringBuffer();
		for (int i = 0; i < columns.size(); ++i)
			buff.append(columns.get(i) + ", ");

		return buff.substring(0, buff.length() - 2);

	}

	protected String valuesArrayListToString() {
		StringBuffer buff;
		buff = new StringBuffer();
		for (int i = 0; i < values.size(); ++i) {
			if (values.get(i).getClass().equals(Boolean.class)) {
				if ((Boolean) values.get(i) == true)
					buff.append(1 + ", ");
				else
					buff.append(0 + ", ");
			} else if (values.get(i).getClass().equals(String.class))
				buff.append("\"" + values.get(i) + "\"" + ", ");
			else
				buff.append(values.get(i) + ", ");
		}

		return buff.substring(0, buff.length() - 2);
	}

	public String createQueryString() {
		String queryString = null;
		switch (mainStat) {
		case DELETE: {

			queryString = mainStat.toString() + " " + tableName + " WHERE "
					+ condition;
			break;
		}
		case INSERT: {
			queryString = mainStat.toString() + " " + tableName + " ("
					+ columnArrayListToString() + ")" + " VALUES " + " ("
					+ valuesArrayListToString() + ")";
			break;
		}
		case UPDATE: {
			queryString = mainStat.toString() + " " + tableName + " SET "
					+ updateColumn + "=" + valuesArrayListToString()
					+ " WHERE " + condition;
			break;
		}
		default:
			break;

		}
		return queryString;
	}

	// TODO: create distinct abstract class for parsing?
	public static Object parseColumnType(String columnTypeName) {

		switch (columnTypeName) {
		case "INTEGER": {
			return new Integer(0);
		}
		case "INT": {
			return new Integer(0);
		}
		case "DECIMAL": {
			return new Double(0);
		}
		case "CHAR": {
			return new String();
		}
		case "VARCHAR": {
			return new String();
		}
		case "DATE": {
			// TODO: check of database date format
			return new Date(System.currentTimeMillis());
		}
		case "TIME": {
			return new Time(System.currentTimeMillis());
		}
		case "BIT": {
			return new Boolean(false);
		}
		default: {
			LOGGER.info("Column type: " + columnTypeName
					+ " unrecognizable, parsing to String.");
			return new String();
		}
		}
	}
}

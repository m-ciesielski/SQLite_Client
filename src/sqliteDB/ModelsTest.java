package sqliteDB;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class ModelsTest {
	 ArrayList<String> columns;
	 int condition[];
	 ArrayList<Object> values;
	 
	 @Before
	    public void setUp() {
		 columns = new ArrayList<String>();
		 condition=new int [4];
		 values=new ArrayList<Object>();
	    }
	
	@Test
	public void deleteTest() throws Exception {
		columns.clear();
		columns.add("column1");
		for(int i=0;i<condition.length;++i)
			condition[i]=i;
		Query query = new Query(Query.MainStatement.DELETE, "table", columns, condition);
		assertEquals("DELETE FROM table WHERE column1 = 0 OR 1 OR 2 OR 3 ", query.createQueryString());
		//System.out.println(query.createQueryString());
		
	}
	
	
	@Test
	public void deleteCompositePKTest() throws Exception {
		columns.clear();
		columns.add("column1");
		columns.add("column2");
		columns.add("column3");
		
		for(int i=0;i<condition.length;++i)
			condition[i]=i;
		
		//3 columns, 4 keys
		Query query = new Query(Query.MainStatement.DELETE, "table", columns, condition);
		assertEquals("DELETE FROM table WHERE ( column1 = 0 AND column2 = 1 AND column3 = 2 ) ", query.createQueryString());
		
		//4 columns, 4 keys
		columns.add("column4");
		query = new Query(Query.MainStatement.DELETE, "table", columns, condition);
		assertEquals("DELETE FROM table WHERE ( column1 = 0 AND column2 = 1 AND column3 = 2 AND column4 = 3 ) ", query.createQueryString());
		
		
	}
	
	/**
	 * Testing IllegalArgumentException handling of Query constructor.
	 * Test cases:<br>
	 * constructing Query with 0 columns and 4 keys<br>
	 * constructing Query with 3 columns and 0 keys<br>
	 * constructing Query with 4 columns and 2 keys<br>
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class) 
	public void deleteIllegalArgumentsTest() throws Exception {
		columns.clear();
		columns.add("column1");
		columns.add("column2");
		columns.add("column3");

		for(int i=0;i<condition.length;++i)
			condition[i]=i;
		
		//0 columns, 4 keys
				Query query = new Query(Query.MainStatement.DELETE, "table", null, condition);
		//3 columns, 0 keys 
				int conditionEmpty[] = null;
				query = new Query(Query.MainStatement.DELETE, "table", columns, conditionEmpty);
		//No main statement
				query = new Query(null, "table", columns, condition);
		//4 columns, 2 keys
				int condition2[]=new int [2];
				condition2[0]=5;
				condition2[1]=7;
				query = new Query(Query.MainStatement.DELETE, "table", columns, condition2);
	}
	
	@Test
	public void insertTest() throws Exception {
		columns.clear();
		columns.add("column1");
		columns.add("column2");
		
		values.clear();
		values.add(new Integer(4));
		values.add(new Integer(5));
		
		//TEST CASE 1
		Query query=new Query(Query.MainStatement.INSERT, "table", columns, values);
		assertEquals("INSERT INTO table (column1, column2) VALUES  (4, 5)", query.createQueryString());
		
		
	}
	
	
	@Test(expected = IllegalArgumentException.class) 
	public void insertIllegalArgumentsTest() throws Exception {
		columns.clear();
		columns.add("column1");
		columns.add("column2");
		
		values.clear();
		values.add(new Integer(4));
		values.add(new Integer(5));
		values.add(new Integer(4));
		Query query=new Query(Query.MainStatement.INSERT, "table",columns, values);
	}
	
	@Test
	public void updateTest() throws Exception {
		columns.clear();
		columns.add("column1");
		
		values.clear();
		values.add(new Integer(2));
		
		int condition3[]=new int[1];
		condition3[0]=4;
		
		Query query= new Query(Query.MainStatement.UPDATE,"table" ,columns,
        		condition3, new Integer(4), "id");
		assertEquals("UPDATE table SET id=4 WHERE column1 = 4 ", query.createQueryString());
	}
	
	@Test
	public void updateCompositePKTest() throws Exception {
		columns.clear();
		columns.add("column1");
		columns.add("column2");
		values.clear();
		values.add(new Integer(2));
		
		int condition3[]=new int[2];
		condition3[0]=4;
		condition3[1]=5;
		Query query= new Query(Query.MainStatement.UPDATE,"table" ,columns,
        		condition3, new Integer(4), "id");
		assertEquals("UPDATE table SET id=4 WHERE ( column1 = 4 AND column2 = 5 ) ", query.createQueryString());
		
	}
}

package sqliteDB;
/**
@version 1.01 2004-09-24
@author Cay Horstmann
*/

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
Program sprawdzajṗcy poprawnoæ konfiguracji
bazy danych i sterownika JDBC.
*/
class dbase
{  
	private static void createAndShowGUI (final JFrame f, final Connection conn, final Statement stat){
		System.out.println("Created GUI on EDT? "+
	            SwingUtilities.isEventDispatchThread());
	    f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    f.setSize(600,600);
	    f.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				dbase.closeJDBCResources(conn, stat);
				f.dispose();
				System.exit(0); 
				
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    //f.pack();
	    
	    f.setVisible(true);
	}
	
	private static void closeJDBCResources(Connection conn, Statement stat){
		try{
			stat.close();
			conn.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
public static void main (String args[])
{  
	
   try
   {  
      //runTest();
	   Logger logger=Logger.getLogger("CRUD SQLITE Log");
	   Log.initializeLog(logger);
	   final Connection conn = getConnection();
	   final Statement stat = conn.createStatement();
	   
	 //+=================================================//
	    ///TEST MVC
	   
	   	DatabaseTableModel.stat=stat;
	   	AbstractModel.conn=conn;
	   	AbstractModel.logger=logger;
	   	final JFrame f = new JFrame("CRUD");
	    DatabaseTableModel tableModel=new DatabaseTableModel("Towar");
	    TablePanel mainPanel=new TablePanel(f);
	    Controller.logger=logger;
	    Controller.stat=stat;
	    TableController mainPanelController=new TableController(tableModel, mainPanel);
	    SidePanel sidePanel=new SidePanel(f);
	    SidePanelModel sideModel= new SidePanelModel();
	    SideController sideController=new SideController(sideModel, sidePanel,mainPanelController);
	   
	    sideController.insertTableColumns();
	    mainPanelController.createTable();
	    mainPanelController.addNewRecordButtonListener(mainPanel.getNewRecordButton(), f);
	    sideController.addSidePanelListener();
	  //+=================================================//
	    
	 //=====================================================//
	   //Inicjalizacja GUI
	   SwingUtilities.invokeLater(new Runnable() {
           public void run() {
           		f.repaint();
               //createAndShowGridGUI(mainPan);
        	   createAndShowGUI(f,conn,stat);
        	   
           }
       });
	 //=====================================================//
   }
   catch (SQLException ex)
   {  
      while (ex != null)
      {  
         ex.printStackTrace();
         ex = ex.getNextException();
      }
   }
   catch (IOException ex)
   {  
      ex.printStackTrace();
   }
   
}


/**
   Nawiṗzuje poġṗczenie, korzystajṗc
   z wġaciwoci w pliku database.properties
   @return poġṗczenie do bazy danych
*/
public static Connection getConnection()
   throws SQLException, IOException
{  
   Properties props = new Properties();
   FileInputStream in = new FileInputStream("database.properties");
   props.load(in);
   in.close();

   String drivers = props.getProperty("jdbc.drivers");
   if (drivers != null)
      System.setProperty("jdbc.drivers", drivers);
   String url = props.getProperty("jdbc.url");
   //String username = props.getProperty("jdbc.username");
   //String password = props.getProperty("jdbc.password");

   return DriverManager.getConnection(url);//, username, password);
}

/*
	public static void showMenu(Scanner in,Statement stat, Connection con)
	{
		System.out.println();
		System.out.println("Wybierz tabel�: ");
		System.out.println("1. Pracownik");
		System.out.println("2. Zamowienie");
		System.out.println("3. Klient");
		System.out.println("4. Adres");
		System.out.println("5. Towar");
		System.out.println("6. SHOW DATABASE");
		System.out.println("7. SHOW TABLE");
		System.out.println("8. QUIT");
		menuAction(in,stat,con);
		
	}
	
	public static void menuAction(Scanner in,Statement stat, Connection con)
	{
		int order=0;
		order=Integer.parseInt(in.nextLine());
		
		if(order==8)
		{
			System.exit(0);
		}
		else if(order==6)
		{
			showDatabaseContent(stat);
			System.out.println("Nacisnij enter by wrocic do menu.");
			in.nextLine();
			showMenu(in,stat,con);
		}
			
		else if(order==7)
		{
			System.out.println("Podaj nazwe tablicy: ");
			String name=in.nextLine();
			int size=getTableSize(stat, name);
			showTableContent(stat,name,size);
			System.out.println("Nacisnij enter by wrocic do menu.");
			in.nextLine();
			showMenu(in,stat,con);
		}
		else if(order==1)
		{
			System.out.println("1. Dodaj nowego pracownika.");
			System.out.println("2. Zmien pensje pracownika.");
			System.out.println("3. Zmien premie pracownika.");
			System.out.println("4. Usun pracownika.");
			System.out.println("5. Wyswietl wszystkich pracownikow.");
			System.out.println("6. Wroc do menu.");
			int input=Integer.parseInt(in.nextLine());
			if(input==1)
				newEmployee(stat, con, in);
			else if (input==2)
			updateSalary(stat, con, in);
			else if (input==3)
				updateBonus(stat, con, in);
			else if (input==4)
				deleteEmployee(stat, con, in);
			else if (input==5)
			{
				int size=getTableSize(stat, "Pracownik");
				showTableContent(stat, "Pracownik", size);
			}
			else if (input==6)
			showMenu(in,stat,con);
			
			showMenu(in,stat,con);
		}
		else if(order==2)
		{
			System.out.println("1. Dodaj zamowienie.");
			System.out.println("2. Zmien rabat.");
			System.out.println("3. Zmien wartosc.");
			System.out.println("4. Usun zamowienie.");
			System.out.println("5. Wyswietl zestawienie zamowie�.");
			System.out.println("6. Wyswietl zawarto�� koszyka zam�wienia.");
			System.out.println("7. Dodaj nowy koszyk.");
			System.out.println("8. Wroc do menu.");
			int input=Integer.parseInt(in.nextLine());
			if(input==1)
				newOrder(stat, con, in);
			else if (input==2)
			updateOrderDiscount(stat, con, in);
			else if (input==3)
				updateOrderValue(stat, con, in);
			else if (input==4)
				deleteOrder(stat, con, in);
			else if (input==5)
			showOrdersSummary(stat, con, in);
			else if (input==6)
				showOrderDetails(stat, con, in);
			else if (input==7)
				newProductList(stat, con, in);
			else if (input==8)
			showMenu(in,stat,con);
			
			showMenu(in,stat,con);
		}
		else if(order==3)
		{
			System.out.println("1. Dodaj klienta.");
			System.out.println("2. Zmien nazwe klienta.");
			System.out.println("3. Zmien numer konta klienta.");
			System.out.println("4. Usun klienta.");
			System.out.println("5. Wyswietl dane klientow.");
			System.out.println("6. Wroc do menu.");
			int input=Integer.parseInt(in.nextLine());
			if(input==1)
				newCustomer(stat, con, in);
			else if (input==2)
			updateCustomerName(stat, con, in);
			else if (input==3)
				updateCustomerBankAccount(stat, con, in);
			else if (input==4)
				deleteCustomer(stat, con, in);
			else if (input==5)
			showCustomers(stat, con, in);
			else if (input==6)
			showMenu(in,stat,con);
			
			showMenu(in,stat,con);
		}
		else if(order==4)
		{
			System.out.println("1. Dodaj adres.");
		System.out.println("2. Zmien adres.");
		System.out.println("3. Usun adres.");
		System.out.println("4. Wyswietl wszystkie adresy.");
		System.out.println("5. Wroc do menu.");
		int input=Integer.parseInt(in.nextLine());
		if(input==1)
			newAddress(stat, con, in);
		else if (input==2)
		updateAddress(stat, con, in);
		else if (input==3)
			deleteAddress(stat, con, in);
		else if (input==4)
			showAddress(stat, con, in);
		else if (input==5)
		showMenu(in,stat,con);
			
		showMenu(in,stat,con);
		}
		
		else if(order==5)
		{
			System.out.println("1. Dodaj nowy typ towaru.");
		System.out.println("2. Dodaj egzemplarze towaru.");
		System.out.println("3. Usun egzemplarze towaru.");
		System.out.println("4. Usun typ towaru towaru.");
		System.out.println("5. Pokaz zestawienie towarow.");
		System.out.println("6. Zmien nazwe typu towaru.");
		System.out.println("7. Zmien cene i wartosc VAT towaru.");
		System.out.println("8. Wroc do menu.");
		int input=Integer.parseInt(in.nextLine());
		if(input==1)
			newProductType(stat, con, in);
		else if (input==2)
		newProduct(stat, con, in);
		else if (input==3)
			deleteProduct(stat,con,in);
		else if (input==4)
			deleteProductType(stat, con, in);
		else if (input==5)
		showProducts(stat,con,in);
		else if (input==6)
			updateProductTypeName(stat,con,in);
		else if (input==7)
			updateProduct(stat,con,in);
		else if (input==8)
		showMenu(in,stat,con);
		
		showMenu(in,stat,con);
		}

			
	}
*/
	public static void showTableContent(Statement stat, String tableName, int size)
	{
		ResultSet result;
		
		try {
			result = stat.executeQuery("SELECT * FROM "+tableName);
			ResultSetMetaData meta=result.getMetaData();
			result.next();
			int counter=1;
			System.out.print(" ");
			for(int i=1;i<=meta.getColumnCount();++i)
			{
				System.out.format("%1s%15s%1s","|",meta.getColumnLabel(i),"|");
			}
			System.out.println();
			while(result.isAfterLast()==false)
			{
				System.out.print(counter);
				for(int i=1;i<=meta.getColumnCount();++i)
				System.out.format("%1s%15s%1s", "|",result.getString(i),"|");
				result.next();
				++counter;
				System.out.println();
			}
			result.close();
		} catch (SQLException e) {
			System.out.println("Nie mozna wyswietlic zawartosci tablicy o nazwie: "+tableName);
			e.printStackTrace();
		}
		
	}
	
	public static void showTableContent(Statement stat, StringBuffer tableName, int size)
	{
		ResultSet result;
		
		try {
			result = stat.executeQuery("SELECT * FROM "+tableName);
			ResultSetMetaData meta=result.getMetaData();
			result.next();
			int counter=1;
			System.out.print(" ");
			for(int i=1;i<=meta.getColumnCount();++i)
			{
				System.out.format("%1s%15s%1s","|",meta.getColumnLabel(i),"|");
			}
			System.out.println();
			while(result.isAfterLast()==false)
			{
				System.out.print(counter);
				for(int i=1;i<=meta.getColumnCount();++i)
				System.out.format("%1s%15s%1s", "|",result.getString(i),"|");
				result.next();
				++counter;
				System.out.println();
			}
			result.close();
		} catch (SQLException e) {
			System.out.println("Nie mozna wyswietlic zawartosci tablicy o nazwie: "+tableName);
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void showDatabaseContent(Statement stat)
	{
		try {
			ResultSet result= stat.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table';");
			result.next();
			int tableCount=(result.getInt(1));
			StringBuffer tableName=new StringBuffer();
			for(int i=0;i<tableCount;++i)
			{
				result = stat.executeQuery("SELECT * FROM sqlite_master WHERE type='table';");
				for(int j=0;j<=i;++j)
				result.next();
				System.out.println(result.getString(2));
				tableName.delete(0, tableName.length());
				tableName.append(result.getString(2));
				
				result = stat.executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND tbl_name= '"+tableName + "';");
				result.next();
				System.out.println(result.getString("sql").substring(7));
				
				result= stat.executeQuery("SELECT COUNT(*) FROM "+tableName);
				result.next();
				int tableSize=(result.getInt(1));
				
				result=stat.executeQuery("SELECT * FROM "+tableName);

				showTableContent(stat,tableName,tableSize);
				System.out.println();
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void showOrdersSummary(Statement stat, Connection con, Scanner in)
	{
		
		try {
			String queryString="SELECT * FROM Zamowienie WHERE data>? AND data<?;";
			PreparedStatement queryStatement=con.prepareStatement(queryString);
			System.out.println("Podaj poczatek przedzialu: ");
			queryStatement.setString(1, in.nextLine());
			System.out.println("Podaj koniec przedzialu: ");
			queryStatement.setString(2, in.nextLine());
			
			ResultSet result=queryStatement.executeQuery();
			ResultSetMetaData meta=result.getMetaData();
			result.next();
			int counter=1;
			System.out.print(" ");
			for(int i=1;i<=meta.getColumnCount();++i)
			{
				System.out.format("%1s%15s%1s","|",meta.getColumnLabel(i),"|");
			}
			System.out.println();
			while(result.isAfterLast()==false)
			{
				System.out.print(counter);
				for(int i=1;i<=meta.getColumnCount();++i)
				System.out.format("%1s%15s%1s", "|",result.getString(i),"|");
				result.next();
				++counter;
				System.out.println();
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		

	}
	
	public static void showOrderDetails(Statement stat, Connection con, Scanner in)
	{
		
		try {
			String queryString="SELECT k.id_Zamowienie, tp.nazwa, k.ilosc, k.wartosc, k.podatek  FROM Koszyk k LEFT JOIN Towar t ON k.id_Towar = t.id_Towar"
					+ " LEFT JOIN Typ_towaru tp ON t.id_Typ_towaru=tp.id_Typ_towaru WHERE k.id_Zamowienie=?;";
			PreparedStatement queryStatement=con.prepareStatement(queryString);
			System.out.println("Podaj id zamowienia: ");
			queryStatement.setInt(1, Integer.parseInt(in.nextLine()));
			
			ResultSet result=queryStatement.executeQuery();
			ResultSetMetaData meta=result.getMetaData();
			result.next();
			int counter=1;
			System.out.print(" ");
			for(int i=1;i<=meta.getColumnCount();++i)
			{
				System.out.format("%1s%15s%1s","|",meta.getColumnLabel(i),"|");
			}
			System.out.println();
			while(result.isAfterLast()==false)
			{
				System.out.print(counter);
				for(int i=1;i<=meta.getColumnCount();++i)
				System.out.format("%1s%15s%1s", "|",result.getString(i),"|");
				result.next();
				++counter;
				System.out.println();
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		

	}

	

	
	public static void updateSalary(Statement stat, Connection con, Scanner in)
	{
		try {
			String updateString="UPDATE Pracownik SET pensja=? WHERE id_Pracownik=?;";
			System.out.println(updateString);
			PreparedStatement updateStatement=con.prepareStatement(updateString);
			System.out.println("Wpisz id pracownika: ");
			updateStatement.setInt(2, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz pensje: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			updateStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateOrderValue(Statement stat, Connection con, Scanner in)
	{
		try {
			String updateString="UPDATE Zamowienie SET wartosc=? WHERE id_Zamowienie=?;";
			System.out.println(updateString);
			PreparedStatement updateStatement=con.prepareStatement(updateString);
			System.out.println("Wpisz ID zamowienia: ");
			updateStatement.setInt(2, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz wartosc: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			updateStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void updateOrderDiscount(Statement stat, Connection con, Scanner in)
	{
		try {
			String updateString="UPDATE Zamowienie SET rabat=? WHERE id_Zamowienie=?;";
			System.out.println(updateString);
			PreparedStatement updateStatement=con.prepareStatement(updateString);
			System.out.println("Wpisz ID zamowienia: ");
			updateStatement.setInt(2, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz procentowa wartosc rabatu: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			updateStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void newEmployee(Statement stat, Connection con, Scanner in)
	{
		try {
			String insertString="INSERT INTO Pracownik (imie,nazwisko,pensja,premia) VALUES(?,?,?,?);";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz imie: ");
			updateStatement.setString(1, in.nextLine());
			System.out.println("Wpisz nazwisko: ");
			updateStatement.setString(2, in.nextLine());
			System.out.println("Wpisz pensje: ");
			updateStatement.setInt(3, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz premie: ");
			updateStatement.setInt(4, Integer.parseInt(in.nextLine()));
			updateStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void deleteEmployee(Statement stat, Connection con, Scanner in)
	{
		try {
			String insertString="DELETE FROM Pracownik WHERE id_Pracownik=?;";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz ID Pracownika: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			updateStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void deleteOrder(Statement stat, Connection con, Scanner in)
	{
		try {
			String insertString="DELETE FROM Zamowienie WHERE id_Zamowienie=?;";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz ID Zamowienia: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			updateStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void newOrder(Statement stat, Connection con, Scanner in)
	{
		try {
			//TODO tworzenie listy towarow w zamowieniu, przekazanie id listy tow. do inserta
			String insertString="INSERT INTO Zamowienie VALUES(?,?,?,?,?,?,?);";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz nazwe: ");
			updateStatement.setString(2, in.nextLine());
			System.out.println("Wpisz id klienta: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			
			System.out.println("Wpisz wartosc: ");
			updateStatement.setInt(4, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz % rabatu: ");
			updateStatement.setInt(3, Integer.parseInt(in.nextLine()));
			long time = System.currentTimeMillis();
			java.sql.Date date = new java.sql.Date(time);
			java.sql.Time sqltime=new java.sql.Time(time);
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			updateStatement.setString(5,dateFormat.format(date));
			updateStatement.setString(6,timeFormat.format(sqltime));
			updateStatement.execute();
			
			//uzupelnienie koszyka
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void newProductList(Statement stat, Connection con, Scanner in)
	{
		try {
			//dodanie koszyka
			System.out.println("Wpisz wartosc: ");
			String ordString="INSERT INTO Koszyk VALUES(?,?,?,?,?);";
			System.out.println(ordString);
			PreparedStatement ordStatement=con.prepareStatement(ordString);
			System.out.println("Wpisz ID zamowienia: ");
			ordStatement.setInt(1, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz ID towaru: ");
			ordStatement.setInt(2, Integer.parseInt(in.nextLine()));
			
			System.out.println("Wpisz ilosc: ");
			ordStatement.setInt(3, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz wartosc: ");
			ordStatement.setInt(4, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz podatek: ");
			ordStatement.setInt(5, Integer.parseInt(in.nextLine()));
			ordStatement.execute();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void newProduct(Statement stat, Connection con, Scanner in)
	{
		try {
			String insertString="INSERT INTO Towar(cena,podatek, id_Typ_towaru) VALUES(?,?,?);";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz ID typu towaru: ");
			updateStatement.setInt(3, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz cene: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz podatek: ");
			updateStatement.setInt(2, Integer.parseInt(in.nextLine()));
			updateStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void newProductType(Statement stat, Connection con, Scanner in)
	{
		try {
			String insertString="INSERT INTO Typ_Towaru(nazwa) VALUES(?);";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz nazwe nowego typu towaru: ");
			updateStatement.setString(1, in.nextLine());

			updateStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void updateBonus(Statement stat, Connection con, Scanner in)
	{
		try {
			String updateString="UPDATE Pracownik SET premia=? WHERE id_Pracownik=?;";
			System.out.println(updateString);
			PreparedStatement updateStatement=con.prepareStatement(updateString);
			
			System.out.println("Wpisz id pracownika: ");
			updateStatement.setInt(2, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz premie: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			updateStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void updateProduct(Statement stat, Connection con, Scanner in)
	{
		try {
			String updateString="UPDATE Towar SET cena=?,podatek=? WHERE id_Towar=?;";
			System.out.println(updateString);
			PreparedStatement updateStatement=con.prepareStatement(updateString);
			
			System.out.println("Wpisz id towaru: ");
			updateStatement.setInt(3, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz cene: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz procentowa wartosc podatku: ");
			updateStatement.setInt(2, Integer.parseInt(in.nextLine()));
			updateStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void newAddress(Statement stat, Connection con, Scanner in)
	{
		try {
			String insertString="INSERT INTO Adres(miasto,numer,ulica,kod) VALUES(?,?,?,?);";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz nazwe miasta: ");
			updateStatement.setString(1, in.nextLine());
			System.out.println("Wpisz kod pocztowy: ");
			updateStatement.setString(4, in.nextLine());
			System.out.println("Wpisz nazwe ulicy: ");
			updateStatement.setString(3, in.nextLine());
			System.out.println("Wpisz numer budynku: ");
			updateStatement.setString(2, in.nextLine());
			updateStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void updateAddress(Statement stat, Connection con, Scanner in)
	{
		try {
			String updateString="UPDATE Adres SET miasto=?,kod=?,ulica=?,numer=? WHERE id_Adres=?;";
			System.out.println(updateString);
			PreparedStatement updateStatement=con.prepareStatement(updateString);
			
			System.out.println("Wpisz id adresu: ");
			updateStatement.setInt(5, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz nazwe miasta: ");
			updateStatement.setString(1, in.nextLine());
			System.out.println("Wpisz kod pocztowy: ");
			updateStatement.setString(2, in.nextLine());
			System.out.println("Wpisz nazwe ulicy: ");
			updateStatement.setString(3, in.nextLine());
			System.out.println("Wpisz numer budynku: ");
			updateStatement.setString(4, in.nextLine());
			updateStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void deleteAddress(Statement stat, Connection con, Scanner in)
	{
		try {
			String insertString="DELETE FROM Adres WHERE id_Adres=?;";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz ID adresu: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			updateStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void showAddress(Statement stat, Connection con, Scanner in)
	{
		
		try {
			String queryString="SELECT * FROM Adres;";
			PreparedStatement queryStatement=con.prepareStatement(queryString);
			
			ResultSet result=queryStatement.executeQuery();
			ResultSetMetaData meta=result.getMetaData();
			result.next();
			int counter=1;
			System.out.print(" ");
			for(int i=1;i<=meta.getColumnCount();++i)
			{
				System.out.format("%1s%15s%1s","|",meta.getColumnLabel(i),"|");
			}
			System.out.println();
			while(result.isAfterLast()==false)
			{
				System.out.print(counter);
				for(int i=1;i<=meta.getColumnCount();++i)
				System.out.format("%1s%15s%1s", "|",result.getString(i),"|");
				result.next();
				++counter;
				System.out.println();
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	public static void deleteProduct(Statement stat, Connection con, Scanner in)
	{
		try {
			String insertString="DELETE FROM Towar WHERE id_Towar=?;";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz ID towaru: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			updateStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void deleteProductType(Statement stat, Connection con, Scanner in)
	{
		try {
			String insertString="DELETE FROM Typ_towaru WHERE id_Typ_towaru=?;";
			System.out.println(insertString);
			PreparedStatement updateStatement=con.prepareStatement(insertString);
			System.out.println("Wpisz ID typu towaru: ");
			updateStatement.setInt(1, Integer.parseInt(in.nextLine()));
			updateStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void updateProductTypeName(Statement stat, Connection con, Scanner in)
	{
		try {
			String updateString="UPDATE Typ_towaru SET nazwa=? WHERE id_Typ_towaru=?;";
			System.out.println(updateString);
			PreparedStatement updateStatement=con.prepareStatement(updateString);
			
			System.out.println("Wpisz id typu towaru: ");
			updateStatement.setInt(2, Integer.parseInt(in.nextLine()));
			System.out.println("Wpisz nazwe typu towaru: ");
			updateStatement.setString(1, in.nextLine());

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void showProducts(Statement stat, Connection con, Scanner in)
	{
		
		try {
			String queryString="SELECT tp.nazwa,t.id_Towar,tp.id_Typ_towaru,t.cena,t.podatek FROM Towar t JOIN Typ_towaru tp ON t.id_Typ_Towaru=tp.id_Typ_towaru;";
			PreparedStatement queryStatement=con.prepareStatement(queryString);
			
			ResultSet result=queryStatement.executeQuery();
			ResultSetMetaData meta=result.getMetaData();
			result.next();
			int counter=1;
			System.out.print(" ");
			for(int i=1;i<=meta.getColumnCount();++i)
			{
				System.out.format("%1s%15s%1s","|",meta.getColumnLabel(i),"|");
			}
			System.out.println();
			while(result.isAfterLast()==false)
			{
				System.out.print(counter);
				for(int i=1;i<=meta.getColumnCount();++i)
				System.out.format("%1s%15s%1s", "|",result.getString(i),"|");
				result.next();
				++counter;
				System.out.println();
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
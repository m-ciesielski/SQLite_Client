package sqliteDB;
/**
@version 1.01 2004-09-24
@author Cay Horstmann
*/

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.awt.GridLayout;
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
	static Connection conn;
	static Statement stat;
	private static void createAndShowGUI (final JFrame f){
		System.out.println("Created GUI on EDT? "+
	            SwingUtilities.isEventDispatchThread());
	    f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    f.setSize(800,600);
	    f.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				
				dbase.closeJDBCResources();
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
	
public static void main (String args[])
{  
	
   try
   {  
      //runTest();
	   Logger logger=Logger.getLogger("CRUD SQLITE Log");
	   Log.initializeLog(logger);
	   conn = getConnection();
	   stat = conn.createStatement();
	   
	 //+=================================================//
	    ///TEST MVC
	   	Query.logger=logger;
	   	DatabaseTableModel.stat=stat;
	   	AbstractModel.conn=conn;
	   	AbstractModel.logger=logger;
	   	final JFrame f = new JFrame("CRUD");
	    DatabaseTableModel tableModel=new DatabaseTableModel("Towar");
	    TablePanel mainPanel=new TablePanel(f);
	    Controller.logger=logger;
	    TableController mainPanelController=new TableController(tableModel, mainPanel);
	    SidePanel sidePanel=new SidePanel(f);
	    SidePanelModel sideModel= new SidePanelModel();
	    SideController sideController=new SideController(sideModel, sidePanel,mainPanelController);
	    TopMenu topMenu=new TopMenu(f,logger,sideController);
	    sideController.insertTableNames();
	    mainPanelController.createTable();
	    
	   // sideController.addSidePanelListListener();
	    sideController.addSidePanelTabListener();
	  //+=================================================//
	    
	 //=====================================================//
	   //Inicjalizacja GUI
	   SwingUtilities.invokeLater(new Runnable() {
           public void run() {
           		f.repaint();
               //createAndShowGridGUI(mainPan);
        	   createAndShowGUI(f);
        	   
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
private static void closeJDBCResources(){
	try{
		stat.close();
		conn.close();
	}
	catch(SQLException e){
		e.printStackTrace();
	}
}

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

public static Connection getConnection(String databasePath)
		throws SQLException, IOException{
	Properties props = new Properties();
	 
	FileInputStream in = new FileInputStream("database.properties");
	   props.load(in);
	   in.close();
	   props.setProperty("jdbc.url","jdbc:sqlite:"+databasePath );
	   System.out.println(props.getProperty("jdbc.url", null));
	   String drivers = props.getProperty("jdbc.drivers");
	   if (drivers != null)
	      System.setProperty("jdbc.drivers", drivers);
	   String url = props.getProperty("jdbc.url");
	   return DriverManager.getConnection(url);
	}
}


package sqliteDB;

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


interface Loggable{
	final static Logger LOGGER=Logger.getLogger("CRUD SQLITE Log");
}



/**
 * Main Class.
 * @author mciesielski
 *
 */
class dbase
{  
	static Connection conn;
	static Statement stat;
	static boolean connected=false;
	private static void createAndShowGUI (final JFrame f){
	    f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    f.setSize(800,600);
	    f.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				
				dbase.closeJDBCResources();
				f.dispose();
				System.exit(0); 
				
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
	    
	    //f.pack();
	    
	    f.setVisible(true);
	}
	
public static void main (String args[])
{  
      //runTest();
	   final JFrame f = new JFrame("CRUD");
	   TablePanel mainPanel=new TablePanel(f);
	   SidePanel sidePanel=new SidePanel(f);
	   
		Logger logger=Logger.getLogger("CRUD SQLITE Log");
	   	//Log.initializeLog(logger);
	   	Query.logger=logger;
	   	
	   	DefaultTableModel tableModel=new DefaultTableModel();
	   	
	    SidePanelModel sideModel= new SidePanelModel();
	    DefaultTableController mainPanelController=new DefaultTableController(tableModel, mainPanel);
	   	SideController sideController=new SideController(sideModel, sidePanel,mainPanelController);
	   	
	   	TopMenu topMenu=new TopMenu(f, sideController);
	   if(connected==true)
	   {
		   
		   	
		   	//DatabaseTableModel.stat=stat;
		   	//AbstractModel.conn=conn;
		   	

		   tableModel.setTableName(sideModel.getTableName(0));
		    sideController.updateTableNames();
		    System.out.println("fdsfdsf");
		    mainPanelController.createTable();
		    
		   // sideController.addSidePanelListListener();
		    sideController.addSidePanelTabListener();
	   }
	   
	   
	   
	   //conn = getConnection();
	   //stat = conn.createStatement();
	   
	 //+=================================================//
	    ///TEST MVC
	   	
	  //+=================================================//
	    
	 //=====================================================//
	   //Inicjalizacja GUI
	   SwingUtilities.invokeLater(new Runnable() {
           public void run() {
           		//f.repaint();
               //createAndShowGridGUI(mainPan);
        	   createAndShowGUI(f);
        	   
           }
       });
	 //=====================================================//

   
}


/**
   Nawiṗzuje poġṗczenie, korzystajṗc
   z wġaciwoci w pliku database.properties
   @return poġṗczenie do bazy danych
*/
private static void closeJDBCResources(){
	try{
		if(stat!=null)
		stat.close();
		if(conn!=null)
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


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

//TODO: Update on tables with composite keys


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
	   final JFrame f = new JFrame("CRUD");
	   TablePanel mainPanel=new TablePanel(f);
	   SidePanel sidePanel=new SidePanel(f);
	   
	   	
	   	DefaultTableModel tableModel=new DefaultTableModel();
	   	
	    SidePanelModel sideModel= new SidePanelModel();
	    DefaultTableController mainPanelController=new DefaultTableController(tableModel, mainPanel);
	   	SideController sideController=new SideController(sideModel, sidePanel,mainPanelController);
	   	
	   	TopMenu topMenu=new TopMenu(f, sideController);
	   if(connected==true)
	   {
		   
		   	

		   tableModel.setTableName(sideModel.getTableName(0));
		    sideController.updateTableNames();

		    mainPanelController.createTable();
		    
		    sideController.addSidePanelTabListener();
	   }
	   
	   
	 
	   //Inicjalizacja GUI
	   SwingUtilities.invokeLater(new Runnable() {
           public void run() {
        	   createAndShowGUI(f);
        	   
           }
       });

   
}


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

public static Connection getConnection(String username, String password)
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
   //props.setProperty("jdbc.password", password);
   //props.setProperty("jdbc.user", username);

   return DriverManager.getConnection(url, username, password);
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


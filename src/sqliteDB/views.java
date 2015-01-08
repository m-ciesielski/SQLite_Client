package sqliteDB;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

interface ListPanel{
	public void addListElement(String arg);
	public void createList();
}

class SidePanel extends JPanel implements ListPanel{
	private JList<String> list;
	private DefaultListModel <String> listModel;
	private JScrollPane listScroll;
	
	public SidePanel(JFrame frame){
		listModel=new DefaultListModel<String>();
		listScroll=new JScrollPane();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		frame.add(this, BorderLayout.WEST);
		frame.pack();
	}
	
	public void addListElement(String arg){listModel.addElement(arg);}
	
	public void createList(){
		list=new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		
		listScroll.setMaximumSize(new Dimension(100, 200));
		listScroll.setMinimumSize (new Dimension (100,200));
		listScroll.setPreferredSize(listScroll.getMaximumSize());
		listScroll.setViewportView(list);
		this.add(listScroll);
		repaint();
	}
	
public void addListSelectionListener(ListSelectionListener arg){
		list.getSelectionModel().addListSelectionListener(arg);
	}
	
}

class TablePanel extends JPanel implements ListPanel{
	private JList<String> list;
	private DefaultListModel <String> listModel;
	private JScrollPane listScroll;
	private JTable table;
	private JScrollPane tableScroll;
	private JButton newRecordButton;
	private JInternalFrame newRecordFrame;
	
	class NewRecordFrame extends JInternalFrame{

		public NewRecordFrame() {
			// TODO Auto-generated constructor stub
			super("Document #",
			          true, //resizable
			          true, //closable
			          true, //maximizable
			          true);//iconifiable
		}
	}
	
	public TablePanel(JFrame frame){
		//listModel=new DefaultListModel<String>();
		//listScroll=new JScrollPane();
		newRecordButton= new JButton();
		newRecordButton.setPreferredSize(new Dimension(50,50));
		this.add(newRecordButton, BorderLayout.NORTH);
		frame.add(this,BorderLayout.CENTER);
		frame.pack();
	}
	
	public JButton getNewRecordButton (){return newRecordButton;}
	public void addListElement(String arg){listModel.addElement(arg);}
	
	public void createList(){
		list=new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(1);
		
		listScroll.setMaximumSize(new Dimension(100, 200));
		listScroll.setMinimumSize (new Dimension (100,200));
		listScroll.setPreferredSize(listScroll.getMaximumSize());
		listScroll.setViewportView(list);
		this.add(listScroll);
		repaint();
	}
	
	public void createNewRecordFrame(JFrame parentFrame){
		newRecordFrame=new JInternalFrame("New Record", true,true,true,true);
		newRecordFrame.setVisible(true);
		newRecordFrame.setPreferredSize(new Dimension(150,150));
		parentFrame.add(newRecordFrame);
		parentFrame.revalidate();
	}
	
	
	public void createTable(Object [][] data, Object [] columnNames){
		table= new JTable(data, columnNames);
		table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        table.setFillsViewportHeight(true);
        tableScroll = new JScrollPane(table);
        this.add(tableScroll);
        tableScroll.repaint();
		revalidate();
	}
	public void createTable(TableEditionModel editionModel){
		table= new JTable(editionModel);
		table.setPreferredScrollableViewportSize(new Dimension(500, 500));
        table.setFillsViewportHeight(true);
        tableScroll = new JScrollPane(table);
        this.add(tableScroll);
        tableScroll.repaint();
		revalidate();
	}
	
}


import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

public class AdminPanel extends WindowAdapter implements ActionListener {

	JFrame frame;
	MulticastClient parent;
	JTable table;
	MyTableModel mymodel;
	JButton refresh;
	String[][] localDir = new String[100][2];
	ImageIcon img;

	String[] columnNames = { "Username", "Real Name" };
	
	String[][] classList = {
			
			{"Francis","NOT FRANCIS"}

	};

	public AdminPanel(MulticastClient par) {

		frame = new JFrame("Administrator Panel");
		frame.setSize(75, 200);
		frame.addWindowListener(this);
		img = new ImageIcon("panel.png");
		//http://www.customicondesign.com/ Image Source
		frame.setIconImage(img.getImage());

		refresh = new JButton("Refresh");
		refresh.addActionListener(this);

		parent = par;
		
		match();
		mymodel = new MyTableModel(localDir, columnNames);
		table = new JTable(mymodel);

		frame.add(new JScrollPane(table), "North");
		frame.add(refresh, "South");

		frame.pack();

		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		Runnable refreshTime = new Runnable() {
			public void run() {
				parent.directory = new String[100][2];
				parent.send(parent.REQUEST, parent.ADMIN_NAME, null);
				try {
					Thread.sleep(500);
					match();
					mymodel.changeData(localDir);
					frame.repaint();
				} catch (Exception e) {
				}
			}
		};

		SwingUtilities.invokeLater(refreshTime);

	}

	public void windowClosing(WindowEvent e) {
		parent.admin = false;
	}
	
	public void match(){
		String[][] key = parent.directory; 
		localDir = new String[100][2];
		for(int i=0;i<key.length;i++){
			if(key[i][0]!=null){
				//there is a valid entry in this. attempt to replace username with real name
				localDir[i][0] = key[i][0]; //this is the username
				localDir[i][1] = key[i][1];
				for(int j=0;j<classList.length;j++){
					if(localDir[i][1].equals(classList[j][0])){
						//we have a match!!
						localDir[i][1] = classList[j][1]; //assign real name to the username of the local directory
						break;
					}
				}
			}
		}
	}
	
	public void search(){
		
	}

	class MyTableModel extends AbstractTableModel {
		private String[] columnNames;
		private Object[][] data;

		public MyTableModel(Object[][] data, String[] columnNames) {
			super();
			this.columnNames = columnNames;
			this.data = data;
		}

		public void changeData(Object[][] data) {
			this.data = data;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return data.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

	}
}

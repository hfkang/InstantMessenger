/*********************************************************
*  Name: Francis Kang                                    *
*  Course: ICS 4M  Pd. 5                                 *
*  Assignment #1                                         *
*  Purpose: End of Year Summative: Instant Messenger     *
*  Due Date: May 2014                                    *
*********************************************************/

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;

public class NewGUI extends WindowAdapter implements KeyListener, ListSelectionListener, ActionListener {
	JFrame frame = new JFrame("(C) Frajis K@ang");
	ImageIcon img;
	JButton change;
	JPanel pane;

	JTextPane txtPane = new JTextPane();
	JTextArea inputPane = new JTextArea();
	

	JScrollPane scroll = new JScrollPane(txtPane);
	JScrollPane userList;
	JScrollPane type = new JScrollPane(inputPane);

	JTable userDir;
	UserTable tb;
	ListSelectionModel lsm;

	JSplitPane layer2;
	JSplitPane layer1;

	StyledDocument doc = txtPane.getStyledDocument();
	SimpleAttributeSet set1 = new SimpleAttributeSet();
	SimpleAttributeSet set2 = new SimpleAttributeSet();
	SimpleAttributeSet set3 = new SimpleAttributeSet();
	SimpleAttributeSet green = new SimpleAttributeSet();
	SimpleAttributeSet boldText = new SimpleAttributeSet();


	String input;
	MulticastClient parent;


	/*** NewGUI **********************************************
	   * Purpose: constructor - makes GUI                    *
	   * Parameters: parent object multicastclient           *
	   * Returns: none                                       *
	   ******************************************************/
	public NewGUI(MulticastClient parent) {
		
		this.parent = parent;
		frame.setMinimumSize(new Dimension(685, 610));
		frame.setLayout(new BorderLayout());
		//img = new ImageIcon("cat.png");
		img = new ImageIcon(getClass().getResource("cat.png"));
		frame.setIconImage(img.getImage());


		txtPane.setEditable(false);
		txtPane.setPreferredSize(new Dimension(300, 300));
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		StyleConstants.setForeground(set1, Color.BLACK);
		StyleConstants.setForeground(set2, Color.RED);
		StyleConstants.setForeground(set3, Color.BLUE);
		StyleConstants.setForeground(boldText, Color.BLACK);
		StyleConstants.setBold(boldText, true);
		
		change = new JButton("Change Room");
		change.addActionListener(this);
		
		
		String[] nam = { "Users" };
		tb = new UserTable(parent.list, nam);
		userDir = new JTable(tb);
		userDir.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		lsm = userDir.getSelectionModel();
		lsm.addListSelectionListener(this);
		userDir.setSelectionModel(lsm);

		userList = new JScrollPane(userDir);
		userList.setPreferredSize(new Dimension(54,400));
		
		pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		change.setAlignmentX(0.5f);
		pane.add(userList);
		pane.add(change);

		layer2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, pane);
		layer1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, layer2, type);

		layer2.setResizeWeight(1.0);
		layer1.setResizeWeight(1.0);

		type.setPreferredSize(new Dimension(frame.getX(), 80));
		type.setMinimumSize(new Dimension(frame.getX(), 80));
		inputPane.addKeyListener(this);
		inputPane.setWrapStyleWord(true);
		type.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		frame.add(layer1);

		frame.addWindowListener(this);
		frame.setLocationRelativeTo(null);
		
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		inputPane.requestFocus();
	}

	public void windowGainedFocus(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}
	 /*** windowClosing***************************************
	   * Purpose: Disconnects from network when program exits*
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void windowClosing(WindowEvent e) {

			parent.send(parent.BYE_BYE, parent.userName, "");
			parent.leave();
	}

	public void keyPressed(KeyEvent e) {
	}
	 /*** keyReleased*****************************************
	   * Purpose: serves as surrogate actionListener for     * 
	   * the input textpane                                  *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void keyReleased(KeyEvent e) {
		if (e.isShiftDown() && e.getKeyCode() == 10)
			inputPane.append("\n");
		else if (e.getKeyCode() == 10) {
			input = inputPane.getText().trim();
			inputPane.setText(null);

			// send message
			if (!input.equals(""))
				if (input.charAt(0) == '/') // sends it off for further analysis
					parent.command(input);
				else
					parent.send(parent.MESSAGE,parent.userName,input);

		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	 /*** getName ********************************************
	   * Purpose: ask user for what username they want to use*
	   * if the user quits, the program quits                *
	   * if the user provides invalid input, program retries *
	   * Parameters: command string - input shown            *
	   * Returns: username string                            *
	   ******************************************************/
	public String getName(String c) {
		
		String name = JOptionPane.showInputDialog(c); 
		//String name = ("" + Math.random()).substring(0, 5);
		if (name == null) {
			parent.leave();
			System.exit(0);
		} else
			while (name.equals("")) {
				name = JOptionPane.showInputDialog(c);
				if (name == null) {
					parent.leave();
					System.exit(0);
				}
			}

		return name;
	}
	 /*** refreshList*****************************************
	   * Purpose: repaints the userdirectory in the frame    *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void refreshList() {

		tb.changeData(parent.list);
		frame.repaint();
	}
	 /*** display*********************************************
	   * Purpose: displays a message received                *
	   * Parameters: sender name, and their message          *
	   * Returns: none                                       *
	   ******************************************************/
	public void display(String sender,String message) {

		try {
			doc.insertString(doc.getLength(), sender+": ", boldText);
			doc.insertString(doc.getLength(), message + "\n", set1);
			txtPane.setCaretPosition(doc.getLength());

		} catch (BadLocationException e) {
		}
	}
	 /*** PM *********************************************
	   * Purpose: display a private message received         *
	   * displayes message in red text
	   * Parameters: message                                 *
	   * Returns: none                                       *
	   ******************************************************/
	public void PM(String message) {

		try {
			doc.insertString(doc.getLength(), message + "\n", set2);
			txtPane.setCaretPosition(doc.getLength());

		} catch (BadLocationException e) {
		}

	}
	 /*** announce *******************************************
	   * Purpose: display system announcements in blue font  *
	   * Parameters: message string                                     *
	   * Returns: none                                       *
	   ******************************************************/
	public void announce(String s) {

		try {
			doc.insertString(doc.getLength(), s + "\n", set3);
			txtPane.setCaretPosition(doc.getLength());

		} catch (BadLocationException e) {
		}

	}

	public boolean auth() {
		boolean ans = false;
		JPasswordField pf = new JPasswordField(20);
		JOptionPane.showConfirmDialog(null, pf, "Enter Password",
				JOptionPane.OK_CANCEL_OPTION);
		if (new String(pf.getPassword()).equals(parent.ADMIN_PASSWORD))
			ans = true;
		return ans;
	}
	 /*** valueChanged  **************************************
	   * Purpose: handle events on user directory            *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()){
			String name = tb.getName(userDir.getSelectedRow());
			if(name!=null){
				inputPane.setText("/msg "+name+" ");
				inputPane.requestFocus();
			}
			
			lsm.clearSelection();
		}	
	}
	/*** chooseRoom  **************************************
	   * Purpose: get user selection for chatroom            *
	   * Parameters: none                                    *
	   * Returns: int                                        *
	   ******************************************************/
	public int chooseRoom() {
		parent.peeker.query();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {}
		int a = parent.peeker.getAverage(0);
		int b = parent.peeker.getAverage(1);
		int c = parent.peeker.getAverage(2);
		int d = parent.peeker.getAverage(3);
		String question = "Which chatroom would you like to use?";
		String[] options = { "Room A ("+a+" online)", "Room B ("+b+" online)", "Room C ("+c+" online)", "Room D ("+d+" online)" };
		int ans = -1;
		int selection = 0;
		if(parent.currentRoom!=-1)
			selection = parent.currentRoom;
		ans = JOptionPane.showOptionDialog(frame, question, null,
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[selection]);
		return ans;
	}

	/*** actionPerformed  **************************************
	   * Purpose: handle button click            *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void actionPerformed(ActionEvent arg0) {
		Runnable werk = new Runnable() {
			public void run(){
				parent.getRoom();
			}
		};
		SwingUtilities.invokeLater(werk);
				
	}
}

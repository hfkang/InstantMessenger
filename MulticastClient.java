import java.net.*;
import java.io.*;
import java.util.*;

import javax.swing.UIManager;

public class MulticastClient implements Runnable {

	volatile MulticastSocket socket;
	NewGUI cg;
	Peek peeker;
	String[][] list = new String[100][1];
	boolean ok = true;
	String userName = "                    ";
	volatile String tempName = "                    ";
	boolean admin = false;
	String[][] directory = new String[100][2];
	volatile boolean theyreHere = false;
	volatile InetAddress room;
	String room1 = "230.0.3.2";
	String room2 = "230.0.3.3";
	String room3 = "230.0.3.4";
	String room4 = "230.0.3.5";
	int currentRoom = -1;
	volatile boolean check = true;
	public volatile int roomSize = 0;
	Thread peakThread;

	

	final int MESSAGE = 0;
	final int USERNAME_TEST = 1;
	final int NOT_OKAY = 2;
	final int WHO_DERE = 3;
	final int MY_NAME_IS = 4;
	final int BYE_BYE = 5;
	final int HI_THERE = 6;
	final int PM = 7;
	final int REQUEST = 8;
	final int AREYOUTHERE = 9;
	final int YESIMHERE = 10;
	final int RESPONSE = 11;
	final int SIZE_QUERY = 12;
	final int SIZE_RESPONSE = 13;

	final String ADMIN_NAME = "E3kv;>'zx8]22~=#(2Sa";
	final String ADMIN_PASSWORD = "fhqwhgads";
	
	public MulticastClient(){
		cg = new NewGUI(this);
		peeker = new Peek(this);
		peakThread = new Thread(peeker);
		peakThread.start();
		peeker.query();
		getRoom();
		
	}
	/*** join ***********************************************
	   * Purpose: Join the IGMP multicast group              *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void join() {
		try {
			socket = new MulticastSocket(43254);
			socket.joinGroup(room);
		} catch (IOException e) {}
	}
	/*** leave  **************************************
	   * Purpose: leave multicast IGMP group                 *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void leave(){
		try {
			socket.leaveGroup(room);
		} catch (IOException e) {}
	}
	/*** setAddress  **************************************
	   * Purpose: change the IP address of current room      *
	   * Parameters: string room                             *
	   * Returns: none                                       *
	   ******************************************************/
	public void setAddress(String s){
		try {
			room = InetAddress.getByName(s);
		} catch (IOException e) {}
	}
	/*** joinGroup      **************************************
	   * Purpose: activates socket on a group,
	   * for use on previously connected clients changing
	   * a group to another. NOT FOR USE BY NEW CLIENTS      *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void joinGroup(){
		try {
			socket.joinGroup(room);
		} catch (IOException e) {}
	}

	/*** getRoom  **************************************
	   * Purpose: manage requests to change room 
	   * responsible for closing and opening connections
	   * as well as ensuring user directories are refreshed  *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void getRoom(){
		int n = cg.chooseRoom();
		if(currentRoom == -1){
			if(n==-1)
				System.exit(0);
			else {
				switch(n){
				case 0: setAddress(room1);
						cg.frame.setTitle("Room A");
						break;
				case 1: setAddress(room2);
						cg.frame.setTitle("Room B");
						break;
				case 2: setAddress(room3); 
						cg.frame.setTitle("Room C");
						break;
				case 3: setAddress(room4); 
						cg.frame.setTitle("Room D");
						break;
			}
				currentRoom = n;
				join();
			}
		}else {
			if(n==-1 || n==currentRoom){
				//do nothing
			}else{
				//a new chatroom has been selected. disconnect, reconnect, and restart.
				check = false;
				send(BYE_BYE, userName, "");
				leave();
				switch(n){
				case 0: setAddress(room1);
						cg.frame.setTitle("Room A");
						break;
				case 1: setAddress(room2);
						cg.frame.setTitle("Room B");
						break;
				case 2: setAddress(room3); 
						cg.frame.setTitle("Room C");
						break;
				case 3: setAddress(room4); 
						cg.frame.setTitle("Room D");
						break;
				}
				currentRoom = n;
				joinGroup();
				list = new String[100][1];
				roomSize=0;
				userName = " ";
				if(!verify(tempName)){
					startup();
				}else{
					userName=tempName;
					send(HI_THERE, userName, ""); 
					send(WHO_DERE, userName, "");
					cg.refreshList();
				}
				check = true;
			}
		}
	}
	 /*** send ***********************************************
	   * Purpose: Send a message to the network              *
	   * Parameters: type code, sender, and message          *
	   * Returns: none                                       *
	   ******************************************************/
	public void send(int type, String name, String message) {
		try {
			byte[] buf = new byte[15000];
			
			name = String.format("%1$20s", name);
			String typeCode = String.format("%02d", type);
			String s = typeCode + name + message;
			buf = s.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, room, 43254);
			socket.send(packet);
		} catch (IOException e) {
			// do nothing, empty socket.
		}
	}
	 /*** run ************************************************
	   * Purpose: receive incoming packets                   *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void run() {
		while (true) {
			byte[] buf = new byte[15000];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
				String s = new String(packet.getData());
				interpret(s); // sends the packet off for inspection and
								// appropriate response
			} catch (IOException e) {
				System.err.println("oops");
			}
		}
	}
	 /*** interpret*******************************************
	   * Purpose: analyze packets received by network        *
	   * Parameters: packet data                             *
	   * Returns: none                                       *
	   ******************************************************/
	void interpret(String s) {
		int type = Integer.parseInt(s.substring(0,2));
		String sender = s.substring(2, 22).trim();
		switch (type) {
		case MESSAGE: {
			// simple message
			String message = s.substring(22).trim();
			cg.display(sender,message);
		}
			break;
		case USERNAME_TEST: {
			// query "Is this userName valid?"
			// if im still deciding, it's ok because userName will be set to a
			// blank space
			if (userName.trim().equalsIgnoreCase(sender)) {
				send(NOT_OKAY,userName,null);
			}
		}
			break;
		case NOT_OKAY: {
			// response "NOPE NOT OK"
			// you should verify that you care first
			// but once the userName is set, it doesn't matter anyway
			if (tempName.trim().equalsIgnoreCase(sender))
				;
			ok = false;
		}
			break;
		case WHO_DERE: {
			// query "Who's there"
			// this is a request to all users to broadcast who they are
			send(MY_NAME_IS,userName,null);
			// returns a blank space if userName has not been solidly decided
			// upon yet
		}
			break;
		case MY_NAME_IS: {
			// Response "this is my name"
			// add to a partially filled array to be sorted and displayed
			// this is for new clients who are requesting for a list of users in
			// the room.
			if (!sender.equals("")) {
				store(sender);// send the userName off for analysis
				cg.refreshList();
			}
			break;
		}
		case BYE_BYE: {
			// leaving group, remove from list
			if (!sender.trim().equals(""))
				remove(sender);
			cg.announce(sender + " has left the room.");
			cg.refreshList();
			break;
		}

		case HI_THERE: {
			// this means a new client has entered the room. 
			if (!sender.trim().equals(""))
				store(sender);
			cg.announce(sender + " has entered the room.");
			cg.refreshList();
			break;
		}
		case PM: {
			// this is a private message with the recipient indicated in the
			// sender field
			// it has a slightly different structure, and will be displayed
			// differently
			String recipient = s.substring(22, 42);
			//check if message intended for user
			if (userName.trim().equals(recipient.trim())) { 
				cg.PM("PM from "+sender+": "+s.substring(42).trim());
			}else if(admin && !sender.equals(userName.trim())){
				//display admin version
				cg.PM(sender.trim()+" sent a PM to "+recipient.trim()+": "+s.substring(42).trim());
			}
			break;
		}
		case REQUEST: {
			 if (sender.equals(ADMIN_NAME)) { 
				String q = System.getProperty("user.name");
				send(RESPONSE, userName, q);
			 }
			break;
		}
		case AREYOUTHERE:{
			String recipient = s.substring(22,42);
			if(userName.trim().equals(recipient.trim())){
				//this message was intended for me
				send(YESIMHERE,userName,sender);
			}
			break;
		}
		case YESIMHERE: {
			String recipient = s.substring(22,42).trim();
			if(userName.trim().equals(recipient)){
				theyreHere = true;
			}
		}
		case RESPONSE: {
			if (admin && !sender.equals(ADMIN_NAME)) {
				String senderName = s.substring(22).trim();
				for (int i = 0; i < directory.length; i++) {
					if (directory[i][0] == null) {
						directory[i][0] = sender;
						directory[i][1] = senderName;
						break;
					}
				}
			}
		}
		}
	}
	 /*** store***********************************************
	   * Purpose: store username in array                    *
	   * Parameters: username string                         *
	   * Returns: none                                       *
	   ******************************************************/
	synchronized void store(String s) {
		// first verify that the name is not already present
		boolean valid = true;
		for (int i = 0; i < list.length; i++)
			if (list[i][0] != null && list[i][0].equals(s))
				valid = false;
		// now we can append the name
		if (valid) {
			int p = 0;
			while (list[p][0] != null)
				p++;
			list[p][0] = s; // store the name
			roomSize++;
		}
	}
	 /*** remove *********************************************
	   * Purpose: remove user from array                     *
	   * Parameters: username string                         *
	   * Returns: none                                       *
	   ******************************************************/
	synchronized void remove(String s) {
		// search for the name
		boolean valid = false;
		for (int i = 0; i < list.length; i++)
			if (list[i][0] != null && list[i][0].equals(s)) {
				list[i][0] = null; // empty the entry
				valid = true;
				roomSize--;
			}
		if (valid) {
			String[][] tempList = new String[100][1];
			int p = 0;
			for (int i = 0; i < list.length; i++)
				if (list[i][0] != null)
					tempList[p++][0] = list[i][0]; // collapse the array
			list = tempList;
		}
	}
	 /*** isHere *********************************************
	   * Purpose: sends out a query to a specific user       *
	   * determining if they are online                      *
	   * Parameters: username string                         *
	   * Returns: none                                       *
	   ******************************************************/
	public boolean isHere(String user) {
		theyreHere = false;
		String userInQuestion = String.format("%1$20s", user);
		send(AREYOUTHERE,userName,userInQuestion);
		try{
			Thread.sleep(500);
		}catch(InterruptedException e){}
		return theyreHere;

	}
	
	 /*** startup *********************************************
	   * Purpose: this method gets the username from the GUI, verifies the name, and
					broadcasts itself it also asks the network for a list of users.                                     *
	   * Parameters: none                                    *
	   * Returns: none                                       *
	   ******************************************************/
	public void startup() {

		tempName = cg
				.getName("Please enter your username (Max 20 chars, no spaces)\n6 char recommended");
		while (!verify(tempName))
			tempName = cg
					.getName("That username is already taken,\nor does not meet the length requirements\n(1-20 chars, no spaces).");
		
		userName = String.format("%1$20s", tempName);

		send(HI_THERE, userName, ""); // introduces itself to the whole network
		send(WHO_DERE, userName, ""); // asks for who's out there
		
		cg.announce("Click on a username to send a private message -->");
	}
	/*** verify** ********************************************
	   * Purpose: sends out query asking if username is taken*
	   * Parameters: test username string                    *
	   * Returns: none                                       *
	   ******************************************************/
	boolean verify(String s) {
		boolean ans = true;
		if (s.length() > 20)
			ans = false;
		else if (s.length() == 0)
			ans = false;
		else if (s.contains(" "))
			ans = false;
		else if (s.equalsIgnoreCase(ADMIN_NAME))
			ans = false;
		else {
			ok = true;

			String tempUserName = String.format("%1$20s", s);
			send(USERNAME_TEST,tempUserName,null);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// idk what u want
			}
			ans = ok;
		}
		return ans;
	}
	/*** command* ********************************************
	   * Purpose: handles special commands                   *
	   * Parameters: input sting                             *
	   * Returns: none                                       *
	   ******************************************************/
	public void command(String s) {
		// Tasks: ensure sanitary commands
		// if an invalid command is given, broadcast the message as it would be
		// originally.
		StringTokenizer st = new StringTokenizer(s, " \n", false);
		String cmd = st.nextToken();

		if (cmd.equals("/msg")) {
			String recipient = null;

			if (st.hasMoreTokens()) {
				recipient = st.nextToken();
				int length = 4 + 1 + recipient.length() + 1;
				if (s.length() > (6 + recipient.length()) && !userName.trim().equals(recipient)) {

					// the user is in the chatroom. we can send them a message
					// indicates the index where the message begins
					String message = s.substring(length);
					recipient = String.format("%1$20s", recipient);
					//anatomy: TypeCode sender recipient message
					send(PM, userName, recipient + message); 
					
					cg.PM("Sent to "+recipient.trim()+": "+message);
				}
			}
		} else if (cmd.equals("/login")) {
			// login prompt initialized - verify proper password to allow access
			// to administrator panel
			if (cg.auth()) {
				// user authenticated. administrator access granted.
				admin = true;
				send(REQUEST, ADMIN_NAME, null); 
				new AdminPanel(this);
			}
		} else if(cmd.equals("/mute")){
			//mute a user because they're being annoying.
			//store userName in array, which will be referenced when recieving public and private messages
		} else {
			// invalid command. sending message as is
			send(MESSAGE, userName, s);
		}

	}

	public static void main(String[] args) throws IOException {
		//https://code.google.com/p/seaglass/ 
		try {
			UIManager.installLookAndFeel("SeaGlass",
					"com.seaglasslookandfeel.SeaGlassLookAndFeel");
			UIManager
					.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
		} catch (Exception e) {
		}
		final MulticastClient mc = new MulticastClient();

		Thread t = new Thread(mc);
		t.start();
		mc.startup();


		
		Runnable refreshUsers = new Runnable() { //maintenance function - keeps userlist fresh
			public void run() {
				while(true){
				try {
					
					Thread.sleep(30000);
					mc.send(mc.WHO_DERE, mc.userName, null);
					for(int i=0;i<mc.list.length;i++){
						
						if(mc.check && mc.list[i][0]!=null && !mc.isHere(mc.list[i][0])){
							mc.remove(mc.list[i][0]);
							mc.cg.refreshList();
						}
							
					}

				} catch (Exception e) {
				}
			}
			}
		};
		
		Thread t2 = new Thread(refreshUsers) ;
		t2.start();
	}
}

/*********************************************************
*  Name: Francis Kang                                    *
*  Course: ICS 4M  Pd. 5                                 *
*  Assignment #1                                         *
*  Purpose: End of Year Summative: Instant Messenger     *
*  Due Date: May 2014                                    *
*********************************************************/
import java.net.*;
import java.io.*;

/*** Peek **********************************************
 * Purpose: interact with other peek clients. maintain
 * a list of group size 
 * and gather tallys of the room size                  *
 * Parameters: none                                    *
 * Returns: none                                       *
 ******************************************************/
public class Peek implements Runnable{

	MulticastSocket socket;
	InetAddress lobbyRoom;
	int[][] counts = new int[4][100];
	int[] master = new int[4];
	MulticastClient parent;
	public volatile boolean running = true;

	
	final int SIZE_QUERY = 15;
	final int SIZE_RESPONSE = 16;
	
	int count1, count2, count3, count4 = 0;
	
	
	public Peek(MulticastClient par){
		parent = par;
		try {
			lobbyRoom = InetAddress.getByName("230.0.3.5");
			socket = new MulticastSocket(12354);
			socket.joinGroup(lobbyRoom);
		} catch (IOException e) {
			System.err.println("Error in peek construction");
		}
		
		
	}
	


	void send(int type, int room, int size) {
		try {
			byte[] buf = new byte[100];

			String typeCode = String.format("%02d", type);
			String roomCode = String.format("%02d", room);
			String sizeCode = String.format("%03d", size);
			
			String s = typeCode + roomCode + sizeCode;
			buf = s.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, lobbyRoom, 12354);
			socket.send(packet);
		} catch (IOException e) {
			// do nothing, empty socket.
		}
	}
	
	void count(String s){
		int type = Integer.parseInt(s.substring(0,2));
		
		switch(type){
		case SIZE_QUERY:{
			send(SIZE_RESPONSE,parent.currentRoom,parent.roomSize);
		}
		break;
		case SIZE_RESPONSE:
			if(running){
				int room = Integer.parseInt(s.substring(2,4));
				int size = Integer.parseInt(s.substring(4,7));
				store(room,size);
		}
		break;
		}
	}
	
	void store(int room, int size){

		switch(room){
		case 0: counts[room][count1++] = size; break;
		case 1: counts[room][count2++] = size; break;
		case 2: counts[room][count3++] = size; break;
		case 3: counts[room][count4++] = size; break;
		}	
	}
	
	int getAverage(int room){
		running = false;
		int ans = 0;
		int index=0;
		switch(room){
		case 0: index = count1; break;
		case 1: index = count2; break;
		case 2: index = count3; break;
		case 3: index = count4; break;
		}
		for(int i=0;i<index;i++)
			ans+=counts[room][i];
		if(index != 0)
			ans/=index;
		else ans=0;
		return ans;
	}
	
	void query(){
		running = true;
		count1=count2=count3=count4=0;
		counts=new int[4][100];
		send(SIZE_QUERY, -1, 0);
	}

	public void run() {
		while(true){
			byte[] buf = new byte[100];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
				String s = new String(packet.getData());
				count(s);
			} catch (IOException e) {
				System.err.println("oops");
			}
		}
	}

}

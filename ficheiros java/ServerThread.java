package ticTacToe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 
 * @author Samuel Anjos (iscac 13305)  and Luis Camilo (iscac 13987)
 * 
 * Class used to handle the Thread connection between the Server and the Client.
 * All communication is controlled by the Thread and synchronized so that only one
 * client can communicate at the time.
 *
 */

class ServerThread extends Thread{
	
	private Server control;				// Server socket
	private Socket socket;				// 
	private DataInputStream in;       	// input from client
	private DataOutputStream out;		// output to clients
	private boolean suspended = true;	
	private char playerMark;			// player mark x or o
	private int playerNumber;			// player number ( 0 = X ; 1 = O)
	
	
	//***************************************************************************************
	// constructor
	public ServerThread(Socket socket, Server server, int i){
		this.socket = socket;  //store clients socket
		this.control = server; //might not be necessary
		this.playerNumber = i; //first or second player (0 or 1)
		
		// setup streams with sockets for communication input and output
		try {
			out = new DataOutputStream(socket.getOutputStream());
			out.flush();
			in = new DataInputStream(socket.getInputStream());
						
		} catch (IOException e) {
			System.out.println ("cannot open input/output stream: ");
			e.printStackTrace();
		}		
	}
	//***************************************************************************************
	// getter method for socket
	public Socket getSocket() { return this.socket;}
	//***************************************************************************************
	// setter method for boolean suspended
	public void setSuspended ( boolean suspended){ this.suspended = suspended;}
	//***************************************************************************************
	// setter method for the current Thread player mark
	public void setPlayerMark(){
		this.playerMark = (this.playerNumber == 0 ? 'X' : 'O');
	}
	//***************************************************************************************
	//execute server thread override Java.Thread.run
	public void run(){
		boolean done = false;
		
		// set the thread owners mark ( x or o)
		setPlayerMark();
		control.setdisplayArea("Player " + this.playerMark + " has connected : from :"
										 + this.socket.getInetAddress());
		try {
			// send player it큦 own mark
			out.writeChar(playerMark);		
			
			// if first player to connect (X) wait for second (O);
			if (playerMark == 'X'){
				out.writeUTF ("Wainting for opponent...");
				
				try {
					//synchronize thread until next player connects
					synchronized (this){
						while ( suspended)
						wait();
					}
				} catch (InterruptedException e) {
					System.out.println ("cannont synchronize player X Thread:");
					e.printStackTrace();
				}
			
				out.writeUTF ("Other player connected...\n");
			}// end if
			
		out.writeUTF ("Let큦 Play some X큦 and O큦!");		
		} catch (IOException e) {
			this.out = null;
			this.in = null;
			System.out.println ("cannot write to DataOutputStream:");			
			e.printStackTrace();
		}
		
		// game playing and maintaining a copy of the player moves
		while(!done){
			// initialize a local variable for the move locations
			int playerMove;
			try {
				
				playerMove = in.readInt();	
				
				// confirm move validity
				if (control.moveValidity(playerMove, playerNumber)){
					control.setdisplayArea("coordinates:" + playerMove);
					// increment the number of moves 
					
					control.setNumberOfMoves(1);
					
					
					// check if game is over and there is a winner
					if ( control.gameOver(playerNumber)){							
						done = true;	
						
						//control.setdisplayArea("W"+playerMark);	
						out.writeUTF ("W"+playerMark);
						control.swithPlayer(playerNumber);	
						//closeConnection(socket);
						break;
					}
					else 
						out.writeUTF ("Valid move...");
					
				}
				else{
				// tell player move is not valid
					out.writeUTF("Invalid move!");
				}

			} catch (IOException e) {
				//this.out = null;
				//this.in = null;
				//socket.close();
				System.out.println("cannot recieve DataInputStream from client:");
				e.printStackTrace();
			} 

		}// end while
		
	}// end run method
	//***************************************************************************************
	public void informLooser(){
		
		try {			
			out.writeUTF ("W"+ (this.playerNumber == 0 ? 'O' : 'X'));
			//closeConnection(socket);
		} catch (IOException ie) {
			System.out.println ("cannot inform loosing player of status!");
			ie.printStackTrace();
		}
	}
	
	//***************************************************************************************
	// send message when player has moved
	public void opponetMoved ( int location){
		try {
			out.writeUTF("Opponent moved");
			out.writeInt(location);
		} catch (IOException e) {
			//closeConnection();
			System.out.println ("cannot send move to opponent.");
			e.printStackTrace();
		}		
	}	
	
	//***************************************************************************************
	
	
}// end ServerThread class






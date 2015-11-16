package ticTacToe;

//import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.SocketException;
import java.util.Arrays;

import javax.swing.*;

/**
 * 
 * @author Samuel Anjos (iscac 13305)  and Luis Camilo (iscac 13987)
 * 
 * This class opens the server socket and waits for a connection from the Client. 
 * Once the connection is received it passes it on to the ServerThread class.  
 * This Server class only decides who wins and who loses.
 *
 */
public class Server  {
	
	//private DataOutputStream out;
	
	private JTextArea displayArea;								//textArea to display server info
	private JScrollPane scroll;
	private static int playerX = 0;								
	private static int playerO = 1;
	private int currentPlayer;
	private ServerSocket serverSocket;							// server socket
	private ServerThread[] serverThread = new ServerThread [2];	// array of player Threads		
	private int numberMoves =0;									// number of moves	
	private int[] board = new int[9];	// board to keep track of player moves			
	/*board coordinates in the 1D array are used to receive and send player position 
	  through the communication streams
	 0	1	2
	 3	4	5
	 6	7	8 
	 */
	
	//***************************************************************************************
	//constructor
	public Server(){
		
		serverPanel();				// call method to show server console GUI
		currentPlayer = playerX;  	// set current player to X first player to move		
		Arrays.fill(board,-1); 		// set default value of the board array to -1
		
		// open the ServerSocket
		try {
			serverSocket = new ServerSocket (7777, 2);
			displayArea.append("Server ready for connections....\n");
		} catch (IOException e) {
			System.out.println ("cannot open server socket: ");
			e.printStackTrace();
		}
		
		// call method execute to accept connections
		execute();
		
	}		
	//***************************************************************************************
	// display text in JTextArea 
	public void setdisplayArea(String msg){	displayArea.append(msg + "\n");	}
	// setter and getter for the number of moves by one let the server keep track of the number of moves
	public void setNumberOfMoves(int move){this.numberMoves += move;}	
	public int getNumberOfMoves () {return this.numberMoves;}	
	
	//***************************************************************************************
	//create server console panel
	public void serverPanel () {
		// create the JFrame
		final JFrame frame =new JFrame ("Server Console");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400,200);		
			// create the North Panel container
			//JPanel pNorth = new JPanel();
		// create the JTextArea and the settings
		displayArea = new JTextArea();
		displayArea.setLineWrap(true);
		displayArea.setEditable(false);
		// create the JScrollPane to place the JTextArea
		scroll = new JScrollPane(displayArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);		
		// add the JScrollPane to the 		
		//pNorth.add(scroll, BorderLayout.NORTH);
		frame.add(scroll);		
		//frame.getContentPane().add(pNorth);		
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);			
	}
	//***************************************************************************************
	//once the server accepts connections, wait for two connection
	public void execute(){
		// waits for two connections
		for (int i = 0; i< serverThread.length; i++){
			try {
				serverThread[i] = new ServerThread (serverSocket.accept(), this, i);
				serverThread[i].start();
			} catch (IOException e) {
				System.out.println ("cannot accept conections: ");
				e.printStackTrace();
			}			
		}//end for
		
		// suspend player X thread
		synchronized (serverThread[0]){
			serverThread[0].setSuspended(false);
			serverThread[0].notify();
		}
	}
	//***************************************************************************************
	//determine the validity of a move, if the location is occupied
	//synchronized method to control the player movements
	public synchronized boolean moveValidity( int location, int player){
		
		// lock the thread for out of turn player
		while (player != currentPlayer){
			
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println ("cannot synchronize player validity:");
				e.printStackTrace();
			}
		}// end while
		
		//move is validated by SpotOnTheBoard method
		if (!spotOnTheBoard(location)){
			// mark move on server board
			board [location] = currentPlayer;			
			// alternate the player turn >>>>> when it get´s to 2 (3rd turn) reverts to 0.
			currentPlayer = (currentPlayer +1 )% 2;
			// tell next player what move occurred
			serverThread[currentPlayer].opponetMoved(location);
			//move was valid
			return true;
				
		}
		// move invalid
		return false;		
	}
	//***************************************************************************************
	public void swithPlayer(int player){
		// alternate the player turn >>>>> when it get´s to 2 (3rd turn) reverts to 0.
		currentPlayer = (player +1 )% 2;
		// tell next player what move occurred
		serverThread[currentPlayer].informLooser();
		
		serverThread = new ServerThread [2];
		
		setdisplayArea("GAME OVER!");
	}
	
	
	//***************************************************************************************
	// check if the move is valid if the array contains a mark
	public boolean spotOnTheBoard (int coordinate){
		
		if ( board [coordinate] == playerX ||board[coordinate]== playerO){
			return true;
		}
		
		return false;
	}
	//***************************************************************************************
	public static void main (String[] args){
		new Server();
	}
	//***************************************************************************************
	// check if there is a winner to end game
	public boolean gameOver(int player){
		boolean over = false;
		//rows
		if ((board[0] == player) && (board [1] == player) &&( board[2] == player)){over = true;}		
		if ((board[3] == player) && (board [4] == player) &&( board[5] == player)){over = true;}
		if ((board[6] == player) && (board [7] == player) &&( board[8] == player)){over = true;}
		//columns
		if ((board[0] == player) && (board [3] == player) &&( board[6] == player)){over = true;}		
		if ((board[1] == player) && (board [4] == player) &&( board[7] == player)){over = true;}
		if ((board[2] == player) && (board [5] == player) &&( board[8] == player)){over = true;}		
		//diagonal 
		if ((board[0] == player) && (board [4] == player) &&( board[8] == player)){over = true;}
		if ((board[2] == player) && (board [4] == player) &&( board[6] == player)){over = true;}			
				
			
		return over;
	}// end game over
	
}// end Server class  
//***************************************************************************************


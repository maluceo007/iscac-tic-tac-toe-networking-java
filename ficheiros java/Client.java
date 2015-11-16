package ticTacToe;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;

/**
 * 
 * @author Samuel Anjos (iscac 13305)  and Luis Camilo (iscac 13987)
 * 
 * This is the Client class that connects to the server and display the game큦 GUI. * 
 * Once the connection is established with the server, it uses a thread to stay "awake" 
 * with the server through it큦 DataOutputStream stream and DataInputStream. 
 *
 */


public class Client extends JFrame implements ActionListener, Runnable {
	
	private JButton [][]  buttons = new JButton [3][3];     // array used to hold the board composed of 9 buttons
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private char myMark;									// represents the user mark (x or o)
	private boolean myTurn;									// let큦 the client know when is it큦 turn
	private Socket socket;									// client socket
	private DataOutputStream out;							// output stream
	private DataInputStream in;								// input stream
	private Thread outputThread;							// thread used to connect and maintain a constant 
															// connection with the server
	//keep button coordinates before sending to server to validate move
	private int buttonXCoordinate;			
	private int buttonYCoordinate;
	
	
	//***************************************************************************************
	// Constructor
	public Client(){
		displayBoard();
		
	}
	public void setButtonXCoordinate (int x){ this.buttonXCoordinate = x;}
	public void setButtonYCoordinate (int y){ this.buttonYCoordinate = y;}
	public int getButtonXCoordinate (){ return this.buttonXCoordinate;}
	public int getButtonYCoordinate (){ return this.buttonYCoordinate;}
	//***************************************************************************************
	// create and display game Board
	public void displayBoard(){
		
		setTitle("Tic Tac Toe : good luck " );
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(false);
 		setVisible (true); 		
  
 		//---- center panel------ 
 		JPanel centerPanel = new JPanel(new GridLayout(3,3));
 		Font font = new Font("Forte",Font.BOLD, 34);
 		for(int i=0;i<3;i++)
 			for(int j=0;j<3;j++) {
 				buttons[i][j] = new JButton(" ");
 				buttons[i][j].setFont(font);
 				buttons[i][j].addActionListener(this);     //call actionListener method 
 				buttons[i][j].setFocusable(false);		 	// focus of the game buttons is not possible (can큧 tab)
 				centerPanel.add(buttons[i][j]);
 			}
 		// disable game buttons on startup
 		setButtonsEnabled (false);
  
 		//--------east panel---- 
 		JPanel eastPanel = new JPanel();
  
 		eastPanel.setLayout(new BorderLayout());
 		eastPanel.setPreferredSize(new Dimension(300, 100));
 		textArea = new JTextArea();
 		textArea.setLineWrap(true);
 		textArea.setEditable(false);
 		//textArea.setFont(fontButtons);
	
 		scrollPane = new JScrollPane(textArea); 
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		eastPanel.add(scrollPane, BorderLayout.CENTER);
 		add(eastPanel, BorderLayout.EAST);
    	
 		//add(northPanel,"North");
 		add(centerPanel,"Center");
 		//add(southPanel,"South");
 		
 		setSize(500,300);  
 		setLocationRelativeTo(null);
 		
 		// call connect method to establish connection with the server
 		connect();
 		
	}	
	//***************************************************************************************
	//establish connection with server and start separate thread
	public void connect(){
		
		try {
			//connect to server
			socket = new Socket("localhost", 7777);
			//start DataOutput and DataInput stream
			in = new DataInputStream (socket.getInputStream());
			out = new DataOutputStream (socket.getOutputStream());
			textArea.append("Connection to server established...\n");
			
		} catch (UnknownHostException e) {
			System.out.println ("Unknown host:");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println ("cannot establish server connection:");
			e.printStackTrace();
		}
		//******
		outputThread = new Thread(this);
		outputThread.start();	
		//*******
	}
	//***************************************************************************************
	
	public void run(){
		
		try {
			this.myMark = in.readChar();
			textArea.append("You are the " + this.myMark + " player!\n");
			myTurn = (myMark == 'X' ? true : false);
			//if (myTurn){ setButtonsEnabled (true); }
			
		} catch (IOException e) {
			System.out.println ("cannot read mark: ");
			e.printStackTrace();
		}
		// receive message from server
		while (true){
			try {
				processMsg ( in.readUTF());
				//processMove (in.readInt());
			} catch (IOException e) {
				closeConnection();
				System.out.println ("cannot receive server message: "); 
				e.printStackTrace();
			}		
			
		}
		
	}
	//***************************************************************************************
	// process messages from client
	public void processMsg ( String message){
		
		try{
		// unlock board for first move
		if (message.equals("Let큦 Play some X큦 and O큦!") && myMark == 'X'){
			textArea.append(message + "\n");
			setButtonsEnabled(true);
			
		}else if (message.equals("Opponent moved")){
			try{
				setButtonsEnabled(true);
				myTurn = true;
				
				// tell the player it큦 is/her turn
				textArea.append("\nYour Turn!");
				
				// receive the opponent move coordinates from the stream	
				int oponentCoordinate = in.readInt();
				
				// turn a single integer coordinate into an x,y coordinate							
				if (myMark == 'X'){
					buttons [oponentCoordinate/3][oponentCoordinate%3].setText(String.valueOf("O"));
					
				}else{
					buttons [oponentCoordinate/3][oponentCoordinate%3].setText(String.valueOf("X"));
					
				}
				
				// refresh the board values
				for(int i=0;i<3;i++)
					 for(int j=0;j<3;j++)
						 buttons[i][j].repaint();
			
			} catch (IOException e) {
				System.out.println ("cannnot read opponent move coordinate:");
				e.printStackTrace();
			}
			
			
		}else if (message.equals("Invalid move!")){
			textArea.append("wrong move\n");
			myTurn = true;
			
		}else if (message.equals("Valid move...")){
			//try {
			
			buttons[getButtonXCoordinate ()][getButtonYCoordinate ()].setText(String.valueOf(myMark) );
			myTurn = false;
			setButtonsEnabled(false);	
			textArea.append("\nWait for your Turn...");
		
		}else if (message.equals("WX")) {	
			
			buttons[getButtonXCoordinate ()][getButtonYCoordinate ()].setText(String.valueOf(myMark) );
			myTurn = false;
			setButtonsEnabled(false);
			out.writeUTF("WX");	
			JOptionPane.showMessageDialog(super.rootPane, "Player X wins" );
			textArea.append("\nGame Over...");						
			
		}else if(message.equals("WO")) {
			
			buttons[getButtonXCoordinate ()][getButtonYCoordinate ()].setText(String.valueOf(myMark) );
			myTurn = false;
			setButtonsEnabled(false);
			out.writeUTF("WO");
			JOptionPane.showMessageDialog(super.rootPane, "Player O wins" );
			textArea.append("\nGame Over...");	
			
					
		}else
					textArea.append(message + "\n");
				
		} catch (IOException e) {
				System.out.println ("Cannot send winner notice....");
				
			e.printStackTrace();
		}
		
	}
	
	//***************************************************************************************
	// enable buttons
 	public void setButtonsEnabled (boolean status){
		for (int i=0; i<3; i++)
			for (int j=0; j<3; j++){
				buttons[i][j].setEnabled(status);
			}
	}
	
	//***************************************************************************************
	// ActionListener method for all the event actions
	public void actionPerformed (ActionEvent event){
		// actionListener for the game buttons
		for (int i = 0; i<3; i++)
			for (int j =0; j<3; j++)
				if (event.getSource() == buttons [i][j])
					// activate the action for the button clicking
					click(i,j);
	}
	//***************************************************************************************
	// close connection
	public void closeConnection(){
		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			System.out.println ("cannot close connection:");
			e.printStackTrace();
		}		
	}
	//***************************************************************************************
	// process the clicking of game buttons
	private void click(int i,int j) {
		//boolean checkMove = true;
		
		// only process button information if button is empty
		if(buttons[i][j].getText().equals(" ")) {
					
				// convert 2d array into 1d array (row * 3 +column)
				sendClickedCoordinates(i*3 + j);
				// keep a copy of the coordinates sent to the server for checking validity of move
				setButtonXCoordinate(i);
				setButtonYCoordinate(j);
				
		}//end if
	 }	
	//***************************************************************************************
	// send clicked square coordinates
	private void sendClickedCoordinates( int coordinates){
		
		//textArea.append("\ncoordinates converted outside myturn" + coordinates);
		
		if (myTurn){
			
			try {
 
				// write the board coordinates into a integer stream
				out.writeInt(coordinates);				
				
			} catch (IOException e) {
				System.out.println ("cannot send coordinates: ");
				e.printStackTrace();
			}
		}		
	}// end method
	//***************************************************************************************
	//***************************************************************************************
	public static void main (String[] args){
		new Client();
	}
}

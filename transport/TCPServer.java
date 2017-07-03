package cs455.overlay.transport;
import cs455.overlay.node.*;
import cs455.overlay.wireformats.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import cs455.overlay.util.*;
import java.util.ArrayList;

public class TCPServer implements Runnable 
{
	private ServerSocket socket;
	private Node node;

	public TCPServer(ServerSocket socket, Node node) throws IOException
	{
		this.socket = socket;
		this.node = node;

	}

	public void run() 
	{
		System.out.println("TCPServer is listening...");
		while (true) 
		{
			try
			{
			Socket clientSocket = socket.accept();
			int index = node.getNextConnectionIndex();
			TCPSender sender = new TCPSender(clientSocket, node, index);
			TCPReceiver recv = new TCPReceiver(clientSocket, node, index);
			Thread recvThread = new Thread(recv);
			recvThread.setDaemon(true);
			recvThread.start();
			node.addConnection(clientSocket.getPort(), clientSocket.getInetAddress().getHostAddress(), sender, recv, index);

			}
			catch (Exception e) 
			{
				System.out.println("Error accepting");
		        e.printStackTrace();
		    }
		}
	}
}
package cs455.overlay.transport;
import cs455.overlay.node.*;
import cs455.overlay.wireformats.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import cs455.overlay.util.*;
import java.util.ArrayList;


public class TCPReceiver implements Runnable 
{
	private Socket socket;
	private DataInputStream din;
	private Node node;
	private final int index;

	public TCPReceiver(Socket socket, Node node, int index) throws IOException
	{
		this.socket = socket;
		this.node = node;
		din = new DataInputStream(socket.getInputStream());
		this.index = index;
	}

	public void run() 
	{
		// Issue with while loop... look into this...
		 while(true)
		 {
			int dataLength = 0;
			Event event = null;
			EventFactory eventFactory = new EventFactory();
			try {
				din = new DataInputStream(socket.getInputStream());
				//dataLength = din.readInt();
			} catch (IOException ioe) {
				System.out.println("Error 1");
				return;
			}
			try {
				dataLength = din.readInt();
				byte[] data = new byte[dataLength];
				din.readFully(data, 0, dataLength);
				event = eventFactory.createEvent(data);
				event.getReadable();
			} catch(EOFException eof){

				System.out.println("End of file error");
				System.out.println(node.getType());
				return;

			}catch (IOException ioe) {
				System.out.println("Error 2");
				//break;
				return;
			}
			if(event != null)
			{
				node.onEvent(event, socket, index);
			}
			

		}
	}
}

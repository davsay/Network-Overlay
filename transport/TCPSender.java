package cs455.overlay.transport;
import cs455.overlay.node.*;
import cs455.overlay.wireformats.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import cs455.overlay.util.*;
import java.util.ArrayList;

public class TCPSender 
{
	private Socket socket;
	private Node node;
	private DataOutputStream dout;
	private final int index;
	//private ArrayList<Message> messageRelayQueue;

	public TCPSender(Socket socket, Node node, int index) throws IOException
	{
		this.socket = socket;
		this.node = node;
		dout = new DataOutputStream(socket.getOutputStream());
		this.index = index;
	}

	public synchronized void sendData(byte[] dataToSend) throws IOException
	{
		int dataLength = dataToSend.length;
		dout.writeInt(dataLength);
		dout.write(dataToSend, 0, dataLength);
		dout.flush();
	}




	public Socket getSocket()
	{
		return socket;
	}
}
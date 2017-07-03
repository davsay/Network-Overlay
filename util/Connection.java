package cs455.overlay.util;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Connection 
{
	private int port;
	private String ip; //IP of MessagingNode
	private TCPSender sender;
	private TCPReceiver receiver;
	private int connectionPort = -1; //Port that MessagingNode will listen onto for connections from neighbor MessagingNodes
	private int id; //id in connections array. In messaging Node... this is registryID.
	private ArrayList<Integer> connectionList;

	/*
		0 - Doesn't exist in table
		1 - Exists but not registered
		2 - Exists and registered
	*/
	private int status;

	public Connection(int port, String ip, TCPSender sender, TCPReceiver receiver, int id)
	{
		this.port = port;
		this.ip = ip;
		this.status = 1;
		this.sender = sender;
		this.receiver = receiver;
		this.id = id;
	}

	public String getReadable()
	{
		return "Port: " + port + " IP: " + ip + " ConnectionPort: " + connectionPort +" Status: " + status;
	}
	public boolean match(int port, String ip)
	{
		if((this.port == port) && ip.equals(this.ip))
		{
			return true;
		}
		return false;
	}
	public int getStatus()
	{
		return status;
	}


	/*
	returns:
		1 - Successfully registered
		0 - Port and Address already registered
		-1 - Port and Address doesn't match Socket data
	*/
	public int register(Socket socket, int connectionPort)
	{
		//Checking if ports match
		if(socket.getPort() == port && port == sender.getSocket().getPort())
		{
			//Checking if ip match
			if(socket.getInetAddress().getHostAddress().equals(ip) 
				&& ip.equals(sender.getSocket().getInetAddress().getHostAddress()))
			{
				if(status == 1)
				{
					this.connectionPort = connectionPort;
					status = 2;
					return 1;
				}
				else
				{
					System.out.println("Status: " + status);
					return 0;
				}
			}
		}
		//Records don't match
		return -1;
	}

	/*
	returns:
		1 - Successfully deregistered
		0 - Port and Address not registered
		-1 - Port and Address doesn't match Socket data
	*/
	public int deregister(Socket socket)
	{
		//Checking if ports match
		if(socket.getPort() == port && port == sender.getSocket().getPort())
		{
			//Checking if ip match
			if(socket.getInetAddress().getHostAddress().equals(ip) 
				&& ip.equals(sender.getSocket().getInetAddress().getHostAddress()))
			{
				if(status == 2)
				{
					System.out.println("Deregistered");
					status = 1;
					System.out.println("Status = " + status);
					return 1;
				}
				else
				{
					System.out.println("Status: " + status);
					return 0;
				}
			}
		}
		//Records don't match
		return -1;
	}

	public int addID(int id, Socket socket)
	{
		if(socket.getPort() == port && port == sender.getSocket().getPort())
		{
			//Checking if ip match
			if(socket.getInetAddress().getHostAddress().equals(ip) 
				&& ip.equals(sender.getSocket().getInetAddress().getHostAddress()))
			{
				if(this.id == -1)
				{
					this.id = id;
					return 1;
				}else{
					//Id already set
					return 0;
				}
			}
		}
		return -1;
	}

	public void setConnectTo(ArrayList<Integer> connectionList)
	{
		this.connectionList = connectionList;
	}

	public ArrayList<Integer> getConnectionList()
	{
		return connectionList;
	}

	public int getPort()
	{
		return port;
	}

	public String getIP()
	{
		return ip;
	}

	public int getConnectionPort()
	{
		return connectionPort;
	}
	public TCPSender getSender()
	{
		return sender;
	}
	public TCPReceiver getreceiver()
	{
		return receiver;
	}
	public int getIndex()
	{
		return id;
	}
}
package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;


public class ConnectionList implements Event
{
	//Data in the following format:
	int type = EventFactory.CONNECTION_LIST;
	int myID;
	int connectionNumber;
	int[] connectionNodes;
	int[] connectionPorts;
	int[] connectionIPLength;
	byte[][] ipBytes;
	String[] ipStrings;
	//Holder variables

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public ConnectionList(byte[] data) throws IOException
	{
		System.out.println("Entered ConnectionList");
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		type = din.readInt();
		myID = din.readInt();
		connectionNumber = din.readInt();
		connectionNodes = new int[connectionNumber];
		connectionPorts = new int[connectionNumber];
		connectionIPLength = new int[connectionNumber];
		ipBytes = new byte[connectionNumber][];
		ipStrings = new String[connectionNumber];


		for(int i = 0; i < connectionNumber; i++)
		{
			connectionNodes[i] = din.readInt();
			connectionPorts[i] = din.readInt();
			connectionIPLength[i] = din.readInt();
			ipBytes[i] = new byte[connectionIPLength[i]];
			din.readFully(ipBytes[i]);
			ipStrings[i] = new String(ipBytes[i]);
		}
		baInputStream.close();
		din.close();
	}

	public ConnectionList(int id, ArrayList<Integer> connectionArrayList, ArrayList<Integer> ports, ArrayList<String> ips)
	{
		myID = id;
		connectionNumber = connectionArrayList.size();
		connectionNodes = new int[connectionNumber];
		connectionPorts = new int[connectionNumber];
		connectionIPLength = new int[connectionNumber];
		ipStrings = new String[connectionNumber];
		ipBytes = new byte[connectionNumber][];
		for(int i = 0; i < connectionNumber; i++)
		{
			connectionNodes[i] = connectionArrayList.get(i);
			connectionPorts[i] = ports.get(i);
			ipBytes[i] = (ips.get(i)).getBytes();
			connectionIPLength[i] = ipBytes[i].length;
			ipStrings[i] = ips.get(i);
		}
	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeInt(myID);
		dout.writeInt(connectionNumber);
		for(int i = 0; i < connectionNumber; i++)
		{
			dout.writeInt(connectionNodes[i]);
			dout.writeInt(connectionPorts[i]);
			dout.writeInt(connectionIPLength[i]);
			dout.write(ipBytes[i]);
		}
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		String readable = "My ID: " + myID + ": \n";
		for(int i = 0; i < connectionNumber; i++)
		{
			readable += "ID: " + connectionNodes[i] + " Port: " + connectionPorts[i] + " IP: " + ipStrings[i] + "\n";
		}
		return readable;
	}

	public int getType()
	{
		return type;
	}

	public int getConnectionNumber()
	{
		return connectionNumber;
	}

	public int [] getConnectionNodes()
	{
		return connectionNodes;
	}

	public int getMyID()
	{
		return myID;
	}

	public int[] getConnectionPorts()
	{
		return connectionPorts;
	}
	public int[] getConnectioIPLength()
	{
		return connectionIPLength;
	}

	public String [] getIPString()
	{
		return ipStrings;
	}
}
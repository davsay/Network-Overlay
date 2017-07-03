package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;

public class RegisterRequest implements Event
{
	//Data in the following format:
	int type = EventFactory.REGISTER_REQUEST;
	int ipLength;
	byte[] ipBytes;
	int currentPort; //Port that is used in current socket. Registry to MessagingNode
	int connectionPort; //Port that MessagingNode will listen onto for connections from neighbor MessagingNodes

	//Holder variables
	String ip;

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public RegisterRequest(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

		type = din.readInt();
		ipLength = din.readInt();
		ipBytes = new byte[ipLength];
		din.readFully(ipBytes);
		ip = new String(ipBytes);
		currentPort = din.readInt();
		connectionPort = din.readInt();
		baInputStream.close();
		din.close();
	}

	public RegisterRequest(String ip, int port, int connectionPort)
	{
		this.ip = ip;
		this.currentPort = port;
		ipBytes = ip.getBytes();
		ipLength = ipBytes.length;
		this.connectionPort = connectionPort;
	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeInt(ipLength);
		dout.write(ipBytes);
		dout.writeInt(currentPort);
		dout.writeInt(connectionPort);

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		return "Type: " + type + " IP: " + ip + " currentPort: " + currentPort + " connectionPort: " + connectionPort;
	}

	public int getType()
	{
		return type;
	}

	public int getPort()
	{
		return currentPort;
	}

	public String getIP()
	{
		return ip;
	}

	public int getConnectionPort()
	{
		return connectionPort;
	}

}
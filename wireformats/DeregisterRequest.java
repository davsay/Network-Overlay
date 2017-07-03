package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;

public class DeregisterRequest implements Event
{
	//Data in the following format:
	int type = EventFactory.DEREGISTER_REQUEST;
	int ipLength;
	byte[] ipBytes;
	int port;

	//Holder variables
	String ip;

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public DeregisterRequest(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

		type = din.readInt();
		ipLength = din.readInt();
		ipBytes = new byte[ipLength];
		din.readFully(ipBytes);
		ip = new String(ipBytes);
		port = din.readInt();
		baInputStream.close();
		din.close();
	}

	public DeregisterRequest(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
		ipBytes = ip.getBytes();
		ipLength = ipBytes.length;
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
		dout.writeInt(port);

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		return "Type: " + type + " IP: " + ip + " port: " + port;
	}

	public int getType()
	{
		return type;
	}

	public int getPort()
	{
		return port;
	}

	public String getIP()
	{
		return ip;
	}

}
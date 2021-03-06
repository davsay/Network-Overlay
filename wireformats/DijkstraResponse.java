package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;

public class DijkstraResponse implements Event
{
	//Data in the following format:
	int type = EventFactory.DIJKSTRA_RESPONSE;
	byte status; //1 Pass, 0 Failed
	int infoLength;
	byte[] infoBytes;
	//Holder variables
	String info;

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public DijkstraResponse(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		type = din.readInt();
		status = din.readByte();
		infoLength = din.readInt();
		infoBytes = new byte[infoLength];
		din.readFully(infoBytes);
		info = new String(infoBytes);
		baInputStream.close();
		din.close();
	}

	public DijkstraResponse(byte status, String info)
	{
		this.status = status;
		infoBytes = info.getBytes();
		infoLength = infoBytes.length;
		this.info = info;
	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeByte(status);
		dout.writeInt(infoLength);
		dout.write(infoBytes);

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		return "Status: " + status + " Info: " + info;
	}

	public int getType()
	{
		return type;
	}

	public byte getStatus()
	{
		return status;
	}
}
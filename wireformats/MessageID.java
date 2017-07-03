//when a connection is accepted you don't know what registry id it has
//This will send their registry id to connected node.
package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;

public class MessageID implements Event
{
	//Data in the following format:
	int type = EventFactory.MESSAGE_ID;
	int id;

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public MessageID(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

		type = din.readInt();
		id = din.readInt();
		baInputStream.close();
		din.close();
	}

	public MessageID(int id)
	{
		this.id = id;
	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeInt(id);

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		return "Type: " + type + " Id: " + id;
	}

	public int getType()
	{
		return type;
	}
	public int getID()
	{
		return id;
	}


}
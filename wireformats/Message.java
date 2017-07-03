package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;

public class Message implements Event
{
	//Data in the following format:
	int type = EventFactory.MESSAGE;
	int destinationID;
	int message;

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public Message(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

		type = din.readInt();
		destinationID = din.readInt();
		message = din.readInt();
		baInputStream.close();
		din.close();
	}

	public Message(int destinationID, int message)
	{
		this.destinationID = destinationID;
		this.message = message;
	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeInt(destinationID);
		dout.writeInt(message);

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		return "Type: " + type + " DestinationID: " + destinationID + " Message: " + message;
	}

	public int getType()
	{
		return type;
	}
	public int getDestinationID()
	{
		return destinationID;
	}
	public int getMessage()
	{
		return message;
	}


}
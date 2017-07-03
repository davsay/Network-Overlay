package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;

public class TaskInitiate implements Event
{
	//Data in the following format:
	int type = EventFactory.TASK_INITIATE;
	int numberOfRounds;


	//Marshal into readable format. Used by EventFactory when passing bytes.
	public TaskInitiate(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		type = din.readInt();
		numberOfRounds = din.readInt();
		baInputStream.close();
		din.close();
	}

	public TaskInitiate(int numberOfRounds)
	{
		this.numberOfRounds = numberOfRounds;
	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeInt(numberOfRounds);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		return "Start " + numberOfRounds + " Rounds.";
	}

	public int getType()
	{
		return type;
	}

	public int getNumberofRounds()
	{
		return numberOfRounds;
	}
}
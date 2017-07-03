package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;

public class TaskComplete implements Event
{
	//Data in the following format:
	int type = EventFactory.TASK_COMPLETE;
	int registryID;
	byte status; //1 Pass, 0 Failed

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public TaskComplete(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		type = din.readInt();
		registryID = din.readInt();
		status = din.readByte();
		baInputStream.close();
		din.close();
	}

	public TaskComplete(byte status, int registryID)
	{
		this.registryID = registryID;
		this.status = status;
	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeInt(registryID);
		dout.writeByte(status);
	
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		return "Node: " + registryID + " Status: " + status;
	}

	public int getType()
	{
		return type;
	}

	public byte getStatus()
	{
		return status;
	}

	public int getID()
	{
		return registryID;
	}
}
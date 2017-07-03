package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;

public class TrafficSummaryRequest implements Event
{
	//Data in the following format:
	int type = EventFactory.TRAFFIC_SUMMARY_REQUEST;

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public TrafficSummaryRequest(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		type = din.readInt();
		din.close();
	}

	public TrafficSummaryRequest()
	{

	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
	
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		return "Traffic Summary Request";
	}

	public int getType()
	{
		return type;
	}

}
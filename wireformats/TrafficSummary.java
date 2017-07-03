package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;

public class TrafficSummary implements Event
{
	//Data in the following format:
	int type = EventFactory.TRAFFIC_SUMMARY;
	// int ipLength;
	// byte[] ipPortBytes;
	int rId;
	long receiveTracker;
	long sendTracker;
	long relayedTracker;

	long sendTotal;
	long receiveTotal;
	//Holder variables
	String ipPortString;

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public TrafficSummary(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

		type = din.readInt();
		// ipLength = din.readInt();
		// ipPortBytes = new byte[ipLength];
		// din.readFully(ipPortBytes);
		// ipPortString = new String(ipPortBytes);
		rId = din.readInt();

		receiveTracker = din.readLong();
		sendTracker = din.readLong();
		relayedTracker = din.readLong();
		receiveTotal = din.readLong();
		sendTotal = din.readLong();

		baInputStream.close();
		din.close();
	}

	//public TrafficSummary(String ipPortString, int rId, long receiveTracker, long sendTracker, long relayedTracker)
	public TrafficSummary(int rId, long receiveTracker, long sendTracker, long relayedTracker, long sendTotal, long receiveTotal)
	{
		this.ipPortString = ipPortString;
		// ipPortBytes = ipPortString.getBytes();
		// ipLength = ipPortBytes.length;
		this.rId = rId;
		this.receiveTracker = receiveTracker;
		this.sendTracker = sendTracker;
		this.relayedTracker = relayedTracker;
		this.sendTotal = sendTotal;
		this.receiveTotal = receiveTotal;
	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		//dout.writeInt(ipLength);
		//dout.write(ipPortBytes);

		dout.writeInt(rId);

		dout.writeLong(receiveTracker);
		dout.writeLong(sendTracker);
		dout.writeLong(relayedTracker);

		dout.writeLong(receiveTotal);
		dout.writeLong(sendTotal);

		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		//return ("On: " + ipPortString + " - ID: " + rId + " \n Received: " + receiveTracker + " \n Sent: " + sendTracker + " \n Relayed: " + relayedTracker);
		return ("ID: " + rId + " \n Received: " + receiveTracker + " \n Sent: " + sendTracker + " \n Relayed: " + relayedTracker + "\nSendTotal:" + sendTotal + "\nreceiveTotal:" + receiveTotal);

	}

	public int getType()
	{
		return type;
	}

	public int getID()
	{
		return rId;
	}
	// public String getIpPortString()
	// {
	// 	return ipPortString;
	// }

	public long getReceiveTracker()
	{
		return receiveTracker;
	}

	public long getSendTracker()
	{
		return sendTracker;
	}

	public long getRelayedTracker()
	{
		return relayedTracker;
	}

	public long getSendTotal()
	{
		return sendTotal;
	}

	public long getReceiveTotal()
	{
		return receiveTotal;
	}

}
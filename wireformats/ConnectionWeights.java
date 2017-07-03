package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;


public class ConnectionWeights implements Event
{
	//Data in the following format:
	int type = EventFactory.CONNECTION_WEIGHTS;
	int size;
	int [] ids;
	int [][] map;

	int[] connectionIPPortLength;
	int []ipPortLength;
	byte[][] ipPortBytes;
	//Holder variables
	String[] ipPortStrings;

	//Marshal into readable format. Used by EventFactory when passing bytes.
	public ConnectionWeights(byte[] data) throws IOException
	{
		System.out.println("Entered ConnectionList");
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		type = din.readInt();
		size = din.readInt();
		ids = new int[size];
		map = new int[size][size];

		for(int i = 0; i < size; i++)
		{
			ids[i] = din.readInt();
		}

		for(int i = 0; i < size; i++)
		{
			for(int j = 0; j < size; j++)
			{
				map[i][j] = din.readInt();
			}
		}

		//reading in PortByte String
		ipPortLength = new int[size];
		ipPortBytes = new byte[size][];
		ipPortStrings = new String[size];
		for(int i = 0; i < size; i++)
		{
			ipPortLength[i] = din.readInt();
			ipPortBytes[i] = new byte[ipPortLength[i]];
			din.readFully(ipPortBytes[i]);
			ipPortStrings[i] = new String(ipPortBytes[i]);
		}

		baInputStream.close();
		din.close();
	}

	public ConnectionWeights(int [] ids, int [][] map, String [] ipPortStrings)
	{
		size = ids.length;
		this.ids = ids;
		this.map = map;
		this.ipPortStrings = ipPortStrings;
		ipPortBytes = new byte[size][];
		ipPortLength = new int[size];
		for(int i = 0; i < size; i++)
		{
			ipPortBytes[i] = ipPortStrings[i].getBytes();
			ipPortLength[i] = ipPortBytes[i].length;
		}
	}

	//Marshal into sendable format.
	public byte[] getBytes() throws IOException
	{
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeInt(size);
		for(int i = 0; i < size; i++)
		{
			dout.writeInt(ids[i]);
		}

		for(int i = 0; i < size; i++)
		{
			for(int j = 0; j < size; j++)
			{
				dout.writeInt(map[i][j]);
			}
		}

		for(int i = 0; i < size; i++)
		{
			dout.writeInt(ipPortLength[i]);
			dout.write(ipPortBytes[i]);
		}


		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public String getReadable()
	{
		String readable = "";

		for(int i = 0; i < size; i++)
		{
			readable += ids[i] + "\t" + ipPortStrings[i] + "\n";
		}

		readable += "\n\n";
		for(int i = 0; i < size; i++)
		{
			readable += "\t" + ids[i];
		}
		readable += "\n------------------------------------------------------------------------------------";

		for(int i = 0; i<size; i++)
		{
			readable += ids[i] + " |\t";
			for(int j = 0; j<size; j++)
			{
				readable += map[i][j] + "\t";
			}
			readable += "\n";
		}


		return readable;
	}

	public int getType()
	{
		return type;
	}

	public int [] getIds()
	{
		return ids;
	}

	public int [][] getMap()
	{
		return map;
	}

	public int getSize()
	{
		return size;
	}

	public String[] ipPortStrings()
	{
		return ipPortStrings;
	}
}
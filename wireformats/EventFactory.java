package cs455.overlay.wireformats;
import cs455.overlay.node.*;
import java.io.*;
import java.net.*;

public class EventFactory
{
	public static final int REGISTER_REQUEST = 1;
	public static final int REGISTER_RESPONSE = 2;
	public static final int DEREGISTER_REQUEST = 3;
	public static final int DEREGISTER_RESPONSE = 4;
	public static final int CONNECTION_LIST = 5; //List of connection that this node will intitate connection to.
	public static final int MESSAGE_ID = 6; //Send id to message node.
	public static final int CONNECTION_RESPONSE = 7; //Messageing Node: Response if connections was successful or failed.
	public static final int CONNECTION_WEIGHTS = 8;//Sends overlay weight and connections
	public static final int DIJKSTRA_RESPONSE = 9;//Messaging Node: response if dikjstra's complete successfully or failed.
	public static final int TASK_INITIATE = 10;//Registry: Send MN rounds.
	public static final int TASK_COMPLETE = 11;
	public static final int TRAFFIC_SUMMARY_REQUEST = 12;
	public static final int TRAFFIC_SUMMARY = 13;
	public static final int MESSAGE = 14;




	public Event createEvent(byte[] data) throws IOException
	{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		int type = din.readInt();
		Event event = null;
		//Create new wire format based on the type. 
		switch(type)
		{
			case REGISTER_REQUEST:
				System.out.println("Register_request received");
				event = new RegisterRequest(data);
				break;
			case REGISTER_RESPONSE:
				System.out.println("Register_response received");
				event = new RegisterResponse(data);
				break;
			case DEREGISTER_REQUEST:
				System.out.println("Deregister_request received");
				event = new DeregisterRequest(data);
				break;
			case DEREGISTER_RESPONSE:
				System.out.println("Deregister_response received.");
				event = new DeregisterResponse(data);
				break;
			case CONNECTION_LIST:
				System.out.println("CONNECTION_LIST received.");
				event = new ConnectionList(data);
				break;
			case MESSAGE_ID:
				System.out.println("MESSAGE_ID received");
				event = new MessageID(data);
				break;
			case CONNECTION_RESPONSE:
				//Skip for now
				System.out.println("CONNECTION_RESPONSE received. Skip for now.");
				//event = new ConnectionList(data);
				break;
			case CONNECTION_WEIGHTS:
				System.out.println("CONNECTION_WEIGHTS received.");
				event = new ConnectionWeights(data);
				break;
			case DIJKSTRA_RESPONSE:
				System.out.println("DIJKSTRA_RESPONSE received.");
				event = new DijkstraResponse(data);
				break;
			case TASK_INITIATE:
				System.out.println("TASK_INITIATE received.");
				event = new TaskInitiate(data);
				break;
			case TASK_COMPLETE:
				//System.out.println("TASK_COMPLETE received.");
				event = new TaskComplete(data);
				break;
			case MESSAGE:
				//System.out.println("MESSAGE");
				event = new Message(data);
				break;
			case TRAFFIC_SUMMARY_REQUEST:
				System.out.println("TRAFFIC_SUMMARY_REQUEST received.");
				event = new TrafficSummaryRequest(data);
				break;
			case TRAFFIC_SUMMARY:
				//System.out.println("TRAFFIC_SUMMARY received.");
				event = new TrafficSummary(data);
				break;


			default:
				System.out.println("Error: Invalid event type passed");
		}
		baInputStream.close();
		din.close();
		return event;
	}
}
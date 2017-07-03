package cs455.overlay.node;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;
import cs455.overlay.util.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Registry implements Node 
{
	private volatile String log = "";
	private ArrayList<Connection> connections = new ArrayList<Connection>();
	private int nextConnectionIndex = 0;
	private int registeredNodesCount = 0;
	private int dijkstraCount = 0;
	private Overlay overlay;
	private int taskCompleteCount = 0; //Counts number of tasks
	private int trafficSummaryCount = 0; //Counts number of trafficSummary received.

	private ArrayList<TrafficSummary> summaryList = new ArrayList<TrafficSummary>();

	public static void main(String [] args) 
	{
		Registry registry = new Registry();
		System.out.println("Started Registry");

		if(args.length >= 1)
		{
			System.out.println("The port number entered is " + args[0]);
		}

		int portNum = Integer.valueOf(args[0]);
		try{
			ServerSocket serverSocket = new ServerSocket(portNum);
			TCPServer server = new TCPServer(serverSocket, registry);
			Thread serverThread = new Thread(server);
			serverThread.setDaemon(true);
			serverThread.start();
		} catch (IOException e) {
			System.out.println("Error");
		}
		registry.runForegroundProcess();
	}


	public void runForegroundProcess()
	{
		System.out.println("Running foreground Process...");
		Scanner keyboard = new Scanner(System.in);
		String input;
		while(true){
			input = keyboard.nextLine();
			System.out.println("Inputed: " + input);

			switch(input)
			{
				case "list-messaging-nodes":
				case "1":
					listMessagingNodes();
					break;
				case "list-weights":
					listWeights();
					break;
				case "send-overlay-link-weights":
				case "4":
					sendWeights();
					break;
				case "print-overlay":
				case "5":
					displayWeights();
					break;
				case "getAllConn":
				case "3":
					System.out.println(getAllConnReadable());
					break;
				default:
					Scanner lineRead = new Scanner(input);
					//setup-overlay
					if(lineRead.hasNext())
					{
						String segmentOne = lineRead.next();
						String segmentTwo = "";
						if(segmentOne.equals("setup-overlay"))
						{
							if(lineRead.hasNext())
							{
								segmentTwo = lineRead.next();
								int segmentTwoInt;
								try{
								segmentTwoInt = Integer.parseInt(segmentTwo);
								} catch (NumberFormatException e) {
								    System.out.println("Invalid input: setup-overlay number-of-connections");
								    break;
								}
								setupOverlay(segmentTwoInt);
								break;
							}
						}

						if(segmentOne.equals("start"))
						{
							if(lineRead.hasNext())
							{
								segmentTwo = lineRead.next();
								int segmentTwoInt;
								try{
								segmentTwoInt = Integer.parseInt(segmentTwo);
								} catch (NumberFormatException e) {
								    System.out.println("Invalid input: set number-of-rounds");
								    break;
								}
								if(startTask(segmentTwoInt) == 0)
								{
									System.out.println("Error: Sending Start Message Failed.");
								}

								break;
							}
						}
					}
					printUsage();
					System.out.println("Invalid input.");
					break;
			}
		}
	}

	public void printUsage()
	{
		System.out.println("'list-messaging-nodes' - Show all registered nodes");
		System.out.println("'setup-overlay #' - Begins Overlay setup over # connections");
		System.out.println("'send-overlay-link-weights'");
		System.out.println("'start # - Initiate Task to for each Messaging node with # rounds.");
		System.out.println("'getAllConn' - Show all connections. Registered or not");


	}


	public void appendMessage(String str)
	{
		this.log = str;
	}

	//The is where registry responds to events. 
	public void onEvent(Event event, Socket socket, int index)
	{
		//System.out.println("New Event!");
		int results;
		String responseString;
		byte resultByte;
		switch(event.getType())
		{
			case EventFactory.REGISTER_REQUEST:
				results = addRegistry(((RegisterRequest)event).getPort(), ((RegisterRequest)event).getIP(), socket, ((RegisterRequest)event).getConnectionPort());
				responseString = getRegisterResponseString(results);
				System.out.println("Reponse: " + responseString);
				if(results == 1)
				{
					resultByte = 1;
				}
				else
				{
					resultByte = 0;
				}
				RegisterResponse regResponse = new RegisterResponse(resultByte, responseString);
				//Sends Marshalled bytes along connection sender
				try{
					getConnection(index).getSender().sendData(regResponse.getBytes());
				} catch (IOException e){
					System.out.println("Error sending");
				}

				break;

			case EventFactory.DEREGISTER_REQUEST:
				results = removeRegistry(((DeregisterRequest)event).getPort(), ((DeregisterRequest)event).getIP(), socket, index);
				responseString = getDeregisterResponseString(results);
				if(results == 1)
				{
					resultByte = 1;
				}
				else
				{
					resultByte = 0;
				}
				DeregisterResponse deregResponse = new DeregisterResponse(resultByte, responseString);
				try{
					getConnection(index).getSender().sendData(deregResponse.getBytes());
				} catch (IOException e){
					System.out.println("Error sending");
				}
				break;

			case EventFactory.DIJKSTRA_RESPONSE:
				byte responseStatus = ((DijkstraResponse)event).getStatus();
				if(responseStatus == 1)
				{
					updateDijkstraCount();
					if(getDijkstraCount() == registeredNodesCount)
					{
						System.out.println("All Messaging Nodes have finished Dijkstra's Successfully");
						taskCompleteCount = 0;
					}
				}
				else
				{
					System.out.println("Error Running Dijkstra's");
				}
				break;

			case EventFactory.TASK_COMPLETE:
				taskComplete(((TaskComplete)event).getID(), ((TaskComplete)event).getStatus());
				break;

			case EventFactory.TRAFFIC_SUMMARY:
				updateTrafficSummaryCount((TrafficSummary)event);
				if(getTrafficSummaryCount() == registeredNodesCount)
				{
					System.out.println("All TrafficSummarys received.");
					printSummary();
				}

				break;
			default:
				System.out.println("Error: Invalid Message type.");
		}
	}
	public synchronized void updateDijkstraCount(){
		dijkstraCount++;
	}
	public synchronized int getDijkstraCount(){
		return dijkstraCount;
	}
	public synchronized void updateTrafficSummaryCount(TrafficSummary ts){
		summaryList.add(ts);
		trafficSummaryCount++;
	}
	public synchronized int getTrafficSummaryCount(){
		return trafficSummaryCount;
	}
	public void printSummary()
	{
		long totalSend = 0;
		long totalRecieve = 0;
		System.out.println("Node\t|Send Count\t|Received Count\t|Relayed Tracker\t|Send total value\t|Received total value\t");
		for(TrafficSummary sum : summaryList)
		{
			System.out.println(sum.getID() + "\t|" + sum.getSendTracker() + "\t\t|" + sum.getReceiveTracker() + "\t\t|" + sum.getRelayedTracker() + "\t\t\t|" + sum.getSendTotal() + " \t\t|" + sum.getReceiveTotal());
		} 
		System.out.println("Sum total Send Value: " + totalSend);
		System.out.println("Sum total Received Value: " + totalRecieve);
	}

	public void taskComplete(int rID, byte status)
	{
		if(status != 1)
		{
			System.out.println("MessagingNode: " + rID + " failed to complete task.");
		}
		incrementTaskCompleteCounter();
		if(taskCompleteCount == registeredNodesCount)
		{
			System.out.println("TaskCompletes received from all Messaging Nodes.");
			pullTrafficSummary();
		}
	}

	public synchronized void incrementTaskCompleteCounter()
	{
		taskCompleteCount++;
	}
	public void pullTrafficSummary()
	{
		System.out.println("Waiting 15 seconds.");
		try{
			Thread.sleep(15000);

		}catch(InterruptedException e){
			System.out.println("Error Waiting");
		}

		System.out.println("Pulling Traffic Summaries");
		int [] registeredIDs = getRegisteredIndices();
		TrafficSummaryRequest request = new TrafficSummaryRequest();
		for(int i = 0; i < registeredIDs.length; i++)
		{
			try{
				Connection currentConnetion = connections.get(registeredIDs[i]);
				currentConnetion.getSender().sendData(request.getBytes());
			} catch (IOException e){
				System.out.println("Error: Sending request");
				//return 0;
			}
		}
	}


	public String getLog()
	{
		return log;
	}


	/*
	Loops through connections ArrayList to determine if given port and address combination is listed.

	return:
		0 - Doesn't exist in table
		1 - Exists but not registered
		2 - Exists and registered
	*/
	public int getConnectionStatus(int port, String address)
	{
		for(int i = 0; i < connections.size(); i++)
		{
			Connection con = connections.get(i);
			if(con.match(port, address))
			{
				return con.getStatus();
			}
		}
		return 0;
	}

	public String getAllConnReadable()
	{
		String info = "";
		// for(int i = 0; i < connections.size(); i++)
		// {
		// 	info.concat(connections.get(i).getReadable());
		// }
		for (Connection con : connections) 
		{   
			info += con.getReadable() + "\n";
		}

		return info;
	}

	//List all registered nodes
	public void listMessagingNodes()
	{
		System.out.println("ID\t|Port\t|IP\t\t|ConPort");
		System.out.println("-----------------------------------------");
		for (Connection con : connections) 
		{   
			if(con.getStatus() == 2) //Checks if connection is registered
				System.out.println(con.getIndex() + "\t|" + con.getPort() + "\t|" + con.getIP() + "\t|" + con.getConnectionPort());
		}
	}

	public void listWeights()
	{
		System.out.println("list-weights not implemented");
	}

	public int getNextConnectionIndex()
	{
		return nextConnectionIndex;
	}

	public Connection getConnection(int index)
	{
		return connections.get(index);
	}
	/*
	returns:
		1: Successfully added connection
		0: Failed adding connections
	*/
	public int addConnection(int port, String address, TCPSender sender, TCPReceiver receiver, int index)
	{
		//If this connection has not been added to the table, Add it.
		int status = getConnectionStatus(port, address);
		if(status == 0)
		{	
			connections.add(index, new Connection(port, address, sender, receiver, index));
			nextConnectionIndex = index + 1;
			registeredNodesCount++;
			System.out.println("Successfully added connection");
			return 1;
		}
		else if(status == 1)
		{
			System.out.println("Connection exists and is not registered");
			return 0;
		}
		else
		{
			System.out.println("Connection exists and registered.");
			return 0;
		}
			
	}
	/*
	address - HostAddress (ip)
	returns:
		1 - Successfully registered
		0 - Port and Address already registered
		-1 - Port and Address doesn't match Socket data
		-2 - Connection not found.
	*/
	public int addRegistry(int port, String address, Socket socket, int connectionPort)
	{
		for (Connection con : connections) 
		{   
			if(con.match(port, address))
			{
				return con.register(socket, connectionPort);
			}
		}
		return -2;
	}

	//Response message. Tells messaging node status of registration.
	public String getRegisterResponseString(int results)
	{
		String responseString;
		switch(results)
		{
			case 1:
				responseString = "Successfully registered Messaging. NodeRegistered Node Count:" + registeredNodesCount;
				break;
			case 0:
				responseString = "Error: Port and Address already registered";
				break;
			case -1:
				responseString = "Error: Port and Address doens't match Socket data";
				break;
			case -2:
				responseString = "Error: Connection not found.";
				break;
			default:
				responseString = "Error: Unknown - " + results;
		}

		return responseString;
	}

	/*
	returns:
		1 - Successfully deregistered
		0 - Port and Address not registered
		-1 - Port and Address doesn't match Socket data
	*/
	public int removeRegistry(int port, String ip, Socket socket, int index)
	{
		//First Check if port and ip match socket
		if(overlay != null)
		{
			//Overlay already setup. Can't deregister.
			return -2;
		}
		if(port == socket.getPort() && ip.equals(socket.getInetAddress().getHostAddress()))
		{
			Connection con = connections.get(index);
			int resultsHolder = con.deregister(socket);
			if(resultsHolder == 1)
			{
				//Lower registered nodes count
				registeredNodesCount--;
				return 1;
			}
			else
			{
				return 0;
			}
		}
		else
		{ //Port and IP don't match socket
			return -1;
		}
	}

	//Response message. Tells messaging node status of registration.
	public String getDeregisterResponseString(int results)
	{
		String responseString;
		switch(results)
		{
			case 1:
				responseString = "Successfully deregistered MessagingNode. Registered Node Count:" + registeredNodesCount;
				break;
			case 0:
				responseString = "Error: Port and Address not registered";
				break;
			case -1:
				responseString = "Error: Port and Address doens't match Socket data";
				break;
			case -2:
				responseString = "Error: Connection not found.";
				break;
			case -3:
				responseString = "Error: Overlay already setup. Unable to deregister";
				break;
			default:
				responseString = "Error: Unknown - " + results;
		}

		return responseString;
	}

	public int getType(){
		return 2;
	}

	/*
	
	*/
	public int setupOverlay(int numberOfConnections)
	{
		overlay = new Overlay(numberOfConnections, registeredNodesCount);
		if(overlay.createOverlay(getRegisteredIndices()) == 1)
		{
			return sendConnectionInfo();

		}
		else
		{
			System.out.println("Error: Unable to create Overlay. Try different connection count.");
			return 0;
		}
		
	}

	//Sends connectionList to each Messaging Node
	public int sendConnectionInfo()
	{
		for (Connection con : connections) 
		{   
			if(con.getStatus() == 2) //Checks if connection is registered
			{
				int conID = con.getIndex();
				con.setConnectTo(overlay.getConnectionList(conID));
				ArrayList<Integer> conList = con.getConnectionList();
				ArrayList<Integer> conPortList = getConnectionPorts(conList);
				ArrayList<String> conIPlist = getConnectionIPs(conList);
				ConnectionList connectionList = new ConnectionList(conID,conList,conPortList,conIPlist);

				try{
				con.getSender().sendData(connectionList.getBytes());
				}catch(IOException e){
					System.out.println("Error: send connection info");
					return 0;
				}
			}
		}

		System.out.println("Connection Lists Sent");
		return 1;
	}

	//Gets Ports of connection list for messagingnode in order.
	public ArrayList<Integer> getConnectionPorts(ArrayList<Integer> conList)
	{
		ArrayList<Integer> ports = new ArrayList<Integer>();
		for(int index : conList)
		{
			ports.add(connections.get(index).getConnectionPort());
		}
		return ports;
	}

	//Gets Ips of connection list for messagingnode in order.
	public ArrayList<String> getConnectionIPs(ArrayList<Integer> conList)
	{
		ArrayList<String> ips = new ArrayList<String>();
		for(int index : conList)
		{
			ips.add(connections.get(index).getIP());
		}
		return ips;
	}

	/*
	Get an array with
	returns:
		Array[(index):(Connections index)]
	*/
	public int[] getRegisteredIndices()
	{
		int[] registeredIndex = new int[registeredNodesCount];
		int counter = 0;
		for (Connection con : connections) 
		{   
			if(con.getStatus() == 2) // if connection is registered
			{	
				if(counter < registeredNodesCount)
				{
					registeredIndex[counter] = con.getIndex();
					counter++;
				}
				else
				{
					System.out.println("Error1: registeredNodesCount is inncorrect");
					return null;
				}
				

			}
		}

		if(counter != registeredNodesCount)
		{
			System.out.println("Error2: registeredNodesCount is inncorrect");
			return null;
		}

		return registeredIndex;
	
	}

	public String[] getRegisteredIpPorts()
	{
		String[] ipPortString = new String[registeredNodesCount];
		int counter = 0;
		for (Connection con : connections) 
		{   
			if(con.getStatus() == 2) // if connection is registered
			{	
				if(counter < registeredNodesCount)
				{
					ipPortString[counter] = con.getIP() + ":" + con.getConnectionPort();
					counter++;
				}
				else
				{
					System.out.println("Error1: getRegisteredIpPorts is inncorrect");
					return null;
				}
				

			}
		}

		if(counter != registeredNodesCount)
		{
			System.out.println("Error2: registeredNodesCount is inncorrect");
			return null;
		}

		return ipPortString;
	}

	public int sendWeights()
	{
		System.out.println("Sending Weights");
		int [] registeredIDs = getRegisteredIndices();
		String [] ipPortStrings = getRegisteredIpPorts();
		int [][] map = overlay.getWeights();

		ConnectionWeights connectionWeights = new ConnectionWeights(registeredIDs, map, ipPortStrings);
		for(int i = 0; i < registeredIDs.length; i++)
		{
			Connection currentConnetion = connections.get(registeredIDs[i]);
			try{
				currentConnetion.getSender().sendData(connectionWeights.getBytes());
			} catch (IOException e){
				System.out.println("Error: sendWeights");
				//return 0;
			}
		}
		return 1;
	}

	public int displayWeights()
	{
		int [] registeredIDs = getRegisteredIndices();
		int [][] map = overlay.getWeights();
		for(int i = 0; i < map.length; i++)
		{
			System.out.print("\t" + registeredIDs[i]);
		}
		System.out.println();
		System.out.println("------------------------------------------------------------------------------------");

		for(int i = 0; i<map.length; i++)
		{
			System.out.print(registeredIDs[i] + " |\t");
			for(int j = 0; j<map.length; j++)
			{
				System.out.print(map[i][j] + "\t");
			}
			System.out.println();
		}
		return 1;
	}

	public int startTask(int numberOfRounds)
	{
		summaryList = new ArrayList<TrafficSummary>();
		TaskInitiate taskInitiate = new TaskInitiate(numberOfRounds);
		System.out.println("Sending Start " + taskInitiate.getNumberofRounds() + " rounds.");
		int [] registeredIDs = getRegisteredIndices();
		for(int i = 0; i < registeredIDs.length; i++)
		{
			try{
				Connection currentConnetion = connections.get(registeredIDs[i]);
				currentConnetion.getSender().sendData(taskInitiate.getBytes());
			} catch (IOException e){
				System.out.println("Error: sendWeights");
				//return 0;
			}
		}
		return 1;
	}



}
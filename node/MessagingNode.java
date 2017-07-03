package cs455.overlay.node;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;
import cs455.overlay.util.*;
import cs455.overlay.dijkstra.*;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;

public class MessagingNode implements Node 
{
	//Sockets
	private Socket registrySocket;
	private ServerSocket connectionSocket;

	//Ports and IPs
	private String registryIP; //IP Registry is running on
	private int registryPort; //Port Registry is running on
	private int localRegistryPort; //Local port used to connect with RegistryNode

	private String hostname; //IP of this MessagingNode
	private int connPort; //Port to listen for new connections from neighbor MessagingNodes

	//Transport Objects
	private TCPSender registrySender;
	private TCPReceiver registryReceiver;
	private TCPServer server;

	//Event Factory
	private EventFactory factory;

	//Connections
	private ArrayList<Connection> connections = new ArrayList<Connection>();
	private int totalConnections = 0;

	//Registered
	//0 - Not registered
	//1 - Registered
	//-1 - Error registering
	//2 - Waiting for Response
	private volatile int registered = 0; 

	//Overlay map and Registry IDs Map
	private int[] registryIds;
	private int[][] overlayMap;
	private String [] registryIpPorts;

	private int myRegistryID;
	private int myIndexID;
	//private Map<String, Integer> map;

	//Maps registry Id to own index.
	private int [] registryIDtoIndex;

	//Index to Index
	private int [] routingTable;

	//Index to connection
	private Connection [] routeToConnection; 

	//Tracking Communications between routers.
	private volatile long receiveTracker = 0; //Number of messages for me
	private volatile long sendTracker = 0; // Number of messages I have sent/started
	private volatile long  relayedTracker = 0;// Number of messages I have relayed
	private volatile long receiveTotal = 0; //Total received value
	private volatile long sendTotal = 0; //Total sent values





	public MessagingNode(String registryIP, int registryPort)
	{
		try {
            InetAddress ip = InetAddress.getLocalHost();
            this.hostname = ip.getHostAddress();
            System.out.println("Your IP: " + hostname);
 
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
		this.registryIP = registryIP;
		this.registryPort = registryPort;
	}

	/*
	Registers with Registry Node

		1. Creates connection with Registry
		2. Sends RegisterRequest
		3. Receives RegisterResponse

	Return:
		1 - Successfully registered
		0 - Failed to register
	*/
	public int registerNode()
	{
		try
		{
			System.out.println("Connecting to registryNode");
			registrySocket = new Socket(registryIP, registryPort);
			localRegistryPort = registrySocket.getLocalPort();
			System.out.println("Connected port: " + localRegistryPort);
		}
		catch (IOException e)
		{
			System.out.println("Error: Binding to Registry Node");
			return 0;
		}
		
		try{
			connectionSocket = new ServerSocket(0);
			connPort = connectionSocket.getLocalPort();

			server = new TCPServer(connectionSocket, this);
			Thread serverThread = new Thread(server);
			serverThread.setDaemon(true);
			serverThread.start();

			registrySender = new TCPSender(registrySocket, this, -1);
			factory = new EventFactory();
			registered = 2; //Waiting for response 
			RegisterRequest registerReq = new RegisterRequest(hostname, localRegistryPort, connPort);
			registrySender.sendData(registerReq.getBytes());	
		} catch (IOException e) {
			System.out.println("ERROR");
			return 0;
		}

		try{
			registryReceiver = new TCPReceiver(registrySocket, this, -1);
			Thread receiverThread = new Thread(registryReceiver);
			receiverThread.setDaemon(true);
			receiverThread.start();


		} catch (IOException e) {
			System.out.println("Error: receiving.");
			return 0;
		}

		return 1;
	}

	public static void main(String [] args) 
	{
		System.out.println("Started MessagingNode");
		String registryIP;
		int registryPort;
		if(args.length == 2)
		{
			registryIP = args[0];
			registryPort = Integer.parseInt(args[1]);
		}
		else
		{
			System.out.println("Invalid Arugments: registryIP registryPort");
			return;
		}


		MessagingNode node = new MessagingNode(registryIP, registryPort);
		if(node.registerNode() == 0)
		{
			System.out.println("Terminating: Error Registering.");
		}
		else
		{
			System.out.println("Registering Node.");
		}

		node.runForegroundProcess();
		
		
	}

	public void onEvent(Event event, Socket socket, int index)
	{
		//System.out.println("New Event!");
		byte responseStatus;
		switch(event.getType())
		{
			case EventFactory.REGISTER_RESPONSE:
				System.out.println(event.getReadable());
				responseStatus = ((RegisterResponse) event).getStatus();
				if(responseStatus == 1)
				{
					registered = 1;
				}
				else
				{
					registered = -1;
				}
				break;
			case EventFactory.DEREGISTER_RESPONSE:
				System.out.println(event.getReadable());
				responseStatus = ((DeregisterResponse)event).getStatus();
				if(responseStatus == 1)
				{
					//Successfully deregistered. Updated.
					registered = 0;
				}
				break;
			case EventFactory.CONNECTION_LIST:
				myRegistryID = ((ConnectionList)event).getMyID();
				System.out.println(event.getReadable());
				System.out.println(((ConnectionList) event).getConnectionNumber());
				int results = establishConnections(((ConnectionList) event));
				break;

			case EventFactory.MESSAGE_ID:	
				int idReceived = ((MessageID)event).getID();
				setConnectionID(idReceived, socket);
				break;

			case EventFactory.CONNECTION_WEIGHTS:
				System.out.println(event.getReadable());		
				registryIds = ((ConnectionWeights) event).getIds();
				overlayMap = ((ConnectionWeights) event).getMap();
				registryIpPorts = ((ConnectionWeights) event).ipPortStrings();
				System.out.println("Starting Dijkstra's Algorithm");
				int dijkResults = runDijkstra();
				byte status;
				String message;
				if(dijkResults == 1)
				{
					status = 1;
					message = "Dijkstra's Completed Successfully";
					System.out.println("Complete with Dijkstra's. Send Dijkstra's Response");
				}else{
					status = 0;
					message = "Dijkstra's Failed";
					System.out.println("Dijkstra's Failed. Send Dijkstra's Response");
				}
				//Dijkstras complete.
				//Let Registry know
				DijkstraResponse dijkstraResponse = new DijkstraResponse(status, message);
				try{
					registrySender.sendData(dijkstraResponse.getBytes());
				} catch (IOException e){
					System.out.println("Dijkstra Send Failure");
				}
				System.out.println("here");
				break;

			case EventFactory.TASK_INITIATE:
				System.out.println(event.getReadable());
				startTask(((TaskInitiate)event).getNumberofRounds());
				break;

			case EventFactory.MESSAGE:
				receivedMessage((Message)event);
				break;

			case EventFactory.TRAFFIC_SUMMARY_REQUEST:
				sendTrafficSummary();
				break;
			default:
				System.out.println("Error: Invalid Message type.");
		}
	}

	public void setConnectionID(int id, Socket socket)
	{
		for(int i = 0; i < connections.size(); i++)
		{
			Connection temp = connections.get(i);
			int result = temp.addID(id, socket);
			if(result == 1){
				return;
			}
			else if(result == 0)
			{
				System.out.println("Already Set ID.");
				return;
			}
		}
		System.out.println("Error: Unable to find matching socket");

	}
	public int establishConnections(ConnectionList listWire)
	{
		//Note server is already created. Listening to new conenctions. 
		int connNumber = listWire.getConnectionNumber();
		totalConnections = connNumber;
		int [] connNodes = listWire.getConnectionNodes();
		int[] connPorts = listWire.getConnectionPorts();
		String [] connIPs = listWire.getIPString();
		//Connection(int port, String ip, TCPSender sender, TCPReceiver receiver, int id)

		//Time to establish new connections
		for(int i = 0; i < connNumber; i++)
		{
			try
			{	
				System.out.println("My Port: " + connPort);
				System.out.println("Send to: " + connIPs[i] + " : " + connPorts[i]);
				int tempID = connNodes[i];
				int tempPort = connPorts[i];
				String tempIP = connIPs[i];
				Socket messagingSocket = new Socket(connIPs[i], connPorts[i]);
				TCPSender sender = new TCPSender(messagingSocket, this, tempID);
				TCPReceiver recv = new TCPReceiver(messagingSocket, this, tempID);
				Thread recvThread = new Thread(recv);
				recvThread.setDaemon(true);
				recvThread.start();
				addConnection(messagingSocket.getPort(), messagingSocket.getInetAddress().getHostAddress(), sender, recv, tempID);
				//Need to send ID you newly connected MessagingNode
				MessageID messageID = new MessageID(myRegistryID);
				sender.sendData(messageID.getBytes());
				System.out.println("Sent");
			}
			catch (IOException e)
			{
				System.out.println("Error: Binding to Registry Node");
				return 0;
			}
		}
		return 1;
	}

	//
	public int addConnection(int port, String address, TCPSender sender, TCPReceiver receiver, int index)
	{
		//Add Connection
		Connection messageConnection = new Connection(port, address, sender, receiver, index);
		System.out.println("AddConnection");
		if(messageConnection == null)
		{
			System.out.println("Null connection");
		}
		connections.add(messageConnection);
		return 1;	
	}
	public void appendMessage(String str)
	{
		//log.append(str);
	}

	//Need to verify with Messaging Node Connection. For now say unset.
	public int getNextConnectionIndex()
	{
		return -1;
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
				case "registered":
				case "1":
					if(registered == 1){
						System.out.println("Successfully Registered");
					}
					else if(registered == 2){
						System.out.println("Waiting for registry request response.");
					}
					else if(registered == 0){
						System.out.println("Not Registered");
					}else{
						System.out.println("Registeration Failed");
					}
					break;
				case "exit-overlay":
				case "2":
					exitOverlay();
					break;
				case "show-connections":
				case "3":
					//if(totalConnections == connections.size())
					//{
						System.out.println("Printing Connections Current ID: " + myRegistryID);
						for(int i = 0; i < connections.size(); i++)
						{
							if(connections.get(i) != null)
							{
								System.out.println(connections.get(i).getIndex());
							}
							else
							{
								System.out.println("Error: Null pointer");
							}
						}
					//}
					break;

				case "print-shortest-path":
					getAllPaths();
					break;
				case "print-traffic-summary":
					getTrafficInfo();
					break;
				default:
					printUsage();
			}
		}
	}

	public void printUsage()
	{
		System.out.println("'exit-overlay' or '2' - To remove node from Registry overlay");
		System.out.println("'1' - Check on Registry status");
		System.out.println("'show-connections'  or 3 - Shoe messaging node connections");
		System.out.println("'print-shortest-path'  - After running dijkstra's. Print path to all nodes.");
		System.out.println("'print-traffic-summary'  - After running dijkstra's. Print path to all nodes.");

	}

	public void exitOverlay()
	{
		DeregisterRequest deregisterRequest = new DeregisterRequest(hostname, localRegistryPort);
		try{
			System.out.println("Send Exit message");
			registrySender.sendData(deregisterRequest.getBytes());
		} catch (IOException e) {
			System.out.println("Exit Overlay Error.");
		}
	}
	public int getType(){
		return 1;
	}

	public int runDijkstra()
	{
		Dijkstra dijkstra = new Dijkstra(registryIds, overlayMap, myRegistryID);
		routingTable = dijkstra.getRoutingTable();

		//Once Dijkstra's is complete create mappings
		return createMappings();
	}

	//Creates RegistryID to Index Mapping 
	public int createMappings()
	{
		//get myIndexID
		for(int i = 0; i < registryIds.length; i++)
		{
			if(registryIds[i] == myRegistryID)
			{
				myIndexID = i;
				break;
			}
		}

		//creating registryID to this Nodes Index mapper.
		int max = 0;
		for(int i = 0; i < registryIds.length; i++)
		{
			if(max < registryIds[i])
			{
				max = registryIds[i];
			}
		}
		registryIDtoIndex = new int[max + 1];
		for(int i = 0; i< registryIDtoIndex.length; i++)
		{
			registryIDtoIndex[i] = -1;
		}
		for(int i = 0; i < registryIds.length; i++)
		{
			registryIDtoIndex[registryIds[i]] = i;
		}

		//Given an destination node index, we need to route to where to send it next
		routeToConnection = new Connection [registryIds.length]; 
		for(int i = 0; i < registryIds.length; i++)
		{
			int nextIndex = getNextHop(i);
			routeToConnection[i] = getConnection(nextIndex);
		}

		return 1;
	}

	public Connection getConnection(int index)
	{
		if(index == myIndexID)
		{
			return null;
		}
		//Change index to Registry ID
		//System.out.println("INDEX: " + index);
		int regID = registryIds[index];
		//System.out.println("REGID: " + regID);
		for(int i = 0; i < connections.size(); i++)
		{
			Connection temp = connections.get(i);
			//System.out.println("CON INDEX: " + temp.getIndex());
			if(temp.getIndex() == regID)
			{
				return temp;
			}
		}
		System.out.println("Error: No conenctions Match.");
		return null;
	}



	public int getNextHop(int index)
	{
		if(routingTable[index] == myIndexID)
		{
			return index;
		}
		if(routingTable[index] == -1){//Destination reached
			return index;
		}
		return getNextHop(routingTable[index]);
	}




	public void getAllPaths()
	{
		for(int i = 0; i < registryIds.length; i++)
		{
			getPath(i);
		}
	}

	public void getPath(int index)
	{
		System.out.println(getPathRecursive(index));
	}

	public String getPathRecursive(int index)
	{
		if(routingTable[index] == -1)
		{
			return registryIpPorts[myIndexID];
		}

		return getPathRecursive(routingTable[index]) + "--" + overlayMap[index][routingTable[index]] + "--" + registryIpPorts[index];

	}

	public void startTask(int numberOfRounds)
	{
		receiveTracker = 0; //Number of messages for me
		sendTracker = 0; // Number of messages I have sent/started
		relayedTracker = 0;// Number of messages I have relayed
		for(int i = 0; i < numberOfRounds; i++)
		{
			System.out.println("Starting Round:" + i);
			startRound();
		}

		sendTaskComplete();
	}

	public void receivedMessage(Message message)
	{
		int destinationID = message.getDestinationID();
		int destinationIndex = registryIDtoIndex[destinationID];
		if(destinationIndex == myIndexID)
		{
			updateReceiveTotal(message.getMessage());
			updateReceiveTracker();
		}
		else
		{
			//relay message. 
			Connection tempConnection = routeToConnection[destinationIndex];
			try{
				tempConnection.getSender().sendData(message.getBytes());
				updateRelayTracker();
			} 
			catch (IOException e)
			{
				System.out.println("Relay Message Error");
			}
		}
	}

	public void startRound()
	{
		int messagesPerRound = 5;
		Random randomGenerator = new Random();
		int destinationIndex;
		int destinationID;
		int message;
		destinationIndex = randomGenerator.nextInt(registryIds.length);
		while(destinationIndex == myIndexID) //If myself, rerun.
		{
			destinationIndex = randomGenerator.nextInt(registryIds.length);
		}

		destinationID = registryIds[destinationIndex];

		for(int i = 0; i < 5; i++)
		{
			message = randomGenerator.nextInt();
			System.out.println("Destination: " + destinationIndex + " Message: " + message);
			Message messageLink = new Message(destinationID, message);
			Connection tempConnection = routeToConnection[destinationIndex];
			try{
				tempConnection.getSender().sendData(messageLink.getBytes());
				updateSendTracker();
				updateSendTotal(message);
			} 
			catch (IOException e)
			{
				System.out.println("Sending Message Error: startRound");
			}

		}
	}

	public void sendTaskComplete()
	{
		System.out.println("Send Completed. Sending the TaskComplete to Registry");
		byte status = 1;
		TaskComplete taskComplete = new TaskComplete(status, myRegistryID);
		try{
			registrySender.sendData(taskComplete.getBytes());
		} 
		catch (IOException e)
		{
			System.out.println("Failed sending TaskComplete.");
		}
	}



	public synchronized void updateReceiveTracker()
	{
		receiveTracker = receiveTracker + 1;
	}

	public synchronized void updateReceiveTotal(long value)
	{
		receiveTotal += value;
	}	

	public synchronized void updateSendTracker()
	{
		sendTracker = sendTracker + 1;
	}

	public synchronized void updateSendTotal(long value)
	{
		sendTotal += value;
	}
	public synchronized void updateRelayTracker()
	{
		relayedTracker = relayedTracker + 1;
	}

	public void getTrafficInfo()
	{
		System.out.println("Received: " + receiveTracker);
		System.out.println("Send: " + sendTracker);
		System.out.println("Relay: " + relayedTracker);
		System.out.println("SendTotal: " + sendTotal);
		System.out.println("ReceiveTotal: " + receiveTotal);
	}

	public void sendTrafficSummary()
	{
		System.out.println("Sending Traffic Summary");
		String ipPortString = hostname + ":" + connPort;
		//TrafficSummary trafficSummary = new TrafficSummary(ipPortString, myRegistryID, receiveTracker, sendTracker, relayedTracker);
		TrafficSummary trafficSummary = new TrafficSummary(myRegistryID, receiveTracker, sendTracker, relayedTracker, sendTotal, receiveTotal);
		try{
			registrySender.sendData(trafficSummary.getBytes());
		} 
		catch (IOException e)
		{
			System.out.println("Failed sending TrafficSummary.");
		}
		
	}

}
Author: David Sahud
Date: 2/15/16

Assignment Description:

—————————————————————————————————————————————————————————————————————————————————————————————
To Run:
	MessagingNode: java cs455.overlay.node.MessagingNode IP-of-registry Port-of-registry
	Registry: java cs455.overlay.node.Registry

	Registry Commands: 
		'list-messaging-nodes' - Displays all registered nodes
		'setup-overlay #' - Begins Overlay setup over # connections. Has MessagingNodes establish initial connections with neighboring nodes.
		'send-overlay-link-weights' - Full overlay is sent to messaging nodes. Each messaging node run dijkstra.
		'start # - Initiate Task to for each Messaging node with # rounds.

	MessagingNode Commands:
		‘exit-overlay’ - Sends request to be removed from overlay
		‘show-connections’ - Display connections to other messaging nodes. Only run after registry has run ‘setup-overlay’
		‘print-shortest-path’ - Display shortest path to every other node on the network. Only run after ‘send-overlay-link-weights’ has been run on registry.
		‘print-traffic-summary’ - Print traffic summary details.

—————————————————————————————————————————————————————————————————————————————————————————————
File Descriptions:

	cs455.overlay.dijkstra:
		Dijkstra.java - With a given Overlay map, and registryID table. Creates path array to determine shortest path to each other node
	
	cs455.overlay.node:
		Node.java - Interface for Registry and MessagingNode. Allows Receiver and Sender call a generic node for onEvent() and addConnection()
		Registry.java - 
		MessagingNode.java - 
	
	cs455.overlay.transport:
		TCPReceiver.java - Threaded receiver, that takes in all messages to this socket.
		TCPSender.java - One sender per socket. It send data across this socket.
		TCPServer.java - Accepting new connections. When connection established. Makes new TCPReceiver and TCPSender.
	
	cs455.overlay.util:
		Connection.java - Connection Object. Holds information about each connection.
		Link.java - Links in OverlayMap. Contains the weight, hostA and hostB ids.
		Overlay.java - Generates overlay in a 2d integer matrix. 0 indicates no connection. -1 Indicates self. If >0 This is the weight of the connection.

	cs455.overlay.wireformats:
		ConnectionList.java - Registry sends list of nodes each MessagingNode to initiate connection to.
		ConnectionWeights.java - Registry sends full overlay table including weights.
		DeregisterRequest.java - MessagingNode send request to leave overlay.
		DeregisterResponse.java - Register responds to results of DeregisterRequest.
		DijkstraReponse.java - Message sends response after dijkstra’s has been run. 
		Event.java - interface for all wireformats.
		EventFactory.java - Creates new Events given byte arrays.
		Message.java - Messages that are send to each MessagingNode after ’start’ command has been run.
		MessageID - When messaging nodes make connections to each other, their RegistryID is sent to link connection with node.
		RegisterRequest.java - MessagingNode send request to join overlay.
		RegisterResponse.java - Register responds to results of RegisterRequest.
		TaskComplete.java - MessagingNode has sent completed sending messages.
		TrafficSummary.java - MessagingNode sends TrafficSummary to registry.
		TrafficSummaryRequest.java - Registry requests Traffic Summary from messaging node.

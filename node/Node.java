package cs455.overlay.node;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;
import cs455.overlay.util.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public interface Node 
{
	public void onEvent(Event event, Socket socket, int index);
	public int addConnection(int port, String address, TCPSender sender, TCPReceiver receiver, int index);
	public void appendMessage(String str);
	public int getNextConnectionIndex();
	public int getType();
}
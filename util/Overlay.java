package cs455.overlay.util;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;

public class Overlay 
{
	private int nodeCount;
	private int connectionCount;
	private int [] registerMap; //Maps Overlay Indicies to Registry ID. Will be used when reverting it back.
	private ArrayList<Integer> idToIndex; //Maps Registry IDs to Overlay Indicies

	private int [][] map;
	private Link [][] linkMap;
	private int [] linkCount;

	public Overlay(int connectionCount, int nodeCount)
	{
		this.nodeCount = nodeCount;
		this.connectionCount = connectionCount;
	}

	public void printOverlay()
	{
		for(int i = 0; i < nodeCount; i++)
		{
			System.out.print("Node: " + i + " - ");
			for(int j = 0; j < nodeCount; j++)
			{
				if(map[i][j] > 0)
				{
					System.out.print(j + ", ");
				}
			}
			System.out.println();
		}
	}

	public void printWeights(int index, boolean registryMode)
	{
		if(registryMode)
		{
			System.out.println("Registry Node: " + registerMap[index]);
		}
		else
		{
			System.out.println("Overlay Node: " + index);
		}
		System.out.println("HostA\t|HostB\t|Weight");
		for(int i = 0; i < nodeCount; i++)
		{
			Link temp = linkMap[index][i];
			if(temp != null)
			{
				System.out.println(temp.getHostA() + "\t|" + temp.getHostB() + "\t|" + temp.getWeight());
			}

		}
		System.out.println("");


	}

	public void printAllWeights(boolean registryMode)
	{
		for(int i = 0; i < nodeCount; i++)
		{
			printWeights(i, registryMode);

		}
	}

	public void createIDToIndexMap()
	{
		idToIndex = new ArrayList<Integer>(10);
		//create id to Index Map
		int max = 0;
		for(int i = 0; i < nodeCount; i++)
		{
			if(max < registerMap[i])
			{
				max = registerMap[i];
			}
		}

		for(int i = 0; i < max; i++)
		{
			idToIndex.add(-1);
		}

		for(int i = 0; i < nodeCount; i++)
		{
			//add(index, element)
			idToIndex.add(this.registerMap[i], i);
		}
	}
	//Generates connections between nodes.
	public int createOverlay(int [] registerMap)
	{
		if(registerMap == null)
		{
			System.out.println("RegistryMap not set yet");
			return 0;
		}
		this.registerMap = registerMap;
		if(registerMap.length != nodeCount)
		{
			System.out.println("Invalid Map");
			return 0;
		}
		createIDToIndexMap();

		// 1 = Connected
		// 0 = Not Connected
		// -1 = Self
		int createdOverlay = 0;
		for(int i = 0; i < 30; i++)
		{
			if(createOverlayHelper() == 1)
			{
				createdOverlay = 1;
				System.out.println("Successfully Created Overlay. Tries: " + i);
				break;	
			}
		}

		if(createdOverlay == 0){
			//System.out.println("Failed Creating Overlay.");
			return 0;
		}
	
		//Overlay success

		//Map 
		printAllWeights(false);
		printOverlay();
		convertLinks();
		printAllWeights(true);
		return 1;
	}

	public void convertLinks()
	{
		for(int i = 0; i < nodeCount; i++)
		{
			for(int k = 0; k < nodeCount; k++)
			{
				Link temp = linkMap[i][k];
				if(temp != null && temp.getFormat() == false) 
				{
					//If format is in overlay format, convert it.
					temp.convertRegistryFormat(registerMap[temp.getHostA()], registerMap[temp.getHostB()]);
				}
			}
		}
	}


	public int createOverlayHelper()
	{
		map = new int[nodeCount][nodeCount];
		linkCount = new int[nodeCount];
		linkMap = new Link[nodeCount][nodeCount];

		//Set self
		for(int i = 0; i < nodeCount; i++)
		{
			map[i][i] = -1;
		}

		//Set intial link to have connected graph
		for(int i = 0; i < nodeCount - 1; i++)
		{
			Random wRand = new Random();
			int randWeight = wRand.nextInt(10) + 1;
			Link tempLink = new Link(i+1,i,randWeight);
			map[i][i+1] = randWeight;
			map[i+1][i] = randWeight;
			linkMap[i][i+1] = tempLink;
			linkMap[i+1][i] = tempLink;
			linkCount[i]++;
			linkCount[i+1]++;
		}

		for(int i = 0; i < nodeCount; i++)
		{
			if(linkCount[i] > connectionCount)
			{
				return 0;
			}
		}

		int currentIndex = 0;
		while(fullLinkCount() == false){
			Random rand = new Random();
			int randomIndex = rand.nextInt(nodeCount);
			if(linkCount[currentIndex] < connectionCount) //atempting to add a connec
			{
				int added = 0;
				for(int i = 0; i < nodeCount; i++)
				{
					int randomCounter = (i + randomIndex) % nodeCount;
					if(map[currentIndex][randomCounter] == 0 && map[randomCounter][currentIndex] == 0 && linkCount[randomCounter] < connectionCount)
					{
						//Add Connection
						int randWeight = rand.nextInt(10) + 1;
						Link tempLink = new Link(currentIndex,randomCounter,randWeight);
						linkMap[currentIndex][randomCounter] = tempLink;
						linkMap[randomCounter][currentIndex] = tempLink;

						map[currentIndex][randomCounter] = randWeight;
						map[randomCounter][currentIndex] = randWeight;
						linkCount[randomCounter]++;
						linkCount[currentIndex]++;
						added = 1;
						i = nodeCount;
					}
				}

				if(added == 0)
				{
					return 0;
				}
			}
			currentIndex = (currentIndex + 1) % nodeCount;
		}

		return 1;
	}

	//Returns true if all nodes have the required number of links
	public boolean fullLinkCount()
	{
		for(int i = 0; i < nodeCount; i++)
		{
			if(linkCount[i] < connectionCount)
			{
				return false;
			}
		}
		return true;
	}

	public int generateLinkWeights()
	{
		Random rand = new Random();
		int  n = rand.nextInt(10) + 1;
		return n;
	}

	//Returns list of MessageNode Ids that MessagingNode(id) should initiate connnectios to. 
	//This a a link where hostA = id. Return list of hostB.
	public ArrayList<Integer> getConnectionList(int id)
	{
		 ArrayList<Integer> connectionList = new ArrayList<Integer>();
		int index = idToIndex.get(id);
		for(int i = 0; i < nodeCount; i++)
		{
			Link temp = linkMap[index][i];
			if(temp != null && temp.getHostA() == id)
			{
				connectionList.add(temp.getHostB());
			}
		}
		return connectionList;
	}

	public int[][] getWeights()
	{
		return map;
	}

	//Testing Method for Overlay
	public static void main(String args[])
	{
		int [] connection = {1,2,3,4,5,6,7,8,9,10};
		System.out.println(connection[0]);
		Overlay overlay = new Overlay(4, connection.length);
		System.out.println(overlay.createOverlay(connection));
		ArrayList<Integer> connectTo = overlay.getConnectionList(connection[0]);
		System.out.println(connection[0] + " must Connection to nodes: ");
		for(int i = 0; i < connectTo.size(); i++)
		{
			System.out.println(connectTo.get(i));
		}

	}
}
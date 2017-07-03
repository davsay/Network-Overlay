package cs455.overlay.dijkstra;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Dijkstra 
{
	private int [] routingTable;
	//[registryIds][nextHop]
	private int [] registryIds;
	private int [][] overlayMap;
	private int myID;
	private int myIndex = -1;
	private int size;

	public Dijkstra(int [] registryIds, int [][] overlayMap, int myID)
	{
		this.registryIds = registryIds;
		size = registryIds.length;
		this.overlayMap = overlayMap;

		this.myID = myID;
		//Calculating my index
		for(int i = 0; i < size; i++)
		{
			if(registryIds[i] == myID)
			{
				myIndex = i;
				return;
			}
		}
		if(myIndex == -1)
		{
			System.out.println("Error: ID not found in registry.");
		}
	}

	//Returns routingTable in generic format. Will need to convert to RegistryIds.
	public int[] getRoutingTable()
	{
		int size = registryIds.length;
		routingTable = new int[size]; //Contains next hop.
		boolean[] finalized = new boolean[size];
		int[] weights = new int[size];
		int[] path = new int[size];

		//intitalize
		for(int i = 0; i < size; i++)
		{
			finalized[i] = false;
			routingTable[i] = -1;
			weights[i] = -1;
			path[i] = -1;
		}
		finalized[myIndex] = true;
		routingTable[myIndex] = 0;

		//set Neighbors
		for(int i = 0; i < size; i++)
		{
			if(overlayMap[myIndex][i] > 0)
			{
				routingTable[i] = i; //The next hop would be itself
				weights[i] = overlayMap[myIndex][i];
			}
		}

		//Starting dijkstra's
		for(int i = 0; i < size - 1; i++)
		{
			int minIndex = -1;
			int minVal = -1;

			//Finding Minimum Value and Index.
			for(int node = 0; node < size; node++)
			{
				if(finalized[node] == false && weights[node] != -1)
				{
					if(minVal == -1)
					{
						minVal = weights[node];
						minIndex = node;
					}
					else
					{
						if(weights[node] < minVal)
						{
							minVal = weights[node];
							minIndex = node;
						}
					}
				}
			}

			if(minVal == -1)
			{
				break;
			}
			//System.out.println("Finalize: " + minIndex);
			finalized[minIndex] = true;

			//Going through each node adjacent to minNode.
			for (int node = 0; node < size; node++)
			{
				if( ((weights[minIndex] + overlayMap[minIndex][node] < weights[node]) || (weights[node] == -1 && overlayMap[minIndex][node] > 0)) 
					&& overlayMap[minIndex][node] > 0 && finalized[node] == false)
				{
					//System.out.println("Weight = " + weights[node]);
					//System.out.println("updated! Node(" +node+") = weights[" + minIndex + "](=" + weights[minIndex] + ") + overlayMap[" + node + "]["+minIndex+"](="+ overlayMap[node][minIndex] + ")");
					weights[node] = weights[minIndex] + overlayMap[node][minIndex];
					path[node] = minIndex;

				}
			}

		}
		for(int i = 0; i < size; i++)
		{
			if(path[i] == -1 && i != myIndex)
			{//Direct link, then next hop is self
				path[i] = myIndex;
			}
		}
		return path;
	}


	public static void main(String args[])
	{
		int [] ids =    {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

		int [][] map ={{-1, 1, 0, 0, 0, 1, 0, 0, 4, 3},
						{1,-1, 9, 5, 5, 0, 0, 0, 0, 0},
						{0, 9,-1, 1, 0, 0, 0, 9, 9, 0},
						{0, 5, 1,-1, 7,10, 0, 0, 0, 0},
						{0, 5, 0, 7,-1, 3, 9, 0, 0, 0},
						{1,	0, 0,10, 3,-1,10, 0, 0, 0},
						{0, 0, 0, 0, 9,10,-1, 2, 0, 2},
						{0, 0, 9, 0, 0, 0, 2,-1, 1, 6},
						{4, 0, 9, 0, 0, 0, 0, 1,-1, 2},
						{3, 0, 0, 0, 0, 0, 2, 6, 2,-1}	};
		int currentNode = 0;
		Dijkstra dijkstra = new Dijkstra(ids, map, currentNode);
		int [] rt = dijkstra.getRoutingTable();

		for(int i = 0; i < ids.length; i++)
		{
			System.out.println(i + "\t|" + rt[i]);
		}
	}
}
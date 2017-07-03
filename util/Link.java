package cs455.overlay.util;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;


public class Link 
{
	private int hostA;
	private int hostB;
	private int weight;
	private boolean registryIndexFormat = false; //true, when converted to registryIndexFormat.

	public Link(int hostA, int hostB, int weight)
	{
		this.hostA = hostA;
		this.hostB = hostB;
		this.weight = weight;
	}

	public int getHostA()
	{
		return hostA;
	}

	public int getHostB()
	{
		return hostB;
	}

	public int getWeight()
	{
		return weight;
	}

	public boolean getFormat()
	{
		return registryIndexFormat;
	}

	public void convertRegistryFormat(int hostA, int hostB)
	{
		registryIndexFormat = true;
		this.hostA = hostA;
		this.hostB = hostB;
	}
}
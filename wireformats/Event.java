package cs455.overlay.wireformats;
import java.io.*;
public interface Event 
{
	public byte[] getBytes() throws IOException;
	public String getReadable();
	public int getType();
}
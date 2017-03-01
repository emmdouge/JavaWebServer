/* Project 1: Web Server
 * Programmer: Emmanuel Douge 
 * Course: CSC 431 Section 1 (10:00 –11:50 am) 
 * Instructor: S. Lee
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class WebServer
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(7777);

        while(true)
        {
            //Server Socket stops execution and waits for connection request from client
            //When client connection request arrives, server creates new socket for communication
            Socket socket = serverSocket.accept();

            // Construct an object to process the HTTP request message.
            HttpRequest request = new HttpRequest(socket);

            // Create a new thread to process the request.
            Thread thread = new Thread(request);

            thread.start();
        }


    }
}

final class HttpRequest implements Runnable
{
    Socket socket;
    public final static String CRLF = "\r\n";
    public HttpRequest(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try
        {
            processRequest();
        }
        catch (Exception e)
        {
            //because run must adhere to its interface Runnable
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception
    {

        //Inputstream of socket
        InputStream is = socket.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

        // Get the request line of the HTTP request message.
        String requestLine = br.readLine();

        // Display the request line.
        System.out.println(requestLine + " <--- This is the request line.\n");

        // Extract the filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine, " ");
        
        // skip over the method, which should be "GET"
        tokens.nextToken();  
        
        String fileName = tokens.nextToken();

        // Prepend a "." so that file request is within the current directory.
        // Must be typed in the url bar of your browser
        fileName = "." + fileName;

        // Display the request line.
        System.out.println(fileName + " <--- This is the file name.\n");

        // Get and display the header lines.
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0)
        {
            System.out.println("-" + headerLine);
        }
        
        //Open the requested file.
        FileInputStream fis = null;
        boolean fileExists = true;
        try 
        {
        	fis = new FileInputStream(fileName);
        } 
        catch (FileNotFoundException e) 
        {
        	System.out.println("File Not Found!\n\n");
        	fileExists = false;
        }
        
        //Construct the response message.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if(fileExists) 
        {
        	System.out.println("File Found: Will send bytes");
        	statusLine = "HTTP/1.0 200 OK" + CRLF;
        	contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
        	
        } 
        else 
        {
        	statusLine = "HTTP/1.0 404 Not Found" + CRLF;
        	contentTypeLine = "text/html" + CRLF;
        	entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></HTML>";
        }
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
       
        //Send the status line.
        dos.writeBytes(statusLine);
        
        //Send the content type line.
        dos.writeBytes(contentTypeLine);
        
        //Send a blank line to indicate the end of the header lines.
        dos.writeBytes(CRLF);
        
        //Send the entity body if the file exists.
        if (fileExists)
        {
        	sendBytes(fis, dos);
        	fis.close();
        } 
        else 
        {
        	dos.writeBytes(entityBody);
        }
        br.close();
        
        //Send data to remote socket.
        //dos.writeUTF("[NOTICE] Test Message1 from Server.");
        dos.close();
        socket.close();
    }

    private static String contentType(String fileName)
    {
    	if(fileName.endsWith(".htm") || fileName.endsWith(".html")) 
    	{
    		return "text/html";
    	}
    	if(fileName.endsWith(".jpeg")) 
    	{
    		return "image/jpeg";
    	}
    	if(fileName.endsWith(".gif")) 
    	{
    		return "image/gif";
    	}
    	return "application/octet-stream";
    }
    
    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception
    {
    	// Construct a 1K buffer to hold bytes on their way to the socket.
    	byte[] buffer = new byte[1024];
    	int bytes = 0;
    	
    	// Copy requested file into the socket's output stream.
    	while((bytes = fis.read(buffer)) != -1 ) 
    	{
    		os.write(buffer, 0, bytes);
    	}
    }

}
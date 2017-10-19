/*
 *
 * Worker class
 */

import java.io.*;
import java.nio.file.*;
import java.net.Socket;
import java.lang.StringBuilder;

public class Worker implements Runnable{

    private static String SERVER_NAME = "PMKServer";
		
    private Socket conn;
    private BufferedReader input;
    private DataOutputStream output;
    private String request = "";
    private String response = "";
    private String[] parser;
    @SuppressWarnings("unused")
	private Files file = null;
    
  /* Default constructor, creates the connection to parse out, as well
     * as setting up input and output streams.
     * 
     * @param {Socket} in  A socket with which to send and receive from.
     * 
     * @author Paul Staples paul.staples@ucalgary.ca
     */
    
    public Worker(Socket in){
    	conn = in;
    	try {
	    input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    output = new DataOutputStream(conn.getOutputStream());
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.getMessage());
	}
    }
    
    /* Default run method, uses the Socket conn to receive, parse and reply to properly formed HTTP GET
     * requests. 
     * @author Paul Staples paul.staples@ucalgary.ca
     */
    
    public void run(){
	
	System.out.println("Worker is running");
	
    	try{
	    request = input.readLine();
	    parser = request.split(" ");
	    
	    if(parser.length != 3 || !parser[0].equals("GET") || 
	       !(parser[2].equals("HTTP/1.1") || parser[2].equals("HTTP/1.0"))){
		response = "HTTP/1.1 400 Bad Request\r\n"  +
		    "Date: \r\n" + "Server: " + SERVER_NAME + "\r\n" +
		    "Connection: close\r\n" +
		    "\r\n";;
		
		output.writeBytes(response);
		output.flush();
		output.close();
		input.close();
	    }
	    else{
		
	    StringBuilder sb = new StringBuilder(parser[1]);
	    sb.deleteCharAt(0);
	    String tmp = sb.toString();
	//    System.out.println(tmp);
		Path path = Paths.get(tmp);
		
		if(!Files.exists(path)){
		    response = "HTTP/1.1 404 Bad File Not Found\r\n"  +
			"Date: \r\n" +
			"Server: " + SERVER_NAME + "\r\n" +
			"Connection: close\r\n" +
			"\r\n";;
		    
		    output.writeBytes(response);
		    output.flush();
		    output.close();
		    input.close();
		}
		else{
		    
		    response = "HTTP/1.1 200 OK\r\n" +
			"Date: \r\n" +
			"Server: " + SERVER_NAME + "\r\n" +
			"Content-Length: \r\n" +
			"Content-Type: \r\n" +
			"Connection: close\r\n" +
			"\r\n";

		    byte[] one = response.getBytes();
		    byte[] two = Files.readAllBytes(path);
		    byte[] outputBytes = new byte[one.length + two.length];

		    System.arraycopy(one, 0, outputBytes, 0, one.length);
		    System.arraycopy(two, 0, outputBytes, one.length, two.length);
		    
		    output.write(outputBytes);
		    output.flush();
		    output.close();
		    input.close();
        	}
	    }
	    conn.close();
	}
	catch(Exception e){
	    System.out.println("Error: " + e.getMessage());
	}
    }
}

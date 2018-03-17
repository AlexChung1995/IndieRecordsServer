package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.postgresql.*;

import Communications.HTTPRequest;
import Communications.HTTPResponse;
import Communications.Request;
import Communications.Response;
import db.Driving;
import protocols.HTTP;
import server.Route;
import utils.ByteUtils;
import utils.StringUtils;
//server class for listening and handling requests
public class Server implements Runnable {

	private ExecutorService fixedThreadPool;
	private int port;
	private ServerSocket serverSocket;
	private Route base;
	private Driving db;
	private int sizeData;
	
	/**
	 * Class constructor
	 * @param portNumber specify which port to bind this server to
	 * @param threadNumber specify how many threads will handle requests concurrently
	 * @throws IOException if unable to bind to a server socket
	 */
	public Server(int portNumber, int threadNumber) throws IOException{
		this.port = portNumber;
		System.out.println("portNumber: " + portNumber);
		this.fixedThreadPool = Executors.newFixedThreadPool(threadNumber);
		this.serverSocket = new ServerSocket(port);
		this.sizeData = Byte.SIZE;//default encoding 
	}
	
	/**
	 * Starting function for server
	 * Calling server.run() will begin accepting remtoe requests until an exception is caught
	 * Each time a request is received, the thread pool executor assigns it to a thread
	 */
	public void run() {
		while (true) {
			try {
				System.out.println("accepting new connection");
				Socket clientSocket = this.serverSocket.accept();
				System.out.println("this.base: " + this.base.toString());
				Connection connection = new Connection(clientSocket,this.base, new HTTP());
				this.fixedThreadPool.execute(connection);
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		try {
			this.serverSocket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setSize(int size) {
		this.sizeData = size;
	}
	
	/**
	 * Set the routes this server will function on ie http://yourdomain.com (/yourRoute)
	 * See the Route documentation for further information on how to define a route
	 * To define custom logic on this server software you must use server.setRoutes(Route base) before calling server.run()
	 * @param base base route for your server, typically "/"
	 */
	public void setRoutes(Route base) {
		this.base = base;
	}
	
	/**
	 * use this to initiate an SQL database for use with this server
	 * @param url SQL database link to connect to
	 * @param user username
	 * @param password password
	 * @return Driving, a class for interacting with the database
	 * @throws SQLException throws if no suitable driver found or other SQL problems encountered (ie no database at the supplied url)
	 */
	public Driving initDB(String url, String user, String password) throws SQLException {
		Driving driver = new Driving(url,user,password);
		this.db = driver;
		return driver;
	}
	
	public void initServer() {
		
	}
	
	/**
	 * Get the database interaction object on this server
	 * @return database interaction object
	 */
	public Driving getDB() {
		return this.db;
	}
	
	public static void main(String[] args) {
		try {
			Server server = new Server(Integer.parseInt(System.getenv("PORT")),Integer.parseInt(System.getenv("THREAD_NUMBER")));
			server.initServer();
			server.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

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
	
	public Server(int portNumber, int threadNumber) throws Exception{
		this.port = portNumber;
		System.out.println("portNumber: " + portNumber);
		this.fixedThreadPool = Executors.newFixedThreadPool(threadNumber);
		this.serverSocket = new ServerSocket(port);
		this.sizeData = Byte.SIZE;//default encoding 
	}
	
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
	
	public void setRoutes(Route base) {
		this.base = base;
	}
	
	public Driving initDB(String url, String user, String password) throws SQLException, ClassNotFoundException {
		Driving driver = new Driving(url,user,password);
		this.db = driver;
		return driver;
	}
	
	public void initServer() {
		
	}
	
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

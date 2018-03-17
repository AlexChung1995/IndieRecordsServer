package server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Function;

import Communications.Request;
import Communications.Response;
import utils.ByteUtils;

public class Route {
	
	/**
	 * Defines common RESTful function definitions
	 * @author Anonymous
	 *
	 */
	public static enum REST{
		GET,POST,PUT
	}

	private HashMap<String,Route> router;
	private HashMap<String,Function<Request,Response>> functions; 
	private Function<Request, Response> defaultFunc;
	
	/**
	 * Class constructor
	 * Routes are defined following classic file system tree structure in this server
	 * @param routes a hashmap of String to Route, each String defines a child route of this route object
	 * so if you pass a hashmap of [(one,Route);(two,Route);(three,Route)] and your current route is title "base"
	 * you will have defined behaviour on your server at base/one, base/two, base/three
	 * @param get the get request at this route
	 * @param put the post request at this route
	 * @param post the put request at this route
	 * @param defaultFunc the default function at this route, if any of "get", "post", or "put" is null 
	 * the default func is used when you receive a request to this route
	 */
	public Route(HashMap<String,Route> routes, Function<Request, Response> get, Function <Request, Response> put, 
			Function <Request, Response> post, Function<Request,Response> defaultFunc){
		this.router = routes;
		this.defaultFunc = defaultFunc;
		this.router.put("", this);//loopback
		this.functions = new HashMap<String,Function<Request,Response>>();
		this.functions.put("GET", get);
		this.functions.put("PUT", put);
		this.functions.put("POST", post);
	}
	
	/**
	 * The function to find the route that the request has specified
	 * @param path the path given by the request
	 * @param pathPlace the iteration number along this path
	 * @param request the RESTful request type ("GET", "POST, "PUT")
	 * @return Lambda Function taking type Request and returning a type Response defined by developer
	 */
	public Function<Request,Response> route(String [] path, int pathPlace, String request) {
		System.out.println("path: " + Arrays.toString(path) + " pathPlace: " + pathPlace + " path.length: " + path.length + " " + request + ";");	
		if (pathPlace == path.length) {
			Function<Request,Response> func = this.functions.get(request);
			if (func == null) {
				System.out.println("no route, sending defaultFunc");
				func = this.defaultFunc;
			}
			return func;
		}else {
			try {
				return this.router.get(path[pathPlace]).route(path, pathPlace + 1, request);
			} catch(NullPointerException e) {
				System.out.println("No such route: " + Arrays.toString(path));
				return this.defaultFunc;
			}
		}
	}
	
	/**
	 * toString function for Route
	 */
	public String toString() {
		String routeAsString = "route: {\r\n";
		for (Entry<String, Function<Request,Response>> route: functions.entrySet()) {
			routeAsString += "\r" + route.getKey() + ": " + route.getValue() + "\r\n";
		}
		routeAsString += "}";
		return routeAsString;
	}
	
}

package cz.nuc.wheelgo;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Holder;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import cz.nuc.wheelgo.dijkstra.DijkstraAlgorithm;
import cz.nuc.wheelgo.dijkstra.Edge;
import cz.nuc.wheelgo.dijkstra.Graph;
import cz.nuc.wheelgo.dijkstra.Vertex;

public class NavigationTask {

	private static double offset = 0.003;
	
	public static List<NavigationNode> navigate(Double latFrom, Double longFrom,
			Double latTo, Double longTo, NavigationParameters params) throws Exception {
		
		Date start = new Date();
		System.out.println("Connecting to OSM");
		
		String output = queryOverpassAPI(latFrom, longFrom, latTo, longTo);
		//String output = queryClassicAPI(latFrom, longFrom, latTo, longTo);
		Date end = new Date();
		
		System.out.println("Recieved response from OSM. It took: " + (int)(end.getTime() - start.getTime())/1000 + " s");
		List<Vertex> vertices;
		List<Edge> edges;
		Holder<List<Vertex>> verticesHolder = new Holder<List<Vertex>>();
		Holder<List<Edge>> edgesHolder = new Holder<List<Edge>>();
		try {
			XMLOsmParser.parseMap(output, params, verticesHolder, edgesHolder);
		} catch (XPathExpressionException | SAXException | IOException
				| ParserConfigurationException e) {
			e.printStackTrace();
			return new LinkedList<NavigationNode>();
		}
		edges = edgesHolder.value;
		vertices = verticesHolder.value;

		// find closest vertex to our source and destination point
		Float minSource = Float.MAX_VALUE;
		Float minDestination = Float.MAX_VALUE;
		Vertex source = new Vertex("", "", latFrom, longFrom);
		Vertex destination = new Vertex("", "", latTo, longTo);
		Vertex sourceInOsm = null, destinationInOsm = null;
		for (Edge v : edges) {
			// System.out.println(v);
			float distance = (float) distToSegment(source, v.getSource(), v.getDestination());
			if (minSource > distance) {
				minSource = distance;
				sourceInOsm = v.getSource();
				// System.out.println(v + " -- distance=" + distance + "-- " +
				// source);
			}
			distance = (float) distToSegment(destination, v.getSource(), v.getDestination());
			if (minDestination > distance) {
				minDestination = distance;
				destinationInOsm = v.getDestination();
				// System.out.println(v + " -- distance=" + distance + "-- " +
				// destination);
			}
		}

		if (sourceInOsm == null)
		{
			throw new Exception("Source could not be mapped to OSM node");
		}
		if (destinationInOsm == null)
		{
			throw new Exception("Destination could not be mapped to OSM node");
		}
		if (sourceInOsm.equals(destinationInOsm))
		{
			throw new Exception("Source and destination nodes are same");
		}
		
		Graph g = new Graph(vertices, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(g);
		dijkstra.execute(sourceInOsm); // from
		LinkedList<Vertex> path = dijkstra.getPath(destinationInOsm); // to

		List<NavigationNode> ret = new LinkedList<NavigationNode>();
		if (path != null) {
			for (Vertex vertex : path) {
				// System.out.println(vertex.getLatitude()+","+vertex.getLongitude());
				ret.add(new NavigationNode(vertex));
			}
		}

		return ret;
	}

	public static int generateCode(Double latFrom, Double longFrom,
			Double latTo, Double longTo, List<Location> locationsToAvoid) {
		return (latFrom + "" + longFrom + latTo + longTo + locationsToAvoid.hashCode()).hashCode();
	}
	
	public static int generateCode(Double latFrom, Double longFrom,
			Double latTo, Double longTo) {
		return (latFrom + "" + longFrom + latTo + longTo).hashCode();
	}

	private static double sqr(double x) {
		return x * x;
	}

	private static double dist2(Vertex v, Vertex w) {
		return sqr(v.getLatitude() - w.getLatitude())
				+ sqr(v.getLongitude() - w.getLongitude());
	}

	private static double distToSegmentSquared(Vertex p, Vertex v, Vertex w) {
		double l2 = dist2(v, w);
		if (l2 == 0)
			return dist2(p, v);
		double t = ((p.getLatitude() - v.getLatitude())
				* (w.getLatitude() - v.getLatitude()) + (p.getLongitude() - v
				.getLongitude()) * (w.getLongitude() - v.getLongitude()))
				/ l2;
		if (t < 0)
			return dist2(p, v);
		if (t > 1)
			return dist2(p, w);
		Vertex temp = new Vertex("", "temp");
		temp.setLatitude(v.getLatitude() + t
				* (w.getLatitude() - v.getLatitude()));
		temp.setLongitude(v.getLongitude() + t
				* (w.getLongitude() - v.getLongitude()));
		return dist2(p, temp);
	}

	public static double distToSegment(Vertex p, Vertex v, Vertex w) {
		return Math.sqrt(distToSegmentSquared(p, v, w));
	}
	
	/**
	 * GET /api/0.6/map?bbox=left,bottom,right,top
	 * 
	 * left is the longitude of the left (westernmost) side of the bounding
	 * box. bottom is the latitude of the bottom (southernmost) side of the
	 * bounding box. right is the longitude of the right (easternmost) side
	 * of the bounding box. top is the latitude of the top (northernmost)
	 * side of the bounding box.
	 */
	public static String queryClassicAPI(Double latFrom, Double longFrom,
			Double latTo, Double longTo)
	{
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		client.setReadTimeout(300000);
		client.setConnectTimeout(30000);
		config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT,30000);//30 seconds read timeout
		config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT,30000);//30 seconds read timeout

		URI uri = UriBuilder
				.fromUri("http://api.openstreetmap.org/api/0.6").build();
		WebResource service = client.resource(uri);
		
		String bboxParam = "";
		// left
		if (longFrom < longTo) {
			bboxParam += longFrom-offset;
		} else {
			bboxParam += longTo-offset;
		}
		bboxParam += ",";
		// bottom
		if (latFrom < latTo) {
			bboxParam += latFrom-offset;
		} else {
			bboxParam += latTo-offset;
		}
		bboxParam += ",";
		// right
		if (longFrom > longTo) {
			bboxParam += longFrom+offset;
		} else {
			bboxParam += longTo+offset;
		}
		bboxParam += ",";
		// top
		if (latFrom > latTo) {
			bboxParam += latFrom+offset;
		} else {
			bboxParam += latTo+offset;
		}

		return service.path("map").queryParam("bbox", bboxParam)
				// .accept(MediaType.APPLICATION_XML)
				.get(String.class);
	}
	
	public static String queryOverpassAPI(Double latFrom, Double longFrom,
			Double latTo, Double longTo)
	{
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		client.setReadTimeout(300000);
		client.setConnectTimeout(30000);
		config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT,30000);//30 seconds read timeout
		config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT,30000);//30 seconds read timeout
		
		String bboxParam = "";
		// left
		if (longFrom < longTo) {
			bboxParam += longFrom-offset;
		} else {
			bboxParam += longTo-offset;
		}
		bboxParam += ",";
		// bottom
		if (latFrom < latTo) {
			bboxParam += latFrom-offset;
		} else {
			bboxParam += latTo-offset;
		}
		bboxParam += ",";
		// right
		if (longFrom > longTo) {
			bboxParam += longFrom+offset;
		} else {
			bboxParam += longTo+offset;
		}
		bboxParam += ",";
		// top
		if (latFrom > latTo) {
			bboxParam += latFrom+offset;
		} else {
			bboxParam += latTo+offset;
		}
		
		URI uri = UriBuilder
				.fromUri("http://www.overpass-api.de/api/xapi?way[bbox=" + bboxParam + "]").build();
		WebResource service = client.resource(uri);
				
		return service.get(String.class);
	}
	
}

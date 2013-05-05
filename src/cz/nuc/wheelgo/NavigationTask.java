package cz.nuc.wheelgo;

import java.io.IOException;
import java.net.URI;
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

	public static List<Spot> navigate(Double latFrom, Double longFrom,
			Double latTo, Double longTo) {
		URI baseUri = UriBuilder
				.fromUri("http://api.openstreetmap.org/api/0.6").build();

		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(baseUri);
		/**
		 * GET /api/0.6/map?bbox=left,bottom,right,top
		 * 
		 * left is the longitude of the left (westernmost) side of the bounding
		 * box. bottom is the latitude of the bottom (southernmost) side of the
		 * bounding box. right is the longitude of the right (easternmost) side
		 * of the bounding box. top is the latitude of the top (northernmost)
		 * side of the bounding box.
		 */
		// construct bounding box parameter according to API description above
		String bboxParam = "";
		// left
		if (longFrom < longTo) {
			bboxParam += longFrom;
		} else {
			bboxParam += longTo;
		}
		bboxParam += ",";
		// bottom
		if (latFrom < latTo) {
			bboxParam += latFrom;
		} else {
			bboxParam += latTo;
		}
		bboxParam += ",";
		// right
		if (longFrom > longTo) {
			bboxParam += longFrom;
		} else {
			bboxParam += longTo;
		}
		bboxParam += ",";
		// top
		if (latFrom > latTo) {
			bboxParam += latFrom;
		} else {
			bboxParam += latTo;
		}

		String output = service.path("map").queryParam("bbox", bboxParam)
		// .accept(MediaType.APPLICATION_XML)
				.get(String.class);
		List<Vertex> vertices;
		List<Edge> edges;
		Holder<List<Vertex>> verticesHolder = new Holder<List<Vertex>>();
		Holder<List<Edge>> edgesHolder = new Holder<List<Edge>>();
		try {
			XMLOsmParser.parseMap(output, verticesHolder, edgesHolder);
		} catch (XPathExpressionException | SAXException | IOException
				| ParserConfigurationException e) {
			e.printStackTrace();
			return new LinkedList<Spot>();
		}
		edges = edgesHolder.value;
		vertices = verticesHolder.value;

		// find closest vertex to our source and destination point
		Float minSource = Float.MAX_VALUE;
		Float minDestination = Float.MAX_VALUE;
		Vertex source = new Vertex("", "", latFrom, longFrom);
		Vertex destination = new Vertex("", "", latTo, longTo);
		Vertex sourceInOsm = null, destinationInOsm = null;
		for (Vertex v : vertices) {
			// System.out.println(v);
			float distance = Edge.calculateWeight(source, v);
			if (minSource > distance) {
				minSource = distance;
				sourceInOsm = v;
				// System.out.println(v + " -- distance=" + distance + "-- " +
				// source);
			}
			distance = Edge.calculateWeight(destination, v);
			if (minDestination > distance) {
				minDestination = distance;
				destinationInOsm = v;
				// System.out.println(v + " -- distance=" + distance + "-- " +
				// destination);
			}
		}

		if (sourceInOsm == null || destinationInOsm == null
				|| sourceInOsm.equals(destinationInOsm))
			return new LinkedList<Spot>();

		Graph g = new Graph(vertices, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(g);
		dijkstra.execute(sourceInOsm); // from
		LinkedList<Vertex> path = dijkstra.getPath(destinationInOsm); // to

		List<Spot> ret = new LinkedList<Spot>();
		if (path != null) {
			for (Vertex vertex : path) {
				// System.out.println(vertex.getLatitude()+","+vertex.getLongitude());
				ret.add(new Spot(vertex));
			}
		}

		return ret;
	}

	public static int generateCode(Double latFrom, Double longFrom,
			Double latTo, Double longTo) {
		return (latFrom + "" + longFrom + latTo + longTo).hashCode();
	}

}

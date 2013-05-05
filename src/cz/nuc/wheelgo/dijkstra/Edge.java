package cz.nuc.wheelgo.dijkstra;

/**
 * 
 * @source http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html
 *
 */

public class Edge  {
	  private final String id; 
	  private final Vertex source;
	  private final Vertex destination;
	  private final float weight; 
	  
	  public Edge(String id, Vertex source, Vertex destination, float weight) {
	    this.id = id;
	    this.source = source;
	    this.destination = destination;
	    this.weight = weight;
	  }
	  
	  public String getId() {
	    return id;
	  }
	  public Vertex getDestination() {
	    return destination;
	  }

	  public Vertex getSource() {
	    return source;
	  }
	  public float getWeight() {
	    return weight;
	  }
	  
	  @Override
	  public String toString() {
	    return  "ID=" + id + " -> " + source + " " + destination;
	  }

	public static float calculateWeight(Vertex vertex, Vertex oldVertex) {
	    double earthRadius = 6369;
	    double dLat = Math.toRadians(vertex.getLatitude()-oldVertex.getLatitude());
	    double dLng = Math.toRadians(vertex.getLongitude()-oldVertex.getLongitude());
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(oldVertex.getLatitude())) * Math.cos(Math.toRadians(vertex.getLatitude())) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;


	    return new Float(dist);
	}
	  
	  
	} 
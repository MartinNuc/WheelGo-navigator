package cz.nuc.wheelgo.dijkstra;

import java.util.List;

/**
 * 
 * @source http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html
 *
 */

public class Graph {
  private final List<Vertex> vertexes;
  private final List<Edge> edges;

  public Graph(List<Vertex> vertexes, List<Edge> edges) {
    this.vertexes = vertexes;
    this.edges = edges;
  }

  public List<Vertex> getVertexes() {
    return vertexes;
  }

  public List<Edge> getEdges() {
    return edges;
  }
  
  
  
} 
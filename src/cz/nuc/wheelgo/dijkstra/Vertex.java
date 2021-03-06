package cz.nuc.wheelgo.dijkstra;

/**
 * 
 * @source http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html
 * 
 */

public class Vertex {
	final private String id;
	final private String name;

	private Double latitude = null;
	private Double longitude = null;

	public Vertex(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public Vertex(String id, String name, Double latitude, Double longitude) {
		this.id = id;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(ID=" + id + "__" + latitude + "," + longitude + ")";
	}

}
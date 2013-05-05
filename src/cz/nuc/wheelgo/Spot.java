package cz.nuc.wheelgo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import cz.nuc.wheelgo.dijkstra.Vertex;

@XmlRootElement(name = "spot", namespace="")
@XmlType
public class Spot {

	protected String latitude;
	
	protected String longitude;
	
	public Spot()
	{
		
	}
	
	public Spot(Vertex node)
	{
		this.latitude = "" + node.getLatitude();
		this.longitude = "" + node.getLongitude();
	}
	
	public Spot(String latitude, String longitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	@XmlElement
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	@XmlElement
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	
}

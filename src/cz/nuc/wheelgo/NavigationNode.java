package cz.nuc.wheelgo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import cz.nuc.wheelgo.dijkstra.Vertex;

@XmlRootElement(name = "NavigationNode", namespace="")
@XmlType
public class NavigationNode {

	protected String latitude;
	
	protected String longitude;
	
	public NavigationNode()
	{
		
	}
	
	public NavigationNode(Vertex node)
	{
		this.latitude = "" + node.getLatitude();
		this.longitude = "" + node.getLongitude();
	}
	
	public NavigationNode(String latitude, String longitude)
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

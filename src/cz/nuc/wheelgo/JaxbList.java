package cz.nuc.wheelgo;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(name = "root", namespace="")
@XmlSeeAlso({Spot.class})
public class JaxbList<T> {

	@XmlElement(name = "list")
    protected List<T> list;

    public JaxbList(){}

}

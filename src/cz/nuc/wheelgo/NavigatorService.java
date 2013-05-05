package cz.nuc.wheelgo;

import java.io.StringReader;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

@Path("/api/")
public class NavigatorService {

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/navigate")
	public String navigate(@QueryParam("latFrom") String latFrom,
			@QueryParam("longFrom") String longFrom,
			@QueryParam("latTo") String latTo,
			@QueryParam("longTo") String longTo) throws JAXBException {

		Double latFromDouble;
		Double longFromDouble;
		Double latToDouble;
		Double longToDouble;
		try {
			latFromDouble = Double.parseDouble(latFrom);
			longFromDouble = Double.parseDouble(longFrom);
			latToDouble = Double.parseDouble(latTo);
			longToDouble = Double.parseDouble(longTo);
			createBackendTask(latFromDouble, longFromDouble, latToDouble,
					longToDouble);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		return "id="
				+ NavigationTask.generateCode(latFromDouble, longFromDouble,
						latToDouble, longToDouble);
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/getResult")
	public List<Spot> getPath(@QueryParam("id") String id)
			throws EntityNotFoundException, JAXBException {
		Key k = KeyFactory.createKey("path", Long.valueOf(id));
		Entity result = Util.getDatastoreServiceInstance().get(k);
		String marshalled = ((Text) result.getProperty("path")).getValue();

		JAXBContext context = JAXBContext.newInstance(JaxbList.class);
		Unmarshaller um = context.createUnmarshaller();
		JaxbList<Spot> response = (JaxbList<Spot>) um.unmarshal(new StreamSource(new StringReader(marshalled)));
		return response.list;
	}

	private void createBackendTask(Double latFrom, Double longFrom,
			Double latTo, Double longTo) {
		Queue queue = QueueFactory.getDefaultQueue();
		TaskOptions taskOptions = TaskOptions.Builder.withUrl("/findPath")
				.param("latFrom", latFrom.toString())
				.param("longFrom", longFrom.toString())
				.param("latTo", latTo.toString())
				.param("longTo", longTo.toString()).method(Method.POST);
		queue.add(taskOptions);
	}
}
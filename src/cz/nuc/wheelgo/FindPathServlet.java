package cz.nuc.wheelgo;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

public class FindPathServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		performPathFinding(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		performPathFinding(req, resp);
	}

	@Produces({ MediaType.APPLICATION_JSON })
	private void performPathFinding(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json; charset=utf-8");
		resp.setHeader("Cache-Control", "no-cache");
		Double latFromDouble;
		Double longFromDouble;
		Double latToDouble;
		Double longToDouble;
		try {
			latFromDouble = Double.parseDouble(req.getParameter("latFrom"));
			longFromDouble = Double.parseDouble(req.getParameter("longFrom"));
			latToDouble = Double.parseDouble(req.getParameter("latTo"));
			longToDouble = Double.parseDouble(req.getParameter("longTo"));
			List<Spot> path = NavigationTask.navigate(latFromDouble,
					longFromDouble, latToDouble, longToDouble);

			// marshal path for database
			JAXBContext jc = JAXBContext.newInstance(JaxbList.class);
			Marshaller m = jc.createMarshaller();
			JaxbList<Spot> jaxbList = new JaxbList<Spot>();
			jaxbList.list = path;
			StringWriter sw = new StringWriter();
			m.marshal(jaxbList, sw);

			// use generated ID
			Entity result = new Entity("path", NavigationTask.generateCode(
					latFromDouble, longFromDouble, latToDouble, longToDouble));

			Text text = new Text(sw.toString());
			result.setProperty("path", text);
			result.setProperty("timestamp", new Date());
			Util.persistEntity(result);
		} catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(200);
			return;
		}
	}
}

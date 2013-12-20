package cz.nuc.wheelgo;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
		Gson gson = new Gson();
		
		Date start = new Date();
		System.out.println("----- START");
		
		resp.setContentType("application/json; charset=utf-8");
		resp.setHeader("Cache-Control", "no-cache");
		Double latFromDouble;
		Double longFromDouble;
		Double latToDouble;
		Double longToDouble;
		NavigationParameters params;
		try {
			latFromDouble = Double.parseDouble(req.getParameter("latFrom"));
			longFromDouble = Double.parseDouble(req.getParameter("longFrom"));
			latToDouble = Double.parseDouble(req.getParameter("latTo"));
			longToDouble = Double.parseDouble(req.getParameter("longTo"));
			
			params = gson.fromJson(req.getParameter("gsonParameters"), NavigationParameters.class);
			List<NavigationNode> path = NavigationTask.navigate(latFromDouble,
					longFromDouble, latToDouble, longToDouble, params);

			// marshal path for database
			Text gsonPath = new Text(gson.toJson(path));
			
			/*JAXBContext jc = JAXBContext.newInstance(JaxbList.class);
			Marshaller m = jc.createMarshaller();
			JaxbList<NavigationNode> jaxbList = new JaxbList<NavigationNode>();
			jaxbList.list = path;
			StringWriter sw = new StringWriter();
			m.marshal(jaxbList, sw);
			Text text = new Text(sw.toString());
			*/

			// use generated ID
			int id;
			if (params == null)
			{
				id = NavigationTask.generateCode(
					latFromDouble, longFromDouble, latToDouble, longToDouble);
			}
			else
			{
				id = NavigationTask.generateCode(
						latFromDouble, longFromDouble, latToDouble, longToDouble, params.locationsToAvoid);
			}
			Entity result = new Entity("path", id);

			result.setProperty("path", gsonPath);
			result.setProperty("timestamp", new Date());
			Util.persistEntity(result);
			
			Date end = new Date();
			System.out.println("----- Finished path finding. It took: " + (int)(end.getTime() - start.getTime())/1000 + " s");
			System.out.println("----- Saved with ID: " + id);
		} catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(200);
			return;
		}
	}
}

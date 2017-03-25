package example;

import org.w3c.dom.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
/**
 * Created by davidsu on 13/03/2017.
 */
@Path("/tasks")
public class Tasks {
    //todo return proper errors to client when failing
    //todo which columns must be 'not null' in tasks table?
    @GET
    @Secured
    //@Path("/filteredTasks")
    public String getFilteredTasks(@QueryParam("id") String id,
                                   @QueryParam("status") String status,
                                   @QueryParam("taskType") String type,
                                   @QueryParam("open_date") String openDate,
                                   @QueryParam("exec_date") String execDate,
                                   @QueryParam("page") Integer page) {
        System.out.println("status = " + status);
        System.out.println("type = " + type);
        System.out.println("page = " + page);
        try {
            String filter = buildTaskFilter(id, status, type, openDate, execDate);
            Document doc = JDBC.getFilteredTasks(filter, page);
            return Utils.DocumentToString(doc, true);
        } catch (Exception e) {
            System.out.println("exception in getTasks");
            e.printStackTrace();
        }
        return null;
    }

    private String buildTaskFilter(String id, String status, String type, String openDate, String execDate) {
        ArrayList<String> filterArr = new ArrayList<>();
        filterArr.add(Utils.buildIdFilter(id));
        filterArr.add(Utils.buildFilter(status, "status"));
        filterArr.add(Utils.buildFilter(type, "taskType"));
        filterArr.add(Utils.buildDatesFilter(openDate, "open_date"));
        filterArr.add(Utils.buildDatesFilter(execDate, "exec_date"));
        filterArr.removeAll(Collections.singleton(null));
        String filter = String.join(" and ", filterArr);
        return filter.length() > 0 ? " where " + filter : "";
    }

    //todo - why do we use id in this method?
    @GET
    @Secured
    @Path("/newTaskMetadata")
    public String getNewTaskMetadata(@PathParam("id") String id){
        try {
            Document doc = JDBC.getTaskMetadata();
            return Utils.DocumentToString(doc);
        } catch (Exception e) {
            System.out.println("exception in getUsers");
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Secured
    @Path("/{id : [0-9]+}")
    public String getTaskById(@PathParam("id") String id){
        try {
            Document doc = JDBC.getTask(id);
            return Utils.DocumentToString(doc);
        } catch (Exception e) {
            System.out.println("exception in getUsers");
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Secured
    @Path("/createOrUpdate")
    @Consumes("application/xml")
    public Response createOrUpdate(String incomingXML){
        try {
            Document doc = Utils.createDocumentFromString(incomingXML);
            JDBC.createOrUpdateTask(doc);
            return Response.ok().build();
        } catch (Exception e) {
            System.out.println("exception in getUsers");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

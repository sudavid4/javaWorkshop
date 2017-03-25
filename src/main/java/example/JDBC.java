package example;

import com.mysql.cj.jdbc.result.ResultSetImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JDBC {
    //todo need to implement something to avoid sql injection
    //todo move logic from this class to the specific classes (the relevant classes for the logic)
    //todo do we need the dictionaries? how do we pass info from static tables (id or value)?
    private Connection conn;
    private static final int pageSize = 20;
    private static JDBC instance = null;

    private static HashMap<String, Integer> environmentsDict = new HashMap<String, Integer>();
    private static HashMap<String, Integer> productsDict = new HashMap<String, Integer>();
    private static HashMap<String, Integer> prioritiesDict = new HashMap<String, Integer>();
    private static HashMap<String, Integer> statusesDict = new HashMap<String, Integer>();
    private static HashMap<String, Integer> taskTypesDict = new HashMap<String, Integer>();
    private static HashMap<String, Integer> usersDict = new HashMap<String, Integer>();

    public JDBC() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection("jdbc:mysql://localhost/java_workshop?autoReconnect=true&useSSL=false", "root", "");

    }

    private static JDBC getInstance() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        if (instance == null) {
            instance = new JDBC();
            try {
                init();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private static void init() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, ParserConfigurationException
    {
        Connection conn = getInstance().conn;
        ResultSet rs;
        String _sql;
        Statement stmt = conn.createStatement();

        stmt = conn.createStatement();
        _sql = "select * from environments";
        rs = stmt.executeQuery(_sql);
        Utils.resultSetToDictionary(rs, environmentsDict, 2, 1);
        stmt = conn.createStatement();
        _sql = "select * from priority";
        rs = stmt.executeQuery(_sql);
        Utils.resultSetToDictionary(rs, prioritiesDict ,2, 1);
        stmt = conn.createStatement();
        _sql = "select * from status";
        rs = stmt.executeQuery(_sql);
        Utils.resultSetToDictionary(rs, statusesDict,2, 1);
        stmt = conn.createStatement();
        _sql = "select * from taskTypes";
        rs = stmt.executeQuery(_sql);
        Utils.resultSetToDictionary(rs, taskTypesDict,2,1);
    }

    public static <K,V> void StaticTableToDict(String tableName, HashMap<K,V> dict, int keyIndex, int valIndex ) throws SQLException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        Statement stmt;
        ResultSet rs;
        stmt = getInstance().conn.createStatement();
        rs = stmt.executeQuery(String.format("SELECT * FROM %1s", tableName));
        Utils.resultSetToDictionary(rs, dict, keyIndex, valIndex);
    }

    public static int getUserType(String user, String password) throws SQLException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        Statement stmt;
        ResultSet rs;
        stmt = getInstance().conn.createStatement();
        rs = stmt.executeQuery(String.format("SELECT * FROM users where email = '%1s' and password = '%2s'", user, password));
        return (!rs.next()) ? 0 : rs.getInt("type");
    }

    public static Document getUsers() throws SQLException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Statement stmt;
        ResultSet rs;
        stmt = getInstance().conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM v_Users");
        return Utils.createDocumentFromResultSet((ResultSetImpl) rs, "user");
    }

    public static Document getUser(String userId) throws SQLException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        Statement stmt;
        ResultSet rs;
        String _sql;
        Connection conn = getInstance().conn;

        stmt = conn.createStatement();
        _sql = "select * from users where id = " + userId;
        rs = stmt.executeQuery(_sql);
        Document userDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "task", "tasks");

        stmt = conn.createStatement();
        _sql = "select * from userTypes";
        rs = stmt.executeQuery(_sql);
        Document userTypesDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "userType", "userTypes");
        Document[] docs = {userDoc, userTypesDoc};
        return Utils.mergeDocs(docs);
    }

    private static boolean needToQuote(String str) {
        if (str.matches("^(\\d+|true|false|NULL)$")) {
            return false;
        }
        return true;
    }

    private static String updateFieldsFromDocument(Document doc) {
        NodeList children = doc.getFirstChild().getChildNodes();
        ArrayList<String> arralistresult = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node curr = children.item(i);
            String text = curr.getTextContent().trim();
            if (text != null && text.length() == 0) {
                text = "NULL";
            }
            if (needToQuote(text)) {
                text = "'" + text + "'";
            }
            arralistresult.add(curr.getNodeName() + "=" + text);
        }
        return String.join(", ", arralistresult);
    }

    private static void updateTable(String tableName, Document doc, String id) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String update = "UPDATE " + tableName + " ";
        String set = "SET " + updateFieldsFromDocument(doc) + " ";
        String where = "WHERE id=" + id + ";";
        String sql = update + set + where;
        System.out.println(sql);
        getInstance().conn.createStatement().executeUpdate(sql);
    }

    private static void insertIntoTable(String tableName, Document doc) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        ArrayList<String> columns = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        NodeList children = doc.getFirstChild().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node curr = children.item(i);
            String text = curr.getTextContent().trim();
            if (text != null && text.length() == 0) {
                text = "NULL";
            }
            if (needToQuote(text)) {
                text = "'" + text + "'";
            }
            values.add(text);
            columns.add(curr.getNodeName());
        }
        String sql = "" +
                "INSERT INTO " +
                tableName +
                " ("+ String.join(",", columns) + ") "+
                "VALUES (" + String.join(",", values) + ");";
        System.out.println(sql);
        getInstance().conn.createStatement().executeUpdate(sql);

    }

    //builds an insert command with sql injection protection - currently only checks for the free text "additionalInfo" column (in Tasks)
    private static String buildProtectedInsertCommand(String tableName, ArrayList<String> columns, ArrayList<String> values) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String columnList = String.join(",", columns);
        StringBuilder valuePlaceHolders = new StringBuilder();
        for(int i=0; i < columns.size(); i++)
        {
            if(columns.get(i) == "additionalInfo")
            {
                valuePlaceHolders.append("?,");
            }
            else
            {
                valuePlaceHolders.append(values.get(i) + ",");
            }
            //valuePlaceHolders.append("?,");
        }
        //valuePlaceHolders.append("?");
        valuePlaceHolders.setLength(valuePlaceHolders.length() - 1);
        String insert = String.format("INSERT INTO %1s(%2s) VALUES(%3s)",tableName, columnList, valuePlaceHolders.toString());
        PreparedStatement ps = getInstance().conn.prepareStatement(insert);
        for(int i=0; i < values.size(); i++)
        {
            if(columns.get(i) == "additionalInfo")
            {
                ps.setString(1, values.get(i));
            }
        }
        return ps.toString();
    }

    private static void updateTableFromDocument(Document doc, String tableName) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node idNode;
        String id;

        idNode = doc.getElementsByTagName("id").item(0);
        id = idNode.getTextContent();
        idNode.getParentNode().removeChild(idNode);

        updateTable(tableName, doc, id);
    }

    public static void createOrUpdateTask(Document doc) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        if(doc.getElementsByTagName("id").getLength() == 1){
            updateTableFromDocument(doc, "tasks");
        }else{
            insertIntoTable("tasks", doc);
        }
    }

    public static void createOrUpdateUser(Document doc) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        if(doc.getElementsByTagName("id").getLength() == 1){
            updateTableFromDocument(doc, "users");
        }else{
            insertIntoTable("users", doc);
        }
    }

    public static Document getTaskMetadata() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, ParserConfigurationException {
        Connection conn = getInstance().conn;
        ResultSet rs;
        String _sql;
        Statement stmt = conn.createStatement();


        stmt = conn.createStatement();
        _sql = "select * from taskTypes";
        rs = stmt.executeQuery(_sql);
        Document taskTypesDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "taskTypeEntry", "taskTypes");

        stmt = conn.createStatement();
        _sql = "select * from products";
        rs = stmt.executeQuery(_sql);
        Document productsDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "product", "products");

        stmt = conn.createStatement();
        _sql = "select * from environments";
        rs = stmt.executeQuery(_sql);
        Document environmentsDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "environment", "environments");

        stmt = conn.createStatement();
        _sql = "select id, full_name, email from users";
        rs = stmt.executeQuery(_sql);
        Document usersDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "user", "users");

        stmt = conn.createStatement();
        _sql = "select * from priority";
        rs = stmt.executeQuery(_sql);
        Document priorityDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "priority", "priorities");

        stmt = conn.createStatement();
        _sql = "select * from status";
        rs = stmt.executeQuery(_sql);
        Document statusDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "status", "statuses");

        Document[] docs = {taskTypesDoc, productsDoc, environmentsDoc, usersDoc, priorityDoc, statusDoc};
        return Utils.mergeDocs(docs);

    }

    public static Document getTask(String taskId) throws SQLException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Statement stmt;
        ResultSet rs;
        String _sql;
        Connection conn = getInstance().conn;

        stmt = conn.createStatement();
        _sql = "select * from tasks where id = " + taskId;
        rs = stmt.executeQuery(_sql);
        Document tasksDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "task", "tasks");

        Document metadata = getTaskMetadata();
        Document[] docs = {tasksDoc, metadata};
        return Utils.mergeDocs(docs);

    }

    public static Document getTasks(Integer page) throws SQLException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Statement stmt;
        ResultSet rs;
        String _sql;

        stmt = getInstance().conn.createStatement();
        page = ( page == null ? 1 : page );

        _sql = String.format("select ceil(count(*)/%1s) as 'TotalPages', %2s as 'Page' from Tasks ",pageSize, page);
        rs = stmt.executeQuery(_sql);
        Document pageDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "PageInfo");


        _sql = "" +
                "select t.id, open_date, statusName as status, exec_date, taskType " +
                "from tasks t " +
                "join taskTypes tt on t.taskTypeId = tt.id " +
                "join status s on t.statusId = s.id order by id" +
                String.format(" limit %1s offset %2s",pageSize, (page-1)*pageSize);
        rs = stmt.executeQuery(_sql);
        Document tasksDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "task");
        Document[] docs = {pageDoc, tasksDoc};

        return Utils.mergeDocs(docs);
    }

    public static Document getFilteredTasks(String filter, Integer page) throws SQLException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Statement stmt;
        ResultSet rs;

        stmt = getInstance().conn.createStatement();
        page = ( page == null ? 1 : page );
        String _sql = String.format("select ceil(count(*)/%1s) as 'TotalPages', %2s as 'Page' from v_Tasks %3s",pageSize, page, filter);
        rs = stmt.executeQuery(_sql);
        Document pageDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "PageInfo");
        String pageFilter = String.format(" limit %1s offset %2s",pageSize, (page-1)*pageSize);
        _sql = "select * from v_Tasks " + filter + pageFilter;
        rs = stmt.executeQuery(_sql);
        Document resDoc = Utils.createDocumentFromResultSet((ResultSetImpl) rs, "task");
        Document[] docs = {pageDoc, resDoc};
        return Utils.mergeDocs(docs);
    }

    /*
    SP Parameters:
        IN taskTypeId int,
        IN productId int,
        IN envId int,
        IN requesterId int,
        IN priority int,
        IN open_date DATE,
        IN status VARCHAR(20),
        IN qaGO bool,
        IN rollBack bool,
        IN urgent bool,
        IN additionalInfoText TEXT
     */
//    public boolean saveTask(Task task) throws SQLException, ParserConfigurationException {
//        Statement stmt;
//        ResultSet rs;
//        stmt = conn.createStatement();
//        String date = new SimpleDateFormat("yyyy-MM-dd").format(task.getOpenDate());
//        String sqlCommand = String.format("CALL addTask (%1s,%2s,%3s,%4s,%5s,'%6s','%7s',%8s,%9s,%10s,'%11s')",
//                task.getTaskTypeId(), task.getProductId(), task.getEnvId(), task.getRequesterId(), task.getPriority(),
//                date, task.getStatus(), task.getQaGo(), task.getRollback(), task.getUrgent(), task.getAdditionalInfo());
//        stmt.execute(sqlCommand);
//        return true;
//    }

    public static void main(String[] args) {
        try {
            JDBC jdbc = new JDBC();
            ArrayList<String> cols = new ArrayList<String>(){{
                add("a");
                add("b");
                add("c");
                add("additionalInfo");
            }};
            ArrayList<String> vals = new ArrayList<String>(){{
                add("val1");
                add("val2");
                add("val3");
                add("555");
            }};
            String vv = jdbc.buildProtectedInsertCommand("xxx",cols,vals);
            //Document doc = jdbc.getFilteredTasks(null, null, null, "2017-03-10","2017-03-20", 1);

           //Document doc = jdbc.getUsers();
            //System.out.println(Utils.DocumentToString(doc, true));
        } catch (Exception ex) {
            System.out.println("threw exception");
            System.out.println(ex);
            // handle the error
        }
    }
}
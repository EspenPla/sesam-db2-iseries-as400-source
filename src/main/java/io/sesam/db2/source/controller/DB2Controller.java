package io.sesam.db2.source.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import io.sesam.db2.source.db.DB2IAS400Connector;
import io.sesam.db2.source.db.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Timur Samkharadze
 */
@RestController
public class DB2Controller {

    @Autowired
    private DB2IAS400Connector dbConnector;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private static final Logger LOG = LoggerFactory.getLogger(DB2Controller.class);
    
    @RequestMapping(value = {"/datasets/{table}/entities"}, method = {RequestMethod.GET})
    public void getTableData(@PathVariable String table, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Table tableObj;
        String id = "";
        String lmdt = "";
        if (request.getParameter("lmdt") == null || request.getParameter("id") == null){
            id = "";
            lmdt = "";
            LOG.error("Parameters id or lmdt is not set in the url. Please add it to your pipe and try again.");
        }else{
            id = request.getParameter("id");
            lmdt = request.getParameter("lmdt");
            LOG.info("Primary key field = {} & Last modified field = {}", id, lmdt);       
        }
        long rowCounter = 0;
        String since = "";
        LOG.info("Serving request to fetch data from {} table with id: {}", table, id);
        if (request.getParameter("since") != null){
            since = request.getParameter("since");
            LOG.info("Since value is fetched from Sesam, value: {}", since);
        }else{
            since = "20180101";
            LOG.info("Since value is not set, setting 20180101");
        }
        try {
            tableObj = dbConnector.fetchTable(table, since, id, lmdt);
        } catch (ClassNotFoundException | SQLException ex) {
            response.sendError(500, ex.getMessage());
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        writer.print('[');
        boolean isFirst = true;

        while (tableObj.next()) {
            List<Map<String, Object>> batch = tableObj.nextBatch();
            if (batch.isEmpty()) {
                LOG.warn("Empty batch, break fetching");
                break;
            }
            for (var item : batch) {
                if (!isFirst) {
                    writer.print(',');
                } else {
                    isFirst = false;
                }
                rowCounter++;
                writer.print(MAPPER.writeValueAsString(item));
            }
        }

        writer.print(']');
        writer.flush();
        try {
            tableObj.close();
            LOG.info("Sucessfully processed {} rows", rowCounter);
            LOG.info("Successfully closed DB connection");
        } catch (SQLException ex) {
            LOG.error("Couldn't close DB connection due to", ex);
        }
    }
}
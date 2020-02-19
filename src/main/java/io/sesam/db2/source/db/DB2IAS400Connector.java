package io.sesam.db2.source.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Timur Samkharadze
 */
@Component
public class DB2IAS400Connector {

    private static final String URL_TEMPLATE = "jdbc:as400://%s;libraries=%s;user=%s;password=%s";
    private static final String DRIVER_CLASS = "com.ibm.as400.access.AS400JDBCDriver";

    private final String host;
    private final String dbName;
    private final String user;
    private final String password;

    @Autowired
    public DB2IAS400Connector(DB2IAS400ConnectorConfiguration db2Config) {
        this.host = db2Config.getDbHost();
        this.dbName = db2Config.getDbName();
        this.user = db2Config.getDbUser();
        this.password = db2Config.getDbPassword();
    }
    
    public DB2IAS400Connector(String host, String dbName, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.dbName = dbName;
    }
    
    private Connection connect() throws ClassNotFoundException, SQLException {
        
        Class.forName(DRIVER_CLASS);
        String connUrl = String.format(URL_TEMPLATE, host, dbName, user, password);
        return DriverManager.getConnection(connUrl);
    }

    /**
     * Method to obtain service object for DB table with given name
     *
     * @param tableName name of DB table
     * @param since from Sesam: date of the last received entity
     * @param id name of the primary key in table
     * @param lm field with Last Modified time
     * @return tbale object
     * @throws ClassNotFoundException if DB2 iSeries AS400 driver not found (AS400JDBCDriver)
     * @throws SQLException if any SQL exception occurs during connection
     */
    public Table fetchTable(String tableName, String since, String id, String lm) throws ClassNotFoundException, SQLException {
        String dbCombined = "";
        if (tableName.matches("%[^_A-Z0-9@$#]%")) {
            throw new IllegalArgumentException(String.format("bad table name %s", tableName));
        } else {
            dbCombined = dbName + "." + tableName;
        }
        return new Table(dbCombined, since, id, lm, this.connect());
    }
}
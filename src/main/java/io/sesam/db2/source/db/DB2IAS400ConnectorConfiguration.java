package io.sesam.db2.source.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties
@Component
public class DB2IAS400ConnectorConfiguration {

    @Value("${DB2_HOSTNAME:null}")
    private String dbHost;
    @Value("${DB2_DBNAME:null}")
    private String dbName;
    @Value("${DB2_USERNAME:null}")
    private String dbUser;
    @Value("${DB2_PASSWORD:null}")
    private String dbPassword;

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }
}

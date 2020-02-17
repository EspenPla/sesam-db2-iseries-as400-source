package io.sesam.db2.source.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.as400.access.AS400JDBCResultSet;
import com.ibm.as400.access.AS400JDBCResultSetMetaData;

import org.springframework.beans.factory.annotation.Autowired;
/**
 *
 * @author Timur Samkharadze
 */
public class Table implements AutoCloseable {

    private static final int BATCH_SIZE = 10_000;

    private final Connection conn;
    private final String stmtStr;

    private PreparedStatement pStmt;
    private int offset;
    private int lastBatchSize;
    private AS400JDBCResultSetMetaData metaData;

    protected Table(String dbCombined, String since, String id, String lm, Connection conn) throws SQLException {
        this.conn = conn;

        this.stmtStr = String.format("SELECT CAST(%s AS VARCHAR(20)) AS \"_id\", %s AS \"_updated\", %s.* FROM %s WHERE %s >= %s ORDER BY %s LIMIT %s OFFSET ?", id, lm, dbCombined, dbCombined, lm, since, lm, Table.BATCH_SIZE);


        this.offset = 0; 
        this.lastBatchSize = Table.BATCH_SIZE;
    }

    public boolean next() {
        return !(this.lastBatchSize < Table.BATCH_SIZE);
    }

    /**
     * Method to fetch next batch of data from current table
     *
     * @return
     */
    public List<Map<String, Object>> nextBatch() {
        if (null == this.pStmt) {
            this.prepareStatement();
        }

        this.setOffsetToStatement();

        AS400JDBCResultSet resultSet = (AS400JDBCResultSet) this.getResultSet();

        if (null == this.metaData) {
            this.populateMetadata(resultSet);
        }

        List<Map<String, Object>> resultList = this.fetchBatchData(resultSet);
        this.lastBatchSize = resultList.size();
        this.updateOffset();

        return resultList;
    }

    private void prepareStatement() {
        try {
            this.pStmt = this.conn.prepareStatement(this.stmtStr);
        } catch (SQLException ex) {
            throw new RuntimeException("couldn't prepare statement due to", ex);
        }
    }

    private void setOffsetToStatement() {
        try {
            this.pStmt.setInt(1, this.offset);
        } catch (SQLException ex) {
            throw new RuntimeException("couldn't update offset in query due to", ex);
        }
    }

    private ResultSet getResultSet() {
        try {
            return this.pStmt.executeQuery();
        } catch (SQLException ex) {
            throw new RuntimeException("couldn't fetch result set due to", ex);
        }
    }

    private void populateMetadata(AS400JDBCResultSet resultSet) {
        try {
            this.metaData = (AS400JDBCResultSetMetaData) resultSet.getMetaData();
        } catch (SQLException ex) {
            throw new RuntimeException("couldn't obtain metadata from result set due to", ex);
        }
    }

    private List<Map<String, Object>> fetchBatchData(AS400JDBCResultSet resultSet) {
        List<Map<String, Object>> resultList = new ArrayList<>(Table.BATCH_SIZE);
        try {
            try (resultSet) {
                while (resultSet.next()) {
                    Map<String, Object> item = new HashMap<>(this.metaData.getColumnCount());
                    for (int i = 1; i <= this.metaData.getColumnCount(); i++) {
                        String colLabel = this.metaData.getColumnLabel(i);
                        switch (this.metaData.getColumnTypeName(i)) {
                            case "SMALLINT":
                            case "INTEGER":
                            case "INT":
                                item.put(colLabel, resultSet.getInt(colLabel));
                                break;
                            case "BIGINT":
                                item.put(colLabel, resultSet.getLong(colLabel));
                                break;
                            case "DECIMAL":
                            case "FLOAT":
                                item.put(colLabel, resultSet.getDouble(colLabel));
                                break;
                            case "GRAPHIC":
                            case "VARGRAPHIC":
                                item.put(colLabel, resultSet.getString(colLabel).strip());
                                break;
                            case "VARCHAR":
                                item.put(colLabel, resultSet.getString(colLabel).strip());
                                break;
                            default:
                                String type = this.metaData.getColumnTypeName(i);
                                throw new RuntimeException(String.format("data type %s is not supported", type));
                        }
                    }
                    resultList.add(item);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("couldn't process result set due to", ex);
        }

        return resultList;
    }

    private void updateOffset() {
        this.offset += this.lastBatchSize;
    }

    @Override
    public void close() throws SQLException {
        this.conn.close();
    }
    
}
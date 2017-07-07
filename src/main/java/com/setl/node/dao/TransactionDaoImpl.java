package com.setl.node.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

/**
 * Created by christhornley on 07/07/2017.
 */
public class TransactionDaoImpl implements TransactionDao {
  private static final String SP_SET_HASHES = "CALL wfl_sp_CompleteTransactions(?,?,?)";
  private static final String DB_DRIVERNAME = "com.mysql.cj.jdbc.Driver";
  private static final String DB_SCHEMA = "setlnet";
  private static final String DB_HOST = "jp-dev.opencsd.io:3306";
  private static final String DB_USER = "wfluser";
  private static final String DB_PASSWORD = "9C&tX$o6nDlUqGSD#4o1Lk2Gy0a68b!7I4LW^nBW";
  Connection conn = null;
  public TransactionDaoImpl() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
      conn = getDataSource().getConnection();
    } catch ( IllegalAccessException| InstantiationException|ClassNotFoundException | SQLException ex) {
      ex.printStackTrace();
    }
  }

  public DataSource getDataSource() {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(DB_DRIVERNAME);
    dataSource.setUrl("jdbc:mysql://"+DB_HOST+"/" + DB_SCHEMA + "?noAccessToProcedureBodies=true");
    dataSource.setUsername(DB_USER);
    dataSource.setPassword(DB_PASSWORD);
    return dataSource;
  }

  @Override
  public void update(int height, List<String> transactionHashes) {
    CallableStatement cs=null;
    try {
    cs = conn.prepareCall(SP_SET_HASHES);
      for (String hash : transactionHashes) {
        cs.setString(1, "");
        cs.setInt(2, height);
        cs.setString(3, hash);
        cs.addBatch();
      }
      cs.executeBatch();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    System.out.println("Height = " + height);
    }
}

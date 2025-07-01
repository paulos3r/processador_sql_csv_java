package br.processador.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConnectionFactory {

  public static Connection getConnection(Dialect dialect,
                                         String host,
                                         String port,
                                         String database,
                                         String user,
                                         String password) throws ClassNotFoundException, SQLException {
    Class.forName(dialect.getDriverClass());
    String url = dialect.formatUrl(host,port,database);
    return DriverManager.getConnection(url,user,password);
  }
}

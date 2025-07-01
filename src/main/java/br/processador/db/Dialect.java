package br.processador.db;

public enum Dialect {
  MYSQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC"),
  POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%s/%s"),
  ORACLE("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@%s:%s:%s"),
  FIREBIRD("org.firebirdsql.jdbc.FBDriver", "jdbc:firebirdsql://%s/%s"),
  SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:%s;databaseName=%s");

  private final String driverClass;
  private final String urlPattern;

  Dialect(String driverClass, String urlPattern){
    this.driverClass = driverClass;
    this.urlPattern = urlPattern;
  }

  public String getDriverClass(){
    return driverClass;
  }

  public String formatUrl(String host, String port, String database){
    return String.format(urlPattern,host,port,database);
  }

  public static Dialect fromString(String db){
    return switch (db.toLowerCase()) {
      case "mysql"->MYSQL;
      case "postgressql"->POSTGRESQL;
      case "oracle"->ORACLE;
      case "sqlserver"->SQLSERVER;
      case "firebird" -> FIREBIRD;
      default -> null;
    };
  }
}

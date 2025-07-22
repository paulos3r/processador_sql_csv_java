package br.processador;

import br.processador.db.ConnectionFactory;
import br.processador.db.Dialect;
import br.processador.service.ConsultaExecutor;

import java.io.File;
import java.sql.Connection;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    final String VERSAO = "1.0.0";

    System.out.print("""
            **************************************
            *         VERSÃO %s               *
            **************************************
           
            """.formatted(VERSAO));
    try (Scanner scanner = new Scanner(System.in)) {
      System.out.print("""
              SUPORTE (mysql / postgresql / sqlserver)
              Informe o tipo do banco:""");
      Dialect dialect = Dialect.fromString(scanner.nextLine().trim());

      if (dialect == null) {
        System.err.println("**** Banco de dados não suportado. ****");
        return;
      }

      System.out.print("""
              SUPORTE: (localhost , 127.0.0.1)
              Informe o host:""");
      String host = scanner.nextLine().trim();

      System.out.print("""
              SUPORTE: (PG: 5432, SQL SERVER: 1433, MYSQL: 3306)
              Informe a porta:""");
      String port = scanner.nextLine().trim();

      // caminho C:/MeusBancos/bairros.fdb
      System.out.print("""
              SUPORTE: ( PG: NOMO_DO_BANCO, MYSQL: NOME_BANCO, SQL SERVER: NOME_DO_BANCO
              Informe o nome do banco (schema):""");
      String database = scanner.nextLine().trim();

      System.out.print("""
              SUPORTE: ( PG: postgres, SQL SERVER: sa, MYSQL: root )
              Informe o usuário:""");
      String user = scanner.nextLine().trim();

      System.out.println("""
              SUPORTE: ( CADA BANCO TEM SUA PARTICULARIDADE )
              Informe a senha:""");
      String password = scanner.nextLine().trim();
      System.out.print("""
              SUPORTE: ( CAMINHO DA PASTA COMPLETO ) *** ARQUIVO DEVE SER ( ARQUIVO.sql) ***
              Informe o caminho da pasta com arquivos .sql:""");
      String path = scanner.nextLine().trim();

      try (Connection connection = ConnectionFactory.getConnection(dialect, host, port, database, user, password)) {
        ConsultaExecutor executor = new ConsultaExecutor(connection);
        executor.executarConsultasEmPasta(new File(path));
      }

    } catch (Exception e) {
      System.err.println("Erro: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
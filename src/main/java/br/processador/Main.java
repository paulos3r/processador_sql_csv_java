package br.processador;

import br.processador.db.ConnectionFactory;
import br.processador.db.Dialect;
import br.processador.service.ConsultaExecutor;

import java.io.File;
import java.sql.Connection;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    final String VERSAO = "1.0.1";

    System.out.printf("""
    **************************************
    *         VERSÃO %s               *
    **************************************
   
    """, VERSAO );
    try (Scanner scanner = new Scanner(System.in)) {

      System.out.print("""
              1: mysql
              2: postgresql
              3: sqlserver
              Informe o tipo do banco: """);
      String bancoInput = scanner.nextLine().trim();
      String banco;
      switch (bancoInput){
        case "1" -> banco="mysql";
        case "2" -> banco="postgresql";
        case "3" -> banco="sqlserver";

        default -> {
          System.err.println("**** Banco de dados não suportado. ****");
          return;
        }
      }

      Dialect dialect = Dialect.fromString(banco);

      System.out.print("""
              1: localhost
              2: 127.0.0.1
              3: outros
              Informe o host:""");
      String host = scanner.nextLine().trim();

      switch (host){
        case "1" -> host = "localhost";
        case "2" -> host = "127.0.0.1";
        case "3" -> {
          System.out.println("Informe o host: ");
          host = scanner.nextLine().trim();
        }
        default -> {
          System.err.println("**** Opção invalida ***");
          return;
        }
      }

      System.out.print("""
              1: 3306
              2: 5432
              3: 1433
              4: OUTROS
              Informe a porta:""");
      String port = scanner.nextLine().trim();

      switch (port){
        case "1" -> port = "3306";
        case "2" -> port = "5432";
        case "3" -> port = "1433";
        case "4" -> {
          System.out.println("Informe o host: ");
          port = scanner.nextLine().trim();
        }
        default -> {
          System.err.println("**** Opção invalida ***");
          return;
        }
      }

      // caminho C:/MeusBancos/bairros.fdb
      System.out.print("""
              SUPORTE: ( PG: NOMO_DO_BANCO, MYSQL: NOME_BANCO, SQL SERVER: NOME_DO_BANCO
              Informe o nome do banco (schema):""");
      String database = scanner.nextLine().trim();

      System.out.print("""
              1: root
              2: postgres
              3: sa
              4: outros
              Informe o usuário: """);
      String user = scanner.nextLine().trim();

      switch (user){
        case "1" -> user = "root";
        case "2" -> user = "postgres";
        case "3" -> user = "sa";
        case "4" -> {
          System.out.println("Informe o host: ");
          user = scanner.nextLine().trim();
        }
        default -> {
          System.err.println("**** Opção invalida ***");
          return;
        }
      }

      System.out.println("""
              SUPORTE: ( CADA BANCO TEM SUA PARTICULARIDADE )
              Informe a senha: """);
      String password = scanner.nextLine().trim();

      System.out.print("""
              SUPORTE: ( CAMINHO DA PASTA COMPLETO ) *** ARQUIVO DEVE SER ( ARQUIVO.sql) ***
              Informe o caminho da pasta com arquivos .sql:""");
      String path = scanner.nextLine().trim();

        assert dialect != null;

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
package br.processador;

import br.processador.db.ConnectionFactory;
import br.processador.db.Dialect;
import br.processador.service.ConsultaExecutor;

import java.io.File;
import java.sql.Connection;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    try (Scanner scanner = new Scanner(System.in)) {
      System.out.print("Informe o tipo do banco (mysql/postgresql/oracle/sqlserver/firebird): ");
      Dialect dialect = Dialect.fromString(scanner.nextLine().trim());

      if (dialect == null) {
        System.err.println("Banco de dados não suportado.");
        return;
      }

      System.out.print("Informe o host: ");
      String host = scanner.nextLine().trim();

      System.out.print("Informe a porta: ");
      String port = scanner.nextLine().trim();

      // caminho C:/MeusBancos/bairros.fdb
      System.out.print("Informe o nome do banco (schema): fb passar o caminho EX: C:/MeusBancos/bairros.fdb: ");
      String database = scanner.nextLine().trim();

      System.out.print("Informe o usuário: ");
      String user = scanner.nextLine().trim();

      System.out.print("Informe a senha: ");
      String password = scanner.nextLine().trim();

      System.out.print("Informe o caminho da pasta com arquivos .sql: ");
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
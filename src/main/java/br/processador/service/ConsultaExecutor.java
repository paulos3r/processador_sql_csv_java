package br.processador.service;

import br.processador.util.TextSanitizer; // Assuming this utility exists

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException; // Import SQLException
import java.sql.Statement;

public class ConsultaExecutor {

  private final Connection connection;

  public ConsultaExecutor(Connection connection) {
    // Validate connection to prevent NullPointerExceptions later
    if (connection == null) {
      throw new IllegalArgumentException("A conexão com o banco de dados não pode ser nula.");
    }
    this.connection = connection;
  }

  /**
   * Executes SQL queries found in .sql files within a specified directory.
   * Each .sql file is expected to contain a single SELECT statement on its first line.
   * Results are written to a corresponding .csv file.
   *
   * @param folder The directory containing the .sql files.
   * @throws IllegalArgumentException If the provided File is not an existing directory.
   */
  public void executarConsultasEmPasta(File folder) {
    if (folder == null) {
      throw new IllegalArgumentException("A pasta não pode ser nula.");
    }
    if (!folder.exists() || !folder.isDirectory()) {
      throw new IllegalArgumentException("Caminho inválido ou não é um diretório: " + folder.getAbsolutePath());
    }

    // Use a more robust filter and handle potential null from listFiles
    File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".sql"));

    if (files == null || files.length == 0) {
      System.out.println("Nenhum arquivo .sql encontrado na pasta: " + folder.getAbsolutePath());
      return;
    }

    System.out.println("Processando " + files.length + " arquivo(s) SQL...");
    for (File file : files) {
      executarConsultaArquivo(file); // Corrected method name
    }
    System.out.println("Processamento de consultas concluído.");
  }

  /**
   * Executes a single SQL query from a .sql file and writes the results to a .csv file.
   * The .sql file is expected to contain a single SELECT statement on its first line.
   *
   * @param inputFile The .sql file containing the query.
   */
  private void executarConsultaArquivo(File inputFile) {

    String outputFilePath = inputFile.getAbsolutePath().replaceFirst("\\.sql$", ".csv");
    File outputFile = new File(outputFilePath);

    System.out.println("\nProcessando arquivo: " + inputFile.getName());

    String sql = null;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {

      StringBuilder sqlBuilder = new StringBuilder();

      String lines;
      while ((lines=br.readLine()) != null){
        sqlBuilder.append(lines);
        sqlBuilder.append(" ");
      }
      sql = sqlBuilder.toString();

      sql = sql.replaceAll("[\\t\\n\\r]+", " ").trim();

      if (sql == null || sql.trim().isEmpty()) {
        System.out.println("Ignorado: " + inputFile.getName() + " (arquivo vazio ou linha SQL vazia)");
        return;
      }

      String trimmedSql = sql;

      if (!trimmedSql.toLowerCase().startsWith("select")) {
        System.out.println("Ignorado: " + inputFile.getName() + " ( a primeira linha não é um comando SELECT )");
        return;
      }
      if (trimmedSql.toLowerCase().endsWith("from")) {
        System.out.println("Ignorado: " + inputFile.getName() + " ( o arquivo não e um select valido pois o ultima linha e from )");
        return;
      }

      try (Statement stmt = connection.createStatement();
           ResultSet rs = stmt.executeQuery(trimmedSql)) {

        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        // Write CSV header
        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
          header.append(TextSanitizer.sanitize(meta.getColumnLabel(i)));
          if (i < columnCount) {
            header.append(";");
          }
        }
        bw.write(header.toString());
        bw.newLine();

        // Write data rows
        while (rs.next()) {
          StringBuilder line = new StringBuilder();
          for (int i = 1; i <= columnCount; i++) {

            String value = rs.getString(i);

            value = TextSanitizer.sanitize(value != null ? value : "");

            if (value.contains(";") || value.contains("\"")) {
              value = "\"" + value.replace("\"", "\"\"") + "\"";
            }
            line.append(value);
            if (i < columnCount) {
              line.append(";");
            }
          }
          bw.write(line.toString());
          bw.newLine();
        }

        System.out.println("Arquivo gerado com sucesso: " + outputFile.getName());

      } catch (SQLException e) {

        System.err.println("Erro de SQL ao processar consulta do arquivo " + inputFile.getName() + ": " + e.getMessage());
        if (sql != null) {
          System.err.println("Consulta SQL: " + sql);
        }
        e.printStackTrace();
      }
    } catch (IOException e) {

      System.err.println("Erro de I/O ao processar arquivo " + inputFile.getName() + ": " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("Um erro inesperado ocorreu ao processar arquivo " + inputFile.getName() + ": " + e.getMessage());
      e.printStackTrace();
    }
  }
}
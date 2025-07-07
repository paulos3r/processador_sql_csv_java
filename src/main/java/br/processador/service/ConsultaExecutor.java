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
    // Use a more reliable way to construct the output file path
    String outputFilePath = inputFile.getAbsolutePath().replaceFirst("\\.sql$", ".csv");
    File outputFile = new File(outputFilePath);

    // Informative message before processing each file
    System.out.println("\nProcessando arquivo: " + inputFile.getName());

    String sql = null; // Declare sql outside try-with-resources to use in catch block
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8")); // Specify UTF-8 encoding
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) { // Specify UTF-8 encoding

      StringBuilder sqlBuilder = new StringBuilder();

      String lines;
      while ((lines=br.readLine()) != null){
        sqlBuilder.append(lines);
        sqlBuilder.append(" ");
      }
      sql = sqlBuilder.toString(); // Converte o StringBuilder para String

      // Remove todos os caracteres de tabulação, nova linha e retorno de carro,
      // substituindo-os por um único espaço e depois removendo espaços extras
      sql = sql.replaceAll("[\\t\\n\\r]+", " ");

      if (sql == null || sql.trim().isEmpty()) { // Check for empty or null line after trim
        System.out.println("Ignorado: " + inputFile.getName() + " (arquivo vazio ou linha SQL vazia)");
        return;
      }

      // More robust check for SELECT at the beginning, ignoring comments or leading whitespace
      String trimmedSql = sql.trim();
      if (!trimmedSql.toLowerCase().startsWith("select")) {
        System.out.println("Ignorado: " + inputFile.getName() + " (a primeira linha não é um comando SELECT)");
        return;
      }

      // Attempt to write CSV header (column names)
      try (Statement stmt = connection.createStatement();
           ResultSet rs = stmt.executeQuery(trimmedSql)) { // Use trimmedSql for execution

        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        // Write CSV header
        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
          header.append(TextSanitizer.sanitize(meta.getColumnLabel(i))); // Use getColumnLabel for display names
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
            // Get value as String; handle potential nulls from ResultSet
            String value = rs.getString(i);
            // Sanitize and append; escape quotes if value contains semicolons for CSV
            value = TextSanitizer.sanitize(value != null ? value : ""); // Convert null to empty string before sanitizing
            // Basic CSV escaping: if value contains semicolon or double quote, enclose in double quotes and escape internal double quotes
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
        // Catch SQLException specifically for database errors
        System.err.println("Erro de SQL ao processar consulta do arquivo " + inputFile.getName() + ": " + e.getMessage());
        if (sql != null) {
          System.err.println("Consulta SQL: " + sql);
        }
        // Log stack trace for detailed debugging
        e.printStackTrace();
      }
    } catch (IOException e) {
      // Catch IOException specifically for file I/O errors
      System.err.println("Erro de I/O ao processar arquivo " + inputFile.getName() + ": " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      // Catch any other unexpected exceptions
      System.err.println("Um erro inesperado ocorreu ao processar arquivo " + inputFile.getName() + ": " + e.getMessage());
      e.printStackTrace();
    }
  }
}
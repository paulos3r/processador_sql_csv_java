package br.processador.service;

import br.processador.util.TextSanitizer;

import java.io.*;
import java.sql.*;

public class ExecultarConsultaEmArquivo {

    private final Connection connection;

    public ExecultarConsultaEmArquivo(Connection connection) {
        this.connection = connection;
    }

    public void executarConsultaArquivo(File inputFile) {

        String outputFilePath = inputFile.getAbsolutePath().replaceFirst("\\.sql$", ".csv");
        File outputFile = new File(outputFilePath);

        System.out.println("\nProcessando arquivo: " + inputFile.getName());

        String sql = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"))) {

            StringBuilder sqlBuilder = new StringBuilder();

            String lines;
            while ((lines = br.readLine()) != null) {
                sqlBuilder.append(lines);
                sqlBuilder.append(" ");
            }
            sql = sqlBuilder.toString();

            sql = sql
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();

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
                System.out.println("Ignorado: " + inputFile.getName() + " ( o arquivo não e um select valido pois o ultima linha e FROM )");
                return;
            }
            System.out.println();

            // Somente criar o arquivo de .csv após todas as validações


            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(trimmedSql);
                 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {

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

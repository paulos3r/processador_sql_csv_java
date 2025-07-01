package br.processador.service;

import br.processador.util.TextSanitizer;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class ConsultaExecutor {

  private final Connection connection;


  public ConsultaExecutor(Connection connection) {
    this.connection = connection;
  }

  public void executarConsultasEmPasta(File pasta){
    if (!pasta.exists() || !pasta.isDirectory()){
      throw new IllegalArgumentException("Caminho invalido: " + pasta.getAbsolutePath());
    }

    File[] arquivos = pasta.listFiles((dir, name) -> name.toLowerCase().endsWith(".sql"));

    if (arquivos == null || arquivos.length==0){
      System.out.println("Nenhum arquivo .sql encontrado.");
      return;
    }

    for (File arquivo:arquivos){
      execultarConsultaArquivo(arquivo);
    }

  }

  private void execultarConsultaArquivo(File arquivoEntrada) {
    String arquivoSaida = arquivoEntrada.getAbsolutePath().replace(".sql", ".csv");

    try (BufferedReader br = new BufferedReader(new FileReader(arquivoEntrada));
         BufferedWriter bw = new BufferedWriter(new FileWriter(arquivoSaida))) {

      String sql = br.readLine();
      if (sql == null || !sql.trim().toLowerCase().startsWith("select")) {
        System.out.println("Ignorado: " + arquivoEntrada.getName() + " (não contém SELECT)");
        return;
      }

      try (Statement stmt = connection.createStatement();
           ResultSet rs = stmt.executeQuery(sql)) {

        ResultSetMetaData meta = rs.getMetaData();
        int colunas = meta.getColumnCount();

        while (rs.next()) {
          StringBuilder linha = new StringBuilder();
          for (int i = 1; i <= colunas; i++) {
            String valor = rs.getString(i);
            valor = TextSanitizer.sanitize(valor);
            linha.append(valor);
            if (i < colunas) linha.append(";");
          }
          bw.write(linha.toString());
          bw.newLine();
        }

        System.out.println("Arquivo gerado: " + arquivoSaida);
      }

    } catch (Exception e) {
      System.err.println("Erro ao processar arquivo " + arquivoEntrada.getName() + ": " + e.getMessage());
    }
  }
}

package br.processador.service;

import java.io.File;

public class ExecultarConsultasEmPasta {

    private static ExecultarConsultaEmArquivo executarConsultaArquivo;

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
            executarConsultaArquivo.executarConsultaArquivo(file); // Corrected method name
        }
        System.out.println("Processamento de consultas concluído.");
    }
}

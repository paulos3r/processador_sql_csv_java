package br.processador.util;

import java.text.Normalizer;

public class TextSanitizer {
  public static String sanitize(String input) {
    if (input == null || input.trim().isEmpty()) return "";
    return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .replaceAll("[\\t\\n\\r]+", " ")
            .replaceAll(" +", " ")
            .replaceAll("[ªº°'\"]","")
            .replaceAll("¹","1")
            .replaceAll("²","2")
            .replaceAll("³","3")
            .replaceAll(";",":")
            .trim();
  }
}
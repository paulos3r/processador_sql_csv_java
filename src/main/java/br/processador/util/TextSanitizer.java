package br.processador.util;

import java.text.Normalizer;

public class TextSanitizer {
  public static String sanitize(String input) {
    if (input == null) return "";
    return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .replaceAll("[\\t\\n\\r]+", " ")
            .replaceAll(" +", " ")
            .trim();
  }
}

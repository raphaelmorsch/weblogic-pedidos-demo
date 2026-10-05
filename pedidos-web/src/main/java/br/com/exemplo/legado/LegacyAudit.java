package br.com.exemplo.legado;
import java.io.*;
public final class LegacyAudit {
 private LegacyAudit() { }
 public static void append(String message) throws IOException {
  // Caminho absoluto e armazenamento local: problema intencional de cloud readiness.
  try (FileWriter writer = new FileWriter("/var/legacy/pedidos/audit.log", true)) {
   writer.write(message + System.lineSeparator());
  }
 }
}

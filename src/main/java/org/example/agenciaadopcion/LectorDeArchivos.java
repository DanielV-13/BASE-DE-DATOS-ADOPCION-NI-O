package org.example.agenciaadopcion;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class LectorDeArchivos {

    private static final String[] ORDEN_HOJAS = {
            "ENFERMEDAD", "TIPO_DOCUMENTO", "AMBITO_DOCUMENTO", "FAMILIA",
            "SOLICITANTE", "NIÑO", "PROCESO", "VERIF_DOC_SOLICITANTE",
            "VERIF_DOC_NIÑO", "VERIF_DOC_PROCESO", "TIENE_SOLICITANTE_ENFERMEDAD",
            "TIENE_NIÑO_ENFERMEDAD"
    };

    // --- FASE 1: VALIDACIÓN ---
    public static String validarIntegridad(File archivo) {
        StringBuilder reporte = new StringBuilder("📋 REPORTE PREVIO A LA CARGA:\n\n");
        boolean archivoValido = true;

        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (String nombreHoja : ORDEN_HOJAS) {
                Sheet hoja = buscarHoja(workbook, nombreHoja);
                if (hoja == null) {
                    reporte.append("⚠️ Faltante: Hoja '").append(nombreHoja).append("'\n");
                    continue;
                }

                int filasCorrectas = 0;
                int filasConError = 0;
                String ejemploError = "";

                // IMPORTANTE: i=1 porque la fila 0 son encabezados
                for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                    Row fila = hoja.getRow(i);
                    if (esFilaVacia(fila)) continue;

                    try {
                        validarFila(nombreHoja, fila);
                        filasCorrectas++;
                    } catch (Exception e) {
                        filasConError++;
                        archivoValido = false;
                        if (ejemploError.isEmpty()) ejemploError = "Fila " + (i + 1) + ": " + e.getMessage();
                    }
                }

                reporte.append("📄 ").append(nombreHoja).append(": ").append(filasCorrectas).append(" registros válidos");
                if (filasConError > 0) {
                    reporte.append(" | ❌ ").append(filasConError).append(" ERRORES");
                    reporte.append("\n   👉 Causa probable: ").append(ejemploError);
                }
                reporte.append("\n");
            }

            if (!archivoValido) reporte.append("\n❌ EL ARCHIVO TIENE ERRORES. CORRIJA EL EXCEL.");
            else reporte.append("\n✅ ARCHIVO VALIDADO CORRECTAMENTE. ¿Desea importarlo?");

        } catch (Exception e) {
            return "Error crítico leyendo el archivo: " + e.getMessage();
        }
        return reporte.toString();
    }

    // --- FASE 2: IMPORTACIÓN REAL ---
    public static String importarExcelBD(File archivo) {
        StringBuilder reporte = new StringBuilder("Log de Importación:\n");

        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection conn = ConexionDB.getConnection()) {

            conn.setAutoCommit(false);

            for (String nombreHoja : ORDEN_HOJAS) {
                Sheet hoja = buscarHoja(workbook, nombreHoja);
                if (hoja == null) continue;

                String sql = obtenerSqlParaHoja(nombreHoja);
                if (sql.isEmpty()) continue;

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    int insertados = 0;
                    int errores = 0;
                    String primerError = "";

                    // IMPORTANTE: i=1 porque la fila 0 son encabezados
                    for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                        Row fila = hoja.getRow(i);
                        if (esFilaVacia(fila)) continue;

                        try {
                            llenarPreparedStatement(pstmt, nombreHoja, fila);
                            pstmt.execute();
                            insertados++;
                        } catch (Exception ex) {
                            errores++;
                            if (primerError.isEmpty()) primerError = ex.getMessage();
                            System.out.println("Error en " + nombreHoja + " fila " + (i+1) + ": " + ex.getMessage());
                        }
                    }
                    reporte.append("Results ").append(nombreHoja).append(": ").append(insertados).append(" OK");
                    if (errores > 0) {
                        reporte.append(" | ⚠️ ").append(errores).append(" FALLARON.");
                        reporte.append("\n   -> Razón: ").append(primerError);
                    }
                    reporte.append("\n");
                }
            }
            conn.commit();
            reporte.append("\n--- FIN DEL PROCESO ---");

        } catch (Exception e) {
            return "Error crítico: " + e.getMessage();
        }
        return reporte.toString();
    }

    // --- MÉTODOS AUXILIARES ---

    // 1. LECTURA DE FECHAS TODOTERRENO (Date o String)
    private static java.sql.Date leerFecha(Cell celda) {
        if (celda == null) return null;
        try {
            // Intento 1: Es formato Fecha Excel
            return new java.sql.Date(celda.getDateCellValue().getTime());
        } catch (Exception e) {
            // Intento 2: Es Texto (ej: "1985-05-20" o "20/05/1985")
            try {
                DataFormatter fmt = new DataFormatter();
                String texto = fmt.formatCellValue(celda).trim();
                if (texto.isEmpty()) return null;

                // Si viene como YYYY-MM-DD
                if (texto.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    return java.sql.Date.valueOf(texto);
                }
                // Si viene como DD/MM/YYYY (común en Excel español)
                /*if (texto.contains("/")) {
                     // Aquí podrías parsear con SimpleDateFormat si fuera necesario
                }*/
            } catch (Exception ex2) {
                System.out.println("Fecha inválida: " + celda.toString());
            }
        }
        return null; // Si todo falla
    }

    private static String limpiarID(String textoRaw) {
        if (textoRaw == null) return "";
        return textoRaw.replace("\uFEFF", "").replace("\u00A0", "").trim();
    }

    private static Sheet buscarHoja(Workbook wb, String nombre) {
        Sheet hoja = wb.getSheet(nombre);
        if (hoja == null && nombre.equals("ENFERMEDAD")) hoja = wb.getSheet("ENFERMEDADES");
        if (hoja == null && nombre.equals("NIÑO")) hoja = wb.getSheet("NINO");
        return hoja;
    }

    private static boolean esFilaVacia(Row fila) {
        if (fila == null) return true;
        Cell c = fila.getCell(0);
        return c == null || c.toString().trim().isEmpty();
    }

    private static void validarFila(String hoja, Row fila) throws Exception {
        DataFormatter fmt = new DataFormatter();
        if (hoja.equals("SOLICITANTE")) {
            // Validar Dinero (La fecha ya no la validamos estricta aquí para dejar pasar al importador)
            Cell celdaDinero = fila.getCell(12);
            String ingresoStr = fmt.formatCellValue(celdaDinero);
            String dineroLimpio = ingresoStr.replace("$", "").replace(",", "").trim();
            if (!dineroLimpio.isEmpty()) {
                try { Double.parseDouble(dineroLimpio); } catch (Exception e) { throw new Exception("Dinero mal"); }
            }
        }
    }

    private static String obtenerSqlParaHoja(String hoja) {
        switch (hoja) {
            case "ENFERMEDAD": return "SELECT importar_enfermedad(?, ?, ?)";
            case "TIPO_DOCUMENTO": return "SELECT importar_tipo_documento(?, ?)";
            case "AMBITO_DOCUMENTO": return "SELECT importar_ambito_documento(?, ?, ?)";
            case "FAMILIA": return "SELECT importar_familia(?, ?, ?, ?)";
            case "SOLICITANTE": return "SELECT importar_solicitante(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::numeric)";
            case "NIÑO": return "SELECT importar_nino(?, ?, ?, ?, ?, ?, ?, ?)";
            case "PROCESO": return "SELECT importar_proceso(?, ?, ?, ?, ?, ?)";
            case "TIENE_SOLICITANTE_ENFERMEDAD": return "SELECT importar_tiene_solicitante_enfermedad(?, ?)";
            case "TIENE_NIÑO_ENFERMEDAD": return "SELECT importar_tiene_nino_enfermedad(?, ?)";
            case "VERIF_DOC_SOLICITANTE": return "SELECT importar_verif_doc_solicitante(?, ?, ?)";
            case "VERIF_DOC_NIÑO": return "SELECT importar_verif_doc_nino(?, ?, ?)";
            case "VERIF_DOC_PROCESO": return "SELECT importar_verif_doc_proceso(?, ?, ?)";
            default: return "";
        }
    }

    private static void llenarPreparedStatement(PreparedStatement pstmt, String hoja, Row fila) throws Exception {
        DataFormatter formatter = new DataFormatter();

        if (hoja.equals("ENFERMEDAD")) {
            pstmt.setString(1, limpiarID(formatter.formatCellValue(fila.getCell(0))));
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1)).trim());
            pstmt.setString(3, formatter.formatCellValue(fila.getCell(2)).trim());
        }
        else if (hoja.equals("TIPO_DOCUMENTO")) {
            pstmt.setString(1, limpiarID(formatter.formatCellValue(fila.getCell(0))));
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1)).trim());
        }
        else if (hoja.equals("AMBITO_DOCUMENTO")) {
            pstmt.setString(1, limpiarID(formatter.formatCellValue(fila.getCell(0))));
            String ambitoRaw = formatter.formatCellValue(fila.getCell(1));
            String ambitoCorregido = ambitoRaw.trim().replace("SOLCITANTE", "SOLICITANTE");
            pstmt.setString(2, ambitoCorregido);
            String boolStr = formatter.formatCellValue(fila.getCell(2));
            pstmt.setBoolean(3, Boolean.parseBoolean(boolStr) || boolStr.equalsIgnoreCase("TRUE"));
        }
        else if (hoja.equals("FAMILIA")) {
            pstmt.setString(1, limpiarID(formatter.formatCellValue(fila.getCell(0))));
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1)).trim());
            pstmt.setString(3, formatter.formatCellValue(fila.getCell(2)));
            pstmt.setString(4, formatter.formatCellValue(fila.getCell(3)));
        }
        else if (hoja.equals("SOLICITANTE")) {
            // ... (Tus setters del 1 al 11 siguen igual) ...
            pstmt.setString(1, limpiarID(formatter.formatCellValue(fila.getCell(0))));
            pstmt.setString(2, limpiarID(formatter.formatCellValue(fila.getCell(1))));
            pstmt.setString(3, formatter.formatCellValue(fila.getCell(2)).trim());
            pstmt.setString(4, formatter.formatCellValue(fila.getCell(3)).trim());
            pstmt.setString(5, formatter.formatCellValue(fila.getCell(4)));
            pstmt.setString(6, formatter.formatCellValue(fila.getCell(5)));
            pstmt.setString(7, formatter.formatCellValue(fila.getCell(6)));

            // FECHA (Usando tu método leerFecha)
            pstmt.setDate(8, leerFecha(fila.getCell(7)));

            pstmt.setString(9, formatter.formatCellValue(fila.getCell(8)));
            pstmt.setString(10, formatter.formatCellValue(fila.getCell(9)));
            pstmt.setString(11, formatter.formatCellValue(fila.getCell(10)));
            pstmt.setString(12, formatter.formatCellValue(fila.getCell(11)));

            // --- CORRECCIÓN INTELIGENTE DE DINERO ---
            String ingresoStr = formatter.formatCellValue(fila.getCell(12)).trim();
            // Quitamos símbolo de dólar
            ingresoStr = ingresoStr.replace("$", "").trim();

            // Detectamos si usa coma como decimal (Formato: 2.500,50)
            if (ingresoStr.contains(",") && ingresoStr.indexOf(",") > ingresoStr.indexOf(".")) {
                // Si la coma está AL FINAL, es decimal. Quitamos puntos de miles y cambiamos la coma por punto.
                ingresoStr = ingresoStr.replace(".", ""); // Quitar miles (2.500,50 -> 2500,50)
                ingresoStr = ingresoStr.replace(",", "."); // Poner punto decimal (2500,50 -> 2500.50)
            } else {
                // Formato estándar o inglés (2,500.50). Quitamos comas de miles.
                ingresoStr = ingresoStr.replace(",", "");
            }

            if (ingresoStr.isEmpty()) ingresoStr = "0";

            try {
                pstmt.setDouble(13, Double.parseDouble(ingresoStr));
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error parseando dinero: " + ingresoStr + " -> Se guardará 0");
                pstmt.setDouble(13, 0.0);
            }
        }
        else if (hoja.equals("NIÑO")) {
            pstmt.setString(1, limpiarID(formatter.formatCellValue(fila.getCell(0))));
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1)).trim());
            pstmt.setString(3, formatter.formatCellValue(fila.getCell(2)).trim());
            pstmt.setString(4, formatter.formatCellValue(fila.getCell(3)));
            pstmt.setString(5, formatter.formatCellValue(fila.getCell(4)));
            pstmt.setString(6, formatter.formatCellValue(fila.getCell(5)));

            // USAR EL MÉTODO DE FECHA NUEVO
            pstmt.setDate(7, leerFecha(fila.getCell(6)));

            pstmt.setString(8, formatter.formatCellValue(fila.getCell(7)));
        }
        else if (hoja.equals("PROCESO")) {
            pstmt.setString(1, limpiarID(formatter.formatCellValue(fila.getCell(0))));
            String idNino = limpiarID(formatter.formatCellValue(fila.getCell(1)));
            pstmt.setString(2, idNino.isEmpty() ? null : idNino);
            pstmt.setString(3, limpiarID(formatter.formatCellValue(fila.getCell(2))));

            // USAR EL MÉTODO DE FECHA NUEVO
            java.sql.Date fSol = leerFecha(fila.getCell(3));
            pstmt.setDate(4, fSol != null ? fSol : new java.sql.Date(System.currentTimeMillis()));

            pstmt.setString(5, formatter.formatCellValue(fila.getCell(4)).trim());

            // USAR EL MÉTODO DE FECHA NUEVO
            pstmt.setDate(6, leerFecha(fila.getCell(5)));
        }
        else if (hoja.startsWith("VERIF_DOC")) {
            String idDoc = limpiarID(formatter.formatCellValue(fila.getCell(0)));
            if (idDoc.equals("DOC-00010")) idDoc = "DOC-0010";
            if (idDoc.equals("DOC-00011")) idDoc = "DOC-0011";
            if (idDoc.equals("DOC-00012")) idDoc = "DOC-0012";

            pstmt.setString(1, idDoc);
            pstmt.setString(2, limpiarID(formatter.formatCellValue(fila.getCell(1))));
            String boolStr = formatter.formatCellValue(fila.getCell(2));
            pstmt.setBoolean(3, Boolean.parseBoolean(boolStr) || boolStr.equalsIgnoreCase("TRUE"));
        }
        else if (hoja.startsWith("TIENE_")) {
            pstmt.setString(1, limpiarID(formatter.formatCellValue(fila.getCell(0))));
            pstmt.setString(2, limpiarID(formatter.formatCellValue(fila.getCell(1))));
        }
    }
}
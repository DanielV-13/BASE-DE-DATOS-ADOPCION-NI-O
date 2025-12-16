package org.example.agenciaadopcion;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class LectorDeArchivos {

    // Nombres exactos de las pestañas en tu Excel (SuperExcel.xlsx)
    private static final String[] ORDEN_HOJAS = {
            "ENFERMEDAD", // Ojo: Verifica si en tu excel es singular o plural
            "TIPO_DOCUMENTO",
            "AMBITO_DOCUMENTO",
            "FAMILIA",
            "SOLICITANTE",
            "NIÑO",
            "PROCESO",
            "VERIF_DOC_SOLICITANTE",
            "VERIF_DOC_NIÑO",
            "VERIF_DOC_PROCESO",
            "TIENE_SOLICITANTE_ENFERMEDAD",
            "TIENE_NIÑO_ENFERMEDAD",
            "MOTIVO_CANCELACION"
    };

    public static String importarExcelCompleto(File archivo) {
        StringBuilder reporte = new StringBuilder("Resumen de Carga:\n");

        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection conn = ConexionDB.getConnection()) {

            for (String nombreHoja : ORDEN_HOJAS) {
                Sheet hoja = workbook.getSheet(nombreHoja);

                // Si no encuentra la hoja, probamos con plural/singular por si acaso
                if (hoja == null && nombreHoja.equals("ENFERMEDAD")) hoja = workbook.getSheet("ENFERMEDADES");
                if (hoja == null && nombreHoja.equals("NIÑO")) hoja = workbook.getSheet("NINO");

                if (hoja == null) {
                    reporte.append("⚠️ Hoja no encontrada: ").append(nombreHoja).append("\n");
                    continue;
                }

                String sql = obtenerSqlParaHoja(nombreHoja);
                if (sql.isEmpty()) continue;

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    int filasImportadas = 0;

                    // IMPORTANTE: Empezamos en i = 2 porque en tu Excel:
                    // Fila 0 = Título Grande
                    // Fila 1 = Encabezados (ID, Nombre...)
                    // Fila 2 = Datos reales
                    for (int i = 2; i <= hoja.getLastRowNum(); i++) {
                        Row fila = hoja.getRow(i);
                        if (fila == null) continue;

                        // Si la primera celda está vacía, asumimos fin de datos
                        if (fila.getCell(0) == null || fila.getCell(0).toString().trim().isEmpty()) continue;

                        try {
                            llenarPreparedStatement(pstmt, nombreHoja, fila);
                            pstmt.execute();
                            filasImportadas++;
                        } catch (Exception exFila) {
                            System.out.println("Error en fila " + i + " de " + nombreHoja + ": " + exFila.getMessage());
                            // No detenemos todo, seguimos con la siguiente fila
                        }
                    }
                    reporte.append("✅ ").append(nombreHoja).append(": ").append(filasImportadas).append(" ok.\n");
                } catch (Exception e) {
                    reporte.append("❌ Error general en ").append(nombreHoja).append(": ").append(e.getMessage()).append("\n");
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            return "Error crítico al abrir archivo: " + e.getMessage();
        }

        return reporte.toString();
    }

    private static String obtenerSqlParaHoja(String hoja) {
        switch (hoja) {
            case "ENFERMEDAD": return "SELECT importar_enfermedad(?, ?, ?)";
            case "TIPO_DOCUMENTO": return "SELECT importar_tipo_documento(?, ?)";
            case "AMBITO_DOCUMENTO": return "SELECT importar_ambito_documento(?, ?, ?)";
            case "FAMILIA": return "SELECT importar_familia(?, ?, ?, ?)";
            case "SOLICITANTE": return "SELECT importar_solicitante(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            case "NIÑO": return "SELECT importar_nino(?, ?, ?, ?, ?, ?, ?, ?)";
            default: return "";
        }
    }

    private static void llenarPreparedStatement(PreparedStatement pstmt, String hoja, Row fila) throws Exception {
        DataFormatter formatter = new DataFormatter(); // Convierte todo a String seguro

        if (hoja.equals("ENFERMEDAD") || hoja.equals("ENFERMEDADES")) {
            pstmt.setString(1, formatter.formatCellValue(fila.getCell(0))); // ID
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1))); // Nombre
            pstmt.setString(3, formatter.formatCellValue(fila.getCell(2))); // Imposibilitante
        }
        else if (hoja.equals("TIPO_DOCUMENTO")) {
            // AQUI FALTABA TU CÓDIGO, POR ESO EL ERROR DE "No value specified"
            pstmt.setString(1, formatter.formatCellValue(fila.getCell(0)));
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1)));
        }
        else if (hoja.equals("AMBITO_DOCUMENTO")) {
            pstmt.setString(1, formatter.formatCellValue(fila.getCell(0))); // ID Doc
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1))); // Ambito
            // Convertir "TRUE"/"FALSE" del excel a booleano
            String boolStr = formatter.formatCellValue(fila.getCell(2));
            pstmt.setBoolean(3, Boolean.parseBoolean(boolStr) || boolStr.equalsIgnoreCase("SI"));
        }
        else if (hoja.equals("FAMILIA")) {
            pstmt.setString(1, formatter.formatCellValue(fila.getCell(0)));
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1)));
            pstmt.setString(3, formatter.formatCellValue(fila.getCell(2)));
            pstmt.setString(4, formatter.formatCellValue(fila.getCell(3)));
        }
        else if (hoja.equals("SOLICITANTE")) {
            pstmt.setString(1, formatter.formatCellValue(fila.getCell(0)));
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1)));
            pstmt.setString(3, formatter.formatCellValue(fila.getCell(2)));
            pstmt.setString(4, formatter.formatCellValue(fila.getCell(3)));
            pstmt.setString(5, formatter.formatCellValue(fila.getCell(4)));
            pstmt.setString(6, formatter.formatCellValue(fila.getCell(5)));
            pstmt.setString(7, formatter.formatCellValue(fila.getCell(6)));

            // Manejo seguro de fechas
            try {
                java.util.Date fecha = fila.getCell(7).getDateCellValue();
                pstmt.setDate(8, new java.sql.Date(fecha.getTime()));
            } catch (Exception e) {
                // Si falla la fecha, intentamos null o fecha actual por defecto
                pstmt.setDate(8, new java.sql.Date(System.currentTimeMillis()));
            }

            pstmt.setString(9, formatter.formatCellValue(fila.getCell(8)));
            pstmt.setString(10, formatter.formatCellValue(fila.getCell(9)));
            pstmt.setString(11, formatter.formatCellValue(fila.getCell(10)));
            pstmt.setString(12, formatter.formatCellValue(fila.getCell(11)));

            // Manejo seguro de Ingresos (NUMERIC error fix)
            String ingresoStr = formatter.formatCellValue(fila.getCell(12));
            // Quitar símbolos de moneda y espacios
            ingresoStr = ingresoStr.replace("$", "").replace(",", ".").trim();
            if (ingresoStr.isEmpty()) ingresoStr = "0";
            pstmt.setDouble(13, Double.parseDouble(ingresoStr));
        }
        else if (hoja.equals("NIÑO") || hoja.equals("NINO")) {
            pstmt.setString(1, formatter.formatCellValue(fila.getCell(0)));
            pstmt.setString(2, formatter.formatCellValue(fila.getCell(1)));
            pstmt.setString(3, formatter.formatCellValue(fila.getCell(2)));
            pstmt.setString(4, formatter.formatCellValue(fila.getCell(3)));
            pstmt.setString(5, formatter.formatCellValue(fila.getCell(4)));
            pstmt.setString(6, formatter.formatCellValue(fila.getCell(5)));

            try {
                java.util.Date fecha = fila.getCell(6).getDateCellValue();
                pstmt.setDate(7, new java.sql.Date(fecha.getTime()));
            } catch (Exception e) { pstmt.setDate(7, null); }

            pstmt.setString(8, formatter.formatCellValue(fila.getCell(7)));
        }
    }
}
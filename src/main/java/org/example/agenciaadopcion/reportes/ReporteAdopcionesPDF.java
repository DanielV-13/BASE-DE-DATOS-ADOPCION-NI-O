package org.example.agenciaadopcion.reportes;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;


import java.io.FileOutputStream;
import java.util.List;


public class ReporteAdopcionesPDF extends GeneradorPDF {


    public static void generar(List<ReporteAdopcion> datos, String ruta)
            throws Exception {


        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        doc.open();


        agregarEncabezado(doc, "Reporte de Adopciones Completadas");


        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);


        tabla.addCell(celdaHeader("Proceso"));
        tabla.addCell(celdaHeader("Padres"));
        tabla.addCell(celdaHeader("Niño"));
        tabla.addCell(celdaHeader("Sexo"));
        tabla.addCell(celdaHeader("Educación"));
        tabla.addCell(celdaHeader("Fecha"));


        for (ReporteAdopcion r : datos) {
            tabla.addCell(celda(r.getIdProceso()));
            tabla.addCell(celda(r.getPadres()));
            tabla.addCell(celda(r.getNombreNino()));
            tabla.addCell(celda(r.getSexoNino()));
            tabla.addCell(celda(r.getNivelEducacion()));
            tabla.addCell(celda(
                    r.getFechaSolicitud() != null
                            ? r.getFechaSolicitud().toString()
                            : ""
            ));
        }


        doc.add(tabla);
        doc.close();
    }
}


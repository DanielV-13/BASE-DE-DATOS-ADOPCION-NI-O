package org.example.agenciaadopcion.reportes;


import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.example.agenciaadopcion.reportes.GeneradorPDF;
import org.example.agenciaadopcion.reportes.ReporteEstadoProceso;


import java.io.FileOutputStream;
import java.util.List;


public class ReporteEstadosPDF extends GeneradorPDF {


    public static void generar(List<ReporteEstadoProceso> datos, String ruta)
            throws Exception {


        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        doc.open();


        agregarEncabezado(doc, "Reporte de Estados de Procesos");


        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(60);


        tabla.addCell(celdaHeader("Estado"));
        tabla.addCell(celdaHeader("Cantidad"));


        for (ReporteEstadoProceso r : datos) {
            tabla.addCell(celda(r.getEstadoProceso()));
            tabla.addCell(celda(String.valueOf(r.getCantidad())));
        }


        doc.add(tabla);
        doc.close();
    }
}


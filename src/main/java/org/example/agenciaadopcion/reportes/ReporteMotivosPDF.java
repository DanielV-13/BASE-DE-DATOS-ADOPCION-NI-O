package org.example.agenciaadopcion.reportes;


import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.example.agenciaadopcion.reportes.ReporteMotivoCancelacion;


import java.io.FileOutputStream;
import java.util.List;


public class ReporteMotivosPDF extends GeneradorPDF {


    public static void generar(List<ReporteMotivoCancelacion> datos, String ruta)
            throws Exception {


        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        doc.open();


        agregarEncabezado(doc, "Reporte de Motivos de Cancelación");


        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);


        tabla.addCell(celdaHeader("Motivo"));
        tabla.addCell(celdaHeader("Cantidad"));
        tabla.addCell(celdaHeader("Ejemplos"));


        for (ReporteMotivoCancelacion r : datos) {
            tabla.addCell(celda(r.getTipoMotivo()));
            tabla.addCell(celda(String.valueOf(r.getCantidad())));
            tabla.addCell(celda(r.getEjemplos()));
        }


        doc.add(tabla);
        doc.close();
    }
}


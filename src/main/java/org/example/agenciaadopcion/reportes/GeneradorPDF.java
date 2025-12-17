package org.example.agenciaadopcion.reportes;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileOutputStream;
import java.time.LocalDate;


public class GeneradorPDF {


    protected static Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    protected static Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    protected static Font cellFont = new Font(Font.FontFamily.HELVETICA, 11);


    protected static void agregarEncabezado(Document doc, String titulo)
            throws DocumentException {


        // Título principal
        Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph pTitulo = new Paragraph(titulo, fontTitulo);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        pTitulo.setSpacingAfter(10);


        // Fecha y hora actuales
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


        Font fontInfo = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Paragraph pInfo = new Paragraph(
                "Generado el: " + ahora.format(formato) +
                        "\nUsuario: Administrador del sistema",
                fontInfo
        );
        pInfo.setAlignment(Element.ALIGN_CENTER);
        pInfo.setSpacingAfter(20);


        // Línea separadora
        LineSeparator linea = new LineSeparator();
        linea.setLineColor(BaseColor.LIGHT_GRAY);


        doc.add(pTitulo);
        doc.add(pInfo);
        doc.add(linea);
        doc.add(Chunk.NEWLINE);
    }




    protected static PdfPCell celdaHeader(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, headerFont));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }


    protected static PdfPCell celda(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, cellFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}


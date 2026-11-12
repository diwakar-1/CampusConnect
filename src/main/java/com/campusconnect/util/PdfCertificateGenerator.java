package com.campusconnect.util;

import com.campusconnect.entity.Certificate;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

public final class PdfCertificateGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PdfCertificateGenerator.class);

    private PdfCertificateGenerator() {}

    public static byte[] generateCertificatePdf(Certificate cert, String verificationBaseUrl) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Landscape A4
            Document document = new Document(PageSize.A4.rotate(), 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            PdfContentByte canvas = writer.getDirectContent();
            float pageWidth = PageSize.A4.rotate().getWidth();
            float pageHeight = PageSize.A4.rotate().getHeight();

            // Outer decorative border
            canvas.setColorStroke(new Color(99, 102, 241)); // Indigo accent
            canvas.setLineWidth(5f);
            canvas.rectangle(20, 20, pageWidth - 40, pageHeight - 40);
            canvas.stroke();

            // Inner thin gold border
            canvas.setColorStroke(new Color(234, 179, 8)); // Gold
            canvas.setLineWidth(1.5f);
            canvas.rectangle(26, 26, pageWidth - 52, pageHeight - 52);
            canvas.stroke();

            // Fonts
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(15, 23, 42));
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 12, new Color(100, 116, 139));
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, new Color(99, 102, 241));
            Font recipientLeadFont = FontFactory.getFont(FontFactory.HELVETICA, 13, new Color(71, 85, 105));
            Font recipientNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new Color(15, 23, 42));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 13, new Color(51, 65, 85));
            Font eventFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(99, 102, 241));
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(148, 163, 184));
            Font signatureTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(30, 41, 59));

            // Header Section
            Paragraph institutionPara = new Paragraph("CGC UNIVERSITY MOHALI", headerFont);
            institutionPara.setAlignment(Element.ALIGN_CENTER);
            document.add(institutionPara);

            Paragraph campusPara = new Paragraph("Landran, Sector 112, Greater Mohali, Punjab 140307", subHeaderFont);
            campusPara.setAlignment(Element.ALIGN_CENTER);
            campusPara.setSpacingAfter(15f);
            document.add(campusPara);

            // Certificate Type Title
            Paragraph certTitlePara = new Paragraph(cert.getTypeDisplayName().toUpperCase(), titleFont);
            certTitlePara.setAlignment(Element.ALIGN_CENTER);
            certTitlePara.setSpacingAfter(18f);
            document.add(certTitlePara);

            // Presentation text
            Paragraph presentPara = new Paragraph("THIS IS PROUDLY PRESENTED TO", recipientLeadFont);
            presentPara.setAlignment(Element.ALIGN_CENTER);
            presentPara.setSpacingAfter(8f);
            document.add(presentPara);

            // Student Name
            Paragraph namePara = new Paragraph(cert.getStudentName().toUpperCase(), recipientNameFont);
            namePara.setAlignment(Element.ALIGN_CENTER);
            namePara.setSpacingAfter(6f);
            document.add(namePara);

            // College / Institution
            Paragraph collegePara = new Paragraph("of " + cert.getCollegeName(), recipientLeadFont);
            collegePara.setAlignment(Element.ALIGN_CENTER);
            collegePara.setSpacingAfter(14f);
            document.add(collegePara);

            // Description / Event detail
            String eventDateStr = cert.getEvent().getStartDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            Paragraph descPara = new Paragraph();
            descPara.setAlignment(Element.ALIGN_CENTER);
            descPara.setFont(bodyFont);
            descPara.add(new Chunk("for active and successful participation in "));
            descPara.add(new Chunk(cert.getEvent().getTitle(), eventFont));
            descPara.add(new Chunk(" held on " + eventDateStr + " organized by CGC University Mohali."));
            descPara.setSpacingAfter(25f);
            document.add(descPara);

            // Lower Section: Table with Signatures and QR Code
            Table table = new Table(3);
            table.setBorder(Rectangle.NO_BORDER);
            table.setWidth(100f);
            table.setWidths(new float[]{35f, 30f, 35f});

            // Cell 1: Date & Verification
            Cell leftCell = new Cell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph datePara = new Paragraph("Date of Issue: " + cert.getIssueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), metaFont);
            Paragraph idPara = new Paragraph("Cert ID: " + cert.getCertificateNumber(), metaFont);
            leftCell.add(datePara);
            leftCell.add(idPara);
            table.addCell(leftCell);

            // Cell 2: QR Code for Verification
            Cell centerCell = new Cell();
            centerCell.setBorder(Rectangle.NO_BORDER);
            centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            try {
                String verifyUrl = verificationBaseUrl + "/certificates/verify/" + cert.getVerificationToken();
                byte[] qrBytes = QrCodeGenerator.generateQrCodeImage(verifyUrl, 85, 85);
                Image qrImage = Image.getInstance(qrBytes);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                centerCell.add(qrImage);
                Paragraph qrLabel = new Paragraph("Scan to Verify", metaFont);
                qrLabel.setAlignment(Element.ALIGN_CENTER);
                centerCell.add(qrLabel);
            } catch (Exception e) {
                logger.warn("Could not attach QR code to certificate PDF: {}", e.getMessage());
            }
            table.addCell(centerCell);

            // Cell 3: Signatures
            Cell rightCell = new Cell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph sigLine = new Paragraph("_________________________", signatureTitleFont);
            sigLine.setAlignment(Element.ALIGN_CENTER);
            Paragraph signatoryPara = new Paragraph(cert.getAuthorizedSignatory(), signatureTitleFont);
            signatoryPara.setAlignment(Element.ALIGN_CENTER);
            Paragraph signSubPara = new Paragraph("Campus Event Committee", metaFont);
            signSubPara.setAlignment(Element.ALIGN_CENTER);
            rightCell.add(sigLine);
            rightCell.add(signatoryPara);
            rightCell.add(signSubPara);
            table.addCell(rightCell);

            document.add(table);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            logger.error("Failed to generate PDF certificate: {}", e.getMessage());
            throw new RuntimeException("Error rendering certificate PDF", e);
        }
    }
}


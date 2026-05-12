package com.suwapatha.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.suwapatha.dto.MedicalRecordResponse;
import com.suwapatha.entity.User;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfGeneratorService {

    public byte[] generateMedicalRecordsPdf(User patient, List<MedicalRecordResponse> records) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font styles
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.decode("#94B4C1"));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("Medical History Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Patient Info
            Paragraph patientInfo = new Paragraph();
            patientInfo.add(new Chunk("Patient Name: ", boldFont));
            patientInfo.add(new Chunk(patient.getFirstName() + " " + patient.getLastName() + "\n", normalFont));
            patientInfo.add(new Chunk("Patient ID: ", boldFont));
            patientInfo.add(new Chunk(patient.getId() + "\n", normalFont));
            patientInfo.add(new Chunk("Report Date: ", boldFont));
            patientInfo.add(new Chunk(java.time.LocalDate.now().toString() + "\n", normalFont));
            patientInfo.setSpacingAfter(20);
            document.add(patientInfo);

            document.add(new Paragraph("Medical Records Summary", headerFont));
            document.add(new Paragraph(" ", normalFont)); // spacer

            for (MedicalRecordResponse record : records) {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1, 3});
                table.setSpacingBefore(10);
                table.setSpacingAfter(10);

                // Header Cell
                PdfPCell headerCell = new PdfPCell(new Phrase("Visit: " + record.getDate(), boldFont));
                headerCell.setColspan(2);
                headerCell.setBackgroundColor(Color.decode("#F3F4F6"));
                headerCell.setPadding(8);
                table.addCell(headerCell);

                // Hospital & Doctor
                table.addCell(new Phrase("Hospital", boldFont));
                table.addCell(new Phrase(record.getHospital(), normalFont));
                
                table.addCell(new Phrase("Doctor", boldFont));
                table.addCell(new Phrase(record.getDoctor(), normalFont));

                // Vitals
                table.addCell(new Phrase("Vitals", boldFont));
                String vitals = String.format("BP: %s | Pulse: %s bpm | Weight: %s kg | Temp: %s °F", 
                    record.getBp(), record.getPulse(), record.getWeight(), record.getTemp());
                table.addCell(new Phrase(vitals, normalFont));

                // Diagnosis
                table.addCell(new Phrase("Diagnosis", boldFont));
                table.addCell(new Phrase(record.getDiagnosis(), normalFont));

                // Notes
                table.addCell(new Phrase("Notes", boldFont));
                table.addCell(new Phrase(record.getConsultationNotes(), normalFont));

                document.add(table);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return out.toByteArray();
    }
}

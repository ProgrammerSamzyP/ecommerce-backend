package com.ecommerce.app.service;

import com.ecommerce.app.model.Order;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] generateInvoice(Order order) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);

            // Use Helvetica – works perfectly now
            PDType1Font font = PDType1Font.HELVETICA;

            // Title
            content.beginText();
            content.setFont(font, 18);
            content.newLineAtOffset(50, 750);
            content.showText("INVOICE");
            content.endText();

            // Order info
            content.beginText();
            content.setFont(font, 12);
            content.newLineAtOffset(50, 720);
            content.showText("Order ID: " + order.getId().substring(0, 8));
            content.newLineAtOffset(0, -20);
            content.showText("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            content.newLineAtOffset(0, -20);
            content.showText("Customer: " + order.getUserEmail());
            content.newLineAtOffset(0, -20);
            content.showText("Status: " + order.getStatus());
            content.endText();

            // Table header
            int y = 640;
            content.beginText();
            content.setFont(font, 12);
            content.newLineAtOffset(50, y);
            content.showText("Product");
            content.newLineAtOffset(250, 0);
            content.showText("Qty");
            content.newLineAtOffset(50, 0);
            content.showText("Price");
            content.newLineAtOffset(50, 0);
            content.showText("Subtotal");
            content.endText();

            // Items
            y -= 20;
            for (Order.OrderItem item : order.getItems()) {
                content.beginText();
                content.setFont(font, 10);
                content.newLineAtOffset(50, y);
                content.showText(item.getProductName());
                content.newLineAtOffset(250, 0);
                content.showText(String.valueOf(item.getQuantity()));
                content.newLineAtOffset(50, 0);
                content.showText("NGN " + String.format("%.2f", item.getPrice()));         // ✅ Naira replaced
                content.newLineAtOffset(50, 0);
                content.showText("NGN " + String.format("%.2f", item.getPrice() * item.getQuantity())); // ✅
                content.endText();
                y -= 20;
            }

            // Total
            content.beginText();
            content.setFont(font, 12);
            content.newLineAtOffset(50, y - 10);
            content.showText("TOTAL: NGN " + String.format("%.2f", order.getTotal()));     // ✅
            content.endText();

            content.close();
            document.save(out);
            return out.toByteArray();
        }
    }
}
package com.invoiceapp.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
@Slf4j
public class OcrService {

    private final Tesseract tesseract;

    public OcrService() {
        this.tesseract = new Tesseract();
        // Set the tessdata path if you have it in a custom location
        // tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);
    }

    /**
     * Extract text from image bytes
     */
    public String extractTextFromImage(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                log.warn("Failed to read image for OCR");
                return "";
            }

            String text = tesseract.doOCR(image);
            log.debug("OCR extracted text length: {}", text.length());
            return text;
        } catch (TesseractException | IOException e) {
            log.error("Error extracting text from image", e);
            return "";
        }
    }

    /**
     * Extract text from PDF bytes
     */
    public String extractTextFromPdf(byte[] pdfBytes) {
        StringBuilder text = new StringBuilder();

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String pageText = tesseract.doOCR(image);
                text.append(pageText).append("\n\n");
            }

            log.debug("OCR extracted text from PDF, total length: {}", text.length());
            return text.toString();
        } catch (IOException | TesseractException e) {
            log.error("Error extracting text from PDF", e);
            return "";
        }
    }

    /**
     * Extract text from file based on content type
     */
    public String extractText(byte[] fileBytes, String contentType) {
        if (contentType.startsWith("image/")) {
            return extractTextFromImage(fileBytes);
        } else if (contentType.equals("application/pdf")) {
            return extractTextFromPdf(fileBytes);
        } else {
            log.warn("Unsupported content type for OCR: {}", contentType);
            return "";
        }
    }
}

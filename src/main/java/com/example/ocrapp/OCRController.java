package com.example.ocrapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/upload")
    public ResponseEntity<byte[]> uploadAndTranslate(
            @RequestParam("file") MultipartFile image,
            @RequestParam("format") String format) {
        try {
            String extractedText = geminiService.extractTextFromImage(image);
            System.out.println("Extracted text: " + extractedText);

            if (extractedText == null || extractedText.startsWith("❌") || extractedText.startsWith("⚠️")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(extractedText.getBytes(StandardCharsets.UTF_8));
            }

            byte[] fileBytes;
            String fileName;
            String mimeType;

            if ("pdf".equalsIgnoreCase(format)) {
                fileBytes = FileGenerator.createPDF(extractedText);
                fileName = "translated.pdf";
                mimeType = "application/pdf";
            } else {
                fileBytes = FileGenerator.createDOCX(extractedText);
                fileName = "translated.docx";
                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("❌ Unexpected server error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }
}
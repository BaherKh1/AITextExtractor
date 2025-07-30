package com.example.ocrapp;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DocxGenerator {

    public static ByteArrayOutputStream generateDocx(String text) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph paragraph = document.createParagraph();
            paragraph.createRun().setText(text);

            document.write(out);
            return out;
        }
    }
}

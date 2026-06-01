package ru.adnr.converter.conversion.converter.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;
import ru.adnr.converter.conversion.context.ConversionContext;
import ru.adnr.converter.conversion.converter.FileConverter;
import ru.adnr.converter.conversion.exception.TxtConversionException;
import ru.adnr.converter.conversion.model.FileType;

@Component
public class TxtToPdfConverter implements FileConverter {

    private static final float FONT_SIZE = 12;
    private static final float LEADING = 16;
    private static final float MARGIN = 50;
    private static final PDType1Font FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    @Override
    public boolean supports(FileType fileType) {
        return fileType == FileType.TXT;
    }

    @Override
    public byte[] convertToPdf(InputStream inputStream, ConversionContext context) {
        try {
            String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return createPdf(text);
        } catch (IOException ex) {
            throw new TxtConversionException(context.originalFileName(), ex);
        }
    }

    private byte[] createPdf(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = startContentStream(document, page);
            float currentY = page.getMediaBox().getHeight() - MARGIN;
            float maxTextWidth = page.getMediaBox().getWidth() - 2 * MARGIN;

            for (String sourceLine : text.split("\\R", -1)) {
                List<String> wrappedLines = wrapLine(sourceLine, maxTextWidth);
                if (wrappedLines.isEmpty()) {
                    wrappedLines = List.of("");
                }

                for (String line : wrappedLines) {
                    if (currentY <= MARGIN) {
                        contentStream.endText();
                        contentStream.close();

                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = startContentStream(document, page);
                        currentY = page.getMediaBox().getHeight() - MARGIN;
                    }

                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -LEADING);
                    currentY -= LEADING;
                }
            }

            contentStream.endText();
            contentStream.close();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private PDPageContentStream startContentStream(PDDocument document, PDPage page) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(FONT, FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);
        return contentStream;
    }

    private List<String> wrapLine(String line, float maxTextWidth) throws IOException {
        List<String> wrappedLines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : line.split("\\s+")) {
            if (word.isBlank()) {
                continue;
            }

            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (textWidth(candidate) <= maxTextWidth) {
                currentLine.setLength(0);
                currentLine.append(candidate);
                continue;
            }

            if (!currentLine.isEmpty()) {
                wrappedLines.add(currentLine.toString());
                currentLine.setLength(0);
            }

            if (textWidth(word) <= maxTextWidth) {
                currentLine.append(word);
            } else {
                wrappedLines.addAll(splitLongWord(word, maxTextWidth));
            }
        }

        if (!currentLine.isEmpty()) {
            wrappedLines.add(currentLine.toString());
        }

        return wrappedLines;
    }

    private List<String> splitLongWord(String word, float maxTextWidth) throws IOException {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();

        for (char character : word.toCharArray()) {
            String candidate = currentPart.toString() + character;
            if (textWidth(candidate) <= maxTextWidth) {
                currentPart.append(character);
            } else {
                if (!currentPart.isEmpty()) {
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                }
                currentPart.append(character);
            }
        }

        if (!currentPart.isEmpty()) {
            parts.add(currentPart.toString());
        }

        return parts;
    }

    private float textWidth(String text) throws IOException {
        return FONT.getStringWidth(text) / 1000 * FONT_SIZE;
    }
}

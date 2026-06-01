package ru.adnr.converter.conversion.converter.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import ru.adnr.converter.conversion.context.ConversionContext;
import ru.adnr.converter.conversion.model.FileType;

import static org.assertj.core.api.Assertions.assertThat;

class TxtToPdfConverterTest {

    private final TxtToPdfConverter converter = new TxtToPdfConverter();

    @Test
    void supportsOnlyTxt() {
        assertThat(converter.supports(FileType.TXT)).isTrue();
        assertThat(converter.supports(FileType.PNG)).isFalse();
    }

    @Test
    void convertsTextToPdfAndPreservesTextContent() throws IOException {
        String text = """
                First line
                Second line

                Last line
                """;

        byte[] pdf = converter.convertToPdf(
                new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
                new ConversionContext("test.txt", "corr-1", FileType.TXT)
        );

        assertThat(pdf).startsWith("%PDF".getBytes(StandardCharsets.US_ASCII));

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String extractedText = new PDFTextStripper().getText(document);

            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            assertThat(extractedText).contains("First line", "Second line", "Last line");
        }
    }

    @Test
    void wrapsLongTextIntoPdf() throws IOException {
        String longLine = "long-word ".repeat(400);

        byte[] pdf = converter.convertToPdf(
                new ByteArrayInputStream(longLine.getBytes(StandardCharsets.UTF_8)),
                new ConversionContext("long.txt", "corr-2", FileType.TXT)
        );

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);
        }
    }
}

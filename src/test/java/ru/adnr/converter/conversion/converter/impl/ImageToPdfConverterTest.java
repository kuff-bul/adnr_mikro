package ru.adnr.converter.conversion.converter.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import ru.adnr.converter.conversion.context.ConversionContext;
import ru.adnr.converter.conversion.model.FileType;

import static org.assertj.core.api.Assertions.assertThat;

class ImageToPdfConverterTest {

    private final ImageToPdfConverter converter = new ImageToPdfConverter();

    @Test
    void supportsPngAndJpg() {
        assertThat(converter.supports(FileType.PNG)).isTrue();
        assertThat(converter.supports(FileType.JPG)).isTrue();
        assertThat(converter.supports(FileType.TXT)).isFalse();
        assertThat(converter.supports(FileType.ZIP)).isFalse();
    }

    @Test
    void convertsPngToSinglePagePdf() throws IOException {
        byte[] image = createImage("png");

        byte[] pdf = converter.convertToPdf(
                new ByteArrayInputStream(image),
                new ConversionContext("image.png", "corr-1", FileType.PNG)
        );

        assertThat(pdf).startsWith("%PDF".getBytes());

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void convertsJpgToSinglePagePdf() throws IOException {
        byte[] image = createImage("jpg");

        byte[] pdf = converter.convertToPdf(
                new ByteArrayInputStream(image),
                new ConversionContext("image.jpg", "corr-2", FileType.JPG)
        );

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }

    private byte[] createImage(String format) throws IOException {
        BufferedImage image = new BufferedImage(120, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(Color.BLUE);
        graphics.fillRect(10, 10, 100, 60);
        graphics.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }
}

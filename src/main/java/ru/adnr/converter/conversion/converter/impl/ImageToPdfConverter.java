package ru.adnr.converter.conversion.converter.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;
import ru.adnr.converter.conversion.context.ConversionContext;
import ru.adnr.converter.conversion.converter.FileConverter;
import ru.adnr.converter.conversion.exception.ImageConversionException;
import ru.adnr.converter.conversion.model.FileType;

@Component
public class ImageToPdfConverter implements FileConverter {

    private static final float MARGIN = 36;

    @Override
    public boolean supports(FileType fileType) {
        return fileType == FileType.PNG || fileType == FileType.JPG;
    }

    @Override
    public byte[] convertToPdf(InputStream inputStream, ConversionContext context) {
        try {
            byte[] imageBytes = inputStream.readAllBytes();
            return createPdf(imageBytes, context.originalFileName());
        } catch (IOException ex) {
            throw new ImageConversionException(context.originalFileName(), ex);
        }
    }

    private byte[] createPdf(byte[] imageBytes, String imageName) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDImageXObject image = PDImageXObject.createFromByteArray(document, imageBytes, imageName);
            ImagePlacement placement = calculatePlacement(page.getMediaBox(), image);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(image, placement.x(), placement.y(), placement.width(), placement.height());
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private ImagePlacement calculatePlacement(PDRectangle pageSize, PDImageXObject image) {
        float availableWidth = pageSize.getWidth() - 2 * MARGIN;
        float availableHeight = pageSize.getHeight() - 2 * MARGIN;

        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        float scale = Math.min(availableWidth / imageWidth, availableHeight / imageHeight);

        float targetWidth = imageWidth * scale;
        float targetHeight = imageHeight * scale;
        float x = (pageSize.getWidth() - targetWidth) / 2;
        float y = (pageSize.getHeight() - targetHeight) / 2;

        return new ImagePlacement(x, y, targetWidth, targetHeight);
    }

    private record ImagePlacement(float x, float y, float width, float height) {
    }
}

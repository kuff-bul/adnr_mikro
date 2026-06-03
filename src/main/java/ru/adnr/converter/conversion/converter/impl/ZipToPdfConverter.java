package ru.adnr.converter.conversion.converter.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import ru.adnr.converter.conversion.context.ConversionContext;
import ru.adnr.converter.conversion.converter.FileConverter;
import ru.adnr.converter.conversion.exception.ConverterNotFoundException;
import ru.adnr.converter.conversion.exception.ZipConversionException;
import ru.adnr.converter.conversion.model.FileType;
import ru.adnr.converter.conversion.registry.ConverterRegistry;
import ru.adnr.converter.conversion.service.FileTypeResolver;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZipToPdfConverter implements FileConverter {

    private final ObjectProvider<ConverterRegistry> converterRegistryProvider;
    private final FileTypeResolver fileTypeResolver;

    @Override
    public boolean supports(FileType fileType) {
        return fileType == FileType.ZIP;
    }

    @Override
    public byte[] convertToPdf(InputStream inputStream, ConversionContext context) {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            return convertZipEntries(zipInputStream, context);
        } catch (IOException ex) {
            throw new ZipConversionException(context.originalFileName(), ex);
        }
    }

    private byte[] convertZipEntries(ZipInputStream zipInputStream, ConversionContext context) throws IOException {
        PDFMergerUtility mergerUtility = new PDFMergerUtility();
        List<RandomAccessReadBuffer> pdfSources = new ArrayList<>();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    continue;
                }

                convertEntry(zipInputStream, zipEntry, context)
                        .ifPresent(pdfBytes -> {
                            RandomAccessReadBuffer pdfSource = new RandomAccessReadBuffer(pdfBytes);
                            pdfSources.add(pdfSource);
                            mergerUtility.addSource(pdfSource);
                        });
            }

            if (pdfSources.isEmpty()) {
                throw new ZipConversionException("ZIP archive does not contain supported files: "
                        + context.originalFileName());
            }

            mergerUtility.setDestinationStream(outputStream);
            mergerUtility.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
            return outputStream.toByteArray();
        } finally {
            closePdfSources(pdfSources);
        }
    }

    private Optional<byte[]> convertEntry(ZipInputStream zipInputStream,
                                          ZipEntry zipEntry,
                                          ConversionContext parentContext) throws IOException {
        Optional<FileType> fileType = fileTypeResolver.resolve(zipEntry.getName());
        if (fileType.isEmpty()) {
            log.warn("Skipping unsupported file inside ZIP: {}", zipEntry.getName());
            return Optional.empty();
        }

        FileConverter converter;
        try {
            converter = converterRegistryProvider.getObject().getConverter(fileType.get());
        } catch (ConverterNotFoundException ex) {
            log.warn("Skipping file inside ZIP because converter is not found. file={}, type={}",
                    zipEntry.getName(), fileType.get());
            return Optional.empty();
        }

        byte[] entryContent = zipInputStream.readAllBytes();
        ConversionContext entryContext = new ConversionContext(
                zipEntry.getName(),
                parentContext.correlationId(),
                fileType.get()
        );

        return Optional.of(converter.convertToPdf(new ByteArrayInputStream(entryContent), entryContext));
    }

    private void closePdfSources(List<RandomAccessReadBuffer> pdfSources) {
        for (RandomAccessReadBuffer pdfSource : pdfSources) {
            try {
                pdfSource.close();
            } catch (IOException ex) {
                log.warn("Failed to close temporary PDF source while converting ZIP", ex);
            }
        }
    }
}

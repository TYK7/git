package com.example.qachatbot.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Service
public class DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

    // We can make the document name configurable via application.properties
    @Value("${app.document.name:docs/sample_document.txt}") // Default to sample_document.txt
    private String documentName;

    private String storedDocumentContent;

    @PostConstruct
    public void init() {
        LOGGER.info("Attempting to load and parse document: {}", documentName);
        try {
            ClassPathResource resource = new ClassPathResource(documentName);
            if (!resource.exists()) {
                LOGGER.error("Document not found in classpath: {}", documentName);
                storedDocumentContent = "Error: Document not found (" + documentName + "). Please ensure it's in src/main/resources.";
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                Tika tika = new Tika();
                storedDocumentContent = tika.parseToString(inputStream);
                LOGGER.info("Document loaded and parsed successfully. Content length: {} characters.", storedDocumentContent.length());
                // For security and simplicity, we might want to log only a snippet or hash in real scenarios
                // LOGGER.debug("Document content snippet: {}", storedDocumentContent.substring(0, Math.min(storedDocumentContent.length(), 200)));
            }
        } catch (IOException e) {
            LOGGER.error("IOException while loading document: {}", documentName, e);
            storedDocumentContent = "Error: Could not read document due to IOException.";
        } catch (TikaException e) {
            LOGGER.error("TikaException while parsing document: {}", documentName, e);
            storedDocumentContent = "Error: Could not parse document content.";
        } catch (Exception e) {
            LOGGER.error("Unexpected error while loading/parsing document: {}", documentName, e);
            storedDocumentContent = "Error: An unexpected error occurred while processing the document.";
        }
    }

    public String getDocumentContent() {
        if (storedDocumentContent == null || storedDocumentContent.startsWith("Error:")) {
            LOGGER.warn("Document content is not available or contains an error: {}", storedDocumentContent);
        }
        return storedDocumentContent;
    }
}

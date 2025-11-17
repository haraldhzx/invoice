package com.invoiceapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {

    /**
     * Store a file and return the storage key
     */
    String store(MultipartFile file, String folder) throws IOException;

    /**
     * Store a file from InputStream and return the storage key
     */
    String store(InputStream inputStream, String fileName, String contentType, long size, String folder) throws IOException;

    /**
     * Get a file as InputStream
     */
    InputStream get(String storageKey) throws IOException;

    /**
     * Get a signed/public URL for a file
     */
    String getUrl(String storageKey);

    /**
     * Delete a file
     */
    void delete(String storageKey) throws IOException;

    /**
     * Check if a file exists
     */
    boolean exists(String storageKey);
}

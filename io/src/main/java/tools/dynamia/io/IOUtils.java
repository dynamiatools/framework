/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.io;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.io.impl.SpringResourceLocator;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * IOUtils is a utility class that provides a wide range of static methods for file and stream manipulation,
 * resource location, serialization, encoding, and other common I/O operations. It is designed to simplify
 * working with files, directories, streams, and resources in Java applications, offering convenience methods
 * for reading, writing, copying, formatting, and more. This class is not intended to be instantiated.
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Resource location and retrieval from classpath or file system</li>
 *     <li>File and stream copying using efficient NIO channels</li>
 *     <li>Serialization and deserialization of objects</li>
 *     <li>Base64 encoding and decoding for files</li>
 *     <li>File size formatting and file name utilities</li>
 *     <li>Directory and file management (delete, unzip, download, etc.)</li>
 *     <li>Additional utilities for working with {@link File} and {@link Path}</li>
 * </ul>
 *
 * @author Mario A. Serrano Leones
 */
public abstract class IOUtils {

    /**
     * The default resource locator.
     */
    private static final ResourceLocator DEFAULT_RESOURCE_LOCATOR;
    private static final LoggingService LOGGER = new SLF4JLoggingService(IOUtils.class);

    /**
     * Instantiates a new IO utils.
     */
    private IOUtils() {
    }

    static {
        SpringResourceLocator locator = new SpringResourceLocator();
        locator.setApplicationContext(new AnnotationConfigApplicationContext());
        DEFAULT_RESOURCE_LOCATOR = locator;
    }

    /**
     * Gets the resource for the given location using available ResourceLocators.
     * If no custom locator is found, uses the default SpringResourceLocator.
     *
     * @param location the resource location (classpath, file path, URL, etc.)
     * @return the located Resource, or null if not found
     */
    public static Resource getResource(String location) {
        Resource resource = null;
        Collection<ResourceLocator> locators = Containers.get().findObjects(ResourceLocator.class);
        if (locators != null && !locators.isEmpty()) {
            for (ResourceLocator resourceLocator : locators) {
                resource = resourceLocator.getResource(location);
                if (resource != null) {
                    break;
                }
            }
        } else {
            resource = DEFAULT_RESOURCE_LOCATOR.getResource(location);
        }
        return resource;
    }

    /**
     * Gets all resources matching the given location using available ResourceLocators.
     * If no custom locator is found, uses the default SpringResourceLocator.
     *
     * @param location the resource location (classpath, file path, URL, etc.)
     * @return an array of located Resources
     * @throws IOException if an I/O error occurs
     */
    public static Resource[] getResources(String location) throws IOException {
        Resource[] results = new Resource[0];
        Collection<ResourceLocator> locators = Containers.get().findObjects(ResourceLocator.class);
        if (locators != null && !locators.isEmpty()) {
            for (ResourceLocator resourceLocator : locators) {
                Resource[] resources = resourceLocator.getResources(location);
                if (resources != null) {
                    results = resources;
                    break;
                }
            }
        } else {
            results = DEFAULT_RESOURCE_LOCATOR.getResources(location);
        }

        return results;
    }

    /**
     * Creates a File object from a classpath resource.
     * Example: path = "/java/lang/String.class"
     *
     * @param path the classpath resource path
     * @return the File object, or null if not found or error
     */
    public static File createFromClasspath(final String path) {
        try {
            return new File(IOUtils.class.getResource(path).toURI());
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    /**
     * Serializes an object to an in-memory byte array.
     *
     * @param obj the Serializable object
     * @return the byte array representing the serialized object
     * @throws IOException if an I/O error occurs during serialization
     */
    public static byte[] serializeToBytes(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        return baos.toByteArray();
    }

    /**
     * Deserializes an object from a byte array.
     *
     * @param data the byte array containing the serialized object
     * @return the deserialized Serializable object
     * @throws IOException            if an I/O error occurs during deserialization
     * @throws ClassNotFoundException if the class of the object cannot be found
     */
    public static Serializable deserializeFromBytes(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Serializable) ois.readObject();
    }

    /**
     * Reads the content of a file from the classpath or file system as a String using the default charset.
     *
     * @param path the file path or classpath resource
     * @return the file content as a String
     * @throws IOException if an I/O error occurs
     */
    public static String readContent(String path) throws IOException {
        InputStream in = IOUtils.class.getResourceAsStream(path);
        if (in == null) {
            in = new FileInputStream(path);
        }

        return readContent(in, Charset.defaultCharset());
    }

    /**
     * Reads the content of a file from the classpath or file system as a String using the specified charset.
     *
     * @param path    the file path or classpath resource
     * @param charset the charset name
     * @return the file content as a String
     * @throws IOException if an I/O error occurs
     */
    public static String readContent(String path, String charset) throws IOException {
        InputStream in = IOUtils.class.getResourceAsStream(path);
        if (in == null) {
            in = new FileInputStream(path);
        }

        return readContent(in, Charset.forName(charset));
    }

    /**
     * Reads the content of an InputStream as a String using the specified charset.
     *
     * @param inputStream the InputStream to read
     * @param charset     the charset to use
     * @return the content as a String
     * @throws IOException if an I/O error occurs
     */
    public static String readContent(InputStream inputStream, Charset charset) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        }

        return content.toString();
    }

    /**
     * Copies data from an InputStream to an OutputStream using NIO channels for fast transfer.
     *
     * @param streamIn  the source InputStream
     * @param streamOut the destination OutputStream
     * @throws IOException if an I/O error occurs
     */
    public static void copy(InputStream streamIn, OutputStream streamOut) throws IOException {
        ReadableByteChannel src = Channels.newChannel(streamIn);
        WritableByteChannel dest = Channels.newChannel(streamOut);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

        while (src.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();
            // write to the channel, may block
            dest.write(buffer);
            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();
        }
        // EOF will leave buffer in fill state
        buffer.flip();
        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }

    }

    /**
     * Copies data from an InputStream to a File.
     *
     * @param streamIn the source InputStream
     * @param fileOut  the destination File
     * @throws IOException if an I/O error occurs
     */
    public static void copy(InputStream streamIn, File fileOut) throws IOException {
        copy(streamIn, new FileOutputStream(fileOut));
    }

    /**
     * Copies data from one File to another.
     *
     * @param fileIn  the source File
     * @param fileOut the destination File
     * @throws IOException if an I/O error occurs
     */
    public static void copy(File fileIn, File fileOut) throws IOException {
        FileInputStream fis = new FileInputStream(fileIn);
        FileOutputStream fos = new FileOutputStream(fileOut);
        copy(fis, fos);

    }

    /**
     * Copies a byte array to a File.
     *
     * @param bytes   the source byte array
     * @param fileOut the destination File
     * @throws IOException if an I/O error occurs
     */
    public static void copy(byte[] bytes, File fileOut) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        FileOutputStream fos = new FileOutputStream(fileOut);
        copy(bais, fos);

    }

    /**
     * Copies a byte array to an OutputStream.
     *
     * @param bytes     the source byte array
     * @param streamOut the destination OutputStream
     * @throws IOException if an I/O error occurs
     */
    public static void copy(byte[] bytes, OutputStream streamOut) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        copy(bais, streamOut);
    }

    /**
     * Formats a file size in bytes to a human-readable string (B, KB, MB, GB).
     *
     * @param length the file size in bytes
     * @return the formatted file size string
     */
    public static String formatFileSize(long length) {
        DecimalFormat f = new DecimalFormat("###,###.#");
        if (length > 1024) {
            double kb = (double) length / 1024;
            if (kb > 1024) {
                double mb = kb / 1024;
                if (mb > 1024) {
                    double gb = mb / 1024;
                    return f.format(gb) + " GB";
                } else {
                    return f.format(mb) + " MB";
                }
            } else {
                return f.format(kb) + " KB";
            }
        } else {
            return length + " B";
        }
    }

    /**
     * Gets the file name without its extension.
     *
     * @param file the File object
     * @return the file name without extension
     */
    public static String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        if (name.contains(".")) {
            return name.substring(0, name.lastIndexOf("."));
        } else {
            return name;
        }

    }

    /**
     * Gets the file extension from a File object.
     *
     * @param file the File object
     * @return the file extension (without dot)
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * Finds a FileInfo object by name in a list of FileInfo objects.
     *
     * @param name     the file name to search for
     * @param fileList the list of FileInfo objects
     * @return the matching FileInfo, or null if not found
     */
    public static FileInfo find(String name, List<FileInfo> fileList) {
        for (FileInfo fileInfo : fileList) {
            if (fileInfo.getName().equals(name)) {
                return fileInfo;
            }
        }
        return null;
    }

    /**
     * Unzips a specific zip file into the given output folder.
     *
     * @param zipFile      the zip file to unzip
     * @param outputfolder the output directory
     * @throws IOException if an I/O error occurs
     */
    public static void unzipFile(File zipFile, File outputfolder) throws IOException {
        byte[] buffer = new byte[1024];

        // create output directory is not exists
        if (!outputfolder.exists()) {
            outputfolder.mkdir();
        }

        // get the zipped file list entry
        try ( // get the zip file content
              ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputfolder, fileName);
                // create all non exists folders
                // else you will hit FileNotFoundException for compressed folder
                if (ze.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    /**
     * Returns the path to the system temporary directory.
     *
     * @return the system temporary directory path
     */
    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Returns a File representing the system temporary directory.
     *
     * @return the system temporary directory as a File
     */
    public static File getTempDirectory() {
        return new File(getTempDirectoryPath());
    }

    /**
     * Returns the path to the user's home directory.
     *
     * @return the user's home directory path
     */
    public static String getUserDirectoryPath() {
        return System.getProperty("user.home");
    }

    /**
     * Returns a File representing the user's home directory.
     *
     * @return the user's home directory as a File
     */
    public static File getUserDirectory() {
        return new File(getUserDirectoryPath());
    }

    /**
     * Deletes a directory recursively, including all its files and subdirectories.
     *
     * @param directory the directory to delete
     * @return true if the directory was deleted, false otherwise
     */
    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    /**
     * Downloads a file from a URL to a local folder. Overwrites existing file if present.
     *
     * @param baseURL     the base URL
     * @param fileURI     the file URI
     * @param localFolder the local folder path
     * @return the Path to the downloaded file, or null if failed
     * @throws Exception if an error occurs
     */
    public static Path downloadFile(String baseURL, final String fileURI, final String localFolder) throws Exception {

        if (baseURL == null || baseURL.isEmpty()) {
            LOGGER.info("-No base URL  to download file: " + fileURI);
            return null;
        }

        if (fileURI != null && !fileURI.isEmpty()) {

            String separator = "/";
            if (baseURL.endsWith("/")) {
                separator = "";
            }

            final URL url = URI.create(baseURL + separator + fileURI).toURL();
            final Path folder = Paths.get(localFolder);
            final Path localFile = folder.resolve(fileURI);

            if (Files.notExists(folder)) {
                Files.createDirectories(folder);
            }

            try (InputStream in = url.openStream()) {
                Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);
                return localFile;
            } catch (IOException ex) {
                LOGGER.error("-Error downloading file " + fileURI, ex);
            }
        } else {
            LOGGER.info("-No file to download");
        }
        return null;
    }

    /**
     * Downloads a file from a URL and saves it to the specified destination using Path.
     * Uses NIO for greater efficiency.
     *
     * @param url      the URL of the file to download
     * @param destPath the destination Path to save the file
     * @return the Path to the downloaded file, or null if it fails
     * @throws IOException if an I/O error occurs
     */
    public static Path downloadFile(URL url, Path destPath) throws IOException {
        Objects.requireNonNull(url, "URL cannot be null");
        Objects.requireNonNull(destPath, "Destination path cannot be null");
        try (InputStream in = url.openStream()) {
            Files.createDirectories(destPath.getParent());
            Files.copy(in, destPath, StandardCopyOption.REPLACE_EXISTING);
            return destPath;
        }
    }

    /**
     * Downloads multiple files from a list of URLs to a local folder. Overwrites existing files if present.
     *
     * @param urls     the list of URLs to download
     * @param destPath the local folder path
     * @return a list of Paths to the downloaded files
     * @throws IOException if an I/O error occurs
     */
    public static List<Path> downloadFiles(List<URL> urls, Path destPath) throws IOException {
        List<Path> downloadedFiles = new java.util.ArrayList<>();

        if (Files.notExists(destPath)) {
            Files.createDirectories(destPath);
        }
        for (URL url : urls) {
            String fileName = Paths.get(url.getPath()).getFileName().toString();
            Path localFile = destPath.resolve(fileName);
            try (InputStream in = url.openStream()) {
                Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);
                downloadedFiles.add(localFile);
            }
        }
        return downloadedFiles;
    }

    /**
     * Encodes the contents of a file to a Base64 String.
     *
     * @param file the File to encode
     * @return the Base64-encoded String
     * @throws IOException if an I/O error occurs
     */
    public static String encodeBase64(File file) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    }

    /**
     * Decodes a Base64 String and writes the result to a file.
     *
     * @param base64     the Base64 String
     * @param outputFile the output File
     * @throws IOException if an I/O error occurs
     */
    public static void decodeBase64(String base64, File outputFile) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] data = Base64.getDecoder().decode(base64.getBytes());
            outputStream.write(data);
        }
    }

    /**
     * Gets the size of a file in bytes.
     *
     * @param file the File object
     * @return the file size in bytes, or -1 if file does not exist
     */
    public static long getFileSize(File file) {
        return (file != null && file.exists()) ? file.length() : -1;
    }

    /**
     * Lists all files (not directories) in a given directory.
     *
     * @param directory the directory to list files from
     * @return a list of Files, or empty list if none
     */
    public static List<File> listFiles(File directory) {
        if (directory != null && directory.isDirectory()) {
            return List.of(Objects.requireNonNull(directory.listFiles(File::isFile)));
        }
        return List.of();
    }

    /**
     * Lists all directories in a given directory.
     *
     * @param directory the directory to list subdirectories from
     * @return an list of Files representing directories, or empty list if none
     */
    public static List<File> listDirectories(File directory) {
        if (directory != null && directory.isDirectory()) {
            return List.of(Objects.requireNonNull(directory.listFiles(File::isDirectory)));
        }
        return List.of();
    }

    /**
     * Moves a file from source to target location.
     *
     * @param source the source File
     * @param target the target File
     * @throws IOException if an I/O error occurs
     */
    public static void moveFile(File source, File target) throws IOException {
        if (source != null && target != null) {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Reads all lines from a file as a List of Strings using the default charset.
     *
     * @param file the File to read
     * @return a List of lines, or empty list if file does not exist
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLines(File file) throws IOException {
        if (file != null && file.exists()) {
            return Files.readAllLines(file.toPath());
        }
        return List.of();
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param file the File to check
     * @return true if exists, false otherwise
     */
    public static boolean exists(File file) {
        return file != null && file.exists();
    }

    /**
     * Gets the last modified date of a file.
     *
     * @param file the File object
     * @return the last modified time in milliseconds, or -1 if file does not exist
     */
    public static long getLastModified(File file) {
        return (file != null && file.exists()) ? file.lastModified() : -1;
    }

    /**
     * Creates an empty file at the specified location.
     *
     * @param file the File to create
     * @return true if file was created, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public static boolean createEmptyFile(File file) throws IOException {
        if (file != null) {
            return file.createNewFile();
        }
        return false;
    }

    /**
     * Gets the absolute Path of a File.
     *
     * @param file the File object
     * @return the absolute Path, or null if file is null
     */
    public static Path getAbsolutePath(File file) {
        return (file != null) ? file.toPath().toAbsolutePath() : null;
    }

}

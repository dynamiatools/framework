/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
import org.springframework.util.Base64Utils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.io.impl.SpringResourceLocator;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * The Class IOUtils.
 *
 * @author Mario A. Serrano Leones
 */
public abstract class IOUtils {

    /**
     * The default resource locator.
     */
    private static ResourceLocator DEFAULT_RESOURCE_LOCATOR;
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
     * Gets the resource.
     *
     * @param location the location
     * @return the resource
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
     * Gets the resources.
     *
     * @param location the location
     * @return the resources
     * @throws IOException Signals that an I/O exception has occurred.
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
     * Crear a File object in the classpath using the path parameter example.
     * path = /java/lang/String.class
     *
     * @param path the path
     * @return the file
     */
    public static File createFromClasspath(final String path) {
        try {
            return new File(IOUtils.class.getResource(path).toURI());
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    /**
     * Serialize an object to an in memory array of bytes.
     *
     * @param obj the obj
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static byte[] serializeToBytes(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        return baos.toByteArray();
    }

    /**
     * Deserialize an objet from an array of bytes.
     *
     * @param data the data
     * @return the serializable
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static Serializable deserializeFromBytes(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Serializable) ois.readObject();
    }

    /**
     * Read the file content from the classpath or file system if is not found
     * in classpath.
     *
     * @param path the path
     * @return the string
     * @throws FileNotFoundException the file not found exception
     * @throws IOException           Signals that an I/O exception has occurred.
     */
    public static String readContent(String path) throws IOException {
        InputStream in = IOUtils.class.getResourceAsStream(path);
        if (in == null) {
            in = new FileInputStream(path);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder content = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            content.append(line);
        }

        return content.toString();
    }

    public static String readContent(String path, String charset) throws IOException {
        InputStream in = IOUtils.class.getResourceAsStream(path);
        if (in == null) {
            in = new FileInputStream(path);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(in, charset));
        StringBuilder content = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            content.append(line);
        }

        return content.toString();
    }

    /**
     * Perform a fast copy using java.nio.Channel
     *
     * @param streamIn  the stream in
     * @param streamOut the stream out
     * @throws IOException Signals that an I/O exception has occurred.
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
     * Copy.
     *
     * @param streamIn the stream in
     * @param fileOut  the file out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copy(InputStream streamIn, File fileOut) throws IOException {
        copy(streamIn, new FileOutputStream(fileOut));
    }

    /**
     * Copy.
     *
     * @param fileIn  the file in
     * @param fileOut the file out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copy(File fileIn, File fileOut) throws IOException {
        FileInputStream fis = new FileInputStream(fileIn);
        FileOutputStream fos = new FileOutputStream(fileOut);
        copy(fis, fos);

    }

    /**
     * Copy.
     *
     * @param bytes   the bytes
     * @param fileOut the file out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copy(byte[] bytes, File fileOut) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        FileOutputStream fos = new FileOutputStream(fileOut);
        copy(bais, fos);

    }

    /**
     * Copy.
     *
     * @param bytes     the bytes
     * @param streamOut the stream out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copy(byte[] bytes, OutputStream streamOut) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        copy(bais, streamOut);
    }

    /**
     * Format file size.
     *
     * @param length the length
     * @return the string
     */
    public static String formatFileSize(long length) {
        DecimalFormat f = new DecimalFormat("###,###.#");
        if (length > 1024) {
            double kb = length / 1024;
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
     * Gets the file name without extension.
     *
     * @param file the file
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
     * Gets the file extension.
     *
     * @param file the file
     * @return the file extension
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * Find.
     *
     * @param name     the name
     * @param fileList the file list
     * @return the file info
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
     * Unzip specific file in outputfolder
     *
     * @param zipFile
     * @param outputfolder
     * @throws IOException
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
     * @return the path to the system temporary directory.
     */
    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Returns a {@link File} representing the system temporary directory.
     *
     * @return the system temporary directory.
     */
    public static File getTempDirectory() {
        return new File(getTempDirectoryPath());
    }

    /**
     * Returns the path to the user's home directory.
     *
     * @return the path to the user's home directory.
     */
    public static String getUserDirectoryPath() {
        return System.getProperty("user.home");
    }

    /**
     * Returns a {@link File} representing the user's home directory.
     *
     * @return the user's home directory.
     */
    public static File getUserDirectory() {
        return new File(getUserDirectoryPath());
    }

    /**
     * Delete a directory recursively
     *
     * @param directory
     * @return
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
     * Download file from URL to local folder. If file exist overwrite existing file.
     *
     * @param baseURL
     * @param fileURI
     * @param localFolder
     * @throws Exception
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

            final URL url = new URL(baseURL + separator + fileURI);
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
     * Encode a file bytes to Base64 String
     *
     * @param file
     * @return
     */
    public static String encodeBase64(File file) throws IOException {
        return Base64Utils.encodeToString(Files.readAllBytes(file.toPath()));
    }


    /**
     * Decode Base64 string to file
     *
     * @param base64
     * @param outputFile
     * @throws IOException
     */
    public static void decodeBase64(String base64, File outputFile) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] data = Base64.getDecoder().decode(base64.getBytes());
            outputStream.write(data);
        }
    }

}

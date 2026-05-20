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

package tools.dynamia.modules.entityfile.remote;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;
import tools.dynamia.domain.InMemoryCrudService;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.domain.enums.EntityFileState;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.modules.entityfile.local.LocalEntityFileStorage;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for {@link BuckieEntityFileStorage}.
 *
 * <p>Pure-logic tests (buildKey, getFileName, etc.) always run.
 * HTTP tests are skipped automatically via {@code Assume.assumeTrue}
 * when the SFS server is not reachable.</p>
 *
 * <p>Server configuration via system properties or environment variables:
 * <ul>
 *   <li>{@code SFS_URL}      – SFS server base URL (default: {@code http://localhost:8081})</li>
 *   <li>{@code SFS_BUCKET}   – bucket name         (default: {@code test})</li>
 *   <li>{@code SFS_IDENTITY} – SFS identity        (default: empty)</li>
 *   <li>{@code SFS_SECRET}   – SFS secret          (default: empty)</li>
 * </ul>
 * Maven example: {@code mvn test -DSFS_URL=http://my-sfs:8081 -DSFS_BUCKET=test}
 * </p>
 */
public class BuckieEntityFileStorageTest {

    private static String sfsUrl;
    private static String sfsBucket;
    private static String sfsIdentity;
    private static String sfsSecret;

    private BuckieEntityFileStorage storage;

    // ── Setup ─────────────────────────────────────────────────────────────────

    @BeforeClass
    public static void readConfiguration() {
        sfsUrl = systemOrEnv(BuckieEntityFileStorage.SFS_URL, "http://localhost:8500");
        sfsBucket = systemOrEnv(BuckieEntityFileStorage.SFS_BUCKET, "test");
        sfsIdentity = systemOrEnv(BuckieEntityFileStorage.SFS_IDENTITY, "test");
        sfsSecret = systemOrEnv(BuckieEntityFileStorage.SFS_SECRET, "test");

        System.out.println("[SFS Test] URL=" + sfsUrl + " | BUCKET=" + sfsBucket);
    }

    @Before
    public void setUp() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty(BuckieEntityFileStorage.SFS_URL, sfsUrl);
        env.setProperty(BuckieEntityFileStorage.SFS_BUCKET, sfsBucket);
        env.setProperty(BuckieEntityFileStorage.SFS_IDENTITY, sfsIdentity);
        env.setProperty(BuckieEntityFileStorage.SFS_SECRET, sfsSecret);

        var local = new LocalEntityFileStorage(noOpParameters(), new InMemoryCrudService(), env);

        storage = new BuckieEntityFileStorage(local, noOpParameters(), new InMemoryCrudService(), env);
    }

    // ── Pure-logic tests (no server required) ─────────────────────────────────

    @Test
    public void testGetId() {
        assertEquals(BuckieEntityFileStorage.ID, storage.getId());
    }

    @Test
    public void testGetName() {
        assertNotNull(storage.getName());
        assertFalse("Storage name must not be blank", storage.getName().isBlank());
    }

    @Test
    public void testBuildKey_withoutSubfolder() {
        EntityFile ef = buildEntityFile("report.pdf", null, 10L);
        String key = storage.buildKey(ef);

        assertTrue("Key must start with account10/", key.startsWith("account10/"));
        assertTrue("Key must contain the uuid", key.contains(ef.getUuid()));
    }

    @Test
    public void testBuildKey_withSubfolder() {
        EntityFile ef = buildEntityFile("image.jpg", "photos/2026", 5L);
        String key = storage.buildKey(ef);

        assertTrue("Key must start with account5/", key.startsWith("account5/"));
        assertTrue("Key must contain the subfolder path", key.contains("photos/2026/"));
    }

    @Test
    public void testGetFileName_withSpacesAndDashes() {
        EntityFile ef = buildEntityFile("My File-Final.pdf", null, 1L);
        String name = BuckieEntityFileStorage.getFileName(ef);

        assertFalse("File name must not contain spaces", name.contains(" "));
        assertFalse("File name base must not contain dashes",
                name.substring(name.lastIndexOf('/') + 1).replace(ef.getUuid(), "").contains("-"));
    }

    @Test
    public void testGetFileName_withAccentsAndSpecialChars() {
        EntityFile ef = buildEntityFile("Ñoño Ávido Murió.pdf", null, 1L);
        String name = BuckieEntityFileStorage.getFileName(ef);

        assertFalse("File name must not contain ñ", name.contains("ñ"));
        assertFalse("File name must not contain á", name.contains("á"));
        assertFalse("File name must not contain ó", name.contains("ó"));
        assertFalse("File name must not contain spaces", name.contains(" "));
    }

    @Test
    public void testGetFileName_usesStoredFileNameWhenSet() {
        EntityFile ef = buildEntityFile("original.pdf", null, 1L);
        ef.setStoredFileName("custom_stored_name.pdf");

        String name = BuckieEntityFileStorage.getFileName(ef);

        assertEquals("Must use storedFileName when it is set", "custom_stored_name.pdf", name);
    }

    @Test
    public void testGetFileName_withoutSubfolder() {
        EntityFile ef = buildEntityFile("doc.txt", null, 1L);
        String name = BuckieEntityFileStorage.getFileName(ef);

        assertFalse("Without subfolder the name must not start with /", name.startsWith("/"));
        assertTrue("Name must contain the uuid", name.contains(ef.getUuid()));
    }

    @Test
    public void testGetAccountFolderName() {
        assertEquals("account42/", BuckieEntityFileStorage.getAccountFolderName(42L));
        assertEquals("account1/", BuckieEntityFileStorage.getAccountFolderName(1L));
        assertEquals("account999/", BuckieEntityFileStorage.getAccountFolderName(999L));
    }

    @Test
    public void testBuildRemoteUrl_containsUrlBucketAndKey() {
        EntityFile ef = buildEntityFile("document.pdf", null, 3L);
        String url = storage.buildRemoteUrl(ef);

        assertTrue("URL must start with the SFS base URL", url.startsWith(sfsUrl));
        assertTrue("URL must contain the bucket name", url.contains(sfsBucket));
        assertTrue("URL must contain the account folder", url.contains("account3/"));
        assertTrue("URL must contain the file uuid", url.contains(ef.getUuid()));
    }

    @Test
    public void testDownload_returnsRemoteStoredEntityFile() {
        EntityFile ef = buildEntityFile("file.txt", null, 1L);
        StoredEntityFile stored = storage.download(ef);

        assertNotNull("StoredEntityFile must not be null", stored);
        assertNotNull("URL must not be null", stored.getUrl());
        assertNull("Remote file must not have a local real file", stored.getRealFile());
    }

    @Test
    public void testThumbnailUrl_containsDimensionParameters() {
        EntityFile ef = buildEntityFile("photo.jpg", null, 1L);
        StoredEntityFile stored = storage.download(ef);

        String thumb100 = stored.getThumbnailUrl(100, 100);
        assertTrue("Thumbnail URL must contain w=100", thumb100.contains("w=100"));
        assertTrue("Thumbnail URL must contain h=100", thumb100.contains("h=100"));

        String thumb200 = stored.getThumbnailUrl(200, 300);
        assertTrue("Thumbnail URL must contain w=200", thumb200.contains("w=200"));
        assertTrue("Thumbnail URL must contain h=300", thumb200.contains("h=300"));
    }

    @Test
    public void testReloadParams_resetsAndRebuildsClient() {
        // Force initial client build
        storage.client();

        // reloadParams must clear the internal client
        storage.reloadParams();

        // First call after reload must rebuild the client without throwing
        assertNotNull("Client must be rebuilt after reloadParams", storage.client());
    }

    @Test
    public void testToResource_returnsInputStreamResource() {
        Assume.assumeTrue("SFS server not available at " + sfsUrl, isServerReachable());

        // Upload a file first so the URL is actually retrievable
        EntityFile ef = buildEntityFile("to-resource-" + System.currentTimeMillis() + ".txt", null, 1L);
        byte[] bytes = "toResource content".getBytes(StandardCharsets.UTF_8);
        UploadedFileInfo info = new UploadedFileInfo(ef.getName(), "text/plain", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);
        storage.upload(ef, info);

        StoredEntityFile stored = storage.download(ef);
        // toResource() must authenticate with SFS and return an InputStreamResource
        assertNotNull("toResource() must not throw or return null", stored.toResource());
    }

    @Test
    public void testToThumbnailResource_returnsInputStreamResource() {
        Assume.assumeTrue("SFS server not available at " + sfsUrl, isServerReachable());

        EntityFile ef = buildEntityFile("to-thumb-" + System.currentTimeMillis() + ".png", null, 1L);
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xD8}; // minimal JPEG-like stub
        UploadedFileInfo info = new UploadedFileInfo(ef.getName(), "image/png", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);
        storage.upload(ef, info);

        StoredEntityFile stored = storage.download(ef);
        // toThumbnailResource() must authenticate with SFS and return an InputStreamResource
        assertNotNull("toThumbnailResource() must not throw or return null",
                stored.toThumbnailResource(200, 200));
    }

    // ── Integration tests (require a live SFS server) ─────────────────────────

    @Test
    public void testUpload_textFile() {
        Assume.assumeTrue("SFS server not available at " + sfsUrl, isServerReachable());

        EntityFile ef = buildEntityFile("test-upload-" + System.currentTimeMillis() + ".txt", null, 1L);
        String content = "Hello SFS from automated test - " + System.currentTimeMillis();
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        UploadedFileInfo info = new UploadedFileInfo(
                ef.getName(), "text/plain", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);

        storage.upload(ef, info);

        assertTrue("File size must be > 0 after a successful upload", ef.getSize() > 0);
    }

    @Test
    public void testUpload_withSubfolder() {
        Assume.assumeTrue("SFS server not available at " + sfsUrl, isServerReachable());

        EntityFile ef = buildEntityFile("document.txt", "subfolder/tests", 1L);
        byte[] bytes = "content with subfolder".getBytes(StandardCharsets.UTF_8);

        UploadedFileInfo info = new UploadedFileInfo(
                ef.getName(), "text/plain", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);

        storage.upload(ef, info);

        String key = storage.buildKey(ef);
        assertTrue("Key must include the subfolder path", key.contains("subfolder/tests/"));
    }

    @Test
    public void testUpload_nameWithSpacesDoesNotFail() {
        Assume.assumeTrue("SFS server not available at " + sfsUrl, isServerReachable());

        EntityFile ef = buildEntityFile("file with spaces and ñ.txt", null, 1L);
        byte[] bytes = "content".getBytes(StandardCharsets.UTF_8);

        UploadedFileInfo info = new UploadedFileInfo(
                ef.getName(), "text/plain", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);

        // Must not throw — the name is sanitised before being sent to SFS
        storage.upload(ef, info);
    }

    @Test
    public void testDelete_changesStateToDeleted() {
        Assume.assumeTrue("SFS server not available at " + sfsUrl, isServerReachable());

        // 1. Upload a file first
        EntityFile ef = buildEntityFile("test-delete-" + System.currentTimeMillis() + ".txt", null, 1L);
        byte[] bytes = "temporary file to delete".getBytes(StandardCharsets.UTF_8);

        UploadedFileInfo info = new UploadedFileInfo(
                ef.getName(), "text/plain", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);

        storage.upload(ef, info);

        // 2. Delete it
        storage.delete(ef);

        // 3. Verify state
        assertEquals("State must change to DELETED", EntityFileState.DELETED, ef.getState());
    }

    @Test
    public void testUploadAndDownloadUrl_areConsistent() {
        Assume.assumeTrue("SFS server not available at " + sfsUrl, isServerReachable());

        EntityFile ef = buildEntityFile("consistency-" + System.currentTimeMillis() + ".txt", null, 1L);
        byte[] bytes = "URL consistency check content".getBytes(StandardCharsets.UTF_8);

        UploadedFileInfo info = new UploadedFileInfo(
                ef.getName(), "text/plain", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);

        storage.upload(ef, info);

        StoredEntityFile stored = storage.download(ef);
        String url = stored instanceof BuckieEntityFileStorage.BuckieStoredEntityFile r ? r.getRemoteUrl() : stored.getUrl();

        // The URL returned by download() must point to the same resource that was uploaded
        assertNotNull(url);
        assertTrue("URL must contain the bucket name", url.contains(sfsBucket));
        assertTrue("URL must contain the key of the uploaded file", url.contains(storage.buildKey(ef)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a minimal {@link EntityFile} suitable for testing.
     */
    private EntityFile buildEntityFile(String name, String subfolder, Long accountId) {
        EntityFile ef = new EntityFile();
        ef.setName(name);
        ef.setSubfolder(subfolder);
        ef.setAccountId(accountId);
        ef.setType(EntityFileType.FILE);
        ef.setExtension("txt");
        ef.setTargetEntity("TestEntity");
        ef.setTargetEntityId(1L);
        return ef;
    }

    /**
     * Attempts to open a short-lived connection to the SFS server.
     * Returns {@code true} if the server responds, {@code false} otherwise.
     */
    private boolean isServerReachable() {
        try {
            HttpURLConnection con = (HttpURLConnection) URI.create(sfsUrl).toURL().openConnection();
            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);
            con.setRequestMethod("GET");
            con.connect();
            int responseCode = con.getResponseCode();
            con.disconnect();
            return responseCode > 0;
        } catch (Exception e) {
            System.out.println("[SFS Test] Server not reachable: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads a value from system properties ({@code -Dkey=value}) first,
     * then from environment variables, falling back to {@code defaultValue}.
     */
    private static String systemOrEnv(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) return value;
        value = System.getenv(key);
        if (value != null && !value.isBlank()) return value;
        return defaultValue;
    }

    /**
     * Minimal no-op implementation of {@link Parameters} used as a fallback.
     * In tests, {@link MockEnvironment} already supplies all SFS values, so
     * this implementation is never actually invoked except on unexpected errors.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Parameters noOpParameters() {
        return new Parameters() {
            @Override
            public List getParameters(List<String> n) {
                return List.of();
            }

            @Override
            public List getParameters(Class<? extends Parameter> c, List<String> n) {
                return List.of();
            }

            @Override
            public List all() {
                return List.of();
            }

            @Override
            public Parameter getParameter(String name) {
                return null;
            }

            @Override
            public String getValue(String p) {
                return null;
            }

            @Override
            public String getValue(Class<? extends Parameter> c, String p) {
                return null;
            }

            @Override
            public String getValue(String p, String def) {
                return def;
            }

            @Override
            public String getValue(Class<? extends Parameter> c, String p, String def) {
                return def;
            }

            @Override
            public void save(Parameter p) {
            }

            @Override
            public void save(Collection params) {
            }

            @Override
            public void setParameter(Class<? extends Parameter> c, String n, Object v) {
            }

            @Override
            public void setParameter(String n, Object v) {
            }

            @Override
            public Parameter getParameter(Class<? extends Parameter> c, String n) {
                return null;
            }

            @Override
            public void increaseCounter(Parameter p) {
            }

            @Override
            public long findNextCounterValue(Parameter p) {
                return 0;
            }

            @Override
            public Parameter findParameter(Class<? extends Parameter> c, String n, QueryParameters f) {
                return null;
            }
        };
    }
}


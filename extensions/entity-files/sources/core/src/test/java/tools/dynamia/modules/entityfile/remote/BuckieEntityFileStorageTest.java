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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link BuckieEntityFileStorage}.
 *
 * <p>Pure-logic tests (buildKey, getFileName, etc.) always run.
 * HTTP tests are skipped automatically via {@code Assumptions.assumeTrue}
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

    @BeforeAll
    public static void readConfiguration() {
        sfsUrl = systemOrEnv(BuckieEntityFileStorage.SFS_URL, "http://localhost:8500");
        sfsBucket = systemOrEnv(BuckieEntityFileStorage.SFS_BUCKET, "test");
        sfsIdentity = systemOrEnv(BuckieEntityFileStorage.SFS_IDENTITY, "test");
        sfsSecret = systemOrEnv(BuckieEntityFileStorage.SFS_SECRET, "test");

        System.out.println("[SFS Test] URL=" + sfsUrl + " | BUCKET=" + sfsBucket);
    }

    @BeforeEach
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
        assertFalse(storage.getName().isBlank(), "Storage name must not be blank");
    }

    @Test
    public void testBuildKey_withoutSubfolder() {
        EntityFile ef = buildEntityFile("report.pdf", null, 10L);
        String key = storage.buildKey(ef);

        assertTrue(key.startsWith("account10/"), "Key must start with account10/");
        assertTrue(key.contains(ef.getUuid()), "Key must contain the uuid");
    }

    @Test
    public void testBuildKey_withSubfolder() {
        EntityFile ef = buildEntityFile("image.jpg", "photos/2026", 5L);
        String key = storage.buildKey(ef);

        assertTrue(key.startsWith("account5/"), "Key must start with account5/");
        assertTrue(key.contains("photos/2026/"), "Key must contain the subfolder path");
    }

    @Test
    public void testGetFileName_withSpacesAndDashes() {
        EntityFile ef = buildEntityFile("My File-Final.pdf", null, 1L);
        String name = BuckieEntityFileStorage.getFileName(ef);

        assertFalse(name.contains(" "), "File name must not contain spaces");
        assertFalse(name.substring(name.lastIndexOf('/') + 1).replace(ef.getUuid(), "").contains("-"),
                "File name base must not contain dashes");
    }

    @Test
    public void testGetFileName_withAccentsAndSpecialChars() {
        EntityFile ef = buildEntityFile("Ñoño Ávido Murió.pdf", null, 1L);
        String name = BuckieEntityFileStorage.getFileName(ef);

        assertFalse(name.contains("ñ"), "File name must not contain ñ");
        assertFalse(name.contains("á"), "File name must not contain á");
        assertFalse(name.contains("ó"), "File name must not contain ó");
        assertFalse(name.contains(" "), "File name must not contain spaces");
    }

    @Test
    public void testGetFileName_usesStoredFileNameWhenSet() {
        EntityFile ef = buildEntityFile("original.pdf", null, 1L);
        ef.setStoredFileName("custom_stored_name.pdf");

        String name = BuckieEntityFileStorage.getFileName(ef);

        assertEquals("custom_stored_name.pdf", name, "Must use storedFileName when it is set");
    }

    @Test
    public void testGetFileName_withoutSubfolder() {
        EntityFile ef = buildEntityFile("doc.txt", null, 1L);
        String name = BuckieEntityFileStorage.getFileName(ef);

        assertFalse(name.startsWith("/"), "Without subfolder the name must not start with /");
        assertTrue(name.contains(ef.getUuid()), "Name must contain the uuid");
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

        assertTrue(url.startsWith(sfsUrl), "URL must start with the SFS base URL");
        assertTrue(url.contains(sfsBucket), "URL must contain the bucket name");
        assertTrue(url.contains("account3/"), "URL must contain the account folder");
        assertTrue(url.contains(ef.getUuid()), "URL must contain the file uuid");
    }

    @Test
    public void testDownload_returnsRemoteStoredEntityFile() {
        EntityFile ef = buildEntityFile("file.txt", null, 1L);
        StoredEntityFile stored = storage.download(ef);

        assertNotNull(stored, "StoredEntityFile must not be null");
        assertNotNull(stored.getUrl(), "URL must not be null");
        assertNull(stored.getRealFile(), "Remote file must not have a local real file");
    }

    @Test
    public void testThumbnailUrl_containsDimensionParameters() {
        EntityFile ef = buildEntityFile("photo.jpg", null, 1L);
        StoredEntityFile stored = storage.download(ef);

        String thumb100 = stored.getThumbnailUrl(100, 100);
        assertTrue(thumb100.contains("w=100"), "Thumbnail URL must contain w=100");
        assertTrue(thumb100.contains("h=100"), "Thumbnail URL must contain h=100");

        String thumb200 = stored.getThumbnailUrl(200, 300);
        assertTrue(thumb200.contains("w=200"), "Thumbnail URL must contain w=200");
        assertTrue(thumb200.contains("h=300"), "Thumbnail URL must contain h=300");
    }

    @Test
    public void testReloadParams_resetsAndRebuildsClient() {
        // Force initial client build
        storage.client();

        // reloadParams must clear the internal client
        storage.reloadParams();

        // First call after reload must rebuild the client without throwing
        assertNotNull(storage.client(), "Client must be rebuilt after reloadParams");
    }

    @Test
    public void testToResource_returnsInputStreamResource() {
        Assumptions.assumeTrue(isServerReachable(), "SFS server not available at " + sfsUrl);

        // Upload a file first so the URL is actually retrievable
        EntityFile ef = buildEntityFile("to-resource-" + System.currentTimeMillis() + ".txt", null, 1L);
        byte[] bytes = "toResource content".getBytes(StandardCharsets.UTF_8);
        UploadedFileInfo info = new UploadedFileInfo(ef.getName(), "text/plain", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);
        storage.upload(ef, info);

        StoredEntityFile stored = storage.download(ef);
        // toResource() must authenticate with SFS and return an InputStreamResource
        assertNotNull(stored.toResource(), "toResource() must not throw or return null");
    }

    @Test
    public void testToThumbnailResource_returnsInputStreamResource() {
        Assumptions.assumeTrue(isServerReachable(), "SFS server not available at " + sfsUrl);

        EntityFile ef = buildEntityFile("to-thumb-" + System.currentTimeMillis() + ".png", null, 1L);
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xD8}; // minimal JPEG-like stub
        UploadedFileInfo info = new UploadedFileInfo(ef.getName(), "image/png", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);
        storage.upload(ef, info);

        StoredEntityFile stored = storage.download(ef);
        // toThumbnailResource() must authenticate with SFS and return an InputStreamResource
        assertNotNull(stored.toThumbnailResource(200, 200),
                "toThumbnailResource() must not throw or return null");
    }

    // ── Integration tests (require a live SFS server) ─────────────────────────

    @Test
    public void testUpload_textFile() {
        Assumptions.assumeTrue(isServerReachable(), "SFS server not available at " + sfsUrl);

        EntityFile ef = buildEntityFile("test-upload-" + System.currentTimeMillis() + ".txt", null, 1L);
        String content = "Hello SFS from automated test - " + System.currentTimeMillis();
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        UploadedFileInfo info = new UploadedFileInfo(
                ef.getName(), "text/plain", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);

        storage.upload(ef, info);

        assertTrue(ef.getSize() > 0, "File size must be > 0 after a successful upload");
    }

    @Test
    public void testUpload_withSubfolder() {
        Assumptions.assumeTrue(isServerReachable(), "SFS server not available at " + sfsUrl);

        EntityFile ef = buildEntityFile("document.txt", "subfolder/tests", 1L);
        byte[] bytes = "content with subfolder".getBytes(StandardCharsets.UTF_8);

        UploadedFileInfo info = new UploadedFileInfo(
                ef.getName(), "text/plain", new ByteArrayInputStream(bytes));
        info.setLength(bytes.length);

        storage.upload(ef, info);

        String key = storage.buildKey(ef);
        assertTrue(key.contains("subfolder/tests/"), "Key must include the subfolder path");
    }

    @Test
    public void testUpload_nameWithSpacesDoesNotFail() {
        Assumptions.assumeTrue(isServerReachable(), "SFS server not available at " + sfsUrl);

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
        Assumptions.assumeTrue(isServerReachable(), "SFS server not available at " + sfsUrl);

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
        assertEquals(EntityFileState.DELETED, ef.getState(), "State must change to DELETED");
    }

    @Test
    public void testUploadAndDownloadUrl_areConsistent() {
        Assumptions.assumeTrue(isServerReachable(), "SFS server not available at " + sfsUrl);

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
        assertTrue(url.contains(sfsBucket), "URL must contain the bucket name");
        assertTrue(url.contains(storage.buildKey(ef)), "URL must contain the key of the uploaded file");
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

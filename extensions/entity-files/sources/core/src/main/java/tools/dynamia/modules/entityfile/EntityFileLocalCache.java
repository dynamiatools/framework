package tools.dynamia.modules.entityfile;

import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.integration.sterotypes.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

@Component
public class EntityFileLocalCache implements EntityFileCache {

    public static final String CACHE_EXTENSION = ".cache";
    private final LoggingService logger = LoggingService.get(EntityFileLocalCache.class);
    private final Path cacheDirectory;
    private final Duration defaultTtl;

    public EntityFileLocalCache(Environment env) {
        if (env != null) {
            String ttlStr = env.getProperty("entityfile.cache.ttl", Duration.ofHours(12).toString());
            String dirStr = env.getProperty("entityfile.cache.dir", Paths.get(System.getProperty("java.io.tmpdir"), "entityfile-cache").toString());
            defaultTtl = Duration.parse(ttlStr);
            cacheDirectory = Paths.get(dirStr);
        } else {
            defaultTtl = Duration.ofHours(12);
            cacheDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "entityfile-cache");
        }
    }


    @Override
    public Optional<Resource> get(String uuid, String etag) {
        Path file = resolvePath(uuid, etag);
        if (Files.exists(file)) {
            return Optional.of(new FileSystemResource(file));
        }
        return Optional.empty();
    }

    @Override
    public Resource put(String uuid, String etag, Resource resource) {
        try {
            if (uuid != null && etag != null && resource != null) {
                Files.createDirectories(cacheDirectory);
                Path dest = resolvePath(uuid, etag);
                try (InputStream in = resource.getInputStream()) {
                    Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                }
                return new FileSystemResource(dest);
            }
        } catch (IOException e) {
            logger.error("Failed to cache entity file for uuid: " + uuid + ", etag: " + etag, e);
        }
        return resource;
    }

    @Override
    public Path resolvePath(String uuid, String etag) {
        return cacheDirectory.resolve(uuid + "-" + etag + CACHE_EXTENSION);
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    @Override
    public void evict() {
        Duration ttl = resolveTtl();
        Instant cutoff = Instant.now().minus(ttl);
        deleteIf(p -> {
            try {
                return Files.getLastModifiedTime(p).toInstant().isBefore(cutoff);
            } catch (IOException e) {
                logger.error("Failed to check last modified time for cache file: " + p, e);
                return false;
            }
        });
    }

    @Override
    public void clear() {
        deleteIf(p -> true);
    }

    public void deleteIf(Function<Path, Boolean> condition) {
        if (!Files.exists(cacheDirectory)) return;
        SchedulerUtil.run(() -> {
            logger.info("Cleaning entity file cache in directory: " + cacheDirectory);
            try (var stream = Files.list(cacheDirectory)) {
                stream.forEach(p -> {
                    try {
                        if (p.getFileName().toString().endsWith(CACHE_EXTENSION) && condition.apply(p) == Boolean.TRUE) {
                            logger.info("Deleting cache file: " + p);
                            Files.delete(p);
                        }
                    } catch (IOException e) {
                        logger.error("Failed to delete cache file: " + p, e);
                    }
                });
            } catch (IOException e) {
                logger.error("Failed to list cache directory: " + cacheDirectory, e);
            }
        });
    }

    @Override
    public Duration resolveTtl() {
        return defaultTtl;
    }
}
